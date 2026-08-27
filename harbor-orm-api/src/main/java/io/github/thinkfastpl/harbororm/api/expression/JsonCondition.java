// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Boolean result of a JSON operation (contains, hasKey, etc.).
 */
@RequiredArgsConstructor
@Getter
public class JsonCondition implements Condition {

    @NonNull
    private final Expression<?> source;

    @NonNull
    private final JsonExpression.Operator operator;

    private final String[] args;

    private final Expression<?> other;
}
