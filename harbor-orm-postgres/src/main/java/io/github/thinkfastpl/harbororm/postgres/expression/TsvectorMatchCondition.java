// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.expression;

import io.github.thinkfastpl.harbororm.api.dialect.StandardDialects;
import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.core.sql.dialect.DialectSpecificExpression;
import io.github.thinkfastpl.harbororm.core.sql.dialect.EmptySqlDialect;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;
import io.github.thinkfastpl.harbororm.core.utils.HarborListUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * PostgreSQL {@code tsvector @@ tsquery} condition.
 */
@RequiredArgsConstructor
@Getter
public class TsvectorMatchCondition implements Condition, DialectSpecificExpression {

    @NonNull
    private final TsvectorExpression tsvector;

    @NonNull
    private final TsqueryExpression tsquery;

    @Override
    public String getDialectName() {
        return StandardDialects.POSTGRES;
    }

    @Override
    public SqlQuery toSqlPart(@NonNull EmptySqlDialect dialect, ColumnContext columnContext) {
        SqlQuery tsvectorSql = dialect.toSqlPart(tsvector, columnContext);
        SqlQuery tsquerySql = dialect.toSqlPart(tsquery, columnContext);
        return new SqlQuery(
                tsvectorSql.getSql() + " @@ " + tsquerySql.getSql(),
                HarborListUtils.merge(tsvectorSql.getParams(), tsquerySql.getParams())
        );
    }
}
