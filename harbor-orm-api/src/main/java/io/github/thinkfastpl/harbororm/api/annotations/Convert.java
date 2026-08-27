// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import io.github.thinkfastpl.harbororm.api.converter.AttributeConverter;

import java.lang.annotation.*;

/**
 * Specifies a custom converter for mapping between a Java type and a database type.
 *
 * <p>Use this annotation when the default type mapping is insufficient, such as
 * storing booleans as 'Y'/'N' strings, encrypting values, or mapping custom types.
 *
 * <p>The converter class must implement {@link AttributeConverter}
 * and have a no-argument constructor. Converters are instantiated once and reused.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @Column(name = "active_flag", nullable = false)
 * @Convert(converter = BooleanToYesNoConverter.class)
 * private Boolean active;
 * }</pre>
 *
 * @see AttributeConverter
 */
@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Convert {

    /**
     * The converter class implementing {@link AttributeConverter}.
     *
     * @return the converter class
     */
    Class<? extends AttributeConverter<?, ?>> converter();
}
