// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect;

import io.github.thinkfastpl.harbororm.api.dialect.StandardDialects;
import io.github.thinkfastpl.harbororm.api.expression.*;
import io.github.thinkfastpl.harbororm.api.interval.Interval;
import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.api.metadata.ConverterData;
import io.github.thinkfastpl.harbororm.api.query.data.*;
import io.github.thinkfastpl.harbororm.api.sql.SqlTypeHandler;
import io.github.thinkfastpl.harbororm.core.sql.TypeHandlerRegistry;
import io.github.thinkfastpl.harbororm.core.sql.dialect.AbstractSqlDialect;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;
import io.github.thinkfastpl.harbororm.core.utils.HarborListUtils;
import io.github.thinkfastpl.harbororm.core.utils.HarborSQLTypesUtils;
import io.github.thinkfastpl.harbororm.core.utils.HarborStringUtils;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PostgreSqlDialect extends AbstractSqlDialect {

    public PostgreSqlDialect() {
        super(StandardDialects.POSTGRES);
    }

    @Override
    public String escapeKeyword(@NonNull String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    @Override
    public SqlQuery toQuery(@NonNull InsertQueryData data) {
        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();

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

        if (data.getOnConflictData() != null) {
            appendOnConflict(sb, params, data.getOnConflictData());
        }

        if (!data.getReturningExpressions().isEmpty()) {
            sb.append(" RETURNING ");

            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    data.getReturningExpressions(),
                    expression -> appendExpression(sb, params, expression)
            );
        }

        return new SqlQuery(sb, params);
    }

    private void appendOnConflict(StringBuilder sb, List<SqlQuery.Param> params, OnConflictData conflict) {
        sb.append(" ON CONFLICT (");
        HarborStringUtils.iterateAppendingJoiningDelimiter(
                sb,
                ", ",
                conflict.getConflictColumns(),
                c -> sb.append(escapeKeyword(c.getColumnName()))
        );
        sb.append(")");

        if (conflict.getAction() == OnConflictData.ConflictAction.DO_NOTHING) {
            sb.append(" DO NOTHING");
            return;
        }

        sb.append(" DO UPDATE SET ");
        final var updateColumns = conflict.getUpdateColumns();
        final var updateValues = conflict.getUpdateValues();
        for (int i = 0; i < updateColumns.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(escapeKeyword(updateColumns.get(i).getColumnName()));
            sb.append(" = ");
            appendExpression(sb, params, updateValues.get(i));
        }

        if (conflict.getWhereClause() != null) {
            sb.append(" WHERE ");
            appendExpression(sb, params, conflict.getWhereClause());
        }
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
                    sb.append("LATERAL ");
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
            sb.append(" RETURNING ");

            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    data.getReturningExpressions(),
                    expression -> appendExpression(sb, params, expression)
            );
        }

        return new SqlQuery(sb, params);
    }

    @Override
    public SqlQuery toQuery(@NonNull DeleteQueryData data) {
        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();

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
            sb.append(" RETURNING ");

            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    data.getReturningExpressions(),
                    expression -> appendExpression(sb, params, expression)
            );
        }

        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderConstantExpression(@NonNull ConstantExpression<?> constantExpression, ColumnContext columnContext) {
        final ConverterData converterData = columnContext == null ? null : columnContext.getConverterData();
        final Class<?> typeHandlerClass = columnContext == null ? null : columnContext.getTypeHandlerClass();

        if (typeHandlerClass != null) {
            SqlTypeHandler<?> handler = TypeHandlerRegistry.get(typeHandlerClass);
            Object value = constantExpression.getConstantValue();
            return new SqlQuery("?", List.of(new SqlQuery.Param(value, handler.sqlType(), handler)));
        }

        final Object value;
        final Class<?> javaType;

        if (converterData != null) {
            value = convertToDatabaseColumn(constantExpression.getConstantValue(), converterData);
            javaType = converterData.getConverterClassDbType();
        } else if (columnContext != null && columnContext.isJson()) {
            value = serializeJsonValue(constantExpression.getConstantValue());
            javaType = String.class;
        } else {
            value = constantExpression.getConstantValue();
            javaType = constantExpression.getJavaType();
        }

        if (columnContext != null && columnContext.isJson()) {
            String customType = columnContext.getCustomType();
            String cast = HarborStringUtils.isNotBlank(customType) ? customType : "jsonb";
            return new SqlQuery("?::" + cast, List.of(new SqlQuery.Param(value, HarborSQLTypesUtils.getSqlTypeByClass(javaType))));
        }

        if (columnContext != null && HarborStringUtils.isNotBlank(columnContext.getCustomType())) {
            return new SqlQuery("?::" + columnContext.getCustomType(), List.of(new SqlQuery.Param(value, HarborSQLTypesUtils.getSqlTypeByClass(javaType))));
        }

        return new SqlQuery("?", List.of(new SqlQuery.Param(value, HarborSQLTypesUtils.getSqlTypeByClass(javaType))));
    }



    @Override
    protected SqlQuery renderPortableFunctionExpressionConcat(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().isEmpty()) {
            return super.renderPortableFunctionExpressionConcat(
                    new PortableFunctionExpression<>(
                            PortableFunctionExpression.Function.CONCAT,
                            List.of(DSL.constant("")),
                            String.class),
                    columnContext
            );
        } else {
            return super.renderPortableFunctionExpressionConcat(functionExpression, columnContext);
        }
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionToHex(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("to_hex function must have exactly one param");
        }
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("upper(to_hex(" + paramsSqlQuery.getSql() + "))", paramsSqlQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionJsonObject(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("json_build_object(" + paramsSqlQuery.getSql() + ")", paramsSqlQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionJsonArray(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("json_build_array(" + paramsSqlQuery.getSql() + ")", paramsSqlQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionJsonArrayLength(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("json_array_length function must have exactly one param");
        }
        SqlQuery paramsSqlQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("json_array_length(CAST(" + paramsSqlQuery.getSql() + " AS json))", paramsSqlQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionPercentileCont(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("percentile_cont() is not supported on PostgreSQL.");
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionPercentileDisc(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("percentile_disc() is not supported on PostgreSQL.");
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionRound(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() == 2 && !BigDecimal.class.isAssignableFrom(functionExpression.getParams().get(0).getJavaType())) {
            Expression<BigDecimal> value = functionExpression.getParams().get(0).cast(BigDecimal.class);
            return super.renderPortableFunctionExpressionRound(
                    new PortableFunctionExpression<>(
                            PortableFunctionExpression.Function.ROUND,
                            List.of(value, functionExpression.getParams().get(1)), functionExpression.getJavaType()
                    ),
                    columnContext
            );
        } else {
            return super.renderPortableFunctionExpressionRound(functionExpression, columnContext);
        }
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionRegexReplace(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 3) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly three params");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("regexp_replace(" + paramsQuery.getSql() + ", 'g')", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionSpace(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("repeat(' ', " + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionLog(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        final List<Expression<?>> castParams = functionExpression.getParams().stream()
                .map(p -> {
                    if (
                            Float.class.isAssignableFrom(p.getJavaType())
                                    || float.class.isAssignableFrom(p.getJavaType())
                                    || Double.class.isAssignableFrom(p.getJavaType())
                                    || double.class.isAssignableFrom(p.getJavaType())
                    ) {
                        return p.cast(BigDecimal.class);
                    } else {
                        return p;
                    }
                })
                .toList();

        return super.renderPortableFunctionExpressionLog(
                new PortableFunctionExpression<>(PortableFunctionExpression.Function.LOG, castParams, functionExpression.getJavaType()),
                columnContext
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitXnor(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_xnor function must have exactly two params");
        }
        final SqlQuery left = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery right = toSqlPart(functionExpression.getParams().get(1), columnContext);
        return new SqlQuery(
                "(~((" + left.getSql() + " # " + right.getSql() + ")))",
                HarborListUtils.merge(left.getParams(), right.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitXor(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_xor function must have exactly two params");
        }
        final SqlQuery left = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery right = toSqlPart(functionExpression.getParams().get(1), columnContext);
        return new SqlQuery(
                "(" + left.getSql() + " # " + right.getSql() + ")",
                HarborListUtils.merge(left.getParams(), right.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCurrentLocalDateTime(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        return new SqlQuery("cast(CURRENT_TIMESTAMP AS timestamp)", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionDateDiff(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("date_diff function expects 2 params");
        }

        final SqlQuery date1Sql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery date2Sql = toSqlPart(functionExpression.getParams().get(1), columnContext);

        return new SqlQuery(
                "(" + date1Sql.getSql() + " - " + date2Sql.getSql() + ")",
                HarborListUtils.merge(date1Sql.getParams(), date2Sql.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionExtractDayOfYear(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("extract_day_of_year function must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery(
                "extract(DOY FROM " + paramSql.getSql() + ")",
                paramSql.getParams()
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionExtractIsoDayOfWeek(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("extract_iso_day_of_week function must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery(
                "extract(ISODOW FROM " + paramSql.getSql() + ")",
                paramSql.getParams()
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionIfNull(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly two params");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("coalesce(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionNvl(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly two params");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("coalesce(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionUuid(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly 0 params");
        }
        return new SqlQuery("gen_random_uuid()", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionRandom(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly 0 params");
        }
        return new SqlQuery("random()", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCoth(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("coth function must have exactly one param");
        }
        SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final CharSequence x = paramSql.getSql();
        final List<SqlQuery.Param> doubleParams = new ArrayList<>(paramSql.getParams().size() * 2);
        doubleParams.addAll(paramSql.getParams());
        doubleParams.addAll(paramSql.getParams());
        return new SqlQuery("((exp((" + x + " * 2)) + 1) / (exp((" + x + " * 2)) - 1))", doubleParams);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionTanh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("tanh function must have exactly one param");
        }
        SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final CharSequence x = paramSql.getSql();
        final List<SqlQuery.Param> doubleParams = new ArrayList<>(paramSql.getParams().size() * 2);
        doubleParams.addAll(paramSql.getParams());
        doubleParams.addAll(paramSql.getParams());
        return new SqlQuery("((exp((" + x + " * 2)) - 1) / (exp((" + x + " * 2)) + 1))", doubleParams);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionNumNulls(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("num_nulls(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionNumNonNulls(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("num_nonnulls(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCurrentTime(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly 0 params");
        }
        return new SqlQuery("cast(CURRENT_TIME AS time)", List.of());
    }

    @Override
    protected SqlQuery renderMultisetAggExpression(@NonNull MultisetAggExpression multisetAggExpression, ColumnContext columnContext) {
        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();

        sb.append("coalesce(json_agg(jsonb_build_array(");

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

        sb.append(")), json_build_array())");

        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionArrayAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("array_agg aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("array_agg", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionGroupConcat(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 2) {
            throw new IllegalArgumentException("group_concat aggregate function must have exactly two expressions");
        }

        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();

        sb.append("string_agg(");

        if (aggregateExpression.getQuantifier() == AggregateQuantifier.DISTINCT) {
            sb.append("DISTINCT ");
        } else if (aggregateExpression.getQuantifier() == AggregateQuantifier.ALL) {
            sb.append("ALL ");
        }

        final Expression<?> valueExpression = aggregateExpression.getExpressions().get(0);
        sb.append("CAST(");
        appendExpression(sb, params, valueExpression, ColumnContext.combineContexts(columnContext, valueExpression.getColumnContext(dialectName)));
        sb.append(" AS varchar), ");

        final Expression<?> separatorExpression = aggregateExpression.getExpressions().get(1);
        if (separatorExpression instanceof ConstantExpression<?> constant && constant.getConstantValue() instanceof String separator) {
            sb.append('\'').append(separator.replace("'", "''")).append('\'');
        } else {
            // PostgreSQL accepts an arbitrary expression as the string_agg delimiter
            appendExpression(sb, params, separatorExpression, columnContext);
        }

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
    protected SqlQuery renderPortableAggregateExpressionJsonArrayAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("json_agg function must have exactly one param");
        }

        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();

        sb.append("json_agg(");

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
            throw new IllegalArgumentException("json_object_agg aggregate function must have exactly two expressions");
        }
        return renderPortableAggregate("json_object_agg", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderBinaryOperatorCondition(@NonNull BinaryOperatorCondition condition, ColumnContext columnContext) {
        if (condition.getOperator() == BinaryOperatorCondition.Operator.IN && condition.getRightExpression() instanceof ConstantsExpression<?> constantsExpression) {
            ColumnContext leftColumnContext = ColumnContext.combineContexts(condition.getLeftExpression().getColumnContext(dialectName), columnContext);
            ConverterData converterDataForConstants = leftColumnContext == null ? null : leftColumnContext.getConverterData();

            SqlQuery leftPart = toSqlPart(condition.getLeftExpression(), leftColumnContext);

            final Class<?> dbType;
            final Object[] arrayValues;

            if (converterDataForConstants != null) {
                dbType = converterDataForConstants.getConverterClassDbType();
                arrayValues = constantsExpression.getConstantsValue().stream()
                        .map(v -> convertToDatabaseColumn(v, converterDataForConstants))
                        .toArray();
            } else {
                dbType = constantsExpression.getJavaType();
                arrayValues = constantsExpression.getConstantsValue().toArray();
            }

            String customType = leftColumnContext == null ? null : leftColumnContext.getCustomType();
            String pgArrayElementType = HarborStringUtils.isNotBlank(customType)
                    ? customType
                    : getPgArrayTypeName(dbType);
            PostgreSqlArrayValue arrayValue = new PostgreSqlArrayValue(pgArrayElementType, arrayValues);

            return new SqlQuery(
                    leftPart.getSql() + " = ANY (?)",
                    HarborListUtils.merge(leftPart.getParams(), List.of(new SqlQuery.Param(arrayValue, java.sql.Types.ARRAY)))
            );
        }
        return super.renderBinaryOperatorCondition(condition, columnContext);
    }

    @Override
    protected SqlQuery renderIntervalConstant(@NonNull IntervalConstant intervalConstant, ColumnContext columnContext) {
        Interval interval = intervalConstant.getInterval();
        return new SqlQuery(
                "interval '" + interval.getValue() + " " + interval.getUnit().name().toLowerCase() + "'",
                List.of()
        );
    }

    @Override
    protected String getDbTypeName(@NonNull Class<?> clazz) {
        if (clazz == Boolean.class) return "boolean";
        if (clazz == Byte.class) return "smallint";
        if (clazz == Double.class) return "double precision";
        return super.getDbTypeName(clazz);
    }

    private static String escapeSqlString(String value) {
        return value.replace("'", "''");
    }

    @Override
    protected SqlQuery renderJsonExpression(@NonNull JsonExpression jsonExpr, ColumnContext columnContext) {
        SqlQuery sourceSql = toSqlPart(jsonExpr.getSource(), jsonExpr.getSource().getColumnContext(dialectName));
        return switch (jsonExpr.getOperator()) {
            case EXTRACT -> {
                String key = escapeSqlString(jsonExpr.getArgs()[0]);
                yield new SqlQuery(sourceSql.getSql() + " -> '" + key + "'", sourceSql.getParams());
            }
            case EXTRACT_PATH -> {
                String path = "{" + Arrays.stream(jsonExpr.getArgs()).map(PostgreSqlDialect::escapeSqlString).collect(Collectors.joining(",")) + "}";
                yield new SqlQuery(sourceSql.getSql() + " #> '" + path + "'", sourceSql.getParams());
            }
            case LITERAL -> toSqlPart(jsonExpr.getSource(), new ColumnContext(null, null, true));
            default -> throw new UnsupportedOperationException("Unexpected JSON operator: " + jsonExpr.getOperator());
        };
    }

    @Override
    protected SqlQuery renderJsonTextExpression(@NonNull JsonTextExpression jsonTextExpr, ColumnContext columnContext) {
        SqlQuery sourceSql = toSqlPart(jsonTextExpr.getSource(), jsonTextExpr.getSource().getColumnContext(dialectName));
        return switch (jsonTextExpr.getOperator()) {
            case EXTRACT_TEXT -> {
                String key = escapeSqlString(jsonTextExpr.getArgs()[0]);
                yield new SqlQuery(sourceSql.getSql() + " ->> '" + key + "'", sourceSql.getParams());
            }
            case EXTRACT_PATH_TEXT -> {
                String path = "{" + Arrays.stream(jsonTextExpr.getArgs()).map(PostgreSqlDialect::escapeSqlString).collect(Collectors.joining(",")) + "}";
                yield new SqlQuery(sourceSql.getSql() + " #>> '" + path + "'", sourceSql.getParams());
            }
            default -> throw new UnsupportedOperationException("Unexpected JSON text operator: " + jsonTextExpr.getOperator());
        };
    }

    @Override
    protected SqlQuery renderJsonCondition(@NonNull JsonCondition jsonCondition, ColumnContext columnContext) {
        SqlQuery sourceSql = toSqlPart(jsonCondition.getSource(), jsonCondition.getSource().getColumnContext(dialectName));
        return switch (jsonCondition.getOperator()) {
            case CONTAINS -> {
                SqlQuery otherSql = toSqlPart(jsonCondition.getOther(), null);
                yield new SqlQuery(sourceSql.getSql() + " @> " + otherSql.getSql(),
                        HarborListUtils.merge(sourceSql.getParams(), otherSql.getParams()));
            }
            case CONTAINED_IN -> {
                SqlQuery otherSql = toSqlPart(jsonCondition.getOther(), null);
                yield new SqlQuery(sourceSql.getSql() + " <@ " + otherSql.getSql(),
                        HarborListUtils.merge(sourceSql.getParams(), otherSql.getParams()));
            }
            case HAS_KEY -> {
                String key = escapeSqlString(jsonCondition.getArgs()[0]);
                yield new SqlQuery(sourceSql.getSql() + " ?? '" + key + "'", sourceSql.getParams());
            }
            case HAS_ANY_KEY -> {
                String keys = Arrays.stream(jsonCondition.getArgs()).map(PostgreSqlDialect::escapeSqlString).collect(Collectors.joining("','"));
                yield new SqlQuery(sourceSql.getSql() + " ??| array['" + keys + "']", sourceSql.getParams());
            }
            case HAS_ALL_KEYS -> {
                String keys = Arrays.stream(jsonCondition.getArgs()).map(PostgreSqlDialect::escapeSqlString).collect(Collectors.joining("','"));
                yield new SqlQuery(sourceSql.getSql() + " ??& array['" + keys + "']", sourceSql.getParams());
            }
            default -> throw new UnsupportedOperationException("Unexpected JSON condition: " + jsonCondition.getOperator());
        };
    }

    /**
     * Returns the PostgreSQL type name suitable for {@link java.sql.Connection#createArrayOf}
     * based on the Java class of the array elements.
     */
    private String getPgArrayTypeName(@NonNull Class<?> clazz) {
        if (clazz == String.class) return "text";
        if (clazz == Integer.class) return "int4";
        if (clazz == Long.class) return "int8";
        if (clazz == Short.class) return "int2";
        if (clazz == Boolean.class) return "bool";
        if (clazz == Float.class) return "float4";
        if (clazz == Double.class) return "float8";
        if (clazz == BigDecimal.class) return "numeric";
        if (clazz == java.math.BigInteger.class) return "numeric";
        if (clazz == java.time.LocalDate.class) return "date";
        if (clazz == java.time.LocalTime.class) return "time";
        if (clazz == java.time.LocalDateTime.class) return "timestamp";
        if (clazz == java.time.OffsetDateTime.class) return "timestamptz";
        if (clazz == java.util.UUID.class) return "uuid";
        if (clazz == Byte.class) return "int2";
        return "text"; // safe fallback — PostgreSQL will cast
    }
}
