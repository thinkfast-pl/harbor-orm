// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect;

import io.github.thinkfastpl.harbororm.api.lob.PortableBlob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

/**
 * PostgreSQL-specific {@link PortableBlob} backed by a Large Object referenced by OID.
 */
@Slf4j
class PostgreSqlPortableBlob implements PortableBlob {

    private Long largeObjectOid;

    PostgreSqlPortableBlob() {
    }

    PostgreSqlPortableBlob(Long largeObjectOid) {
        this.largeObjectOid = largeObjectOid;
    }

    @Override
    public boolean isPresent() {
        return largeObjectOid != null;
    }

    Optional<InputStream> getInputStream(PGConnection connection) throws SQLException {
        if (this.largeObjectOid == null) {
            return Optional.empty();
        }
        return Optional.of(new LargeObjectInputStream(connection.getLargeObjectAPI().open(this.largeObjectOid, LargeObjectManager.READ)));
    }

    long getLength(PGConnection connection) throws SQLException {
        if (this.largeObjectOid == null) {
            return 0;
        }

        try (LargeObject largeObject = connection.getLargeObjectAPI().open(this.largeObjectOid, LargeObjectManager.READ)) {
            return largeObject.size64();
        }
    }

    void clear(PGConnection connection) throws SQLException {
        if (largeObjectOid != null) {
            connection.getLargeObjectAPI().delete(largeObjectOid);
            largeObjectOid = null;
        }
    }

    void updateBlob(PGConnection connection, InputStream inputStream) throws SQLException, IOException {
        clear(connection);

        final LargeObjectManager largeObjectAPI = connection.getLargeObjectAPI();

        final long newOid = largeObjectAPI.createLO();

        try (
                LargeObject largeObject = largeObjectAPI.open(newOid, LargeObjectManager.WRITE);
                OutputStream outputStream = largeObject.getOutputStream()
        ) {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            largeObjectAPI.delete(newOid);
            throw e;
        }

        this.largeObjectOid = newOid;
    }

    void setStatementParameter(PreparedStatement ps, int parameterIndex) throws SQLException {
        if (largeObjectOid == null) {
            ps.setNull(parameterIndex, Types.BIGINT);
        } else {
            ps.setLong(parameterIndex, largeObjectOid);
        }
    }
}
