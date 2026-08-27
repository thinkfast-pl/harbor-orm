// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.dialect

import io.github.thinkfastpl.harbororm.api.expression.*
import spock.lang.Specification

import java.sql.Types

import static io.github.thinkfastpl.harbororm.mariadb.dialect.MariaDbDialectTestSupport.*

class MariaDbSqlDialectToSqlPartFunctionsSpec extends Specification {

    def dialect = newDialect()

    // ---------- concat ----------

    def "concat with empty params injects empty-string constant"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.CONCAT,
                    [] as List<Expression<?>>,
                    String.class
            )
        when:
            def sql = dialect.toSqlPart(fn, null)
        then:
            sql.sql.toString() == 'concat(?)'
            sql.params.size() == 1
            sql.params[0].value == ""
    }

    def "concat with 1 arg is unchanged"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.CONCAT,
                    [col(String, "a")] as List<Expression<?>>,
                    String.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'concat(`a`)')
    }

    def "concat with multiple args is unchanged"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.CONCAT,
                    [col(String, "a"), col(String, "b"), col(String, "c")] as List<Expression<?>>,
                    String.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'concat(`a`, `b`, `c`)')
    }

    def "cbrt with 1 column param renders SIGN times POW over ABS"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.CBRT,
                    [col(Double, "x")] as List<Expression<?>>,
                    Double.class
            )
        expect:
            assertSql(
                    dialect.toSqlPart(fn, null),
                    '(SIGN(`x`) * POW(ABS(`x`), 1E0 / 3E0))'
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
            e.message == "CBRT function expression must have exactly one param"
    }

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
                    'CASE WHEN `a` IS NULL THEN ? ELSE ? END + ' +
                            'CASE WHEN `b` IS NULL THEN ? ELSE ? END',
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
                    'CASE WHEN `a` IS NOT NULL THEN ? ELSE ? END + ' +
                            'CASE WHEN `b` IS NOT NULL THEN ? ELSE ? END',
                    [[1, Types.INTEGER], [0, Types.INTEGER], [1, Types.INTEGER], [0, Types.INTEGER]]
            )
    }

    // ---------- Inherited paths (verify still work on MariaDB) ----------

    def "POSITION inherited renders as position(a IN b)"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.POSITION,
                    [col(String, "a"), col(String, "b")] as List<Expression<?>>,
                    Integer.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'position(`a` IN `b`)')
    }

    def "ABS inherited renders as abs(...)"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.ABS,
                    [col(Integer, "x")] as List<Expression<?>>,
                    Integer.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'abs(`x`)')
    }

    // ---------- getDbTypeName overrides (exercised via CastExpression) ----------

    def "CAST to Boolean uses MariaDB type name 'UNSIGNED'"() {
        expect:
            assertSql(dialect.toSqlPart(col(Integer, "x").cast(Boolean), null),
                    'CAST(`x` AS UNSIGNED)')
    }

    def "CAST to Byte uses MariaDB type name 'SIGNED'"() {
        expect:
            assertSql(dialect.toSqlPart(col(Integer, "x").cast(Byte), null),
                    'CAST(`x` AS SIGNED)')
    }

    def "CAST to Double uses MariaDB type name 'DOUBLE'"() {
        expect:
            assertSql(dialect.toSqlPart(col(Integer, "x").cast(Double), null),
                    'CAST(`x` AS DOUBLE)')
    }

    def "CAST to String uses MariaDB type name 'CHAR'"() {
        expect:
            assertSql(dialect.toSqlPart(col(Integer, "x").cast(String), null),
                    'CAST(`x` AS CHAR)')
    }

    def "CAST to BigDecimal uses MariaDB type name 'DECIMAL'"() {
        expect:
            assertSql(dialect.toSqlPart(col(Integer, "x").cast(BigDecimal), null),
                    'CAST(`x` AS DECIMAL)')
    }

    // ---------- JSON support on MariaDB ----------

    def "JsonExpression EXTRACT renders JSON_EXTRACT with bare path segment"() {
        given:
            def jsonCol = col(Object, "j")
            def expr = new JsonExpression(jsonCol, JsonExpression.Operator.EXTRACT, ["k"] as String[], null)
        expect:
            assertSql(dialect.toSqlPart(expr, null), "JSON_EXTRACT(`j`, '\$.k')")
    }

    def "JsonExpression EXTRACT_PATH collapses keys into a single JSON path"() {
        given:
            def jsonCol = col(Object, "j")
            def expr = new JsonExpression(jsonCol, JsonExpression.Operator.EXTRACT_PATH, ["a", "b"] as String[], null)
        expect:
            assertSql(dialect.toSqlPart(expr, null), "JSON_EXTRACT(`j`, '\$.a.b')")
    }

    def "JsonExpression EXTRACT quotes segments with special characters"() {
        given:
            def jsonCol = col(Object, "j")
            def expr = new JsonExpression(jsonCol, JsonExpression.Operator.EXTRACT, ["win.rate"] as String[], null)
        expect:
            assertSql(dialect.toSqlPart(expr, null), "JSON_EXTRACT(`j`, '\$.\"win.rate\"')")
    }

    def "JsonExpression chain compresses to single JSON_EXTRACT"() {
        given:
            def jsonCol = col(Object, "j")
            def inner = new JsonExpression(jsonCol, JsonExpression.Operator.EXTRACT, ["a"] as String[], null)
            def outer = new JsonExpression(inner, JsonExpression.Operator.EXTRACT, ["b"] as String[], null)
        expect:
            assertSql(dialect.toSqlPart(outer, null), "JSON_EXTRACT(`j`, '\$.a.b')")
    }

    def "JsonTextExpression EXTRACT_TEXT wraps JSON_EXTRACT with JSON_UNQUOTE"() {
        given:
            def jsonCol = col(Object, "j")
            def expr = new JsonTextExpression(jsonCol, JsonExpression.Operator.EXTRACT_TEXT, ["k"] as String[])
        expect:
            assertSql(dialect.toSqlPart(expr, null), "JSON_UNQUOTE(JSON_EXTRACT(`j`, '\$.k'))")
    }

    def "JsonTextExpression chained after EXTRACT compresses into single path"() {
        given:
            def jsonCol = col(Object, "j")
            def inner = new JsonExpression(jsonCol, JsonExpression.Operator.EXTRACT, ["a"] as String[], null)
            def outer = new JsonTextExpression(inner, JsonExpression.Operator.EXTRACT_TEXT, ["b"] as String[])
        expect:
            assertSql(dialect.toSqlPart(outer, null), "JSON_UNQUOTE(JSON_EXTRACT(`j`, '\$.a.b'))")
    }

    def "JsonCondition HAS_KEY renders JSON_CONTAINS_PATH with one mode"() {
        given:
            def jsonCol = col(Object, "j")
            def cond = new JsonCondition(jsonCol, JsonExpression.Operator.HAS_KEY, ["k"] as String[], null)
        expect:
            assertSql(dialect.toSqlPart(cond, null), "JSON_CONTAINS_PATH(`j`, 'one', '\$.k')")
    }

    def "JsonCondition HAS_ANY_KEY lists all paths under one mode"() {
        given:
            def jsonCol = col(Object, "j")
            def cond = new JsonCondition(jsonCol, JsonExpression.Operator.HAS_ANY_KEY, ["a", "b"] as String[], null)
        expect:
            assertSql(dialect.toSqlPart(cond, null), "JSON_CONTAINS_PATH(`j`, 'one', '\$.a', '\$.b')")
    }

    def "JsonCondition HAS_ALL_KEYS lists all paths under all mode"() {
        given:
            def jsonCol = col(Object, "j")
            def cond = new JsonCondition(jsonCol, JsonExpression.Operator.HAS_ALL_KEYS, ["a", "b"] as String[], null)
        expect:
            assertSql(dialect.toSqlPart(cond, null), "JSON_CONTAINS_PATH(`j`, 'all', '\$.a', '\$.b')")
    }
}
