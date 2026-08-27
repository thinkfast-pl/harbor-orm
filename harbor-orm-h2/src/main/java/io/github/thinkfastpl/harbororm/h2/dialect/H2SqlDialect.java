// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect;

import io.github.thinkfastpl.harbororm.api.dialect.StandardDialects;
import io.github.thinkfastpl.harbororm.api.expression.*;
import io.github.thinkfastpl.harbororm.api.interval.Interval;
import io.github.thinkfastpl.harbororm.api.lob.PortableBlob;
import io.github.thinkfastpl.harbororm.api.lob.PortableClob;
import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.api.metadata.ConverterData;
import io.github.thinkfastpl.harbororm.api.query.data.DeleteQueryData;
import io.github.thinkfastpl.harbororm.api.query.data.InsertQueryData;
import io.github.thinkfastpl.harbororm.api.query.data.SelectQueryData;
import io.github.thinkfastpl.harbororm.api.query.data.UpdateQueryData;
import io.github.thinkfastpl.harbororm.api.sql.SqlTypeHandler;
import io.github.thinkfastpl.harbororm.core.sql.TypeHandlerRegistry;
import io.github.thinkfastpl.harbororm.core.sql.dialect.AbstractSqlDialect;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;
import io.github.thinkfastpl.harbororm.core.utils.HarborListUtils;
import io.github.thinkfastpl.harbororm.core.utils.HarborSQLTypesUtils;
import io.github.thinkfastpl.harbororm.core.utils.HarborStringUtils;
import lombok.NonNull;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class H2SqlDialect extends AbstractSqlDialect {

    public H2SqlDialect() {
        super(StandardDialects.H2);
    }

    @Override
    public String escapeKeyword(@NonNull String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    @Override
    public SqlQuery toQuery(@NonNull InsertQueryData data) {
        if (data.getOnConflictData() != null) {
            throw new UnsupportedOperationException("ON CONFLICT clause is not supported by H2 dialect");
        }

        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();

        if (!data.getReturningExpressions().isEmpty()) {
            sb.append("SELECT ");

            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    data.getReturningExpressions(),
                    expression -> appendExpression(sb, params, expression)
            );

            sb.append(" FROM FINAL TABLE (");
        }

        sb.append("INSERT INTO ");
        sb.append(toSqlPart(data.getTable().withoutAlias()).getSql());

        if (!data.getRows().isEmpty()) {
            sb.append("(");
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    data.getColumns(),
                    c -> sb.append(escapeKeyword(c.getColumnName()))
            );
            sb.append(") VALUES ");

            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    data.getRows(),
                    row -> {
                        sb.append("(");
                        HarborStringUtils.iterateAppendingJoiningDelimiter(
                                sb,
                                ", ",
                                data.getColumns(),
                                c -> appendExpression(sb, params, row.get(c), c.getColumnContext(dialectName))
                        );
                        sb.append(")");
                    }
            );
        }

        if (!data.getReturningExpressions().isEmpty()) {
            sb.append(")");
        }

        return new SqlQuery(sb, params);
    }

    @Override
    public SqlQuery toQuery(@NonNull SelectQueryData data) {
        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();

        if (HarborListUtils.isNotEmpty(data.getCtes())) {
            sb.append("WITH ");

            if (data.isWithRecursive()) {
                sb.append("RECURSIVE ");
            }

            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    data.getCtes(),
                    cte -> appendCommonTableExpression(sb, params, cte)
            );

            sb.append(' ');
        }

        sb.append("SELECT");

        if (data.isDistinct()) {
            sb.append(" DISTINCT");
        }

        if (!data.getSelectExpressions().isEmpty()) {
            sb.append(" ");
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    data.getSelectExpressions(),
                    selectExpression -> appendExpression(sb, params, selectExpression)
            );
        }

        if (data.getFrom() != null) {
            sb.append(" FROM ");

            final SqlQuery sqlPartTable = toSqlPart(data.getFrom());
            sb.append(sqlPartTable.getSql());
            params.addAll(sqlPartTable.getParams());
        }

        if (data.getJoins() != null) {
            for (Join join : data.getJoins()) {
                switch (join.getType()) {
                    case INNER:
                        sb.append(" INNER JOIN ");
                        break;
                    case LEFT:
                        sb.append(" LEFT JOIN ");
                        break;
                    case RIGHT:
                        sb.append(" RIGHT JOIN ");
                        break;
                    case FULL_OUTER:
                        sb.append(" FULL OUTER JOIN ");
                        break;
                    case CROSS:
                        sb.append(" CROSS JOIN ");
                        break;
                }

                if (join.isLateral()) {
                    throw new UnsupportedOperationException("LATERAL JOIN is not supported by H2 dialect");
                }

                final SqlQuery sqlPartTable = toSqlPart(join.getTable());
                sb.append(sqlPartTable.getSql());
                params.addAll(sqlPartTable.getParams());

                if (join.getOn() != null) {
                    sb.append(" ON ");

                    final SqlQuery sqlPartOnCondition = toSqlPart(join.getOn(), null);
                    sb.append(sqlPartOnCondition.getSql());
                    params.addAll(sqlPartOnCondition.getParams());
                }
            }
        }

        if (HarborListUtils.isNotEmpty(data.getWhereConditions())) {
            sb.append(" WHERE ");
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    " AND ",
                    data.getWhereConditions(),
                    condition -> appendExpression(sb, params, condition)
            );
        }

        if (HarborListUtils.isNotEmpty(data.getGroupByExpressions())) {
            sb.append(" GROUP BY ");
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    data.getGroupByExpressions(),
                    groupByExpression -> appendExpression(sb, params, groupByExpression)
            );
        }

        if (HarborListUtils.isNotEmpty(data.getHavingConditions())) {
            sb.append(" HAVING ");
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    " AND ",
                    data.getHavingConditions(),
                    condition -> appendExpression(sb, params, condition)
            );
        }

        if (HarborListUtils.isNotEmpty(data.getCombinations())) {
            for (SelectQueryData.Combination combination : data.getCombinations()) {
                sb.append(" ");

                sb.append(switch (combination.getCombination()) {
                    case UNION -> "UNION";
                    case INTERSECT -> "INTERSECT";
                    case EXCEPT -> "EXCEPT";
                });

                if (combination.isAll()) {
                    sb.append(" ALL");
                }

                sb.append(" (");

                final SqlQuery combinationQueryPart = toQuery(extractQueryData(combination.getSelectExpression()));
                sb.append(combinationQueryPart.getSql());
                params.addAll(combinationQueryPart.getParams());

                sb.append(")");
            }
        }

        if (HarborListUtils.isNotEmpty(data.getOrders())) {
            sb.append(" ORDER BY ");
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    data.getOrders(),
                    order -> {
                        SqlQuery sqlPart = toSqlPart(order);
                        sb.append(sqlPart.getSql());
                        params.addAll(sqlPart.getParams());
                    }
            );
        }

        if (data.getLimit() != null) {
            sb.append(" LIMIT ");
            sb.append(data.getLimit());
        }

        if (data.getOffset() != null) {
            sb.append(" OFFSET ");
            sb.append(data.getOffset());
        }

        if (data.isForUpdate()) {
            sb.append(" FOR UPDATE");
        }

        return new SqlQuery(sb, params);
    }

    @Override
    public SqlQuery toQuery(@NonNull UpdateQueryData data) {
        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();

        if (!data.getReturningExpressions().isEmpty()) {
            sb.append("SELECT ");

            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    data.getReturningExpressions(),
                    expression -> appendExpression(sb, params, expression)
            );

            sb.append(" FROM FINAL TABLE (");
        }

        sb.append("UPDATE ");
        sb.append(toSqlPart(data.getTable()).getSql());

        if (data.getColumns().isEmpty()) {
            throw new IllegalArgumentException("No columns to update");
        }

        if (data.getColumns().size() != data.getValues().size()) {
            throw new IllegalArgumentException("Columns and values do not match");
        }

        sb.append(" SET ");

        HarborListUtils.iterateSimultaneously(
                data.getColumns(),
                data.getValues(),
                (qColumn, expression, index) -> {
                    if (index > 0) {
                        sb.append(", ");
                    }

                    SqlQuery expressionPart = toSqlPart(expression, qColumn.getColumnContext(dialectName));
                    sb
                            .append(escapeKeyword(qColumn.getColumnName()))
                            .append(" = ")
                            .append(expressionPart.getSql());

                    params.addAll(expressionPart.getParams());
                }
        );

        if (!data.getConditions().isEmpty()) {
            sb.append(" WHERE ");
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    " AND ",
                    data.getConditions(),
                    condition -> appendExpression(sb, params, condition)
            );
        }

        if (!data.getReturningExpressions().isEmpty()) {
            sb.append(")");
        }

        return new SqlQuery(sb, params);
    }

    @Override
    public SqlQuery toQuery(@NonNull DeleteQueryData data) {
        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();

        if (!data.getReturningExpressions().isEmpty()) {
            sb.append("SELECT ");

            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    data.getReturningExpressions(),
                    expression -> appendExpression(sb, params, expression)
            );

            sb.append(" FROM OLD TABLE (");
        }

        sb.append("DELETE FROM ");
        sb.append(toSqlPart(data.getTable()).getSql());

        if (!data.getConditions().isEmpty()) {
            sb.append(" WHERE ");
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    " AND ",
                    data.getConditions(),
                    condition -> appendExpression(sb, params, condition)
            );
        }


        if (!data.getReturningExpressions().isEmpty()) {
            sb.append(")");
        }

        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderConstantExpression(@NonNull ConstantExpression<?> constant, ColumnContext columnContext) {
        final ConverterData converterData = columnContext == null ? null : columnContext.getConverterData();
        final Class<?> typeHandlerClass = columnContext == null ? null : columnContext.getTypeHandlerClass();

        if (typeHandlerClass != null) {
            SqlTypeHandler<?> handler = TypeHandlerRegistry.get(typeHandlerClass);
            Object value = constant.getConstantValue();
            return new SqlQuery("?", List.of(new SqlQuery.Param(value, handler.sqlType(), handler)));
        }

        final Object value;
        final Class<?> javaType;

        if (converterData != null) {
            value = convertToDatabaseColumn(constant.getConstantValue(), converterData);
            javaType = converterData.getConverterClassDbType();
        } else if (columnContext != null && columnContext.isJson()) {
            value = serializeJsonValue(constant.getConstantValue());
            javaType = String.class;
        } else {
            value = constant.getConstantValue();
            javaType = constant.getJavaType();
        }

        if (columnContext != null && columnContext.isJson()) {
            // H2 stores a VARCHAR bound into a JSON column as a JSON string (double-encoded);
            // FORMAT JSON binds the text as a JSON value (also accepted by VARCHAR columns).
            return new SqlQuery(
                    "? FORMAT JSON",
                    List.of(new SqlQuery.Param(value, HarborSQLTypesUtils.getSqlTypeByClass(javaType)))
            );
        }

        if (PortableBlob.class.isAssignableFrom(javaType) || PortableClob.class.isAssignableFrom(javaType)) {
            return new SqlQuery("?", List.of(new SqlQuery.Param(value, Types.OTHER)));
        } else {
            final String h2Type = H2JavaTypeMapping.map(javaType, value);
            return new SqlQuery(
                    "CAST(? AS " + h2Type + ")",
                    List.of(
                            new SqlQuery.Param(value, HarborSQLTypesUtils.getSqlTypeByClass(javaType))
                    )
            );
        }
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionConcat(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        List<Expression<?>> newParams = functionExpression.getParams().size() < 2 ? new ArrayList<>(functionExpression.getParams()) : functionExpression.getParams();
        while (newParams.size() < 2) {
            newParams.add(DSL.constant(String.class, null));
        }

        final SqlQuery paramsQuery = renderParams(newParams, columnContext);
        return new SqlQuery("concat(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionConcatWs(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        List<Expression<?>> newParams = functionExpression.getParams().size() < 3 ? new ArrayList<>(functionExpression.getParams()) : functionExpression.getParams();
        while (newParams.size() < 3) {
            newParams.add(DSL.constant(String.class, null));
        }

        final SqlQuery paramsQuery = renderParams(newParams, columnContext);
        return new SqlQuery("concat_ws(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionToHex(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("to_hex function must have exactly one param");
        }
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("trim(to_char(" + paramsSqlQuery.getSql() + ", 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX'))", paramsSqlQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionReverse(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("reverse() is not supported on H2.");
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionMd5(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("md5() is not supported on H2.");
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionJsonObject(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        final List<Expression<?>> params = functionExpression.getParams();
        if (params.size() % 2 != 0) {
            throw new IllegalArgumentException("json_object requires an even number of arguments (alternating key/value pairs).");
        }

        final StringBuilder sb = new StringBuilder("json_object(");
        final List<SqlQuery.Param> sqlParams = new ArrayList<>();
        for (int i = 0; i < params.size(); i += 2) {
            if (i > 0) {
                sb.append(", ");
            }
            final SqlQuery keySql = toSqlPart(params.get(i), columnContext);
            final SqlQuery valueSql = toSqlPart(params.get(i + 1), columnContext);
            sb.append("KEY ").append(keySql.getSql()).append(" VALUE ").append(valueSql.getSql());
            sqlParams.addAll(keySql.getParams());
            sqlParams.addAll(valueSql.getParams());
        }
        sb.append(")");
        return new SqlQuery(sb.toString(), sqlParams);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionJsonArray(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().isEmpty()) {
            return new SqlQuery("json_array()", List.of());
        }
        // NULL ON NULL overrides H2's ABSENT ON NULL default so null elements are kept, as on PostgreSQL and MariaDB
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("json_array(" + paramsSqlQuery.getSql() + " NULL ON NULL)", paramsSqlQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionJsonArrayLength(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("json_array_length function must have exactly one param");
        }
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("CARDINALITY(" + paramsSqlQuery.getSql() + " FORMAT JSON)", paramsSqlQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCurrentTime(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly 0 params");
        }
        return new SqlQuery("LOCALTIME", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionAcosh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("acosh function must have exactly one param");
        }
        SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final CharSequence x = paramSql.getSql();
        final List<SqlQuery.Param> tripleParams = new ArrayList<>(paramSql.getParams().size() * 3);
        tripleParams.addAll(paramSql.getParams());
        tripleParams.addAll(paramSql.getParams());
        tripleParams.addAll(paramSql.getParams());
        return new SqlQuery("ln((" + x + " + sqrt(((" + x + " * " + x + ") - 1))))", tripleParams);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionAsinh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("asinh function must have exactly one param");
        }
        SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final CharSequence x = paramSql.getSql();
        final List<SqlQuery.Param> tripleParams = new ArrayList<>(paramSql.getParams().size() * 3);
        tripleParams.addAll(paramSql.getParams());
        tripleParams.addAll(paramSql.getParams());
        tripleParams.addAll(paramSql.getParams());
        return new SqlQuery("ln((" + x + " + sqrt(((" + x + " * " + x + ") + 1))))", tripleParams);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionAtanh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("atanh function must have exactly one param");
        }
        SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final CharSequence x = paramSql.getSql();
        final List<SqlQuery.Param> doubleParams = new ArrayList<>(paramSql.getParams().size() * 2);
        doubleParams.addAll(paramSql.getParams());
        doubleParams.addAll(paramSql.getParams());
        return new SqlQuery("(ln(((1 + " + x + ") / (1 - " + x + "))) / 2)", doubleParams);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCosh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("cosh function must have exactly one param");
        }
        SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery("cosh(" + paramSql.getSql() + ")", paramSql.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionSinh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("sinh function must have exactly one param");
        }
        SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery("sinh(" + paramSql.getSql() + ")", paramSql.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCoth(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("coth function must have exactly one param");
        }
        SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery("(1 / tanh(" + paramSql.getSql() + "))", paramSql.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionTanh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("tanh function must have exactly one param");
        }
        SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery("tanh(" + paramSql.getSql() + ")", paramSql.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCbrt(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("cbrt function must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final CharSequence x = paramSql.getSql();
        final List<SqlQuery.Param> doubleParams = new ArrayList<>(paramSql.getParams().size() * 2);
        doubleParams.addAll(paramSql.getParams());
        doubleParams.addAll(paramSql.getParams());
        return new SqlQuery("(SIGN(" + x + ") * POWER(ABS(" + x + "), 1.0 / 3))", doubleParams);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitAnd(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_and function must have exactly two params");
        }
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("bitand(" + paramsSqlQuery.getSql() + ")", paramsSqlQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitOr(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_or function must have exactly two params");
        }
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("bitor(" + paramsSqlQuery.getSql() + ")", paramsSqlQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitNand(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_nand function must have exactly two params");
        }
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("bitnot(bitand(" + paramsSqlQuery.getSql() + "))", paramsSqlQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitNor(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_nor function must have exactly two params");
        }
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("bitnot(bitor(" + paramsSqlQuery.getSql() + "))", paramsSqlQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitXnor(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_xnor function must have exactly two params");
        }
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("bitxnor(" + paramsSqlQuery.getSql() + ")", paramsSqlQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitXor(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_xor function must have exactly two params");
        }
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("bitxor(" + paramsSqlQuery.getSql() + ")", paramsSqlQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitGet(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_get function must have exactly two params");
        }
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery(
                "CASE bitget(" + paramsSqlQuery.getSql() + ") WHEN TRUE THEN 1 WHEN FALSE THEN 0 END",
                paramsSqlQuery.getParams()
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitNot(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("bit_not function must have exactly one param");
        }
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("bitnot(" + paramsSqlQuery.getSql() + ")", paramsSqlQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitSet(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_set function must have exactly two params");
        }
        SqlQuery left = toSqlPart(functionExpression.getParams().get(0), columnContext);
        SqlQuery right = toSqlPart(functionExpression.getParams().get(1), columnContext);
        return new SqlQuery(
                "bitor(" + left.getSql() + ", lshift(1, " + right.getSql() + "))",
                HarborListUtils.merge(left.getParams(), right.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionArrayAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("array_agg aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("array_agg", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitAndAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_and_agg aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("BIT_AND_AGG", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitNandAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_nand_agg aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("BIT_NAND_AGG", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitNorAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_nor_agg aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("BIT_NOR_AGG", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitOrAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_or_agg aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("BIT_OR_AGG", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitXorAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_xor_agg aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("BIT_XOR_AGG", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitXnorAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_xnor_agg aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("BIT_XNOR_AGG", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionGroupConcat(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 2) {
            throw new IllegalArgumentException("group_concat aggregate function must have exactly two expressions");
        }
        if (!(aggregateExpression.getExpressions().get(1) instanceof ConstantExpression<?> constant) || !(constant.getConstantValue() instanceof String separator)) {
            throw new IllegalArgumentException("group_concat separator must be a constant string");
        }

        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();

        sb.append("GROUP_CONCAT(");

        if (aggregateExpression.getQuantifier() == AggregateQuantifier.DISTINCT) {
            sb.append("DISTINCT ");
        } else if (aggregateExpression.getQuantifier() == AggregateQuantifier.ALL) {
            sb.append("ALL ");
        }

        final Expression<?> valueExpression = aggregateExpression.getExpressions().get(0);
        appendExpression(sb, params, valueExpression, ColumnContext.combineContexts(columnContext, valueExpression.getColumnContext(dialectName)));

        if (!aggregateExpression.getOrders().isEmpty()) {
            sb.append(" ORDER BY ");
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    aggregateExpression.getOrders(),
                    order -> {
                        final SqlQuery orderSqlPart = toSqlPart(order);
                        sb.append(orderSqlPart.getSql());
                        params.addAll(orderSqlPart.getParams());
                    }
            );
        }

        sb.append(" SEPARATOR '").append(separator.replace("'", "''")).append("')");

        if (!aggregateExpression.getFilter().isEmpty()) {
            sb.append(" FILTER (WHERE ");
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    " AND ",
                    aggregateExpression.getFilter(),
                    condition -> appendExpression(sb, params, condition)
            );
            sb.append(')');
        }

        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionJsonArrayAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("json_arrayagg function must have exactly one param");
        }

        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();

        sb.append("JSON_ARRAYAGG(");

        if (aggregateExpression.getQuantifier() == AggregateQuantifier.DISTINCT) {
            sb.append("DISTINCT ");
        } else if (aggregateExpression.getQuantifier() == AggregateQuantifier.ALL) {
            sb.append("ALL ");
        }

        final Expression<?> valueExpression = aggregateExpression.getExpressions().get(0);
        appendExpression(sb, params, valueExpression, ColumnContext.combineContexts(columnContext, valueExpression.getColumnContext(dialectName)));

        if (!aggregateExpression.getOrders().isEmpty()) {
            sb.append(" ORDER BY ");
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    aggregateExpression.getOrders(),
                    order -> {
                        final SqlQuery orderSqlPart = toSqlPart(order);
                        sb.append(orderSqlPart.getSql());
                        params.addAll(orderSqlPart.getParams());
                    }
            );
        }

        sb.append(')');

        if (!aggregateExpression.getFilter().isEmpty()) {
            sb.append(" FILTER (WHERE ");
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    " AND ",
                    aggregateExpression.getFilter(),
                    condition -> appendExpression(sb, params, condition)
            );
            sb.append(')');
        }

        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionJsonObjectAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 2) {
            throw new IllegalArgumentException("json_objectagg function must have exactly two params");
        }

        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();

        sb.append("JSON_OBJECTAGG(KEY ");

        final Expression<?> keyExpression = aggregateExpression.getExpressions().get(0);
        appendExpression(sb, params, keyExpression, ColumnContext.combineContexts(columnContext, keyExpression.getColumnContext(dialectName)));

        sb.append(" VALUE ");

        final Expression<?> valueExpression = aggregateExpression.getExpressions().get(1);
        appendExpression(sb, params, valueExpression, ColumnContext.combineContexts(columnContext, valueExpression.getColumnContext(dialectName)));

        sb.append(')');

        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionShl(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("shl function must have exactly two params");
        }
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("lshift(" + paramsSqlQuery.getSql() + ")", paramsSqlQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionShr(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("shr function must have exactly two params");
        }
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("rshift(" + paramsSqlQuery.getSql() + ")", paramsSqlQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCurrentLocalDateTime(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        return new SqlQuery("CURRENT_TIMESTAMP", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionDateAdd(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("date_add function expects 2 params");
        }

        final SqlQuery dateSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery daysSql = toSqlPart(functionExpression.getParams().get(1), columnContext);

        return new SqlQuery(
                "DATEADD(DAY, " + daysSql.getSql() + ", " + dateSql.getSql() + ")",
                HarborListUtils.merge(daysSql.getParams(), dateSql.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionDateSub(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("date_sub function expects 2 params");
        }

        final SqlQuery dateSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery secondSql = toSqlPart(functionExpression.getParams().get(1), columnContext);

        return new SqlQuery(
                "DATEADD(DAY, -(" + secondSql.getSql() + "), " + dateSql.getSql() + ")",
                HarborListUtils.merge(secondSql.getParams(), dateSql.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionDateDiff(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("date_diff function expects 2 params");
        }
        final SqlQuery date1Sql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery date2Sql = toSqlPart(functionExpression.getParams().get(1), columnContext);
        return new SqlQuery(
                "datediff(DAY, " + date2Sql.getSql() + ", " + date1Sql.getSql() + ")",
                HarborListUtils.merge(date2Sql.getParams(), date1Sql.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionRandom(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly 0 params");
        }
        return new SqlQuery("rand()", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionUuid(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly 0 params");
        }
        return new SqlQuery("random_uuid()", List.of());
    }

    @Override
    protected SqlQuery renderMultisetAggExpression(@NonNull MultisetAggExpression multisetAggExpression, ColumnContext columnContext) {
        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();

        sb.append("json_arrayagg(json_array(");

        HarborStringUtils.iterateAppendingJoiningDelimiter(
                sb,
                ", ",
                multisetAggExpression.getExpressions(),
                expr -> {
                    Expression<?> sqlExpr = stripAlias(expr);
                    SqlQuery exprSqlPart = toSqlPart(sqlExpr, ColumnContext.combineContexts(columnContext, sqlExpr.getColumnContext(dialectName)));
                    sb.append(exprSqlPart.getSql());
                    params.addAll(exprSqlPart.getParams());
                }
        );

        sb.append(" NULL ON NULL))");

        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderIntervalConstant(@NonNull IntervalConstant intervalConstant, ColumnContext columnContext) {
        Interval interval = intervalConstant.getInterval();
        int v = interval.getValue();
        return switch (interval.getUnit()) {
            case WEEK -> new SqlQuery("INTERVAL '" + (v * 7) + "' DAY", List.of());
            case MILLISECOND -> new SqlQuery(
                    "INTERVAL '" + BigDecimal.valueOf(v).movePointLeft(3).toPlainString() + "' SECOND(9, 6)",
                    List.of()
            );
            case MICROSECOND -> new SqlQuery(
                    "INTERVAL '" + BigDecimal.valueOf(v).movePointLeft(6).toPlainString() + "' SECOND(9, 6)",
                    List.of()
            );
            default -> super.renderIntervalConstant(intervalConstant, columnContext);
        };
    }

    @Override
    protected SqlQuery renderJsonExpression(@NonNull JsonExpression jsonExpr, ColumnContext columnContext) {
        if (jsonExpr.getOperator() == JsonExpression.Operator.LITERAL) {
            return toSqlPart(jsonExpr.getSource(), jsonExpr.getSource().getColumnContext(dialectName));
        }

        throw new UnsupportedOperationException("JSON operator " + jsonExpr.getOperator() + " is not supported on H2.");
    }

    @Override
    protected SqlQuery renderJsonTextExpression(@NonNull JsonTextExpression jsonTextExpr, ColumnContext columnContext) {
        throw new UnsupportedOperationException("JSON text extraction is not supported on H2.");
    }

    @Override
    protected SqlQuery renderJsonCondition(@NonNull JsonCondition jsonCondition, ColumnContext columnContext) {
        throw new UnsupportedOperationException("JSON condition " + jsonCondition.getOperator() + " is not supported on H2.");
    }

    @Override
    protected String getDbTypeName(@NonNull Class<?> clazz) {
        if (clazz == Boolean.class) return "BOOLEAN";
        if (clazz == Double.class) return "DOUBLE PRECISION";
        return super.getDbTypeName(clazz);
    }
}
