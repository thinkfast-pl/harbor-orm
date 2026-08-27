// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.core.sql.dialect;

import io.github.thinkfastpl.harbororm.api.converter.AttributeConverter;
import io.github.thinkfastpl.harbororm.api.converter.AttributeConverterSupplier;
import io.github.thinkfastpl.harbororm.api.converter.JsonSerializer;
import io.github.thinkfastpl.harbororm.api.expression.*;
import io.github.thinkfastpl.harbororm.api.interval.Interval;
import io.github.thinkfastpl.harbororm.api.metadata.*;
import io.github.thinkfastpl.harbororm.api.query.data.SelectQueryData;
import io.github.thinkfastpl.harbororm.api.sql.SqlTypeHandler;
import io.github.thinkfastpl.harbororm.core.sql.TypeHandlerRegistry;
import io.github.thinkfastpl.harbororm.core.utils.HarborBeanUtils;
import io.github.thinkfastpl.harbororm.core.utils.HarborListUtils;
import io.github.thinkfastpl.harbororm.core.utils.HarborSQLTypesUtils;
import io.github.thinkfastpl.harbororm.core.utils.HarborStringUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AbstractSqlDialect extends EmptySqlDialect {
    private static final SqlQuery EMPTY_SQL_QUERY = new SqlQuery("", Collections.emptyList());

    protected final String dialectName;
    private AttributeConverterSupplier attributeConverterSupplier;
    private JsonSerializer jsonSerializer;

    @Override
    public String getName() {
        return dialectName;
    }

    @Override
    public void init(@NonNull AttributeConverterSupplier attributeConverterSupplier, JsonSerializer jsonSerializer) {
        this.attributeConverterSupplier = attributeConverterSupplier;
        this.jsonSerializer = jsonSerializer;
    }

    @Override
    protected SqlQuery renderQTableName(@NonNull QTableName tableName) {
        final StringBuilder sb = new StringBuilder();
        if (tableName.getSchema() != null && !tableName.getSchema().isBlank()) {
            sb.append(escapeKeyword(tableName.getSchema()));
            sb.append(".");
        }

        sb.append(escapeKeyword(tableName.getName()));

        if (tableName.getAlias() != null && !tableName.getAlias().isBlank()) {
            sb.append(' ');
            sb.append(tableName.getAlias());
        }

        return new SqlQuery(sb, Collections.emptyList());
    }

    @Override
    protected SqlQuery renderDefaultSelectExpressionTableSource(@NonNull DefaultSelectExpressionTableSource expressionTableSource) {
        final SqlQuery sqlPart = toQuery(expressionTableSource.toQueryData());

        final StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(sqlPart.getSql());
        sb.append(") AS ");

        sb.append(escapeKeyword(expressionTableSource.getTableAlias()));

        return new SqlQuery(sb, sqlPart.getParams());
    }

    @Override
    protected SqlQuery renderCommonTableExpression(@NonNull CommonTableExpression cte) {
        return new SqlQuery(escapeKeyword(cte.getTableName()), Collections.emptyList());
    }

    @Override
    protected SqlQuery renderFunctionCallTableSource(@NonNull FunctionCallTableSource<?> functionTableSource) {
        final QTableName fnName = functionTableSource.getTableName();
        final StringBuilder sb = new StringBuilder();

        if (fnName.getSchema() != null && !fnName.getSchema().isBlank()) {
            sb.append(escapeKeyword(fnName.getSchema()));
            sb.append(".");
        }
        sb.append(escapeKeyword(fnName.getName()));
        sb.append("(");

        List<SqlQuery.Param> sqlParams = new ArrayList<>();
        List<Expression<?>> params = functionTableSource.getParams();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            SqlQuery paramSql = toSqlPart(params.get(i), null);
            sb.append(paramSql.getSql());
            sqlParams.addAll(paramSql.getParams());
        }
        sb.append(")");

        if (fnName.getAlias() != null && !fnName.getAlias().isBlank()) {
            sb.append(" AS ");
            sb.append(escapeKeyword(fnName.getAlias()));
        }

        return new SqlQuery(sb, sqlParams);
    }

    @Override
    protected SqlQuery renderQColumn(@NonNull QColumn<?> column, ColumnContext columnContext) {
        final String suffix;
        if (HarborStringUtils.isNotBlank(column.getAlias())) {
            suffix = " AS " + escapeKeyword(column.getAlias().trim());
        } else {
            suffix = "";
        }

        if (column.getTableAlias() != null) {
            return new SqlQuery(
                    escapeKeyword(column.getTableAlias()) + "." + escapeKeyword(column.getColumnName()) + suffix,
                    Collections.emptyList()
            );
        } else {
            return new SqlQuery(
                    escapeKeyword(column.getColumnName()) + suffix,
                    Collections.emptyList()
            );
        }
    }

    @Override
    protected SqlQuery renderExcludedColumnExpression(@NonNull ExcludedColumnExpression<?> excluded, ColumnContext columnContext) {
        return new SqlQuery(
                "EXCLUDED." + escapeKeyword(excluded.getColumn().getColumnName()),
                Collections.emptyList()
        );
    }

    @Override
    protected SqlQuery renderCommonTableExpressionColumn(@NonNull CommonTableExpression.Column<?> column, ColumnContext columnContext) {
        return new SqlQuery(
                escapeKeyword(column.getTableName()) + "." + escapeKeyword(column.getName()),
                Collections.emptyList()
        );
    }

    @Override
    protected SqlQuery renderAliasedExpression(@NonNull AliasedExpression<?> aliasedExpression, ColumnContext columnContext) {
        final SqlQuery parentSqlPart = toSqlPart(aliasedExpression.getParentExpression(), columnContext);
        return new SqlQuery(
                parentSqlPart.getSql() + " AS " + escapeKeyword(aliasedExpression.getAlias()),
                parentSqlPart.getParams()
        );
    }

    @Override
    protected SqlQuery renderBooleanConstantExpression(@NonNull BooleanConstantExpression booleanConstantExpression, ColumnContext columnContext) {
        if (columnContext == null) {
            return new SqlQuery(
                    booleanConstantExpression.getValue() == null ? "null" : booleanConstantExpression.getValue().toString(),
                    Collections.emptyList()
            );
        } else {
            return renderConstantExpression(new ConstantExpression<>(Boolean.class, booleanConstantExpression.getValue()), columnContext);
        }
    }

    @Override
    protected SqlQuery renderConstantExpression(@NonNull ConstantExpression<?> constantExpression, ColumnContext columnContext) {
        final ConverterData converterData = columnContext == null ? null : columnContext.getConverterData();
        final Class<?> typeHandlerClass = columnContext == null ? null : columnContext.getTypeHandlerClass();

        if (typeHandlerClass != null) {
            // TypeHandler bypasses converters and default JDBC — handler owns the value
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

        return new SqlQuery("?", List.of(new SqlQuery.Param(value, HarborSQLTypesUtils.getSqlTypeByClass(javaType))));
    }

    @Override
    protected SqlQuery renderConstantsExpression(@NonNull ConstantsExpression<?> constantsExpression, ColumnContext columnContext) {
        final ConverterData converterData = columnContext == null ? null : columnContext.getConverterData();

        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();
        final Class<?> typeHandlerClass = columnContext == null ? null : columnContext.getTypeHandlerClass();

        HarborStringUtils.iterateAppendingJoiningDelimiter(
                sb,
                ",",
                constantsExpression.getConstantsValue(),
                v -> {
                    sb.append("?");
                    if (typeHandlerClass != null) {
                        SqlTypeHandler<?> handler = TypeHandlerRegistry.get(typeHandlerClass);
                        params.add(new SqlQuery.Param(v, handler.sqlType(), handler));
                    } else if (converterData == null) {
                        params.add(new SqlQuery.Param(v, HarborSQLTypesUtils.getSqlTypeByClass(constantsExpression.getJavaType())));
                    } else {
                        params.add(new SqlQuery.Param(convertToDatabaseColumn(v, converterData), HarborSQLTypesUtils.getSqlTypeByClass(converterData.getConverterClassDbType())));
                    }
                }
        );

        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderBinaryOperatorExpression(@NonNull BinaryOperatorExpression<?> binaryOperatorExpression, ColumnContext columnContext) {
        SqlQuery leftPart = toSqlPart(
                binaryOperatorExpression.getLeftExpression(),
                ColumnContext.combineContexts(binaryOperatorExpression.getRightExpression().getColumnContext(dialectName), columnContext)
        );
        SqlQuery rightPart = toSqlPart(
                binaryOperatorExpression.getRightExpression(),
                ColumnContext.combineContexts(binaryOperatorExpression.getLeftExpression().getColumnContext(dialectName), columnContext)
        );

        final String operator = switch (binaryOperatorExpression.getOperator()) {
            case ADDITION -> "+";
            case SUBTRACTION -> "-";
            case MULTIPLICATION -> "*";
            case DIVISION -> "/";
            case MODULO -> "%";
            case EXPONENTIATION -> "^";
        };

        return new SqlQuery(
                leftPart.getSql() + " " + operator + " " + rightPart.getSql(),
                HarborListUtils.merge(leftPart.getParams(), rightPart.getParams())
        );
    }

    @Override
    protected SqlQuery renderExpressionBooleanCondition(@NonNull ExpressionBooleanCondition expressionBooleanCondition, ColumnContext columnContext) {
        return toSqlPart(expressionBooleanCondition.getExpression(), columnContext);
    }

    @Override
    protected SqlQuery renderUnaryOperatorCondition(@NonNull UnaryOperatorCondition condition, ColumnContext columnContext) {
        SqlQuery exprPart = toSqlPart(condition.getExpression(), columnContext);

        final String sql = switch (condition.getOperator()) {
            case NOT -> "NOT " + exprPart.getSql();
            case IS_NULL -> exprPart.getSql() + " IS NULL";
            case IS_NOT_NULL -> exprPart.getSql() + " IS NOT NULL";
            case IS_TRUE -> exprPart.getSql() + " IS TRUE";
            case IS_NOT_TRUE -> exprPart.getSql() + " IS NOT TRUE";
            case IS_FALSE -> exprPart.getSql() + " IS FALSE";
            case IS_NOT_FALSE -> exprPart.getSql() + " IS NOT FALSE";
            case IS_UNKNOWN -> exprPart.getSql() + " IS UNKNOWN";
            case IS_NOT_UNKNOWN -> exprPart.getSql() + " IS NOT UNKNOWN";
        };

        return new SqlQuery(
                sql,
                exprPart.getParams()
        );
    }

    @Override
    protected SqlQuery renderBinaryOperatorCondition(@NonNull BinaryOperatorCondition condition, ColumnContext columnContext) {
        SqlQuery leftPart = toSqlPart(
                condition.getLeftExpression(),
                ColumnContext.combineContexts(condition.getRightExpression().getColumnContext(dialectName), columnContext)
        );
        SqlQuery rightPart = toSqlPart(
                condition.getRightExpression(),
                ColumnContext.combineContexts(condition.getLeftExpression().getColumnContext(dialectName), columnContext)
        );
        return switch (condition.getOperator()) {
            case EQ -> new SqlQuery(
                    leftPart.getSql() + " = " + rightPart.getSql(),
                    HarborListUtils.merge(leftPart.getParams(), rightPart.getParams())
            );
            case GT -> new SqlQuery(
                    leftPart.getSql() + " > " + rightPart.getSql(),
                    HarborListUtils.merge(leftPart.getParams(), rightPart.getParams())
            );
            case GE -> new SqlQuery(
                    leftPart.getSql() + " >= " + rightPart.getSql(),
                    HarborListUtils.merge(leftPart.getParams(), rightPart.getParams())
            );
            case LT -> new SqlQuery(
                    leftPart.getSql() + " < " + rightPart.getSql(),
                    HarborListUtils.merge(leftPart.getParams(), rightPart.getParams())
            );
            case LE -> new SqlQuery(
                    leftPart.getSql() + " <= " + rightPart.getSql(),
                    HarborListUtils.merge(leftPart.getParams(), rightPart.getParams())
            );
            case NOT_EQ -> new SqlQuery(
                    leftPart.getSql() + " != " + rightPart.getSql(),
                    HarborListUtils.merge(leftPart.getParams(), rightPart.getParams())
            );
            case IS_DISTINCT_FROM -> new SqlQuery(
                    leftPart.getSql() + " IS DISTINCT FROM " + rightPart.getSql(),
                    HarborListUtils.merge(leftPart.getParams(), rightPart.getParams())
            );
            case IS_NOT_DISTINCT_FROM -> new SqlQuery(
                    leftPart.getSql() + " IS NOT DISTINCT FROM " + rightPart.getSql(),
                    HarborListUtils.merge(leftPart.getParams(), rightPart.getParams())
            );
            case IN -> new SqlQuery(
                    leftPart.getSql() + " IN (" + rightPart.getSql() + ")",
                    HarborListUtils.merge(leftPart.getParams(), rightPart.getParams())
            );
        };
    }

    @Override
    protected SqlQuery renderTernaryOperatorCondition(@NonNull TernaryOperatorCondition condition, ColumnContext columnContext) {
        SqlQuery leftPart = toSqlPart(
                condition.getLeftExpression(),
                ColumnContext.combineContexts(columnContext, condition.getMiddleExpression().getColumnContext(dialectName), condition.getRightExpression().getColumnContext(dialectName))
        );
        SqlQuery middlePart = toSqlPart(
                condition.getMiddleExpression(),
                ColumnContext.combineContexts(columnContext, condition.getLeftExpression().getColumnContext(dialectName), condition.getRightExpression().getColumnContext(dialectName))
        );
        SqlQuery rightPart = toSqlPart(
                condition.getRightExpression(),
                ColumnContext.combineContexts(columnContext, condition.getLeftExpression().getColumnContext(dialectName), condition.getMiddleExpression().getColumnContext(dialectName))
        );

        final String sql = switch (condition.getOperator()) {
            case BETWEEN -> leftPart.getSql() + " BETWEEN " + middlePart.getSql() + " AND " + rightPart.getSql();
            case BETWEEN_SYMMETRIC -> leftPart.getSql() + " BETWEEN SYMMETRIC " + middlePart.getSql() + " AND " + rightPart.getSql();
            case NOT_BETWEEN -> leftPart.getSql() + " NOT BETWEEN " + middlePart.getSql() + " AND " + rightPart.getSql();
            case NOT_BETWEEN_SYMMETRIC -> leftPart.getSql() + " NOT BETWEEN SYMMETRIC " + middlePart.getSql() + " AND " + rightPart.getSql();
            case LIKE_ESCAPE -> leftPart.getSql() + " LIKE " + middlePart.getSql() + " ESCAPE " + rightPart.getSql();
        };

        return new SqlQuery(
                sql,
                HarborListUtils.merge(leftPart.getParams(), middlePart.getParams(), rightPart.getParams())
        );
    }

    @Override
    protected SqlQuery renderEmbeddableEqCondition(@NonNull EmbeddableEqCondition embeddableEqCondition, ColumnContext columnContext) {
        final StringBuilder sb = new StringBuilder("(");
        final List<SqlQuery.Param> params = new ArrayList<>();

        HarborStringUtils.iterateAppendingJoiningDelimiter(
                sb,
                " AND ",
                embeddableEqCondition.getQEmbeddable().getAllAttributes(),
                qAttribute -> {
                    final Condition condition;
                    if (qAttribute instanceof QColumn<?> qColumn) {
                        if (embeddableEqCondition.getValue() == null) {
                            condition = qColumn.isNull();
                        } else {
                            @SuppressWarnings("unchecked") final QColumn<Object> castedColumn = (QColumn<Object>) qColumn;
                            condition = castedColumn.eq(HarborBeanUtils.getPropertyValue(embeddableEqCondition.getValue(), qColumn.getPropertyName()));
                        }

                    } else if (qAttribute instanceof QEmbeddable<?> qEmbeddable) {
                        condition = new EmbeddableEqCondition(
                                qEmbeddable,
                                embeddableEqCondition.getValue() == null ? null : HarborBeanUtils.getPropertyValue(embeddableEqCondition.getValue(), qEmbeddable.getPropertyName())
                        );
                    } else {
                        throw new IllegalArgumentException("Unsupported embeddable field for eq condition: " + qAttribute.getClass());
                    }

                    SqlQuery sqlPart = toSqlPart(condition, columnContext);
                    sb.append(sqlPart.getSql());
                    params.addAll(sqlPart.getParams());
                }
        );

        sb.append(")");

        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderEmbeddableInCondition(@NonNull EmbeddableInCondition embeddableInCondition, ColumnContext columnContext) {
        if (embeddableInCondition.getValues() == null || embeddableInCondition.getValues().isEmpty()) {
            return new SqlQuery("FALSE", Collections.emptyList());
        }

        final StringBuilder sb = new StringBuilder("(");
        final List<SqlQuery.Param> params = new ArrayList<>();

        HarborStringUtils.iterateAppendingJoiningDelimiter(
                sb,
                " OR ",
                embeddableInCondition.getValues(),
                val -> {
                    SqlQuery sqlPart = renderEmbeddableEqCondition(new EmbeddableEqCondition(embeddableInCondition.getQEmbeddable(), val), columnContext);
                    sb.append(sqlPart.getSql());
                    params.addAll(sqlPart.getParams());
                }
        );

        sb.append(")");

        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderComplexCondition(@NonNull ComplexCondition condition, ColumnContext columnContext) {
        final SqlQuery firstPart = toSqlPart(condition.getFirstCondition(), null);

        final List<CharSequence> parts = new ArrayList<>();
        parts.add(firstPart.getSql());

        final List<SqlQuery.Param> params = new ArrayList<>(firstPart.getParams());

        condition.visitOtherConditions((operator, otherCondition) -> {
            parts.add(switch (operator) {
                case AND -> "AND";
                case OR -> "OR";
            });

            final SqlQuery otherPart = toSqlPart(otherCondition, null);
            parts.add(otherPart.getSql());
            params.addAll(otherPart.getParams());
        });

        return new SqlQuery(
                String.join(" ", parts),
                params
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionAscii(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("ascii(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitLength(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("bit_length(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBtrim(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1 && functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one or two params");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("btrim(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionChr(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("chr(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionConcat(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("concat(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionConcatWs(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have at least one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("concat_ws(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCharLength(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("length(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionLeft(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("left(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionLower(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("lower(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionLpad(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 3) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly three param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("lpad(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionLtrim(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1 && functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one or two params");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("ltrim(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionOctetLength(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("octet_length(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionPosition(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly two params");
        }

        final SqlQuery param1SqlQuery = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery param2SqlQuery = toSqlPart(functionExpression.getParams().get(1), columnContext);
        return new SqlQuery(
                "position(" + param1SqlQuery.getSql() + " IN " + param2SqlQuery.getSql() + ")",
                HarborListUtils.merge(param1SqlQuery.getParams(), param2SqlQuery.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionRepeat(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly two params");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("repeat(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionRight(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly two params");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("right(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionRpad(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 3) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly three param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("rpad(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionRtrim(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1 && functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one or two params");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("rtrim(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionSubstring(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2 && functionExpression.getParams().size() != 3) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly two or three params");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("substring(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionTrim(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() == 1) {
            final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
            return new SqlQuery("trim(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
        } else if (functionExpression.getParams().size() == 2) {
            final SqlQuery stringSqlQuery = toSqlPart(functionExpression.getParams().get(0), columnContext);
            final SqlQuery charsSqlQuery = toSqlPart(functionExpression.getParams().get(1), columnContext);
            return new SqlQuery(
                    "TRIM(BOTH " + charsSqlQuery.getSql() + " FROM " + stringSqlQuery.getSql() + ")",
                    HarborListUtils.merge(charsSqlQuery.getParams(), stringSqlQuery.getParams())
            );
        } else {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one or two params");
        }
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionToHex(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("to_hex(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionUpper(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("upper(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionReplace(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 3) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly three params");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("replace(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionRegexReplace(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 3) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly three params");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("regexp_replace(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionReverse(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("reverse(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionSpace(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("space(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionMd5(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("md5(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionMod(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly two params");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("mod(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionPower(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly two params");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("power(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionRound(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1 && functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one or two params");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("round(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCeil(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("ceil(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionFloor(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("floor(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionTruncate(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("trunc(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionAbs(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("abs(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionNeg(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("(-" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionSign(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("sign(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionSqrt(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("sqrt(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCbrt(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("cbrt(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionSquare(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }

        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final List<SqlQuery.Param> doubleParams = new ArrayList<>(paramSql.getParams().size() * 2);
        doubleParams.addAll(paramSql.getParams());
        doubleParams.addAll(paramSql.getParams());
        return new SqlQuery("(" + paramSql.getSql() + " * " + paramSql.getSql() + ")", doubleParams);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionDegrees(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("degrees(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionRadians(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("radians(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionE(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly 0 params");
        }
        return new SqlQuery("exp(1)", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionExp(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("exp(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionGreatest(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("greatest(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionLeast(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("least(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionLn(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("ln(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionLog(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly two params");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("log(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionLog10(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("log10(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionPi(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly 0 params");
        }
        return new SqlQuery("pi()", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionAcos(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("acos(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionAsin(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("asin(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionAtan(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("atan(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionAtan2(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly two params");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("atan2(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCos(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("cos(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCot(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("cot(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionSin(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("sin(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionTan(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("tan(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionAcosh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("acosh(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionAsinh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("asinh(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionAtanh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("atanh(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCosh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("cosh(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCoth(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("coth(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionSinh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("sinh(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionTanh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("tanh(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCoalesce(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("coalesce(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionNullIf(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly two params");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("nullif(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionIfNull(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly two params");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("ifnull(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionNvl(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly two params");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("nvl(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionNumNulls(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        final List<Expression<Integer>> list = functionExpression.getParams().stream()
                .map(e -> DSL.caseWhen(e.isNull(), DSL.constant(1)).else_(DSL.constant(0)))
                .toList();
        return toSqlPart(DSL.add(list), columnContext);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionNumNonNulls(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        final List<Expression<Integer>> list = functionExpression.getParams().stream()
                .map(e -> DSL.caseWhen(e.isNotNull(), DSL.constant(1)).else_(DSL.constant(0)))
                .toList();
        return toSqlPart(DSL.add(list), columnContext);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected SqlQuery renderPortableFunctionExpressionIif(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 3) {
            throw new IllegalArgumentException("iif function expects 3 params");
        }

        Condition condition = (Condition) functionExpression.getParams().get(0);
        Expression<?> thenValue = functionExpression.getParams().get(1);
        Expression<?> elseValue = functionExpression.getParams().get(2);

        CaseWhenThenExpression<?> caseExpr = new CaseWhenThenExpression(
                List.of(new CaseWhenThenExpression.WhenThen(condition, thenValue)),
                elseValue,
                functionExpression.getJavaType()
        );
        return renderCaseWhenThenExpression(caseExpr, columnContext);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionExists(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly one param");
        }
        final SqlQuery paramsQuery = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("exists(" + paramsQuery.getSql() + ")", paramsQuery.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCurrentTime(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly 0 params");
        }
        return new SqlQuery("CURRENT_TIME", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCurrentDate(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly 0 params");
        }
        return new SqlQuery("CURRENT_DATE", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCurrentTimestamp(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly 0 params");
        }
        return new SqlQuery("CURRENT_TIMESTAMP", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCurrentLocalDateTime(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException(functionExpression.getFunction() + " function expression must have exactly 0 params");
        }
        return new SqlQuery("LOCALTIMESTAMP", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionDate(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("date function must have exactly one param");
        }
        Expression<?> param = functionExpression.getParams().get(0);
        if (!(param instanceof ConstantExpression<?> constant) || !(constant.getConstantValue() instanceof String stringValue)) {
            throw new IllegalArgumentException("date function requires a String literal");
        }
        return new SqlQuery("DATE '" + stringValue.replace("'", "''") + "'", List.of());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionDateAdd(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("date_add function expects 2 params");
        }

        final SqlQuery dateSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery daysSql = toSqlPart(functionExpression.getParams().get(1), columnContext);

        return new SqlQuery(
                "(" + dateSql.getSql() + " + (" + daysSql.getSql() + " * interval '1 day'))",
                HarborListUtils.merge(dateSql.getParams(), daysSql.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionDateAddInterval(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("date_add_interval function expects 2 params");
        }

        final SqlQuery dateSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery intervalSql = toSqlPart(functionExpression.getParams().get(1), columnContext);

        return new SqlQuery(
                "(" + dateSql.getSql() + " + " + intervalSql.getSql() + ")",
                HarborListUtils.merge(dateSql.getParams(), intervalSql.getParams())
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
                "(" + dateSql.getSql() + " - (" + daysSql.getSql() + " * interval '1 day'))",
                HarborListUtils.merge(dateSql.getParams(), daysSql.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionDateSubInterval(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("date_sub_interval function expects 2 params");
        }

        final SqlQuery dateSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery intervalSql = toSqlPart(functionExpression.getParams().get(1), columnContext);

        return new SqlQuery(
                "(" + dateSql.getSql() + " - " + intervalSql.getSql() + ")",
                HarborListUtils.merge(dateSql.getParams(), intervalSql.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionExtractDay(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("extract_day function must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery(
                "extract(DAY FROM " + paramSql.getSql() + ")",
                paramSql.getParams()
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionExtractDayOfYear(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("extract_day_of_year function must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery(
                "extract(DAY_OF_YEAR FROM " + paramSql.getSql() + ")",
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
                "extract(ISO_DAY_OF_WEEK FROM " + paramSql.getSql() + ")",
                paramSql.getParams()
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionExtractHour(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("extract_hour function must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery(
                "extract(HOUR FROM " + paramSql.getSql() + ")",
                paramSql.getParams()
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionExtractMinute(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("extract_minute function must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery(
                "extract(MINUTE FROM " + paramSql.getSql() + ")",
                paramSql.getParams()
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionExtractMonth(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("extract_month function must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery(
                "extract(MONTH FROM " + paramSql.getSql() + ")",
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
                "extract(QUARTER FROM " + paramSql.getSql() + ")",
                paramSql.getParams()
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionExtractSecond(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("extract_second function must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery(
                "extract(SECOND FROM " + paramSql.getSql() + ")",
                paramSql.getParams()
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionExtractYear(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("extract_year function must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery(
                "extract(YEAR FROM " + paramSql.getSql() + ")",
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
                "extract(EPOCH FROM " + paramSql.getSql() + ")",
                paramSql.getParams()
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionDateTrunc(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("date_trunc function must have exactly one param");
        }
        final String precision = switch (functionExpression.getFunction()) {
            case DATE_TRUNC_YEAR -> "year";
            case DATE_TRUNC_QUARTER -> "quarter";
            case DATE_TRUNC_MONTH -> "month";
            case DATE_TRUNC_WEEK -> "week";
            case DATE_TRUNC_DAY -> "day";
            case DATE_TRUNC_HOUR -> "hour";
            case DATE_TRUNC_MINUTE -> "minute";
            case DATE_TRUNC_SECOND -> "second";
            case DATE_TRUNC_MILLISECOND -> "millisecond";
            case DATE_TRUNC_MICROSECOND -> "microsecond";
            default -> throw new IllegalArgumentException("unsupported function: " + functionExpression.getFunction());
        };

        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery(
                "DATE_TRUNC('" + precision + "', " + paramSql.getSql() + ")",
                paramSql.getParams()
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitAnd(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_and function must have exactly two params");
        }
        final SqlQuery left = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery right = toSqlPart(functionExpression.getParams().get(1), columnContext);
        return new SqlQuery(
                "(" + left.getSql() + " & " + right.getSql() + ")",
                HarborListUtils.merge(left.getParams(), right.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitOr(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_or function must have exactly two params");
        }
        final SqlQuery left = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery right = toSqlPart(functionExpression.getParams().get(1), columnContext);
        return new SqlQuery(
                "(" + left.getSql() + " | " + right.getSql() + ")",
                HarborListUtils.merge(left.getParams(), right.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitNand(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_nand function must have exactly two params");
        }
        final SqlQuery left = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery right = toSqlPart(functionExpression.getParams().get(1), columnContext);
        return new SqlQuery(
                "(~((" + left.getSql() + " & " + right.getSql() + ")))",
                HarborListUtils.merge(left.getParams(), right.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitNot(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("bit_not function must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery("(~" + paramSql.getSql() + ")", paramSql.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitNor(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_nor function must have exactly two params");
        }
        final SqlQuery left = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery right = toSqlPart(functionExpression.getParams().get(1), columnContext);
        return new SqlQuery(
                "(~((" + left.getSql() + " | " + right.getSql() + ")))",
                HarborListUtils.merge(left.getParams(), right.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitGet(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_get function must have exactly two params");
        }
        final SqlQuery left = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery right = toSqlPart(functionExpression.getParams().get(1), columnContext);
        final List<SqlQuery.Param> params = new ArrayList<>(left.getParams().size() + right.getParams().size() * 2);
        params.addAll(left.getParams());
        params.addAll(right.getParams());
        params.addAll(right.getParams());
        return new SqlQuery(
                "((" + left.getSql() + " & (1 << " + right.getSql() + ")) >> " + right.getSql() + ")",
                params
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionBitSet(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("bit_set function must have exactly two params");
        }
        final SqlQuery left = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery right = toSqlPart(functionExpression.getParams().get(1), columnContext);
        return new SqlQuery(
                "(" + left.getSql() + " | (1 << " + right.getSql() + "))",
                HarborListUtils.merge(left.getParams(), right.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionShl(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("shl function must have exactly two params");
        }
        final SqlQuery left = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery right = toSqlPart(functionExpression.getParams().get(1), columnContext);
        return new SqlQuery(
                "(" + left.getSql() + " << " + right.getSql() + ")",
                HarborListUtils.merge(left.getParams(), right.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionShr(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("shr function must have exactly two params");
        }
        final SqlQuery left = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final SqlQuery right = toSqlPart(functionExpression.getParams().get(1), columnContext);
        return new SqlQuery(
                "(" + left.getSql() + " >> " + right.getSql() + ")",
                HarborListUtils.merge(left.getParams(), right.getParams())
        );
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionRowNumber(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException("row_number function must have exactly zero params");
        }
        return new SqlQuery("row_number()", Collections.emptyList());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionRank(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException("rank function must have exactly zero params");
        }
        return new SqlQuery("rank()", Collections.emptyList());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionDenseRank(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException("dense_rank function must have exactly zero params");
        }
        return new SqlQuery("dense_rank()", Collections.emptyList());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionPercentRank(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException("percent_rank function must have exactly zero params");
        }
        return new SqlQuery("percent_rank()", Collections.emptyList());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionCumeDist(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (!functionExpression.getParams().isEmpty()) {
            throw new IllegalArgumentException("cume_dist function must have exactly zero params");
        }
        return new SqlQuery("cume_dist()", Collections.emptyList());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionNtile(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("ntile function must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery("ntile(" + paramSql.getSql() + ")", paramSql.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionLead(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1 && functionExpression.getParams().size() != 2 && functionExpression.getParams().size() != 3) {
            throw new IllegalArgumentException("lead function must have exactly one or two or three params");
        }

        final SqlQuery paramSql = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("lead(" + paramSql.getSql() + ")", paramSql.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionLag(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1 && functionExpression.getParams().size() != 2 && functionExpression.getParams().size() != 3) {
            throw new IllegalArgumentException("lag function must have exactly one or two or three params");
        }

        final SqlQuery paramSql = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("lag(" + paramSql.getSql() + ")", paramSql.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionFirstValue(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("first_value function must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery("first_value(" + paramSql.getSql() + ")", paramSql.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionLastValue(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException("last_value function must have exactly one param");
        }
        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        return new SqlQuery("last_value(" + paramSql.getSql() + ")", paramSql.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionNthValue(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 2) {
            throw new IllegalArgumentException("nth_value function must have exactly two params");
        }

        final SqlQuery paramSql = renderParams(functionExpression.getParams(), columnContext);
        return new SqlQuery("nth_value(" + paramSql.getSql() + ")", paramSql.getParams());
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionPercentileCont(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        return renderOrderedSetFunction("percentile_cont", functionExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableFunctionExpressionPercentileDisc(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        return renderOrderedSetFunction("percentile_disc", functionExpression, columnContext);
    }

    private SqlQuery renderOrderedSetFunction(@NonNull String functionName, @NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        if (functionExpression.getParams().size() != 1) {
            throw new IllegalArgumentException(functionName + " function must have exactly one param");
        }
        if (functionExpression.getWithinGroupOrders().isEmpty()) {
            throw new IllegalArgumentException(functionName + " function must have at least one WITHIN GROUP order");
        }

        final SqlQuery paramSql = toSqlPart(functionExpression.getParams().get(0), columnContext);
        final StringBuilder sb = new StringBuilder(functionName);
        final List<SqlQuery.Param> params = new ArrayList<>(paramSql.getParams());
        sb.append('(').append(paramSql.getSql()).append(") WITHIN GROUP (ORDER BY ");
        HarborStringUtils.iterateAppendingJoiningDelimiter(
                sb,
                ", ",
                functionExpression.getWithinGroupOrders(),
                order -> {
                    SqlQuery orderQuery = toSqlPart(order);
                    sb.append(orderQuery.getSql());
                    params.addAll(orderQuery.getParams());
                }
        );
        sb.append(')');
        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderFunctionExpression(@NonNull FunctionExpression<?> functionExpression, ColumnContext columnContext) {
        return new SqlQueryBuilder()
                .append(functionExpression.getFunction())
                .append("(")
                .append(renderParams(functionExpression.getParams(), columnContext))
                .append(")")
                .toSqlQuery();
    }

    @Override
    protected SqlQuery renderLiteralExpression(@NonNull LiteralExpression<?> literalExpression, ColumnContext columnContext) {
        return new SqlQuery(literalExpression.getLiteral(), List.of());
    }

    @Override
    protected SqlQuery renderIntervalConstant(@NonNull IntervalConstant intervalConstant, ColumnContext columnContext) {
        final Interval interval = intervalConstant.getInterval();
        return new SqlQuery("INTERVAL '" + interval.getValue() + "' " + interval.getUnit().name(), Collections.emptyList());
    }

    @Override
    protected SqlQuery renderNameExpression(@NonNull NameExpression<?> nameExpression, ColumnContext columnContext) {
        return new SqlQuery(escapeKeyword(nameExpression.getName()), List.of());
    }

    @Override
    protected SqlQuery renderCastExpression(@NonNull CastExpression<?> castExpression, ColumnContext columnContext) {
        final SqlQuery sqlPartParent = toSqlPart(castExpression.getParentExpression(), columnContext);
        return new SqlQuery("CAST(" + sqlPartParent.getSql() + " AS " + getDbTypeName(castExpression.getJavaType()) + ")", sqlPartParent.getParams());
    }

    @Override
    protected SqlQuery renderSelectExpression(@NonNull SelectExpression<?> selectExpression, ColumnContext columnContext) {
        final SqlQuery query = toQuery(extractQueryData(selectExpression));
        return new SqlQuery("(" + query.getSql() + ")", query.getParams());
    }

    @Override
    protected SqlQuery renderFluentSelectExpression(@NonNull FluentSelectExpression<?> selectExpression, ColumnContext columnContext) {
        final SqlQuery query = toQuery(extractQueryData(selectExpression.getExpression()));
        return new SqlQuery("(" + query.getSql() + ")", query.getParams());
    }

    @Override
    protected SqlQuery renderWindowExpression(@NonNull WindowExpression<?> windowExpression, ColumnContext columnContext) {
        final SqlQuery funcQuery = toSqlPart(windowExpression.getFunction(), columnContext);
        final StringBuilder sb = new StringBuilder(funcQuery.getSql());
        final List<SqlQuery.Param> params = new ArrayList<>(funcQuery.getParams());

        sb.append(" OVER (");

        boolean needsSpace = false;

        // PARTITION BY
        if (!windowExpression.getPartitionBy().isEmpty()) {
            sb.append("PARTITION BY ");
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    windowExpression.getPartitionBy(),
                    expr -> {
                        SqlQuery partQuery = toSqlPart(expr, null);
                        sb.append(partQuery.getSql());
                        params.addAll(partQuery.getParams());
                    }
            );
            needsSpace = true;
        }

        // ORDER BY
        if (!windowExpression.getOrderBy().isEmpty()) {
            if (needsSpace) sb.append(" ");
            sb.append("ORDER BY ");
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    windowExpression.getOrderBy(),
                    order -> {
                        SqlQuery orderQuery = toSqlPart(order);
                        sb.append(orderQuery.getSql());
                        params.addAll(orderQuery.getParams());
                    }
            );
            needsSpace = true;
        }

        // Frame clause
        if (windowExpression.getFrameType() != null) {
            if (needsSpace) sb.append(" ");
            sb.append(windowExpression.getFrameType());
            sb.append(" BETWEEN ");
            sb.append(windowExpression.getFrameStart().getSql());
            sb.append(" AND ");
            sb.append(windowExpression.getFrameEnd().getSql());
        }

        sb.append(")");

        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionAnyValue(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("any_value aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("any_value", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionAvg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("avg aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("avg", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitAndAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_and_agg aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("bit_and", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitNandAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_nand_agg aggregate function must have exactly one expression");
        }
        final SqlQuery inner = renderPortableAggregate("bit_and", aggregateExpression, columnContext);
        return new SqlQuery("~(" + inner.getSql() + ")", inner.getParams());
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitNorAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_nor_agg aggregate function must have exactly one expression");
        }
        final SqlQuery inner = renderPortableAggregate("bit_or", aggregateExpression, columnContext);
        return new SqlQuery("~(" + inner.getSql() + ")", inner.getParams());
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitOrAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_or_agg aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("bit_or", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitXorAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_xor_agg aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("bit_xor", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBitXnorAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bit_xnor_agg aggregate function must have exactly one expression");
        }
        final SqlQuery inner = renderPortableAggregate("bit_xor", aggregateExpression, columnContext);
        return new SqlQuery("~(" + inner.getSql() + ")", inner.getParams());
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBoolAnd(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bool_and aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("bool_and", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionBoolOr(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("bool_or aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("bool_or", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionCount(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() > 1) {
            throw new IllegalArgumentException("count aggregate function must have at most one expression");
        }
        if (aggregateExpression.getExpressions().isEmpty() && aggregateExpression.getQuantifier() == AggregateQuantifier.DISTINCT) {
            throw new IllegalArgumentException("count(DISTINCT) aggregate function requires an expression");
        }
        return renderPortableAggregate("count", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionEvery(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("every aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("every", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionMin(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("min aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("min", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionMax(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("max aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("max", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionStddevPop(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("stddev_pop aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("stddev_pop", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionStddevSamp(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("stddev_samp aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("stddev_samp", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionSum(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("sum aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("sum", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionVarPop(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("var_pop aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("var_pop", aggregateExpression, columnContext);
    }

    @Override
    protected SqlQuery renderPortableAggregateExpressionVarSamp(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        if (aggregateExpression.getExpressions().size() != 1) {
            throw new IllegalArgumentException("var_samp aggregate function must have exactly one expression");
        }
        return renderPortableAggregate("var_samp", aggregateExpression, columnContext);
    }

    protected SqlQuery renderPortableAggregate(@NonNull String functionName, @NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        final StringBuilder sb = new StringBuilder();
        final List<SqlQuery.Param> params = new ArrayList<>();

        sb.append(functionName).append('(');

        if (aggregateExpression.getQuantifier() == AggregateQuantifier.DISTINCT) {
            sb.append("DISTINCT ");
        } else if (aggregateExpression.getQuantifier() == AggregateQuantifier.ALL) {
            sb.append("ALL ");
        }

        if (aggregateExpression.getExpressions().isEmpty()) {
            sb.append('*');
        } else {
            HarborStringUtils.iterateAppendingJoiningDelimiter(
                    sb,
                    ", ",
                    aggregateExpression.getExpressions(),
                    expr -> appendExpression(sb, params, expr, ColumnContext.combineContexts(columnContext, expr.getColumnContext(dialectName)))
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
    protected SqlQuery renderNextvalExpression(@NonNull NextvalExpression nextvalExpression, ColumnContext columnContext) {
        return new SqlQuery("nextval('" + nextvalExpression.getSequence().replace("'", "''") + "')", Collections.emptyList());
    }

    @Override
    protected SqlQuery renderCaseWhenThenExpression(@NonNull CaseWhenThenExpression<?> cwtExpression, ColumnContext columnContext) {
        final StringBuilder sb = new StringBuilder("CASE ");
        final List<SqlQuery.Param> params = new ArrayList<>();

        for (CaseWhenThenExpression.WhenThen<?> condition : cwtExpression.getConditions()) {
            final SqlQuery whenSqlPart = toSqlPart(condition.getWhen(), ColumnContext.combineContexts(columnContext, condition.getWhen().getColumnContext(dialectName)));

            sb
                    .append("WHEN ")
                    .append(whenSqlPart.getSql())
                    .append(" THEN ");

            params.addAll(whenSqlPart.getParams());

            if (condition.getThen() != null) {
                final SqlQuery thenSqlPart = toSqlPart(condition.getThen(), ColumnContext.combineContexts(columnContext, condition.getThen().getColumnContext(dialectName)));
                sb.append(thenSqlPart.getSql());
                params.addAll(thenSqlPart.getParams());
            } else {
                sb.append("NULL");
            }

            sb.append(" ");
        }

        if (cwtExpression.getElse_() != null) {
            final SqlQuery elseSqlPart = toSqlPart(cwtExpression.getElse_(), ColumnContext.combineContexts(columnContext, cwtExpression.getElse_().getColumnContext(dialectName)));

            sb
                    .append("ELSE ")
                    .append(elseSqlPart.getSql())
                    .append(" ");

            params.addAll(elseSqlPart.getParams());
        }

        sb.append("END");

        return new SqlQuery(sb, params);
    }

    @Override
    protected SqlQuery renderParenthesesExpression(@NonNull ParenthesesExpression<?> parenthesesExpression, ColumnContext columnContext) {
        final SqlQuery part = toSqlPart(parenthesesExpression.getExpression(), columnContext);
        return new SqlQuery("(" + part.getSql() + ")", part.getParams());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Object convertToDatabaseColumn(Object entityValue, @NonNull ConverterData converterData) {
        AttributeConverter attributeConverter = converterData.getConverterInstance() != null
                ? converterData.getConverterInstance()
                : attributeConverterSupplier.supply(Objects.requireNonNull(converterData.getConverterClass(), "converter instance or converter class is required"));
        return attributeConverter.convertToDatabaseColumn(entityValue);
    }

    protected Object serializeJsonValue(Object value) {
        if (value == null || value instanceof String) {
            return value;
        }
        if (jsonSerializer != null) {
            return jsonSerializer.serialize(value);
        }
        throw new IllegalStateException(
                "Cannot serialize " + value.getClass().getName() + " to JSON: no JsonSerializer configured. " +
                        "Use HarborSessionFactory.builder().jsonSerializer(jsonSerializer).build() to enable automatic POJO serialization for @Json fields."
        );
    }

    protected SqlQuery renderParams(@NonNull List<Expression<?>> paramsExpressions, ColumnContext columnContext) {
        if (paramsExpressions.isEmpty()) {
            return EMPTY_SQL_QUERY;
        }

        final List<SqlQuery> paramParts = paramsExpressions.stream()
                .map(e -> toSqlPart(e, columnContext))
                .toList();

        final String paramsSql = paramParts.stream()
                .map(SqlQuery::getSql)
                .collect(Collectors.joining(", "));

        final List<SqlQuery.Param> params = paramParts.stream()
                .flatMap(pp -> pp.getParams().stream())
                .toList();

        return new SqlQuery(paramsSql, params);
    }

    protected String getDbTypeName(@NonNull Class<?> clazz) {
        if (clazz == Boolean.class) return "bool";
        if (clazz == Integer.class) return "int";
        if (clazz == Byte.class) return "tinyint";
        if (clazz == Short.class) return "smallint";
        if (clazz == Long.class) return "bigint";
        if (clazz == Float.class) return "real";
        if (clazz == Double.class) return "double";
        if (clazz == BigInteger.class) return "numeric";
        if (clazz == BigDecimal.class) return "numeric";
        if (clazz == Character.class) return "char";
        if (clazz == String.class) return "varchar";
        if (clazz == java.sql.Date.class) return "date";
        if (clazz == java.util.Date.class) return "date";
        if (clazz == java.time.LocalDate.class) return "date";
        if (clazz == java.sql.Time.class) return "time";
        if (clazz == java.time.LocalTime.class) return "time";
        if (clazz == java.sql.Timestamp.class) return "timestamp";
        if (clazz == java.time.LocalDateTime.class) return "timestamp";
        if (clazz == java.time.OffsetDateTime.class) return "timestamp with time zone";

        throw new IllegalArgumentException("Unknown DB type for Java type: " + clazz.getName());
    }

    protected AttributeConverterSupplier getAttributeConverterSupplier() {
        return attributeConverterSupplier;
    }

    protected JsonSerializer getJsonSerializer() {
        return jsonSerializer;
    }

    protected void appendExpression(@NonNull StringBuilder sb, @NonNull List<SqlQuery.Param> params, Expression<?> expression) {
        appendExpression(sb, params, expression, null);
    }

    protected void appendExpression(@NonNull StringBuilder sb, @NonNull List<SqlQuery.Param> params, Expression<?> expression, ColumnContext columnContext) {
        if (expression == null) {
            sb.append("NULL");
        } else {
            SqlQuery sqlPart = toSqlPart(expression, columnContext);
            sb.append(sqlPart.getSql());
            params.addAll(sqlPart.getParams());
        }
    }

    protected void appendCommonTableExpression(@NonNull StringBuilder sb, @NonNull List<SqlQuery.Param> params, @NonNull CommonTableExpression cte) {
        sb.append(escapeKeyword(cte.getTableName()));

        sb.append('(');

        HarborStringUtils.iterateAppendingJoiningDelimiter(
                sb,
                ", ",
                cte.getColumns(),
                column -> sb.append(escapeKeyword(column.getName()))
        );

        sb.append(") AS (");

        SelectQueryData selectQueryData = extractQueryData(cte.getExpression());

        if (cte.getColumns().size() != selectQueryData.getSelectExpressions().size()) {
            throw new IllegalArgumentException("Number of defined columns in CommonTableExpression is different from number of selected expressions (%d vs %d)".formatted(
                    cte.getColumns().size(),
                    selectQueryData.getSelectExpressions().size()
            ));
        }

        List<Expression<?>> aliasedSelectExpression = new ArrayList<>(selectQueryData.getSelectExpressions().size());

        HarborListUtils.iterateSimultaneously(
                selectQueryData.getSelectExpressions(),
                cte.getColumns(),
                (expression, column, integer) -> aliasedSelectExpression.add(expression.as(column.getName()))
        );

        final SelectQueryData selectQueryDataAliased = new SelectQueryData(
                selectQueryData.getCtes(),
                selectQueryData.isDistinct(),
                selectQueryData.isWithRecursive(),
                aliasedSelectExpression,
                selectQueryData.getFrom(),
                selectQueryData.getJoins(),
                selectQueryData.getWhereConditions(),
                selectQueryData.getGroupByExpressions(),
                selectQueryData.getHavingConditions(),
                selectQueryData.getCombinations(),
                selectQueryData.getOrders(),
                selectQueryData.getLimit(),
                selectQueryData.getOffset(),
                selectQueryData.isForUpdate()
        );

        final SqlQuery sqlPart = toQuery(selectQueryDataAliased);
        sb.append(sqlPart.getSql());
        params.addAll(sqlPart.getParams());

        sb.append(")");
    }

    protected Expression<?> stripAlias(Expression<?> expression) {
        if (expression instanceof AliasedExpression<?> aliased) {
            return aliased.getParentExpression();
        }
        if (expression instanceof QColumn<?> col && HarborStringUtils.isNotBlank(col.getAlias())) {
            return col.as(null);
        }
        return expression;
    }
}
