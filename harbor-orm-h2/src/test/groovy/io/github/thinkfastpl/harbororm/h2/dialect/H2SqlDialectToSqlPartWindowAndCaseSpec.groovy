// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.*
import io.github.thinkfastpl.harbororm.api.expression.CaseWhenThenExpression.WhenThen
import spock.lang.Specification

import static io.github.thinkfastpl.harbororm.h2.dialect.H2SqlDialectTestSupport.*

class H2SqlDialectToSqlPartWindowAndCaseSpec extends Specification {

    def dialect = newDialect()

    // ---------- WindowExpression ----------

    private static rowNumberFn() {
        new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.ROW_NUMBER,
                [] as List<Expression<?>>,
                Long.class
        )
    }

    def "row_number with empty OVER renders empty parentheses"() {
        given:
            def win = new DefaultWindowExpression<>(rowNumberFn(), Long.class)
        expect:
            assertSql(dialect.toSqlPart(win, null), 'row_number() OVER ()')
    }

    def "row_number with PARTITION BY only"() {
        given:
            def win = new DefaultWindowExpression<>(rowNumberFn(), Long.class)
            win.partitionBy(col(Integer, "g"))
        expect:
            assertSql(dialect.toSqlPart(win, null), 'row_number() OVER (PARTITION BY "g")')
    }

    def "row_number with ORDER BY only"() {
        given:
            def win = new DefaultWindowExpression<>(rowNumberFn(), Long.class)
            win.orderBy(DSL.asc("x"))
        expect:
            assertSql(dialect.toSqlPart(win, null), 'row_number() OVER (ORDER BY "x" ASC)')
    }

    def "row_number with PARTITION BY and ORDER BY"() {
        given:
            def win = new DefaultWindowExpression<>(rowNumberFn(), Long.class)
            win.partitionBy(col(Integer, "g"))
            win.orderBy(DSL.asc("x"))
        expect:
            assertSql(
                    dialect.toSqlPart(win, null),
                    'row_number() OVER (PARTITION BY "g" ORDER BY "x" ASC)'
            )
    }

    def "row_number with ROWS frame"() {
        given:
            def win = new DefaultWindowExpression<>(rowNumberFn(), Long.class)
            win.rowsBetween(FrameBound.UNBOUNDED_PRECEDING, FrameBound.CURRENT_ROW)
        expect:
            assertSql(
                    dialect.toSqlPart(win, null),
                    'row_number() OVER (ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)'
            )
    }

    def "row_number with PARTITION BY + ORDER BY + ROWS frame"() {
        given:
            def win = new DefaultWindowExpression<>(rowNumberFn(), Long.class)
            win.partitionBy(col(Integer, "g"))
            win.orderBy(DSL.asc("x"))
            win.rowsBetween(FrameBound.UNBOUNDED_PRECEDING, FrameBound.CURRENT_ROW)
        expect:
            assertSql(
                    dialect.toSqlPart(win, null),
                    'row_number() OVER (PARTITION BY "g" ORDER BY "x" ASC ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)'
            )
    }

    // ---------- CaseWhenThenExpression ----------

    def "CASE WHEN ... THEN ... END with one branch, no ELSE"() {
        given:
            def cond = col(Integer, "a").eq(col(Integer, "b"))
            def then_ = col(Integer, "c")
            def expr = new CaseWhenThenExpression<>(
                    [new WhenThen<>(cond, then_)],
                    null,
                    Integer.class
            )
        expect:
            assertSql(dialect.toSqlPart(expr, null), 'CASE WHEN "a" = "b" THEN "c" END')
    }

    def "CASE WHEN ... THEN ... ELSE ... END with one branch and ELSE"() {
        given:
            def cond = col(Integer, "a").eq(col(Integer, "b"))
            def then_ = col(Integer, "c")
            def else_ = col(Integer, "d")
            def expr = new CaseWhenThenExpression<>(
                    [new WhenThen<>(cond, then_)],
                    else_,
                    Integer.class
            )
        expect:
            assertSql(dialect.toSqlPart(expr, null), 'CASE WHEN "a" = "b" THEN "c" ELSE "d" END')
    }

    def "CASE with null then-expression renders THEN NULL"() {
        given:
            def cond = col(Integer, "a").eq(col(Integer, "b"))
            def expr = new CaseWhenThenExpression<>(
                    [new WhenThen<>(cond, null)],
                    null,
                    Integer.class
            )
        expect:
            assertSql(dialect.toSqlPart(expr, null), 'CASE WHEN "a" = "b" THEN NULL END')
    }

    def "CASE with two WHEN/THEN branches and ELSE"() {
        given:
            def cond1 = col(Integer, "a").eq(col(Integer, "b"))
            def cond2 = col(Integer, "d").eq(col(Integer, "e"))
            def expr = new CaseWhenThenExpression<>(
                    [
                            new WhenThen<>(cond1, col(Integer, "c")),
                            new WhenThen<>(cond2, col(Integer, "f"))
                    ],
                    col(Integer, "g"),
                    Integer.class
            )
        expect:
            assertSql(
                    dialect.toSqlPart(expr, null),
                    'CASE WHEN "a" = "b" THEN "c" WHEN "d" = "e" THEN "f" ELSE "g" END'
            )
    }
}
