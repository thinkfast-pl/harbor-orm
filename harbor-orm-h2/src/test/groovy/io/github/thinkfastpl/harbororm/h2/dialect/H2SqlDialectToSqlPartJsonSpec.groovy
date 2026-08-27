// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect


import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.JsonCondition
import io.github.thinkfastpl.harbororm.api.expression.JsonExpression
import io.github.thinkfastpl.harbororm.api.expression.JsonTextExpression
import spock.lang.Specification

import java.sql.Types

import static io.github.thinkfastpl.harbororm.h2.dialect.H2SqlDialectTestSupport.*

class H2SqlDialectToSqlPartJsonSpec extends Specification {

    def dialect = newDialect()

    // ---------- JSON LITERAL (supported via delegation) ----------

    def "JsonExpression LITERAL delegates to source rendering"() {
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
                    'CAST(? AS VARCHAR)',
                    [['{"k":1}', Types.VARCHAR]]
            )
    }

    // ---------- JsonExpression unsupported operators ----------

    def "JsonExpression EXTRACT throws UnsupportedOperationException"() {
        given:
            def expr = new JsonExpression(
                    col(String, "j"),
                    JsonExpression.Operator.EXTRACT,
                    ["k"] as String[],
                    null
            )
        when:
            dialect.toSqlPart(expr, null)
        then:
            def e = thrown(UnsupportedOperationException)
            e.message.startsWith("JSON operator EXTRACT is not supported on H2.")
    }

    def "JsonTextExpression EXTRACT_TEXT throws UnsupportedOperationException"() {
        given:
            def expr = new JsonTextExpression(
                    col(String, "j"),
                    JsonExpression.Operator.EXTRACT_TEXT,
                    ["k"] as String[]
            )
        when:
            dialect.toSqlPart(expr, null)
        then:
            def e = thrown(UnsupportedOperationException)
            e.message == "JSON text extraction is not supported on H2."
    }

    def "JsonCondition HAS_KEY throws UnsupportedOperationException"() {
        given:
            def cond = new JsonCondition(
                    col(String, "j"),
                    JsonExpression.Operator.HAS_KEY,
                    ["k"] as String[],
                    null
            )
        when:
            dialect.toSqlPart(cond, null)
        then:
            def e = thrown(UnsupportedOperationException)
            e.message == "JSON condition HAS_KEY is not supported on H2."
    }
}
