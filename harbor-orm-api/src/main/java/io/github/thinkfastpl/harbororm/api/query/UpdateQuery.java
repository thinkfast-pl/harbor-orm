// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.query;

import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.expression.ConstantExpression;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.metadata.QTableName;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Fluent builder for SQL UPDATE statements.
 *
 * <p>The builder follows a step-by-step progression: TABLE, SET, WHERE, RETURNING, EXECUTE.
 * An optional RETURNING clause allows retrieving the values of updated rows.
 *
 * <h2>Usage example</h2>
 * <pre>{@code
 * int rowsUpdated = session.update()
 *     .table(TABLE)
 *     .set(TABLE.name, "Bob")
 *     .set(TABLE.age, TABLE.age.add(1))
 *     .where(TABLE.id.eq(1L))
 *     .execute();
 * }</pre>
 *
 * <h2>Update with RETURNING clause</h2>
 * <pre>{@code
 * Record updated = session.update()
 *     .table(TABLE)
 *     .set(TABLE.status, "active")
 *     .where(TABLE.id.eq(1L))
 *     .returning(TABLE.id, TABLE.status)
 *     .executeAndFetchSingle();
 * }</pre>
 *
 * @see HarborSession#update()
 * @see HarborSession#update(QTableName)
 */
public interface UpdateQuery {

    /**
     * Step for specifying the target table of the UPDATE statement.
     *
     * <p>This is the first step in the fluent UPDATE builder chain. It also extends
     * {@link SetStep}, allowing the table to be set implicitly via
     * {@link HarborSession#update(QTableName)}.
     */
    interface TableStep extends SetStep {

        /**
         * Specifies the target table for the UPDATE statement.
         *
         * @param table the target table name
         * @return the SET step for assigning new column values
         */
        SetStep table(@NonNull QTableName table);
    }

    /**
     * Step for assigning new column values in the UPDATE statement.
     *
     * <p>Call {@link #set(QColumn, Object)} or {@link #set(QColumn, Expression)} for each
     * column to update. When all values are set, proceed to {@link WhereStep},
     * {@link ReturningStep}, or {@link ExecuteStep}.
     */
    interface SetStep extends WhereStep {

        /**
         * Sets a column to the result of an expression.
         *
         * <p>The expression can reference other columns for computed updates, e.g.,
         * {@code set(TABLE.count, TABLE.count.add(1))}.
         *
         * @param <C> the column's Java type
         * @param column the column to update
         * @param expression the expression whose result becomes the new value
         * @return this step for further column assignments
         */
        <C> SetStep set(@NonNull QColumn<C> column, @NonNull Expression<C> expression);

        /**
         * Sets a column to a literal value. Convenience overload that wraps the value in a
         * {@link ConstantExpression}.
         *
         * @param <C> the column's Java type
         * @param column the column to update
         * @param value the new value (may be {@code null} if the column is nullable)
         * @return this step for further column assignments
         */
        default <C> SetStep set(@NonNull QColumn<C> column, C value) {
            return set(column, new ConstantExpression<>(column.getJavaType(), value));
        }
    }

    /**
     * Step for adding WHERE conditions to restrict which rows are updated.
     *
     * <p>Multiple calls to {@code where()} are combined with AND. This step also extends
     * {@link ReturningStep} and {@link ExecuteStep}, so WHERE is optional.
     *
     * <p><b>Caution:</b> Omitting a WHERE clause will update all rows in the table.
     */
    interface WhereStep extends ReturningStep {

        /**
         * Adds WHERE conditions combined with AND.
         *
         * @param conditions the filter conditions
         * @return this step for adding more conditions or proceeding to RETURNING/EXECUTE
         */
        WhereStep where(@NonNull List<Condition> conditions);

        /**
         * Adds a single WHERE condition. Convenience overload that delegates to
         * {@code where(List.of(condition))}.
         *
         * @param condition the filter condition
         * @return this step for adding more conditions
         */
        default WhereStep where(@NonNull Condition condition) {
            return where(List.of(condition));
        }

        /**
         * Adds WHERE conditions. Convenience varargs overload that delegates to
         * {@code where(Arrays.asList(conditions))}.
         *
         * @param conditions the filter conditions
         * @return this step for adding more conditions
         */
        default WhereStep where(@NonNull Condition... conditions) {
            return where(Arrays.asList(conditions));
        }
    }

    /**
     * Step for optionally adding a RETURNING clause to the UPDATE statement.
     *
     * <p>The RETURNING clause causes the UPDATE to return the values of the specified
     * expressions for each updated row.
     */
    interface ReturningStep extends ExecuteStep {

        /**
         * Adds a RETURNING clause to the UPDATE statement.
         *
         * @param expressions the expressions whose values to return for each updated row
         * @return the execute-and-fetch step for retrieving the returned rows
         */
        ExecuteFetchStep<io.github.thinkfastpl.harbororm.api.query.result.Record> returning(@NonNull List<Expression<?>> expressions);

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
     * Step for executing an UPDATE with a RETURNING clause and fetching the returned rows.
     *
     * @param <T> the result row type
     */
    interface ExecuteFetchStep<T> extends ExecuteStep {

        /**
         * Executes the UPDATE statement and returns all rows produced by the RETURNING clause.
         *
         * @return the list of returned rows
         */
        List<T> executeAndFetchAll();

        /**
         * Executes the UPDATE and returns exactly one result from the RETURNING clause.
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
         * Executes the UPDATE and returns zero or one result from the RETURNING clause.
         *
         * @return an {@link Optional} containing the returned row, or empty if no rows were updated
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
     * Terminal step for executing the UPDATE statement.
     */
    interface ExecuteStep {

        /**
         * Executes the UPDATE statement and returns the number of affected rows.
         *
         * @return the number of rows updated
         */
        int execute();
    }
}
