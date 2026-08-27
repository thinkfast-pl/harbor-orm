// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import java.util.Optional;

/**
 * Entity metadata interface for full CRUD operations.
 *
 * <p>Generated {@code Q<EntityName>} classes implement this interface. It provides all information
 * needed to insert, update, delete, and select entities, including primary key metadata, entity
 * class type, lifecycle callbacks, and all attribute definitions.
 *
 * <p>Each generated class requires an alias parameter in its constructor (can be {@code null} for the default):
 * <pre>{@code
 * QSomeEntity ENTITY = new QSomeEntity(null);   // default alias
 * QSomeEntity ENTITY = new QSomeEntity("e");    // custom alias "e"
 * }</pre>
 *
 * <p>Example usage with {@code HarborSession}:
 * <pre>{@code
 * QSomeEntity ENTITY = new QSomeEntity(null);
 * List<SomeEntity> results = session
 *     .selectEntity(ENTITY)
 *     .where(ENTITY.name.eq("value"))
 *     .fetchAll();
 * }</pre>
 *
 * @param <T>  the entity Java type
 * @param <ID> the entity primary key type
 * @see QTable
 * @see QAttributeHolder
 * @see QComparableAttribute
 */
public interface QEntity<T, ID> extends QAttributeHolder {

    /**
     * Returns the qualified table name including optional schema and alias.
     *
     * @return the table name metadata
     */
    QTableName getTableName();

    /**
     * Returns the entity Java class.
     *
     * @return the entity bean type
     */
    Class<T> getBeanType();

    /**
     * Returns the primary key column (or embedded composite key) metadata.
     *
     * @return the ID column as a comparable attribute
     */
    QComparableAttribute<ID> getIdColumn();

    /**
     * Returns the {@code @Version} column metadata for optimistic locking, if present.
     *
     * @return an {@link Optional} containing the version column, or empty if no version column is defined
     */
    default Optional<QColumn<?>> getVersionColumn() {
        return Optional.empty();
    }
}
