// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A condition with a single operand, used for {@code IS NULL}, {@code IS NOT NULL},
 * {@code IS TRUE}, {@code IS FALSE}, {@code IS UNKNOWN}, and {@code NOT} operations.
 */
@RequiredArgsConstructor
public class UnaryOperatorCondition implements Condition {

    public enum Operator {
        NOT,
        IS_NULL,
        IS_NOT_NULL,
        IS_TRUE,
        IS_NOT_TRUE,
        IS_FALSE,
        IS_NOT_FALSE,
        IS_UNKNOWN,
        IS_NOT_UNKNOWN,
    }

    @NonNull
    private final Expression<?> expression;

    @NonNull
    private final Operator operator;

    public @NonNull Expression<?> getExpression() {
        return expression;
    }

    public @NonNull Operator getOperator() {
        return operator;
    }
}
