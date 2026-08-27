// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.writer;

import io.github.thinkfastpl.harbororm.modelgen.metadata.ColumnMetadata;
import io.github.thinkfastpl.harbororm.modelgen.metadata.ViewMetadata;
import lombok.RequiredArgsConstructor;

import java.io.PrintWriter;
import java.util.stream.Collectors;

/**
 * Generates the {@code Q<ClassName>} source file for a {@link io.github.thinkfastpl.harbororm.api.annotations.View @View}
 * class, implementing {@link io.github.thinkfastpl.harbororm.api.metadata.QView QView<T>}.
 *
 * <p>The generated class contains typed {@code QColumn} fields for each {@code @Column(nullable = false)}-annotated
 * field in the view class, plus the standard {@code QView} interface methods
 * ({@code getTableName()}, {@code getBeanType()}, {@code getAllColumns()}).
 */
@RequiredArgsConstructor
class QViewWriter {
    private static final int INDENT = 4;
    private final PrintWriter writer;
    private final ViewMetadata metadata;

    void write() {
        writer.println(packageDeclaration());
        writer.println();
        writer.println(imports());
        writer.println();
        writer.println(classDeclaration());
        writer.println();

        // Column field declarations
        for (ColumnMetadata column : metadata.columns()) {
            writer.println(QColumnWriterUtils.generateColumnDeclaration(column).indent(INDENT));
        }

        writer.println("private final List<QColumn<?>> __allColumns;".indent(INDENT));
        writer.println("private final QTableName __tableName;".indent(INDENT));

        // Constructor
        writer.print(constructorDeclaration().indent(INDENT));
        writer.print(constructorBody().indent(INDENT * 2));
        writer.println("}".indent(INDENT));

        // QView interface methods
        writer.println(getTableNameMethod().indent(INDENT));
        writer.println(getBeanTypeMethod().indent(INDENT));
        writer.print(getAllColumnsMethod().indent(INDENT));

        writer.println("}");
    }

    private String packageDeclaration() {
        return "package %s;".formatted(metadata.packageName());
    }

    private String imports() {
        return """
                import io.github.thinkfastpl.harbororm.api.metadata.*;
                import java.util.List;
                import java.util.Map;""";
    }

    private String classDeclaration() {
        return "%sclass %s implements QView<%s> {".formatted(
                metadata.publicClass() ? "public " : "", metadata.qClassName(), metadata.className());
    }

    private String constructorDeclaration() {
        return "public %s(String alias) {\n".formatted(metadata.qClassName());
    }

    private String constructorBody() {
        StringBuilder sb = new StringBuilder();

        String schemaLiteral = metadata.schemaName() == null || metadata.schemaName().isBlank()
                ? "null"
                : "\"" + metadata.schemaName() + "\"";

        sb.append("this.__tableName = new QTableName(\"%s\", %s, alias);\n".formatted(
                metadata.viewName(), schemaLiteral));

        for (ColumnMetadata column : metadata.columns()) {
            sb.append(QColumnWriterUtils.generateColumnConstructorInit(column));
            sb.append("\n");
        }

        String cols = metadata.columns().stream()
                .map(c -> "this.%s".formatted(c.fieldName()))
                .collect(Collectors.joining(", "));
        sb.append("""
                this.__allColumns = List.of(
                        %s
                );
                """.formatted(cols));

        return sb.toString();
    }

    private String getTableNameMethod() {
        return """
                @Override
                public QTableName getTableName() {
                    return __tableName;
                }
                """;
    }

    private String getBeanTypeMethod() {
        return """
                @Override
                public Class<%s> getBeanType() {
                    return %s.class;
                }
                """.formatted(metadata.className(), metadata.className());
    }

    private String getAllColumnsMethod() {
        return """
                @Override
                public List<QColumn<?>> getAllColumns() {
                    return this.__allColumns;
                }
                """;
    }
}
