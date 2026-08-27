// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * Represents a collection of constant values, typically used as the right-hand side of an {@code IN} clause.
 *
 * @param <T> the Java type of the constant values
 */
@RequiredArgsConstructor
public class ConstantsExpression<T> implements Expression<T> {

    @NonNull
    private final Class<T> constantsClass;

    @Getter
    private final Collection<T> constantsValue;

    @Override
    public Class<T> getJavaType() {
        return constantsClass;
    }

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }
}
