// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.query;

import io.github.thinkfastpl.harbororm.api.expression.DSL;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.metadata.QTableName;
import io.github.thinkfastpl.harbororm.api.query.result.InsertResult;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Fluent builder for SQL INSERT statements.
 *
 * <p>This interface provides two APIs for building INSERT statements:
 * <ul>
 *   <li><b>Fluent API</b> (via inner step interfaces) -- method-chained builder obtained from
 *       {@link HarborSession#insert()}</li>
 *   <li><b>Non-fluent API</b> (via direct methods on this interface) -- imperative style where
 *       each method is called separately</li>
 * </ul>
 *
 * <h2>Fluent API example</h2>
 * <pre>{@code
 * session.insert()
 *     .into(TABLE)
 *     .set(TABLE.name, "Alice")
 *     .set(TABLE.age, 30)
 *     .execute();
 * }</pre>
 *
 * <h2>Multi-row insert example</h2>
 * <pre>{@code
 * session.insert()
 *     .into(TABLE)
 *     .set(TABLE.name, "Alice")
 *     .nextRow()
 *     .set(TABLE.name, "Bob")
 *     .execute();
 * }</pre>
 *
 * <h2>Insert with RETURNING clause</h2>
 * <pre>{@code
 * Record result = session.insert()
 *     .into(TABLE)
 *     .set(TABLE.name, "Alice")
 *     .returning(TABLE.id)
 *     .executeAndFetchSingle();
 * }</pre>
 *
 * @see HarborSession#insert()
 * @see HarborSession#insertInto(QTableName)
 */
public interface InsertQuery {

    /**
     * Step for specifying the target table of the INSERT statement.
     *
     * <p>This is the first step in the fluent INSERT builder chain.
     */
    interface IntoStep {

        /**
         * Specifies the target table for the INSERT statement.
         *
         * @param table the target table name
         * @return the SET step for assigning column values
         */
        SetStep into(@NonNull QTableName table);

        /**
         * Specifies the target table by name. Convenience overload that delegates to
         * {@code into(new QTableName(tableName, null, null))}.
         *
         * @param tableName the target table name as a string
         * @return the SET step for assigning column values
         */
        default SetStep into(@NonNull String tableName) {
            return into(new QTableName(tableName, null, null));
        }
    }

    /**
     * Step for assigning column values in the INSERT statement.
     *
     * <p>Call {@link #set(QColumn, Object)} or {@link #set(QColumn, Expression)} for each
     * column-value pair. Use {@link #nextRow()} to begin a new row in a multi-row insert.
     * When all values are set, proceed to {@link ReturningStep} or {@link ExecuteStep}.
     */
    interface SetStep extends ReturningStep {

        /**
         * Sets a column to a literal value. Convenience overload that wraps the value in a
         * constant expression via {@link DSL#constant(Class, Object)}.
         *
         * @param <C>    the column's Java type
         * @param column the target column
         * @param value  the value to insert (may be {@code null} if the column is nullable)
         * @return this step for further column assignments
         */
        default <C> SetStep set(@NonNull QColumn<C> column, C value) {
            return set(column, DSL.constant(column.getJavaType(), value));
        }

        /**
         * Sets a column to the result of an expression.
         *
         * @param <C>    the column's Java type
         * @param column the target column
         * @param value  the expression whose result will be inserted
         * @return this step for further column assignments
         */
        <C> SetStep set(@NonNull QColumn<C> column, @NonNull Expression<C> value);

        /**
         * Begins a new row in a multi-row INSERT statement.
         *
         * <p>After calling this method, use {@link #set(QColumn, Object)} to assign values
         * for the next row.
         *
         * @return this step for assigning column values to the new row
         */
        SetStep nextRow();

        /**
         * Begins an ON CONFLICT clause specifying the conflict target columns.
         *
         * <p>This is a PostgreSQL-only feature. Using it with H2 will throw
         * {@link UnsupportedOperationException} at execution time.
         *
         * @param columns the conflict target columns (e.g., unique index columns)
         * @return the step for choosing the conflict action
         */
        OnConflictColumnsStep onConflict(@NonNull QColumn<?>... columns);
    }

    /**
     * Step for optionally adding a RETURNING clause to the INSERT statement.
     *
     * <p>The RETURNING clause causes the INSERT to return the values of the specified
     * expressions for each inserted row, rather than just executing silently.
     */
    interface ReturningStep extends ExecuteStep {

        /**
         * Adds a RETURNING clause to the INSERT statement.
         *
         * @param expressions the expressions whose values to return for each inserted row
         * @return the execute-and-fetch step for retrieving the returned rows
         */
        ExecuteFetchStep<Record> returning(@NonNull List<Expression<?>> expressions);

        /**
         * Adds a RETURNING clause. Convenience varargs overload that delegates to
         * {@code returning(Arrays.asList(expressions))}.
         *
         * @param expressions the expressions whose values to return
         * @return the execute-and-fetch step for retrieving the returned rows
         */
        default ExecuteFetchStep<Record> returning(@NonNull Expression<?>... expressions) {
            return returning(Arrays.asList(expressions));
        }
    }

    /**
     * Step for executing an INSERT with a RETURNING clause and fetching the returned rows.
     *
     * @param <T> the result row type
     */
    interface ExecuteFetchStep<T> extends ExecuteStep {

        /**
         * Executes the INSERT statement and returns all rows produced by the RETURNING clause.
         *
         * @return the list of returned rows
         */
        List<T> executeAndFetchAll();

        /**
         * Executes the INSERT and returns exactly one result from the RETURNING clause.
         *
         * @return the single returned row
         * @throws IllegalStateException if the result count is not exactly 1
         */
        default T executeAndFetchSingle() {
            final List<T> all = executeAndFetchAll();
            if (all.size() != 1) {
                throw new IllegalStateException("Expected only 1 result. Got: " + all.size());
            }
            return all.get(0);
        }

        /**
         * Executes the INSERT and returns zero or one result from the RETURNING clause.
         *
         * @return an {@link Optional} containing the returned row, or empty if no rows were returned
         * @throws IllegalStateException if more than 1 row is returned
         */
        default Optional<T> executeAndFetchOne() {
            final List<T> all = executeAndFetchAll();
            return switch (all.size()) {
                case 0 -> Optional.empty();
                case 1 -> Optional.of(all.get(0));
                default -> throw new IllegalStateException("Expected exactly 1 or 0 results. Got: " + all.size());
            };
        }
    }

    /**
     * Terminal step for executing the INSERT statement.
     */
    interface ExecuteStep {
        /**
         * Executes the INSERT statement without returning any results.
         */
        void execute();

        /**
         * Converts this fluent builder into its non-fluent {@link InsertQuery} representation.
         *
         * @return the non-fluent insert query
         */
        InsertQuery asNonFluent();
    }

    /**
     * Step for choosing the conflict action after specifying conflict target columns.
     */
    interface OnConflictColumnsStep {

        /**
         * Specifies that conflicting rows should be silently skipped.
         *
         * @return the terminal step for executing or adding RETURNING
         */
        OnConflictFinalStep doNothing();

        /**
         * Specifies that conflicting rows should be updated.
         *
         * @return the step for specifying which columns to update
         */
        OnConflictUpdateStep doUpdate();
    }

    /**
     * Step for specifying column assignments in an ON CONFLICT DO UPDATE SET clause.
     *
     * <p>Use {@link DSL#excluded(QColumn)} to reference the proposed-but-conflicted row values.
     */
    interface OnConflictUpdateStep extends OnConflictFinalStep {

        /**
         * Sets a column to a literal value on conflict.
         *
         * @param <T>    the column's Java type
         * @param column the column to update
         * @param value  the literal value
         * @return this step for further assignments
         */
        default <T> OnConflictUpdateStep set(@NonNull QColumn<T> column, T value) {
            return set(column, DSL.constant(column.getJavaType(), value));
        }

        /**
         * Sets a column to an expression on conflict.
         *
         * @param <T>        the column's Java type
         * @param column     the column to update
         * @param expression the value expression (may use {@link DSL#excluded(QColumn)})
         * @return this step for further assignments
         */
        <T> OnConflictUpdateStep set(@NonNull QColumn<T> column, @NonNull Expression<T> expression);

        /**
         * Adds an optional WHERE condition to the DO UPDATE clause.
         * The update only applies to rows matching this condition.
         *
         * @param condition the filter condition
         * @return the terminal step for executing or adding RETURNING
         */
        OnConflictFinalStep where(@NonNull Expression<Boolean> condition);
    }

    /**
     * Terminal step after an ON CONFLICT clause. Allows RETURNING or direct execution.
     */
    interface OnConflictFinalStep extends ReturningStep {
    }

    // ---- Non-fluent API methods ----

    /**
     * Sets the target table for this insert query (non-fluent API).
     *
     * @param table the target table name
     */
    void into(@NonNull QTableName table);

    /**
     * Sets the target table by name (non-fluent API). Convenience overload that delegates to
     * {@code into(new QTableName(tableName, null, null))}.
     *
     * @param tableName the target table name as a string
     */
    default void into(@NonNull String tableName) {
        into(new QTableName(tableName, null, null));
    }

    /**
     * Sets the target table by name and schema (non-fluent API). Convenience overload that delegates to
     * {@code into(new QTableName(tableName, schema, null))}.
     *
     * @param tableName the target table name as a string
     * @param schema    the database schema name, or {@code null} for the default schema
     */
    default void into(@NonNull String tableName, String schema) {
        into(new QTableName(tableName, schema, null));
    }

    /**
     * Sets a column to the result of an expression (non-fluent API).
     *
     * @param <C>    the column's Java type
     * @param column the target column
     * @param value  the expression whose result will be inserted
     */
    <C> void set(@NonNull QColumn<C> column, @NonNull Expression<C> value);

    /**
     * Sets a column to a literal value (non-fluent API). Convenience overload that wraps
     * the value in a constant expression via {@link DSL#constant(Class, Object)}.
     *
     * @param <C>    the column's Java type
     * @param column the target column
     * @param value  the value to insert
     */
    default <C> void set(@NonNull QColumn<C> column, C value) {
        set(column, DSL.constant(column.getJavaType(), value));
    }

    /**
     * Begins a new row in a multi-row INSERT (non-fluent API).
     */
    void nextRow();

    /**
     * Adds a RETURNING clause to this insert query (non-fluent API).
     *
     * @param expressions the expressions whose values to return for each inserted row
     */
    void returning(@NonNull List<Expression<?>> expressions);

    /**
     * Adds a RETURNING clause (non-fluent API). Convenience varargs overload that delegates to
     * {@code returning(Arrays.asList(expressions))}.
     *
     * @param expressions the expressions whose values to return
     */
    default void returning(@NonNull Expression<?>... expressions) {
        returning(Arrays.asList(expressions));
    }

    /**
     * Executes the INSERT and returns all rows produced by the RETURNING clause (non-fluent API).
     *
     * <p>Must be preceded by a call to {@link #returning(List)}.
     *
     * @return the list of returned records
     */
    List<Record> executeAndFetchAll();

    /**
     * Executes the INSERT and returns exactly one result from the RETURNING clause (non-fluent API).
     *
     * @return the single returned record
     * @throws IllegalStateException if the result count is not exactly 1
     */
    default Record executeAndFetchSingle() {
        final List<Record> all = executeAndFetchAll();
        if (all.size() != 1) {
            throw new IllegalStateException("Expected only 1 result. Got: " + all.size());
        }
        return all.get(0);
    }

    /**
     * Executes the INSERT and returns zero or one result from the RETURNING clause (non-fluent API).
     *
     * @return an {@link Optional} containing the returned record, or empty if no rows were returned
     * @throws IllegalStateException if more than 1 row is returned
     */
    default Optional<Record> executeAndFetchOne() {
        final List<Record> all = executeAndFetchAll();
        return switch (all.size()) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of(all.get(0));
            default -> throw new IllegalStateException("Expected exactly 1 or 0 results. Got: " + all.size());
        };
    }

    /**
     * Executes the INSERT statement and returns the result summary (non-fluent API).
     *
     * @return the insert result containing information about the execution
     */
    InsertResult execute();
}
