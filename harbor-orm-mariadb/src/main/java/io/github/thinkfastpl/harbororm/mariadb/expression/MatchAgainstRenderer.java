// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.expression;

import io.github.thinkfastpl.harbororm.api.dialect.StandardDialects;
import io.github.thinkfastpl.harbororm.core.sql.dialect.EmptySqlDialect;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;
import io.github.thinkfastpl.harbororm.core.utils.HarborStringUtils;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

final class MatchAgainstRenderer {

    private MatchAgainstRenderer() {
    }

    static SqlQuery render(MariaDbMatch match, MariaDbAgainst against, EmptySqlDialect dialect) {
        final StringBuilder sb = new StringBuilder("MATCH(");
        final List<SqlQuery.Param> params = new ArrayList<>();

        HarborStringUtils.iterateAppendingJoiningDelimiter(
                sb,
                ", ",
                match.getColumns(),
                column -> {
                    SqlQuery columnSql = dialect.toSqlPart(column, column.getColumnContext(StandardDialects.MARIADB));
                    sb.append(columnSql.getSql());
                    params.addAll(columnSql.getParams());
                }
        );

        sb.append(") AGAINST(? ");
        sb.append(switch (against.getMode()) {
            case NATURAL -> "IN NATURAL LANGUAGE MODE";
            case BOOLEAN -> "IN BOOLEAN MODE";
            case WITH_QUERY_EXPANSION -> "IN NATURAL LANGUAGE MODE WITH QUERY EXPANSION";
        });
        sb.append(')');

        params.add(new SqlQuery.Param(against.getQuery(), Types.VARCHAR));

        return new SqlQuery(sb, params);
    }
}
