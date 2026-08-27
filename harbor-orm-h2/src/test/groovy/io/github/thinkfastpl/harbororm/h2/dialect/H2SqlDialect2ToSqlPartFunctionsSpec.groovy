// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.expression.PortableFunctionExpression
import spock.lang.Specification

import static io.github.thinkfastpl.harbororm.h2.dialect.H2SqlDialectTestSupport.assertSql
import static io.github.thinkfastpl.harbororm.h2.dialect.H2SqlDialectTestSupport.col

class H2SqlDialect2ToSqlPartFunctionsSpec extends Specification {

    def dialect = new H2SqlDialect()

    // ---------- json_array_length ----------

    def "json_array_length renders CARDINALITY with FORMAT JSON"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.JSON_ARRAY_LENGTH,
                    [col(String, "x")] as List<Expression<?>>,
                    Integer.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'CARDINALITY("x" FORMAT JSON)')
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
