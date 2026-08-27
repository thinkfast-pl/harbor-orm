// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.*
import io.github.thinkfastpl.harbororm.api.lob.PortableBlob
import io.github.thinkfastpl.harbororm.api.lob.PortableClob
import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext
import spock.lang.Specification

import java.sql.Types

import static io.github.thinkfastpl.harbororm.h2.dialect.H2SqlDialectTestSupport.assertSql
import static io.github.thinkfastpl.harbororm.h2.dialect.H2SqlDialectTestSupport.newDialect

class H2SqlDialectToSqlPartConstantsSpec extends Specification {

    def dialect = newDialect()

    def "ConstantExpression of #javaType.simpleName renders CAST(? AS #h2Type) with sqlType=#sqlType"() {
        given:
            def expr = new ConstantExpression(javaType, value)
        expect:
            assertSql(dialect.toSqlPart(expr, null), "CAST(? AS ${h2Type})", [[value, sqlType]])
        where:
            javaType      | value | h2Type             | sqlType
            Integer.class | 5     | "INT"              | Types.INTEGER
            String.class  | "hi"  | "VARCHAR"          | Types.VARCHAR
            Boolean.class | true  | "BOOLEAN"          | Types.BOOLEAN
            Long.class    | null  | "BIGINT"           | Types.BIGINT
            Double.class  | 1.5d  | "DOUBLE PRECISION" | Types.DOUBLE
    }

    def "ConstantExpression of PortableBlob renders as ? with Types.OTHER (no CAST)"() {
        given:
            def mockBlob = Mock(PortableBlob)
            def expr = new ConstantExpression<>(PortableBlob.class, mockBlob)
        expect:
            assertSql(dialect.toSqlPart(expr, null), "?", [[mockBlob, Types.OTHER]])
    }

    def "ConstantExpression of PortableClob renders as ? with Types.OTHER (no CAST)"() {
        given:
            def mockClob = Mock(PortableClob)
            def expr = new ConstantExpression<>(PortableClob.class, mockClob)
        expect:
            assertSql(dialect.toSqlPart(expr, null), "?", [[mockClob, Types.OTHER]])
    }

    def "ConstantExpression with JSON column context renders ? FORMAT JSON"() {
        given:
            def expr = new ConstantExpression(String.class, '{"k":1}')
            def context = new ColumnContext(null, null, true)
        expect:
            assertSql(dialect.toSqlPart(expr, context), '? FORMAT JSON', [['{"k":1}', Types.VARCHAR]])
    }

    def "ConstantsExpression of Integer renders ?,?,? with each param Types.INTEGER"() {
        given:
            def expr = new ConstantsExpression<>(Integer.class, [1, 2, 3] as Collection)
        expect:
            assertSql(
                    dialect.toSqlPart(expr, null),
                    "?,?,?",
                    [[1, Types.INTEGER], [2, Types.INTEGER], [3, Types.INTEGER]]
            )
    }

    def "BooleanConstantExpression(true) with null context renders literal true"() {
        expect:
            assertSql(dialect.toSqlPart(new BooleanConstantExpression(true), null), "true")
    }

    def "BooleanConstantExpression(false) with null context renders literal false"() {
        expect:
            assertSql(dialect.toSqlPart(new BooleanConstantExpression(false), null), "false")
    }

    def "BooleanConstantExpression(null) with null context renders literal null"() {
        expect:
            assertSql(dialect.toSqlPart(new BooleanConstantExpression(null), null), "null")
    }

    def "BooleanConstantExpression(true) with non-null context routes through ConstantExpression<Boolean>"() {
        given:
            def context = new ColumnContext(null, null, false)
        expect:
            assertSql(
                    dialect.toSqlPart(new BooleanConstantExpression(true), context),
                    "CAST(? AS BOOLEAN)",
                    [[true, Types.BOOLEAN]]
            )
    }

    def "LiteralExpression renders the literal verbatim with no params"() {
        expect:
            assertSql(
                    dialect.toSqlPart(new LiteralExpression<>(Long.class, "COUNT(*)"), null),
                    "COUNT(*)"
            )
    }

    def "NameExpression renders the escaped name with no params"() {
        expect:
            assertSql(dialect.toSqlPart(new NameExpression<>(Object.class, "x"), null), '"x"')
    }

    def "NameExpression escapes embedded double quotes by doubling"() {
        expect:
            assertSql(dialect.toSqlPart(new NameExpression<>(Object.class, 'a"b'), null), '"a""b"')
    }
}
