// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.lob;

import java.io.InputStream;

/**
 * Portable handle for large binary data (BLOB) that works across all supported databases.
 *
 * <p>A {@code PortableBlob} is a lightweight reference to binary content stored in the
 * database — it does not expose the content itself. All content operations go through
 * {@link io.github.thinkfastpl.harbororm.api.HarborSession}:
 * <ul>
 *   <li>{@link io.github.thinkfastpl.harbororm.api.HarborSession#createBlob(InputStream, long) createBlob} — create a new BLOB</li>
 *   <li>{@link io.github.thinkfastpl.harbororm.api.HarborSession#readBlobAllBytes(PortableBlob) readBlobAllBytes} /
 *       {@link io.github.thinkfastpl.harbororm.api.HarborSession#readBlobData(PortableBlob) readBlobData} — read content (fully or streaming)</li>
 *   <li>{@link io.github.thinkfastpl.harbororm.api.HarborSession#updateBlob(PortableBlob, InputStream, long) updateBlob} — replace content</li>
 *   <li>{@link io.github.thinkfastpl.harbororm.api.HarborSession#clearBlob(PortableBlob) clearBlob} — remove content</li>
 *   <li>{@link io.github.thinkfastpl.harbororm.api.HarborSession#getBlobLength(PortableBlob) getBlobLength} — content length in bytes</li>
 * </ul>
 *
 * <p>When the owning entity is fetched, only the BLOB reference is loaded; actual content
 * loading depends on the database dialect (e.g. PostgreSQL stores a Large Object OID and
 * streams the content on demand).
 *
 * <h3>Example</h3>
 * <pre>{@code
 * // Create and persist a blob
 * byte[] data = ...;
 * PortableBlob blob = session.createBlob(new ByteArrayInputStream(data), data.length);
 * session.insertEntity(qImage, new ImageEntity(1L, "photo.png", blob));
 *
 * // Read blob content
 * ImageEntity loaded = session.selectEntity(qImage).whereIdEq(1L).fetchSingle();
 * byte[] content = session.readBlobAllBytes(loaded.getData());
 * }</pre>
 *
 * @see PortableClob
 * @see io.github.thinkfastpl.harbororm.api.HarborSession#createBlob(InputStream, long)
 */
public interface PortableBlob {

    /**
     * Returns whether this BLOB contains data (is not null/empty).
     *
     * @return {@code true} if this BLOB has content, {@code false} otherwise
     */
    boolean isPresent();

    /**
     * Returns whether this BLOB is empty (has no content).
     *
     * <p>Inverse of {@link #isPresent()}.
     *
     * @return {@code true} if this BLOB has no content, {@code false} otherwise
     */
    default boolean isEmpty() {
        return !this.isPresent();
    }
}
