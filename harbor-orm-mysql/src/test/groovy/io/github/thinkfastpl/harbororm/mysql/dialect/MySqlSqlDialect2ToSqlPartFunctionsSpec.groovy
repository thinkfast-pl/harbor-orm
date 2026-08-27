// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mysql.dialect

import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.expression.PortableFunctionExpression
import spock.lang.Specification

import static io.github.thinkfastpl.harbororm.mysql.dialect.MySqlDialectTestSupport.assertSql
import static io.github.thinkfastpl.harbororm.mysql.dialect.MySqlDialectTestSupport.col

class MySqlSqlDialect2ToSqlPartFunctionsSpec extends Specification {

    def dialect = new MySqlSqlDialect()

    // ---------- json_array_length ----------

    def "json_array_length renders json_length"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.JSON_ARRAY_LENGTH,
                    [col(Object, "j")] as List<Expression<?>>,
                    Integer.class
            )
        expect:
            assertSql(dialect.toSqlPart(fn, null), 'json_length(`j`)')
    }

    def "json_array_length with 2 params throws IllegalArgumentException"() {
        given:
            def fn = new PortableFunctionExpression<>(
                    PortableFunctionExpression.Function.JSON_ARRAY_LENGTH,
                    [col(Object, "j"), col(Object, "k")] as List<Expression<?>>,
                    Integer.class
            )
        when:
            dialect.toSqlPart(fn, null)
        then:
            thrown(IllegalArgumentException)
    }
}
