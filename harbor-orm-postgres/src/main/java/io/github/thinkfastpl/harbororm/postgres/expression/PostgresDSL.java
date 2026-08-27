// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.expression;

import io.github.thinkfastpl.harbororm.api.expression.Expression;
import lombok.NonNull;

/**
 * PostgreSQL full-text search DSL. Static factories for tsvector/tsquery expressions.
 */
public class PostgresDSL {

    /**
     * Creates a PostgreSQL {@code to_tsvector(column)} expression using the database's default text search configuration.
     *
     * @param column the text column to convert to tsvector
     * @return a {@link TsvectorExpression}
     */
    public static TsvectorExpression toTsvector(@NonNull Expression<?> column) {
        return new TsvectorExpression(TsvectorExpression.Operator.TO_TSVECTOR_DEFAULT, column, null);
    }

    /**
     * Creates a PostgreSQL {@code to_tsvector('config', column)} expression with explicit text search configuration.
     *
     * @param config the text search configuration name (e.g. "english", "simple")
     * @param column the text column to convert to tsvector
     * @return a {@link TsvectorExpression}
     */
    public static TsvectorExpression toTsvector(@NonNull String config, @NonNull Expression<?> column) {
        return new TsvectorExpression(TsvectorExpression.Operator.TO_TSVECTOR, column, config);
    }

    /**
     * Creates a PostgreSQL {@code plainto_tsquery(query)} expression using the database's default text search configuration.
     *
     * @param query the search query string
     * @return a {@link TsqueryExpression}
     */
    public static TsqueryExpression plainToTsquery(@NonNull String query) {
        return new TsqueryExpression(TsqueryExpression.Operator.PLAIN_TO_TSQUERY, null, query);
    }

    /**
     * Creates a PostgreSQL {@code plainto_tsquery('config', query)} expression with explicit text search configuration.
     *
     * @param config the text search configuration name (e.g. "english", "simple")
     * @param query  the search query string
     * @return a {@link TsqueryExpression}
     */
    public static TsqueryExpression plainToTsquery(@NonNull String config, @NonNull String query) {
        return new TsqueryExpression(TsqueryExpression.Operator.PLAIN_TO_TSQUERY, config, query);
    }

    /**
     * Creates a PostgreSQL {@code to_tsquery(query)} expression using raw tsquery syntax.
     * The query string must use tsquery operators ({@code &}, {@code |}, {@code !}, {@code <->}).
     *
     * @param query the raw tsquery string
     * @return a {@link TsqueryExpression}
     */
    public static TsqueryExpression toTsquery(@NonNull String query) {
        return new TsqueryExpression(TsqueryExpression.Operator.TO_TSQUERY, null, query);
    }

    /**
     * Creates a PostgreSQL {@code to_tsquery('config', query)} expression with explicit configuration and raw tsquery syntax.
     *
     * @param config the text search configuration name (e.g. "english", "simple")
     * @param query  the raw tsquery string
     * @return a {@link TsqueryExpression}
     */
    public static TsqueryExpression toTsquery(@NonNull String config, @NonNull String query) {
        return new TsqueryExpression(TsqueryExpression.Operator.TO_TSQUERY, config, query);
    }

    /**
     * Creates a PostgreSQL {@code phraseto_tsquery('config', query)} expression for exact phrase matching.
     *
     * @param config the text search configuration name (e.g. "english", "simple")
     * @param query  the phrase to search for
     * @return a {@link TsqueryExpression}
     */
    public static TsqueryExpression phraseToTsquery(@NonNull String config, @NonNull String query) {
        return new TsqueryExpression(TsqueryExpression.Operator.PHRASE_TO_TSQUERY, config, query);
    }

    /**
     * Creates a PostgreSQL {@code websearch_to_tsquery('config', query)} expression.
     * Supports web-search-style syntax: unquoted terms are ANDed, quoted phrases, {@code -} for negation, {@code OR} for alternatives.
     *
     * @param config the text search configuration name (e.g. "english", "simple")
     * @param query  the web-search-style query string
     * @return a {@link TsqueryExpression}
     */
    public static TsqueryExpression websearchToTsquery(@NonNull String config, @NonNull String query) {
        return new TsqueryExpression(TsqueryExpression.Operator.WEBSEARCH_TO_TSQUERY, config, query);
    }

    /**
     * Creates a PostgreSQL {@code ts_rank(tsvector, tsquery)} expression.
     *
     * @param tsvector the tsvector expression
     * @param tsquery  the tsquery expression
     * @return a {@link TsRankExpression} (Double)
     */
    public static TsRankExpression tsRank(@NonNull TsvectorExpression tsvector, @NonNull TsqueryExpression tsquery) {
        return new TsRankExpression(TsRankExpression.Operator.TS_RANK, tsvector, tsquery);
    }

    /**
     * Creates a PostgreSQL {@code ts_rank_cd(tsvector, tsquery)} expression (cover density ranking).
     *
     * @param tsvector the tsvector expression
     * @param tsquery  the tsquery expression
     * @return a {@link TsRankExpression} (Double)
     */
    public static TsRankExpression tsRankCd(@NonNull TsvectorExpression tsvector, @NonNull TsqueryExpression tsquery) {
        return new TsRankExpression(TsRankExpression.Operator.TS_RANK_CD, tsvector, tsquery);
    }
}
