// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import lombok.NonNull;

import java.util.List;

/**
 * Metadata for an {@code @ElementCollection} field on an entity.
 *
 * <p>Element collections store collections of simple values or embeddables in a separate database table
 * linked to the parent entity via join columns. The collection table has no primary key by default,
 * and duplicates are allowed.
 *
 * <p>Lifecycle: elements are loaded lazily, and inserting/updating/deleting the parent entity
 * cascades to the collection table automatically.
 *
 * @param <T> the Java type of the collection element
 * @see QAttribute
 * @see JoinColumnData
 */
public interface QElementCollection<T> extends QAttribute {

    /**
     * Returns the column (or embeddable) metadata for the collection element.
     *
     * @return the element attribute metadata
     */
    QComparableAttribute<T> getAttribute();

    /**
     * Returns the Java property name of the collection field on the parent entity.
     *
     * @return the entity field name
     */
    String getPropertyName();

    /**
     * Returns the database table name that stores the collection elements.
     *
     * @return the collection table name
     */
    String getTableName();

    /**
     * Returns the database schema of the collection table, or {@code null} if not specified.
     *
     * @return the schema name, or {@code null}
     */
    String getTableSchemaName();

    /**
     * Returns the join columns linking the collection table to the parent entity table.
     *
     * @return an immutable list of join column metadata
     */
    List<JoinColumnData> getJoinColumns();

    /**
     * Creates a new {@code QElementCollection} instance.
     *
     * @param <T>             the Java type of the collection element
     * @param attribute       the element attribute metadata
     * @param propertyName    the entity field name
     * @param tableName       the collection table name
     * @param tableSchemaName the collection table schema, or {@code null}
     * @param joinColumns     the join columns linking to the parent entity
     * @return a new element collection metadata instance
     */
    static <T> QElementCollection<T> of(
            @NonNull QComparableAttribute<T> attribute,
            @NonNull String propertyName,
            @NonNull String tableName,
            String tableSchemaName,
            @NonNull List<JoinColumnData> joinColumns
    ) {
        return new DefaultQElementCollection<>(
                attribute,
                propertyName,
                tableName,
                tableSchemaName,
                List.copyOf(joinColumns)
        );
    }
}
