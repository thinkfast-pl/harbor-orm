// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.expression;

import io.github.thinkfastpl.harbororm.api.dialect.StandardDialects;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.core.sql.dialect.DialectSpecificExpression;
import io.github.thinkfastpl.harbororm.core.sql.dialect.EmptySqlDialect;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * MariaDB {@code MATCH(cols) AGAINST(? <mode>)} expression yielding the relevance score (Double).
 */
@RequiredArgsConstructor
@Getter
public class MariaDbMatchAgainstRankExpression implements Expression<Double>, DialectSpecificExpression {

    @NonNull
    private final MariaDbMatch match;

    @NonNull
    private final MariaDbAgainst against;

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
        return StandardDialects.MARIADB;
    }

    @Override
    public SqlQuery toSqlPart(@NonNull EmptySqlDialect dialect, ColumnContext columnContext) {
        return MatchAgainstRenderer.render(match, against, dialect);
    }
}
