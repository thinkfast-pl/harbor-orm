// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents a single constant (parameter) value in a SQL query.
 * The {@code constantValue} may be {@code null}, representing an SQL {@code NULL} literal.
 *
 * @param <T> the Java type of the constant value
 */
@RequiredArgsConstructor
public final class ConstantExpression<T> implements Expression<T> {

    @NonNull
    private final Class<T> constantClass;

    /**
     * The constant value, or {@code null} for SQL {@code NULL}.
     */
    @Getter
    private final T constantValue;

    @Override
    public Class<T> getJavaType() {
        return constantClass;
    }

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }
}
