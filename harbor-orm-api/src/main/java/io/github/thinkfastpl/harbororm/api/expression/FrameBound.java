// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import lombok.Getter;

/**
 * Window frame boundary specification for ROWS/RANGE BETWEEN clauses.
 */
@Getter
public class FrameBound {
    public static final FrameBound UNBOUNDED_PRECEDING = new FrameBound("UNBOUNDED PRECEDING");
    public static final FrameBound CURRENT_ROW = new FrameBound("CURRENT ROW");
    public static final FrameBound UNBOUNDED_FOLLOWING = new FrameBound("UNBOUNDED FOLLOWING");

    private final String sql;

    private FrameBound(String sql) {
        this.sql = sql;
    }

    public static FrameBound preceding(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must be non-negative");
        }
        return new FrameBound(offset + " PRECEDING");
    }

    public static FrameBound following(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must be non-negative");
        }
        return new FrameBound(offset + " FOLLOWING");
    }

    public String toSql() {
        return sql;
    }
}
