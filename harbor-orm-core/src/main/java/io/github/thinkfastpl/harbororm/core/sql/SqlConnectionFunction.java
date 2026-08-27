// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import lombok.NonNull;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Functional interface for executing an operation that requires a JDBC {@link java.sql.Connection}
 * and returns a result.
 *
 * @param <R> the return type of the operation
 * @see SqlConnectionAccessor#execute(SqlConnectionFunction)
 */
@FunctionalInterface
public interface SqlConnectionFunction<R> {

    /**
     * Executes an operation using the given JDBC connection.
     *
     * @param connection the JDBC connection to use
     * @return the result of the operation
     * @throws SQLException if a database access error occurs
     */
    R execute(@NonNull Connection connection) throws SQLException;
}
