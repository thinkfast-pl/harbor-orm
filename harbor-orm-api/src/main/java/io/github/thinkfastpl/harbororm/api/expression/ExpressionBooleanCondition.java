// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Wraps an Expression<Boolean> as a Condition.
 * <p>
 * This allows boolean expressions (such as EXISTS, boolean functions, or
 * boolean columns) to be used in WHERE clauses and other conditional contexts.
 */
@RequiredArgsConstructor
@Getter
public final class ExpressionBooleanCondition implements Condition {

    @NonNull
    private final Expression<Boolean> expression;

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return expression.getColumnContext(dialectName);
    }
}
