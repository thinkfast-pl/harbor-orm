// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import lombok.NonNull;
import lombok.Value;

import java.util.List;

/**
 * Default implementation of {@link QManyToMany}.
 *
 * <p>This is a package-private immutable value class created by {@link QManyToMany#of}.
 *
 * @param <T> the related entity type (or ID type for the ID-only variant)
 * @see QManyToMany
 */
@Value
class DefaultQManyToMany<T> implements QManyToMany<T> {

    @NonNull
    String propertyName;

    @NonNull
    String tableName;

    String tableSchemaName;

    @NonNull
    List<JoinColumnData> joinColumns;

    @NonNull
    List<JoinColumnData> inverseJoinColumns;

    QEntity<?, ?> relatedQEntity;

    QEmbeddable<?> elementEmbeddable;

    boolean idOnly;
}
