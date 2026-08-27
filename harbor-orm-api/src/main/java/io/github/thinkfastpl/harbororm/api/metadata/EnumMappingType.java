// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

/**
 * Defines how Java enum values are mapped to database column values.
 *
 * <p>Used with the {@code @Enumerated} annotation on entity fields.
 *
 * @see io.github.thinkfastpl.harbororm.api.annotations.Enumerated
 */
public enum EnumMappingType {
    /**
     * Maps the enum's {@link Enum#name()} to a VARCHAR column.
     * This is the default mapping type and is safe against enum reordering.
     */
    STRING,

    /**
     * Maps the enum's {@link Enum#ordinal()} to an INT column.
     * More compact but will break if the enum declaration order changes.
     */
    ORDINAL,
}
