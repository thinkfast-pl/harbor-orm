// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an entity field as a JSON column. The underlying database type is dialect-specific:
 * PostgreSQL uses {@code jsonb}, H2 uses {@code varchar}.
 *
 * <p>Annotating a field with {@code @Json} unlocks JSON-specific query operators in the DSL
 * (extraction, containment, key existence) via {@code QJsonColumn}.
 *
 * <h3>Supported field types</h3>
 * <ul>
 *   <li><b>{@code String}</b> -- stored as-is with no serialization. No additional configuration required.</li>
 *   <li><b>POJO</b> -- automatically serialized/deserialized using a {@link io.github.thinkfastpl.harbororm.api.converter.JsonSerializer}
 *       that must be provided when creating the {@link io.github.thinkfastpl.harbororm.api.HarborSession}.</li>
 *   <li><b>POJO with {@link Convert}</b> -- manual conversion via an {@link io.github.thinkfastpl.harbororm.api.converter.AttributeConverter}.
 *       No {@link io.github.thinkfastpl.harbororm.api.converter.JsonSerializer} is needed in this case.</li>
 * </ul>
 *
 * <h3>Example -- String field</h3>
 * <pre>{@code
 * @Json
 * @Column(nullable = false)
 * private String payload;
 * }</pre>
 *
 * <h3>Example -- POJO field</h3>
 * <pre>{@code
 * @Json
 * @Column(nullable = false)
 * private Address address; // serialized via JsonSerializer
 * }</pre>
 *
 * @see Convert
 * @see io.github.thinkfastpl.harbororm.api.converter.JsonSerializer
 * @see io.github.thinkfastpl.harbororm.api.converter.AttributeConverter
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Json {
}
