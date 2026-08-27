// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain.converter;

import io.github.thinkfastpl.harbororm.api.converter.AttributeConverter;

/**
 * Converter that prepends a configurable prefix when writing to database
 * and strips it when reading back. Has no no-arg constructor — requires
 * a custom {@link io.github.thinkfastpl.harbororm.api.converter.AttributeConverterSupplier} to instantiate.
 */
public class PrefixStringConverter implements AttributeConverter<String, String> {

    private final String prefix;

    public PrefixStringConverter(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return prefix + attribute;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        if (dbData.startsWith(prefix)) {
            return dbData.substring(prefix.length());
        }
        return dbData;
    }
}
