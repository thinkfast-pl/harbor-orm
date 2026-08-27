// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Overrides the column mapping for a field in an {@link Embeddable} component.
 *
 * <p>When embedding the same embeddable class multiple times, or when the default
 * column names conflict with existing columns, use this annotation to specify
 * custom column mappings for individual fields.
 *
 * <p>This annotation is repeatable. Multiple overrides can be specified directly
 * on a field, or grouped using {@link AttributeOverrides}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @Embedded
 * @AttributeOverride(name = "street", column = @Column(name = "home_street", nullable = false))
 * @AttributeOverride(name = "city", column = @Column(name = "home_city", nullable = false))
 * private Address homeAddress;
 * }</pre>
 *
 * @see AttributeOverrides
 * @see Embedded
 * @see Column
 */
@Repeatable(AttributeOverrides.class)
@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AttributeOverride {

    /**
     * The name of the field in the embeddable class to override.
     *
     * @return the field name
     */
    String name();

    /**
     * The {@link Column} annotation specifying the new column mapping.
     *
     * @return the column mapping
     */
    Column column();
}
