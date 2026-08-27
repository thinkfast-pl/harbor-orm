// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a stored function that returns a table.
 * The annotated class defines the return columns using {@link Column} annotations.
 * Parameters are declared in the {@link #params()} attribute.
 *
 * <p>The annotation processor generates a {@code <ClassName>Function} class
 * with a type-safe {@code call()} method that returns a {@link io.github.thinkfastpl.harbororm.api.expression.FunctionCallTableSource}
 * for use in FROM clauses.
 *
 * <pre>{@code
 * @StoredFunction(
 *     name = "get_recent_books",
 *     params = {
 *         @Param(name = "author_id", type = Long.class),
 *         @Param(name = "since_date", type = LocalDate.class)
 *     }
 * )
 * public class GetRecentBooks {
 *     @Column(nullable = false) private Long id;
 *     @Column(nullable = false) private String title;
 *     @Column(nullable = false) private LocalDate published;
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface StoredFunction {
    /** The SQL function name. */
    String name();

    /** Optional database schema. */
    String schema() default "";

    /** Function parameters in declaration order. */
    Param[] params() default {};
}
