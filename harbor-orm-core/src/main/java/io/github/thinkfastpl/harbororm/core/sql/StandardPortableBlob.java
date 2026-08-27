// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.api.lob.PortableBlob;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.Optional;

/**
 * Default {@link PortableBlob} implementation backed by a JDBC {@link Blob}.
 */
@Slf4j
class StandardPortableBlob implements PortableBlob {

    private Blob blob;

    StandardPortableBlob() {
    }

    StandardPortableBlob(Blob blob) {
        this.blob = blob;
    }

    @Override
    public boolean isPresent() {
        return this.blob != null;
    }

    void set(Connection connection, InputStream inputStream) throws SQLException, IOException {
        if (this.blob != null) {
            this.blob.free();
            this.blob = null;
        }

        final Blob newBlob = connection.createBlob();
        try (OutputStream outputStream = newBlob.setBinaryStream(1)) {
            inputStream.transferTo(outputStream);
        }
        this.blob = newBlob;
    }

    public Optional<InputStream> getInputStream() throws SQLException {
        if (this.blob == null) {
            return Optional.empty();
        }

        return Optional.of(this.blob.getBinaryStream());
    }

    public long getLength() throws SQLException {
        if (this.blob == null) {
            return 0;
        }
        return this.blob.length();
    }

    void clear() throws SQLException {
        if (this.blob != null) {
            this.blob.free();
            this.blob = null;
        }
    }

    void setStatementBlobParameter(@NonNull PreparedStatement ps, int parameterIndex) throws SQLException {
        if (this.blob == null) {
            ps.setNull(parameterIndex, Types.BLOB);
        } else {
            ps.setBlob(parameterIndex, this.blob);
        }
    }
}
