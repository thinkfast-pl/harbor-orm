// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect;

import io.github.thinkfastpl.harbororm.api.lob.PortableBlob;
import io.github.thinkfastpl.harbororm.api.lob.PortableClob;
import io.github.thinkfastpl.harbororm.core.sql.RdbmsSupport;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlDialect;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;
import lombok.NonNull;
import org.postgresql.jdbc.PgConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * PostgreSQL {@link RdbmsSupport} implementation.
 * <p>
 * Overrides the default JDBC LOB handling to use PostgreSQL Large Objects (OIDs)
 * via {@link PgConnection}. BLOBs and CLOBs are stored as Large Objects and
 * require explicit deletion ({@link #requiresManualBlobDeletion()} and
 * {@link #requiresManualClobDeletion()} both return {@code true}).
 * <p>
 * Large Object content is streamed on demand through the Large Object API — fetching
 * an entity loads only the OID reference. Large Object access requires an active
 * transaction, so LOB operations must run on a connection with {@code autoCommit=false}.
 */
public class PostgreSqlRdbmsSupport implements RdbmsSupport {

    /**
     * {@inheritDoc}
     */
    @Override
    public SqlDialect createDialect() {
        return new PostgreSqlDialect();
    }

    @Override
    public PortableBlob createBlob(@NonNull Connection connection, @NonNull InputStream inputStream, long length) throws SQLException, IOException {
        PostgreSqlPortableBlob blob = new PostgreSqlPortableBlob();
        updateBlob(connection, blob, inputStream, length);
        return blob;
    }

    @Override
    public PortableBlob createBlob(@NonNull Connection connection) throws SQLException {
        return new PostgreSqlPortableBlob();
    }

    @Override
    public void updateBlob(@NonNull Connection connection, @NonNull PortableBlob blob, @NonNull InputStream inputStream, long length) throws SQLException, IOException {
        asPgBlob(blob).updateBlob(asPgConnection(connection), inputStream);
    }

    @Override
    public void clearBlob(@NonNull Connection connection, @NonNull PortableBlob blob) throws SQLException {
        asPgBlob(blob).clear(asPgConnection(connection));
    }

    @Override
    public Optional<InputStream> readBlobData(@NonNull Connection connection, @NonNull PortableBlob blob) throws SQLException {
        return asPgBlob(blob).getInputStream(asPgConnection(connection));
    }

    @Override
    public long getBlobLength(@NonNull Connection connection, @NonNull PortableBlob blob) throws SQLException {
        return asPgBlob(blob).getLength(asPgConnection(connection));
    }

    @Override
    public boolean requiresManualBlobDeletion() {
        return true;
    }

    /**
     * Deletes the Large Object backing the given BLOB, if it exists.
     *
     * @param connection the JDBC connection (must be a {@link PgConnection})
     * @param blob       the BLOB to delete
     * @throws SQLException if the deletion fails
     */
    @Override
    public void deleteBlob(@NonNull Connection connection, @NonNull PortableBlob blob) throws SQLException {
        asPgBlob(blob).clear(asPgConnection(connection));
    }

    @Override
    public PortableClob createClob(@NonNull Connection connection, @NonNull Reader reader, long length) throws SQLException, IOException {
        PostgreSqlPortableClob clob = new PostgreSqlPortableClob();
        updateClob(connection, clob, reader, length);
        return clob;
    }

    @Override
    public PortableClob createClob(@NonNull Connection connection) throws SQLException {
        return new PostgreSqlPortableClob();
    }

    @Override
    public void updateClob(@NonNull Connection connection, @NonNull PortableClob clob, @NonNull Reader reader, long length) throws SQLException, IOException {
        asPgClob(clob).updateClob(asPgConnection(connection), reader);
    }

    @Override
    public void clearClob(@NonNull Connection connection, @NonNull PortableClob clob) throws SQLException {
        asPgClob(clob).clear(asPgConnection(connection));
    }

    @Override
    public Optional<Reader> readClobData(@NonNull Connection connection, @NonNull PortableClob clob) throws SQLException {
        return asPgClob(clob).getReader(asPgConnection(connection));
    }

    @Override
    public long getClobLength(@NonNull Connection connection, @NonNull PortableClob clob) throws SQLException {
        return asPgClob(clob).getLength(asPgConnection(connection));
    }

    @Override
    public boolean requiresManualClobDeletion() {
        return true;
    }

    /**
     * Deletes the Large Object backing the given CLOB, if it exists.
     *
     * @param connection the JDBC connection (must be a {@link PgConnection})
     * @param clob       the CLOB to delete
     * @throws SQLException if the deletion fails
     */
    @Override
    public void deleteClob(@NonNull Connection connection, @NonNull PortableClob clob) throws SQLException {
        asPgClob(clob).clear(asPgConnection(connection));
    }


    @Override
    public void setStatementParameter(@NonNull Connection connection, @NonNull PreparedStatement ps, int parameterIndex, @NonNull SqlQuery.Param param) throws SQLException {
        if (param.getValue() instanceof PostgreSqlArrayValue arrayValue) {
            java.sql.Array sqlArray = connection.createArrayOf(arrayValue.getPgTypeName(), arrayValue.getValues());
            ps.setArray(parameterIndex, sqlArray);
        } else {
            RdbmsSupport.super.setStatementParameter(connection, ps, parameterIndex, param);
        }
    }

    @Override
    public void setStatementBlobParameter(@NonNull Connection connection, @NonNull PreparedStatement ps, int parameterIndex, @NonNull PortableBlob blob) throws SQLException {
        asPgBlob(blob).setStatementParameter(ps, parameterIndex);
    }

    @Override
    public void setStatementClobParameter(@NonNull Connection connection, @NonNull PreparedStatement ps, int parameterIndex, @NonNull PortableClob clob) throws SQLException {
        asPgClob(clob).setStatementParameter(ps, parameterIndex);
    }

    @Override
    public PortableBlob readBlobCell(@NonNull Connection connection, @NonNull ResultSet resultSet, int columnIndex) throws SQLException {
        long oid = resultSet.getLong(columnIndex);
        return new PostgreSqlPortableBlob(resultSet.wasNull() ? null : oid);
    }

    @Override
    public PortableClob readClobCell(@NonNull Connection connection, @NonNull ResultSet resultSet, int columnIndex) throws SQLException {
        long oidValue = resultSet.getLong(columnIndex);
        return new PostgreSqlPortableClob(resultSet.wasNull() ? null : oidValue);
    }

    private static PgConnection asPgConnection(Connection connection) {
        if (connection instanceof PgConnection pgConnection) {
            return pgConnection;
        }
        try {
            if (connection.isWrapperFor(PgConnection.class)) {
                return connection.unwrap(PgConnection.class);
            }
        } catch (SQLException e) {
            // fall through to exception below
        }
        throw new IllegalArgumentException("connection of type " + PgConnection.class.getName() + " was expected");
    }

    private PostgreSqlPortableBlob asPgBlob(PortableBlob blob) {
        if (blob instanceof PostgreSqlPortableBlob pgBlob) {
            return pgBlob;
        } else {
            throw new IllegalArgumentException("Unsupported portable blob type: " + blob.getClass().getName());
        }
    }

    private PostgreSqlPortableClob asPgClob(PortableClob clob) {
        if (clob instanceof PostgreSqlPortableClob pgClob) {
            return pgClob;
        } else {
            throw new IllegalArgumentException("Unsupported portable clob type: " + clob.getClass().getName());
        }
    }
}
