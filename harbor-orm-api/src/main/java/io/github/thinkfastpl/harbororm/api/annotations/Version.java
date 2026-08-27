// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Marks a numeric field as the optimistic lock version column.
 *
 * <p>The version is automatically initialized to 0 on insert (if null),
 * incremented atomically on each update, and checked in WHERE clauses
 * for both UPDATE and DELETE operations. A stale write throws
 * {@link io.github.thinkfastpl.harbororm.api.exception.OptimisticLockException}.
 *
 * <p>Supported field types: {@code int}, {@code Integer}, {@code long},
 * {@code Long}, {@code short}, {@code Short}.
 *
 * <h2>Rules</h2>
 * <ul>
 *   <li>At most one {@code @Version} field per entity</li>
 *   <li>Must not be combined with {@code @Id}</li>
 *   <li>The field must also have {@code @Column(nullable = false)}</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @Entity(table = "products")
 * public class ProductEntity {
 *     @Id
 *     private Long id;
 *
 *     @Column(nullable = false)
 *     private String name;
 *
 *     @Version
 *     @Column(nullable = false)
 *     private Long version;
 * }
 * }</pre>
 *
 * @see Column
 * @see io.github.thinkfastpl.harbororm.api.exception.OptimisticLockException
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Version {
}
