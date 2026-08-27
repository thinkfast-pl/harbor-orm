// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Order
import spock.lang.Specification

import static io.github.thinkfastpl.harbororm.postgres.dialect.PostgreSqlDialectTestSupport.col

class PostgreSqlDialect2ToSqlPartPercentileSpec extends Specification {

    def dialect = new PostgreSqlDialect()

    def "percentile_cont throws UnsupportedOperationException"() {
        given:
            def win = DSL.percentileCont(0.5d, new Order(col(Double, "x"), true))

        when:
            dialect.toSqlPart(win.getFunction(), null)

        then:
            def e = thrown(UnsupportedOperationException)
            e.message == "percentile_cont() is not supported on PostgreSQL."
    }

    def "percentile_disc throws UnsupportedOperationException"() {
        given:
            def win = DSL.percentileDisc(0.25d, new Order(col(Double, "x"), false))

        when:
            dialect.toSqlPart(win.getFunction(), null)

        then:
            def e = thrown(UnsupportedOperationException)
            e.message == "percentile_disc() is not supported on PostgreSQL."
    }

    def "percentile_cont as window expression throws UnsupportedOperationException"() {
        given:
            def win = DSL.percentileCont(0.5d, new Order(col(Double, "x"), true))
            win.partitionBy(col(Integer, "g"))

        when:
            dialect.toSqlPart(win, null)

        then:
            thrown(UnsupportedOperationException)
    }
}
