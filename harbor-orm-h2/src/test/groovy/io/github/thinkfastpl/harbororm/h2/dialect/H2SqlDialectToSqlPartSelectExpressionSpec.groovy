// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DefaultSelectExpression
import io.github.thinkfastpl.harbororm.api.expression.EmbeddableEqCondition
import io.github.thinkfastpl.harbororm.api.expression.EmbeddableInCondition
import io.github.thinkfastpl.harbororm.api.expression.FluentSelectExpression
import io.github.thinkfastpl.harbororm.api.metadata.QAttribute
import io.github.thinkfastpl.harbororm.api.metadata.QEmbeddable
import spock.lang.Specification

import java.util.function.Function

import static io.github.thinkfastpl.harbororm.h2.dialect.H2SqlDialectTestSupport.*

class H2SqlDialectToSqlPartSelectExpressionSpec extends Specification {

    def dialect = newDialect()

    // ---------- DefaultSelectExpression / FluentSelectExpression ----------

    def "DefaultSelectExpression renders SELECT col FROM table"() {
        given:
            def expr = new DefaultSelectExpression<>(false, { r -> null } as Function, Object.class)
            expr.select(col(Integer, "id"))
            expr.from(table("t"))
        expect:
            assertSql(dialect.toSqlPart(expr, null), '(SELECT "id" FROM "t")')
    }

    def "FluentSelectExpression delegates to wrapped DefaultSelectExpression"() {
        given:
            def inner = new DefaultSelectExpression<>(false, { r -> null } as Function, Object.class)
            inner.select(col(Integer, "id"))
            inner.from(table("t"))
            def expr = new FluentSelectExpression<>(inner)
        expect:
            assertSql(dialect.toSqlPart(expr, null), '(SELECT "id" FROM "t")')
    }

    // ---------- EmbeddableEqCondition ----------

    def "EmbeddableEqCondition with null value renders all-IS-NULL conjunction"() {
        given:
            def qEmb = Mock(QEmbeddable)
            qEmb.getAllAttributes() >> ([col(Integer, "a"), col(String, "b")] as List<QAttribute>)
            def cond = new EmbeddableEqCondition(qEmb, null)
        expect:
            assertSql(dialect.toSqlPart(cond, null), '("a" IS NULL AND "b" IS NULL)')
    }

    // ---------- EmbeddableInCondition ----------

    def "EmbeddableInCondition with empty values renders FALSE"() {
        given:
            def qEmb = Mock(QEmbeddable)
            def cond = new EmbeddableInCondition(qEmb, [])
        expect:
            assertSql(dialect.toSqlPart(cond, null), 'FALSE')
    }

    def "EmbeddableInCondition with null-valued entries renders OR chain"() {
        given:
            def qEmb = Mock(QEmbeddable)
            qEmb.getAllAttributes() >> ([col(Integer, "a")] as List<QAttribute>)
            def cond = new EmbeddableInCondition(qEmb, [null, null])
        expect:
            assertSql(dialect.toSqlPart(cond, null), '(("a" IS NULL) OR ("a" IS NULL))')
    }

    // NOTE: EmbeddableEqCondition / EmbeddableInCondition with non-null values requires
    // a bean with a getter matching the QColumn's propertyName plus a non-null propertyName
    // on the simple QColumn (col() helper uses propertyName=null). Those branches are not
    // exercised here.
}
