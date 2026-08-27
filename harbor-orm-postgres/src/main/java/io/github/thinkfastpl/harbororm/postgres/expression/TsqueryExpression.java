// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.expression;

import io.github.thinkfastpl.harbororm.api.dialect.StandardDialects;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.core.sql.dialect.DialectSpecificExpression;
import io.github.thinkfastpl.harbororm.core.sql.dialect.EmptySqlDialect;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.sql.Types;
import java.util.List;

/**
 * PostgreSQL tsquery expression. Represents a tsquery construction function call.
 */
@RequiredArgsConstructor
@Getter
public class TsqueryExpression implements Expression<Object>, DialectSpecificExpression {

    public enum Operator {
        PLAIN_TO_TSQUERY,      // plainto_tsquery(...)
        TO_TSQUERY,            // to_tsquery(...)
        PHRASE_TO_TSQUERY,     // phraseto_tsquery(...)
        WEBSEARCH_TO_TSQUERY,  // websearch_to_tsquery(...)
    }

    @NonNull
    private final Operator operator;

    /** Text search configuration name (e.g. "english"). Null for default config. */
    private final String config;

    @NonNull
    private final String query;

    @Override
    public Class<Object> getJavaType() {
        return Object.class;
    }

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }

    @Override
    public String getDialectName() {
        return StandardDialects.POSTGRES;
    }

    @Override
    public SqlQuery toSqlPart(@NonNull EmptySqlDialect dialect, ColumnContext columnContext) {
        String functionName = switch (operator) {
            case PLAIN_TO_TSQUERY -> "plainto_tsquery";
            case TO_TSQUERY -> "to_tsquery";
            case PHRASE_TO_TSQUERY -> "phraseto_tsquery";
            case WEBSEARCH_TO_TSQUERY -> "websearch_to_tsquery";
        };

        if (config != null) {
            return new SqlQuery(
                    functionName + "('" + PostgresExpressionUtils.escapeSqlString(config) + "', ?)",
                    List.of(new SqlQuery.Param(query, Types.VARCHAR))
            );
        }
        return new SqlQuery(
                functionName + "(?)",
                List.of(new SqlQuery.Param(query, Types.VARCHAR))
        );
    }
}
