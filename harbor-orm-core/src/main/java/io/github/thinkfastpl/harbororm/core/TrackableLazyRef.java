// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

/**
 * Internal interface for tracking modifications to a {@link io.github.thinkfastpl.harbororm.api.LazyRef}.
 * Used by {@link EntityHandler} to determine cascading behavior during update.
 */
interface TrackableLazyRef {

    /**
     * Returns true if the lazy ref was accessed (get() was called).
     */
    boolean isPotentiallyModified();
}
