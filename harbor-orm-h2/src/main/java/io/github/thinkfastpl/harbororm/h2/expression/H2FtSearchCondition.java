// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.expression;

import io.github.thinkfastpl.harbororm.api.dialect.StandardDialects;
import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.core.sql.dialect.DialectSpecificExpression;
import io.github.thinkfastpl.harbororm.core.sql.dialect.EmptySqlDialect;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;
import io.github.thinkfastpl.harbororm.core.utils.HarborListUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.sql.Types;
import java.util.List;

/**
 * H2 native full-text search condition based on the {@code FT_SEARCH_DATA} table function.
 * <p>
 * Renders {@code <idColumn> IN (SELECT FT."KEYS"[1] FROM FT_SEARCH_DATA(?, 0, 0) FT
 * WHERE FT."TABLE" = ?)}. Requires H2 full-text to be initialized
 * ({@code FT_INIT()} + {@code FT_CREATE_INDEX(...)}) and the table to have a
 * single-column primary key.
 */
@RequiredArgsConstructor
@Getter
public class H2FtSearchCondition implements Condition, DialectSpecificExpression {

    @NonNull
    private final Expression<?> idColumn;

    /** Table name as stored in the database metadata (typically uppercase). */
    @NonNull
    private final String tableName;

    @NonNull
    private final String query;

    @Override
    public String getDialectName() {
        return StandardDialects.H2;
    }

    @Override
    public SqlQuery toSqlPart(@NonNull EmptySqlDialect dialect, ColumnContext columnContext) {
        SqlQuery idSql = dialect.toSqlPart(idColumn, idColumn.getColumnContext(getDialectName()));
        return new SqlQuery(
                idSql.getSql() + " IN (SELECT FT.\"KEYS\"[1] FROM FT_SEARCH_DATA(?, 0, 0) FT WHERE FT.\"TABLE\" = ?)",
                HarborListUtils.merge(idSql.getParams(), List.of(
                        new SqlQuery.Param(query, Types.VARCHAR),
                        new SqlQuery.Param(tableName, Types.VARCHAR)
                ))
        );
    }
}
