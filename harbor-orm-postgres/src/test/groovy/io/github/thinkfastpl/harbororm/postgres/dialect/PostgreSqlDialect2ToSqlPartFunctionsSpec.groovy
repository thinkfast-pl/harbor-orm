// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.expression.PortableFunctionExpression
import spock.lang.Specification

import static io.github.thinkfastpl.harbororm.postgres.dialect.PostgreSqlDialectTestSupport.assertSql
import static io.github.thinkfastpl.harbororm.postgres.dialect.PostgreSqlDialectTestSupport.col

class PostgreSqlDialect2ToSqlPartFunctionsSpec extends Specification {

    def dialect = new PostgreSqlDialect()

    // ---------- json_array_length ----------

    def "json_array_length renders json_array_length with CAST to json"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.JSON_ARRAY_LENGTH,
                    [col(String, "x")] as List<Expression<?>>,
                    Integer.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'json_array_length(CAST("x" AS json))')
    }

    def "json_array_length with 2 params throws IllegalArgumentException"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.JSON_ARRAY_LENGTH,
                    [col(String, "x"), col(String, "y")] as List<Expression<?>>,
                    Integer.class
            )
        when:
            dialect.toSqlPart(fn, null)
        then:
            thrown(IllegalArgumentException)
    }
}
