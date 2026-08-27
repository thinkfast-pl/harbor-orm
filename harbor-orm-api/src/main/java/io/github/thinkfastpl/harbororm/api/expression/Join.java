// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.QTableSource;
import lombok.NonNull;
import lombok.Value;

/**
 * Represents a SQL {@code JOIN} clause including the join type, target table,
 * optional {@code ON} condition, and lateral flag.
 */
@Value
public class Join {

    public enum Type {
        INNER,
        LEFT,
        RIGHT,
        FULL_OUTER,
        CROSS,
    }

    @NonNull
    QTableSource table;

    @NonNull
    Type type;

    Condition on;

    boolean lateral;
}
