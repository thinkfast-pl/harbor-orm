// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mysql.expression;

import io.github.thinkfastpl.harbororm.api.expression.Expression;
import lombok.NonNull;

import java.util.List;

/**
 * MySQL full-text search DSL: static factories for {@code MATCH(cols) AGAINST(? <mode>)}
 * conditions and relevance-score expressions.
 */
public class MySqlDSL {

    /**
     * Creates a MySQL {@code MATCH(col1, col2, ...)} clause holder.
     * Combine with {@link #against(String)}, {@link #againstBoolean(String)} or
     * {@link #againstWithQueryExpansion(String)} via {@link MySqlMatch#matches(MySqlAgainst)}
     * to form a condition, or pass to {@link #matchRank(MySqlMatch, MySqlAgainst)} for a
     * relevance score.
     *
     * @param columns one or more columns covered by a MySQL FULLTEXT index
     * @return a {@link MySqlMatch}
     */
    public static MySqlMatch match(@NonNull Expression<?>... columns) {
        return new MySqlMatch(List.of(columns));
    }

    /**
     * Creates a MySQL {@code AGAINST(? IN NATURAL LANGUAGE MODE)} clause holder.
     *
     * @param query the natural-language query string
     * @return a {@link MySqlAgainst}
     */
    public static MySqlAgainst against(@NonNull String query) {
        return new MySqlAgainst(query, MySqlAgainst.Mode.NATURAL);
    }

    /**
     * Creates a MySQL {@code AGAINST(? IN BOOLEAN MODE)} clause holder.
     * The query may use boolean operators ({@code +}, {@code -}, {@code *}, {@code "phrase"}, etc.).
     *
     * @param query the boolean-mode query string
     * @return a {@link MySqlAgainst}
     */
    public static MySqlAgainst againstBoolean(@NonNull String query) {
        return new MySqlAgainst(query, MySqlAgainst.Mode.BOOLEAN);
    }

    /**
     * Creates a MySQL {@code AGAINST(? IN NATURAL LANGUAGE MODE WITH QUERY EXPANSION)} clause holder.
     *
     * @param query the natural-language query string
     * @return a {@link MySqlAgainst}
     */
    public static MySqlAgainst againstWithQueryExpansion(@NonNull String query) {
        return new MySqlAgainst(query, MySqlAgainst.Mode.WITH_QUERY_EXPANSION);
    }

    /**
     * Creates a MySQL {@code MATCH(cols) AGAINST(? <mode>)} expression yielding the relevance score.
     *
     * @param match   the MATCH clause holder
     * @param against the AGAINST clause holder
     * @return a {@link MySqlMatchAgainstRankExpression} (Double)
     */
    public static MySqlMatchAgainstRankExpression matchRank(@NonNull MySqlMatch match, @NonNull MySqlAgainst against) {
        return new MySqlMatchAgainstRankExpression(match, against);
    }
}
