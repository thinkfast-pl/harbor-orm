// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

/**
 * SQL set operations for combining the results of two or more SELECT queries.
 */
public enum SelectCombination {
    UNION,
    INTERSECT,
    EXCEPT,
}
