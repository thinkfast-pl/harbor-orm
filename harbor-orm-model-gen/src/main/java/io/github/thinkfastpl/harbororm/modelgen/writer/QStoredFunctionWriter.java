// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.writer;

import io.github.thinkfastpl.harbororm.modelgen.metadata.ColumnMetadata;
import io.github.thinkfastpl.harbororm.modelgen.metadata.StoredFunctionMetadata;
import io.github.thinkfastpl.harbororm.modelgen.metadata.StoredFunctionParamMetadata;
import lombok.RequiredArgsConstructor;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class QStoredFunctionWriter {
    private static final int INDENT = 4;
    private static final Map<String, String> PRIMITIVE_TO_BOXED = Map.of(
            "boolean", "Boolean",
            "byte", "Byte",
            "char", "Character",
            "short", "Short",
            "int", "Integer",
            "long", "Long",
            "float", "Float",
            "double", "Double"
    );
    private final PrintWriter writer;
    private final StoredFunctionMetadata metadata;

    void write() {
        writer.println(packageDeclaration());
        writer.println();
        writer.println(imports());
        writer.println();
        writer.println(classDeclaration());
        writer.println();

        // Column field declarations (return columns)
        for (ColumnMetadata column : metadata.columns()) {
            writer.println(QColumnWriterUtils.generateColumnDeclaration(column).indent(INDENT));
        }

        writer.println("private final List<QColumn<?>> __allColumns;".indent(INDENT));
        writer.println("private final QTableName __tableName;".indent(INDENT));

        // Constructor
        writer.print(constructorDeclaration().indent(INDENT));
        writer.print(constructorBody().indent(INDENT * 2));
        writer.println("}".indent(INDENT));

        // Expression-based call() method (primary)
        writer.println(expressionCallMethod().indent(INDENT));

        // Raw-value call() convenience method (only when params exist)
        String rawValueMethod = rawValueCallMethod();
        if (!rawValueMethod.isEmpty()) {
            writer.println(rawValueMethod.indent(INDENT));
        }

        // QTable interface methods
        writer.println(getTableNameMethod().indent(INDENT));
        writer.print(getAllColumnsMethod().indent(INDENT));

        writer.println("}");
    }

    private String packageDeclaration() {
        return "package %s;".formatted(metadata.packageName());
    }

    private String imports() {
        List<String> packages = List.of(
                "io.github.thinkfastpl.harbororm.api.expression.ConstantExpression",
                "io.github.thinkfastpl.harbororm.api.expression.Expression",
                "io.github.thinkfastpl.harbororm.api.expression.FunctionCallTableSource",
                "io.github.thinkfastpl.harbororm.api.metadata.*",
                "java.util.List",
                "java.util.Map"
        );
        return packages.stream()
                .map("import %s;"::formatted)
                .collect(Collectors.joining("\n"));
    }

    private String classDeclaration() {
        return "%sclass %s implements QTable {".formatted(
                metadata.publicClass() ? "public " : "", metadata.generatedClassName());
    }

    private String constructorDeclaration() {
        return "public %s(String alias) {\n".formatted(metadata.generatedClassName());
    }

    private String constructorBody() {
        StringBuilder sb = new StringBuilder();

        String schemaLiteral = metadata.schemaName() == null || metadata.schemaName().isBlank()
                ? "null"
                : "\"" + metadata.schemaName() + "\"";

        sb.append("this.__tableName = new QTableName(\"%s\", %s, alias);\n".formatted(
                metadata.functionName(), schemaLiteral));

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

    private String expressionCallMethod() {
        List<StoredFunctionParamMetadata> params = metadata.params();

        // Build parameter list: "Expression<Long> authorId, Expression<LocalDate> sinceDate"
        String paramList = params.stream()
                .map(p -> "Expression<%s> %s".formatted(boxTypeName(p.typeName()), p.name()))
                .collect(Collectors.joining(", "));

        // Build list literal: "List.of(authorId, sinceDate)"
        String listContent = params.stream()
                .map(StoredFunctionParamMetadata::name)
                .collect(Collectors.joining(", "));
        String listLiteral = params.isEmpty()
                ? "List.of()"
                : "List.of(%s)".formatted(listContent);

        return """
                public FunctionCallTableSource<%s> call(%s) {
                    return new FunctionCallTableSource<>(__tableName, __allColumns, %s, %s.class);
                }
                """.formatted(metadata.className(), paramList, listLiteral, metadata.className());
    }

    private String rawValueCallMethod() {
        List<StoredFunctionParamMetadata> params = metadata.params();

        if (params.isEmpty()) {
            return "";
        }

        // Build parameter list: "Long authorId, LocalDate sinceDate"
        String paramList = params.stream()
                .map(p -> "%s %s".formatted(p.typeName(), p.name()))
                .collect(Collectors.joining(", "));

        // Build delegation args: "new ConstantExpression<>(Long.class, authorId), new ConstantExpression<>(LocalDate.class, sinceDate)"
        String delegationArgs = params.stream()
                .map(p -> "new ConstantExpression<>(%s.class, %s)".formatted(boxTypeName(p.typeName()), p.name()))
                .collect(Collectors.joining(", "));

        return """
                public FunctionCallTableSource<%s> call(%s) {
                    return call(%s);
                }
                """.formatted(metadata.className(), paramList, delegationArgs);
    }

    private String getTableNameMethod() {
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

    private static String boxTypeName(String typeName) {
        return PRIMITIVE_TO_BOXED.getOrDefault(typeName, typeName);
    }
}
