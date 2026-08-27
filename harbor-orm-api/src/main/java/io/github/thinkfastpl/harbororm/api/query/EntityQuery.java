// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.query;

import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.expression.Order;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Type-safe query builder for selecting entities.
 *
 * <p>Unlike the raw {@link SelectQuery}, this builder works directly with entity metadata
 * ({@link io.github.thinkfastpl.harbororm.api.metadata.QEntity}) and returns fully mapped entity instances with all
 * relationships (lazy-loaded), element collections, and type conversions applied.
 *
 * <p>The builder supports WHERE, ORDER BY, LIMIT, OFFSET, FOR UPDATE, existence checks,
 * counting, and streaming.
 *
 * <h2>Usage example</h2>
 * <pre>{@code
 * QSomeEntity ENTITY = new QSomeEntity(null);
 *
 * List<SomeEntity> results = session.selectEntity(ENTITY)
 *     .where(ENTITY.name.eq("Alice").and(ENTITY.id.gt(100)))
 *     .orderBy(ENTITY.name.asc())
 *     .limit(10)
 *     .fetchAll();
 * }</pre>
 *
 * <h2>Find by ID</h2>
 * <pre>{@code
 * Optional<SomeEntity> entity = session.selectEntity(ENTITY)
 *     .whereIdEq(42L)
 *     .fetchOne();
 * }</pre>
 *
 * <h2>Streaming for large result sets</h2>
 * <pre>{@code
 * try (Stream<SomeEntity> stream = session.selectEntity(ENTITY).streamAll(100)) {
 *     stream.forEach(entity -> process(entity));
 * }
 * }</pre>
 *
 * @param <T> the entity type
 * @param <ID> the entity's primary key type
 * @see HarborSession#selectEntity(io.github.thinkfastpl.harbororm.api.metadata.QEntity)
 */
public interface EntityQuery<T, ID> {

    /**
     * Adds WHERE conditions to filter entities. Multiple calls are combined with AND.
     *
     * @param conditions the filter conditions
     * @return this query for further chaining
     */
    EntityQuery<T, ID> where(@NonNull List<Condition> conditions);

    /**
     * Adds a single WHERE condition. Convenience overload that delegates to
     * {@code where(List.of(condition))}.
     *
     * @param condition the filter condition
     * @return this query for further chaining
     */
    default EntityQuery<T, ID> where(@NonNull Condition condition) {
        return where(List.of(condition));
    }

    /**
     * Adds WHERE conditions. Convenience varargs overload that delegates to
     * {@code where(Arrays.asList(conditions))}.
     *
     * @param conditions the filter conditions
     * @return this query for further chaining
     */
    default EntityQuery<T, ID> where(@NonNull Condition... conditions) {
        return where(Arrays.asList(conditions));
    }

    /**
     * Adds a WHERE condition that filters by the entity's primary key.
     * Equivalent to {@code where(qEntity.getIdColumn().eq(id))}.
     *
     * @param id the primary key value to match
     * @return this query for further chaining
     */
    EntityQuery<T, ID> whereIdEq(@NonNull ID id);

    /**
     * Adds a WHERE condition that filters by a collection of primary keys
     * (SQL {@code IN} clause).
     *
     * @param id the collection of primary key values to match
     * @return this query for further chaining
     */
    EntityQuery<T, ID> whereIdIn(@NonNull Collection<ID> id);

    /**
     * Sorts the result set by the specified orderings.
     *
     * @param orders the sort orderings (e.g., {@code qEntity.name.asc()})
     * @return this query for further chaining
     */
    EntityQuery<T, ID> orderBy(@NonNull List<Order> orders);

    /**
     * Sorts the result set by a single ordering. Convenience overload that delegates to
     * {@code orderBy(List.of(order))}.
     *
     * @param order the sort ordering
     * @return this query for further chaining
     */
    default EntityQuery<T, ID> orderBy(@NonNull Order order) {
        return orderBy(List.of(order));
    }

    /**
     * Sorts the result set by the specified orderings. Convenience varargs overload that
     * delegates to {@code orderBy(Arrays.asList(orders))}.
     *
     * @param orders the sort orderings
     * @return this query for further chaining
     */
    default EntityQuery<T, ID> orderBy(@NonNull Order... orders) {
        return orderBy(Arrays.asList(orders));
    }

    /**
     * Limits the result set to the specified maximum number of entities.
     *
     * @param limit the maximum number of entities to return
     * @return this query for further chaining
     */
    EntityQuery<T, ID> limit(@NonNull Integer limit);

    /**
     * Skips the specified number of entities before returning results.
     *
     * @param offset the number of entities to skip
     * @return this query for further chaining
     */
    EntityQuery<T, ID> offset(@NonNull Integer offset);

    /**
     * Adds a {@code FOR UPDATE} clause to lock selected rows for the duration of the
     * current transaction.
     *
     * @return this query for further chaining
     */
    EntityQuery<T, ID> forUpdate();

    /**
     * Checks whether any entities match the query conditions without loading them.
     *
     * <p>This is more efficient than {@code fetchAll().isEmpty()} because it uses
     * an existence check (e.g., {@code SELECT EXISTS(...)}).
     *
     * @return {@code true} if at least one matching entity exists
     */
    boolean exists();

    /**
     * Counts the number of entities matching the query conditions without loading them.
     *
     * @return the number of matching entities
     */
    long count();

    /**
     * Executes the query and returns all matching entities as a list.
     *
     * @return the list of matching entities (empty list if no results)
     */
    List<T> fetchAll();

    /**
     * Executes the query and returns exactly one entity.
     *
     * @return the single matching entity
     * @throws IllegalStateException if the result count is not exactly 1
     */
    default T fetchSingle() {
        final List<T> all = fetchAll();
        if (all.size() != 1) {
            throw new IllegalStateException("Expected only 1 result. Got: " + all.size());
        }
        return all.get(0);
    }

    /**
     * Executes the query and returns zero or one entity.
     *
     * @return an {@link Optional} containing the matching entity, or empty if no results
     * @throws IllegalStateException if more than 1 entity is returned
     */
    default Optional<T> fetchOne() {
        final List<T> all = fetchAll();
        return switch (all.size()) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of(all.get(0));
            default -> throw new IllegalStateException("Expected exactly 1 or 0 results. Got: " + all.size());
        };
    }

    /**
     * Executes the query and returns a stream for memory-efficient iteration using
     * the default fetch size.
     *
     * <p><b>Important:</b> The returned stream must be closed by the caller (e.g.,
     * using try-with-resources) to release the underlying database resources.
     *
     * @return a stream of matching entities
     */
    Stream<T> streamAll();

    /**
     * Returns a stream for memory-efficient iteration with custom fetch size.
     * The stream MUST be closed by the caller.
     * @param fetchSize number of rows to fetch per database round-trip
     */
    Stream<T> streamAll(int fetchSize);
}
