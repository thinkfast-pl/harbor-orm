// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.writer;

import io.github.thinkfastpl.harbororm.api.annotations.ColumnNameStrategy;
import io.github.thinkfastpl.harbororm.modelgen.metadata.*;
import io.github.thinkfastpl.harbororm.modelgen.utils.StreamUtils;
import lombok.RequiredArgsConstructor;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class QEntityWriter {
    private static final int INDENT = 4;
    private final PrintWriter writer;
    private final EntityMetadata metadata;

    void write() {
        writer.println("package " + metadata.packageName() + ";");
        writer.println();

        writer.println(imports());
        writer.println();

        writer.println(classDeclaration());

        for (ColumnMetadata column : metadata.columns()) {
            writer.println(QColumnWriterUtils.generateColumnDeclaration(column).indent(INDENT));
        }

        for (EmbeddedMetadata embeddedAttribute : metadata.embeddedAttributes()) {
            writer.println(generateEmbeddedDeclaration(embeddedAttribute).indent(INDENT));
        }

        for (ElementCollectionMetadata elementCollection : metadata.elementCollections()) {
            writer.println(generateElementCollectionDeclaration(elementCollection).indent(INDENT));
        }

        for (EntityRelationMetadata entityRelation : metadata.entityRelations()) {
            writer.println(generateEntityRelationDeclaration(entityRelation).indent(INDENT));
        }

        for (ManyToManyMetadata manyToMany : metadata.manyToManyRelations()) {
            writer.println(generateManyToManyDeclaration(manyToMany).indent(INDENT));
        }

        writer.println(allAttributesDeclaration().indent(INDENT));

        writer.println(tableNameDeclaration().indent(INDENT));

        writer.print(constructorDeclaration().indent(INDENT));
        writer.print(constructorTableNameAssignment().indent(INDENT * 2));

        for (ColumnMetadata column : metadata.columns()) {
            writer.print(QColumnWriterUtils.generateColumnConstructorInit(column).indent(INDENT * 2));
        }

        for (EmbeddedMetadata embedded : metadata.embeddedAttributes()) {
            writer.println(generateEmbeddedConstructorInit(embedded).indent(INDENT * 2));
        }

        for (ElementCollectionMetadata elementCollection : metadata.elementCollections()) {
            writer.println(generateElementCollectionConstructorInit(elementCollection).indent(INDENT * 2));
        }

        for (EntityRelationMetadata entityRelation : metadata.entityRelations()) {
            writer.println(generateEntityRelationConstructorInit(entityRelation).indent(INDENT * 2));
        }

        for (ManyToManyMetadata manyToMany : metadata.manyToManyRelations()) {
            writer.println(generateManyToManyConstructorInit(manyToMany).indent(INDENT * 2));
        }

        writer.print(allAttributesAssignment().indent(INDENT * 2));

        writer.println(endConstructorDeclaration().indent(INDENT));

        writer.println(getTableNameMethod().indent(INDENT));

        writer.println(getIdColumnMethod().indent(INDENT));

        writer.println(getBeanTypeMethod().indent(INDENT));

        writer.println(getAllAttributesMethod().indent(INDENT));

        writer.println(getPreInsertMethodName().indent(INDENT));

        writer.println(getPreUpdateMethodName().indent(INDENT));

        writer.println(getPreDeleteMethodName().indent(INDENT));

        writer.println(getPostInsertMethodName().indent(INDENT));

        writer.println(getPostUpdateMethodName().indent(INDENT));

        writer.println(getPostDeleteMethodName().indent(INDENT));

        writer.print(getVersionColumnMethod().indent(INDENT));

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
               %sclass %s implements QEntity<%s, %s> {
               """
                .formatted(metadata.publicClass() ? "public " : "", metadata.qClassName(), metadata.className(), metadata.idField().getType().getTypeClassNameForGenericUse());
    }

    private String generateEmbeddedDeclaration(EmbeddedMetadata embedded) {
        return "public final %s %s;".formatted(embedded.getQClassNameInPackage(metadata.packageName()), embedded.fieldName());
    }

    private String generateElementCollectionDeclaration(ElementCollectionMetadata elementCollection) {
        return "public final QElementCollection<%s> %s;".formatted(elementCollection.typeClassNameForGenericUse(), elementCollection.fieldName());
    }

    private String generateEntityRelationDeclaration(EntityRelationMetadata entityRelation) {
        return "public final QEntityRelation<%s, %s> %s;".formatted(
                entityRelation.getEntityType().getNameInPackage(metadata.packageName()),
                entityRelation.getEntityIdType().getNameInPackage(metadata.packageName()),
                entityRelation.getFieldName()
        );
    }

    private String allAttributesDeclaration() {
        return "private final List<QAttribute> __allAttributes;";
    }

    private String tableNameDeclaration() {
        return "private final QTableName __tableName;";
    }

    private String constructorDeclaration() {
        return "public %s(String alias) {".formatted(metadata.qClassName());
    }

    private String constructorTableNameAssignment() {
        return "this.__tableName = new QTableName(\"%s\", %s, alias);".formatted(
                metadata.tableName(),
                metadata.schemaName() == null ? "null" : "\"" + metadata.schemaName() + "\""
        );
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
                    embedded.getQClassNameInPackage(metadata.packageName()),
                    embedded.fieldName(),
                    QColumnWriterUtils.generateMapOf(mapEntries)
            );
        } else if (embedded.columnNameStrategy() == ColumnNameStrategy.SNAKE_CASE) {
            return "this.%s = new %s(alias, \"%s\");".formatted(
                    embedded.fieldName(),
                    embedded.getQClassNameInPackage(metadata.packageName()),
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
                    embedded.getQClassNameInPackage(metadata.packageName()),
                    embedded.fieldName(),
                    QColumnWriterUtils.generateMapOf(mapEntries)
            );
        }
    }

    private String generateElementCollectionConstructorInit(ElementCollectionMetadata elementCollection) {
        final String joinColumns = elementCollection.joinColumns().stream()
                .map(JoinColumnDataWriterUtils::generateNewObject)
                .collect(Collectors.joining(","));

        String qComparableAttributeArg;
        if (elementCollection.columnMetadata() != null) {
            qComparableAttributeArg = QColumnWriterUtils.generateColumnOnElementCollection(elementCollection.columnMetadata());
        } else {
            qComparableAttributeArg = "new %s(null, null)".formatted(elementCollection.embeddedMetadata().getQClassNameInPackage(metadata.packageName()));
        }

        return "this.%s = QElementCollection.of(%s, \"%s\", \"%s\", %s, List.of(%s));".formatted(
                elementCollection.fieldName(),
                qComparableAttributeArg,
                elementCollection.fieldName(),
                elementCollection.tableName(),
                elementCollection.tableSchemaName() == null ? "null" : "\"" + elementCollection.tableSchemaName() + "\"",
                joinColumns
        );
    }

    private String generateEntityRelationConstructorInit(EntityRelationMetadata entityRelation) {
        final String joinColumns = entityRelation.getJoinColumns().stream()
                .map(JoinColumnDataWriterUtils::generateNewObject)
                .collect(Collectors.joining(","));

        return switch (entityRelation.getRelationType()) {
            case ONE_TO_MANY -> "this.%s = QEntityRelation.ofOneToMany(\"%s\", new %s(null), List.of(%s));".formatted(
                    entityRelation.getFieldName(),
                    entityRelation.getFieldName(),
                    entityRelation.getQClassNameInPackage(metadata.packageName()),
                    joinColumns
            );
            case ONE_TO_ONE -> "this.%s = QEntityRelation.ofOneToOne(\"%s\", new %s(null), List.of(%s));".formatted(
                    entityRelation.getFieldName(),
                    entityRelation.getFieldName(),
                    entityRelation.getQClassNameInPackage(metadata.packageName()),
                    joinColumns
            );
        };
    }

    private String generateManyToManyDeclaration(ManyToManyMetadata manyToMany) {
        if (manyToMany.idOnly()) {
            return "public final QManyToMany<Void> %s;".formatted(manyToMany.fieldName());
        } else {
            return "public final QManyToMany<%s> %s;".formatted(
                    manyToMany.relatedEntityType().getNameInPackage(metadata.packageName()),
                    manyToMany.fieldName()
            );
        }
    }

    private String generateManyToManyConstructorInit(ManyToManyMetadata manyToMany) {
        final String joinColumns = manyToMany.joinColumns().stream()
                .map(JoinColumnDataWriterUtils::generateNewObject)
                .collect(Collectors.joining(","));

        final String inverseJoinColumns = manyToMany.inverseJoinColumns().stream()
                .map(JoinColumnDataWriterUtils::generateNewObject)
                .collect(Collectors.joining(","));

        if (manyToMany.idOnly()) {
            final String elementEmbeddableQClassName = manyToMany.getElementEmbeddableQClassNameInPackage(metadata.packageName());
            final String elementEmbeddable = elementEmbeddableQClassName == null
                    ? "null"
                    : "new %s(null, \"%s\")".formatted(elementEmbeddableQClassName, manyToMany.fieldName());
            return "this.%s = QManyToMany.of(\"%s\", \"%s\", %s, List.of(%s), List.of(%s), null, %s, true);".formatted(
                    manyToMany.fieldName(),
                    manyToMany.fieldName(),
                    manyToMany.tableName(),
                    manyToMany.tableSchemaName() == null ? "null" : "\"" + manyToMany.tableSchemaName() + "\"",
                    joinColumns,
                    inverseJoinColumns,
                    elementEmbeddable
            );
        } else {
            return "this.%s = QManyToMany.of(\"%s\", \"%s\", %s, List.of(%s), List.of(%s), new %s(null), false);".formatted(
                    manyToMany.fieldName(),
                    manyToMany.fieldName(),
                    manyToMany.tableName(),
                    manyToMany.tableSchemaName() == null ? "null" : "\"" + manyToMany.tableSchemaName() + "\"",
                    joinColumns,
                    inverseJoinColumns,
                    manyToMany.getQClassNameInPackage(metadata.packageName())
            );
        }
    }

    private String allAttributesAssignment() {
        final String cols = StreamUtils.concat(
                        metadata.columns().stream().map(ColumnMetadata::fieldName),
                        metadata.embeddedAttributes().stream().map(EmbeddedMetadata::fieldName),
                        metadata.elementCollections().stream().map(ElementCollectionMetadata::fieldName),
                        metadata.entityRelations().stream().map(EntityRelationMetadata::getFieldName),
                        metadata.manyToManyRelations().stream().map(ManyToManyMetadata::fieldName)
                )
                .map("this.%s"::formatted)
                .collect(Collectors.joining(", "));

        return """
               this.__allAttributes = List.of(
                       %s
               );
               """.formatted(cols);
    }

    private String endConstructorDeclaration() {
        return "}";
    }

    private String getTableNameMethod() {
        return """
               @Override
               public QTableName getTableName() {
                   return __tableName;
               }
               """;
    }

    private String getIdColumnMethod() {
        return """
               @Override
               public QComparableAttribute<%s> getIdColumn() {
                   return this.%s;
               }
               """.formatted(metadata.idField().getType().getTypeClassNameForGenericUse(), metadata.idField().getName());
    }

    private String getBeanTypeMethod() {
        return """
               @Override
               public Class<%s> getBeanType() {
                   return %s.class;
               }
               """.formatted(metadata.className(), metadata.className());
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

    private String getVersionColumnMethod() {
        Optional<ColumnMetadata> versionColumn = metadata.columns().stream()
                .filter(ColumnMetadata::version)
                .findFirst();

        if (versionColumn.isEmpty()) {
            return """
                   @Override
                   public Optional<QColumn<?>> getVersionColumn() {
                       return Optional.empty();
                   }
                   """;
        } else {
            return """
                   @Override
                   public Optional<QColumn<?>> getVersionColumn() {
                       return Optional.of(this.%s);
                   }
                   """.formatted(versionColumn.get().fieldName());
        }
    }

    private String endClassDeclaration() {
        return "}";
    }
}
