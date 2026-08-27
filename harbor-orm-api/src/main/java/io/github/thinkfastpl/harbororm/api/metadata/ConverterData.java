// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import io.github.thinkfastpl.harbororm.api.converter.AttributeConverter;
import lombok.NonNull;
import lombok.Value;

/**
 * Immutable value class holding metadata about an {@link AttributeConverter} attached to a column.
 *
 * <p>Stores either the converter class (for deferred instantiation via {@code AttributeConverterSupplier})
 * or a pre-built converter instance (for built-in converters like enum mappers). Exactly one of
 * {@link #converterClass} or {@link #converterInstance} will be non-null.
 *
 * @see io.github.thinkfastpl.harbororm.api.converter.AttributeConverter
 * @see QColumn
 * @see QColumnConverted
 */
@Value
public class ConverterData {

    /** The converter class for deferred instantiation, or {@code null} if an instance is provided. */
    Class<? extends AttributeConverter<?, ?>> converterClass;

    /** A pre-built converter instance, or {@code null} if a class is provided for deferred instantiation. */
    AttributeConverter<?, ?> converterInstance;

    /** The entity-side Java type that the converter handles. */
    Class<?> converterClassEntityType;

    /** The database-side Java type that the converter produces. */
    Class<?> converterClassDbType;

    /**
     * Creates converter data from a converter class for deferred instantiation.
     *
     * @param converterClass          the converter class
     * @param converterClassEntityType the entity-side Java type
     * @param converterClassDbType     the database-side Java type
     */
    public ConverterData(@NonNull Class<? extends AttributeConverter<?, ?>> converterClass, @NonNull Class<?> converterClassEntityType, @NonNull Class<?> converterClassDbType) {
        this.converterClass = converterClass;
        this.converterInstance = null;
        this.converterClassEntityType = converterClassEntityType;
        this.converterClassDbType = converterClassDbType;
    }

    /**
     * Creates converter data from a pre-built converter instance.
     *
     * @param converterInstance        the converter instance
     * @param converterClassEntityType the entity-side Java type
     * @param converterClassDbType     the database-side Java type
     */
    public ConverterData(@NonNull AttributeConverter<?, ?> converterInstance, @NonNull Class<?> converterClassEntityType, @NonNull Class<?> converterClassDbType) {
        this.converterClass = null;
        this.converterInstance = converterInstance;
        this.converterClassEntityType = converterClassEntityType;
        this.converterClassDbType = converterClassDbType;
    }
}
