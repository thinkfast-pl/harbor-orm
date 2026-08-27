// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A condition with three operands, used for {@code BETWEEN}, {@code NOT BETWEEN},
 * {@code BETWEEN SYMMETRIC}, and {@code LIKE ... ESCAPE} operations.
 */
@RequiredArgsConstructor
@Getter
public class TernaryOperatorCondition implements Condition {

    public enum Operator {
        BETWEEN,
        NOT_BETWEEN,
        BETWEEN_SYMMETRIC,
        NOT_BETWEEN_SYMMETRIC,
        LIKE_ESCAPE,
    }

    @NonNull
    private final Expression<?> leftExpression;

    @NonNull
    private final Expression<?> middleExpression;

    @NonNull
    private final Expression<?> rightExpression;

    @NonNull
    private final Operator operator;
}
