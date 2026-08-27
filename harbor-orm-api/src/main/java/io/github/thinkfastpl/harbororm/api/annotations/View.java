// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Marks a class as a read-only mapping to a database view (or any SELECT-able relation).
 *
 * <p>The annotation processor generates a {@code Q<ClassName>} metadata class implementing
 * {@link io.github.thinkfastpl.harbororm.api.metadata.QView QView<T>}, which provides typed {@code QColumn} fields
 * and can be used with {@link io.github.thinkfastpl.harbororm.api.HarborSession#select(io.github.thinkfastpl.harbororm.api.metadata.QView)
 * session.select(QView)} to query the view and get typed Java instances back.
 *
 * <h2>Differences from {@link Entity}</h2>
 * <ul>
 *   <li>Views are <b>read-only</b> — no insert, update, or delete operations</li>
 *   <li>No {@link Id} field is required (views have no identity concept)</li>
 *   <li>No lifecycle callbacks, relationships, embedded classes, or JSON support</li>
 *   <li>Only {@link Column} annotations are supported on fields</li>
 * </ul>
 *
 * <h2>Requirements</h2>
 * <ul>
 *   <li>A no-argument constructor (can be private)</li>
 *   <li>Fields annotated with {@link Column} to define the column mapping</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @View(name = "user_reports")
 * public class UserReport {
 *
 *     @Column(name = "user_name", nullable = false)
 *     private String userName;
 *
 *     @Column(name = "total_spent", nullable = false)
 *     private BigDecimal totalSpent;
 * }
 *
 * // Querying:
 * QUserReport VIEW = new QUserReport(null);
 * List<UserReport> reports = session.select(VIEW)
 *     .where(VIEW.totalSpent.gt(new BigDecimal("1000")))
 *     .orderBy(VIEW.userName.asc())
 *     .fetchAll();
 * }</pre>
 *
 * @see io.github.thinkfastpl.harbororm.api.metadata.QView
 * @see io.github.thinkfastpl.harbororm.api.query.ViewQuery
 * @see Column
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface View {

    /**
     * The database view name.
     *
     * @return the view name
     */
    String name();

    /**
     * The database schema name. If empty, uses the default schema.
     *
     * @return the schema name, or empty string for default schema
     */
    String schema() default "";

    /**
     * The strategy for converting field names to column names.
     * Applied to fields that don't have an explicit {@link Column#name()}.
     *
     * @return the column naming strategy, default is {@link ColumnNameStrategy#SNAKE_CASE}
     */
    ColumnNameStrategy columnNameStrategy() default ColumnNameStrategy.SNAKE_CASE;
}
