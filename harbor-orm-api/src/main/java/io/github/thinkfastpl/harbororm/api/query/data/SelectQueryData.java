// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.query.data;

import io.github.thinkfastpl.harbororm.api.expression.*;
import io.github.thinkfastpl.harbororm.api.metadata.QTableSource;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

/**
 * Immutable data carrier for a SQL SELECT statement.
 *
 * <p>Captures every clause of a SELECT query: CTEs, DISTINCT, column expressions, FROM source,
 * JOINs, WHERE, GROUP BY, HAVING, set combinations (UNION/INTERSECT/EXCEPT), ORDER BY,
 * LIMIT/OFFSET, and FOR UPDATE. Together these describe a fully built SELECT query ready for
 * execution by a {@link io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor}.
 *
 * @see io.github.thinkfastpl.harbororm.api.query.SelectQuery
 */
@Value
public class SelectQueryData {

    /**
     * A set combination (UNION, INTERSECT, or EXCEPT) paired with another SELECT expression.
     */
    @Value
    public static class Combination {

        /** The right-hand SELECT expression in the set combination. */
        @NonNull
        SelectExpression<?> selectExpression;

        /** The type of set combination (UNION, INTERSECT, or EXCEPT). */
        @NonNull
        SelectCombination combination;

        /** Whether the ALL modifier is applied (e.g., UNION ALL). */
        boolean all;
    }

    /** Common table expressions (WITH clause). Null or empty if none. */
    List<CommonTableExpression> ctes;

    /** Whether the WITH clause is recursive (WITH RECURSIVE). */
    boolean withRecursive;

    /** Whether the SELECT uses DISTINCT. */
    boolean distinct;

    /** Column expressions in the SELECT list. */
    @NonNull
    List<Expression<?>> selectExpressions;

    /** The FROM source (table, subquery, or CTE reference). Null for value-only queries. */
    QTableSource from;

    /** JOIN clauses. Null or empty if none. */
    List<Join> joins;

    /** WHERE conditions. Null or empty if none. */
    List<Condition> whereConditions;

    /** GROUP BY expressions. Null or empty if none. */
    List<Expression<?>> groupByExpressions;

    /** HAVING conditions (applied after GROUP BY). Null or empty if none. */
    List<Condition> havingConditions;

    /** Set combinations (UNION, INTERSECT, EXCEPT) with other queries. Null or empty if none. */
    List<Combination> combinations;

    /** ORDER BY clauses. Null or empty if none. */
    List<Order> orders;

    /** Maximum number of rows to return (LIMIT). Null for unlimited. */
    Integer limit;

    /** Number of rows to skip (OFFSET). Null for no offset. */
    Integer offset;

    /** Whether a FOR UPDATE lock is requested. */
    boolean forUpdate;
}
