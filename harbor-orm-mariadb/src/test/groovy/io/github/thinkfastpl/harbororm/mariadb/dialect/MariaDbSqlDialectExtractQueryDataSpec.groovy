// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.dialect

import io.github.thinkfastpl.harbororm.api.expression.DefaultSelectExpression
import io.github.thinkfastpl.harbororm.api.expression.SelectExpression
import spock.lang.Specification

import java.util.function.Function

class MariaDbSqlDialectExtractQueryDataSpec extends Specification {

    def dialect = new MariaDbSqlDialect()

    def "extractQueryData returns query data of DefaultSelectExpression"() {
        given:
            def expr = new DefaultSelectExpression<>(false, { r -> null } as Function, Object.class)
        when:
            def data = dialect.extractQueryData(expr)
        then:
            data != null
            data.selectExpressions.isEmpty()
            data.from == null
            !data.distinct
    }

    def "extractQueryData throws on unsupported SelectExpression"() {
        given:
            def unknown = Mock(SelectExpression)
        when:
            dialect.extractQueryData(unknown)
        then:
            def e = thrown(IllegalArgumentException)
            e.message.startsWith("Unsupported select expression type")
    }
}
