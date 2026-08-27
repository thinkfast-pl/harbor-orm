// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import io.github.thinkfastpl.harbororm.api.dialect.StandardDialects;

import java.lang.annotation.*;

/**
 * Overrides the database column type for a field.
 *
 * <p>Use this annotation to specify a database-specific type that differs from
 * the default mapping. Multiple {@code @Type} annotations can be applied to
 * support different databases, with the most specific dialect match being used.
 *
 * <p>This annotation is repeatable. When multiple types are specified, they are
 * automatically grouped into {@link Types}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @Column(nullable = false)
 * @Type(dialect = StandardDialects.POSTGRES, columnType = "jsonb")
 * @Type(dialect = StandardDialects.ANY, columnType = "text")
 * private String metadata;
 * }</pre>
 *
 * @see Types
 * @see StandardDialects
 */
@Repeatable(Types.class)
@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Type {

    /**
     * The database dialect this type applies to.
     * Use {@link StandardDialects#ANY} for all databases.
     *
     * @return the dialect identifier
     */
    String dialect() default StandardDialects.ANY;

    /**
     * The database column type name.
     *
     * @return the column type
     */
    String columnType();
}
