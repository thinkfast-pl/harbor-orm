// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.repository;

/**
 * Unchecked exception thrown when an entity lookup by primary key yields no result.
 *
 * <p>This exception is thrown by {@link CrudRepository#findByIdOrThrow(Object)} and
 * {@link CrudRepository#findByIdForUpdateOrThrow(Object)} when no entity with the
 * given ID exists in the database.
 *
 * <p>Example:
 * <pre>{@code
 * try {
 *     ProductEntity product = productRepository.findByIdOrThrow(42L);
 * } catch (EntityNotFoundException e) {
 *     // handle missing entity
 * }
 * }</pre>
 *
 * @see CrudRepository#findByIdOrThrow(Object)
 * @see CrudRepository#findByIdForUpdateOrThrow(Object)
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * Creates an exception with no message or cause.
     */
    public EntityNotFoundException() {
    }

    /**
     * Creates an exception with the specified detail message.
     *
     * @param message the detail message
     */
    public EntityNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates an exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an exception with the specified cause.
     *
     * @param cause the underlying cause
     */
    public EntityNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates an exception with the specified detail message, cause, and control flags.
     *
     * @param message the detail message
     * @param cause the underlying cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public EntityNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
