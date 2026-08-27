// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import spock.lang.Specification

class PostgreSqlDialectEscapeKeywordSpec extends Specification {

    def dialect = new PostgreSqlDialect()

    def "escapeKeyword wraps in double quotes and doubles embedded quotes"() {
        expect:
            dialect.escapeKeyword(value) == expected

        where:
            value           || expected
            "user"          || '"user"'
            ""              || '""'
            'a"b'           || '"a""b"'
            'a"b"c'         || '"a""b""c"'
            "table_name"    || '"table_name"'
            'with space'    || '"with space"'
            '"already"'     || '"""already"""'
    }
}
