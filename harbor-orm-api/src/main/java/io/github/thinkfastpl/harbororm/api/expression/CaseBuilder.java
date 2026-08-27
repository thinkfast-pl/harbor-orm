// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import lombok.NonNull;

/**
 * Fluent builder interface for constructing SQL {@code CASE WHEN ... THEN ... ELSE ... END} expressions.
 *
 * @param <T> the Java type of the CASE expression result
 * @see DSL#case_(Class)
 */
public interface CaseBuilder<T> {

    CaseBuilder<T> whenThen(@NonNull Condition condition, Expression<T> then);

    Expression<T> else_(Expression<T> else_);

    Expression<T> end();
}
