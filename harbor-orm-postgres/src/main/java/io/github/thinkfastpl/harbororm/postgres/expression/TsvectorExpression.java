// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.expression;

import io.github.thinkfastpl.harbororm.api.dialect.StandardDialects;
import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.core.sql.dialect.DialectSpecificExpression;
import io.github.thinkfastpl.harbororm.core.sql.dialect.EmptySqlDialect;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * PostgreSQL tsvector expression. Represents a {@code to_tsvector(...)} call.
 */
@RequiredArgsConstructor
@Getter
public class TsvectorExpression implements Expression<Object>, DialectSpecificExpression {

    public enum Operator {
        TO_TSVECTOR,         // to_tsvector('config', column)
        TO_TSVECTOR_DEFAULT, // to_tsvector(column) — uses database default config
    }

    @NonNull
    private final Operator operator;

    @NonNull
    private final Expression<?> source;

    /** Text search configuration name (e.g. "english"). Null for default config. */
    private final String config;

    /**
     * Creates a full-text match condition using the {@code @@} operator.
     *
     * @param tsquery the tsquery expression to match against
     * @return a condition representing {@code tsvector @@ tsquery}
     */
    public Condition matches(@NonNull TsqueryExpression tsquery) {
        return new TsvectorMatchCondition(this, tsquery);
    }

    @Override
    public Class<Object> getJavaType() {
        return Object.class;
    }

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return source.getColumnContext(dialectName);
    }

    @Override
    public String getDialectName() {
        return StandardDialects.POSTGRES;
    }

    @Override
    public SqlQuery toSqlPart(@NonNull EmptySqlDialect dialect, ColumnContext columnContext) {
        SqlQuery sourceSql = dialect.toSqlPart(source, source.getColumnContext(getDialectName()));
        return switch (operator) {
            case TO_TSVECTOR -> new SqlQuery(
                    "to_tsvector('" + PostgresExpressionUtils.escapeSqlString(config) + "', " + sourceSql.getSql() + ")",
                    sourceSql.getParams()
            );
            case TO_TSVECTOR_DEFAULT -> new SqlQuery(
                    "to_tsvector(" + sourceSql.getSql() + ")",
                    sourceSql.getParams()
            );
        };
    }
}
