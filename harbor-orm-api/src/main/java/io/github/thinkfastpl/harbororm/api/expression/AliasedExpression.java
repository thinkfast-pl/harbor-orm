// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Wraps an expression with a column alias for use in SELECT clauses.
 * Produced by {@link Expression#as(String)}.
 *
 * @param <T> the Java type of the underlying expression
 */
@RequiredArgsConstructor
public final class AliasedExpression<T> implements Expression<T> {

    @NonNull
    private final Expression<T> parentExpression;

    @NonNull
    @Getter
    private final String alias;

    public @NonNull Expression<T> getParentExpression() {
        return parentExpression;
    }

    @Override
    public Class<T> getJavaType() {
        return parentExpression.getJavaType();
    }

    @Override
    public String getResultColumnLabel() {
        return alias;
    }

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return parentExpression.getColumnContext(dialectName);
    }

    @Override
    public Expression<T> as(String alias) {
        return new AliasedExpression<>(parentExpression, alias);
    }
}
