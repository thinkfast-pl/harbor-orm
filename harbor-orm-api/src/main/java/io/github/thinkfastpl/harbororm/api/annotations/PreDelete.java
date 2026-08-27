// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Marks a method to be invoked before an entity is deleted from the database.
 *
 * <p>Use this callback to perform cleanup, audit logging, or validation
 * before the DELETE statement executes. Multiple methods can be annotated
 * and will execute in declaration order.
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
 *     @PreDelete
 *     void onPreDelete() {
 *         System.out.println("Deleting article: " + this.title);
 *     }
 * }
 * }</pre>
 *
 * @see PostDelete
 * @see PreInsert
 * @see PreUpdate
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreDelete {
}
