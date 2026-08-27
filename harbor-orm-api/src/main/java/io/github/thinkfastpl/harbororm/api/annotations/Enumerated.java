// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import io.github.thinkfastpl.harbororm.api.metadata.EnumMappingType;

import java.lang.annotation.*;

/**
 * Specifies how a Java enum is mapped to a database column.
 *
 * <p>Enums can be stored as their string name ({@link EnumMappingType#STRING})
 * or their ordinal position ({@link EnumMappingType#ORDINAL}).
 * String mapping is recommended as it survives enum reordering.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @Column(nullable = false)
 * @Enumerated(EnumMappingType.STRING)
 * private Status status;
 * }</pre>
 *
 * <h2>PostgreSQL Custom Enum Types</h2>
 * <p>Combine with {@link Type} to use database-native enum types:
 * <pre>{@code
 * @Column(nullable = false)
 * @Enumerated
 * @Type(dialect = StandardDialects.POSTGRES, columnType = "account_status")
 * private Status status;
 * }</pre>
 *
 * @see EnumMappingType
 * @see Type
 */
@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Enumerated {

    /**
     * The enum mapping strategy.
     *
     * @return the mapping type, default is {@link EnumMappingType#STRING}
     */
    EnumMappingType value() default EnumMappingType.STRING;
}
