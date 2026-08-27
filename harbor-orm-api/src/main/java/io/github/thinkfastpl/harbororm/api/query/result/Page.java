// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.query.result;

import lombok.NonNull;
import lombok.Value;

import java.util.List;
import java.util.function.Function;

/**
 * An immutable page of query results with pagination metadata.
 *
 * <p>Returned by paginated SELECT queries, a {@code Page} contains a slice of rows together
 * with the page size, current page number, and totals for rows and pages.
 *
 * @param <T> the row type (typically {@link Record} or an entity class)
 * @see io.github.thinkfastpl.harbororm.api.query.SelectQuery
 */
@Value
public class Page<T> {

    /** The rows contained in this page. */
    List<T> rows;

    /** The maximum number of rows per page. */
    int pageSize;

    /** The zero-based page number. */
    int page;

    /** The total number of rows across all pages. */
    long totalRows;

    /** The total number of pages. */
    long totalPages;

    /**
     * Transforms the rows in this page using the given mapping function,
     * preserving all pagination metadata.
     *
     * @param mapper the mapping function to apply to each row
     * @param <U>    the target row type
     * @return a new page with transformed rows and the same pagination metadata
     */
    public <U> Page<U> map(@NonNull Function<? super T, U> mapper) {
        return new Page<>(
                rows.stream().map(mapper).toList(),
                pageSize,
                page,
                totalRows,
                totalPages
        );
    }
}
