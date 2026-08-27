// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import lombok.NonNull;
import lombok.Value;

/**
 * Represents an {@code ORDER BY} specification pairing an expression with a sort direction
 * (ascending or descending).
 */
@Value
public class Order {

    @NonNull
    Expression<?> expression;
    boolean asc;
}
