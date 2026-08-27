// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.expression;

import io.github.thinkfastpl.harbororm.api.expression.Expression;
import lombok.NonNull;

/**
 * H2 full-text search DSL. Static factories for H2-specific expressions.
 */
public class H2DSL {

    /**
     * Creates an H2 native full-text search condition using {@code FT_SEARCH_DATA}.
     * Requires prior initialization: {@code CREATE ALIAS IF NOT EXISTS FT_INIT FOR
     * 'org.h2.fulltext.FullText.init'; CALL FT_INIT(); CALL FT_CREATE_INDEX(...)}.
     *
     * @param idColumn  the primary-key column of the searched table
     * @param tableName the table name as stored in the database (typically uppercase)
     * @param query     the search query (terms are ANDed, case-insensitive)
     * @return a condition filtering rows whose indexed columns match the query
     */
    public static H2FtSearchCondition ftSearch(@NonNull Expression<?> idColumn, @NonNull String tableName, @NonNull String query) {
        return new H2FtSearchCondition(idColumn, tableName, query);
    }
}
