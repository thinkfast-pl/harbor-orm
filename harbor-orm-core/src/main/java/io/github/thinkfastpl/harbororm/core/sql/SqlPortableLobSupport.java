// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.api.lob.PortableBlob;
import io.github.thinkfastpl.harbororm.api.lob.PortableClob;
import io.github.thinkfastpl.harbororm.api.lob.PortableLobSupport;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Optional;

/**
 * {@link PortableLobSupport} implementation that delegates all BLOB and CLOB lifecycle
 * operations (create, read, update, clear, delete) to the database-specific
 * {@link RdbmsSupport}, acquiring connections via a {@link SqlConnectionAccessor}.
 */
@RequiredArgsConstructor
public class SqlPortableLobSupport implements PortableLobSupport {

    @NonNull
    private final SqlConnectionAccessor connectionAccessor;

    @NonNull
    private final RdbmsSupport rdbmsSupport;

    @Override
    public PortableBlob createBlob(@NonNull InputStream inputStream, long length) {
        return connectionAccessor.execute(connection -> {
            try {
                return rdbmsSupport.createBlob(connection, inputStream, length);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void updateBlob(@NonNull PortableBlob blob, @NonNull InputStream inputStream, long length) {
        connectionAccessor.execute(connection -> {
            try {
                rdbmsSupport.updateBlob(connection, blob, inputStream, length);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @Override
    public void clearBlob(@NonNull PortableBlob blob) {
        connectionAccessor.execute(connection -> {
            rdbmsSupport.clearBlob(connection, blob);
            return null;
        });
    }

    @Override
    public Optional<InputStream> readBlobData(@NonNull PortableBlob blob) {
        return connectionAccessor.execute(connection -> rdbmsSupport.readBlobData(connection, blob));
    }

    @Override
    public long getBlobLength(@NonNull PortableBlob blob) {
        return connectionAccessor.execute(connection -> rdbmsSupport.getBlobLength(connection, blob));
    }

    @Override
    public boolean requiresManualBlobDeletion() {
        return rdbmsSupport.requiresManualBlobDeletion();
    }

    @Override
    public void deleteBlob(@NonNull PortableBlob blob) {
        connectionAccessor.execute(connection -> {
            rdbmsSupport.deleteBlob(connection, blob);
            return null;
        });
    }

    @Override
    public PortableClob createClob(@NonNull Reader reader, long length) {
        return connectionAccessor.execute(connection -> {
            try {
                return rdbmsSupport.createClob(connection, reader, length);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void updateClob(@NonNull PortableClob clob, @NonNull Reader reader, long length) {
        connectionAccessor.execute(connection -> {
            try {
                rdbmsSupport.updateClob(connection, clob, reader, length);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @Override
    public void clearClob(@NonNull PortableClob clob) {
        connectionAccessor.execute(connection -> {
            rdbmsSupport.clearClob(connection, clob);
            return null;
        });
    }

    @Override
    public Optional<Reader> readClobData(@NonNull PortableClob clob) {
        return connectionAccessor.execute(connection -> rdbmsSupport.readClobData(connection, clob));
    }

    @Override
    public long getClobLength(@NonNull PortableClob clob) {
        return connectionAccessor.execute(connection -> rdbmsSupport.getClobLength(connection, clob));
    }

    @Override
    public boolean requiresManualClobDeletion() {
        return rdbmsSupport.requiresManualClobDeletion();
    }

    @Override
    public void deleteClob(@NonNull PortableClob clob) {
        connectionAccessor.execute(connection -> {
            rdbmsSupport.deleteClob(connection, clob);
            return null;
        });
    }
}
