// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;

/**
 * Callback interface for monitoring SQL query execution.
 * <p>
 * Implement this interface to observe queries before and after they execute.
 * Both methods have default no-op implementations, so you only need to override
 * the ones you care about.
 * <p>
 * Example — log slow queries:
 * <pre>
 * SqlQueryMonitor monitor = new SqlQueryMonitor() {
 *     &#64;Override
 *     public void afterQuery(SqlQuery query, long executionTimeMs, Exception exception) {
 *         if (executionTimeMs &gt; 100) {
 *             log.warn("Slow query ({}ms): {}", executionTimeMs, query.getSql());
 *         }
 *     }
 * };
 * </pre>
 */
public interface SqlQueryMonitor {

    /**
     * Called immediately before a SQL query is executed.
     *
     * @param query the SQL query about to be executed
     */
    default void beforeQuery(SqlQuery query) {
    }

    /**
     * Called immediately after a SQL query finishes (successfully or not).
     * If the query threw an exception, it is passed here and then rethrown to the caller.
     *
     * @param query           the SQL query that was executed
     * @param executionTimeMs wall-clock execution time in milliseconds
     * @param exception       {@code null} on success; the thrown exception on failure
     */
    default void afterQuery(SqlQuery query, long executionTimeMs, Exception exception) {
    }
}
