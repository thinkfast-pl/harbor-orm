// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.interpreter;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import io.github.thinkfastpl.harbororm.api.metadata.QEntity;
import io.github.thinkfastpl.harbororm.api.metadata.QEntityRelation;
import io.github.thinkfastpl.harbororm.modelgen.metadata.*;
import io.github.thinkfastpl.harbororm.modelgen.source.SourceClassField;
import io.github.thinkfastpl.harbororm.modelgen.source.SourceRefType;
import io.github.thinkfastpl.harbororm.modelgen.source.SourceTypeConstruct;
import io.github.thinkfastpl.harbororm.modelgen.utils.AnnotationClassValueAccessor;
import io.github.thinkfastpl.harbororm.modelgen.utils.StringUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class SourceInterpreter {

    @NonNull
    private final ProcessingEnvironment processingEnv;

    @NonNull
    private final List<SourceTypeConstruct> entityClasses;

    @NonNull
    private final List<SourceTypeConstruct> embeddableClasses;

    private final Map<String, EmbeddableMetadata> createdEmbeddables = new HashMap<>();

    private final Set<String> inProgressEmbeddables = new HashSet<>();

    public SourceInterpreterResult interpret() {
        final List<EmbeddableMetadata> embeddableMetadataList = embeddableClasses.stream()
                .map(this::toEmbeddableMetadata)
                .toList();

        final List<EntityMetadata> entityMetadataList = entityClasses.stream()
                .map(this::toEntityMetadata)
                .toList();

        return new SourceInterpreterResult(entityMetadataList, embeddableMetadataList);
    }

    private EmbeddableMetadata toEmbeddableMetadata(@NonNull SourceTypeConstruct classModel) {
        EmbeddableMetadata cached = createdEmbeddables.get(classModel.getFullClassName());
        if (cached != null) {
            return cached;
        }

        if (!inProgressEmbeddables.add(classModel.getFullClassName())) {
            throw new IllegalStateException(
                    "Circular @Embedded reference detected for: " + classModel.getFullClassName());
        }

        try {
            final List<SourceClassField> fields = classModel.getFields();
            final LifecycleMethodNames lifecycle = LifecycleMethodNames.extract(classModel.getMethods());

            EmbeddableMetadata result = new EmbeddableMetadata(
                    classModel,
                    findColumnsMetadata(fields, ColumnNameStrategy.SNAKE_CASE),
                    findEmbeddedMetadata(fields, ColumnNameStrategy.SNAKE_CASE),
                    findElementCollectionMetadata(fields, ColumnNameStrategy.SNAKE_CASE),
                    lifecycle
            );
            createdEmbeddables.put(classModel.getFullClassName(), result);
            return result;
        } finally {
            inProgressEmbeddables.remove(classModel.getFullClassName());
        }
    }

    private EntityMetadata toEntityMetadata(@NonNull SourceTypeConstruct classModel) {
        final Entity entityAnnotation = classModel.getAnnotation(Entity.class)
                .orElseThrow();

        final List<SourceClassField> fields = classModel.getFields();
        final LifecycleMethodNames lifecycle = LifecycleMethodNames.extract(classModel.getMethods());

        final List<SourceClassField> idFields = classModel.getFields().stream()
                .filter(f -> f.hasAnnotation(Id.class))
                .toList();

        // Validate @Version fields
        for (SourceClassField field : fields) {
            if (field.hasAnnotation(Version.class) && !field.hasAnnotation(Column.class) && !field.hasAnnotation(Id.class)) {
                throw new IllegalStateException("@Version field '" + field.getName() + "' must also have @Column(nullable = false) annotation");
            }
        }

        ColumnNameStrategy columnNameStrategy = entityAnnotation.columnNameStrategy();
        List<ColumnMetadata> columns = findColumnsMetadata(fields, columnNameStrategy);
        long versionFieldCount = columns.stream().filter(ColumnMetadata::version).count();
        if (versionFieldCount > 1) {
            throw new IllegalStateException("Entity " + classModel.getSimpleClassName() + " has more than one @Version field");
        }

        return new EntityMetadata(
                classModel.getPackageName(),
                classModel.getSimpleClassName(),
                classModel.isPublicClass(),
                entityAnnotation.table(),
                StringUtils.isNotBlank(entityAnnotation.schema()) ? entityAnnotation.schema() : null,
                List.of(QEntity.class.getName()),
                columns,
                findEmbeddedMetadata(fields, columnNameStrategy),
                findElementCollectionMetadata(fields, columnNameStrategy),
                findEntityRelationMetadata(fields),
                findManyToManyMetadata(fields),
                lifecycle,
                switch (idFields.size()) {
                    case 0 -> throw new IllegalStateException("No id column in entity: " + classModel.getSimpleClassName());
                    case 1 -> idFields.get(0);
                    default -> throw new IllegalStateException("Too many id columns in entity: " + classModel.getSimpleClassName());
                }
        );
    }

    private List<ColumnMetadata> findColumnsMetadata(List<SourceClassField> fields, ColumnNameStrategy strategy) {
        return fields.stream()
                .filter(f -> (f.hasAnnotation(Id.class) || f.hasAnnotation(Column.class))
                        && !f.hasAnnotation(Embedded.class)
                        && !f.hasAnnotation(ElementCollection.class)
                )
                .map(f -> toColumnMetadata(f, strategy))
                .toList();
    }

    private List<EmbeddedMetadata> findEmbeddedMetadata(List<SourceClassField> fields, ColumnNameStrategy strategy) {
        return fields.stream()
                .filter(f -> f.hasAnnotation(Embedded.class)
                        && !f.hasAnnotation(Column.class)
                        && !f.hasAnnotation(ElementCollection.class)
                )
                .map(f -> toEmbeddedMetadata(f, strategy))
                .toList();
    }

    private List<ElementCollectionMetadata> findElementCollectionMetadata(List<SourceClassField> fields, ColumnNameStrategy strategy) {
        return fields.stream()
                .filter(f -> f.hasAnnotation(ElementCollection.class)
                        && !f.hasAnnotation(Id.class)
                        && !f.hasAnnotation(Column.class)
                        && !f.hasAnnotation(Embedded.class)
                )
                .map(f -> toElementCollectionMetadata(f, strategy))
                .toList();
    }

    private List<EntityRelationMetadata> findEntityRelationMetadata(List<SourceClassField> fields) {
        return fields.stream()
                .filter(f -> f.hasAnnotation(OneToMany.class) || f.hasAnnotation(OneToOne.class))
                .map(this::toEntityRelationMetadata)
                .toList();
    }

    private List<ManyToManyMetadata> findManyToManyMetadata(List<SourceClassField> fields) {
        return fields.stream()
                .filter(f -> f.hasAnnotation(ManyToMany.class))
                .map(this::toManyToManyMetadata)
                .toList();
    }

    private ManyToManyMetadata toManyToManyMetadata(@NonNull SourceClassField fieldModel) {
        if (!Set.class.getName().equals(fieldModel.getType().getFullName())) {
            throw new IllegalArgumentException("@ManyToMany field type must be %s".formatted(Set.class.getName()));
        }

        ManyToMany manyToMany = fieldModel.getAnnotation(ManyToMany.class).orElseThrow();
        List<SourceRefType> genericTypeArguments = fieldModel.getType().getGenericTypeArguments();
        if (genericTypeArguments.isEmpty()) {
            throw new IllegalArgumentException(
                    "Field '%s' must have a generic type parameter (e.g., Set<MyEntity> instead of raw Set)"
                            .formatted(fieldModel.getName()));
        }
        SourceRefType genericType = genericTypeArguments.get(0);

        // Check if the generic type is an @Entity (full entity) or a simple type (ID-only)
        boolean isEntity = genericType.asTypeConstruct().getAnnotation(Entity.class).isPresent();
        boolean isEmbeddable = !isEntity && genericType.asTypeConstruct().getAnnotation(Embeddable.class).isPresent();

        SourceRefType relatedEntityType = null;
        SourceRefType relatedEntityIdType = null;

        if (isEntity) {
            relatedEntityType = genericType;
            SourceClassField idField = genericType.asTypeConstruct().getFields().stream()
                    .filter(f -> f.hasAnnotation(Id.class))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Referenced entity does not have ID column"));
            relatedEntityIdType = idField.getType();
        }

        return new ManyToManyMetadata(
                fieldModel.getName(),
                manyToMany.table(),
                manyToMany.schema().isBlank() ? null : manyToMany.schema(),
                Stream.of(manyToMany.joinColumns()).map(this::toJoinColumnMetadata).toList(),
                Stream.of(manyToMany.inverseJoinColumns()).map(this::toJoinColumnMetadata).toList(),
                relatedEntityType,
                relatedEntityIdType,
                isEmbeddable ? genericType : null,
                !isEntity
        );
    }

    private EmbeddedMetadata toEmbeddedMetadata(@NonNull SourceClassField fieldModel, @NonNull ColumnNameStrategy strategy) {
        final Embedded embedded = fieldModel.getAnnotation(Embedded.class).orElseThrow(() -> new IllegalArgumentException("Embedded annotation required"));
        final SourceTypeConstruct embeddableTypeConstruct = fieldModel.getType().asTypeConstruct();
        final Map<String, Column> attributeOverrides = fieldModel.getAnnotation(AttributeOverrides.class)
                .map(ao -> Stream.of(ao.value()).collect(Collectors.toMap(AttributeOverride::name, AttributeOverride::column)))
                .orElse(null);

        return new EmbeddedMetadata(
                fieldModel.getName(),
                embedded.tableFieldNamePrefix(),
                embeddableTypeConstruct,
                toEmbeddableMetadata(embeddableTypeConstruct),
                attributeOverrides,
                strategy
        );
    }

    private ColumnMetadata toColumnMetadata(@NonNull SourceClassField fieldModel, @NonNull ColumnNameStrategy strategy) {
        final boolean idColumn = fieldModel.getAnnotation(Id.class).isPresent();
        final boolean autogeneratedValue = fieldModel.getAnnotation(AutoGenerated.class).isPresent();
        final Optional<SequenceGenerated> sequenceGeneratedOptional = fieldModel.getAnnotation(SequenceGenerated.class);
        final boolean versionColumn = fieldModel.getAnnotation(Version.class).isPresent();

        if (autogeneratedValue && sequenceGeneratedOptional.isPresent()) {
            throw new IllegalStateException("Column cannot be annotated with both: AutoGenerated and SequenceGenerated");
        }

        if (versionColumn && idColumn) {
            throw new IllegalStateException("@Version and @Id cannot be on the same field: " + fieldModel.getName());
        }

        if (versionColumn) {
            String typeName = fieldModel.getType().getFullName();
            Set<String> allowedTypes = Set.of(
                    "short", "java.lang.Short",
                    "int", "java.lang.Integer",
                    "long", "java.lang.Long"
            );
            if (!allowedTypes.contains(typeName)) {
                throw new IllegalStateException("@Version field must be short, int, or long but was: " + typeName);
            }
        }

        Optional<Column> columnAnnotation = fieldModel.getAnnotation(Column.class);
        Optional<Enumerated> enumeratedAnnotation = fieldModel.getAnnotation(Enumerated.class);
        final boolean jsonColumn = fieldModel.getAnnotation(Json.class).isPresent();

        final Type[] types = fieldModel.getAnnotation(Types.class)
                .map(Types::value)
                .orElse(fieldModel.getAnnotations(Type.class));

        final TypeHandler[] typeHandlers = fieldModel.getAnnotation(TypeHandlers.class)
                .map(TypeHandlers::value)
                .orElse(fieldModel.getAnnotations(TypeHandler.class));

        ConverterMetadata converterMetadata = ConverterMetadataFactory.create(
                processingEnv, fieldModel.getAnnotation(Convert.class).orElse(null));

        TypeHandlerMetadata typeHandlerMetadata = TypeHandlerMetadataFactory.create(processingEnv, typeHandlers);

        if (converterMetadata != null && typeHandlerMetadata != null) {
            throw new IllegalStateException("@Convert and @TypeHandler cannot both be on field: " + fieldModel.getName());
        }

        if (enumeratedAnnotation.isPresent() && typeHandlerMetadata != null) {
            throw new IllegalStateException("@Enumerated and @TypeHandler cannot both be on field: " + fieldModel.getName());
        }

        boolean hasExplicitColumnName = columnAnnotation.map(Column::name).filter(name -> !name.isBlank()).isPresent();

        return new ColumnMetadata(
                fieldModel.getName(),
                columnAnnotation.map(Column::name).filter(name -> !name.isBlank()).orElseGet(() -> strategy.apply(fieldModel.getName())),
                idColumn,
                autogeneratedValue,
                sequenceGeneratedOptional.map(sg -> new SequenceGeneratorMetadata(sg.sequence())).orElse(null),
                converterMetadata,
                columnAnnotation.map(Column::insertable).orElse(true),
                columnAnnotation.map(Column::updatable).orElse(true),
                columnAnnotation.map(Column::nullable).orElse(false),
                fieldModel.getType(),
                enumeratedAnnotation.map(Enumerated::value).orElse(null),
                toTypesMetadata(types),
                versionColumn,
                jsonColumn,
                typeHandlerMetadata,
                hasExplicitColumnName
        );
    }

    private ColumnMetadata toColumnMetadata(SourceRefType type, String fieldName, Id idAnnotation, Column column, Enumerated enumeratedAnnotation, Convert convertAnnotation, Type[] types, TypeHandler[] typeHandlers, ColumnNameStrategy strategy) {
        Optional<Column> columnAnnotation = Optional.ofNullable(column);

        ConverterMetadata converterMetadata = ConverterMetadataFactory.create(processingEnv, convertAnnotation);

        TypeHandlerMetadata typeHandlerMetadata = TypeHandlerMetadataFactory.create(processingEnv, typeHandlers);

        if (converterMetadata != null && typeHandlerMetadata != null) {
            throw new IllegalStateException("@Convert and @TypeHandler cannot both be on field: " + fieldName);
        }

        if (enumeratedAnnotation != null && typeHandlerMetadata != null) {
            throw new IllegalStateException("@Enumerated and @TypeHandler cannot both be on field: " + fieldName);
        }

        boolean hasExplicitColumnName = columnAnnotation.map(Column::name).filter(name -> !name.isBlank()).isPresent();
        String resolvedColumnName = columnAnnotation.map(Column::name).filter(name -> !name.isBlank())
                .orElseGet(() -> fieldName != null ? strategy.apply(fieldName) : null);

        return new ColumnMetadata(
                fieldName,
                resolvedColumnName,
                idAnnotation != null,
                false,
                null,
                converterMetadata,
                columnAnnotation.map(Column::insertable).orElse(true),
                columnAnnotation.map(Column::updatable).orElse(true),
                columnAnnotation.map(Column::nullable).orElse(false),
                type,
                enumeratedAnnotation == null ? null : enumeratedAnnotation.value(),
                toTypesMetadata(types),
                false,
                false,
                typeHandlerMetadata,
                hasExplicitColumnName
        );
    }

    private ElementCollectionMetadata toElementCollectionMetadata(@NonNull SourceClassField fieldModel, @NonNull ColumnNameStrategy strategy) {
        if (!List.class.getName().equals(fieldModel.getType().getFullName())) {
            throw new IllegalArgumentException("Element collection field type must be %s".formatted(List.class.getName()));
        }

        ElementCollection elementCollection = fieldModel.getAnnotation(ElementCollection.class).orElseThrow();

        List<SourceRefType> genericTypeArguments = fieldModel.getType().getGenericTypeArguments();
        if (genericTypeArguments.isEmpty()) {
            throw new IllegalArgumentException(
                    "Field '%s' must have a generic type parameter (e.g., List<String> instead of raw List)"
                            .formatted(fieldModel.getName()));
        }
        SourceRefType genericType = genericTypeArguments.get(0);

        final Column column = genericType.getAnnotation(Column.class).orElse(null);
        final Enumerated enumerated = genericType.getAnnotation(Enumerated.class).orElse(null);
        final Convert convert = genericType.getAnnotation(Convert.class).orElse(null);
        final Embedded embedded = genericType.getAnnotation(Embedded.class).orElse(null);

        final Type[] types = genericType.getAnnotation(Types.class)
                .map(Types::value)
                .orElse(genericType.getAnnotations(Type.class));

        final TypeHandler[] typeHandlers = genericType.getAnnotation(TypeHandlers.class)
                .map(TypeHandlers::value)
                .orElse(genericType.getAnnotations(TypeHandler.class));

        ColumnMetadata columnMetadata = null;
        EmbeddedMetadata embeddedMetadata = null;

        if (column != null) {
            columnMetadata = toColumnMetadata(genericType, null, null, column, enumerated, convert, types, typeHandlers, strategy);
        } else if (embedded != null) {
            SourceTypeConstruct embeddableTypeConstruct = genericType.asTypeConstruct();
            Map<String, Column> attributeOverrides = fieldModel.getAnnotation(AttributeOverrides.class)
                    .map(ao -> Stream.of(ao.value()).collect(Collectors.toMap(AttributeOverride::name, AttributeOverride::column)))
                    .orElse(null);

            embeddedMetadata = new EmbeddedMetadata(
                    null,
                    embedded.tableFieldNamePrefix(),
                    embeddableTypeConstruct,
                    toEmbeddableMetadata(embeddableTypeConstruct),
                    attributeOverrides,
                    strategy
            );
        } else {
            throw new IllegalStateException("Element collection list type must be annotated with @Column(nullable = false) or @Embedded");
        }

        return new ElementCollectionMetadata(
                fieldModel.getName(),
                genericType.getTypeClassNameForGenericUse(),
                elementCollection.table(),
                elementCollection.schema().isBlank() ? null : elementCollection.schema(),
                Stream.of(elementCollection.joinColumns()).map(this::toJoinColumnMetadata).toList(),
                columnMetadata,
                embeddedMetadata
        );
    }

    private EntityRelationMetadata toEntityRelationMetadata(@NonNull SourceClassField fieldModel) {
        if (fieldModel.hasAnnotation(OneToMany.class)) {
            return toOneToManyRelationMetadata(fieldModel);
        } else if (fieldModel.hasAnnotation(OneToOne.class)) {
            return toOneToOneRelationMetadata(fieldModel);
        } else {
            throw new IllegalStateException("Expected @OneToMany or @OneToOne annotation");
        }
    }

    private EntityRelationMetadata toOneToManyRelationMetadata(@NonNull SourceClassField fieldModel) {
        if (!List.class.getName().equals(fieldModel.getType().getFullName())) {
            throw new IllegalArgumentException("Entity relation field type must be %s".formatted(List.class.getName()));
        }

        OneToMany oneToMany = fieldModel.getAnnotation(OneToMany.class).orElseThrow();
        List<SourceRefType> genericTypeArguments = fieldModel.getType().getGenericTypeArguments();
        if (genericTypeArguments.isEmpty()) {
            throw new IllegalArgumentException(
                    "Field '%s' must have a generic type parameter (e.g., List<ChildEntity> instead of raw List)"
                            .formatted(fieldModel.getName()));
        }
        SourceRefType relatedEntityType = genericTypeArguments.get(0);

        SourceClassField idField = relatedEntityType.asTypeConstruct().getFields().stream()
                .filter(f -> f.hasAnnotation(Id.class))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Referenced entity does not have ID column"));

        return new EntityRelationMetadata(
                fieldModel.getName(),
                relatedEntityType,
                idField.getType(),
                QEntityRelation.Type.ONE_TO_MANY,
                Stream.of(oneToMany.joinColumns()).map(this::toJoinColumnMetadata).toList()
        );
    }

    private EntityRelationMetadata toOneToOneRelationMetadata(@NonNull SourceClassField fieldModel) {
        if (!"io.github.thinkfastpl.harbororm.api.LazyRef".equals(fieldModel.getType().getFullName())) {
            throw new IllegalArgumentException("@OneToOne field type must be io.github.thinkfastpl.harbororm.api.LazyRef");
        }

        OneToOne oneToOne = fieldModel.getAnnotation(OneToOne.class).orElseThrow();
        List<SourceRefType> genericTypeArguments = fieldModel.getType().getGenericTypeArguments();
        if (genericTypeArguments.isEmpty()) {
            throw new IllegalArgumentException(
                    "Field '%s' must have a generic type parameter (e.g., LazyRef<ParentEntity> instead of raw LazyRef)"
                            .formatted(fieldModel.getName()));
        }
        SourceRefType relatedEntityType = genericTypeArguments.get(0);

        SourceClassField idField = relatedEntityType.asTypeConstruct().getFields().stream()
                .filter(f -> f.hasAnnotation(Id.class))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Referenced entity does not have ID column"));

        return new EntityRelationMetadata(
                fieldModel.getName(),
                relatedEntityType,
                idField.getType(),
                QEntityRelation.Type.ONE_TO_ONE,
                Stream.of(oneToOne.joinColumns()).map(this::toJoinColumnMetadata).toList()
        );
    }

    private JoinColumnMetadata toJoinColumnMetadata(@NonNull JoinColumn joinColumn) {
        return new JoinColumnMetadata(
                joinColumn.name(),
                AnnotationClassValueAccessor.access(processingEnv, joinColumn::fieldType).getTypeClassNameForGenericUse(),
                joinColumn.referencedColumnName(),
                joinColumn.insertable(),
                joinColumn.updatable(),
                joinColumn.nullable()
        );
    }

    private TypesMetadata toTypesMetadata(Type[] types) {
        if (types == null || types.length == 0) {
            return null;
        }

        final Map<String, String> res = new HashMap<>();
        for (Type type : types) {
            res.put(type.dialect(), type.columnType());
        }
        return new TypesMetadata(res);
    }
}
