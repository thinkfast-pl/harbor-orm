// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect;

import io.github.thinkfastpl.harbororm.api.lob.PortableClob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

@Slf4j
class PostgreSqlPortableClob implements PortableClob {

    private Long largeObjectOid;

    PostgreSqlPortableClob() {
    }

    PostgreSqlPortableClob(Long largeObjectOid) {
        this.largeObjectOid = largeObjectOid;
    }

    @Override
    public boolean isPresent() {
        return largeObjectOid != null;
    }

    Optional<Reader> getReader(PGConnection connection) throws SQLException {
        if (this.largeObjectOid == null) {
            return Optional.empty();
        }

        return Optional.of(new InputStreamReader(
                new LargeObjectInputStream(connection.getLargeObjectAPI().open(this.largeObjectOid, LargeObjectManager.READ)),
                StandardCharsets.UTF_8
        ));
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

    void updateClob(PGConnection connection, Reader reader) throws SQLException, IOException {
        clear(connection);

        final LargeObjectManager largeObjectAPI = connection.getLargeObjectAPI();

        final long newOid = largeObjectAPI.createLO();

        try (
                LargeObject largeObject = largeObjectAPI.open(newOid, LargeObjectManager.WRITE);
                OutputStream outputStream = largeObject.getOutputStream()
        ) {
            IOUtils.copy(reader, outputStream, StandardCharsets.UTF_8);
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
