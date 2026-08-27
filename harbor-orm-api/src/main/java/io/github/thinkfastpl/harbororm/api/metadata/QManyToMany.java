// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import lombok.NonNull;

import java.util.List;

/**
 * Metadata for a {@code @ManyToMany} relationship.
 *
 * @param <T> the related entity type (or ID type for ID-only variant)
 */
public interface QManyToMany<T> extends QAttribute {

    /**
     * The property name on the owning entity.
     */
    String getPropertyName();

    /**
     * The join table name.
     */
    String getTableName();

    /**
     * The join table schema (may be null).
     */
    String getTableSchemaName();

    /**
     * Join columns referencing the owning entity.
     */
    List<JoinColumnData> getJoinColumns();

    /**
     * Join columns referencing the related entity.
     */
    List<JoinColumnData> getInverseJoinColumns();

    /**
     * The related entity's QEntity metadata. Null for ID-only variant.
     */
    QEntity<?, ?> getRelatedQEntity();

    /**
     * Whether this is an ID-only variant (Set of ID type rather than Set of entities).
     */
    boolean isIdOnly();

    /**
     * Embeddable metadata for the element type when this is an ID-only variant whose element type
     * is an {@code @Embeddable} composite ID. Null otherwise.
     */
    default QEmbeddable<?> getElementEmbeddable() {
        return null;
    }

    static <T> QManyToMany<T> of(
            @NonNull String propertyName,
            @NonNull String tableName,
            String tableSchemaName,
            @NonNull List<JoinColumnData> joinColumns,
            @NonNull List<JoinColumnData> inverseJoinColumns,
            QEntity<?, ?> relatedQEntity,
            boolean idOnly
    ) {
        return of(propertyName, tableName, tableSchemaName, joinColumns, inverseJoinColumns, relatedQEntity, null, idOnly);
    }

    static <T> QManyToMany<T> of(
            @NonNull String propertyName,
            @NonNull String tableName,
            String tableSchemaName,
            @NonNull List<JoinColumnData> joinColumns,
            @NonNull List<JoinColumnData> inverseJoinColumns,
            QEntity<?, ?> relatedQEntity,
            QEmbeddable<?> elementEmbeddable,
            boolean idOnly
    ) {
        return new DefaultQManyToMany<>(propertyName, tableName, tableSchemaName, joinColumns, inverseJoinColumns, relatedQEntity, elementEmbeddable, idOnly);
    }
}
