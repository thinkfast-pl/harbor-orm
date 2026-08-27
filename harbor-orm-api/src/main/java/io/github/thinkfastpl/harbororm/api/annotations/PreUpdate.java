// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Marks a method to be invoked before an entity is updated in the database.
 *
 * <p>Use this callback to set modification timestamps, increment version counters,
 * or perform validation before the UPDATE statement executes. Multiple methods
 * can be annotated and will execute in declaration order.
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
 *     @Column(name = "updated_at", nullable = false)
 *     private LocalDateTime updatedAt;
 *
 *     @PreUpdate
 *     void onPreUpdate() {
 *         this.updatedAt = LocalDateTime.now();
 *     }
 * }
 * }</pre>
 *
 * @see PreInsert
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreUpdate {
}
