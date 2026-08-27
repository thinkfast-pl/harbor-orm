// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL

class PortableFunctionExpressionHashIT extends AbstractHarborIT {

    def "select md5() with constant string"() {
        when:
            String result = session.select(DSL.md5(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == String.class

        where:
            session << getSessionsExcept(DbType.H2)

        combined:
            value || expected
            'hello' || '5d41402abc4b2a76b9719d911017c592'
            '' || 'd41d8cd98f00b204e9800998ecf8427e'
            'Hello World' || 'b10a8db164e0754105b7a99be72e3fe5'
    }

    def "select md5() with Expression parameter"() {
        when:
            String result = session.select(DSL.md5(DSL.constant('hello'))).fetchSingle()

        then:
            result == '5d41402abc4b2a76b9719d911017c592'

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "select md5() throws UnsupportedOperationException on H2"() {
        when:
            session.select(DSL.md5('hello')).fetchSingle()

        then:
            thrown(UnsupportedOperationException)

        where:
            session << getSessionsExcept(DbType.POSTGRES, DbType.MARIADB, DbType.MYSQL)
    }
}
