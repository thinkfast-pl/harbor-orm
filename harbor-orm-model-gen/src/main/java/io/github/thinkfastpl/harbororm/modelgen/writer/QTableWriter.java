// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.writer;

import io.github.thinkfastpl.harbororm.modelgen.metadata.ColumnMetadata;
import io.github.thinkfastpl.harbororm.modelgen.metadata.TableMetadata;
import lombok.RequiredArgsConstructor;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class QTableWriter {
    private static final int INDENT = 4;
    private final PrintWriter writer;
    private final TableMetadata metadata;

    void write() {
        writer.println(packageDeclaration());
        writer.println();

        writer.println(imports());
        writer.println();

        writer.println(classDeclaration());
        writer.println();

        for (ColumnMetadata column : metadata.columns()) {
            writer.println(QColumnWriterUtils.generateColumnDeclaration(column).indent(INDENT));
        }

        writer.println(allColumnsDeclaration().indent(INDENT));

        writer.println(tableNameDeclaration().indent(INDENT));

        writer.print(constructorDeclaration().indent(INDENT));
        writer.print(constructorAliasAssignment().indent(INDENT * 2));

        for (ColumnMetadata column : metadata.columns()) {
            writer.print(QColumnWriterUtils.generateColumnConstructorInit(column).indent(INDENT * 2));
        }

        writer.print(allColumnsAssignment().indent(INDENT * 2));

        writer.println(endConstructorDeclaration().indent(INDENT));

        writer.println(getTableName().indent(INDENT));

        writer.print(getAllColumnsMethod().indent(INDENT));

        writer.println(endClassDeclaration());
    }

    private String packageDeclaration() {
        return "package %s;".formatted(metadata.packageName());
    }

    private String imports() {
        List<String> packages = List.of(
                "io.github.thinkfastpl.harbororm.api.metadata.*",
                "java.util.List",
                "java.util.Map"
        );

        return packages.stream()
                .map("import %s;"::formatted)
                .collect(Collectors.joining("\n"));
    }

    private String classDeclaration() {
        return "public class %s implements QTable {".formatted(metadata.qTableClassName());
    }

    private String allColumnsDeclaration() {
        return "private final List<QColumn<?>> __allColumns;";
    }

    private String tableNameDeclaration() {
        return "private final QTableName __tableName;";
    }

    private String constructorDeclaration() {
        return "public %s(String alias) {".formatted(metadata.qTableClassName());
    }

    private String constructorAliasAssignment() {
        return "this.__tableName = new QTableName(\"%s\", %s, alias);".formatted(
                metadata.tableName(),
                metadata.schemaName() == null ? "null" : "\"" + metadata.schemaName() + "\""
        );
    }

    private String allColumnsAssignment() {
        String cols = metadata.columns().stream()
                .map(c -> "this.%s".formatted(c.fieldName()))
                .collect(Collectors.joining(", "));
        return """
                this.__allColumns = List.of(
                        %s
                );
                """.formatted(cols);
    }

    private String endConstructorDeclaration() {
        return "}";
    }

    private String getTableName() {
        return """
                @Override
                public QTableName getTableName() {
                    return __tableName;
                }
                """;
    }

    private String getAllColumnsMethod() {
        return """
                @Override
                public List<QColumn<?>> getAllColumns() {
                    return this.__allColumns;
                }
                """;
    }

    private String endClassDeclaration() {
        return "}";
    }
}
