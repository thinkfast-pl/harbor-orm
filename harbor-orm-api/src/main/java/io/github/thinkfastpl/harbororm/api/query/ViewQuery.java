// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.query;

import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.expression.Order;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Type-safe query builder for selecting view rows as typed Java instances.
 *
 * <p>Unlike {@link EntityQuery}, views are read-only and have no identity concept,
 * lifecycle callbacks, or relationships. This builder provides WHERE, ORDER BY,
 * LIMIT, OFFSET, and DISTINCT support.
 *
 * <h2>Usage examples</h2>
 *
 * <p>Simple filtered query:
 * <pre>{@code
 * List<UserSummary> users = session.selectView(QUserSummary.INSTANCE)
 *     .where(QUserSummary.INSTANCE.active.eq(true))
 *     .orderBy(QUserSummary.INSTANCE.name.asc())
 *     .fetchAll();
 * }</pre>
 *
 * <p>Paginated query:
 * <pre>{@code
 * List<UserSummary> page = session.selectView(QUserSummary.INSTANCE)
 *     .orderBy(QUserSummary.INSTANCE.name.asc())
 *     .limit(20)
 *     .offset(40)
 *     .fetchAll();
 * }</pre>
 *
 * <p>Distinct results:
 * <pre>{@code
 * List<UserSummary> distinct = session.selectView(QUserSummary.INSTANCE)
 *     .distinct()
 *     .fetchAll();
 * }</pre>
 *
 * @param <T> the view class type
 */
public interface ViewQuery<T> {

    /**
     * Adds WHERE conditions to filter rows. Multiple calls are combined with AND.
     *
     * @param conditions the filter conditions
     * @return this query for further chaining
     */
    ViewQuery<T> where(@NonNull List<Condition> conditions);

    /**
     * Adds a single WHERE condition. Convenience overload that delegates to
     * {@code where(List.of(condition))}.
     *
     * @param condition the filter condition
     * @return this query for further chaining
     */
    default ViewQuery<T> where(@NonNull Condition condition) {
        return where(List.of(condition));
    }

    /**
     * Adds WHERE conditions. Convenience varargs overload that delegates to
     * {@code where(Arrays.asList(conditions))}.
     *
     * @param conditions the filter conditions
     * @return this query for further chaining
     */
    default ViewQuery<T> where(@NonNull Condition... conditions) {
        return where(Arrays.asList(conditions));
    }

    /**
     * Sorts the result set by the specified orderings.
     *
     * @param orders the sort orderings (e.g., {@code column.asc()}, {@code column.desc()})
     * @return this query for further chaining
     */
    ViewQuery<T> orderBy(@NonNull List<Order> orders);

    /**
     * Sorts the result set by a single ordering. Convenience overload that delegates to
     * {@code orderBy(List.of(order))}.
     *
     * @param order the sort ordering
     * @return this query for further chaining
     */
    default ViewQuery<T> orderBy(@NonNull Order order) {
        return orderBy(List.of(order));
    }

    /**
     * Sorts the result set by the specified orderings. Convenience varargs overload
     * that delegates to {@code orderBy(Arrays.asList(orders))}.
     *
     * @param orders the sort orderings
     * @return this query for further chaining
     */
    default ViewQuery<T> orderBy(@NonNull Order... orders) {
        return orderBy(Arrays.asList(orders));
    }

    /**
     * Limits the result set to the specified maximum number of rows.
     *
     * @param limit the maximum number of rows to return
     * @return this query for further chaining
     */
    ViewQuery<T> limit(@NonNull Integer limit);

    /**
     * Skips the specified number of rows before returning results.
     *
     * @param offset the number of rows to skip
     * @return this query for further chaining
     */
    ViewQuery<T> offset(@NonNull Integer offset);

    /**
     * Adds the {@code DISTINCT} keyword to the query, eliminating duplicate rows
     * from the result set.
     *
     * @return this query for further chaining
     */
    ViewQuery<T> distinct();

    /**
     * Checks whether any rows match the current query conditions.
     *
     * @return {@code true} if at least one matching row exists, {@code false} otherwise
     */
    boolean exists();

    /**
     * Returns the number of rows matching the current query conditions.
     *
     * @return the row count
     */
    long count();

    /**
     * Executes the query and returns all results as a list.
     *
     * @return the list of result rows (empty list if no results)
     */
    List<T> fetchAll();

    /**
     * Executes the query and returns exactly one result.
     *
     * @return the single result row
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
     * Executes the query and returns zero or one result.
     *
     * @return an {@link Optional} containing the result row, or empty if no results
     * @throws IllegalStateException if more than 1 row is returned
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
     * @return a stream of result rows
     */
    Stream<T> streamAll();

    /**
     * Executes the query and returns a stream for memory-efficient iteration with
     * a custom fetch size.
     *
     * <p><b>Important:</b> The returned stream must be closed by the caller (e.g.,
     * using try-with-resources) to release the underlying database resources.
     *
     * @param fetchSize number of rows to fetch per database round-trip
     * @return a stream of result rows
     */
    Stream<T> streamAll(int fetchSize);
}
