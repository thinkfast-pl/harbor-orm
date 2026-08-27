// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.expression.PortableFunctionExpression
import spock.lang.Specification

import java.sql.Types

import static io.github.thinkfastpl.harbororm.h2.dialect.H2SqlDialectTestSupport.*

class H2SqlDialectToSqlPartFunctionsSpec extends Specification {

    def dialect = newDialect()

    // ---------- concat ----------

    def "concat with 1 arg pads to 2 with NULL"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.CONCAT,
                    [col(String, "a")] as List<Expression<?>>,
                    String.class
            )
        expect:
            assertSql(
                    dialect.toSqlPart(fn, null),
                    'concat("a", CAST(? AS VARCHAR))',
                    [[null, Types.VARCHAR]]
            )
    }

    def "concat with 2 args is unchanged"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.CONCAT,
                    [col(String, "a"), col(String, "b")] as List<Expression<?>>,
                    String.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'concat("a", "b")')
    }

    def "concat with 3 args is unchanged"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.CONCAT,
                    [col(String, "a"), col(String, "b"), col(String, "c")] as List<Expression<?>>,
                    String.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'concat("a", "b", "c")')
    }

    // ---------- concat_ws ----------

    def "concat_ws with 2 args pads to 3 with NULL"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.CONCAT_WS,
                    [col(String, "s"), col(String, "a")] as List<Expression<?>>,
                    String.class
            )
        expect:
            assertSql(
                    dialect.toSqlPart(fn, null),
                    'concat_ws("s", "a", CAST(? AS VARCHAR))',
                    [[null, Types.VARCHAR]]
            )
    }

    def "concat_ws with 3 args is unchanged"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.CONCAT_WS,
                    [col(String, "s"), col(String, "a"), col(String, "b")] as List<Expression<?>>,
                    String.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'concat_ws("s", "a", "b")')
    }

    // ---------- cbrt ----------

    def "cbrt with 1 column param renders SIGN times POWER over ABS"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.CBRT,
                    [col(Double, "x")] as List<Expression<?>>,
                    Double.class
            )
        expect:
            assertSql(
                    dialect.toSqlPart(fn, null),
                    '(SIGN("x") * POWER(ABS("x"), 1.0 / 3))'
            )
    }

    def "cbrt with 0 params throws IllegalArgumentException"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.CBRT,
                    [] as List<Expression<?>>,
                    Double.class
            )
        when:
            dialect.toSqlPart(fn, null)
        then:
            def e = thrown(IllegalArgumentException)
            e.message == "cbrt function must have exactly one param"
    }

    // ---------- num_nulls / num_nonnulls ----------

    def "num_nulls renders sum of CASE WHEN IS NULL THEN 1 ELSE 0"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.NUM_NULLS,
                    [col(Integer, "a"), col(Integer, "b")] as List<Expression<?>>,
                    Integer.class
            )
        expect:
            assertSql(
                    dialect.toSqlPart(fn, null),
                    'CASE WHEN "a" IS NULL THEN CAST(? AS INT) ELSE CAST(? AS INT) END + ' +
                            'CASE WHEN "b" IS NULL THEN CAST(? AS INT) ELSE CAST(? AS INT) END',
                    [[1, Types.INTEGER], [0, Types.INTEGER], [1, Types.INTEGER], [0, Types.INTEGER]]
            )
    }

    def "num_nonnulls renders sum of CASE WHEN IS NOT NULL THEN 1 ELSE 0"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.NUM_NON_NULLS,
                    [col(Integer, "a"), col(Integer, "b")] as List<Expression<?>>,
                    Integer.class
            )
        expect:
            assertSql(
                    dialect.toSqlPart(fn, null),
                    'CASE WHEN "a" IS NOT NULL THEN CAST(? AS INT) ELSE CAST(? AS INT) END + ' +
                            'CASE WHEN "b" IS NOT NULL THEN CAST(? AS INT) ELSE CAST(? AS INT) END',
                    [[1, Types.INTEGER], [0, Types.INTEGER], [1, Types.INTEGER], [0, Types.INTEGER]]
            )
    }

    // ---------- json_array_length ----------

    def "json_array_length renders CARDINALITY(... FORMAT JSON) on H2"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.JSON_ARRAY_LENGTH,
                    [col(String, "x")] as List<Expression<?>>,
                    Integer.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'CARDINALITY("x" FORMAT JSON)')
    }

    // ---------- Inherited paths (verify they still work on H2) ----------

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
}
