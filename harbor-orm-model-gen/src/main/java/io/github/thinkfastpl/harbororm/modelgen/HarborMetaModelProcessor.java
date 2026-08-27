// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import io.github.thinkfastpl.harbororm.api.metadata.EnumMappingType;
import io.github.thinkfastpl.harbororm.modelgen.interpreter.ConverterMetadataFactory;
import io.github.thinkfastpl.harbororm.modelgen.interpreter.SourceInterpreter;
import io.github.thinkfastpl.harbororm.modelgen.interpreter.SourceInterpreterResult;
import io.github.thinkfastpl.harbororm.modelgen.interpreter.TypeHandlerMetadataFactory;
import io.github.thinkfastpl.harbororm.modelgen.metadata.*;
import io.github.thinkfastpl.harbororm.modelgen.source.SourceTypeConstruct;
import io.github.thinkfastpl.harbororm.modelgen.source.processor.ProcessorSourceTypeConstruct;
import io.github.thinkfastpl.harbororm.modelgen.utils.StringUtils;
import io.github.thinkfastpl.harbororm.modelgen.writer.MetadataWriter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.*;

@SupportedAnnotationTypes({
        "io.github.thinkfastpl.harbororm.api.annotations.Entity",
        "io.github.thinkfastpl.harbororm.api.annotations.Embeddable",
        "io.github.thinkfastpl.harbororm.api.annotations.StoredFunction",
        "io.github.thinkfastpl.harbororm.api.annotations.View",
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedOptions({"io.github.thinkfastpl.harbororm.tables.package", "io.github.thinkfastpl.harbororm.log.sources"})
@Slf4j
public class HarborMetaModelProcessor extends AbstractProcessor {

    private MetadataWriter writer;

    private final Set<Element> entities = new HashSet<>();

    private final Set<Element> embeddables = new HashSet<>();

    private final Set<Element> storedFunctions = new HashSet<>();

    private final Set<Element> views = new HashSet<>();

    private String tablesPackage;

    private Exception lastInterpretException;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        boolean logSources = Boolean.parseBoolean(processingEnv.getOptions().get("io.github.thinkfastpl.harbororm.log.sources"));
        writer = new MetadataWriter(processingEnv.getFiler(), logSources);
        tablesPackage = processingEnv.getOptions().get("io.github.thinkfastpl.harbororm.tables.package");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        entities.addAll(roundEnv.getElementsAnnotatedWith(Entity.class));
        embeddables.addAll(roundEnv.getElementsAnnotatedWith(Embeddable.class));
        storedFunctions.addAll(roundEnv.getElementsAnnotatedWith(StoredFunction.class));
        views.addAll(roundEnv.getElementsAnnotatedWith(View.class));

        if (entities.isEmpty() && embeddables.isEmpty() && storedFunctions.isEmpty() && views.isEmpty()) {
            return false;
        }

        // Process entities and embeddables (existing logic — preserve exact original behavior)
        if (!entities.isEmpty() || !embeddables.isEmpty()) {
            final Optional<SourceInterpreterResult> resultOptional = interpret();
            if (resultOptional.isEmpty()) {
                // Interpretation not ready yet; defer to next round (do NOT clear entities).
                // Also defer stored functions so everything is processed in the same round.
                return false;
            }

            final SourceInterpreterResult result = resultOptional.get();

            for (EmbeddableMetadata embeddableMetadata : result.embeddableList()) {
                writer.writeEmbeddable(embeddableMetadata);
            }

            for (EntityMetadata entityMetadata : result.entities()) {
                writer.writeEntity(entityMetadata);
            }

            if (StringUtils.isNotBlank(tablesPackage)) {
                for (TableMetadata tableMetadata : result.extractTables(tablesPackage)) {
                    writer.writeTable(tableMetadata);
                }
            }

            entities.clear();
            embeddables.clear();
        }

        // Process stored functions (new logic — independent of entity/embeddable processing)
        if (!storedFunctions.isEmpty()) {
            for (Element element : storedFunctions) {
                if (element.getKind() == ElementKind.CLASS) {
                    try {
                        TypeElement typeElement = (TypeElement) element;
                        StoredFunctionMetadata sfMetadata = interpretStoredFunction(typeElement);
                        writer.writeStoredFunction(sfMetadata);
                    } catch (Exception e) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                "HarborORM: Failed to process @StoredFunction: " + e.getMessage(), element);
                    }
                }
            }
            storedFunctions.clear();
        }

        // Process views (independent of entity/embeddable processing)
        if (!views.isEmpty()) {
            for (Element element : views) {
                if (element.getKind() == ElementKind.CLASS) {
                    try {
                        TypeElement typeElement = (TypeElement) element;
                        ViewMetadata viewMetadata = interpretView(typeElement);
                        writer.writeView(viewMetadata);
                    } catch (Exception e) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                "HarborORM: Failed to process @View: " + e.getMessage(), element);
                    }
                }
            }
            views.clear();
        }

        return true;
    }

    private Optional<SourceInterpreterResult> interpret() {
        try {
            final List<SourceTypeConstruct> entityClasses = new ArrayList<>();
            final List<SourceTypeConstruct> embeddableClasses = new ArrayList<>();

            entities.stream()
                    .filter(element -> element.getKind() == ElementKind.CLASS)
                    .map(element -> (TypeElement) element)
                    .map(typeElement -> new ProcessorSourceTypeConstruct(processingEnv, typeElement))
                    .forEach(entityClasses::add);

            embeddables.stream()
                    .filter(element -> element.getKind() == ElementKind.CLASS)
                    .map(element -> (TypeElement) element)
                    .map(typeElement -> new ProcessorSourceTypeConstruct(processingEnv, typeElement))
                    .forEach(embeddableClasses::add);

            final SourceInterpreter interpreter = new SourceInterpreter(processingEnv, entityClasses, embeddableClasses);
            return Optional.of(interpreter.interpret());
        } catch (Exception e) {
            if (lastInterpretException != null) {
                // Failed on a previous round too — this is a real error, not a timing issue
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "HarborORM: Failed to interpret entity annotations: " + e.getMessage());
            }
            lastInterpretException = e;
            return Optional.empty();
        }
    }

    private StoredFunctionMetadata interpretStoredFunction(TypeElement typeElement) {
        StoredFunction annotation = typeElement.getAnnotation(StoredFunction.class);
        String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
        String className = typeElement.getSimpleName().toString();

        // Extract params from annotation
        List<StoredFunctionParamMetadata> params = new ArrayList<>();
        for (Param param : annotation.params()) {
            // param.type() will throw MirroredTypeException at compile time — use standard workaround
            String typeName;
            try {
                typeName = param.type().getCanonicalName();
            } catch (javax.lang.model.type.MirroredTypeException e) {
                typeName = e.getTypeMirror().toString();
            }
            params.add(new StoredFunctionParamMetadata(param.name(), typeName));
        }

        // Extract return columns from @Column(nullable = false)-annotated fields
        ProcessorSourceTypeConstruct construct = new ProcessorSourceTypeConstruct(processingEnv, typeElement);
        List<ColumnMetadata> columns = construct.getFields().stream()
                .filter(f -> f.hasAnnotation(Column.class))
                .map(f -> {
                    Column colAnnotation = f.getAnnotation(Column.class).orElseThrow();
                    String fieldName = f.getName();
                    boolean hasExplicitName = !colAnnotation.name().isEmpty();
                    String columnName = hasExplicitName ? colAnnotation.name() : fieldName;
                    EnumMappingType enumMappingType = f.getAnnotation(Enumerated.class)
                            .map(Enumerated::value)
                            .orElse(null);
                    ConverterMetadata converterMetadata = ConverterMetadataFactory.create(
                            processingEnv, f.getAnnotation(Convert.class).orElse(null));
                    TypeHandlerMetadata typeHandlerMetadata = TypeHandlerMetadataFactory.create(processingEnv, f);
                    if (converterMetadata != null && typeHandlerMetadata != null) {
                        throw new IllegalStateException("@Convert and @TypeHandler cannot both be on field: " + fieldName);
                    }
                    if (enumMappingType != null && typeHandlerMetadata != null) {
                        throw new IllegalStateException("@Enumerated and @TypeHandler cannot both be on field: " + fieldName);
                    }
                    return new ColumnMetadata(
                            fieldName,
                            columnName,
                            false,      // id
                            false,      // autoGenerated
                            null,       // sequenceGenerator
                            converterMetadata,
                            true,       // insertable
                            true,       // updatable
                            false,      // nullable
                            f.getType(), // type (SourceRefType)
                            enumMappingType,
                            null,       // typesMetadata
                            false,      // version
                            false,      // json
                            typeHandlerMetadata,
                            hasExplicitName
                    );
                })
                .toList();

        return new StoredFunctionMetadata(
                packageName, className,
                typeElement.getModifiers().contains(javax.lang.model.element.Modifier.PUBLIC),
                annotation.name(),
                annotation.schema().isEmpty() ? null : annotation.schema(),
                params, columns
        );
    }

    private ViewMetadata interpretView(TypeElement typeElement) {
        View annotation = typeElement.getAnnotation(View.class);
        String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
        String className = typeElement.getSimpleName().toString();
        ColumnNameStrategy strategy = annotation.columnNameStrategy();

        ProcessorSourceTypeConstruct construct = new ProcessorSourceTypeConstruct(processingEnv, typeElement);
        List<ColumnMetadata> columns = construct.getFields().stream()
                .filter(f -> f.hasAnnotation(Column.class))
                .map(f -> {
                    Column colAnnotation = f.getAnnotation(Column.class).orElseThrow();
                    String fieldName = f.getName();
                    boolean hasExplicitName = !colAnnotation.name().isEmpty();
                    String columnName = hasExplicitName ? colAnnotation.name() : strategy.apply(fieldName);
                    EnumMappingType enumMappingType = f.getAnnotation(Enumerated.class)
                            .map(Enumerated::value)
                            .orElse(null);
                    ConverterMetadata converterMetadata = ConverterMetadataFactory.create(
                            processingEnv, f.getAnnotation(Convert.class).orElse(null));
                    TypeHandlerMetadata typeHandlerMetadata = TypeHandlerMetadataFactory.create(processingEnv, f);
                    if (converterMetadata != null && typeHandlerMetadata != null) {
                        throw new IllegalStateException("@Convert and @TypeHandler cannot both be on field: " + fieldName);
                    }
                    if (enumMappingType != null && typeHandlerMetadata != null) {
                        throw new IllegalStateException("@Enumerated and @TypeHandler cannot both be on field: " + fieldName);
                    }
                    return new ColumnMetadata(
                            fieldName,
                            columnName,
                            false,      // id
                            false,      // autoGenerated
                            null,       // sequenceGenerator
                            converterMetadata,
                            true,       // insertable
                            true,       // updatable
                            colAnnotation.nullable(),
                            f.getType(), // type
                            enumMappingType,
                            null,       // typesMetadata
                            false,      // version
                            false,      // json
                            typeHandlerMetadata,
                            hasExplicitName
                    );
                })
                .toList();

        return new ViewMetadata(
                packageName, className,
                typeElement.getModifiers().contains(javax.lang.model.element.Modifier.PUBLIC),
                annotation.name(),
                annotation.schema().isEmpty() ? null : annotation.schema(),
                columns
        );
    }
}
