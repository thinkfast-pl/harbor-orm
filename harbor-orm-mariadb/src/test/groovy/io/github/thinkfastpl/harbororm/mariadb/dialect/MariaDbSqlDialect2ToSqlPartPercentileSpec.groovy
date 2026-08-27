// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Order
import spock.lang.Specification

import java.sql.Types

import static io.github.thinkfastpl.harbororm.mariadb.dialect.MariaDbDialectTestSupport.assertSql
import static io.github.thinkfastpl.harbororm.mariadb.dialect.MariaDbDialectTestSupport.col

class MariaDbSqlDialect2ToSqlPartPercentileSpec extends Specification {

    def dialect = new MariaDbSqlDialect()

    def "percentile_cont renders WITHIN GROUP with fraction param"() {
        given:
            def win = DSL.percentileCont(0.5d, new Order(col(Double, "x"), true))
        expect:
            assertSql(
                    dialect.toSqlPart(win.getFunction(), null),
                    'percentile_cont(?) WITHIN GROUP (ORDER BY `x` ASC)',
                    [[0.5d, Types.DOUBLE]]
            )
    }

    def "percentile_disc renders WITHIN GROUP with DESC order"() {
        given:
            def win = DSL.percentileDisc(0.25d, new Order(col(Double, "x"), false))
        expect:
            assertSql(
                    dialect.toSqlPart(win.getFunction(), null),
                    'percentile_disc(?) WITHIN GROUP (ORDER BY `x` DESC)',
                    [[0.25d, Types.DOUBLE]]
            )
    }

    def "percentile_cont with expression fraction and multiple orders"() {
        given:
            def win = DSL.percentileCont(
                    DSL.constant(0.5d),
                    new Order(col(Double, "x"), true),
                    new Order(col(Double, "y"), false)
            )
        expect:
            assertSql(
                    dialect.toSqlPart(win.getFunction(), null),
                    'percentile_cont(?) WITHIN GROUP (ORDER BY `x` ASC, `y` DESC)',
                    [[0.5d, Types.DOUBLE]]
            )
    }

    def "percentile_cont as window expression renders OVER"() {
        given:
            def win = DSL.percentileCont(0.5d, new Order(col(Double, "x"), true))
            win.partitionBy(col(Integer, "g"))
        expect:
            assertSql(
                    dialect.toSqlPart(win, null),
                    'percentile_cont(?) WITHIN GROUP (ORDER BY `x` ASC) OVER (PARTITION BY `g`)',
                    [[0.5d, Types.DOUBLE]]
            )
    }

    def "percentile_cont with empty window spec renders empty OVER"() {
        given:
            def win = DSL.percentileCont(0.5d, new Order(col(Double, "x"), true))
        expect:
            assertSql(
                    dialect.toSqlPart(win, null),
                    'percentile_cont(?) WITHIN GROUP (ORDER BY `x` ASC) OVER ()',
                    [[0.5d, Types.DOUBLE]]
            )
    }
}
