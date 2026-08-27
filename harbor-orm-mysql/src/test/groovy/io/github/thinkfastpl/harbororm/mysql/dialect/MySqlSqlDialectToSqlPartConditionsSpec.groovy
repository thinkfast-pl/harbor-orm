// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mysql.dialect

import io.github.thinkfastpl.harbororm.api.expression.*
import io.github.thinkfastpl.harbororm.api.expression.BinaryOperatorCondition.Operator as BinOp
import io.github.thinkfastpl.harbororm.api.expression.BinaryOperatorExpression.Operator as BinExprOp
import io.github.thinkfastpl.harbororm.api.expression.ComplexCondition.Operator as CondOp
import io.github.thinkfastpl.harbororm.api.expression.TernaryOperatorCondition.Operator as TernOp
import io.github.thinkfastpl.harbororm.api.expression.UnaryOperatorCondition.Operator as UnOp
import spock.lang.Specification

import static io.github.thinkfastpl.harbororm.mysql.dialect.MySqlDialectTestSupport.*

class MySqlSqlDialectToSqlPartConditionsSpec extends Specification {

    def dialect = newDialect()

    def "UnaryOperatorCondition #op renders #expected"() {
        given:
            def expr = new UnaryOperatorCondition(col(Boolean, "x"), op)
        expect:
            assertSql(dialect.toSqlPart(expr, null), expected)
        where:
            op                  || expected
            UnOp.NOT            || 'NOT `x`'
            UnOp.IS_NULL        || '`x` IS NULL'
            UnOp.IS_NOT_NULL    || '`x` IS NOT NULL'
            UnOp.IS_TRUE        || '`x` IS TRUE'
            UnOp.IS_NOT_TRUE    || '`x` IS NOT TRUE'
            UnOp.IS_FALSE       || '`x` IS FALSE'
            UnOp.IS_NOT_FALSE   || '`x` IS NOT FALSE'
            UnOp.IS_UNKNOWN     || '`x` IS UNKNOWN'
            UnOp.IS_NOT_UNKNOWN || '`x` IS NOT UNKNOWN'
    }

    def "BinaryOperatorCondition #op (col vs col) renders #expected"() {
        given:
            def expr = new BinaryOperatorCondition(col(Integer, "a"), col(Integer, "b"), op)
        expect:
            assertSql(dialect.toSqlPart(expr, null), expected)
        where:
            op                          || expected
            BinOp.EQ                    || '`a` = `b`'
            BinOp.NOT_EQ                || '`a` != `b`'
            BinOp.LT                    || '`a` < `b`'
            BinOp.LE                    || '`a` <= `b`'
            BinOp.GT                    || '`a` > `b`'
            BinOp.GE                    || '`a` >= `b`'
            BinOp.IS_DISTINCT_FROM      || 'NOT (`a` <=> `b`)'
            BinOp.IS_NOT_DISTINCT_FROM  || '`a` <=> `b`'
            BinOp.IN                    || '`a` IN (`b`)'
    }

    def "TernaryOperatorCondition #op renders #expected"() {
        given:
            def expr = new TernaryOperatorCondition(
                    col(Integer, "a"),
                    col(Integer, "b"),
                    col(Integer, "c"),
                    op
            )
        expect:
            assertSql(dialect.toSqlPart(expr, null), expected)
        where:
            op                              || expected
            TernOp.BETWEEN                  || '`a` BETWEEN `b` AND `c`'
            TernOp.NOT_BETWEEN              || '`a` NOT BETWEEN `b` AND `c`'
            TernOp.BETWEEN_SYMMETRIC        || '(`a` BETWEEN LEAST(`b`, `c`) AND GREATEST(`b`, `c`))'
            TernOp.NOT_BETWEEN_SYMMETRIC    || '(`a` NOT BETWEEN LEAST(`b`, `c`) AND GREATEST(`b`, `c`))'
            TernOp.LIKE_ESCAPE              || '`a` LIKE `b` ESCAPE `c`'
    }

    def "ComplexCondition AND of two equalities"() {
        given:
            def eq1 = new BinaryOperatorCondition(col(Integer, "a"), col(Integer, "b"), BinOp.EQ)
            def eq2 = new BinaryOperatorCondition(col(Integer, "c"), col(Integer, "d"), BinOp.EQ)
            def cond = new ComplexCondition(eq1, CondOp.AND, eq2)
        expect:
            assertSql(dialect.toSqlPart(cond, null), '`a` = `b` AND `c` = `d`')
    }

    def "ComplexCondition chained with .and() and .or()"() {
        given:
            def eq1 = new BinaryOperatorCondition(col(Integer, "a"), col(Integer, "b"), BinOp.EQ)
            def eq2 = new BinaryOperatorCondition(col(Integer, "c"), col(Integer, "d"), BinOp.EQ)
            def eq3 = new BinaryOperatorCondition(col(Integer, "e"), col(Integer, "f"), BinOp.EQ)
            def cond = new ComplexCondition(eq1, CondOp.AND, eq2).or(eq3)
        expect:
            assertSql(dialect.toSqlPart(cond, null), '`a` = `b` AND `c` = `d` OR `e` = `f`')
    }

    def "ExpressionBooleanCondition wraps boolean column transparently"() {
        given:
            def cond = new ExpressionBooleanCondition(col(Boolean, "flag"))
        expect:
            assertSql(dialect.toSqlPart(cond, null), '`flag`')
    }

    def "BinaryOperatorExpression #op renders #expected"() {
        given:
            def expr = new BinaryOperatorExpression(col(Integer, "a"), col(Integer, "b"), op, Integer.class)
        expect:
            assertSql(dialect.toSqlPart(expr, null), expected)
        where:
            op                          || expected
            BinExprOp.ADDITION || '`a` + `b`'
            BinExprOp.SUBTRACTION || '`a` - `b`'
            BinExprOp.MULTIPLICATION    || '`a` * `b`'
            BinExprOp.DIVISION || '`a` / `b`'
            BinExprOp.MODULO            || '`a` % `b`'
            BinExprOp.EXPONENTIATION    || '`a` ^ `b`'
    }

    def "IN with ConstantsExpression renders as IN (?,?,?) - no PG array rewrite"() {
        given:
            def cond = new BinaryOperatorCondition(
                    col(Integer, "a"),
                    new ConstantsExpression<>(Integer.class, [1, 2, 3] as Collection),
                    BinOp.IN
            )
        when:
            def sql = dialect.toSqlPart(cond, null)
        then:
            sql.sql.toString() == '`a` IN (?,?,?)'
            sql.params.size() == 3
    }
}
