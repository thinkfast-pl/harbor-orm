// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.query.data.SelectQueryData;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps a {@link DefaultSelectExpression} as a derived table source (subquery in a {@code FROM} clause)
 * with column alias management.
 */
@RequiredArgsConstructor
public class DefaultSelectExpressionTableSource implements SelectExpressionTableSource {

    @NonNull
    private final DefaultSelectExpression<?> selectExpression;

    @NonNull
    @Getter
    private final String tableAlias;

    private final Map<Expression<?>, String> expressionAliases = new HashMap<>();

    @Override
    public <T> QColumn<T> getColumn(@NonNull Expression<T> expression, @NonNull String alias) {
        expressionAliases.put(expression, alias);
        return QColumn.simple(expression.getJavaType(), tableAlias, alias);
    }

    @Override
    public <T> QColumn<T> select(@NonNull Expression<T> expression, @NonNull String alias) {
        selectExpression.select(expression);
        expressionAliases.put(expression, alias);
        return QColumn.simple(expression.getJavaType(), tableAlias, alias);
    }

    public SelectQueryData toQueryData() {
        final SelectQueryData selectExpressionQueryData = selectExpression.toQueryData();

        final List<Expression<?>> aliasedSelectExpression = selectExpressionQueryData.getSelectExpressions().stream()
                .map(expression -> {
                    String alias = expressionAliases.get(expression);
                    return alias == null ? expression : expression.as(alias);
                })
                .toList();

        return new SelectQueryData(
                selectExpressionQueryData.getCtes(),
                selectExpressionQueryData.isWithRecursive(),
                selectExpressionQueryData.isDistinct(),
                aliasedSelectExpression,
                selectExpressionQueryData.getFrom(),
                selectExpressionQueryData.getJoins(),
                selectExpressionQueryData.getWhereConditions(),
                selectExpressionQueryData.getGroupByExpressions(),
                selectExpressionQueryData.getHavingConditions(),
                selectExpressionQueryData.getCombinations(),
                selectExpressionQueryData.getOrders(),
                selectExpressionQueryData.getLimit(),
                selectExpressionQueryData.getOffset(),
                selectExpressionQueryData.isForUpdate()
        );
    }
}
