// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.expression;

import io.github.thinkfastpl.harbororm.api.expression.Expression;
import lombok.NonNull;

import java.util.List;

/**
 * MariaDB full-text search DSL: static factories for {@code MATCH(cols) AGAINST(? <mode>)}
 * conditions and relevance-score expressions.
 */
public class MariaDbDSL {

    /**
     * Creates a MariaDB {@code MATCH(col1, col2, ...)} clause holder.
     * Combine with {@link #against(String)}, {@link #againstBoolean(String)} or
     * {@link #againstWithQueryExpansion(String)} via {@link MariaDbMatch#matches(MariaDbAgainst)}
     * to form a condition, or pass to {@link #matchRank(MariaDbMatch, MariaDbAgainst)} for a
     * relevance score.
     *
     * @param columns one or more columns covered by a MariaDB FULLTEXT index
     * @return a {@link MariaDbMatch}
     */
    public static MariaDbMatch match(@NonNull Expression<?>... columns) {
        return new MariaDbMatch(List.of(columns));
    }

    /**
     * Creates a MariaDB {@code AGAINST(? IN NATURAL LANGUAGE MODE)} clause holder.
     *
     * @param query the natural-language query string
     * @return a {@link MariaDbAgainst}
     */
    public static MariaDbAgainst against(@NonNull String query) {
        return new MariaDbAgainst(query, MariaDbAgainst.Mode.NATURAL);
    }

    /**
     * Creates a MariaDB {@code AGAINST(? IN BOOLEAN MODE)} clause holder.
     * The query may use boolean operators ({@code +}, {@code -}, {@code *}, {@code "phrase"}, etc.).
     *
     * @param query the boolean-mode query string
     * @return a {@link MariaDbAgainst}
     */
    public static MariaDbAgainst againstBoolean(@NonNull String query) {
        return new MariaDbAgainst(query, MariaDbAgainst.Mode.BOOLEAN);
    }

    /**
     * Creates a MariaDB {@code AGAINST(? IN NATURAL LANGUAGE MODE WITH QUERY EXPANSION)} clause holder.
     *
     * @param query the natural-language query string
     * @return a {@link MariaDbAgainst}
     */
    public static MariaDbAgainst againstWithQueryExpansion(@NonNull String query) {
        return new MariaDbAgainst(query, MariaDbAgainst.Mode.WITH_QUERY_EXPANSION);
    }

    /**
     * Creates a MariaDB {@code MATCH(cols) AGAINST(? <mode>)} expression yielding the relevance score.
     *
     * @param match   the MATCH clause holder
     * @param against the AGAINST clause holder
     * @return a {@link MariaDbMatchAgainstRankExpression} (Double)
     */
    public static MariaDbMatchAgainstRankExpression matchRank(@NonNull MariaDbMatch match, @NonNull MariaDbAgainst against) {
        return new MariaDbMatchAgainstRankExpression(match, against);
    }
}
