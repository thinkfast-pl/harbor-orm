// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Maps a collection of simple values or embeddables to a separate table.
 *
 * <p>Unlike {@link OneToMany} which references other entities, element collections
 * store values that have no identity of their own. The collection table contains
 * the foreign key to the parent entity and columns for each element's data.
 *
 * <p>Elements are loaded lazily on first access and are automatically cascaded
 * on insert, update, and delete of the parent entity.
 *
 * <h2>Simple Type Collection</h2>
 * <pre>{@code
 * @ElementCollection(
 *     table = "article_tags",
 *     joinColumns = @JoinColumn(name = "article_id", fieldType = Long.class)
 * )
 * private List<@Column(name = "tag", nullable = false) String> tags;
 * }</pre>
 *
 * <h2>Embeddable Collection</h2>
 * <pre>{@code
 * @ElementCollection(
 *     table = "contact_phones",
 *     joinColumns = @JoinColumn(name = "contact_id", fieldType = Long.class)
 * )
 * private List<@Embedded PhoneNumber> phoneNumbers;
 * }</pre>
 *
 * @see JoinColumn
 * @see Embeddable
 * @see Embedded
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ElementCollection {

    /**
     * The name of the collection table.
     *
     * @return the table name
     */
    String table();

    /**
     * The database schema containing the collection table. Default is the entity's schema.
     *
     * @return the schema name, or empty string for default schema
     */
    String schema() default "";

    /**
     * The join column(s) referencing the parent entity.
     *
     * @return the join columns
     */
    JoinColumn[] joinColumns() default {};
}
