// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents a raw SQL literal expression that is rendered as-is (unparameterized) into the query.
 *
 * @param <T> the Java type of the expression result
 */
@RequiredArgsConstructor
public class LiteralExpression<T> implements Expression<T> {

    @NonNull
    private final Class<T> javaType;

    @NonNull
    private final String literal;

    @Override
    public @NonNull Class<T> getJavaType() {
        return javaType;
    }

    public @NonNull String getLiteral() {
        return literal;
    }

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }
}
