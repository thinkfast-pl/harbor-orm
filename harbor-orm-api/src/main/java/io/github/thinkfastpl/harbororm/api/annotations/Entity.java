// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Marks a class as a database entity mapped to a table.
 *
 * <p>An entity represents a row in a database table and must have exactly one
 * field annotated with {@link Id} to define its primary key. The annotation
 * processor generates two metadata classes:
 * <ul>
 *   <li>{@code Q<ClassName>} - for entity operations (insert, update, delete, select)</li>
 *   <li>{@code <TableName>Table} - for raw SQL queries without entity mapping</li>
 * </ul>
 *
 * <h2>Requirements</h2>
 * <ul>
 *   <li>A no-argument constructor (can be private)</li>
 *   <li>Getter methods for all mapped fields</li>
 *   <li>Setter methods for mutable fields</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @Entity(table = "products", schema = "inventory")
 * public class ProductEntity {
 *     @Id
 *     private Long id;
 *
 *     @Column(nullable = false)
 *     private String name;
 * }
 * }</pre>
 *
 * @see Id
 * @see Column
 * @see Embeddable
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Entity {

    /**
     * The database table name.
     *
     * @return the table name
     */
    String table();

    /**
     * The database schema name. If empty, uses the default schema.
     *
     * @return the schema name, or empty string for default schema
     */
    String schema() default "";

    /**
     * The strategy for converting field names to column names.
     *
     * @return the column naming strategy, default is {@link ColumnNameStrategy#SNAKE_CASE}
     */
    ColumnNameStrategy columnNameStrategy() default ColumnNameStrategy.SNAKE_CASE;
}
