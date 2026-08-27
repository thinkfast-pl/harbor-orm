// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.expression.PortableFunctionExpression
import spock.lang.Specification

import static io.github.thinkfastpl.harbororm.postgres.dialect.PostgreSqlDialectTestSupport.*

class PostgreSqlDialectToSqlPartFunctionsSpec extends Specification {

    def dialect = newDialect()

    // ---------- concat ----------

    def "concat with 1 arg is unchanged"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.CONCAT,
                    [col(String, "a")] as List<Expression<?>>,
                    String.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'concat("a")')
    }

    def "concat with multiple args is unchanged"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.CONCAT,
                    [col(String, "a"), col(String, "b"), col(String, "c")] as List<Expression<?>>,
                    String.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'concat("a", "b", "c")')
    }

    // ---------- log ----------

    def "log with Float args casts to numeric"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.LOG,
                    [col(Float, "a"), col(Float, "b")] as List<Expression<?>>,
                    Double.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'log(CAST("a" AS numeric), CAST("b" AS numeric))')
    }

    def "log with Double args casts to numeric"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.LOG,
                    [col(Double, "a"), col(Double, "b")] as List<Expression<?>>,
                    Double.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'log(CAST("a" AS numeric), CAST("b" AS numeric))')
    }

    def "log with BigDecimal args is unchanged"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.LOG,
                    [col(BigDecimal, "a"), col(BigDecimal, "b")] as List<Expression<?>>,
                    BigDecimal.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'log("a", "b")')
    }

    // ---------- Inherited paths (verify still work on PG) ----------

    def "POSITION inherited renders as position(a IN b)"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.POSITION,
                    [col(String, "a"), col(String, "b")] as List<Expression<?>>,
                    Integer.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'position("a" IN "b")')
    }

    def "ABS inherited renders as abs(...)"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.ABS,
                    [col(Integer, "x")] as List<Expression<?>>,
                    Integer.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'abs("x")')
    }

    // ---------- getDbTypeName overrides (exercised via CastExpression) ----------

    def "CAST to Boolean uses PG type name 'boolean'"() {
        expect:
            assertSql(dialect.toSqlPart(col(Integer, "x").cast(Boolean), null),
                    'CAST("x" AS boolean)')
    }

    def "CAST to Byte uses PG type name 'smallint'"() {
        expect:
            assertSql(dialect.toSqlPart(col(Integer, "x").cast(Byte), null),
                    'CAST("x" AS smallint)')
    }

    def "CAST to Double uses PG type name 'double precision'"() {
        expect:
            assertSql(dialect.toSqlPart(col(Integer, "x").cast(Double), null),
                    'CAST("x" AS double precision)')
    }
}
