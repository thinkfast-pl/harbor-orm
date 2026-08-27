// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Specifies a foreign key column for a relationship.
 *
 * <p>Used within {@link OneToMany} and {@link ElementCollection} to define how
 * the child table references the parent entity. The column specified by {@code name}
 * exists in the child table and contains values matching the parent's ID column.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @OneToMany(joinColumns = @JoinColumn(
 *     name = "order_id",
 *     fieldType = Long.class,
 *     referencedColumnName = "id"
 * ))
 * private List<OrderItemEntity> items;
 * }</pre>
 *
 * @see OneToMany
 * @see ElementCollection
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JoinColumn {

    /**
     * The name of the foreign key column in the child table.
     *
     * @return the column name
     */
    String name();

    /**
     * The Java type of the foreign key. Must match the parent entity's ID type.
     *
     * @return the field type class
     */
    Class<?> fieldType();

    /**
     * The column in the parent table being referenced. Defaults to the parent's ID column.
     *
     * @return the referenced column name, or empty string for the ID column
     */
    String referencedColumnName() default "";

    /**
     * Whether this column is included in INSERT statements.
     *
     * @return {@code true} to include in INSERT, {@code false} to exclude
     */
    boolean insertable() default true;

    /**
     * Whether this column is included in UPDATE statements.
     *
     * @return {@code true} to include in UPDATE, {@code false} to exclude
     */
    boolean updatable() default true;

    /**
     * Whether the foreign key allows NULL values.
     *
     * @return {@code true} to allow NULL, {@code false} to require non-null
     */
    boolean nullable() default false;
}
