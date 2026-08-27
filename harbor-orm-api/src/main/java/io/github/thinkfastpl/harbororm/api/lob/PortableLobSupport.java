// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.lob;

import lombok.NonNull;

import java.io.InputStream;
import java.io.Reader;
import java.util.Optional;

/**
 * Internal factory interface for creating and managing {@link PortableBlob} and {@link PortableClob}
 * instances. Dialect-specific modules implement this interface to provide database-appropriate
 * LOB handling.
 *
 * <p>This interface is not intended for direct use by application code. LOBs should be created
 * through the {@link io.github.thinkfastpl.harbororm.api.HarborSession} API instead (e.g.
 * {@code session.createBlob(inputStream, length)} and {@code session.createClob(reader, length)}).
 *
 * @see PortableBlob
 * @see PortableClob
 * @see io.github.thinkfastpl.harbororm.api.HarborSession
 */
public interface PortableLobSupport {

    /**
     * Creates a new {@link PortableBlob} from the given input stream.
     *
     * @param inputStream the stream providing the binary data; never {@code null}
     * @param length      the number of bytes to read from the stream
     * @return a new blob instance containing the data
     */
    PortableBlob createBlob(@NonNull InputStream inputStream, long length);

    /**
     * Replaces the content of the given BLOB with data read from the input stream.
     *
     * <p>Depending on the dialect this may allocate new underlying database resources
     * (e.g. a new PostgreSQL Large Object); the entity holding the BLOB must be
     * re-saved afterwards to persist the new reference.
     *
     * @param blob        the blob to update; never {@code null}
     * @param inputStream the stream providing the new binary data; never {@code null}
     * @param length      the number of bytes to read from the stream
     */
    void updateBlob(@NonNull PortableBlob blob, @NonNull InputStream inputStream, long length);

    /**
     * Removes the content of the given BLOB, releasing its underlying database
     * resources. Afterwards {@link PortableBlob#isPresent()} returns {@code false}.
     *
     * @param blob the blob to clear; never {@code null}
     */
    void clearBlob(@NonNull PortableBlob blob);

    /**
     * Opens a stream over the content of the given BLOB.
     *
     * @param blob the blob to read; never {@code null}
     * @return a stream over the binary content, or {@link Optional#empty()} if the blob is empty
     */
    Optional<InputStream> readBlobData(@NonNull PortableBlob blob);

    /**
     * Returns the length of the given BLOB's content.
     *
     * @param blob the blob to measure; never {@code null}
     * @return the content length in bytes, or {@code 0} if the blob is empty
     */
    long getBlobLength(@NonNull PortableBlob blob);

    /**
     * Indicates whether blobs created by this factory require explicit deletion
     * via {@link #deleteBlob(PortableBlob)} to release underlying database resources.
     *
     * @return {@code true} if manual deletion is required, {@code false} otherwise
     */
    boolean requiresManualBlobDeletion();

    /**
     * Releases any database resources associated with the given blob.
     * Only called when {@link #requiresManualBlobDeletion()} returns {@code true}.
     *
     * @param blob the blob to delete; never {@code null}
     */
    void deleteBlob(@NonNull PortableBlob blob);

    /**
     * Creates a new {@link PortableClob} from the given reader.
     *
     * @param reader the reader providing the character data; never {@code null}
     * @param length the number of characters to read
     * @return a new clob instance containing the data
     */
    PortableClob createClob(@NonNull Reader reader, long length);

    /**
     * Indicates whether clobs created by this factory require explicit deletion
     * via {@link #deleteClob(PortableClob)} to release underlying database resources.
     *
     * @return {@code true} if manual deletion is required, {@code false} otherwise
     */
    boolean requiresManualClobDeletion();

    /**
     * Releases any database resources associated with the given clob.
     * Only called when {@link #requiresManualClobDeletion()} returns {@code true}.
     *
     * @param clob the clob to delete; never {@code null}
     */
    void deleteClob(@NonNull PortableClob clob);

    /**
     * Replaces the content of the given CLOB with data read from the reader.
     *
     * <p>Depending on the dialect this may allocate new underlying database resources
     * (e.g. a new PostgreSQL Large Object); the entity holding the CLOB must be
     * re-saved afterwards to persist the new reference.
     *
     * @param clob   the clob to update; never {@code null}
     * @param reader the reader providing the new character data; never {@code null}
     * @param length the number of characters to read
     */
    void updateClob(@NonNull PortableClob clob, @NonNull Reader reader, long length);

    /**
     * Removes the content of the given CLOB, releasing its underlying database
     * resources. Afterwards {@link PortableClob#isPresent()} returns {@code false}.
     *
     * @param clob the clob to clear; never {@code null}
     */
    void clearClob(@NonNull PortableClob clob);

    /**
     * Opens a reader over the content of the given CLOB.
     *
     * @param clob the clob to read; never {@code null}
     * @return a reader over the character content, or {@link Optional#empty()} if the clob is empty
     */
    Optional<Reader> readClobData(@NonNull PortableClob clob);

    /**
     * Returns the length of the given CLOB's content.
     *
     * @param clob the clob to measure; never {@code null}
     * @return the content length in characters, or {@code 0} if the clob is empty
     */
    long getClobLength(@NonNull PortableClob clob);
}
