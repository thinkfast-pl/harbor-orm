// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.api.lob.PortableClob;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.sql.*;
import java.util.Optional;

/**
 * Default {@link PortableClob} implementation backed by a JDBC {@link Clob}.
 */
@Slf4j
class StandardPortableClob implements PortableClob {

    private Clob clob;

    StandardPortableClob() {
    }

    StandardPortableClob(Clob clob) {
        this.clob = clob;
    }

    @Override
    public boolean isPresent() {
        return this.clob != null;
    }

    void set(Connection connection, Reader reader) throws SQLException, IOException {
        if (this.clob != null) {
            this.clob.free();
            this.clob = null;
        }

        final Clob newClob = connection.createClob();
        try (Writer writer = newClob.setCharacterStream(1)) {
            IOUtils.copy(reader, writer);
        }
        this.clob = newClob;
    }

    Optional<Reader> getReader() throws SQLException {
        if (this.clob == null) {
            return Optional.empty();
        }

        return Optional.of(this.clob.getCharacterStream());
    }

    long getLength() throws SQLException {
        if (this.clob == null) {
            return 0;
        }
        return this.clob.length();
    }

    void clear() throws SQLException {
        if (this.clob != null) {
            this.clob.free();
            this.clob = null;
        }
    }

    void setStatementBlobParameter(@NonNull PreparedStatement ps, int parameterIndex) throws SQLException {
        if (this.clob == null) {
            ps.setNull(parameterIndex, Types.CLOB);
        } else {
            ps.setClob(parameterIndex, this.clob);
        }
    }
}
