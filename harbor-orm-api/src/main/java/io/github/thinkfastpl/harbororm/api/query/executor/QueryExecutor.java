// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.query.executor;

import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.query.data.DeleteQueryData;
import io.github.thinkfastpl.harbororm.api.query.data.InsertQueryData;
import io.github.thinkfastpl.harbororm.api.query.data.SelectQueryData;
import io.github.thinkfastpl.harbororm.api.query.data.UpdateQueryData;
import io.github.thinkfastpl.harbororm.api.query.result.InsertResult;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import lombok.NonNull;
import lombok.Value;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Low-level interface for executing SQL queries against the database.
 *
 * <p>Implementations translate the immutable query data objects ({@link SelectQueryData},
 * {@link InsertQueryData}, {@link UpdateQueryData}, {@link DeleteQueryData}) into
 * vendor-specific SQL, execute them via JDBC, and return results as {@link Record} instances.
 *
 * <p>This interface is not typically used directly by application code. Instead, use the
 * fluent query builders available through {@link io.github.thinkfastpl.harbororm.api.HarborSession}.
 *
 * @see io.github.thinkfastpl.harbororm.api.HarborSession
 */
public interface QueryExecutor {

    /**
     * A pair of records returned from a fetch that includes extra select expressions,
     * separating the main query columns from the additional ones.
     */
    @Value
    class RecordWithExtra {

        /** The record containing the main SELECT columns. */
        @NonNull
        Record record;

        /** The record containing the extra SELECT columns. */
        @NonNull
        Record extraRecord;
    }

    /**
     * Executes a SELECT query and returns all matching rows.
     *
     * @param data the select query data
     * @return all matching records
     */
    List<Record> fetchAll(@NonNull SelectQueryData data);

    /**
     * Executes a SELECT query with additional expressions appended to the SELECT list.
     * Results are split into the main record and an extra record for the appended expressions.
     *
     * @param data                  the select query data
     * @param extraSelectExpressions additional expressions to append to the SELECT list
     * @return records paired with their extra-expression values
     */
    List<RecordWithExtra> fetchAll(@NonNull SelectQueryData data, @NonNull List<Expression<?>> extraSelectExpressions);

    /**
     * Fetches results as a stream for memory-efficient processing of large result sets.
     * Records are read and mapped in batches of {@code fetchSize} using the given mapper.
     * The returned stream MUST be closed by the caller (use try-with-resources).
     * The database connection remains open until the stream is closed.
     *
     * @param data         the select query data
     * @param fetchSize    JDBC fetch size hint, also the size of each mapped batch
     * @param recordMapper mapper applied to every fetched record
     * @param <T>          the mapped result type
     * @return a stream of mapped results that must be closed after use
     */
    <T> Stream<T> fetchStream(@NonNull SelectQueryData data, int fetchSize, @NonNull Function<Record, T> recordMapper);

    /**
     * Executes an INSERT statement.
     *
     * @param data the insert query data
     * @return the insert result containing affected row count and any generated keys
     */
    InsertResult insert(@NonNull InsertQueryData data);

    /**
     * Executes an INSERT statement with a RETURNING clause.
     *
     * @param data the insert query data (must include returning expressions)
     * @return records produced by the RETURNING clause
     */
    List<Record> insertReturning(@NonNull InsertQueryData data);

    /**
     * Executes an UPDATE statement.
     *
     * @param data the update query data
     * @return the number of rows affected
     */
    int update(@NonNull UpdateQueryData data);

    /**
     * Executes an UPDATE statement with a RETURNING clause.
     *
     * @param data the update query data (must include returning expressions)
     * @return records produced by the RETURNING clause
     */
    List<Record> updateReturning(@NonNull UpdateQueryData data);

    /**
     * Executes a DELETE statement.
     *
     * @param data the delete query data
     * @return the number of rows affected
     */
    int delete(@NonNull DeleteQueryData data);

    /**
     * Executes a DELETE statement with a RETURNING clause.
     *
     * @param data the delete query data (must include returning expressions)
     * @return records produced by the RETURNING clause
     */
    List<Record> deleteReturning(@NonNull DeleteQueryData data);

    /**
     * Executes a block of operations on a single JDBC connection, ensuring all queries
     * within the consumer share the same connection. Useful for temporary tables, session
     * variables, or other connection-scoped state.
     *
     * @param singleConnectionQueryExecutorConsumer consumer that receives a single-connection executor
     */
    void executeOnSingleConnection(@NonNull Consumer<QueryExecutor> singleConnectionQueryExecutorConsumer);

    /**
     * Calls a stored procedure (no return value).
     *
     * @param procedureName the name of the stored procedure
     * @param params        the parameters to pass to the procedure
     */
    void call(@NonNull String procedureName, @NonNull Object[] params);

    /**
     * Calls a stored function and returns its result.
     *
     * @param functionName the name of the stored function
     * @param returnType   the expected Java type of the return value
     * @param params       the parameters to pass to the function
     * @param <T>          the return type
     * @return the function's return value
     */
    <T> T callReturning(@NonNull String functionName, @NonNull Class<T> returnType, @NonNull Object[] params);

    /**
     * Gets next value for given sequence
     *
     * @param sequenceName Sequence name
     * @return Next value for given sequence
     */
    Long nextSequenceValue(@NonNull String sequenceName);
}
