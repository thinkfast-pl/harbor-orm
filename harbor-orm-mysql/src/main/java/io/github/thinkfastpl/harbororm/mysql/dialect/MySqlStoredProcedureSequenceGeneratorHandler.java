// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mysql.dialect;

import io.github.thinkfastpl.harbororm.core.sql.CustomSequenceGeneratorHandler;
import lombok.NonNull;

import java.sql.*;

/**
 * {@link CustomSequenceGeneratorHandler} that simulates sequences on MySQL, which has no native
 * sequence support, by delegating to a stored procedure backed by a counter table.
 * <p>
 * The expected database objects (names configurable only for the procedure; the table name is
 * whatever the procedure references):
 * <pre>{@code
 * create table harbor_sequences (
 *     name varchar(100) not null,
 *     next_val bigint not null,
 *     primary key (name)
 * );
 *
 * CREATE PROCEDURE harbor_sequence_nextval(IN seq_name VARCHAR(100))
 *     UPDATE harbor_sequences SET next_val = LAST_INSERT_ID(next_val + 1) WHERE name = seq_name;
 * }</pre>
 * The procedure must set the generated value through {@code LAST_INSERT_ID(expr)}; the handler
 * reads it back with {@code SELECT LAST_INSERT_ID()} on the same connection. That value is
 * connection-scoped, and the atomic UPDATE locks the counter row, so concurrent connections cannot
 * observe the same value.
 * <p>
 * Register via
 * {@code new MySqlRdbmsSupport(new MySqlStoredProcedureSequenceGeneratorHandler("harbor_sequence_nextval"))}.
 */
public class MySqlStoredProcedureSequenceGeneratorHandler implements CustomSequenceGeneratorHandler {

    private final String procedureName;

    public MySqlStoredProcedureSequenceGeneratorHandler(@NonNull String procedureName) {
        this.procedureName = procedureName;
    }

    @Override
    public Long nextVal(@NonNull Connection connection, @NonNull String sequenceName) {
        try {
            try (CallableStatement callableStatement = connection.prepareCall("{call " + procedureName + "(?)}")) {
                callableStatement.setString(1, sequenceName);
                final int updatedRows = callableStatement.executeUpdate();
                if (updatedRows != 1) {
                    throw new IllegalStateException("Sequence not found: " + sequenceName);
                }
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT LAST_INSERT_ID()");
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("Unable to read generated sequence value: " + sequenceName);
                }
                return resultSet.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
