// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import lombok.NonNull;
import lombok.Value;

import java.util.List;

/**
 * Default implementation of {@link QElementCollection}.
 *
 * <p>This is a package-private immutable value class created by {@link QElementCollection#of}.
 *
 * @param <T> the Java type of the collection element
 * @see QElementCollection
 */
@Value
class DefaultQElementCollection<T> implements QElementCollection<T> {

    @NonNull
    QComparableAttribute<T> attribute;

    @NonNull
    String propertyName;

    @NonNull
    String tableName;

    String tableSchemaName;

    @NonNull
    List<JoinColumnData> joinColumns;
}
