// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.writer;

import io.github.thinkfastpl.harbororm.api.annotations.ColumnNameStrategy;
import io.github.thinkfastpl.harbororm.modelgen.metadata.ColumnMetadata;
import io.github.thinkfastpl.harbororm.modelgen.metadata.ElementCollectionMetadata;
import io.github.thinkfastpl.harbororm.modelgen.metadata.EmbeddableMetadata;
import io.github.thinkfastpl.harbororm.modelgen.metadata.EmbeddedMetadata;
import io.github.thinkfastpl.harbororm.modelgen.utils.StreamUtils;
import lombok.RequiredArgsConstructor;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class QEmbeddableWriter {
    private static final int INDENT = 4;
    private final PrintWriter writer;
    private final EmbeddableMetadata metadata;

    void write() {
        // package
        writer.println("package " + metadata.type().getPackageName() + ";");
        writer.println();

        // imports
        writer.println(imports());
        writer.println();

        // class declaration
        writer.println(classDeclaration());

        // columns
        for (ColumnMetadata column : metadata.columns()) {
            writer.println(QColumnWriterUtils.generateColumnDeclaration(column).indent(INDENT));
        }

        // element collections
        for (ElementCollectionMetadata elementCollection : metadata.elementCollections()) {
            writer.println(generateElementCollectionDeclaration(elementCollection).indent(INDENT));
        }

        // embedded attributes (nested embeddables)
        for (EmbeddedMetadata embedded : metadata.embeddedAttributes()) {
            writer.println(generateEmbeddedDeclaration(embedded).indent(INDENT));
        }

        // __propertyName field declaration
        writer.println(propertyNameDeclaration().indent(INDENT));

        // __allAttributes field declaration
        writer.println(allAttributesDeclaration().indent(INDENT));

        // basic constructor
        writer.print(constructorDeclaration().indent(INDENT));

        writer.print(propertyNameAssignment().indent(INDENT * 2));

        for (ColumnMetadata column : metadata.columns()) {
            writer.print(QColumnWriterUtils.generateColumnConstructorInit(column).indent(INDENT * 2));
        }

        for (ElementCollectionMetadata elementCollection : metadata.elementCollections()) {
            writer.println(generateElementCollectionConstructorInit(elementCollection).indent(INDENT * 2));
        }

        for (EmbeddedMetadata embedded : metadata.embeddedAttributes()) {
            writer.println(generateEmbeddedConstructorInit(embedded).indent(INDENT * 2));
        }

        writer.print(allAttributesAssignment().indent(INDENT * 2));

        writer.println(endConstructorDeclaration().indent(INDENT));

        // constructor for attributes attributeOverrides
        writer.print(constructorWithOverwriteDeclaration().indent(INDENT));

        writer.print(propertyNameAssignment().indent(INDENT * 2));

        for (ColumnMetadata column : metadata.columns()) {
            writer.print(QColumnWriterUtils.generateColumnConstructorInit(column, true).indent(INDENT * 2));
        }

        for (ElementCollectionMetadata elementCollection : metadata.elementCollections()) {
            writer.println(generateElementCollectionConstructorInit(elementCollection).indent(INDENT * 2));
        }

        for (EmbeddedMetadata embedded : metadata.embeddedAttributes()) {
            writer.println(generateEmbeddedConstructorInit(embedded).indent(INDENT * 2));
        }

        writer.print(allAttributesAssignment().indent(INDENT * 2));

        writer.println(endConstructorDeclaration().indent(INDENT));

        // methods
        writer.println(getBeanTypeMethod().indent(INDENT));

        writer.println(getPropertyNameMethod().indent(INDENT));

        writer.println(getAllAttributesMethod().indent(INDENT));

        writer.println(getPreInsertMethodName().indent(INDENT));

        writer.println(getPreUpdateMethodName().indent(INDENT));

        writer.println(getPreDeleteMethodName().indent(INDENT));

        writer.println(getPostInsertMethodName().indent(INDENT));

        writer.println(getPostUpdateMethodName().indent(INDENT));

        writer.print(getPostDeleteMethodName().indent(INDENT));

        // end class
        writer.println(endClassDeclaration());
    }

    private String imports() {
        List<String> packages = List.of(
                "io.github.thinkfastpl.harbororm.api.metadata.*",
                "java.util.List",
                "java.util.Map",
                "java.util.Optional"
        );

        return packages.stream()
                .map("import %s;"::formatted)
                .collect(Collectors.joining("\n"));
    }

    private String classDeclaration() {
        return """
               %sclass %s implements QEmbeddable<%s> {
               """
                .formatted(metadata.type().isPublicClass() ? "public " : "", metadata.qClassName(), metadata.type().getSimpleClassNameWithClassParents());
    }

    private String generateElementCollectionDeclaration(ElementCollectionMetadata elementCollection) {
        return "public final QElementCollection<%s> %s;".formatted(elementCollection.typeClassNameForGenericUse(), elementCollection.fieldName());
    }

    private String propertyNameDeclaration() {
        return "private final String __propertyName;";
    }

    private String allAttributesDeclaration() {
        return "private final List<QAttribute> __allAttributes;";
    }

    private String constructorDeclaration() {
        return "public %s(String alias, String propertyName) {".formatted(metadata.qClassName());
    }

    private String constructorWithOverwriteDeclaration() {
        return "public %s(String alias, String propertyName, Map<String, QAttribute> attributeOverrides) {".formatted(metadata.qClassName());
    }

    private String propertyNameAssignment() {
        return "this.__propertyName = propertyName;";
    }

    private String generateElementCollectionConstructorInit(ElementCollectionMetadata elementCollection) {
        final String joinColumns = elementCollection.joinColumns().stream()
                .map(JoinColumnDataWriterUtils::generateNewObject)
                .collect(Collectors.joining(","));

        return "this.%s = QElementCollection.of(%s.class, \"%s\", \"%s\", %s, List.of(%s));".formatted(
                elementCollection.fieldName(),
                elementCollection.typeClassNameForGenericUse(),
                elementCollection.fieldName(),
                elementCollection.tableName(),
                elementCollection.tableSchemaName() == null ? "null" : "\"" + elementCollection.tableSchemaName() + "\"",
                joinColumns
        );
    }

    private String allAttributesAssignment() {
        final String cols = StreamUtils.concat(
                        metadata.columns().stream().map(ColumnMetadata::fieldName),
                        metadata.embeddedAttributes().stream().map(EmbeddedMetadata::fieldName),
                        metadata.elementCollections().stream().map(ElementCollectionMetadata::fieldName)
                )
                .map("this.%s"::formatted)
                .collect(Collectors.joining(", "));

        return """
               this.__allAttributes = List.of(
                       %s
               );
               """.formatted(cols);
    }

    private String generateEmbeddedDeclaration(EmbeddedMetadata embedded) {
        return "public final %s %s;".formatted(embedded.getQClassNameInPackage(metadata.type().getPackageName()), embedded.fieldName());
    }

    private String generateEmbeddedConstructorInit(EmbeddedMetadata embedded) {
        if (embedded.attributeOverrides() != null) {
            List<String> mapEntries = embedded.attributeOverrides().entrySet().stream()
                    .map(entry -> {
                        ColumnMetadata columnMetadata = embedded.embeddableMetadata().columns().stream()
                                .filter(qc -> Objects.equals(qc.fieldName(), entry.getKey()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException("Unknown column " + entry.getKey()));

                        return "\"" + entry.getKey() + "\", " + QColumnWriterUtils.generateQColumnInitCode(columnMetadata, entry.getValue());
                    })
                    .collect(Collectors.toList());

            return "this.%s = new %s(alias, \"%s\", %s);".formatted(
                    embedded.fieldName(),
                    embedded.getQClassNameInPackage(metadata.type().getPackageName()),
                    embedded.fieldName(),
                    QColumnWriterUtils.generateMapOf(mapEntries)
            );
        } else if (embedded.columnNameStrategy() == ColumnNameStrategy.SNAKE_CASE) {
            return "this.%s = new %s(alias, \"%s\");".formatted(
                    embedded.fieldName(),
                    embedded.getQClassNameInPackage(metadata.type().getPackageName()),
                    embedded.fieldName()
            );
        } else {
            List<String> mapEntries = embedded.embeddableMetadata().columns().stream()
                    .map(col -> {
                        String resolvedColumnName = col.explicitColumnName()
                                ? col.columnName()
                                : embedded.columnNameStrategy().apply(col.fieldName());
                        return "\"" + col.fieldName() + "\", " + QColumnWriterUtils.generateQColumnInitCode(col, resolvedColumnName);
                    })
                    .collect(Collectors.toList());

            return "this.%s = new %s(alias, \"%s\", %s);".formatted(
                    embedded.fieldName(),
                    embedded.getQClassNameInPackage(metadata.type().getPackageName()),
                    embedded.fieldName(),
                    QColumnWriterUtils.generateMapOf(mapEntries)
            );
        }
    }

    private String endConstructorDeclaration() {
        return "}";
    }

    private String getBeanTypeMethod() {
        return """
               @Override
               public Class<%s> getJavaType() {
                   return %s.class;
               }
               """.formatted(metadata.type().getSimpleClassNameWithClassParents(), metadata.type().getSimpleClassNameWithClassParents());
    }

    private String getPropertyNameMethod() {
        return """
               @Override
               public String getPropertyName() {
                   return this.__propertyName;
               }
               """;
    }

    private String getAllAttributesMethod() {
        return """
               @Override
               public List<QAttribute> getAllAttributes() {
                   return this.__allAttributes;
               }
               """;
    }

    private String getPreInsertMethodName() {
        if (metadata.lifecycle().preInsertMethodName() == null) {
            return """
                   @Override
                   public Optional<String> getPreInsertMethodName() {
                       return Optional.empty();
                   }
                   """;
        } else {
            return """
                   @Override
                   public Optional<String> getPreInsertMethodName() {
                       return Optional.of("%s");
                   }
                   """.formatted(metadata.lifecycle().preInsertMethodName());
        }
    }

    private String getPreUpdateMethodName() {
        if (metadata.lifecycle().preUpdateMethodName() == null) {
            return """
                   @Override
                   public Optional<String> getPreUpdateMethodName() {
                       return Optional.empty();
                   }
                   """;
        } else {
            return """
                   @Override
                   public Optional<String> getPreUpdateMethodName() {
                       return Optional.of("%s");
                   }
                   """.formatted(metadata.lifecycle().preUpdateMethodName());
        }
    }

    private String getPreDeleteMethodName() {
        if (metadata.lifecycle().preDeleteMethodName() == null) {
            return """
                   @Override
                   public Optional<String> getPreDeleteMethodName() {
                       return Optional.empty();
                   }
                   """;
        } else {
            return """
                   @Override
                   public Optional<String> getPreDeleteMethodName() {
                       return Optional.of("%s");
                   }
                   """.formatted(metadata.lifecycle().preDeleteMethodName());
        }
    }

    private String getPostInsertMethodName() {
        if (metadata.lifecycle().postInsertMethodName() == null) {
            return """
                   @Override
                   public Optional<String> getPostInsertMethodName() {
                       return Optional.empty();
                   }
                   """;
        } else {
            return """
                   @Override
                   public Optional<String> getPostInsertMethodName() {
                       return Optional.of("%s");
                   }
                   """.formatted(metadata.lifecycle().postInsertMethodName());
        }
    }

    private String getPostUpdateMethodName() {
        if (metadata.lifecycle().postUpdateMethodName() == null) {
            return """
                   @Override
                   public Optional<String> getPostUpdateMethodName() {
                       return Optional.empty();
                   }
                   """;
        } else {
            return """
                   @Override
                   public Optional<String> getPostUpdateMethodName() {
                       return Optional.of("%s");
                   }
                   """.formatted(metadata.lifecycle().postUpdateMethodName());
        }
    }

    private String getPostDeleteMethodName() {
        if (metadata.lifecycle().postDeleteMethodName() == null) {
            return """
                   @Override
                   public Optional<String> getPostDeleteMethodName() {
                       return Optional.empty();
                   }
                   """;
        } else {
            return """
                   @Override
                   public Optional<String> getPostDeleteMethodName() {
                       return Optional.of("%s");
                   }
                   """.formatted(metadata.lifecycle().postDeleteMethodName());
        }
    }

    private String endClassDeclaration() {
        return "}";
    }
}
