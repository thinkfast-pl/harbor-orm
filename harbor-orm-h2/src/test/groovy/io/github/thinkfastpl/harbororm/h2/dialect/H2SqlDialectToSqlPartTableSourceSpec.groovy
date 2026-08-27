// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.CommonTableExpression
import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.expression.FunctionCallTableSource
import io.github.thinkfastpl.harbororm.api.metadata.QColumn
import io.github.thinkfastpl.harbororm.api.metadata.QTableName
import io.github.thinkfastpl.harbororm.api.metadata.QTableSource
import spock.lang.Specification

import java.sql.Types

import static io.github.thinkfastpl.harbororm.h2.dialect.H2SqlDialectTestSupport.assertSql
import static io.github.thinkfastpl.harbororm.h2.dialect.H2SqlDialectTestSupport.newDialect

class H2SqlDialectToSqlPartTableSourceSpec extends Specification {

    def dialect = newDialect()

    def "QTableName plain"() {
        expect:
            assertSql(dialect.toSqlPart(new QTableName("users", null, null)), '"users"')
    }

    def "QTableName with schema"() {
        expect:
            assertSql(dialect.toSqlPart(new QTableName("users", "public", null)), '"public"."users"')
    }

    def "QTableName with alias"() {
        expect:
            assertSql(dialect.toSqlPart(new QTableName("users", null, "u")), '"users" u')
    }

    def "QTableName with schema and alias"() {
        expect:
            assertSql(dialect.toSqlPart(new QTableName("users", "public", "u")), '"public"."users" u')
    }

    def "CommonTableExpression renders its name"() {
        given:
            def cte = new CommonTableExpression("cte_name")
        expect:
            assertSql(dialect.toSqlPart(cte), '"cte_name"')
    }

    def "FunctionCallTableSource renders schema.fn(params) AS alias"() {
        given:
            def fnName = new QTableName("my_fn", "myschema", "fn_alias")
            def src = new FunctionCallTableSource(
                    fnName,
                    [] as List<QColumn<?>>,
                    [DSL.constant(1L)] as List<Expression<?>>,
                    Object.class
            )
        when:
            def sql = dialect.toSqlPart(src)
        then:
            sql.sql.toString() == '"myschema"."my_fn"(CAST(? AS BIGINT)) AS "fn_alias"'
            sql.params.size() == 1
            sql.params[0].value == 1L
            sql.params[0].sqlType == Types.BIGINT
    }

    def "FunctionCallTableSource without alias or schema"() {
        given:
            def src = new FunctionCallTableSource(
                    new QTableName("fn", null, null),
                    [] as List<QColumn<?>>,
                    [] as List<Expression<?>>,
                    Object.class
            )
        expect:
            assertSql(dialect.toSqlPart(src), '"fn"()')
    }

    def "throws on unknown table source type"() {
        given:
            def unknown = Mock(QTableSource)
        when:
            dialect.toSqlPart(unknown)
        then:
            thrown(UnsupportedOperationException)
    }
}
