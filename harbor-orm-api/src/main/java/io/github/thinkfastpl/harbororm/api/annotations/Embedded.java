// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Specifies that a field holds an {@link Embeddable} component.
 *
 * <p>The embeddable's fields are mapped to columns in the owning entity's table.
 * Use {@code tableFieldNamePrefix} to add a prefix to generated column names,
 * or {@link AttributeOverride} for complete control over column mapping.
 *
 * <p>Can also be applied to {@link Id} fields to create composite primary keys.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @Entity(table = "customers")
 * public class CustomerEntity {
 *     @Id
 *     private Long id;
 *
 *     @Embedded(tableFieldNamePrefix = "billing_")
 *     private Address billingAddress;
 *
 *     @Embedded(tableFieldNamePrefix = "shipping_")
 *     private Address shippingAddress;
 * }
 * }</pre>
 *
 * @see Embeddable
 * @see AttributeOverride
 * @see AttributeOverrides
 */
@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Embedded {

    /**
     * Prefix prepended to column names of the embedded fields.
     *
     * @return the prefix, or empty string for no prefix
     */
    String tableFieldNamePrefix() default "";
}
