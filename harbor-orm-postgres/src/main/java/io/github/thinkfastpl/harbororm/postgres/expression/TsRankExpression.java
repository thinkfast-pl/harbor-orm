// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.expression;

import io.github.thinkfastpl.harbororm.api.dialect.StandardDialects;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.core.sql.dialect.DialectSpecificExpression;
import io.github.thinkfastpl.harbororm.core.sql.dialect.EmptySqlDialect;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;
import io.github.thinkfastpl.harbororm.core.utils.HarborListUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * PostgreSQL {@code ts_rank} / {@code ts_rank_cd} expression. Returns Double.
 */
@RequiredArgsConstructor
@Getter
public class TsRankExpression implements Expression<Double>, DialectSpecificExpression {

    public enum Operator {
        TS_RANK,    // ts_rank(tsvector, tsquery)
        TS_RANK_CD, // ts_rank_cd(tsvector, tsquery)
    }

    @NonNull
    private final Operator operator;

    @NonNull
    private final TsvectorExpression tsvector;

    @NonNull
    private final TsqueryExpression tsquery;

    @Override
    public Class<Double> getJavaType() {
        return Double.class;
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
        SqlQuery tsvectorSql = dialect.toSqlPart(tsvector, columnContext);
        SqlQuery tsquerySql = dialect.toSqlPart(tsquery, columnContext);
        String functionName = switch (operator) {
            case TS_RANK -> "ts_rank";
            case TS_RANK_CD -> "ts_rank_cd";
        };
        return new SqlQuery(
                functionName + "(" + tsvectorSql.getSql() + ", " + tsquerySql.getSql() + ")",
                HarborListUtils.merge(tsvectorSql.getParams(), tsquerySql.getParams())
        );
    }
}
