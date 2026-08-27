// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.JsonCondition
import io.github.thinkfastpl.harbororm.api.expression.JsonExpression
import io.github.thinkfastpl.harbororm.api.expression.JsonTextExpression
import spock.lang.Specification

import java.sql.Types

import static io.github.thinkfastpl.harbororm.postgres.dialect.PostgreSqlDialectTestSupport.*

class PostgreSqlDialectToSqlPartJsonSpec extends Specification {

    def dialect = newDialect()

    // ---------- JsonExpression ----------

    def "JsonExpression EXTRACT renders -> 'key'"() {
        given:
            def expr = new JsonExpression(
                    col(String, "j"),
                    JsonExpression.Operator.EXTRACT,
                    ["k"] as String[],
                    null
            )
        expect:
            assertSql(dialect.toSqlPart(expr, null), '"j" -> \'k\'')
    }

    def "JsonExpression EXTRACT escapes embedded apostrophe in key"() {
        given:
            def expr = new JsonExpression(
                    col(String, "j"),
                    JsonExpression.Operator.EXTRACT,
                    ["a'b"] as String[],
                    null
            )
        expect:
            assertSql(dialect.toSqlPart(expr, null), '"j" -> \'a\'\'b\'')
    }

    def "JsonExpression EXTRACT_PATH renders #> with path braces"() {
        given:
            def expr = new JsonExpression(
                    col(String, "j"),
                    JsonExpression.Operator.EXTRACT_PATH,
                    ["a", "b", "c"] as String[],
                    null
            )
        expect:
            assertSql(dialect.toSqlPart(expr, null), '"j" #> \'{a,b,c}\'')
    }

    def "JsonExpression LITERAL delegates with JSON context to render ?::jsonb"() {
        given:
            def expr = new JsonExpression(
                    DSL.constant('{"k":1}'),
                    JsonExpression.Operator.LITERAL,
                    null,
                    null
            )
        expect:
            assertSql(
                    dialect.toSqlPart(expr, null),
                    '?::jsonb',
                    [['{"k":1}', Types.VARCHAR]]
            )
    }

    def "JsonExpression with unsupported operator throws UnsupportedOperationException"() {
        given:
            def expr = new JsonExpression(
                    col(String, "j"),
                    JsonExpression.Operator.EXTRACT_TEXT,
                    ["k"] as String[],
                    null
            )
        when:
            dialect.toSqlPart(expr, null)
        then:
            def e = thrown(UnsupportedOperationException)
            e.message.contains("Unexpected JSON operator")
    }

    // ---------- JsonTextExpression ----------

    def "JsonTextExpression EXTRACT_TEXT renders ->> 'key'"() {
        given:
            def expr = new JsonTextExpression(
                    col(String, "j"),
                    JsonExpression.Operator.EXTRACT_TEXT,
                    ["k"] as String[]
            )
        expect:
            assertSql(dialect.toSqlPart(expr, null), '"j" ->> \'k\'')
    }

    def "JsonTextExpression EXTRACT_PATH_TEXT renders #>> with path braces"() {
        given:
            def expr = new JsonTextExpression(
                    col(String, "j"),
                    JsonExpression.Operator.EXTRACT_PATH_TEXT,
                    ["a", "b"] as String[]
            )
        expect:
            assertSql(dialect.toSqlPart(expr, null), '"j" #>> \'{a,b}\'')
    }

    def "JsonTextExpression with unsupported operator throws UnsupportedOperationException"() {
        given:
            def expr = new JsonTextExpression(
                    col(String, "j"),
                    JsonExpression.Operator.EXTRACT,
                    ["k"] as String[]
            )
        when:
            dialect.toSqlPart(expr, null)
        then:
            def e = thrown(UnsupportedOperationException)
            e.message.contains("Unexpected JSON text operator")
    }

    // ---------- JsonCondition ----------

    def "JsonCondition CONTAINS renders @>"() {
        given:
            def cond = new JsonCondition(
                    col(String, "j"),
                    JsonExpression.Operator.CONTAINS,
                    null,
                    col(String, "o")
            )
        expect:
            assertSql(dialect.toSqlPart(cond, null), '"j" @> "o"')
    }

    def "JsonCondition CONTAINED_IN renders <@"() {
        given:
            def cond = new JsonCondition(
                    col(String, "j"),
                    JsonExpression.Operator.CONTAINED_IN,
                    null,
                    col(String, "o")
            )
        expect:
            assertSql(dialect.toSqlPart(cond, null), '"j" <@ "o"')
    }

    def "JsonCondition HAS_KEY renders ?? (JDBC ? escape)"() {
        given:
            def cond = new JsonCondition(
                    col(String, "j"),
                    JsonExpression.Operator.HAS_KEY,
                    ["k"] as String[],
                    null
            )
        expect:
            assertSql(dialect.toSqlPart(cond, null), '"j" ?? \'k\'')
    }

    def "JsonCondition HAS_KEY escapes embedded apostrophe in key"() {
        given:
            def cond = new JsonCondition(
                    col(String, "j"),
                    JsonExpression.Operator.HAS_KEY,
                    ["a'b"] as String[],
                    null
            )
        expect:
            assertSql(dialect.toSqlPart(cond, null), '"j" ?? \'a\'\'b\'')
    }

    def "JsonCondition HAS_ANY_KEY renders ??| array[...]"() {
        given:
            def cond = new JsonCondition(
                    col(String, "j"),
                    JsonExpression.Operator.HAS_ANY_KEY,
                    ["k1", "k2"] as String[],
                    null
            )
        expect:
            assertSql(dialect.toSqlPart(cond, null), '"j" ??| array[\'k1\',\'k2\']')
    }

    def "JsonCondition HAS_ALL_KEYS renders ??& array[...]"() {
        given:
            def cond = new JsonCondition(
                    col(String, "j"),
                    JsonExpression.Operator.HAS_ALL_KEYS,
                    ["k1", "k2"] as String[],
                    null
            )
        expect:
            assertSql(dialect.toSqlPart(cond, null), '"j" ??& array[\'k1\',\'k2\']')
    }

    def "JsonCondition with unsupported operator throws UnsupportedOperationException"() {
        given:
            def cond = new JsonCondition(
                    col(String, "j"),
                    JsonExpression.Operator.EXTRACT,
                    null,
                    null
            )
        when:
            dialect.toSqlPart(cond, null)
        then:
            def e = thrown(UnsupportedOperationException)
            e.message.contains("Unexpected JSON condition")
    }
}
