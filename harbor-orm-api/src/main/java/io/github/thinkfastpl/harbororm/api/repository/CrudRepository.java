// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.repository;

import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Base repository interface providing standard CRUD (Create, Read, Update, Delete) operations
 * for entities.
 *
 * <p>This interface defines the contract for all entity repositories in HarborORM. The concrete
 * implementation is {@code EntityRepository}, which requires a {@link io.github.thinkfastpl.harbororm.api.HarborSession}
 * and a {@link io.github.thinkfastpl.harbororm.api.metadata.QEntity} metadata instance.
 *
 * <h2>Usage example</h2>
 * <pre>{@code
 * public class ProductRepository extends EntityRepository<ProductEntity, Long> {
 *     private static final QProductEntity ENTITY = new QProductEntity(null);
 *
 *     public ProductRepository(HarborSession session) {
 *         super(session, ENTITY);
 *     }
 *
 *     // Custom query methods can be added here
 * }
 *
 * // Usage
 * ProductEntity product = new ProductEntity(1L, "Widget", BigDecimal.TEN);
 * productRepository.insert(product);
 *
 * Optional<ProductEntity> found = productRepository.findById(1L);
 * }</pre>
 *
 * @param <T> the entity type
 * @param <ID> the entity's primary key type
 * @see io.github.thinkfastpl.harbororm.api.HarborSession
 * @see EntityNotFoundException
 */
public interface CrudRepository<T, ID> {

    /**
     * Inserts a new entity into the database.
     *
     * <p>Entity lifecycle callbacks ({@code @PreInsert}, {@code @PostInsert}) are invoked.
     * Auto-generated or sequence-generated IDs are populated on the entity after insertion.
     * Child entities are cascaded automatically.
     *
     * @param entity the entity to insert
     */
    void insert(@NonNull T entity);

    /**
     * Inserts multiple entities into the database. Default implementation calls
     * {@link #insert(Object)} for each entity.
     *
     * @param entities the entities to insert
     */
    default void insertAll(@NonNull Iterable<T> entities) {
        for (T entity : entities) {
            insert(entity);
        }
    }

    /**
     * Finds an entity by its primary key.
     *
     * @param id the primary key
     * @return an {@link Optional} containing the entity, or empty if not found
     */
    Optional<T> findById(@NonNull ID id);

    /**
     * Finds an entity by its primary key, throwing an exception if not found.
     * Delegates to {@link #findById(Object)} and throws {@link EntityNotFoundException}
     * if the result is empty.
     *
     * @param id the primary key
     * @return the entity
     * @throws EntityNotFoundException if no entity exists with the given ID
     */
    default T findByIdOrThrow(@NonNull ID id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity with ID = %s not found".formatted(id)));
    }

    /**
     * Finds an entity by its primary key and acquires a {@code FOR UPDATE} row lock.
     *
     * <p>The lock is held for the duration of the current transaction, preventing concurrent
     * modifications.
     *
     * @param id the primary key
     * @return an {@link Optional} containing the locked entity, or empty if not found
     */
    Optional<T> findByIdForUpdate(@NonNull ID id);

    /**
     * Finds an entity by its primary key with a {@code FOR UPDATE} lock, throwing an
     * exception if not found. Delegates to {@link #findByIdForUpdate(Object)} and throws
     * {@link EntityNotFoundException} if the result is empty.
     *
     * @param id the primary key
     * @return the locked entity
     * @throws EntityNotFoundException if no entity exists with the given ID
     */
    default T findByIdForUpdateOrThrow(@NonNull ID id) {
        return findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity with ID = %s not found".formatted(id)));
    }

    /**
     * Checks whether an entity with the given primary key exists. Default implementation
     * delegates to {@code findById(id).isPresent()}.
     *
     * @param id the primary key
     * @return {@code true} if an entity with the given ID exists
     */
    default boolean existsById(@NonNull ID id) {
        return findById(id).isPresent();
    }

    /**
     * Returns all entities of this type.
     *
     * @return the list of all entities (empty list if none exist)
     */
    List<T> findAll();

    /**
     * Returns all entities whose primary keys are in the given collection.
     *
     * @param ids the collection of primary keys
     * @return the list of matching entities (may be smaller than the input if some IDs are missing)
     */
    List<T> findAllById(@NonNull Collection<ID> ids);

    /**
     * Returns a stream of all entities for memory-efficient iteration.
     *
     * <p><b>Important:</b> The returned stream must be closed by the caller (e.g.,
     * using try-with-resources) to release the underlying database resources.
     *
     * @return a stream of all entities
     */
    Stream<T> streamAll();

    /**
     * Counts the total number of entities of this type.
     *
     * @return the total entity count
     */
    long countAll();

    /**
     * Updates an existing entity in the database.
     *
     * <p>Entity lifecycle callbacks ({@code @PreUpdate}, {@code @PostUpdate}) are invoked.
     * If the entity has a {@code @Version} field, optimistic locking is enforced.
     * Child entities are cascaded only if their collection or reference was accessed.
     *
     * @param entity the entity to update
     * @throws io.github.thinkfastpl.harbororm.api.exception.OptimisticLockException if the entity has a
     *         {@code @Version} field and the row was modified by another transaction
     */
    void update(@NonNull T entity);

    /**
     * Deletes an entity from the database.
     *
     * <p>Entity lifecycle callbacks ({@code @PreDelete}, {@code @PostDelete}) are invoked.
     * Child entities and element collections are deleted first to satisfy foreign key
     * constraints. If the entity has a {@code @Version} field, optimistic locking is enforced.
     *
     * @param entity the entity to delete
     * @throws io.github.thinkfastpl.harbororm.api.exception.OptimisticLockException if the entity has a
     *         {@code @Version} field and the row was modified by another transaction
     */
    void delete(@NonNull T entity);

    /**
     * Deletes multiple entities from the database. Default implementation calls
     * {@link #delete(Object)} for each entity.
     *
     * @param entities the entities to delete
     */
    default void deleteAll(@NonNull List<T> entities) {
        for (T entity : entities) {
            delete(entity);
        }
    }

    /**
     * Deletes an entity by its primary key.
     *
     * @param id the primary key of the entity to delete
     */
    void deleteById(@NonNull ID id);

    /**
     * Deletes multiple entities by their primary keys. Default implementation calls
     * {@link #deleteById(Object)} for each ID.
     *
     * @param ids the collection of primary keys of entities to delete
     */
    default void deleteAllById(@NonNull Collection<ID> ids) {
        for (ID id : ids) {
            deleteById(id);
        }
    }
}
