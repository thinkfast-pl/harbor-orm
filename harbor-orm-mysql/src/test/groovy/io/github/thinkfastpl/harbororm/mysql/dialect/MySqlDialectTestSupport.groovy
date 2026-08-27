// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mysql.dialect

import io.github.thinkfastpl.harbororm.api.metadata.QColumn
import io.github.thinkfastpl.harbororm.api.metadata.QTableName
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery

class MySqlDialectTestSupport {

    static MySqlSqlDialect newDialect() {
        new MySqlSqlDialect()
    }

    static QTableName table(String name, String alias = null, String schema = null) {
        new QTableName(name, schema, alias)
    }

    static <T> QColumn<T> col(Class<T> type, String name, String tableAlias = null) {
        QColumn.simple(type, tableAlias, name)
    }

    static void assertSql(SqlQuery actual, String expectedSql, List<List> expectedParams = []) {
        assert actual.sql.toString() == expectedSql
        assert actual.params.size() == expectedParams.size()
        expectedParams.eachWithIndex { exp, i ->
            assert actual.params[i].value == exp[0]
            assert actual.params[i].sqlType == exp[1]
        }
    }
}
