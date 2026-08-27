// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.dialect;

import io.github.thinkfastpl.harbororm.api.dialect.StandardDialects;
import io.github.thinkfastpl.harbororm.api.expression.*;
import io.github.thinkfastpl.harbororm.api.interval.Interval;
import io.github.thinkfastpl.harbororm.api.interval.IntervalUnit;
import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.api.metadata.QTableName;
import io.github.thinkfastpl.harbororm.api.query.data.*;
import io.github.thinkfastpl.harbororm.core.sql.dialect.AbstractSqlDialect;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;
import io.github.thinkfastpl.harbororm.core.utils.HarborListUtils;
import io.github.thinkfastpl.harbororm.core.utils.HarborStringUtils;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MariaDbSqlDialect extends AbstractSqlDialect {

    public MariaDbSqlDialect() {
        super(StandardDialects.MARIADB);
    }

    @Override
    public String escapeKeyword(@NonNull String value) {
        return "`" + value.replace("`", "``") + "`";
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
            appendOnDuplicateKeyUpdate(sb, params, data.getOnConflictData());
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

    private void appendOnDuplicateKeyUpdate(StringBuilder sb, List<SqlQuery.Param> params, OnConflictData conflict) {
        if (conflict.getWhereClause() != null) {
            throw new UnsupportedOperationException("ON CONFLICT ... WHERE is not supported by MariaDB dialect");
        }

        sb.append(" ON DUPLICATE KEY UPDATE ");

        if (conflict.getAction() == OnConflictData.ConflictAction.DO_NOTHING) {
            String col = escapeKeyword(conflict.getConflictColumns().get(0).getColumnName());
            sb.append(col).append(" = ").append(col);
            return;
        }

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
    }

    @Override
    public SqlQuery toQuery(@NonNull SelectQueryData data) {
        if (data.getFrom() instanceof FunctionCallTableSource<?> fnCall) {
            return toStoredFunctionCallQuery(data, fnCall);
        }

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
                        throw new UnsupportedOperationException("FULL OUTER JOIN is not supported by MariaDB dialect");
                    case CROSS:
                        sb.append(" CROSS JOIN ");
                        break;
                }

                if (join.isLateral()) {
                    throw new UnsupportedOperationException("LATERAL JOIN is not supported by MariaDB dialect");
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
        } else if (data.getOffset() != null) {
            // MariaDB requires LIMIT when OFFSET is present.
            sb.append(" LIMIT 18446744073709551615");
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
            throw new UnsupportedOperationException("UPDATE ... RETURNING is not supported by MariaDB dialect");
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

    private SqlQuery toStoredFunctionCallQuery(SelectQueryData data, FunctionCallTableSource<?> fnCall) {
        final List<?> fnColumns = fnCall.getColumns();
        final List<Expression<?>> selectExprs = data.getSelectExpressions();
        boolean projectionMatches = selectExprs.size() == fnColumns.size();
        if (projectionMatches) {
            for (int i = 0; i < selectExprs.size(); i++) {
                if (selectExprs.get(i) != fnColumns.get(i)) {
                    projectionMatches = false;
                    break;
                }
            }
        }
        if (!projectionMatches) {
            throw new UnsupportedOperationException(
                    "Custom select projections are not supported on stored function calls in MariaDB. " +
                            "The CALL emulation returns the procedure's full column list in its declared order; " +
                            "select all of the function's columns (e.g. via session.select(fn.call(args)).fetchAll()) " +
                            "instead of using a custom select list, COUNT, or EXISTS.");
        }

        if (HarborListUtils.isNotEmpty(data.getCtes())) {
            throw new UnsupportedOperationException("WITH (CTE) is not supported on stored function calls in MariaDB.");
        }
        if (data.isDistinct()) {
            throw new UnsupportedOperationException("DISTINCT is not supported on stored function calls in MariaDB.");
        }
        if (HarborListUtils.isNotEmpty(data.getJoins())) {
            throw new UnsupportedOperationException("JOIN is not supported on stored function calls in MariaDB.");
        }
        if (HarborListUtils.isNotEmpty(data.getWhereConditions())) {
            throw new UnsupportedOperationException("WHERE is not supported on stored function calls in MariaDB. The procedure must filter internally.");
        }
        if (HarborListUtils.isNotEmpty(data.getGroupByExpressions())) {
            throw new UnsupportedOperationException("GROUP BY is not supported on stored function calls in MariaDB.");
        }
        if (HarborListUtils.isNotEmpty(data.getHavingConditions())) {
            throw new UnsupportedOperationException("HAVING is not supported on stored function calls in MariaDB.");
        }
        if (HarborListUtils.isNotEmpty(data.getCombinations())) {
            throw new UnsupportedOperationException("UNION/INTERSECT/EXCEPT is not supported on stored function calls in MariaDB.");
        }
        if (HarborListUtils.isNotEmpty(data.getOrders())) {
            throw new UnsupportedOperationException("ORDER BY is not supported on stored function calls in MariaDB. The procedure must return rows in the desired order.");
        }
        if (data.getLimit() != null) {
            throw new UnsupportedOperationException("LIMIT is not supported on stored function calls in MariaDB.");
        }
        if (data.getOffset() != null) {
            throw new UnsupportedOperationException("OFFSET is not supported on stored function calls in MariaDB.");
        }
        if (data.isForUpdate()) {
            throw new UnsupportedOperationException("FOR UPDATE is not supported on stored function calls in MariaDB.");
        }

        final QTableName fnName = fnCall.getTableName();
        final StringBuilder sb = new StringBuilder("CALL ");
        if (fnName.getSchema() != null && !fnName.getSchema().isBlank()) {
            sb.append(escapeKeyword(fnName.getSchema())).append('.');
        }
        sb.append(escapeKeyword(fnName.getName())).append('(');

        final List<SqlQuery.Param> params = new ArrayList<>();
        HarborStringUtils.iterateAppendingJoiningDelimiter(
                sb,
                ", ",
                fnCall.getParams(),
                arg -> {
                    SqlQuery argSql = toSqlPart(arg, null);
                    sb.append(argSql.getSql());
                    params.addAll(argSql.getParams());
                }
        );
        sb.append(')');
        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderFunctionCallTableSource(@NonNull FunctionCallTableSource<?> functionTableSource) {
        throw new UnsupportedOperationException("Stored function calls are not supported as JOIN/subquery sources in MariaDB.");
    }

    @Override
    protected SqlQuery renderNextvalExpression(@NonNull NextvalExpression nextvalExpression, ColumnContext columnContext) {
        return new SqlQuery("NEXT VALUE FOR " + escapeKeyword(nextvalExpression.getSequence()), List.of());
    }

    @Override
    protected SqlQuery renderBinaryOperatorCondition(@NonNull BinaryOperatorCondition condition, ColumnContext columnContext) {
        // MariaDB has no IS [NOT] DISTINCT FROM: <=> is the NULL-safe equality operator
        if (condition.getOperator() == BinaryOperatorCondition.Operator.IS_DISTINCT_FROM || condition.getOperator() == BinaryOperatorCondition.Operator.IS_NOT_DISTINCT_FROM) {
            final SqlQuery leftPart = toSqlPart(
                    condition.getLeftExpression(),
                    ColumnContext.combineContexts(condition.getRightExpression().getColumnContext(dialectName), columnContext)
            );
            final SqlQuery rightPart = toSqlPart(
                    condition.getRightExpression(),
                    ColumnContext.combineContexts(condition.getLeftExpression().getColumnContext(dialectName), columnContext)
            );
            final String sql = condition.getOperator() == BinaryOperatorCondition.Operator.IS_NOT_DISTINCT_FROM
                    ? leftPart.getSql() + " <=> " + rightPart.getSql()
                    : "NOT (" + leftPart.getSql() + " <=> " + rightPart.getSql() + ")";
            return new SqlQuery(sql, HarborListUtils.merge(leftPart.getParams(), rightPart.getParams()));
        }
        return super.renderBinaryOperatorCondition(condition, columnContext);
    }

    @Override
    protected SqlQuery renderTernaryOperatorCondition(@NonNull TernaryOperatorCondition condition, ColumnContext columnContext) {
        // MariaDB has no BETWEEN SYMMETRIC: bounds are normalized with LEAST/GREATEST
        if (condition.getOperator() == TernaryOperatorCondition.Operator.BETWEEN_SYMMETRIC || condition.getOperator() == TernaryOperatorCondition.Operator.NOT_BETWEEN_SYMMETRIC) {
            final SqlQuery leftPart = toSqlPart(
                    condition.getLeftExpression(),
                    ColumnContext.combineContexts(columnContext, condition.getMiddleExpression().getColumnContext(dialectName), condition.getRightExpression().getColumnContext(dialectName))
            );
            final SqlQuery middlePart = toSqlPart(
                    condition.getMiddleExpression(),
                    ColumnContext.combineContexts(columnContext, condition.getLeftExpression().getColumnContext(dialectName), condition.getRightExpression().getColumnContext(dialectName))
            );
            final SqlQuery rightPart = toSqlPart(
                    condition.getRightExpression(),
                    ColumnContext.combineContexts(columnContext, condition.getLeftExpression().getColumnContext(dialectName), condition.getMiddleExpression().getColumnContext(dialectName))
            );

            final String not = condition.getOperator() == TernaryOperatorCondition.Operator.NOT_BETWEEN_SYMMETRIC ? "NOT " : "";
            // each bound appears twice (LEAST and GREATEST), so its params are added twice in SQL order
            final List<SqlQuery.Param> params = new ArrayList<>(leftPart.getParams().size() + (middlePart.getParams().size() + rightPart.getParams().size()) * 2);
            params.addAll(leftPart.getParams());
            params.addAll(middlePart.getParams());
            params.addAll(rightPart.getParams());
            params.addAll(middlePart.getParams());
            params.addAll(rightPart.getParams());

            return new SqlQuery(
                    "(" + leftPart.getSql() + " " + not + "BETWEEN LEAST(" + middlePart.getSql() + ", " + rightPart.getSql() + ") AND GREATEST(" + middlePart.getSql() + ", " + rightPart.getSql() + "))",
                    params
            );
        }
        return super.renderTernaryOperatorCondition(condition, columnContext);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCharLength(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("char_length(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionConcat(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        List<Expression<?>> newParams = functionExpression.getParams().isEmpty() ? new ArrayList<>(functionExpression.getParams()) : functionExpression.getParams();
        while (newParams.isEmpty()) {
            newParams.add(DSL.constant(String.class, ""));
        }

        final SqlQuery paramsQuery = renderParams(newParams, columnContext);
        return new SqlQuery("concat(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionLtrim(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() == 2) {
            SqlQuery strParamQuery = toSqlPart(functionExpression.getParams().get(0), null);
            SqlQuery remStrParamQuery = toSqlPart(functionExpression.getParams().get(1), null);
            return new SqlQuery(
                    "TRIM(LEADING " + remStrParamQuery.getSql() + " FROM " + strParamQuery.getSql() + ")",
                    HarborListUtils.merge(remStrParamQuery.getParams(), strParamQuery.getParams())
            );
        }
        return super.renderPortableFunctionExpressionLtrim(functionExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionRtrim(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() == 2) {
            SqlQuery strParamQuery = toSqlPart(functionExpression.getParams().get(0), null);
            SqlQuery remStrParamQuery = toSqlPart(functionExpression.getParams().get(1), null);
            return new SqlQuery(
                    "TRIM(TRAILING " + remStrParamQuery.getSql() + " FROM " + strParamQuery.getSql() + ")",
                    HarborListUtils.merge(remStrParamQuery.getParams(), strParamQuery.getParams())
            );
        }
        return super.renderPortableFunctionExpressionRtrim(functionExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBtrim(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() == 1) {
            SqlQuery strParamQuery = toSqlPart(functionExpression.getParams().get(0), null);
            return new SqlQuery(
                    "TRIM(" + strParamQuery.getSql() + ")",
                    strParamQuery.getParams()
            );
        }
        if (functionExpression.getParams().size() == 2) {
            SqlQuery strParamQuery = toSqlPart(functionExpression.getParams().get(0), null);
            SqlQuery remStrParamQuery = toSqlPart(functionExpression.getParams().get(1), null);
            return new SqlQuery(
                    "TRIM(BOTH " + remStrParamQuery.getSql() + " FROM " + strParamQuery.getSql() + ")",
                    HarborListUtils.merge(remStrParamQuery.getParams(), strParamQuery.getParams())
            );
        }
        throw new IllegalArgumentException("btrim function expects 1 or 2 params");
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionToHex(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("hex(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionJsonObject(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("json_object(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionJsonArray(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("json_array(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionJsonArrayLength(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("json_length(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionTruncate(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("truncate(" + paramsQuery.getSql() + ", 0)", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCurrentTime(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly 0 params");
        }
        return new SqlQuery("current_time()", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCurrentDate(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly 0 params");
        }
        return new SqlQuery("current_date()", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCurrentTimestamp(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly 0 params");
        }
        return new SqlQuery("current_timestamp()", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCurrentLocalDateTime(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly 0 params");
        }
        return new SqlQuery("current_timestamp()", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionDateAdd(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("date_add function expects 2 params");
        }
        final SqlQuery dateSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery daysSql = toSqlPart(functionExpression.getParams().get(1), columnContext);

        return new SqlQuery(
                "date_add(" + dateSql.getSql() + ", INTERVAL " + daysSql.getSql() + " DAY)",
                HarborListUtils.merge(dateSql.getParams(), daysSql.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionDateSub(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("date_sub function expects 2 params");
        }
        final SqlQuery dateSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery daysSql = toSqlPart(functionExpression.getParams().get(1), columnContext);

        return new SqlQuery(
                "date_add(" + dateSql.getSql() + ", INTERVAL -" + daysSql.getSql() + " DAY)",
                HarborListUtils.merge(dateSql.getParams(), daysSql.getParams())
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
                "datediff(" + date1Sql.getSql() + ", " + date2Sql.getSql() + ")",
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
                "dayofyear(" + paramSql.getSql() + ")",
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
                "(weekday(" + paramSql.getSql() + ") + 1)",
                paramSql.getParams()
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionExtractQuarter(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("extract_quarter function must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery(
                "quarter(" + paramSql.getSql() + ")",
                paramSql.getParams()
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionEpoch(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("epoch function must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery(
                "UNIX_TIMESTAMP(" + paramSql.getSql() + ")",
                paramSql.getParams()
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionDateTrunc(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("date_trunc function must have exactly one param");
        }

        SqlQuery sourceSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        String src = sourceSql.getSql().toString();

        String sql;
        int sourceReferences;

        switch (functionExpression.getFunction()) {
            case DATE_TRUNC_YEAR -> {
                sql = "CAST(DATE_FORMAT(" + src + ", '%Y-01-01 00:00:00') AS DATETIME)";
                sourceReferences = 1;
            }
            case DATE_TRUNC_MONTH -> {
                sql = "CAST(DATE_FORMAT(" + src + ", '%Y-%m-01 00:00:00') AS DATETIME)";
                sourceReferences = 1;
            }
            case DATE_TRUNC_DAY -> {
                sql = "CAST(DATE_FORMAT(" + src + ", '%Y-%m-%d 00:00:00') AS DATETIME)";
                sourceReferences = 1;
            }
            case DATE_TRUNC_HOUR -> {
                sql = "CAST(DATE_FORMAT(" + src + ", '%Y-%m-%d %H:00:00') AS DATETIME)";
                sourceReferences = 1;
            }
            case DATE_TRUNC_MINUTE -> {
                sql = "CAST(DATE_FORMAT(" + src + ", '%Y-%m-%d %H:%i:00') AS DATETIME)";
                sourceReferences = 1;
            }
            case DATE_TRUNC_SECOND -> {
                sql = "CAST(DATE_FORMAT(" + src + ", '%Y-%m-%d %H:%i:%s') AS DATETIME)";
                sourceReferences = 1;
            }
            case DATE_TRUNC_QUARTER -> {
                sql = "CAST(DATE_FORMAT(DATE_ADD(MAKEDATE(YEAR(" + src + "), 1),"
                        + " INTERVAL (QUARTER(" + src + ") - 1) * 3 MONTH),"
                        + " '%Y-%m-01 00:00:00') AS DATETIME)";
                sourceReferences = 2;
            }
            case DATE_TRUNC_WEEK -> {
                sql = "CAST(DATE_FORMAT(DATE_SUB(DATE(" + src + "),"
                        + " INTERVAL WEEKDAY(" + src + ") DAY),"
                        + " '%Y-%m-%d 00:00:00') AS DATETIME)";
                sourceReferences = 2;
            }
            case DATE_TRUNC_MILLISECOND -> {
                sql = "(" + src + " - INTERVAL (MICROSECOND(" + src + ") MOD 1000) MICROSECOND)";
                sourceReferences = 2;
            }
            case DATE_TRUNC_MICROSECOND -> {
                sql = src;
                sourceReferences = 1;
            }
            default -> throw new IllegalArgumentException("unsupported function: " + functionExpression.getFunction());
        }

        List<SqlQuery.Param> params = new ArrayList<>(sourceSql.getParams().size() * sourceReferences);
        for (int i = 0; i < sourceReferences; i++) {
            params.addAll(sourceSql.getParams());
        }
        return new SqlQuery(sql, params);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCbrt(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final CharSequence x = paramSql.getSql();
        final List<SqlQuery.Param> doubleParams = new ArrayList<>(paramSql.getParams().size() * 2);
        doubleParams.addAll(paramSql.getParams());
        doubleParams.addAll(paramSql.getParams());
        return new SqlQuery("(SIGN(" + x + ") * POW(ABS(" + x + "), 1E0 / 3E0))", doubleParams);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCosh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("cosh function must have exactly one param");
        }
        SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final CharSequence x = paramSql.getSql();
        final List<SqlQuery.Param> doubleParams = new ArrayList<>(paramSql.getParams().size() * 2);
        doubleParams.addAll(paramSql.getParams());
        doubleParams.addAll(paramSql.getParams());
        return new SqlQuery("((exp((" + x + " * 2)) + 1) / (exp(" + x + ") * 2))", doubleParams);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionSinh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("sinh function must have exactly one param");
        }
        SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final CharSequence x = paramSql.getSql();
        final List<SqlQuery.Param> doubleParams = new ArrayList<>(paramSql.getParams().size() * 2);
        doubleParams.addAll(paramSql.getParams());
        doubleParams.addAll(paramSql.getParams());
        return new SqlQuery("((exp((" + x + " * 2)) - 1) / (exp(" + x + ") * 2))", doubleParams);
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
    protected SqlQuery renderPortableFunctionExpressionBitXnor(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_xnor function must have exactly two params");
        }
        final SqlQuery left = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery right = toSqlPart(functionExpression.getParams().get(1), columnContext);
        return new SqlQuery(
                "(~((" + left.getSql() + " ^ " + right.getSql() + ")))",
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
                "(" + left.getSql() + " ^ " + right.getSql() + ")",
                HarborListUtils.merge(left.getParams(), right.getParams())
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
        return new SqlQuery("uuid()", List.of());
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

        sb.append("))");

        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderIntervalConstant(@NonNull IntervalConstant intervalConstant, ColumnContext columnContext) {
        Interval interval = intervalConstant.getInterval();
        if (interval.getUnit() == IntervalUnit.MILLISECOND) {
            return new SqlQuery("INTERVAL " + (interval.getValue() * 1000L) + " MICROSECOND", List.of());
        } else {
            return new SqlQuery("INTERVAL " + interval.getValue() + " " + interval.getUnit().name(), List.of());
        }
    }

    @Override
    protected SqlQuery renderWindowExpression(@NonNull WindowExpression<?> windowExpression, ColumnContext columnContext) {
        // MariaDB has no default argument for LAG/LEAD: lag(x, n, d) OVER w -> COALESCE(lag(x, n) OVER w, d)
        if (
                windowExpression.getFunction() instanceof PortableFunctionExpression<?> functionExpression
                        && (functionExpression.getFunction() == PortableFunctionExpression.Function.LAG || functionExpression.getFunction() == PortableFunctionExpression.Function.LEAD)
                        && functionExpression.getParams().size() == 3
        ) {

            final PortableFunctionExpression<?> functionWithoutDefault = new PortableFunctionExpression<>(
                    functionExpression.getFunction(),
                    functionExpression.getParams().subList(0, 2),
                    functionExpression.getJavaType()
            );

            final DefaultWindowExpression<?> windowWithoutDefault =
                    new DefaultWindowExpression<>(functionWithoutDefault, functionExpression.getJavaType());
            windowWithoutDefault.getPartitionBy().addAll(windowExpression.getPartitionBy());
            windowWithoutDefault.getOrderBy().addAll(windowExpression.getOrderBy());
            windowWithoutDefault.frameBetween(windowExpression.getFrameType(), windowExpression.getFrameStart(), windowExpression.getFrameEnd());

            final SqlQuery innerQuery = super.renderWindowExpression(windowWithoutDefault, columnContext);
            final SqlQuery defaultQuery = toSqlPart(functionExpression.getParams().get(2), columnContext);
            return new SqlQuery(
                    "COALESCE(" + innerQuery.getSql() + ", " + defaultQuery.getSql() + ")",
                    HarborListUtils.merge(innerQuery.getParams(), defaultQuery.getParams())
            );
        }

        return super.renderWindowExpression(windowExpression, columnContext);
    }

    @Override
    protected String getDbTypeName(@NonNull Class<?> clazz) {
        if (clazz == Boolean.class) return "UNSIGNED";
        if (clazz == Byte.class) return "SIGNED";
        if (clazz == Short.class) return "SIGNED";
        if (clazz == Integer.class) return "SIGNED";
        if (clazz == Long.class) return "SIGNED";
        if (clazz == Float.class) return "FLOAT";
        if (clazz == Double.class) return "DOUBLE";
        if (clazz == String.class) return "CHAR";
        if (clazz == java.math.BigDecimal.class) return "DECIMAL";
        if (clazz == java.math.BigInteger.class) return "DECIMAL";
        if (clazz == java.time.LocalDate.class) return "DATE";
        if (clazz == java.time.LocalTime.class) return "TIME";
        if (clazz == java.time.LocalDateTime.class) return "DATETIME";
        if (clazz == java.time.OffsetDateTime.class) return "DATETIME";
        if (clazz == byte[].class) return "BINARY";
        return super.getDbTypeName(clazz);
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
        final List<Condition> filter = aggregateExpression.getFilter();

        sb.append("GROUP_CONCAT(");

        if (aggregateExpression.getQuantifier() == AggregateQuantifier.DISTINCT) {
            sb.append("DISTINCT ");
        }

        // MariaDB has no FILTER clause: emulated with CASE WHEN inside the aggregate
        if (!filter.isEmpty()) {
            sb.append("CASE WHEN ");
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    " AND ",
                    filter,
                    condition -> appendExpression(sb, params, condition)
            );
            sb.append(" THEN ");
        }
        final Expression<?> valueExpression = aggregateExpression.getExpressions().get(0);
        appendExpression(sb, params, valueExpression, ColumnContext.combineContexts(columnContext, valueExpression.getColumnContext(dialectName)));
        if (!filter.isEmpty()) {
            sb.append(" END");
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

        sb.append(" SEPARATOR '").append(separator.replace("'", "''")).append("')");

        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionJsonArrayAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("json_arrayagg function must have exactly one param");
        }

        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();
        final List<Condition> filter = aggregateExpression.getFilter();
        final Expression<?> valueExpression = aggregateExpression.getExpressions().get(0);

        if (filter.isEmpty()) {
            sb.append("JSON_ARRAYAGG(");

            if (aggregateExpression.getQuantifier() == AggregateQuantifier.DISTINCT) {
                sb.append("DISTINCT ");
            }

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

            sb.append(")");

            return new SqlQuery(sb, params);
        }

        // MariaDB has no FILTER clause, and JSON_ARRAYAGG(CASE WHEN ...) aggregates a JSON null
        // for every filtered-out row. GROUP_CONCAT skips NULLs, so the array is assembled from
        // JSON-encoded elements instead; with no matching rows GROUP_CONCAT (and thus CONCAT)
        // yields SQL NULL, matching FILTER semantics on the other dialects.
        sb.append("CONCAT('[', GROUP_CONCAT(");

        if (aggregateExpression.getQuantifier() == AggregateQuantifier.DISTINCT) {
            sb.append("DISTINCT ");
        }

        sb.append("CASE WHEN ");
        HarborStringUtils.iterateAppendingJoiningDelimiter(
                sb,
                " AND ",
                filter,
                condition -> appendExpression(sb, params, condition)
        );
        sb.append(" THEN JSON_EXTRACT(JSON_ARRAY(");
        appendExpression(sb, params, valueExpression, ColumnContext.combineContexts(columnContext, valueExpression.getColumnContext(dialectName)));
        sb.append("), '$[0]') END");

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

        sb.append(" SEPARATOR ','), ']')");

        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionJsonObjectAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 2) {
            throw new IllegalArgumentException("json_objectagg aggregate function must have exactly two expressions");
        }
        return renderPortableAggregate("JSON_OBJECTAGG", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionArrayAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("ARRAY_AGG is not supported by MariaDB dialect");
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionAnyValue(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        // MariaDB has no ANY_VALUE aggregate function; MIN picks a representative value instead
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("any_value aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("MIN", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBoolAnd(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bool_and aggregate function must have exactly one expression");
        }
        final SqlQuery inner = renderPortableAggregate("MIN", aggregateExpression, columnContext);
        return new SqlQuery("(" + inner.getSql() + " <> 0)", inner.getParams());
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBoolOr(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bool_or aggregate function must have exactly one expression");
        }
        final SqlQuery inner = renderPortableAggregate("MAX", aggregateExpression, columnContext);
        return new SqlQuery("(" + inner.getSql() + " <> 0)", inner.getParams());
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionEvery(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("every aggregate function must have exactly one expression");
        }
        final SqlQuery inner = renderPortableAggregate("MIN", aggregateExpression, columnContext);
        return new SqlQuery("(" + inner.getSql() + " <> 0)", inner.getParams());
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitAndAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_and_agg aggregate function must have exactly one expression");
        }
        final SqlQuery inner = renderPortableAggregate("BIT_AND", aggregateExpression, columnContext);
        return new SqlQuery("CAST(" + inner.getSql() + " AS SIGNED)", inner.getParams());
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitOrAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_or_agg aggregate function must have exactly one expression");
        }
        final SqlQuery inner = renderPortableAggregate("BIT_OR", aggregateExpression, columnContext);
        return new SqlQuery("CAST(" + inner.getSql() + " AS SIGNED)", inner.getParams());
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitXorAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_xor_agg aggregate function must have exactly one expression");
        }
        final SqlQuery inner = renderPortableAggregate("BIT_XOR", aggregateExpression, columnContext);
        return new SqlQuery("CAST(" + inner.getSql() + " AS SIGNED)", inner.getParams());
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitNandAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_nand_agg aggregate function must have exactly one expression");
        }
        final SqlQuery inner = renderPortableAggregate("BIT_AND", aggregateExpression, columnContext);
        return new SqlQuery("CAST(~" + inner.getSql() + " AS SIGNED)", inner.getParams());
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitNorAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_nor_agg aggregate function must have exactly one expression");
        }
        final SqlQuery inner = renderPortableAggregate("BIT_OR", aggregateExpression, columnContext);
        return new SqlQuery("CAST(~" + inner.getSql() + " AS SIGNED)", inner.getParams());
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitXnorAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_xnor_agg aggregate function must have exactly one expression");
        }
        final SqlQuery inner = renderPortableAggregate("BIT_XOR", aggregateExpression, columnContext);
        return new SqlQuery("CAST(~" + inner.getSql() + " AS SIGNED)", inner.getParams());
    }

    @Override
    protected SqlQuery renderPortableAggregate(@NonNull String functionName, @NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();
        final List<Condition> filter = aggregateExpression.getFilter();

        sb.append(functionName).append('(');

        // MariaDB aggregate grammar has no ALL keyword; ALL is the default semantics anyway
        if (aggregateExpression.getQuantifier() == AggregateQuantifier.DISTINCT) {
            sb.append("DISTINCT ");
        }

        // MariaDB has no FILTER clause: agg(x) FILTER (WHERE c) -> agg(CASE WHEN c THEN x END),
        // COUNT(*) FILTER (WHERE c) -> COUNT(CASE WHEN c THEN 1 END)
        if (aggregateExpression.getExpressions().isEmpty()) {
            if (filter.isEmpty()) {
                sb.append('*');
            } else {
                sb.append("CASE WHEN ");
                HarborStringUtils.iterateAppendingJoiningDelimiter(
                        sb,
                        " AND ",
                        filter,
                        condition -> appendExpression(sb, params, condition)
                );
                sb.append(" THEN 1 END");
            }
        } else {
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    aggregateExpression.getExpressions(),
                    expr -> {
                        if (!filter.isEmpty()) {
                            sb.append("CASE WHEN ");
                            HarborStringUtils.iterateAppendingJoiningDelimiter(
                                    sb,
                                    " AND ",
                                    filter,
                                    condition -> appendExpression(sb, params, condition)
                            );
                            sb.append(" THEN ");
                        }
                        appendExpression(sb, params, expr, ColumnContext.combineContexts(columnContext, expr.getColumnContext(dialectName)));
                        if (!filter.isEmpty()) {
                            sb.append(" END");
                        }
                    }
            );
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

        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderJsonExpression(@NonNull JsonExpression jsonExpr, ColumnContext columnContext) {
        return switch (jsonExpr.getOperator()) {
            case LITERAL -> toSqlPart(jsonExpr.getSource(), jsonExpr.getSource().getColumnContext(dialectName));
            case EXTRACT, EXTRACT_PATH -> {
                JsonPath path = collectJsonPath(jsonExpr, List.of());
                SqlQuery rootSql = toSqlPart(path.root(), path.root().getColumnContext(dialectName));
                yield new SqlQuery(
                        "JSON_EXTRACT(" + rootSql.getSql() + ", " + buildJsonPathLiteral(path.keys()) + ")",
                        rootSql.getParams()
                );
            }
            default -> throw new UnsupportedOperationException(
                    "JSON operator " + jsonExpr.getOperator() + " is not supported on MariaDB.");
        };
    }

    @Override
    protected SqlQuery renderJsonTextExpression(@NonNull JsonTextExpression jsonTextExpr, ColumnContext columnContext) {
        return switch (jsonTextExpr.getOperator()) {
            case EXTRACT_TEXT, EXTRACT_PATH_TEXT -> {
                JsonPath path = collectJsonPath(jsonTextExpr.getSource(), java.util.Arrays.asList(jsonTextExpr.getArgs()));
                SqlQuery rootSql = toSqlPart(path.root(), path.root().getColumnContext(dialectName));
                yield new SqlQuery(
                        "JSON_UNQUOTE(JSON_EXTRACT(" + rootSql.getSql() + ", " + buildJsonPathLiteral(path.keys()) + "))",
                        rootSql.getParams()
                );
            }
            default -> throw new UnsupportedOperationException(
                    "JSON text operator " + jsonTextExpr.getOperator() + " is not supported on MariaDB.");
        };
    }

    @Override
    protected SqlQuery renderJsonCondition(@NonNull JsonCondition jsonCondition, ColumnContext columnContext) {
        final SqlQuery sourceSql = toSqlPart(jsonCondition.getSource(), jsonCondition.getSource().getColumnContext(dialectName));
        return switch (jsonCondition.getOperator()) {
            case CONTAINS -> {
                SqlQuery otherSql = toSqlPart(jsonCondition.getOther(), null);
                yield new SqlQuery(
                        "JSON_CONTAINS(" + sourceSql.getSql() + ", " + otherSql.getSql() + ")",
                        HarborListUtils.merge(sourceSql.getParams(), otherSql.getParams())
                );
            }
            case CONTAINED_IN -> {
                SqlQuery otherSql = toSqlPart(jsonCondition.getOther(), null);
                yield new SqlQuery(
                        "JSON_CONTAINS(" + otherSql.getSql() + ", " + sourceSql.getSql() + ")",
                        HarborListUtils.merge(otherSql.getParams(), sourceSql.getParams())
                );
            }
            case HAS_KEY, HAS_ANY_KEY -> renderContainsPath(sourceSql, "one", jsonCondition.getArgs());
            case HAS_ALL_KEYS -> renderContainsPath(sourceSql, "all", jsonCondition.getArgs());
            default -> throw new UnsupportedOperationException(
                    "JSON condition " + jsonCondition.getOperator() + " is not supported on MariaDB.");
        };
    }

    private SqlQuery renderContainsPath(SqlQuery sourceSql, String mode, String[] keys) {
        final StringBuilder sb = new StringBuilder("JSON_CONTAINS_PATH(");
        sb.append(sourceSql.getSql()).append(", '").append(mode).append("'");
        for (String key : keys) {
            sb.append(", ").append(buildJsonPathLiteral(List.of(key)));
        }
        sb.append(")");
        return new SqlQuery(sb, sourceSql.getParams());
    }

    private record JsonPath(Expression<?> root, List<String> keys) {
    }

    private JsonPath collectJsonPath(Expression<?> outer, List<String> seedKeys) {
        final List<String> keys = new ArrayList<>(seedKeys);
        Expression<?> current = outer;
        while (current instanceof JsonExpression je) {
            switch (je.getOperator()) {
                case EXTRACT -> {
                    keys.add(0, je.getArgs()[0]);
                    current = je.getSource();
                }
                case EXTRACT_PATH -> {
                    final String[] args = je.getArgs();
                    for (int i = args.length - 1; i >= 0; i--) {
                        keys.add(0, args[i]);
                    }
                    current = je.getSource();
                }
                case LITERAL -> {
                    return new JsonPath(je.getSource(), keys);
                }
                default -> {
                    return new JsonPath(je, keys);
                }
            }
        }
        return new JsonPath(current, keys);
    }

    private static final java.util.regex.Pattern SIMPLE_JSON_IDENTIFIER =
            java.util.regex.Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private static void appendJsonPathSegment(StringBuilder sb, String key) {
        if (SIMPLE_JSON_IDENTIFIER.matcher(key).matches()) {
            sb.append('.').append(key);
            return;
        }
        sb.append(".\"");
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (c == '\\' || c == '"') {
                sb.append('\\');
            }
            sb.append(c);
        }
        sb.append('"');
    }

    private static String buildJsonPathLiteral(List<String> keys) {
        final StringBuilder path = new StringBuilder("$");
        for (String key : keys) {
            appendJsonPathSegment(path, key);
        }
        return "'" + path.toString().replace("'", "''") + "'";
    }
}
