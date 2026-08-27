// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Represents a SQL function call with a function name and a list of parameter expressions.
 *
 * @param <T> the Java type of the function return value
 */
@RequiredArgsConstructor
public class FunctionExpression<T> implements Expression<T> {

    @NonNull
    @Getter
    private final String function;

    @NonNull
    @Getter
    private final List<Expression<?>> params;

    @NonNull
    @Getter
    private final Class<T> javaType;

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }
}
