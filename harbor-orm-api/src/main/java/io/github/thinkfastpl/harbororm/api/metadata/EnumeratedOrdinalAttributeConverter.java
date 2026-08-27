// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import io.github.thinkfastpl.harbororm.api.converter.AttributeConverter;
import lombok.NonNull;

/**
 * Built-in {@link AttributeConverter} that maps Java enum values to their ordinal integer positions.
 *
 * <p>Used internally when a column is annotated with {@code @Enumerated(EnumMappingType.ORDINAL)}.
 * Converts enums to {@link Integer} for database storage and back. Handles {@code null} values
 * in both directions.
 *
 * <p><strong>Warning:</strong> Ordinal mapping is sensitive to enum declaration order. Adding or
 * reordering constants will change the stored values and break existing data.
 *
 * @param <T> the enum type
 * @see EnumMappingType#ORDINAL
 * @see EnumeratedStringAttributeConverter
 */
class EnumeratedOrdinalAttributeConverter<T extends Enum<T>> implements AttributeConverter<T, Integer> {

    @NonNull
    private final T[] enumConstants;

    @NonNull
    private final Class<T> enumClass;

    EnumeratedOrdinalAttributeConverter(@NonNull Class<T> enumClass) {
        this.enumConstants = enumClass.getEnumConstants();
        this.enumClass = enumClass;
    }

    @Override
    public Integer convertToDatabaseColumn(T enumValue) {
        if (enumValue == null) {
            return null;
        }
        return enumValue.ordinal();
    }

    @Override
    public T convertToEntityAttribute(Integer enumOrdinal) {
        if (enumOrdinal == null) {
            return null;
        }
        if (enumOrdinal < 0 || enumOrdinal >= enumConstants.length) {
            throw new IllegalArgumentException(
                    "Invalid ordinal " + enumOrdinal + " for enum " + enumClass.getSimpleName()
                            + " (valid range: 0.." + (enumConstants.length - 1) + ")");
        }
        return enumConstants[enumOrdinal];
    }
}
