// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.query;

import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.QTableName;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Fluent builder for SQL DELETE statements.
 *
 * <p>The builder follows a step-by-step progression: FROM, WHERE, RETURNING, EXECUTE.
 * An optional RETURNING clause allows retrieving the values of deleted rows.
 *
 * <h2>Usage example</h2>
 * <pre>{@code
 * int rowsDeleted = session.delete()
 *     .from(TABLE)
 *     .where(TABLE.id.eq(1L))
 *     .execute();
 * }</pre>
 *
 * <h2>Delete with RETURNING clause</h2>
 * <pre>{@code
 * List<Record> deleted = session.delete()
 *     .from(TABLE)
 *     .where(TABLE.status.eq("inactive"))
 *     .returning(TABLE.id, TABLE.name)
 *     .executeAndFetchAll();
 * }</pre>
 *
 * @see HarborSession#delete()
 * @see HarborSession#delete(QTableName)
 */
public interface DeleteQuery {

    /**
     * Step for specifying the target table of the DELETE statement.
     *
     * <p>This is the first step in the fluent DELETE builder chain. It also extends
     * {@link WhereStep}, allowing the table to be set implicitly via
     * {@link HarborSession#delete(QTableName)}.
     */
    interface FromStep extends WhereStep {

        /**
         * Specifies the target table for the DELETE statement.
         *
         * @param table the target table name
         * @return the WHERE step for adding filter conditions
         */
        WhereStep from(@NonNull QTableName table);

        /**
         * Specifies the target table by name. Convenience overload that delegates to
         * {@code from(new QTableName(table, null, null))}.
         *
         * @param table the target table name as a string
         * @return the WHERE step for adding filter conditions
         */
        default WhereStep from(@NonNull String table) {
            return from(new QTableName(table, null, null));
        }

        /**
         * Specifies the target table by name and schema. Convenience overload that delegates to
         * {@code from(new QTableName(table, schema, null))}.
         *
         * @param table  the target table name as a string
         * @param schema the database schema, or {@code null} for the default schema
         * @return the WHERE step for adding filter conditions
         */
        default WhereStep from(@NonNull String table, String schema) {
            return from(new QTableName(table, schema, null));
        }
    }

    /**
     * Step for adding WHERE conditions to restrict which rows are deleted.
     *
     * <p>Multiple calls to {@code where()} are combined with AND. This step also extends
     * {@link ReturningStep} and {@link ExecuteStep}, so WHERE is optional.
     *
     * <p><b>Caution:</b> Omitting a WHERE clause will delete all rows in the table.
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
     * Step for optionally adding a RETURNING clause to the DELETE statement.
     *
     * <p>The RETURNING clause causes the DELETE to return the values of the specified
     * expressions for each deleted row.
     */
    interface ReturningStep extends ExecuteStep {

        /**
         * Adds a RETURNING clause to the DELETE statement.
         *
         * @param expressions the expressions whose values to return for each deleted row
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
     * Step for executing a DELETE with a RETURNING clause and fetching the returned rows.
     *
     * @param <T> the result row type
     */
    interface ExecuteFetchStep<T> extends ExecuteStep {

        /**
         * Executes the DELETE statement and returns all rows produced by the RETURNING clause.
         *
         * @return the list of returned rows
         */
        List<T> executeAndFetchAll();

        /**
         * Executes the DELETE and returns exactly one result from the RETURNING clause.
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
         * Executes the DELETE and returns zero or one result from the RETURNING clause.
         *
         * @return an {@link Optional} containing the returned row, or empty if no rows were deleted
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
     * Terminal step for executing the DELETE statement.
     */
    interface ExecuteStep {

        /**
         * Executes the DELETE statement and returns the number of affected rows.
         *
         * @return the number of rows deleted
         */
        int execute();
    }
}
