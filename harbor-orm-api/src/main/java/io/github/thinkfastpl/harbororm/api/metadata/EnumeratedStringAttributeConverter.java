// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import io.github.thinkfastpl.harbororm.api.converter.AttributeConverter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Built-in {@link AttributeConverter} that maps Java enum values to their string names.
 *
 * <p>Used internally when a column is annotated with {@code @Enumerated(EnumMappingType.STRING)}
 * or {@code @Enumerated} with no explicit type (STRING is the default). Converts enums to
 * {@link String} via {@link Enum#name()} for database storage and back via {@link Enum#valueOf}.
 * Handles {@code null} values in both directions.
 *
 * @param <T> the enum type
 * @see EnumMappingType#STRING
 * @see EnumeratedOrdinalAttributeConverter
 */
@RequiredArgsConstructor
class EnumeratedStringAttributeConverter<T extends Enum<T>> implements AttributeConverter<T, String> {

    @NonNull
    private final Class<T> enumClass;

    @Override
    public String convertToDatabaseColumn(T enumValue) {
        if (enumValue == null) {
            return null;
        }
        return enumValue.name();
    }

    @Override
    public T convertToEntityAttribute(String enumString) {
        if (enumString == null) {
            return null;
        }
        return Enum.valueOf(enumClass, enumString);
    }
}
