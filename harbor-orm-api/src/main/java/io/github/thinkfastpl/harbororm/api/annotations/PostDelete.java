// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Marks a method to be invoked after an entity is deleted from the database.
 *
 * <p>The callback fires after the DELETE statement and all cascaded deletions
 * have completed. Use this callback for transient side effects such as sending
 * notifications, clearing external caches, or audit logging.
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
 *     @PostDelete
 *     void onPostDelete() {
 *         System.out.println("Deleted article: " + this.title);
 *     }
 * }
 * }</pre>
 *
 * @see PreDelete
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PostDelete {
}
