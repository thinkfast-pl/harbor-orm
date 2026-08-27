// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Defines a many-to-many relationship between entities using a join table.
 *
 * <p>Only the join table rows are managed — related entities are never cascaded
 * (inserted, updated, or deleted). Related entities must exist before being
 * linked and must be managed through their own repositories.
 *
 * <p>The annotated field must be a {@link java.util.Set} of either:
 * <ul>
 *     <li>The related entity type (e.g., {@code Set<BookEntity>}) — loads full entities lazily</li>
 *     <li>The related entity's ID type (e.g., {@code Set<Long>}) — loads only IDs from the join table</li>
 * </ul>
 *
 * <h2>Full entity example</h2>
 * <pre>{@code
 * @ManyToMany(
 *     table = "author_books",
 *     joinColumns = @JoinColumn(name = "author_id", fieldType = Long.class),
 *     inverseJoinColumns = @JoinColumn(name = "book_id", fieldType = Long.class)
 * )
 * private Set<BookEntity> books;
 * }</pre>
 *
 * <h2>ID-only example</h2>
 * <pre>{@code
 * @ManyToMany(
 *     table = "author_books",
 *     joinColumns = @JoinColumn(name = "author_id", fieldType = Long.class),
 *     inverseJoinColumns = @JoinColumn(name = "book_id", fieldType = Long.class)
 * )
 * private Set<Long> bookIds;
 * }</pre>
 *
 * @see JoinColumn
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ManyToMany {

    /**
     * The name of the join table.
     *
     * @return the join table name
     */
    String table();

    /**
     * The database schema containing the join table.
     *
     * @return the schema name, or empty string for default schema
     */
    String schema() default "";

    /**
     * The join column(s) referencing the owning entity (the entity this field lives on).
     *
     * @return the owning-side join columns
     */
    JoinColumn[] joinColumns();

    /**
     * The join column(s) referencing the related entity on the other side.
     *
     * @return the inverse-side join columns
     */
    JoinColumn[] inverseJoinColumns();
}
