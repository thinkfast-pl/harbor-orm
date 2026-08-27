// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Default implementation of WindowExpression.
 *
 * @param <T> the result type of the window function
 */
@Getter
public class DefaultWindowExpression<T> implements WindowExpression<T> {

    private final Expression<?> function;
    private final Class<T> javaType;
    private final List<Expression<?>> partitionBy = new ArrayList<>();
    private final List<Order> orderBy = new ArrayList<>();
    private String frameType;
    private FrameBound frameStart;
    private FrameBound frameEnd;

    public DefaultWindowExpression(@NonNull Expression<?> function, @NonNull Class<T> javaType) {
        this.function = function;
        this.javaType = javaType;
    }

    @Override
    public WindowExpression<T> partitionBy(Expression<?>... expressions) {
        partitionBy.addAll(Arrays.asList(expressions));
        return this;
    }

    @Override
    public WindowExpression<T> orderBy(Order... orders) {
        orderBy.addAll(Arrays.asList(orders));
        return this;
    }

    @Override
    public WindowExpression<T> rowsBetween(FrameBound start, FrameBound end) {
        this.frameType = "ROWS";
        this.frameStart = start;
        this.frameEnd = end;
        return this;
    }

    @Override
    public WindowExpression<T> rangeBetween(FrameBound start, FrameBound end) {
        this.frameType = "RANGE";
        this.frameStart = start;
        this.frameEnd = end;
        return this;
    }

    @Override
    public WindowExpression<T> frameBetween(String frameType, FrameBound start, FrameBound end) {
        this.frameType = frameType;
        this.frameStart = start;
        this.frameEnd = end;
        return this;
    }

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }
}
