// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.dialect

import io.github.thinkfastpl.harbororm.api.expression.CommonTableExpression
import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.expression.FunctionCallTableSource
import io.github.thinkfastpl.harbororm.api.metadata.QColumn
import io.github.thinkfastpl.harbororm.api.metadata.QTableName
import io.github.thinkfastpl.harbororm.api.metadata.QTableSource
import spock.lang.Specification

import static io.github.thinkfastpl.harbororm.mariadb.dialect.MariaDbDialectTestSupport.assertSql
import static io.github.thinkfastpl.harbororm.mariadb.dialect.MariaDbDialectTestSupport.newDialect

class MariaDbSqlDialectToSqlPartTableSourceSpec extends Specification {

    def dialect = newDialect()

    def "QTableName plain"() {
        expect:
            assertSql(dialect.toSqlPart(new QTableName("users", null, null)), '`users`')
    }

    def "QTableName with schema"() {
        expect:
            assertSql(dialect.toSqlPart(new QTableName("users", "public", null)), '`public`.`users`')
    }

    def "QTableName with alias"() {
        expect:
            assertSql(dialect.toSqlPart(new QTableName("users", null, "u")), '`users` u')
    }

    def "QTableName with schema and alias"() {
        expect:
            assertSql(dialect.toSqlPart(new QTableName("users", "public", "u")), '`public`.`users` u')
    }

    def "CommonTableExpression renders its name"() {
        given:
            def cte = new CommonTableExpression("cte_name")
        expect:
            assertSql(dialect.toSqlPart(cte), '`cte_name`')
    }

    def "FunctionCallTableSource as JOIN/subquery source throws on MariaDB"() {
        given:
            def fnName = new QTableName("my_fn", "myschema", "fn_alias")
            def src = new FunctionCallTableSource(
                    fnName,
                    [] as List<QColumn<?>>,
                    [DSL.constant(1L)] as List<Expression<?>>,
                    Object.class
            )
        when:
            dialect.toSqlPart(src)
        then:
            UnsupportedOperationException ex = thrown()
            ex.message.toLowerCase().contains("stored function calls")
    }

    def "FunctionCallTableSource without alias or schema also throws on MariaDB"() {
        given:
            def src = new FunctionCallTableSource(
                    new QTableName("fn", null, null),
                    [] as List<QColumn<?>>,
                    [] as List<Expression<?>>,
                    Object.class
            )
        when:
            dialect.toSqlPart(src)
        then:
            thrown(UnsupportedOperationException)
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
