// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Order
import spock.lang.Specification

import static io.github.thinkfastpl.harbororm.postgres.dialect.PostgreSqlDialectTestSupport.*

class PostgreSqlDialectToSqlPartOrderSpec extends Specification {

    def dialect = newDialect()

    def "asc by column name renders ASC"() {
        expect:
            assertSql(dialect.toSqlPart(DSL.asc("name")), '"name" ASC')
    }

    def "desc by column name renders DESC"() {
        expect:
            assertSql(dialect.toSqlPart(DSL.desc("name")), '"name" DESC')
    }

    def "asc by index"() {
        expect:
            assertSql(dialect.toSqlPart(DSL.asc(2)), '2 ASC')
    }

    def "desc by index"() {
        expect:
            assertSql(dialect.toSqlPart(DSL.desc(3)), '3 DESC')
    }

    def "Order with asc=false renders DESC"() {
        expect:
            assertSql(dialect.toSqlPart(new Order(col(Integer, "age"), false)), '"age" DESC')
    }

    def "Order with asc=true renders ASC"() {
        expect:
            assertSql(dialect.toSqlPart(new Order(col(Integer, "age"), true)), '"age" ASC')
    }
}
