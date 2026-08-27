// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Marks a class as an embeddable component that can be included in entities.
 *
 * <p>An embeddable class defines a group of fields that map to columns in the
 * owning entity's table. Unlike entities, embeddables have no identity of their
 * own and no separate table. They are used to model reusable value objects like
 * addresses, money amounts, or composite keys.
 *
 * <p>The annotation processor generates a {@code Q<ClassName>} metadata class
 * for type-safe access to the embeddable's fields.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @Embeddable
 * public class Address {
 *     @Column(nullable = false)
 *     private String street;
 *
 *     @Column(name = "postal_code", nullable = false)
 *     private String postalCode;
 *
 *     @Column(nullable = false)
 *     private String city;
 * }
 * }</pre>
 *
 * @see Embedded
 * @see AttributeOverride
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Embeddable {
}
