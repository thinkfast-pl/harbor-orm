// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.api.lob.PortableBlob;
import io.github.thinkfastpl.harbororm.api.lob.PortableClob;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlDialect;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * Database-specific support interface providing SQL dialect creation, LOB handling,
 * prepared statement parameter binding, and result set reading.
 * <p>
 * Each supported database (H2, PostgreSQL, etc.) implements this interface to handle
 * differences in BLOB/CLOB management, parameter setting, and value extraction.
 * Default method implementations cover the common JDBC behavior; dialects override
 * only what differs.
 *
 * @see SqlDialect
 * @see SqlPortableLobSupport
 */
public interface RdbmsSupport {

    /**
     * Creates a new {@link SqlDialect} instance for this database.
     *
     * @return a dialect capable of generating SQL specific to this RDBMS
     */
    SqlDialect createDialect();

    /**
     * Returns {@link CustomSequenceGeneratorHandler} or null if default strategy should be used
     *
     * @return Handler
     */
    default CustomSequenceGeneratorHandler getCustomSequenceGeneratorHandler() {
        return null;
    }

    /**
     * Creates a new BLOB populated with data read from the input stream.
     *
     * <p>The default implementation is backed by a JDBC {@link java.sql.Blob}; dialects
     * may override to use database-specific storage (e.g. PostgreSQL Large Objects).
     *
     * @param connection  the JDBC connection to create the BLOB on
     * @param inputStream the stream providing the binary data
     * @param length      the number of bytes to read from the stream
     * @return a new BLOB containing the data
     * @throws SQLException if a database access error occurs
     * @throws IOException  if reading from the stream fails
     */
    default PortableBlob createBlob(@NonNull Connection connection, @NonNull InputStream inputStream, long length) throws SQLException, IOException {
        final StandardPortableBlob standardPortableBlob = new StandardPortableBlob();
        updateBlob(connection, standardPortableBlob, inputStream, length);
        return standardPortableBlob;
    }

    /**
     * Creates a new, empty BLOB handle with no content.
     *
     * @param connection the JDBC connection (may be needed by dialect-specific overrides)
     * @return a new empty BLOB
     * @throws SQLException if a database access error occurs
     */
    default PortableBlob createBlob(@NonNull Connection connection) throws SQLException {
        return new StandardPortableBlob();
    }

    /**
     * Replaces the content of the given BLOB with data read from the input stream.
     *
     * @param connection  the JDBC connection
     * @param blob        the BLOB to update; must have been created by this dialect
     * @param inputStream the stream providing the new binary data
     * @param length      the number of bytes to read from the stream
     * @throws SQLException             if a database access error occurs
     * @throws IOException              if reading from the stream fails
     * @throws IllegalArgumentException if the BLOB was created by a different dialect
     */
    default void updateBlob(@NonNull Connection connection, @NonNull PortableBlob blob, @NonNull InputStream inputStream, long length) throws SQLException, IOException {
        if (blob instanceof StandardPortableBlob stdBlob) {
            stdBlob.set(connection, inputStream);
        } else {
            throw new IllegalArgumentException("Unsupported blob type: " + blob.getClass().getName());
        }
    }

    /**
     * Removes the content of the given BLOB, releasing its underlying database resources.
     *
     * @param connection the JDBC connection
     * @param blob       the BLOB to clear; must have been created by this dialect
     * @throws SQLException             if a database access error occurs
     * @throws IllegalArgumentException if the BLOB was created by a different dialect
     */
    default void clearBlob(@NonNull Connection connection, @NonNull PortableBlob blob) throws SQLException {
        if (blob instanceof StandardPortableBlob stdBlob) {
            stdBlob.clear();
        } else {
            throw new IllegalArgumentException("Unsupported blob type: " + blob.getClass().getName());
        }
    }

    /**
     * Opens a stream over the content of the given BLOB.
     *
     * @param connection the JDBC connection
     * @param blob       the BLOB to read; must have been created by this dialect
     * @return a stream over the binary content, or {@link Optional#empty()} if the BLOB is empty
     * @throws SQLException             if a database access error occurs
     * @throws IllegalArgumentException if the BLOB was created by a different dialect
     */
    default Optional<InputStream> readBlobData(@NonNull Connection connection, @NonNull PortableBlob blob) throws SQLException {
        if (blob instanceof StandardPortableBlob stdBlob) {
            return stdBlob.getInputStream();
        } else {
            throw new IllegalArgumentException("Unsupported blob type: " + blob.getClass().getName());
        }
    }

    /**
     * Returns the length of the given BLOB's content.
     *
     * @param connection the JDBC connection
     * @param blob       the BLOB to measure; must have been created by this dialect
     * @return the content length in bytes, or {@code 0} if the BLOB is empty
     * @throws SQLException             if a database access error occurs
     * @throws IllegalArgumentException if the BLOB was created by a different dialect
     */
    default long getBlobLength(@NonNull Connection connection, @NonNull PortableBlob blob) throws SQLException {
        if (blob instanceof StandardPortableBlob stdBlob) {
            return stdBlob.getLength();
        } else {
            throw new IllegalArgumentException("Unsupported blob type: " + blob.getClass().getName());
        }
    }

    /**
     * Reads a BLOB reference from the given result set column.
     *
     * <p>Only the reference is materialized; the content is loaded lazily through
     * {@link #readBlobData(Connection, PortableBlob)}.
     *
     * @param connection  the JDBC connection
     * @param resultSet   the result set positioned on the current row
     * @param columnIndex the 1-based column index
     * @return the BLOB referenced by the column (empty handle when the column is NULL)
     * @throws SQLException if a database access error occurs
     */
    default PortableBlob readBlobCell(@NonNull Connection connection, @NonNull ResultSet resultSet, int columnIndex) throws SQLException {
        return new StandardPortableBlob(resultSet.getBlob(columnIndex));
    }

    /**
     * Indicates whether BLOBs created by this dialect require explicit deletion via
     * {@link #deleteBlob(Connection, PortableBlob)} to release underlying database
     * resources when the owning entity is deleted (e.g. PostgreSQL Large Objects).
     *
     * @return {@code true} if manual deletion is required, {@code false} otherwise (default)
     */
    default boolean requiresManualBlobDeletion() {
        return false;
    }

    /**
     * Releases any database resources associated with the given BLOB.
     *
     * <p>Only called when {@link #requiresManualBlobDeletion()} returns {@code true};
     * the default implementation does nothing.
     *
     * @param connection the JDBC connection
     * @param blob       the BLOB to delete
     * @throws SQLException if the deletion fails
     */
    default void deleteBlob(@NonNull Connection connection, @NonNull PortableBlob blob) throws SQLException {
    }

    /**
     * Creates a new CLOB populated with data read from the reader.
     *
     * <p>The default implementation is backed by a JDBC {@link java.sql.Clob}; dialects
     * may override to use database-specific storage (e.g. PostgreSQL Large Objects).
     *
     * @param connection the JDBC connection to create the CLOB on
     * @param reader     the reader providing the character data
     * @param length     the number of characters to read
     * @return a new CLOB containing the data
     * @throws SQLException if a database access error occurs
     * @throws IOException  if reading from the reader fails
     */
    default PortableClob createClob(@NonNull Connection connection, @NonNull Reader reader, long length) throws SQLException, IOException {
        final PortableClob clob = new StandardPortableClob();
        updateClob(connection, clob, reader, length);
        return clob;
    }

    /**
     * Creates a new, empty CLOB handle with no content.
     *
     * @param connection the JDBC connection (may be needed by dialect-specific overrides)
     * @return a new empty CLOB
     * @throws SQLException if a database access error occurs
     */
    default PortableClob createClob(@NonNull Connection connection) throws SQLException {
        return new StandardPortableClob();
    }

    /**
     * Replaces the content of the given CLOB with data read from the reader.
     *
     * @param connection the JDBC connection
     * @param clob       the CLOB to update; must have been created by this dialect
     * @param reader     the reader providing the new character data
     * @param length     the number of characters to read
     * @throws SQLException             if a database access error occurs
     * @throws IOException              if reading from the reader fails
     * @throws IllegalArgumentException if the CLOB was created by a different dialect
     */
    default void updateClob(@NonNull Connection connection, @NonNull PortableClob clob, @NonNull Reader reader, long length) throws SQLException, IOException {
        if (clob instanceof StandardPortableClob stdClob) {
            stdClob.set(connection, reader);
        } else {
            throw new IllegalArgumentException("Unsupported clob type: " + clob.getClass().getName());
        }
    }

    /**
     * Removes the content of the given CLOB, releasing its underlying database resources.
     *
     * @param connection the JDBC connection
     * @param clob       the CLOB to clear; must have been created by this dialect
     * @throws SQLException             if a database access error occurs
     * @throws IllegalArgumentException if the CLOB was created by a different dialect
     */
    default void clearClob(@NonNull Connection connection, @NonNull PortableClob clob) throws SQLException {
        if (clob instanceof StandardPortableClob stdClob) {
            stdClob.clear();
        } else {
            throw new IllegalArgumentException("Unsupported clob type: " + clob.getClass().getName());
        }
    }

    /**
     * Opens a reader over the content of the given CLOB.
     *
     * @param connection the JDBC connection
     * @param clob       the CLOB to read; must have been created by this dialect
     * @return a reader over the character content, or {@link Optional#empty()} if the CLOB is empty
     * @throws SQLException             if a database access error occurs
     * @throws IllegalArgumentException if the CLOB was created by a different dialect
     */
    default Optional<Reader> readClobData(@NonNull Connection connection, @NonNull PortableClob clob) throws SQLException {
        if (clob instanceof StandardPortableClob stdClob) {
            return stdClob.getReader();
        } else {
            throw new IllegalArgumentException("Unsupported clob type: " + clob.getClass().getName());
        }
    }

    /**
     * Returns the length of the given CLOB's content.
     *
     * @param connection the JDBC connection
     * @param clob       the CLOB to measure; must have been created by this dialect
     * @return the content length in characters, or {@code 0} if the CLOB is empty
     * @throws SQLException             if a database access error occurs
     * @throws IllegalArgumentException if the CLOB was created by a different dialect
     */
    default long getClobLength(@NonNull Connection connection, @NonNull PortableClob clob) throws SQLException {
        if (clob instanceof StandardPortableClob stdClob) {
            return stdClob.getLength();
        } else {
            throw new IllegalArgumentException("Unsupported clob type: " + clob.getClass().getName());
        }
    }

    /**
     * Reads a CLOB reference from the given result set column.
     *
     * <p>Only the reference is materialized; the content is loaded lazily through
     * {@link #readClobData(Connection, PortableClob)}.
     *
     * @param connection  the JDBC connection
     * @param resultSet   the result set positioned on the current row
     * @param columnIndex the 1-based column index
     * @return the CLOB referenced by the column (empty handle when the column is NULL)
     * @throws SQLException if a database access error occurs
     */
    default PortableClob readClobCell(@NonNull Connection connection, @NonNull ResultSet resultSet, int columnIndex) throws SQLException {
        return new StandardPortableClob(resultSet.getClob(columnIndex));
    }

    /**
     * Indicates whether CLOBs created by this dialect require explicit deletion via
     * {@link #deleteClob(Connection, PortableClob)} to release underlying database
     * resources when the owning entity is deleted (e.g. PostgreSQL Large Objects).
     *
     * @return {@code true} if manual deletion is required, {@code false} otherwise (default)
     */
    default boolean requiresManualClobDeletion() {
        return false;
    }

    /**
     * Releases any database resources associated with the given CLOB.
     *
     * <p>Only called when {@link #requiresManualClobDeletion()} returns {@code true};
     * the default implementation does nothing.
     *
     * @param connection the JDBC connection
     * @param blob       the CLOB to delete
     * @throws SQLException if the deletion fails
     */
    default void deleteClob(@NonNull Connection connection, @NonNull PortableClob blob) throws SQLException {
    }

    /**
     * Binds a query parameter to the given prepared statement at the specified index.
     * <p>
     * If the parameter value is {@code null}, {@link PreparedStatement#setNull} is used
     * with the parameter's SQL type; otherwise {@link PreparedStatement#setObject} is called.
     *
     * @param connection     the JDBC connection (may be needed by dialect-specific overrides)
     * @param ps             the prepared statement to bind the parameter to
     * @param parameterIndex the 1-based parameter index
     * @param param          the parameter descriptor containing the value and SQL type
     * @throws SQLException if a database access error occurs
     */
    default void setStatementParameter(@NonNull Connection connection, @NonNull PreparedStatement ps, int parameterIndex, @NonNull SqlQuery.Param param) throws SQLException {
        if (param.getValue() == null) {
            ps.setNull(parameterIndex, param.getSqlType());
        } else {
            ps.setObject(parameterIndex, param.getValue(), param.getSqlType());
        }
    }

    /**
     * Binds a BLOB parameter to the given prepared statement at the specified index.
     *
     * @param connection     the JDBC connection (may be needed by dialect-specific overrides)
     * @param ps             the prepared statement to bind the BLOB to
     * @param parameterIndex the 1-based parameter index
     * @param blob           the portable BLOB to bind
     * @throws SQLException             if a database access error occurs
     * @throws IllegalArgumentException if the BLOB type is not supported by this dialect
     */
    default void setStatementBlobParameter(@NonNull Connection connection, @NonNull PreparedStatement ps, int parameterIndex, @NonNull PortableBlob blob) throws SQLException {
        if (blob instanceof StandardPortableBlob stdBlob) {
            stdBlob.setStatementBlobParameter(ps, parameterIndex);
        } else {
            throw new IllegalArgumentException("Unsupported blob type: " + blob.getClass().getName());
        }
    }

    /**
     * Binds a CLOB parameter to the given prepared statement at the specified index.
     *
     * @param connection     the JDBC connection (may be needed by dialect-specific overrides)
     * @param ps             the prepared statement to bind the CLOB to
     * @param parameterIndex the 1-based parameter index
     * @param clob           the portable CLOB to bind
     * @throws SQLException             if a database access error occurs
     * @throws IllegalArgumentException if the CLOB type is not supported by this dialect
     */
    default void setStatementClobParameter(@NonNull Connection connection, @NonNull PreparedStatement ps, int parameterIndex, @NonNull PortableClob clob) throws SQLException {
        if (clob instanceof StandardPortableClob stdClob) {
            stdClob.setStatementBlobParameter(ps, parameterIndex);
        } else {
            throw new IllegalArgumentException("Unsupported clob type: " + clob.getClass().getName());
        }
    }

    /**
     * Returns a map of column labels to their 1-based indices from the given result set.
     *
     * @param connection the JDBC connection (may be needed by dialect-specific overrides)
     * @param resultSet  the result set whose column metadata to inspect
     * @return an ordered map where keys are column labels and values are 1-based column indices
     * @throws SQLException if a database access error occurs
     */
    default Map<String, Integer> getColumnsLabels(@NonNull Connection connection, @NonNull ResultSet resultSet) throws SQLException {
        return ResultSetUtils.getLabelColumnIndexMap(resultSet);
    }

    /**
     * Reads a typed value from the specified column of the current result set row.
     *
     * @param connection  the JDBC connection (may be needed by dialect-specific overrides)
     * @param resultSet   the result set positioned on the row to read
     * @param columnIndex the 1-based column index
     * @param clazz       the expected Java type of the column value
     * @param <T>         the value type
     * @return the column value cast to the requested type, or {@code null} if the value is SQL NULL
     * @throws SQLException if a database access error occurs
     */
    default <T> T readCell(@NonNull Connection connection, @NonNull ResultSet resultSet, int columnIndex, @NonNull Class<? extends T> clazz) throws SQLException {
        return ResultSetUtils.getObject(resultSet, columnIndex, clazz);
    }
}
