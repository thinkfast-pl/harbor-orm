// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents a named SQL identifier such as a column or table reference.
 *
 * @param <T> the Java type of the identified element
 */
@RequiredArgsConstructor
public class NameExpression<T> implements Expression<T> {

    @NonNull
    @Getter
    private final Class<T> javaType;

    @NonNull
    @Getter
    private final String name;

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }
}
