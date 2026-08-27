// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.dialect

import spock.lang.Specification

class MariaDbSqlDialectEscapeKeywordSpec extends Specification {

    def dialect = new MariaDbSqlDialect()

    def "escapeKeyword wraps in backticks and doubles embedded backticks"() {
        expect:
            dialect.escapeKeyword(value) == expected

        where:
            value           || expected
            "user"          || '`user`'
            ""              || '``'
            'a"b'           || '`a"b`'
            'a"b"c'         || '`a"b"c`'
            "table_name"    || '`table_name`'
            'with space'    || '`with space`'
            'a`b'           || '`a``b`'
    }
}
