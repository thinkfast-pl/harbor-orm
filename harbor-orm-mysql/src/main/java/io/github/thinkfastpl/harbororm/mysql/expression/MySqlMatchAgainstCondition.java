// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mysql.expression;

import io.github.thinkfastpl.harbororm.api.dialect.StandardDialects;
import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.core.sql.dialect.DialectSpecificExpression;
import io.github.thinkfastpl.harbororm.core.sql.dialect.EmptySqlDialect;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * MySQL {@code MATCH(cols) AGAINST(? <mode>)} condition.
 */
@RequiredArgsConstructor
@Getter
public class MySqlMatchAgainstCondition implements Condition, DialectSpecificExpression {

    @NonNull
    private final MySqlMatch match;

    @NonNull
    private final MySqlAgainst against;

    @Override
    public String getDialectName() {
        return StandardDialects.MYSQL;
    }

    @Override
    public SqlQuery toSqlPart(@NonNull EmptySqlDialect dialect, ColumnContext columnContext) {
        return MatchAgainstRenderer.render(match, against, dialect);
    }
}
