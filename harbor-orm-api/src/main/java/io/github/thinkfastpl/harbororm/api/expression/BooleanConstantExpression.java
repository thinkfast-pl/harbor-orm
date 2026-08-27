// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A condition representing a constant boolean value ({@code TRUE} or {@code FALSE}).
 */
@RequiredArgsConstructor
@Getter
public class BooleanConstantExpression implements Condition {
    private final Boolean value;
}
