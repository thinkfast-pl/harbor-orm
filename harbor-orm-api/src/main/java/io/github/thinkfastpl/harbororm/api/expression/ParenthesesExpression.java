// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Wraps an expression in parentheses for explicit grouping in generated SQL.
 *
 * @param <T> the Java type of the wrapped expression
 */
@RequiredArgsConstructor
public class ParenthesesExpression<T> implements Expression<T> {

    @NonNull
    @Getter
    private final Expression<T> expression;

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return expression.getColumnContext(dialectName);
    }

    @Override
    public Class<T> getJavaType() {
        return expression.getJavaType();
    }
}
