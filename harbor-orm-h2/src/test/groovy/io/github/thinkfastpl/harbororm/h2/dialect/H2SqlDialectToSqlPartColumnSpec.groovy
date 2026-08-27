// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.*
import spock.lang.Specification

import static io.github.thinkfastpl.harbororm.h2.dialect.H2SqlDialectTestSupport.*

class H2SqlDialectToSqlPartColumnSpec extends Specification {

    def dialect = newDialect()

    def "QColumn without table alias renders quoted column name"() {
        expect:
            assertSql(dialect.toSqlPart(col(Integer, "id"), null), '"id"')
    }

    def "QColumn with table alias renders table.column"() {
        expect:
            assertSql(dialect.toSqlPart(col(Integer, "id", "u"), null), '"u"."id"')
    }

    def "QColumn with column alias renders col AS alias"() {
        expect:
            assertSql(dialect.toSqlPart(col(Integer, "id").as("ident"), null), '"id" AS "ident"')
    }

    def "QColumn with table alias and column alias renders table.col AS alias"() {
        expect:
            assertSql(dialect.toSqlPart(col(Integer, "id", "u").as("ident"), null), '"u"."id" AS "ident"')
    }

    def "ExcludedColumnExpression renders EXCLUDED.\"col\""() {
        expect:
            assertSql(
                    dialect.toSqlPart(new ExcludedColumnExpression<>(col(Integer, "id")), null),
                    'EXCLUDED."id"'
            )
    }

    def "CommonTableExpression.Column renders cte.column"() {
        given:
            def cte = new CommonTableExpression("cte_t")
            def column = cte.column("c", Integer.class)
        expect:
            assertSql(dialect.toSqlPart(column, null), '"cte_t"."c"')
    }

    def "AliasedExpression wraps inner expression with AS alias"() {
        expect:
            assertSql(
                    dialect.toSqlPart(new AliasedExpression<>(col(Integer, "id"), "x"), null),
                    '"id" AS "x"'
            )
    }

    def "ParenthesesExpression wraps inner expression in parentheses"() {
        expect:
            assertSql(
                    dialect.toSqlPart(new ParenthesesExpression<>(col(Integer, "id")), null),
                    '("id")'
            )
    }

    def "CastExpression to Boolean uses H2-specific BOOLEAN keyword"() {
        expect:
            assertSql(
                    dialect.toSqlPart(new CastExpression<>(col(Integer, "x"), Boolean.class), null),
                    'CAST("x" AS BOOLEAN)'
            )
    }

    def "CastExpression to Double uses H2-specific DOUBLE PRECISION keyword"() {
        expect:
            assertSql(
                    dialect.toSqlPart(new CastExpression<>(col(Integer, "x"), Double.class), null),
                    'CAST("x" AS DOUBLE PRECISION)'
            )
    }

    def "CastExpression to Long falls through to inherited lowercase bigint"() {
        expect:
            assertSql(
                    dialect.toSqlPart(new CastExpression<>(col(Integer, "x"), Long.class), null),
                    'CAST("x" AS bigint)'
            )
    }

    def "CastExpression to String falls through to inherited lowercase varchar"() {
        expect:
            assertSql(
                    dialect.toSqlPart(new CastExpression<>(col(Integer, "x"), String.class), null),
                    'CAST("x" AS varchar)'
            )
    }
}
