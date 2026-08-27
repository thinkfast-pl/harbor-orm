// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

/**
 * Represents a complete SQL {@code CASE} expression with one or more {@code WHEN/THEN} branches
 * and an optional {@code ELSE} clause.
 *
 * @param <T> the Java type of the CASE expression result
 */
@RequiredArgsConstructor
public class CaseWhenThenExpression<T> implements Expression<T> {

    @Value
    public static class WhenThen<T> {

        @NonNull
        Condition when;

        Expression<T> then;
    }

    @NonNull
    @Getter
    private final List<WhenThen<T>> conditions;

    @Getter
    private final Expression<T> else_;

    @NonNull
    @Getter
    private final Class<T> javaType;

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }
}
