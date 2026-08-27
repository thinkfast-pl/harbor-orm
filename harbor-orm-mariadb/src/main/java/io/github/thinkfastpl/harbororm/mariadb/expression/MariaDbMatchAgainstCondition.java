// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.expression;

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
 * MariaDB {@code MATCH(cols) AGAINST(? <mode>)} condition.
 */
@RequiredArgsConstructor
@Getter
public class MariaDbMatchAgainstCondition implements Condition, DialectSpecificExpression {

    @NonNull
    private final MariaDbMatch match;

    @NonNull
    private final MariaDbAgainst against;

    @Override
    public String getDialectName() {
        return StandardDialects.MARIADB;
    }

    @Override
    public SqlQuery toSqlPart(@NonNull EmptySqlDialect dialect, ColumnContext columnContext) {
        return MatchAgainstRenderer.render(match, against, dialect);
    }
}
