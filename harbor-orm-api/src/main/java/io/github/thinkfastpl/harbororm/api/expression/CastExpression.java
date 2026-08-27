// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents a SQL {@code CAST(expression AS type)} expression that converts a value to a target type.
 *
 * @param <T> the target Java type after casting
 */
@RequiredArgsConstructor
public class CastExpression<T> implements Expression<T> {

    @NonNull
    @Getter
    private final Expression<?> parentExpression;

    @NonNull
    private final Class<T> javaType;

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return parentExpression.getColumnContext(dialectName);
    }

    @Override
    public @NonNull Class<T> getJavaType() {
        return javaType;
    }
}
