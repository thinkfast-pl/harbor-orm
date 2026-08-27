// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

/**
 * Minimal interface for expressions that can appear in a SQL {@code SELECT} clause.
 * Provides only the Java type of the expression result.
 *
 * @param <T> the Java type of the expression result
 */
public interface SelectableExpression<T> {

    Class<T> getJavaType();
}
