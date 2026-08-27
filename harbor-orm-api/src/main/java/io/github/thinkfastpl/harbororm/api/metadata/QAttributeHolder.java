// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import java.util.List;
import java.util.Optional;

/**
 * Interface for metadata objects that hold a collection of entity attributes and lifecycle callback methods.
 *
 * <p>Implemented by {@link QEntity} (for top-level entities) and {@link QEmbeddable} (for embedded
 * value objects). Provides access to all attributes and the names of lifecycle callback methods
 * annotated with {@code @PreInsert}, {@code @PreUpdate}, {@code @PreDelete}, {@code @PostInsert},
 * {@code @PostUpdate}, and {@code @PostDelete}.
 *
 * @see QEntity
 * @see QEmbeddable
 * @see QAttribute
 */
public interface QAttributeHolder {

    /**
     * Returns all attributes (columns, embeddables, relations, element collections) of this holder.
     *
     * @return an immutable list of all attributes
     */
    List<QAttribute> getAllAttributes();

    /**
     * Returns the name of the method annotated with {@code @PreInsert}, if any.
     *
     * @return the method name, or empty if no pre-insert callback is defined
     */
    Optional<String> getPreInsertMethodName();

    /**
     * Returns the name of the method annotated with {@code @PreUpdate}, if any.
     *
     * @return the method name, or empty if no pre-update callback is defined
     */
    Optional<String> getPreUpdateMethodName();

    /**
     * Returns the name of the method annotated with {@code @PreDelete}, if any.
     *
     * @return the method name, or empty if no pre-delete callback is defined
     */
    default Optional<String> getPreDeleteMethodName() { return Optional.empty(); }

    /**
     * Returns the name of the method annotated with {@code @PostInsert}, if any.
     *
     * @return the method name, or empty if no post-insert callback is defined
     */
    default Optional<String> getPostInsertMethodName() { return Optional.empty(); }

    /**
     * Returns the name of the method annotated with {@code @PostUpdate}, if any.
     *
     * @return the method name, or empty if no post-update callback is defined
     */
    default Optional<String> getPostUpdateMethodName() { return Optional.empty(); }

    /**
     * Returns the name of the method annotated with {@code @PostDelete}, if any.
     *
     * @return the method name, or empty if no post-delete callback is defined
     */
    default Optional<String> getPostDeleteMethodName() { return Optional.empty(); }
}
