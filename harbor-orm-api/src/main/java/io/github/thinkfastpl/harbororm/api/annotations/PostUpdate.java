// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Marks a method to be invoked after an entity is updated in the database.
 *
 * <p>The callback fires after the full aggregate has been updated. Since the
 * UPDATE statement has already executed, changes to {@code @Column(nullable = false)}-annotated
 * fields will not be persisted automatically. Use this callback for transient
 * side effects such as sending notifications or updating in-memory caches.
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
 *     @Column(name = "title", nullable = false)
 *     private String title;
 *
 *     @PostUpdate
 *     void onPostUpdate() {
 *         System.out.println("Updated article: " + this.title);
 *     }
 * }
 * }</pre>
 *
 * @see PreUpdate
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PostUpdate {
}
