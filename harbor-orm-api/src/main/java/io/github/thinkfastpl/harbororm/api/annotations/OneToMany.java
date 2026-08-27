// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Defines a one-to-many relationship between entities.
 *
 * <p>The annotated field must be a {@link java.util.List} of the related entity type.
 * Related entities are loaded lazily on first access to the collection, with batch
 * loading for efficiency (up to 50 parents per query).
 *
 * <p>Relationships are read-only from the parent side. To insert, update, or delete
 * child entities, use their own repository directly.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @Entity(table = "authors")
 * public class AuthorEntity {
 *     @Id
 *     private Long id;
 *
 *     @OneToMany(joinColumns = @JoinColumn(
 *         name = "author_id",
 *         fieldType = Long.class
 *     ))
 *     private List<BookEntity> books = new ArrayList<>();
 * }
 * }</pre>
 *
 * @see JoinColumn
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OneToMany {

    /**
     * The join column(s) that define the foreign key relationship in the child table.
     *
     * @return the join columns
     */
    JoinColumn[] joinColumns() default {};
}
