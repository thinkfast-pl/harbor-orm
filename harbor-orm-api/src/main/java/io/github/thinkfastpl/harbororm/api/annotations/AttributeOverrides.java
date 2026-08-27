// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Container annotation for multiple {@link AttributeOverride} annotations.
 *
 * <p>This annotation is used automatically by Java when multiple {@code @AttributeOverride}
 * annotations are applied to the same field. It can also be used explicitly for clarity.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @Embedded
 * @AttributeOverrides({
 *     @AttributeOverride(name = "street", column = @Column(name = "work_street", nullable = false)),
 *     @AttributeOverride(name = "postalCode", column = @Column(name = "work_postal_code", nullable = false)),
 *     @AttributeOverride(name = "city", column = @Column(name = "work_city", nullable = false))
 * })
 * private Address workAddress;
 * }</pre>
 *
 * @see AttributeOverride
 * @see Embedded
 */
@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AttributeOverrides {

    /**
     * The array of {@link AttributeOverride} annotations.
     *
     * @return the attribute overrides
     */
    AttributeOverride[] value();
}
