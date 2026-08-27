// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.lob;

import java.io.Reader;

/**
 * Portable handle for large text data (CLOB) that works across all supported databases.
 *
 * <p>A {@code PortableClob} is a lightweight reference to character content stored in the
 * database — it does not expose the content itself. All content operations go through
 * {@link io.github.thinkfastpl.harbororm.api.HarborSession}:
 * <ul>
 *   <li>{@link io.github.thinkfastpl.harbororm.api.HarborSession#createClob(Reader, long) createClob} — create a new CLOB</li>
 *   <li>{@link io.github.thinkfastpl.harbororm.api.HarborSession#readClobAllChars(PortableClob) readClobAllChars} /
 *       {@link io.github.thinkfastpl.harbororm.api.HarborSession#readClobData(PortableClob) readClobData} — read content (fully or streaming)</li>
 *   <li>{@link io.github.thinkfastpl.harbororm.api.HarborSession#updateClob(PortableClob, Reader, long) updateClob} — replace content</li>
 *   <li>{@link io.github.thinkfastpl.harbororm.api.HarborSession#clearClob(PortableClob) clearClob} — remove content</li>
 *   <li>{@link io.github.thinkfastpl.harbororm.api.HarborSession#getClobLength(PortableClob) getClobLength} — content length in characters</li>
 * </ul>
 *
 * <p>When the owning entity is fetched, only the CLOB reference is loaded; actual content
 * loading depends on the database dialect (e.g. PostgreSQL stores a Large Object OID and
 * streams the content on demand).
 *
 * <h3>Example</h3>
 * <pre>{@code
 * // Create and persist a clob
 * String text = ...;
 * PortableClob clob = session.createClob(new StringReader(text), text.length());
 * session.insertEntity(qArticle, new ArticleEntity(1L, "My Article", clob));
 *
 * // Read clob content
 * ArticleEntity loaded = session.selectEntity(qArticle).whereIdEq(1L).fetchSingle();
 * String body = session.readClobAllChars(loaded.getBody());
 * }</pre>
 *
 * @see PortableBlob
 * @see io.github.thinkfastpl.harbororm.api.HarborSession#createClob(Reader, long)
 */
public interface PortableClob {

    /**
     * Returns whether this CLOB contains data (is not null/empty).
     *
     * @return {@code true} if this CLOB has content, {@code false} otherwise
     */
    boolean isPresent();

    /**
     * Returns whether this CLOB is empty (has no content).
     *
     * <p>Inverse of {@link #isPresent()}.
     *
     * @return {@code true} if this CLOB has no content, {@code false} otherwise
     */
    default boolean isEmpty() {
        return !this.isPresent();
    }
}
