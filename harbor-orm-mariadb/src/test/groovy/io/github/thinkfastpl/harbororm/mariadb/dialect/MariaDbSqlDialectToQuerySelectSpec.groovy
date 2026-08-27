// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.dialect

import io.github.thinkfastpl.harbororm.api.expression.*
import io.github.thinkfastpl.harbororm.api.query.data.SelectQueryData
import io.github.thinkfastpl.harbororm.api.query.result.Record
import spock.lang.Specification

import java.util.function.Function

import static io.github.thinkfastpl.harbororm.mariadb.dialect.MariaDbDialectTestSupport.*

class MariaDbSqlDialectToQuerySelectSpec extends Specification {

    def dialect = newDialect()

    private static <T> DefaultSelectExpression<T> emptySelect(Class<T> javaType = Object.class) {
        new DefaultSelectExpression<T>(false, { r -> null } as Function<Record, T>, javaType)
    }

    def "empty SelectQueryData renders just SELECT"() {
        given:
            def data = new SelectQueryData(
                    null, false, false,
                    [], null, null, null, null, null, null, null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT')
    }

    def "SELECT columns FROM table"() {
        given:
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a"), col(Integer, "b")],
                    table("t"),
                    null, null, null, null, null, null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a`, `b` FROM `t`')
    }

    def "SELECT DISTINCT renders DISTINCT keyword"() {
        given:
            def data = new SelectQueryData(
                    null, false, true,
                    [col(Integer, "a")],
                    table("t"),
                    null, null, null, null, null, null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT DISTINCT `a` FROM `t`')
    }

    def "single WHERE renders WHERE clause"() {
        given:
            def cond = new BinaryOperatorCondition(col(Integer, "a"), col(Integer, "b"), BinaryOperatorCondition.Operator.EQ)
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    null,
                    [cond],
                    null, null, null, null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` WHERE `a` = `b`')
    }

    def "WHERE multiple conditions joined by AND"() {
        given:
            def c1 = new BinaryOperatorCondition(col(Integer, "a"), col(Integer, "b"), BinaryOperatorCondition.Operator.EQ)
            def c2 = new BinaryOperatorCondition(col(Integer, "c"), col(Integer, "d"), BinaryOperatorCondition.Operator.EQ)
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    null,
                    [c1, c2],
                    null, null, null, null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` WHERE `a` = `b` AND `c` = `d`')
    }

    def "INNER JOIN with ON condition"() {
        given:
            def onCond = new BinaryOperatorCondition(col(Integer, "a", "t"), col(Integer, "a", "u"), BinaryOperatorCondition.Operator.EQ)
            def join = new Join(table("u"), Join.Type.INNER, onCond, false)
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    [join],
                    null, null, null, null, null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` INNER JOIN `u` ON `t`.`a` = `u`.`a`')
    }

    def "LEFT JOIN renders LEFT JOIN keyword"() {
        given:
            def onCond = new BinaryOperatorCondition(col(Integer, "a", "t"), col(Integer, "a", "u"), BinaryOperatorCondition.Operator.EQ)
            def join = new Join(table("u"), Join.Type.LEFT, onCond, false)
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    [join],
                    null, null, null, null, null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` LEFT JOIN `u` ON `t`.`a` = `u`.`a`')
    }

    def "RIGHT JOIN renders RIGHT JOIN keyword"() {
        given:
            def onCond = new BinaryOperatorCondition(col(Integer, "a", "t"), col(Integer, "a", "u"), BinaryOperatorCondition.Operator.EQ)
            def join = new Join(table("u"), Join.Type.RIGHT, onCond, false)
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    [join],
                    null, null, null, null, null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` RIGHT JOIN `u` ON `t`.`a` = `u`.`a`')
    }

    def "FULL OUTER JOIN throws UnsupportedOperationException on MariaDB"() {
        given:
            def onCond = new BinaryOperatorCondition(col(Integer, "a", "t"), col(Integer, "a", "u"), BinaryOperatorCondition.Operator.EQ)
            def join = new Join(table("u"), Join.Type.FULL_OUTER, onCond, false)
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    [join],
                    null, null, null, null, null, null, null, false
            )
        when:
            dialect.toQuery(data)
        then:
            thrown(UnsupportedOperationException)
    }

    def "CROSS JOIN has no ON clause"() {
        given:
            def join = new Join(table("u"), Join.Type.CROSS, null, false)
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    [join],
                    null, null, null, null, null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` CROSS JOIN `u`')
    }

    def "LATERAL inner join throws UnsupportedOperationException on MariaDB"() {
        given:
            def onCond = new BinaryOperatorCondition(col(Integer, "a", "t"), col(Integer, "a", "u"), BinaryOperatorCondition.Operator.EQ)
            def join = new Join(table("u"), Join.Type.INNER, onCond, true)
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    [join],
                    null, null, null, null, null, null, null, false
            )
        when:
            dialect.toQuery(data)
        then:
            thrown(UnsupportedOperationException)
    }

    def "GROUP BY single expression"() {
        given:
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    null, null,
                    [col(Integer, "a")],
                    null, null, null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` GROUP BY `a`')
    }

    def "GROUP BY with HAVING"() {
        given:
            def having = new BinaryOperatorCondition(col(Integer, "a"), col(Integer, "b"), BinaryOperatorCondition.Operator.EQ)
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    null, null,
                    [col(Integer, "a")],
                    [having],
                    null, null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` GROUP BY `a` HAVING `a` = `b`')
    }

    def "ORDER BY single"() {
        given:
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    null, null, null, null, null,
                    [new Order(col(Integer, "a"), true)],
                    null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` ORDER BY `a` ASC')
    }

    def "ORDER BY multiple"() {
        given:
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    null, null, null, null, null,
                    [new Order(col(Integer, "a"), true), new Order(col(Integer, "b"), false)],
                    null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` ORDER BY `a` ASC, `b` DESC')
    }

    def "LIMIT only"() {
        given:
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    null, null, null, null, null, null,
                    10, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` LIMIT 10')
    }

    def "OFFSET only (MariaDB requires LIMIT when OFFSET is set; sentinel is bigint max)"() {
        given:
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    null, null, null, null, null, null,
                    null, 5, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` LIMIT 18446744073709551615 OFFSET 5')
    }

    def "LIMIT and OFFSET combined"() {
        given:
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    null, null, null, null, null, null,
                    10, 5, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` LIMIT 10 OFFSET 5')
    }

    def "FOR UPDATE renders FOR UPDATE at the end"() {
        given:
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    null, null, null, null, null, null, null, null, true
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` FOR UPDATE')
    }

    def "UNION combination wraps right-hand SELECT in parens"() {
        given:
            def rhs = emptySelect()
            rhs.from(table("u"))
            rhs.select(col(Integer, "b"))
            def combination = new SelectQueryData.Combination(rhs, SelectCombination.UNION, false)
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    null, null, null, null,
                    [combination],
                    null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` UNION (SELECT `b` FROM `u`)')
    }

    def "UNION ALL renders ALL modifier"() {
        given:
            def rhs = emptySelect()
            rhs.from(table("u"))
            rhs.select(col(Integer, "b"))
            def combination = new SelectQueryData.Combination(rhs, SelectCombination.UNION, true)
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    null, null, null, null,
                    [combination],
                    null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` UNION ALL (SELECT `b` FROM `u`)')
    }

    def "INTERSECT combination renders INTERSECT keyword"() {
        given:
            def rhs = emptySelect()
            rhs.from(table("u"))
            rhs.select(col(Integer, "b"))
            def combination = new SelectQueryData.Combination(rhs, SelectCombination.INTERSECT, false)
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    null, null, null, null,
                    [combination],
                    null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` INTERSECT (SELECT `b` FROM `u`)')
    }

    def "EXCEPT combination renders EXCEPT keyword"() {
        given:
            def rhs = emptySelect()
            rhs.from(table("u"))
            rhs.select(col(Integer, "b"))
            def combination = new SelectQueryData.Combination(rhs, SelectCombination.EXCEPT, false)
            def data = new SelectQueryData(
                    null, false, false,
                    [col(Integer, "a")],
                    table("t"),
                    null, null, null, null,
                    [combination],
                    null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'SELECT `a` FROM `t` EXCEPT (SELECT `b` FROM `u`)')
    }

    def "single non-recursive CTE in WITH clause"() {
        given:
            def cte = new CommonTableExpression("cte")
            def cteCol = cte.column("c1", Integer.class)
            def inner = emptySelect()
            inner.from(table("t"))
            inner.select(col(Integer, "a"))
            cte.as(inner)
            def data = new SelectQueryData(
                    [cte], false, false,
                    [cteCol],
                    cte,
                    null, null, null, null, null, null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'WITH `cte`(`c1`) AS (SELECT `a` AS `c1` FROM `t`) SELECT `cte`.`c1` FROM `cte`')
    }

    def "WITH RECURSIVE renders RECURSIVE keyword"() {
        given:
            def cte = new CommonTableExpression("cte")
            def cteCol = cte.column("c1", Integer.class)
            def inner = emptySelect()
            inner.from(table("t"))
            inner.select(col(Integer, "a"))
            cte.as(inner)
            def data = new SelectQueryData(
                    [cte], true, false,
                    [cteCol],
                    cte,
                    null, null, null, null, null, null, null, null, false
            )
        expect:
            assertSql(dialect.toQuery(data), 'WITH RECURSIVE `cte`(`c1`) AS (SELECT `a` AS `c1` FROM `t`) SELECT `cte`.`c1` FROM `cte`')
    }
}
