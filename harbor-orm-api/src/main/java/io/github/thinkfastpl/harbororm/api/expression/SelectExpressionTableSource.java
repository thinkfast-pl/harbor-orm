// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.metadata.QTableSource;
import lombok.NonNull;

/**
 * A table source backed by a subquery, allowing a {@link SelectExpression} to be used
 * as a derived table in {@code FROM} or {@code JOIN} clauses.
 */
public interface SelectExpressionTableSource extends QTableSource {

    /**
     * Returns a column reference for an expression that was previously selected
     * in the underlying subquery.
     *
     * @param expression the expression whose column reference to retrieve
     * @param alias      the alias assigned to the expression in the subquery
     * @param <T>        the Java type of the column value
     * @return a {@link QColumn} reference pointing to the aliased column in this derived table
     */
    <T> QColumn<T> getColumn(@NonNull Expression<T> expression, @NonNull String alias);

    /**
     * Adds an expression to the underlying subquery's {@code SELECT} list and returns
     * a column reference for accessing it from the outer query.
     *
     * @param expression the expression to select in the subquery
     * @param alias      the alias to assign to the expression
     * @param <T>        the Java type of the column value
     * @return a {@link QColumn} reference pointing to the newly added column in this derived table
     */
    <T> QColumn<T> select(@NonNull Expression<T> expression, @NonNull String alias);
}
