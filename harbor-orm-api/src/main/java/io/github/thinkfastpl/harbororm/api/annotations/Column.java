// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Maps an entity field to a database column.
 *
 * <p>When applied to a field, this annotation specifies how the field maps to
 * a database column. If omitted, the field is still mapped using default settings
 * and the column name is derived from the field name using the entity's
 * {@link Entity#columnNameStrategy()}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @Column(name = "display_name", nullable = true, updatable = false)
 * private String name;
 * }</pre>
 *
 * @see Entity#columnNameStrategy()
 * @see ColumnNameStrategy
 */
@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Column {

    /**
     * Custom column name. If empty, derived from field name using the entity's
     * column naming strategy.
     *
     * @return the column name, or empty string for auto-generated name
     */
    String name() default "";

    /**
     * Whether this column is included in SQL INSERT statements.
     *
     * @return {@code true} to include in INSERT, {@code false} to exclude
     */
    boolean insertable() default true;

    /**
     * Whether this column is included in SQL UPDATE statements.
     *
     * @return {@code true} to include in UPDATE, {@code false} to exclude
     */
    boolean updatable() default true;

    /**
     * Whether the column allows NULL values.
     *
     * @return {@code true} to allow NULL, {@code false} to require non-null
     */
    boolean nullable();
}
