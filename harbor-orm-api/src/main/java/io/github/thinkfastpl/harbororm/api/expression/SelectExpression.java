// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.QTable;
import io.github.thinkfastpl.harbororm.api.metadata.QTableName;
import io.github.thinkfastpl.harbororm.api.metadata.QTableSource;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;

/**
 * Interface for building SQL {@code SELECT} statements.
 * Supports all standard clauses (WITH, SELECT, FROM, JOIN, WHERE, GROUP BY, HAVING,
 * set operations, ORDER BY, LIMIT, OFFSET, FOR UPDATE) in both imperative and
 * fluent step-based styles.
 *
 * @param <T> the Java type of the query result rows
 */
public interface SelectExpression<T> extends Expression<T> {

    /**
     * Fluent step for appending expressions to the {@code SELECT} list.
     *
     * @param <T> the Java type of the query result rows
     */
    interface SelectStep<T> extends FromStep<T> {

        /**
         * Adds a single expression to the {@code SELECT} list.
         *
         * @param expression the expression to select
         * @return this step for further chaining
         */
        SelectStep<T> select(@NonNull Expression<?> expression);

        /**
         * Adds multiple expressions to the {@code SELECT} list.
         *
         * @param expressions the expressions to select
         * @return this step for further chaining
         */
        SelectStep<T> select(@NonNull List<? extends Expression<?>> expressions);
    }

    /**
     * Fluent step for specifying the {@code FROM} clause.
     *
     * @param <T> the Java type of the query result rows
     */
    interface FromStep<T> extends WhereState<T> {

        /**
         * Sets the {@code FROM} clause to the given table source.
         *
         * @param tableSource the table source to select from
         * @return the join step for adding joins or continuing the query
         */
        JoinStep<T> from(@NonNull QTableSource tableSource);

        /**
         * Sets the {@code FROM} clause using a table name.
         *
         * @param tableName the name of the table
         * @return the join step for adding joins or continuing the query
         */
        default JoinStep<T> from(@NonNull String tableName) {
            return from(new QTableName(tableName, null, null));
        }

        /**
         * Sets the {@code FROM} clause using a table name and schema.
         *
         * @param tableName the name of the table
         * @param schema    the schema containing the table, or {@code null} for the default schema
         * @return the join step for adding joins or continuing the query
         */
        default JoinStep<T> from(@NonNull String tableName, String schema) {
            return from(new QTableName(tableName, schema, null));
        }

        /**
         * Sets the {@code FROM} clause using a generated table metadata object.
         *
         * @param qTable the table metadata
         * @return the join step for adding joins or continuing the query
         */
        default JoinStep<T> from(@NonNull QTable qTable) {
            return from(qTable.getTableName());
        }
    }

    /**
     * Fluent step for adding {@code JOIN} clauses.
     *
     * @param <T> the Java type of the query result rows
     */
    interface JoinStep<T> extends WhereState<T> {

        /**
         * Adds an {@code INNER JOIN} to the given table source.
         *
         * @param name the table source to join
         * @return the join-on step for specifying the join condition
         */
        JoinOnStep<T> join(@NonNull QTableSource name);

        /**
         * Adds an {@code INNER JOIN} using a table name.
         *
         * @param tableName the name of the table to join
         * @return the join-on step for specifying the join condition
         */
        default JoinOnStep<T> join(@NonNull String tableName) {
            return join(new QTableName(tableName, null, null));
        }

        /**
         * Adds an {@code INNER JOIN} using a table name and schema.
         *
         * @param tableName the name of the table to join
         * @param schema    the schema containing the table, or {@code null} for the default schema
         * @return the join-on step for specifying the join condition
         */
        default JoinOnStep<T> join(@NonNull String tableName, String schema) {
            return join(new QTableName(tableName, schema, null));
        }

        /**
         * Adds an {@code INNER JOIN} using a generated table metadata object.
         *
         * @param qTable the table metadata
         * @return the join-on step for specifying the join condition
         */
        default JoinOnStep<T> join(@NonNull QTable qTable) {
            return join(qTable.getTableName());
        }

        /**
         * Adds a {@code LEFT JOIN} to the given table source.
         *
         * @param name the table source to join
         * @return the join-on step for specifying the join condition
         */
        JoinOnStep<T> leftJoin(@NonNull QTableSource name);

        /**
         * Adds a {@code LEFT JOIN} using a table name.
         *
         * @param tableName the name of the table to join
         * @return the join-on step for specifying the join condition
         */
        default JoinOnStep<T> leftJoin(@NonNull String tableName) {
            return leftJoin(new QTableName(tableName, null, null));
        }

        /**
         * Adds a {@code LEFT JOIN} using a table name and schema.
         *
         * @param tableName the name of the table to join
         * @param schema    the schema containing the table, or {@code null} for the default schema
         * @return the join-on step for specifying the join condition
         */
        default JoinOnStep<T> leftJoin(@NonNull String tableName, String schema) {
            return leftJoin(new QTableName(tableName, schema, null));
        }

        /**
         * Adds a {@code LEFT JOIN} using a generated table metadata object.
         *
         * @param qTable the table metadata
         * @return the join-on step for specifying the join condition
         */
        default JoinOnStep<T> leftJoin(@NonNull QTable qTable) {
            return leftJoin(qTable.getTableName());
        }

        /**
         * Adds a {@code RIGHT JOIN} to the given table source.
         *
         * @param name the table source to join
         * @return the join-on step for specifying the join condition
         */
        JoinOnStep<T> rightJoin(@NonNull QTableSource name);

        /**
         * Adds a {@code RIGHT JOIN} using a table name.
         *
         * @param tableName the name of the table to join
         * @return the join-on step for specifying the join condition
         */
        default JoinOnStep<T> rightJoin(@NonNull String tableName) {
            return rightJoin(new QTableName(tableName, null, null));
        }

        /**
         * Adds a {@code RIGHT JOIN} using a table name and schema.
         *
         * @param tableName the name of the table to join
         * @param schema    the schema containing the table, or {@code null} for the default schema
         * @return the join-on step for specifying the join condition
         */
        default JoinOnStep<T> rightJoin(@NonNull String tableName, String schema) {
            return rightJoin(new QTableName(tableName, schema, null));
        }

        /**
         * Adds a {@code RIGHT JOIN} using a generated table metadata object.
         *
         * @param qTable the table metadata
         * @return the join-on step for specifying the join condition
         */
        default JoinOnStep<T> rightJoin(@NonNull QTable qTable) {
            return rightJoin(qTable.getTableName());
        }

        /**
         * Adds a {@code CROSS JOIN} to the given table source.
         * Cross joins produce a Cartesian product and require no join condition.
         *
         * @param name the table source to cross join
         * @return this step for further chaining
         */
        JoinStep<T> crossJoin(@NonNull QTableSource name);

        /**
         * Adds an {@code INNER JOIN LATERAL} to the given table source.
         * Lateral joins allow the subquery to reference columns from preceding tables.
         *
         * @param name the table source to laterally join
         * @return the join-on step for specifying the join condition
         */
        JoinOnStep<T> innerJoinLateral(@NonNull QTableSource name);

        /**
         * Adds a {@code LEFT JOIN LATERAL} to the given table source.
         * Lateral joins allow the subquery to reference columns from preceding tables.
         *
         * @param name the table source to laterally join
         * @return the join-on step for specifying the join condition
         */
        JoinOnStep<T> leftJoinLateral(@NonNull QTableSource name);

        /**
         * Adds a {@code CROSS JOIN LATERAL} to the given table source.
         * Lateral joins allow the subquery to reference columns from preceding tables.
         *
         * @param name the table source to laterally cross join
         * @return this step for further chaining
         */
        JoinStep<T> crossJoinLateral(@NonNull QTableSource name);
    }

    /**
     * Fluent step for specifying the {@code ON} condition of a join.
     *
     * @param <T> the Java type of the query result rows
     */
    interface JoinOnStep<T> {

        /**
         * Specifies the join conditions as a list.
         *
         * @param conditions the join conditions (combined with {@code AND})
         * @return the join step for adding more joins or continuing the query
         */
        JoinStep<T> on(@NonNull List<Condition> conditions);

        /**
         * Specifies a single join condition.
         *
         * @param condition the join condition
         * @return the join step for adding more joins or continuing the query
         */
        default JoinStep<T> on(@NonNull Condition condition) {
            return on(List.of(condition));
        }

        /**
         * Specifies multiple join conditions as varargs.
         *
         * @param conditions the join conditions (combined with {@code AND})
         * @return the join step for adding more joins or continuing the query
         */
        default JoinStep<T> on(@NonNull Condition... conditions) {
            return on(Arrays.asList(conditions));
        }
    }

    /**
     * Fluent step for adding {@code WHERE} conditions.
     *
     * @param <T> the Java type of the query result rows
     */
    interface WhereState<T> extends GroupByStep<T> {

        /**
         * Adds {@code WHERE} conditions as a list. Multiple calls are combined with {@code AND}.
         *
         * @param conditions the filter conditions
         * @return this step for further chaining
         */
        WhereState<T> where(@NonNull List<Condition> conditions);

        /**
         * Adds a single {@code WHERE} condition.
         *
         * @param condition the filter condition
         * @return this step for further chaining
         */
        default WhereState<T> where(@NonNull Condition condition) {
            return where(List.of(condition));
        }

        /**
         * Adds multiple {@code WHERE} conditions as varargs.
         *
         * @param conditions the filter conditions (combined with {@code AND})
         * @return this step for further chaining
         */
        default WhereState<T> where(@NonNull Condition... conditions) {
            return where(Arrays.asList(conditions));
        }

        /**
         * Adds a {@code WHERE} condition from a boolean expression.
         *
         * @param condition the boolean expression to use as a filter condition
         * @return this step for further chaining
         */
        default WhereState<T> where(@NonNull Expression<Boolean> condition) {
            return where(DSL.condition(condition));
        }
    }

    /**
     * Fluent step for specifying the {@code GROUP BY} clause.
     *
     * @param <T> the Java type of the query result rows
     */
    interface GroupByStep<T> extends CombinationStep<T> {

        /**
         * Groups the results by the given expressions.
         *
         * @param expressions the expressions to group by
         * @return the having step for adding {@code HAVING} conditions or continuing the query
         */
        HavingStep<T> groupBy(@NonNull List<Expression<?>> expressions);

        /**
         * Groups the results by a single expression.
         *
         * @param expression the expression to group by
         * @return the having step for adding {@code HAVING} conditions or continuing the query
         */
        default HavingStep<T> groupBy(@NonNull Expression<?> expression) {
            return groupBy(List.of(expression));
        }

        /**
         * Groups the results by the given expressions (varargs).
         *
         * @param expressions the expressions to group by
         * @return the having step for adding {@code HAVING} conditions or continuing the query
         */
        default HavingStep<T> groupBy(@NonNull Expression<?>... expressions) {
            return groupBy(Arrays.asList(expressions));
        }
    }

    /**
     * Fluent step for specifying the {@code HAVING} clause.
     *
     * @param <T> the Java type of the query result rows
     */
    interface HavingStep<T> extends CombinationStep<T> {

        /**
         * Adds {@code HAVING} conditions as a list.
         *
         * @param conditions the having conditions
         * @return the combination step for set operations or continuing the query
         */
        CombinationStep<T> having(@NonNull List<Condition> conditions);

        /**
         * Adds a single {@code HAVING} condition.
         *
         * @param condition the having condition
         * @return the combination step for set operations or continuing the query
         */
        default CombinationStep<T> having(@NonNull Condition condition) {
            return having(List.of(condition));
        }

        /**
         * Adds multiple {@code HAVING} conditions as varargs.
         *
         * @param conditions the having conditions (combined with {@code AND})
         * @return the combination step for set operations or continuing the query
         */
        default CombinationStep<T> having(@NonNull Condition... conditions) {
            return having(Arrays.asList(conditions));
        }
    }

    /**
     * Fluent step for combining queries with set operations
     * ({@code UNION}, {@code INTERSECT}, {@code EXCEPT}).
     *
     * @param <T> the Java type of the query result rows
     */
    interface CombinationStep<T> extends OrderStep<T> {

        /**
         * Combines this query with another using the specified set operation.
         *
         * @param selectExpression the other select expression to combine with
         * @param combination      the set operation type
         * @param all              {@code true} to include duplicates ({@code ALL}), {@code false} to eliminate them
         * @return this step for further chaining
         */
        CombinationStep<T> combine(@NonNull SelectExpression<T> selectExpression, @NonNull SelectCombination combination, boolean all);

        /**
         * Combines this query with another using {@code UNION}.
         *
         * @param selectExpression the other select expression
         * @param all              {@code true} for {@code UNION ALL}, {@code false} for {@code UNION}
         * @return this step for further chaining
         */
        default CombinationStep<T> union(@NonNull SelectExpression<T> selectExpression, boolean all) {
            return combine(selectExpression, SelectCombination.UNION, all);
        }

        /**
         * Combines this query with another using {@code UNION} (eliminates duplicates).
         *
         * @param selectExpression the other select expression
         * @return this step for further chaining
         */
        default CombinationStep<T> union(@NonNull SelectExpression<T> selectExpression) {
            return combine(selectExpression, SelectCombination.UNION, false);
        }

        /**
         * Combines this query with another using {@code UNION ALL} (keeps duplicates).
         *
         * @param selectExpression the other select expression
         * @return this step for further chaining
         */
        default CombinationStep<T> unionAll(@NonNull SelectExpression<T> selectExpression) {
            return combine(selectExpression, SelectCombination.UNION, true);
        }

        /**
         * Combines this query with another using {@code INTERSECT}.
         *
         * @param selectExpression the other select expression
         * @param all              {@code true} for {@code INTERSECT ALL}, {@code false} for {@code INTERSECT}
         * @return this step for further chaining
         */
        default CombinationStep<T> intersect(@NonNull SelectExpression<T> selectExpression, boolean all) {
            return combine(selectExpression, SelectCombination.INTERSECT, all);
        }

        /**
         * Combines this query with another using {@code INTERSECT} (eliminates duplicates).
         *
         * @param selectExpression the other select expression
         * @return this step for further chaining
         */
        default CombinationStep<T> intersect(@NonNull SelectExpression<T> selectExpression) {
            return combine(selectExpression, SelectCombination.INTERSECT, false);
        }

        /**
         * Combines this query with another using {@code INTERSECT ALL} (keeps duplicates).
         *
         * @param selectExpression the other select expression
         * @return this step for further chaining
         */
        default CombinationStep<T> intersectAll(@NonNull SelectExpression<T> selectExpression) {
            return combine(selectExpression, SelectCombination.INTERSECT, true);
        }

        /**
         * Combines this query with another using {@code EXCEPT}.
         *
         * @param selectExpression the other select expression
         * @param all              {@code true} for {@code EXCEPT ALL}, {@code false} for {@code EXCEPT}
         * @return this step for further chaining
         */
        default CombinationStep<T> except(@NonNull SelectExpression<T> selectExpression, boolean all) {
            return combine(selectExpression, SelectCombination.EXCEPT, all);
        }

        /**
         * Combines this query with another using {@code EXCEPT} (eliminates duplicates).
         *
         * @param selectExpression the other select expression
         * @return this step for further chaining
         */
        default CombinationStep<T> except(@NonNull SelectExpression<T> selectExpression) {
            return combine(selectExpression, SelectCombination.EXCEPT, false);
        }

        /**
         * Combines this query with another using {@code EXCEPT ALL} (keeps duplicates).
         *
         * @param selectExpression the other select expression
         * @return this step for further chaining
         */
        default CombinationStep<T> exceptAll(@NonNull SelectExpression<T> selectExpression) {
            return combine(selectExpression, SelectCombination.EXCEPT, true);
        }

        /**
         * Combines this query with a completed fluent query using the specified set operation.
         *
         * @param selectExpression the completed fluent select expression
         * @param combination      the set operation type
         * @param all              {@code true} to include duplicates ({@code ALL}), {@code false} to eliminate them
         * @return this step for further chaining
         */
        default CombinationStep<T> combine(@NonNull SelectExpression.CompleteStep<T> selectExpression, @NonNull SelectCombination combination, boolean all) {
            return combine(selectExpression.asNonFluent(), combination, all);
        }

        /**
         * Combines this query with a completed fluent query using {@code UNION}.
         *
         * @param selectExpression the completed fluent select expression
         * @param all              {@code true} for {@code UNION ALL}, {@code false} for {@code UNION}
         * @return this step for further chaining
         */
        default CombinationStep<T> union(@NonNull SelectExpression.CompleteStep<T> selectExpression, boolean all) {
            return combine(selectExpression.asNonFluent(), SelectCombination.UNION, all);
        }

        /**
         * Combines this query with a completed fluent query using {@code UNION} (eliminates duplicates).
         *
         * @param selectExpression the completed fluent select expression
         * @return this step for further chaining
         */
        default CombinationStep<T> union(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
            return combine(selectExpression.asNonFluent(), SelectCombination.UNION, false);
        }

        /**
         * Combines this query with a completed fluent query using {@code UNION ALL} (keeps duplicates).
         *
         * @param selectExpression the completed fluent select expression
         * @return this step for further chaining
         */
        default CombinationStep<T> unionAll(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
            return combine(selectExpression.asNonFluent(), SelectCombination.UNION, true);
        }

        /**
         * Combines this query with a completed fluent query using {@code INTERSECT}.
         *
         * @param selectExpression the completed fluent select expression
         * @param all              {@code true} for {@code INTERSECT ALL}, {@code false} for {@code INTERSECT}
         * @return this step for further chaining
         */
        default CombinationStep<T> intersect(@NonNull SelectExpression.CompleteStep<T> selectExpression, boolean all) {
            return combine(selectExpression.asNonFluent(), SelectCombination.INTERSECT, all);
        }

        /**
         * Combines this query with a completed fluent query using {@code INTERSECT} (eliminates duplicates).
         *
         * @param selectExpression the completed fluent select expression
         * @return this step for further chaining
         */
        default CombinationStep<T> intersect(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
            return combine(selectExpression.asNonFluent(), SelectCombination.INTERSECT, false);
        }

        /**
         * Combines this query with a completed fluent query using {@code INTERSECT ALL} (keeps duplicates).
         *
         * @param selectExpression the completed fluent select expression
         * @return this step for further chaining
         */
        default CombinationStep<T> intersectAll(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
            return combine(selectExpression.asNonFluent(), SelectCombination.INTERSECT, true);
        }

        /**
         * Combines this query with a completed fluent query using {@code EXCEPT}.
         *
         * @param selectExpression the completed fluent select expression
         * @param all              {@code true} for {@code EXCEPT ALL}, {@code false} for {@code EXCEPT}
         * @return this step for further chaining
         */
        default CombinationStep<T> except(@NonNull SelectExpression.CompleteStep<T> selectExpression, boolean all) {
            return combine(selectExpression.asNonFluent(), SelectCombination.EXCEPT, all);
        }

        /**
         * Combines this query with a completed fluent query using {@code EXCEPT} (eliminates duplicates).
         *
         * @param selectExpression the completed fluent select expression
         * @return this step for further chaining
         */
        default CombinationStep<T> except(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
            return combine(selectExpression.asNonFluent(), SelectCombination.EXCEPT, false);
        }

        /**
         * Combines this query with a completed fluent query using {@code EXCEPT ALL} (keeps duplicates).
         *
         * @param selectExpression the completed fluent select expression
         * @return this step for further chaining
         */
        default CombinationStep<T> exceptAll(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
            return combine(selectExpression.asNonFluent(), SelectCombination.EXCEPT, true);
        }
    }

    /**
     * Fluent step for specifying the {@code ORDER BY} clause.
     *
     * @param <T> the Java type of the query result rows
     */
    interface OrderStep<T> extends LimitStep<T> {

        /**
         * Orders the results by the given ordering specifications.
         *
         * @param orders the ordering specifications
         * @return the limit step for adding {@code LIMIT} or continuing the query
         */
        LimitStep<T> orderBy(@NonNull List<Order> orders);

        /**
         * Orders the results by a single ordering specification.
         *
         * @param order the ordering specification
         * @return the limit step for adding {@code LIMIT} or continuing the query
         */
        default LimitStep<T> orderBy(@NonNull Order order) {
            return orderBy(List.of(order));
        }

        /**
         * Orders the results by the given ordering specifications (varargs).
         *
         * @param orders the ordering specifications
         * @return the limit step for adding {@code LIMIT} or continuing the query
         */
        default LimitStep<T> orderBy(@NonNull Order... orders) {
            return orderBy(Arrays.asList(orders));
        }
    }

    /**
     * Fluent step for specifying the {@code LIMIT} clause.
     *
     * @param <T> the Java type of the query result rows
     */
    interface LimitStep<T> extends OffsetStep<T> {

        /**
         * Limits the number of result rows.
         *
         * @param limit the maximum number of rows to return
         * @return the offset step for adding {@code OFFSET} or continuing the query
         */
        OffsetStep<T> limit(@NonNull Integer limit);
    }

    /**
     * Fluent step for specifying the {@code OFFSET} clause.
     *
     * @param <T> the Java type of the query result rows
     */
    interface OffsetStep<T> extends CompleteStep<T> {

        /**
         * Skips the specified number of rows before returning results.
         *
         * @param offset the number of rows to skip
         * @return the complete step for finalizing the query
         */
        CompleteStep<T> offset(@NonNull Integer offset);
    }

    /**
     * Terminal fluent step representing a complete {@code SELECT} statement
     * that can be executed, locked, or converted to other forms.
     *
     * @param <T> the Java type of the query result rows
     */
    interface CompleteStep<T> extends Expression<T> {

        /**
         * Adds a {@code FOR UPDATE} locking clause to the query.
         *
         * @return this step for further chaining
         */
        CompleteStep<T> forUpdate();

        /**
         * Converts this fluent step-based query into a non-fluent {@link SelectExpression}.
         *
         * @return the equivalent non-fluent select expression
         */
        SelectExpression<T> asNonFluent();

        /**
         * Wraps this query as a derived table source with the given alias,
         * for use in {@code FROM} or {@code JOIN} clauses.
         *
         * @param alias the alias for the derived table
         * @return a table source backed by this subquery
         */
        SelectExpressionTableSource asTableSource(@NonNull String alias);

        /**
         * Returns this step as a plain expression, signaling that the query is complete.
         *
         * @return this step cast as an {@link Expression}
         */
        default Expression<T> complete() {
            return this;
        }
    }

    // -----------------------------------------------------------------------
    // Imperative (non-fluent) API methods
    // -----------------------------------------------------------------------

    /**
     * Sets the common table expressions (CTEs) for this query.
     *
     * @param ctes      the common table expressions
     * @param recursive {@code true} for {@code WITH RECURSIVE}, {@code false} for {@code WITH}
     */
    void with(@NonNull List<CommonTableExpression> ctes, boolean recursive);

    /**
     * Sets the common table expressions (CTEs) for this query (non-recursive).
     *
     * @param ctes the common table expressions
     */
    default void with(@NonNull List<CommonTableExpression> ctes) {
        with(ctes, false);
    }

    /**
     * Sets a single common table expression (CTE) for this query (non-recursive).
     *
     * @param cte the common table expression
     */
    default void with(@NonNull CommonTableExpression cte) {
        with(List.of(cte), false);
    }

    /**
     * Sets the common table expressions (CTEs) for this query (non-recursive, varargs).
     *
     * @param ctes the common table expressions
     */
    default void with(@NonNull CommonTableExpression... ctes) {
        with(Arrays.asList(ctes), false);
    }

    /**
     * Sets the recursive common table expressions for this query.
     *
     * @param ctes the common table expressions
     */
    default void withRecursive(@NonNull List<CommonTableExpression> ctes) {
        with(ctes, true);
    }

    /**
     * Sets a single recursive common table expression for this query.
     *
     * @param cte the common table expression
     */
    default void withRecursive(@NonNull CommonTableExpression cte) {
        with(List.of(cte), true);
    }

    /**
     * Sets the recursive common table expressions for this query (varargs).
     *
     * @param ctes the common table expressions
     */
    default void withRecursive(@NonNull CommonTableExpression... ctes) {
        with(Arrays.asList(ctes), true);
    }

    /**
     * Adds a single expression to the {@code SELECT} list.
     *
     * @param expression the expression to select
     */
    void select(@NonNull Expression<?> expression);

    /**
     * Adds multiple expressions to the {@code SELECT} list.
     *
     * @param expressions the expressions to select
     */
    void select(@NonNull List<? extends Expression<?>> expressions);

    /**
     * Sets the {@code FROM} clause to the given table source.
     *
     * @param tableSource the table source to select from
     */
    void from(@NonNull QTableSource tableSource);

    /**
     * Sets the {@code FROM} clause using a table name.
     *
     * @param tableName the name of the table
     */
    default void from(@NonNull String tableName) {
        from(new QTableName(tableName, null, null));
    }

    /**
     * Sets the {@code FROM} clause using a table name and schema.
     *
     * @param tableName the name of the table
     * @param schema    the schema containing the table, or {@code null} for the default schema
     */
    default void from(@NonNull String tableName, String schema) {
        from(new QTableName(tableName, schema, null));
    }

    /**
     * Sets the {@code FROM} clause using a generated table metadata object.
     *
     * @param qTable the table metadata
     */
    default void from(@NonNull QTable qTable) {
        from(qTable.getTableName());
    }

    /**
     * Adds a join clause to this query.
     *
     * @param join the join specification
     */
    void join(@NonNull Join join);

    /**
     * Adds an {@code INNER JOIN} with the given table source and condition.
     *
     * @param table       the table source to join
     * @param onCondition the join condition
     */
    default void join(@NonNull QTableSource table, @NonNull Condition onCondition) {
        join(new Join(table, Join.Type.INNER, onCondition, false));
    }

    /**
     * Adds a {@code LEFT JOIN} with the given table source and condition.
     *
     * @param table       the table source to join
     * @param onCondition the join condition
     */
    default void leftJoin(@NonNull QTableSource table, @NonNull Condition onCondition) {
        join(new Join(table, Join.Type.LEFT, onCondition, false));
    }

    /**
     * Adds a {@code RIGHT JOIN} with the given table source and condition.
     *
     * @param table       the table source to join
     * @param onCondition the join condition
     */
    default void rightJoin(@NonNull QTableSource table, @NonNull Condition onCondition) {
        join(new Join(table, Join.Type.RIGHT, onCondition, false));
    }

    /**
     * Adds a {@code CROSS JOIN} with the given table source.
     *
     * @param table the table source to cross join
     */
    default void crossJoin(@NonNull QTableSource table) {
        join(new Join(table, Join.Type.CROSS, null, false));
    }

    /**
     * Adds an {@code INNER JOIN LATERAL} with the given table source and condition.
     *
     * @param table       the table source to laterally join
     * @param onCondition the join condition
     */
    default void innerJoinLateral(@NonNull QTableSource table, @NonNull Condition onCondition) {
        join(new Join(table, Join.Type.INNER, onCondition, true));
    }

    /**
     * Adds a {@code LEFT JOIN LATERAL} with the given table source and condition.
     *
     * @param table       the table source to laterally join
     * @param onCondition the join condition
     */
    default void leftJoinLateral(@NonNull QTableSource table, @NonNull Condition onCondition) {
        join(new Join(table, Join.Type.LEFT, onCondition, true));
    }

    /**
     * Adds a {@code CROSS JOIN LATERAL} with the given table source.
     *
     * @param table the table source to laterally cross join
     */
    default void crossJoinLateral(@NonNull QTableSource table) {
        join(new Join(table, Join.Type.CROSS, null, true));
    }

    /**
     * Sets the {@code WHERE} conditions as a list. Multiple calls are combined with {@code AND}.
     *
     * @param conditions the filter conditions
     */
    void where(@NonNull List<Condition> conditions);

    /**
     * Sets a single {@code WHERE} condition.
     *
     * @param condition the filter condition
     */
    default void where(@NonNull Condition condition) {
        where(List.of(condition));
    }

    /**
     * Sets the {@code WHERE} conditions as varargs.
     *
     * @param conditions the filter conditions (combined with {@code AND})
     */
    default void where(@NonNull Condition... conditions) {
        where(Arrays.asList(conditions));
    }

    /**
     * Sets the {@code WHERE} condition from a boolean expression.
     *
     * @param condition the boolean expression to use as a filter condition
     */
    default void where(@NonNull Expression<Boolean> condition) {
        where(DSL.condition(condition));
    }

    /**
     * Sets the {@code GROUP BY} clause to the given expressions.
     *
     * @param expressions the expressions to group by
     */
    void groupBy(@NonNull List<Expression<?>> expressions);

    /**
     * Sets the {@code GROUP BY} clause to a single expression.
     *
     * @param expression the expression to group by
     */
    default void groupBy(@NonNull Expression<?> expression) {
        groupBy(List.of(expression));
    }

    /**
     * Sets the {@code GROUP BY} clause to the given expressions (varargs).
     *
     * @param expressions the expressions to group by
     */
    default void groupBy(@NonNull Expression<?>... expressions) {
        groupBy(Arrays.asList(expressions));
    }

    /**
     * Sets the {@code HAVING} conditions as a list.
     *
     * @param conditions the having conditions
     */
    void having(@NonNull List<Condition> conditions);

    /**
     * Sets a single {@code HAVING} condition.
     *
     * @param condition the having condition
     */
    default void having(@NonNull Condition condition) {
        having(List.of(condition));
    }

    /**
     * Sets the {@code HAVING} conditions as varargs.
     *
     * @param conditions the having conditions (combined with {@code AND})
     */
    default void having(@NonNull Condition... conditions) {
        having(Arrays.asList(conditions));
    }

    /**
     * Combines this query with another using the specified set operation.
     *
     * @param selectExpression the other select expression to combine with
     * @param combination      the set operation type
     * @param all              {@code true} to include duplicates ({@code ALL}), {@code false} to eliminate them
     */
    void combine(@NonNull SelectExpression<T> selectExpression, @NonNull SelectCombination combination, boolean all);

    /**
     * Combines this query with another using {@code UNION}.
     *
     * @param selectExpression the other select expression
     * @param all              {@code true} for {@code UNION ALL}, {@code false} for {@code UNION}
     */
    default void union(@NonNull SelectExpression<T> selectExpression, boolean all) {
        combine(selectExpression, SelectCombination.UNION, all);
    }

    /**
     * Combines this query with another using {@code UNION} (eliminates duplicates).
     *
     * @param selectExpression the other select expression
     */
    default void union(@NonNull SelectExpression<T> selectExpression) {
        combine(selectExpression, SelectCombination.UNION, false);
    }

    /**
     * Combines this query with another using {@code UNION ALL} (keeps duplicates).
     *
     * @param selectExpression the other select expression
     */
    default void unionAll(@NonNull SelectExpression<T> selectExpression) {
        combine(selectExpression, SelectCombination.UNION, true);
    }

    /**
     * Combines this query with another using {@code INTERSECT}.
     *
     * @param selectExpression the other select expression
     * @param all              {@code true} for {@code INTERSECT ALL}, {@code false} for {@code INTERSECT}
     */
    default void intersect(@NonNull SelectExpression<T> selectExpression, boolean all) {
        combine(selectExpression, SelectCombination.INTERSECT, all);
    }

    /**
     * Combines this query with another using {@code INTERSECT} (eliminates duplicates).
     *
     * @param selectExpression the other select expression
     */
    default void intersect(@NonNull SelectExpression<T> selectExpression) {
        combine(selectExpression, SelectCombination.INTERSECT, false);
    }

    /**
     * Combines this query with another using {@code INTERSECT ALL} (keeps duplicates).
     *
     * @param selectExpression the other select expression
     */
    default void intersectAll(@NonNull SelectExpression<T> selectExpression) {
        combine(selectExpression, SelectCombination.INTERSECT, true);
    }

    /**
     * Combines this query with another using {@code EXCEPT}.
     *
     * @param selectExpression the other select expression
     * @param all              {@code true} for {@code EXCEPT ALL}, {@code false} for {@code EXCEPT}
     */
    default void except(@NonNull SelectExpression<T> selectExpression, boolean all) {
        combine(selectExpression, SelectCombination.EXCEPT, all);
    }

    /**
     * Combines this query with another using {@code EXCEPT} (eliminates duplicates).
     *
     * @param selectExpression the other select expression
     */
    default void except(@NonNull SelectExpression<T> selectExpression) {
        combine(selectExpression, SelectCombination.EXCEPT, false);
    }

    /**
     * Combines this query with another using {@code EXCEPT ALL} (keeps duplicates).
     *
     * @param selectExpression the other select expression
     */
    default void exceptAll(@NonNull SelectExpression<T> selectExpression) {
        combine(selectExpression, SelectCombination.EXCEPT, true);
    }

    /**
     * Combines this query with a completed fluent query using the specified set operation.
     *
     * @param selectExpression the completed fluent select expression
     * @param combination      the set operation type
     * @param all              {@code true} to include duplicates ({@code ALL}), {@code false} to eliminate them
     */
    default void combine(@NonNull SelectExpression.CompleteStep<T> selectExpression, @NonNull SelectCombination combination, boolean all) {
        combine(selectExpression.asNonFluent(), combination, all);
    }

    /**
     * Combines this query with a completed fluent query using {@code UNION}.
     *
     * @param selectExpression the completed fluent select expression
     * @param all              {@code true} for {@code UNION ALL}, {@code false} for {@code UNION}
     */
    default void union(@NonNull SelectExpression.CompleteStep<T> selectExpression, boolean all) {
        combine(selectExpression.asNonFluent(), SelectCombination.UNION, all);
    }

    /**
     * Combines this query with a completed fluent query using {@code UNION} (eliminates duplicates).
     *
     * @param selectExpression the completed fluent select expression
     */
    default void union(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
        combine(selectExpression.asNonFluent(), SelectCombination.UNION, false);
    }

    /**
     * Combines this query with a completed fluent query using {@code UNION ALL} (keeps duplicates).
     *
     * @param selectExpression the completed fluent select expression
     */
    default void unionAll(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
        combine(selectExpression.asNonFluent(), SelectCombination.UNION, true);
    }

    /**
     * Combines this query with a completed fluent query using {@code INTERSECT}.
     *
     * @param selectExpression the completed fluent select expression
     * @param all              {@code true} for {@code INTERSECT ALL}, {@code false} for {@code INTERSECT}
     */
    default void intersect(@NonNull SelectExpression.CompleteStep<T> selectExpression, boolean all) {
        combine(selectExpression.asNonFluent(), SelectCombination.INTERSECT, all);
    }

    /**
     * Combines this query with a completed fluent query using {@code INTERSECT} (eliminates duplicates).
     *
     * @param selectExpression the completed fluent select expression
     */
    default void intersect(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
        combine(selectExpression.asNonFluent(), SelectCombination.INTERSECT, false);
    }

    /**
     * Combines this query with a completed fluent query using {@code INTERSECT ALL} (keeps duplicates).
     *
     * @param selectExpression the completed fluent select expression
     */
    default void intersectAll(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
        combine(selectExpression.asNonFluent(), SelectCombination.INTERSECT, true);
    }

    /**
     * Combines this query with a completed fluent query using {@code EXCEPT}.
     *
     * @param selectExpression the completed fluent select expression
     * @param all              {@code true} for {@code EXCEPT ALL}, {@code false} for {@code EXCEPT}
     */
    default void except(@NonNull SelectExpression.CompleteStep<T> selectExpression, boolean all) {
        combine(selectExpression.asNonFluent(), SelectCombination.EXCEPT, all);
    }

    /**
     * Combines this query with a completed fluent query using {@code EXCEPT} (eliminates duplicates).
     *
     * @param selectExpression the completed fluent select expression
     */
    default void except(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
        combine(selectExpression.asNonFluent(), SelectCombination.EXCEPT, false);
    }

    /**
     * Combines this query with a completed fluent query using {@code EXCEPT ALL} (keeps duplicates).
     *
     * @param selectExpression the completed fluent select expression
     */
    default void exceptAll(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
        combine(selectExpression.asNonFluent(), SelectCombination.EXCEPT, true);
    }

    /**
     * Sets the {@code ORDER BY} clause to the given ordering specifications.
     *
     * @param orders the ordering specifications
     */
    void orderBy(@NonNull List<Order> orders);

    /**
     * Sets the {@code ORDER BY} clause to a single ordering specification.
     *
     * @param order the ordering specification
     */
    default void orderBy(@NonNull Order order) {
        orderBy(List.of(order));
    }

    /**
     * Sets the {@code ORDER BY} clause to the given ordering specifications (varargs).
     *
     * @param orders the ordering specifications
     */
    default void orderBy(@NonNull Order... orders) {
        orderBy(Arrays.asList(orders));
    }

    /**
     * Sets the maximum number of rows to return.
     *
     * @param limit the maximum number of rows
     */
    void limit(@NonNull Integer limit);

    /**
     * Sets the number of rows to skip before returning results.
     *
     * @param offset the number of rows to skip
     */
    void offset(@NonNull Integer offset);

    /**
     * Adds a {@code FOR UPDATE} locking clause to the query.
     */
    void forUpdate();
}
