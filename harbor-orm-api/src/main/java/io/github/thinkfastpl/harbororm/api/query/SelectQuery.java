// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.query;

import io.github.thinkfastpl.harbororm.api.expression.*;
import io.github.thinkfastpl.harbororm.api.metadata.QTable;
import io.github.thinkfastpl.harbororm.api.metadata.QTableName;
import io.github.thinkfastpl.harbororm.api.metadata.QTableSource;
import io.github.thinkfastpl.harbororm.api.query.result.Page;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Fluent builder for SQL SELECT statements.
 *
 * <p>This is the most feature-rich query interface in HarborORM, supporting the full range of
 * SELECT clauses: WITH (CTE), SELECT, FROM, JOIN, WHERE, GROUP BY, HAVING, set combinations
 * (UNION, INTERSECT, EXCEPT), ORDER BY, LIMIT, OFFSET, FOR UPDATE, and pagination.
 *
 * <p>The builder follows a step-by-step progression through inner interfaces, each representing
 * a stage in the SQL SELECT statement. The type parameter {@code T} represents the result row
 * type -- either {@link io.github.thinkfastpl.harbororm.api.query.result.Record} for multi-column queries or a
 * specific Java type for single-column queries.
 *
 * <h2>Usage examples</h2>
 *
 * <p>Multi-column select:
 * <pre>{@code
 * List<Record> records = session.select(TABLE.id, TABLE.name)
 *     .from(TABLE)
 *     .where(TABLE.name.eq("Alice"))
 *     .orderBy(TABLE.name.asc())
 *     .fetchAll();
 * }</pre>
 *
 * <p>Join with grouping:
 * <pre>{@code
 * List<Record> records = session.select(authors.name, DSL.count())
 *     .from(authors)
 *     .join(books).on(books.authorId.eq(authors.id))
 *     .groupBy(authors.name)
 *     .having(DSL.count().gt(5))
 *     .fetchAll();
 * }</pre>
 *
 * <p>Pagination:
 * <pre>{@code
 * Page<Record> page = session.select(TABLE.id, TABLE.name)
 *     .from(TABLE)
 *     .orderBy(TABLE.id.asc())
 *     .fetchPage(20, 0);
 * }</pre>
 *
 * @param <T> the result row type
 * @see HarborSession#select(java.util.List)
 * @see HarborSession#selectDistinct(java.util.List)
 * @see HarborSession#with(java.util.List, boolean)
 */
public interface SelectQuery<T> {

    /**
     * Step for defining Common Table Expressions (CTEs) before the SELECT clause.
     *
     * <p>CTEs are named temporary result sets that exist only for the duration of the query.
     * This step also extends {@link SelectStep}, so you can proceed directly to the SELECT
     * clause without adding more CTEs.
     *
     * @param <T> the result row type
     * @see io.github.thinkfastpl.harbororm.api.expression.CommonTableExpression
     */
    interface WithStep<T> extends SelectStep<T> {

        /**
         * Adds one or more CTEs to the query, optionally making them recursive.
         *
         * @param ctes the list of Common Table Expressions
         * @param recursive whether to use {@code WITH RECURSIVE} syntax
         * @return this step for adding more CTEs or proceeding to SELECT
         */
        WithStep<T> with(@NonNull List<CommonTableExpression> ctes, boolean recursive);

        /**
         * Adds non-recursive CTEs. Convenience overload that delegates to
         * {@code with(ctes, false)}.
         *
         * @param ctes the list of CTEs
         * @return this step for adding more CTEs or proceeding to SELECT
         */
        default WithStep<T> with(@NonNull List<CommonTableExpression> ctes) {
            return with(ctes, false);
        }

        /**
         * Adds a single non-recursive CTE. Convenience overload that delegates to
         * {@code with(List.of(cte), false)}.
         *
         * @param cte the CTE to add
         * @return this step for adding more CTEs or proceeding to SELECT
         */
        default WithStep<T> with(@NonNull CommonTableExpression cte) {
            return with(List.of(cte), false);
        }

        /**
         * Adds non-recursive CTEs. Convenience varargs overload that delegates to
         * {@code with(Arrays.asList(ctes), false)}.
         *
         * @param ctes the CTEs to add
         * @return this step for adding more CTEs or proceeding to SELECT
         */
        default WithStep<T> with(@NonNull CommonTableExpression... ctes) {
            return with(Arrays.asList(ctes), false);
        }

        /**
         * Adds recursive CTEs. Convenience overload that delegates to
         * {@code with(ctes, true)}.
         *
         * @param ctes the list of recursive CTEs
         * @return this step for adding more CTEs or proceeding to SELECT
         */
        default WithStep<T> withRecursive(@NonNull List<CommonTableExpression> ctes) {
            return with(ctes, true);
        }

        /**
         * Adds a single recursive CTE. Convenience overload that delegates to
         * {@code with(List.of(cte), true)}.
         *
         * @param cte the recursive CTE to add
         * @return this step for adding more CTEs or proceeding to SELECT
         */
        default WithStep<T> withRecursive(@NonNull CommonTableExpression cte) {
            return with(List.of(cte), true);
        }

        /**
         * Adds recursive CTEs. Convenience varargs overload that delegates to
         * {@code with(Arrays.asList(ctes), true)}.
         *
         * @param ctes the recursive CTEs to add
         * @return this step for adding more CTEs or proceeding to SELECT
         */
        default WithStep<T> withRecursive(@NonNull CommonTableExpression... ctes) {
            return with(Arrays.asList(ctes), true);
        }
    }

    /**
     * Step for adding more column expressions to the SELECT clause.
     *
     * <p>This step also extends {@link FromStep}, so you can proceed directly to the
     * FROM clause or even fetch results without a FROM clause (e.g., {@code SELECT 1}).
     *
     * @param <T> the result row type
     */
    interface SelectStep<T> extends FromStep<T> {

        /**
         * Adds additional column expressions to the SELECT clause.
         *
         * @param expressions the expressions to add
         * @return this step for adding more columns or proceeding to FROM
         */
        SelectStep<T> select(@NonNull List<? extends Expression<?>> expressions);

        /**
         * Adds a single column expression to the SELECT clause. Convenience overload that
         * delegates to {@code select(List.of(expression))}.
         *
         * @param expression the expression to add
         * @return this step for adding more columns or proceeding to FROM
         */
        default SelectStep<T> select(@NonNull Expression<?> expression) {
            return select(List.of(expression));
        }

        /**
         * Adds column expressions to the SELECT clause. Convenience varargs overload that
         * delegates to {@code select(Arrays.asList(expressions))}.
         *
         * @param expressions the expressions to add
         * @return this step for adding more columns or proceeding to FROM
         */
        default SelectStep<T> select(@NonNull Expression<?>... expressions) {
            return select(Arrays.asList(expressions));
        }
    }

    /**
     * Step for specifying the FROM clause of the SELECT statement.
     *
     * <p>This step also extends {@link FetchStep}, allowing queries without a FROM clause
     * (e.g., {@code SELECT 1+1} or {@code SELECT nextval('my_seq')}).
     *
     * @param <T> the result row type
     */
    interface FromStep<T> extends FetchStep<T> {

        /**
         * Specifies the primary table source for the FROM clause.
         *
         * @param tableSource the table source (table name, subquery, CTE reference, or function call)
         * @return the JOIN step for adding joins or proceeding to WHERE
         */
        JoinStep<T> from(@NonNull QTableSource tableSource);

        /**
         * Specifies the FROM table by name. Convenience overload that delegates to
         * {@code from(new QTableName(tableName, null, null))}.
         *
         * @param tableName the table name
         * @return the JOIN step
         */
        default JoinStep<T> from(@NonNull String tableName) {
            return from(new QTableName(tableName, null, null));
        }

        /**
         * Specifies the FROM table by name and schema. Convenience overload that delegates to
         * {@code from(new QTableName(tableName, schema, null))}.
         *
         * @param tableName the table name
         * @param schema the database schema, or {@code null} for the default schema
         * @return the JOIN step
         */
        default JoinStep<T> from(@NonNull String tableName, String schema) {
            return from(new QTableName(tableName, schema, null));
        }

        /**
         * Specifies the FROM table using table metadata. Convenience overload that delegates to
         * {@code from(qTable.getTableName())}.
         *
         * @param qTable the table metadata
         * @return the JOIN step
         */
        default JoinStep<T> from(QTable qTable) {
            return from(qTable.getTableName());
        }
    }

    /**
     * Step for adding JOIN clauses to the SELECT statement.
     *
     * <p>Supports INNER JOIN, LEFT JOIN, RIGHT JOIN, FULL OUTER JOIN, CROSS JOIN, and
     * LATERAL join variants. Each join (except CROSS JOIN) requires an ON condition
     * specified via the returned {@link JoinOnStep}.
     *
     * <p>This step also extends {@link WhereState}, so you can proceed directly to
     * WHERE, GROUP BY, ORDER BY, or FETCH if no joins are needed.
     *
     * @param <T> the result row type
     */
    interface JoinStep<T> extends WhereState<T> {

        /**
         * Adds an INNER JOIN clause. Rows are included only when the ON condition is met
         * in both tables.
         *
         * @param tableSource the table to join
         * @return the ON step for specifying the join condition
         */
        JoinOnStep<T> join(@NonNull QTableSource tableSource);

        /**
         * Adds an INNER JOIN by table name. Convenience overload that delegates to
         * {@code join(new QTableName(tableName, null, null))}.
         *
         * @param tableName the table name
         * @return the ON step
         */
        default JoinOnStep<T> join(@NonNull String tableName) {
            return join(new QTableName(tableName, null, null));
        }

        /**
         * Adds an INNER JOIN by table name and schema. Convenience overload that delegates to
         * {@code join(new QTableName(tableName, schema, null))}.
         *
         * @param tableName the table name
         * @param schema the database schema
         * @return the ON step
         */
        default JoinOnStep<T> join(@NonNull String tableName, String schema) {
            return join(new QTableName(tableName, schema, null));
        }

        /**
         * Adds an INNER JOIN using table metadata. Convenience overload that delegates to
         * {@code join(qTable.getTableName())}.
         *
         * @param qTable the table metadata
         * @return the ON step
         */
        default JoinOnStep<T> join(@NonNull QTable qTable) {
            return join(qTable.getTableName());
        }

        /**
         * Alias for {@link #join(QTableSource)}. Adds an explicit INNER JOIN clause.
         *
         * @param tableSource the table to join
         * @return the ON step
         */
        default JoinOnStep<T> innerJoin(@NonNull QTableSource tableSource) {
            return join(tableSource);
        }

        /**
         * Alias for {@link #join(String)}. Convenience overload for INNER JOIN by table name.
         *
         * @param tableName the table name
         * @return the ON step
         */
        default JoinOnStep<T> innerJoin(@NonNull String tableName) {
            return innerJoin(new QTableName(tableName, null, null));
        }

        /**
         * Alias for {@link #join(String, String)}. Convenience overload for INNER JOIN
         * by table name and schema.
         *
         * @param tableName the table name
         * @param schema the database schema
         * @return the ON step
         */
        default JoinOnStep<T> innerJoin(@NonNull String tableName, String schema) {
            return innerJoin(new QTableName(tableName, schema, null));
        }

        /**
         * Alias for {@link #join(QTable)}. Convenience overload for INNER JOIN using
         * table metadata.
         *
         * @param qTable the table metadata
         * @return the ON step
         */
        default JoinOnStep<T> innerJoin(@NonNull QTable qTable) {
            return innerJoin(qTable.getTableName());
        }

        /**
         * Adds a LEFT (OUTER) JOIN clause. All rows from the left table are included;
         * unmatched rows from the right table produce NULLs.
         *
         * @param tableSource the table to join
         * @return the ON step for specifying the join condition
         */
        JoinOnStep<T> leftJoin(@NonNull QTableSource tableSource);

        /**
         * Adds a LEFT JOIN by table name. Convenience overload.
         *
         * @param tableName the table name
         * @return the ON step
         */
        default JoinOnStep<T> leftJoin(@NonNull String tableName) {
            return leftJoin(new QTableName(tableName, null, null));
        }

        /**
         * Adds a LEFT JOIN by table name and schema. Convenience overload.
         *
         * @param tableName the table name
         * @param schema the database schema
         * @return the ON step
         */
        default JoinOnStep<T> leftJoin(@NonNull String tableName, String schema) {
            return leftJoin(new QTableName(tableName, schema, null));
        }

        /**
         * Adds a LEFT JOIN using table metadata. Convenience overload.
         *
         * @param qTable the table metadata
         * @return the ON step
         */
        default JoinOnStep<T> leftJoin(@NonNull QTable qTable) {
            return leftJoin(qTable.getTableName());
        }

        /**
         * Adds a RIGHT (OUTER) JOIN clause. All rows from the right table are included;
         * unmatched rows from the left table produce NULLs.
         *
         * @param tableSource the table to join
         * @return the ON step for specifying the join condition
         */
        JoinOnStep<T> rightJoin(@NonNull QTableSource tableSource);

        /**
         * Adds a RIGHT JOIN by table name. Convenience overload.
         *
         * @param tableName the table name
         * @return the ON step
         */
        default JoinOnStep<T> rightJoin(@NonNull String tableName) {
            return rightJoin(new QTableName(tableName, null, null));
        }

        /**
         * Adds a RIGHT JOIN by table name and schema. Convenience overload.
         *
         * @param tableName the table name
         * @param schema the database schema
         * @return the ON step
         */
        default JoinOnStep<T> rightJoin(@NonNull String tableName, String schema) {
            return rightJoin(new QTableName(tableName, schema, null));
        }

        /**
         * Adds a RIGHT JOIN using table metadata. Convenience overload.
         *
         * @param qTable the table metadata
         * @return the ON step
         */
        default JoinOnStep<T> rightJoin(@NonNull QTable qTable) {
            return rightJoin(qTable.getTableName());
        }

        /**
         * Adds a FULL OUTER JOIN clause. All rows from both tables are included;
         * unmatched rows produce NULLs on the opposite side.
         *
         * <p><b>Note:</b> Not supported on H2. Use PostgreSQL for FULL OUTER JOIN queries.
         *
         * @param tableSource the table to join
         * @return the ON step for specifying the join condition
         */
        JoinOnStep<T> fullOuterJoin(@NonNull QTableSource tableSource);

        /**
         * Adds a FULL OUTER JOIN by table name. Convenience overload.
         *
         * @param tableName the table name
         * @return the ON step
         */
        default JoinOnStep<T> fullOuterJoin(@NonNull String tableName) {
            return fullOuterJoin(new QTableName(tableName, null, null));
        }

        /**
         * Adds a FULL OUTER JOIN by table name and schema. Convenience overload.
         *
         * @param tableName the table name
         * @param schema the database schema
         * @return the ON step
         */
        default JoinOnStep<T> fullOuterJoin(@NonNull String tableName, String schema) {
            return fullOuterJoin(new QTableName(tableName, schema, null));
        }

        /**
         * Adds a FULL OUTER JOIN using table metadata. Convenience overload.
         *
         * @param qTable the table metadata
         * @return the ON step
         */
        default JoinOnStep<T> fullOuterJoin(@NonNull QTable qTable) {
            return fullOuterJoin(qTable.getTableName());
        }

        /**
         * Adds a CROSS JOIN clause. Produces the Cartesian product of both tables
         * (no ON condition required).
         *
         * @param tableSource the table to cross join
         * @return the JOIN step for adding more joins or proceeding to WHERE
         */
        JoinStep<T> crossJoin(@NonNull QTableSource tableSource);

        /**
         * Adds a CROSS JOIN by table name. Convenience overload.
         *
         * @param tableName the table name
         * @return the JOIN step
         */
        default JoinStep<T> crossJoin(@NonNull String tableName) {
            return crossJoin(new QTableName(tableName, null, null));
        }

        /**
         * Adds a CROSS JOIN by table name and schema. Convenience overload.
         *
         * @param tableName the table name
         * @param schema the database schema
         * @return the JOIN step
         */
        default JoinStep<T> crossJoin(@NonNull String tableName, String schema) {
            return crossJoin(new QTableName(tableName, schema, null));
        }

        /**
         * Adds a CROSS JOIN using table metadata. Convenience overload.
         *
         * @param qTable the table metadata
         * @return the JOIN step
         */
        default JoinStep<T> crossJoin(@NonNull QTable qTable) {
            return crossJoin(qTable.getTableName());
        }

        /**
         * Adds an INNER JOIN LATERAL clause. The lateral subquery can reference columns
         * from preceding tables in the FROM clause.
         *
         * <p><b>PostgreSQL only.</b> H2 does not support LATERAL joins.
         *
         * @param tableSource the lateral subquery table source
         * @return the ON step for specifying the join condition
         */
        JoinOnStep<T> innerJoinLateral(@NonNull QTableSource tableSource);

        /**
         * Adds a LEFT JOIN LATERAL clause. Like LEFT JOIN, all rows from the left table
         * are included; the lateral subquery can reference columns from preceding tables.
         *
         * <p><b>PostgreSQL only.</b> H2 does not support LATERAL joins.
         *
         * @param tableSource the lateral subquery table source
         * @return the ON step for specifying the join condition
         */
        JoinOnStep<T> leftJoinLateral(@NonNull QTableSource tableSource);

        /**
         * Adds a CROSS JOIN LATERAL clause. Produces rows by evaluating the lateral subquery
         * for each row of the preceding table. No ON condition is required.
         *
         * <p><b>PostgreSQL only.</b> H2 does not support LATERAL joins.
         *
         * @param tableSource the lateral subquery table source
         * @return the JOIN step for adding more joins or proceeding to WHERE
         */
        JoinStep<T> crossJoinLateral(@NonNull QTableSource tableSource);
    }

    /**
     * Step for specifying the ON condition of a JOIN clause.
     *
     * @param <T> the result row type
     */
    interface JoinOnStep<T> {

        /**
         * Specifies the join condition.
         *
         * @param conditions the join condition
         * @return the JOIN step for adding more joins or proceeding to WHERE
         */
        JoinStep<T> on(@NonNull Condition conditions);

        /**
         * Specifies the join condition as multiple conditions combined with AND.
         * Convenience overload that delegates to {@code on(DSL.and(conditions))}.
         *
         * @param conditions the conditions to AND together
         * @return the JOIN step
         */
        default JoinStep<T> on(@NonNull List<Condition> conditions) {
            return on(DSL.and(conditions));
        }

        /**
         * Specifies the join condition as multiple conditions combined with AND.
         * Convenience varargs overload that delegates to {@code on(DSL.and(Arrays.asList(conditions)))}.
         *
         * @param conditions the conditions to AND together
         * @return the JOIN step
         */
        default JoinStep<T> on(@NonNull Condition... conditions) {
            return on(DSL.and(Arrays.asList(conditions)));
        }
    }

    /**
     * Step for adding WHERE conditions to filter rows.
     *
     * <p>Multiple calls to {@code where()} are combined with AND. This step also extends
     * {@link GroupByStep}, so you can skip WHERE and proceed directly to GROUP BY, ORDER BY,
     * or FETCH.
     *
     * @param <T> the result row type
     */
    interface WhereState<T> extends GroupByStep<T> {

        /**
         * Adds WHERE conditions combined with AND.
         *
         * @param conditions the filter conditions
         * @return this step for adding more conditions or proceeding to GROUP BY
         */
        WhereState<T> where(@NonNull List<Condition> conditions);

        /**
         * Adds a single WHERE condition. Convenience overload that delegates to
         * {@code where(List.of(condition))}.
         *
         * @param condition the filter condition
         * @return this step for adding more conditions
         */
        default WhereState<T> where(@NonNull Condition condition) {
            return where(List.of(condition));
        }

        /**
         * Adds WHERE conditions. Convenience varargs overload that delegates to
         * {@code where(Arrays.asList(conditions))}.
         *
         * @param conditions the filter conditions
         * @return this step for adding more conditions
         */
        default WhereState<T> where(@NonNull Condition... conditions) {
            return where(Arrays.asList(conditions));
        }

        /**
         * Adds a WHERE condition from a boolean expression. Convenience overload that wraps
         * the expression via {@link DSL#condition(Expression)}.
         *
         * @param condition the boolean expression to use as a filter
         * @return this step for adding more conditions
         */
        default WhereState<T> where(@NonNull Expression<Boolean> condition) {
            return where(DSL.condition(condition));
        }
    }

    /**
     * Step for adding a GROUP BY clause to aggregate rows.
     *
     * <p>This step also extends {@link CombinationStep}, so you can skip GROUP BY and
     * proceed to UNION/INTERSECT/EXCEPT, ORDER BY, or FETCH.
     *
     * @param <T> the result row type
     */
    interface GroupByStep<T> extends CombinationStep<T> {

        /**
         * Groups the result set by the specified expressions.
         *
         * @param expressions the grouping expressions
         * @return the HAVING step for filtering grouped rows
         */
        HavingStep<T> groupBy(@NonNull List<Expression<?>> expressions);

        /**
         * Groups the result set by a single expression. Convenience overload that delegates to
         * {@code groupBy(List.of(expression))}.
         *
         * @param expression the grouping expression
         * @return the HAVING step
         */
        default HavingStep<T> groupBy(@NonNull Expression<?> expression) {
            return groupBy(List.of(expression));
        }

        /**
         * Groups the result set by the specified expressions. Convenience varargs overload
         * that delegates to {@code groupBy(Arrays.asList(expressions))}.
         *
         * @param expressions the grouping expressions
         * @return the HAVING step
         */
        default HavingStep<T> groupBy(@NonNull Expression<?>... expressions) {
            return groupBy(Arrays.asList(expressions));
        }
    }

    /**
     * Step for adding a HAVING clause to filter grouped rows.
     *
     * <p>HAVING conditions are applied after GROUP BY and can reference aggregate functions.
     * This step also extends {@link CombinationStep}, so you can skip HAVING and proceed
     * to UNION/INTERSECT/EXCEPT, ORDER BY, or FETCH.
     *
     * @param <T> the result row type
     */
    interface HavingStep<T> extends CombinationStep<T> {

        /**
         * Adds HAVING conditions to filter grouped rows.
         *
         * @param conditions the filter conditions for grouped data
         * @return the combination step for UNION/INTERSECT/EXCEPT, ORDER BY, or FETCH
         */
        CombinationStep<T> having(@NonNull List<Condition> conditions);

        /**
         * Adds a single HAVING condition. Convenience overload that delegates to
         * {@code having(List.of(condition))}.
         *
         * @param condition the filter condition
         * @return the combination step
         */
        default CombinationStep<T> having(@NonNull Condition condition) {
            return having(List.of(condition));
        }

        /**
         * Adds HAVING conditions. Convenience varargs overload that delegates to
         * {@code having(Arrays.asList(conditions))}.
         *
         * @param conditions the filter conditions
         * @return the combination step
         */
        default CombinationStep<T> having(@NonNull Condition... conditions) {
            return having(Arrays.asList(conditions));
        }
    }

    /**
     * Step for combining query results using set operations: UNION, INTERSECT, or EXCEPT.
     *
     * <p>Each combination method accepts either a {@link SelectExpression} (non-fluent) or a
     * {@link SelectExpression.CompleteStep} (fluent) as the right-hand side of the operation.
     * The {@code all} parameter controls whether duplicate rows are retained.
     *
     * <p>This step also extends {@link OrderStep}, so you can skip combinations and proceed
     * directly to ORDER BY, LIMIT, or FETCH.
     *
     * <p><b>Note:</b> {@code INTERSECT ALL} and {@code EXCEPT ALL} are not supported on H2.
     *
     * @param <T> the result row type
     */
    interface CombinationStep<T> extends OrderStep<T> {

        /**
         * Combines this query's results with another query using the specified set operation.
         *
         * @param selectExpression the right-hand query (non-fluent)
         * @param combination the set operation type (UNION, INTERSECT, or EXCEPT)
         * @param all whether to retain duplicates ({@code true} for ALL variant)
         * @return this step for adding more combinations or proceeding to ORDER BY
         */
        CombinationStep<T> combine(@NonNull SelectExpression<T> selectExpression, @NonNull SelectCombination combination, boolean all);

        /**
         * Combines with UNION. Delegates to {@code combine(selectExpression, UNION, all)}.
         *
         * @param selectExpression the right-hand query
         * @param all whether to retain duplicates
         * @return this step for further combinations
         */
        default CombinationStep<T> union(@NonNull SelectExpression<T> selectExpression, boolean all) {
            return combine(selectExpression, SelectCombination.UNION, all);
        }

        /**
         * Combines with UNION (removing duplicates). Delegates to
         * {@code combine(selectExpression, UNION, false)}.
         *
         * @param selectExpression the right-hand query
         * @return this step for further combinations
         */
        default CombinationStep<T> union(@NonNull SelectExpression<T> selectExpression) {
            return combine(selectExpression, SelectCombination.UNION, false);
        }

        /**
         * Combines with UNION ALL (retaining duplicates). Delegates to
         * {@code combine(selectExpression, UNION, true)}.
         *
         * @param selectExpression the right-hand query
         * @return this step for further combinations
         */
        default CombinationStep<T> unionAll(@NonNull SelectExpression<T> selectExpression) {
            return combine(selectExpression, SelectCombination.UNION, true);
        }

        /**
         * Combines with INTERSECT. Delegates to {@code combine(selectExpression, INTERSECT, all)}.
         *
         * @param selectExpression the right-hand query
         * @param all whether to retain duplicates
         * @return this step for further combinations
         */
        default CombinationStep<T> intersect(@NonNull SelectExpression<T> selectExpression, boolean all) {
            return combine(selectExpression, SelectCombination.INTERSECT, all);
        }

        /**
         * Combines with INTERSECT (removing duplicates). Delegates to
         * {@code combine(selectExpression, INTERSECT, false)}.
         *
         * @param selectExpression the right-hand query
         * @return this step for further combinations
         */
        default CombinationStep<T> intersect(@NonNull SelectExpression<T> selectExpression) {
            return combine(selectExpression, SelectCombination.INTERSECT, false);
        }

        /**
         * Combines with INTERSECT ALL (retaining duplicates). Not supported on H2.
         * Delegates to {@code combine(selectExpression, INTERSECT, true)}.
         *
         * @param selectExpression the right-hand query
         * @return this step for further combinations
         */
        default CombinationStep<T> intersectAll(@NonNull SelectExpression<T> selectExpression) {
            return combine(selectExpression, SelectCombination.INTERSECT, true);
        }

        /**
         * Combines with EXCEPT. Delegates to {@code combine(selectExpression, EXCEPT, all)}.
         *
         * @param selectExpression the right-hand query
         * @param all whether to retain duplicates
         * @return this step for further combinations
         */
        default CombinationStep<T> except(@NonNull SelectExpression<T> selectExpression, boolean all) {
            return combine(selectExpression, SelectCombination.EXCEPT, all);
        }

        /**
         * Combines with EXCEPT (removing duplicates). Delegates to
         * {@code combine(selectExpression, EXCEPT, false)}.
         *
         * @param selectExpression the right-hand query
         * @return this step for further combinations
         */
        default CombinationStep<T> except(@NonNull SelectExpression<T> selectExpression) {
            return combine(selectExpression, SelectCombination.EXCEPT, false);
        }

        /**
         * Combines with EXCEPT ALL (retaining duplicates). Not supported on H2.
         * Delegates to {@code combine(selectExpression, EXCEPT, true)}.
         *
         * @param selectExpression the right-hand query
         * @return this step for further combinations
         */
        default CombinationStep<T> exceptAll(@NonNull SelectExpression<T> selectExpression) {
            return combine(selectExpression, SelectCombination.EXCEPT, true);
        }

        /**
         * Combines with another query using the specified set operation (fluent API variant).
         * Convenience overload that converts the fluent step to non-fluent via
         * {@link SelectExpression.CompleteStep#asNonFluent()}.
         *
         * @param selectExpression the right-hand query (fluent)
         * @param combination the set operation type
         * @param all whether to retain duplicates
         * @return this step for further combinations
         */
        default CombinationStep<T> combine(@NonNull SelectExpression.CompleteStep<T> selectExpression, @NonNull SelectCombination combination, boolean all) {
            return combine(selectExpression.asNonFluent(), combination, all);
        }

        /**
         * Combines with UNION (fluent API variant). Delegates to
         * {@code combine(selectExpression.asNonFluent(), UNION, all)}.
         *
         * @param selectExpression the right-hand query (fluent)
         * @param all whether to retain duplicates
         * @return this step for further combinations
         */
        default CombinationStep<T> union(@NonNull SelectExpression.CompleteStep<T> selectExpression, boolean all) {
            return combine(selectExpression.asNonFluent(), SelectCombination.UNION, all);
        }

        /**
         * Combines with UNION removing duplicates (fluent API variant).
         *
         * @param selectExpression the right-hand query (fluent)
         * @return this step for further combinations
         */
        default CombinationStep<T> union(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
            return combine(selectExpression.asNonFluent(), SelectCombination.UNION, false);
        }

        /**
         * Combines with UNION ALL retaining duplicates (fluent API variant).
         *
         * @param selectExpression the right-hand query (fluent)
         * @return this step for further combinations
         */
        default CombinationStep<T> unionAll(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
            return combine(selectExpression.asNonFluent(), SelectCombination.UNION, true);
        }

        /**
         * Combines with INTERSECT (fluent API variant).
         *
         * @param selectExpression the right-hand query (fluent)
         * @param all whether to retain duplicates
         * @return this step for further combinations
         */
        default CombinationStep<T> intersect(@NonNull SelectExpression.CompleteStep<T> selectExpression, boolean all) {
            return combine(selectExpression.asNonFluent(), SelectCombination.INTERSECT, all);
        }

        /**
         * Combines with INTERSECT removing duplicates (fluent API variant).
         *
         * @param selectExpression the right-hand query (fluent)
         * @return this step for further combinations
         */
        default CombinationStep<T> intersect(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
            return combine(selectExpression.asNonFluent(), SelectCombination.INTERSECT, false);
        }

        /**
         * Combines with INTERSECT ALL retaining duplicates (fluent API variant).
         *
         * @param selectExpression the right-hand query (fluent)
         * @return this step for further combinations
         */
        default CombinationStep<T> intersectAll(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
            return combine(selectExpression.asNonFluent(), SelectCombination.INTERSECT, true);
        }

        /**
         * Combines with EXCEPT (fluent API variant).
         *
         * @param selectExpression the right-hand query (fluent)
         * @param all whether to retain duplicates
         * @return this step for further combinations
         */
        default CombinationStep<T> except(@NonNull SelectExpression.CompleteStep<T> selectExpression, boolean all) {
            return combine(selectExpression.asNonFluent(), SelectCombination.EXCEPT, all);
        }

        /**
         * Combines with EXCEPT removing duplicates (fluent API variant).
         *
         * @param selectExpression the right-hand query (fluent)
         * @return this step for further combinations
         */
        default CombinationStep<T> except(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
            return combine(selectExpression.asNonFluent(), SelectCombination.EXCEPT, false);
        }

        /**
         * Combines with EXCEPT ALL retaining duplicates (fluent API variant).
         *
         * @param selectExpression the right-hand query (fluent)
         * @return this step for further combinations
         */
        default CombinationStep<T> exceptAll(@NonNull SelectExpression.CompleteStep<T> selectExpression) {
            return combine(selectExpression.asNonFluent(), SelectCombination.EXCEPT, true);
        }
    }

    /**
     * Step for adding an ORDER BY clause to sort the result set.
     *
     * <p>This step also extends {@link LimitStep}, so you can skip ordering and proceed
     * directly to LIMIT, OFFSET, or FETCH.
     *
     * @param <T> the result row type
     */
    interface OrderStep<T> extends LimitStep<T> {

        /**
         * Sorts the result set by the specified orderings.
         *
         * @param orders the sort orderings (e.g., {@code column.asc()}, {@code column.desc()})
         * @return the LIMIT step for pagination or FETCH
         */
        LimitStep<T> orderBy(@NonNull List<Order> orders);

        /**
         * Sorts the result set by a single ordering. Convenience overload that delegates to
         * {@code orderBy(List.of(order))}.
         *
         * @param order the sort ordering
         * @return the LIMIT step
         */
        default LimitStep<T> orderBy(@NonNull Order order) {
            return orderBy(List.of(order));
        }

        /**
         * Sorts the result set by the specified orderings. Convenience varargs overload
         * that delegates to {@code orderBy(Arrays.asList(orders))}.
         *
         * @param orders the sort orderings
         * @return the LIMIT step
         */
        default LimitStep<T> orderBy(@NonNull Order... orders) {
            return orderBy(Arrays.asList(orders));
        }
    }

    /**
     * Step for adding a LIMIT clause to restrict the number of returned rows.
     *
     * <p>This step also extends {@link OffsetStep}, so you can skip LIMIT and proceed
     * directly to OFFSET or FETCH.
     *
     * @param <T> the result row type
     */
    interface LimitStep<T> extends OffsetStep<T> {

        /**
         * Limits the result set to the specified maximum number of rows.
         *
         * @param limit the maximum number of rows to return
         * @return the OFFSET step for skipping rows or FETCH
         */
        OffsetStep<T> limit(@NonNull Integer limit);
    }

    /**
     * Step for adding an OFFSET clause to skip rows before returning results.
     *
     * <p>This step also extends {@link FetchStep}, so you can skip OFFSET and proceed
     * directly to fetching results.
     *
     * @param <T> the result row type
     */
    interface OffsetStep<T> extends FetchStep<T> {

        /**
         * Skips the specified number of rows before returning results.
         *
         * @param offset the number of rows to skip
         * @return the FETCH step for retrieving results
         */
        FetchStep<T> offset(@NonNull Integer offset);
    }

    /**
     * Terminal step for fetching query results and applying locking.
     *
     * <p>Provides multiple result retrieval strategies:
     * <ul>
     *   <li>{@link #fetchAll()} -- eagerly loads all results into a list</li>
     *   <li>{@link #fetchSingle()} -- expects exactly one result</li>
     *   <li>{@link #fetchOne()} -- expects zero or one result</li>
     *   <li>{@link #fetchStream()} -- streams results for memory-efficient processing</li>
     *   <li>{@link #fetchPage(int, int)} -- fetches a specific page of results</li>
     * </ul>
     *
     * @param <T> the result row type
     */
    interface FetchStep<T> {

        /**
         * Adds a {@code FOR UPDATE} clause to lock selected rows for the duration of the
         * current transaction.
         *
         * @return this step for fetching the locked results
         */
        FetchStep<T> forUpdate();

        /**
         * Executes the query and returns all results as a list.
         *
         * @return the list of result rows (empty list if no results)
         */
        List<T> fetchAll();

        /**
         * Executes the query and returns a stream for memory-efficient iteration using
         * the default fetch size.
         *
         * <p><b>Important:</b> The returned stream must be closed by the caller (e.g.,
         * using try-with-resources) to release the underlying database resources.
         *
         * @return a stream of result rows
         */
        Stream<T> fetchStream();

        /**
         * Returns a stream for memory-efficient iteration with custom fetch size.
         * The stream MUST be closed by the caller.
         * @param fetchSize number of rows to fetch per database round-trip
         */
        Stream<T> fetchStream(int fetchSize);

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
         * Executes the query and returns a specific page of results.
         *
         * <p>This is a convenience for LIMIT/OFFSET-based pagination. The returned
         * {@link Page} contains the result rows and pagination metadata.
         *
         * @param pageSize the number of rows per page
         * @param page the zero-based page index
         * @return the page of results
         * @see Page
         */
        Page<T> fetchPage(int pageSize, int page);
    }
}
