// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Marks a method to be invoked before an entity is inserted into the database.
 *
 * <p>Use this callback to set default values, generate timestamps, or perform
 * validation before the INSERT statement executes. Multiple methods can be
 * annotated and will execute in declaration order.
 *
 * <h2>Requirements</h2>
 * <ul>
 *   <li>Method must have no parameters</li>
 *   <li>Method can have any access modifier</li>
 *   <li>Avoid database operations inside the callback</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @Entity(table = "articles")
 * public class ArticleEntity {
 *     @Column(name = "created_at", nullable = false)
 *     private LocalDateTime createdAt;
 *
 *     @PreInsert
 *     void onPreInsert() {
 *         this.createdAt = LocalDateTime.now();
 *     }
 * }
 * }</pre>
 *
 * @see PreUpdate
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreInsert {
}
