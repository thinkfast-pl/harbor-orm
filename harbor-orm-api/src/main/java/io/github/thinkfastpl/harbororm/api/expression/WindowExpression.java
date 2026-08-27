// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import java.util.List;

/**
 * Expression representing a window function with OVER clause.
 * Supports fluent API for building window specifications.
 *
 * @param <T> the result type of the window function
 */
public interface WindowExpression<T> extends Expression<T> {

    /**
     * Add PARTITION BY clause to the window specification.
     *
     * @param expressions columns to partition by
     * @return this window expression for chaining
     */
    WindowExpression<T> partitionBy(Expression<?>... expressions);

    /**
     * Add ORDER BY clause within the window.
     *
     * @param orders ordering specifications
     * @return this window expression for chaining
     */
    WindowExpression<T> orderBy(Order... orders);

    /**
     * Add ROWS frame specification.
     *
     * @param start frame start bound
     * @param end frame end bound
     * @return this window expression for chaining
     */
    WindowExpression<T> rowsBetween(FrameBound start, FrameBound end);

    /**
     * Add RANGE frame specification.
     *
     * @param start frame start bound
     * @param end frame end bound
     * @return this window expression for chaining
     */
    WindowExpression<T> rangeBetween(FrameBound start, FrameBound end);

    WindowExpression<T> frameBetween(String frameType, FrameBound start, FrameBound end);

    /**
     * Get the underlying function expression.
     */
    Expression<?> getFunction();

    /**
     * Get partition by expressions.
     */
    List<Expression<?>> getPartitionBy();

    /**
     * Get order by specifications.
     */
    List<Order> getOrderBy();

    /**
     * Get frame type (ROWS, RANGE, or null).
     */
    String getFrameType();

    /**
     * Get frame start bound.
     */
    FrameBound getFrameStart();

    /**
     * Get frame end bound.
     */
    FrameBound getFrameEnd();
}
