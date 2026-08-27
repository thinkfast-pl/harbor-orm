// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.exception;

import lombok.Getter;

/**
 * Thrown when an update or delete operation fails because the entity's version
 * does not match the database row. This indicates a concurrent modification.
 */
@Getter
public class OptimisticLockException extends RuntimeException {

    private final Class<?> entityType;
    private final Object id;
    private final Object version;

    public OptimisticLockException(Class<?> entityType, Object id, Object version) {
        super("Entity %s with ID %s was updated or deleted by another transaction (expected version %s)"
                .formatted(entityType.getSimpleName(), id, version));
        this.entityType = entityType;
        this.id = id;
        this.version = version;
    }
}
