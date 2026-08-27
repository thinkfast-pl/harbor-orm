// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import io.github.thinkfastpl.harbororm.api.dialect.StandardDialects;
import io.github.thinkfastpl.harbororm.api.sql.SqlTypeHandler;

import java.lang.annotation.*;

/**
 * Specifies a custom type handler for reading and writing a field's value via JDBC.
 *
 * <p>Type handlers provide low-level control over how values are set on
 * {@link java.sql.PreparedStatement} and read from {@link java.sql.ResultSet}.
 *
 * <p>This annotation is repeatable — declare multiple handlers with different
 * {@link #dialect()} values to use different implementations per database.
 * Resolution order: dialect-specific first, then {@link StandardDialects#ANY} fallback,
 * then default JDBC handling.
 *
 * <p><strong>Mutually exclusive</strong> with {@link Convert} and {@link Enumerated}.
 * Using both on the same field causes a compile-time error.
 *
 * @see Convert
 * @see SqlTypeHandler
 */
@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(TypeHandlers.class)
@Documented
public @interface TypeHandler {

    /**
     * The dialect this handler applies to.
     * Defaults to {@link StandardDialects#ANY} (all databases).
     */
    String dialect() default StandardDialects.ANY;

    /**
     * The type handler implementation class.
     */
    Class<? extends SqlTypeHandler<?>> value();
}
