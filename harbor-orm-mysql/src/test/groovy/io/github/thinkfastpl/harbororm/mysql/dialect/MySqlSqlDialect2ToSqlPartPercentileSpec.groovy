// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mysql.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Order
import spock.lang.Specification

import static io.github.thinkfastpl.harbororm.mysql.dialect.MySqlDialectTestSupport.col

class MySqlSqlDialect2ToSqlPartPercentileSpec extends Specification {

    def dialect = new MySqlSqlDialect()

    def "percentile_cont with fraction param throws UnsupportedOperationException"() {
        given:
            def win = DSL.percentileCont(0.5d, new Order(col(Double, "x"), true))
        when:
            dialect.toSqlPart(win.getFunction(), null)
        then:
            def e = thrown(UnsupportedOperationException)
            e.message == "percentile_cont is not supported by MySQL dialect"
    }

    def "percentile_disc with DESC order throws UnsupportedOperationException"() {
        given:
            def win = DSL.percentileDisc(0.25d, new Order(col(Double, "x"), false))
        when:
            dialect.toSqlPart(win.getFunction(), null)
        then:
            def e = thrown(UnsupportedOperationException)
            e.message == "percentile_disc is not supported by MySQL dialect"
    }

    def "percentile_cont with expression fraction and multiple orders throws UnsupportedOperationException"() {
        given:
            def win = DSL.percentileCont(
                    DSL.constant(0.5d),
                    new Order(col(Double, "x"), true),
                    new Order(col(Double, "y"), false)
            )
        when:
            dialect.toSqlPart(win.getFunction(), null)
        then:
            def e = thrown(UnsupportedOperationException)
            e.message == "percentile_cont is not supported by MySQL dialect"
    }

    def "percentile_cont as window expression throws UnsupportedOperationException"() {
        given:
            def win = DSL.percentileCont(0.5d, new Order(col(Double, "x"), true))
            win.partitionBy(col(Integer, "g"))
        when:
            dialect.toSqlPart(win, null)
        then:
            def e = thrown(UnsupportedOperationException)
            e.message == "percentile_cont is not supported by MySQL dialect"
    }

    def "percentile_cont with empty window spec throws UnsupportedOperationException"() {
        given:
            def win = DSL.percentileCont(0.5d, new Order(col(Double, "x"), true))
        when:
            dialect.toSqlPart(win, null)
        then:
            def e = thrown(UnsupportedOperationException)
            e.message == "percentile_cont is not supported by MySQL dialect"
    }
}
