// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.core.sql.dialect;

import java.util.ArrayList;
import java.util.List;

public class SqlQueryBuilder {
    private final StringBuilder sb = new StringBuilder();
    private final List<SqlQuery.Param> params = new ArrayList<>();

    public SqlQueryBuilder append(String sql) {
        if (sql != null) {
            this.sb.append(sql);
        }
        return this;
    }

    public SqlQueryBuilder append(String sql, SqlQuery.Param param) {
        if (sql != null) {
            this.sb.append(sql);
        }
        if (param != null) {
            this.params.add(param);
        }
        return this;
    }

    public SqlQueryBuilder append(String sql, List<SqlQuery.Param> params) {
        if (sql != null) {
            this.sb.append(sql);
        }
        if (params != null) {
            this.params.addAll(params);
        }
        return this;
    }

    public SqlQueryBuilder append(SqlQuery query) {
        if (query != null) {
            this.sb.append(query.getSql());
            this.params.addAll(query.getParams());
        }
        return this;
    }

    public SqlQuery toSqlQuery() {
        return new SqlQuery(sb, params);
    }
}
