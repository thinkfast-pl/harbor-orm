// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A condition that compares two expressions with a binary operator
 * such as {@code =}, {@code <}, {@code >}, {@code IN}, or {@code IS DISTINCT FROM}.
 */
@RequiredArgsConstructor
@Getter
public final class BinaryOperatorCondition implements Condition {

    public enum Operator {
        LT,
        GT,
        LE,
        GE,
        EQ,
        NOT_EQ,
        IS_DISTINCT_FROM,
        IS_NOT_DISTINCT_FROM,
        IN,
    }

    @NonNull
    private final Expression<?> leftExpression;

    @NonNull
    private final Expression<?> rightExpression;

    @NonNull
    private final Operator operator;
}
