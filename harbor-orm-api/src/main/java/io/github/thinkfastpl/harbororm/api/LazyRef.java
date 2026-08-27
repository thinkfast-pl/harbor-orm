// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api;

import java.util.Optional;

/**
 * A lazy reference to a related entity loaded on first access.
 *
 * <p>Used with {@code @OneToOne} relationships. The referenced entity is loaded
 * lazily when {@link #get()} or {@link #toOptional()} is first called, with
 * batch loading for efficiency (up to 50 parents per query).
 *
 * @param <T> the type of the referenced entity
 */
public interface LazyRef<T> {

    /**
     * Returns the referenced entity, or {@code null} if no related entity exists.
     */
    T get();

    /**
     * Returns the referenced entity wrapped in an {@link Optional}.
     */
    Optional<T> toOptional();

    /**
     * Creates a non-lazy reference holding the given value.
     * Useful for setting values on entities before insert or update.
     *
     * @param value the entity value (may be null)
     * @return a LazyRef holding the given value
     */
    static <T> LazyRef<T> of(T value) {
        return new LazyRef<>() {
            @Override
            public T get() {
                return value;
            }

            @Override
            public Optional<T> toOptional() {
                return Optional.ofNullable(value);
            }
        };
    }
}
