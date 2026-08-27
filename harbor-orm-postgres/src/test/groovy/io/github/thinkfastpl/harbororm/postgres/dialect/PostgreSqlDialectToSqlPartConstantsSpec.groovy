// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import io.github.thinkfastpl.harbororm.api.expression.*
import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext
import spock.lang.Specification

import java.sql.Types

import static io.github.thinkfastpl.harbororm.postgres.dialect.PostgreSqlDialectTestSupport.assertSql
import static io.github.thinkfastpl.harbororm.postgres.dialect.PostgreSqlDialectTestSupport.newDialect

class PostgreSqlDialectToSqlPartConstantsSpec extends Specification {

    def dialect = newDialect()

    def "ConstantExpression of #javaType.simpleName renders ? with sqlType=#sqlType (no CAST on PG)"() {
        given:
            def expr = new ConstantExpression(javaType, value)
        expect:
            assertSql(dialect.toSqlPart(expr, null), "?", [[value, sqlType]])
        where:
            javaType      | value | sqlType
            Integer.class | 5     | Types.INTEGER
            String.class  | "hi"  | Types.VARCHAR
            Boolean.class | true  | Types.BOOLEAN
            Long.class    | null  | Types.BIGINT
            Double.class  | 1.5d  | Types.DOUBLE
    }

    def "ConstantExpression with JSON column context renders ?::jsonb"() {
        given:
            def expr = new ConstantExpression(String.class, '{"k":1}')
            def context = new ColumnContext(null, null, true)
        expect:
            assertSql(
                    dialect.toSqlPart(expr, context),
                    '?::jsonb',
                    [['{"k":1}', Types.VARCHAR]]
            )
    }

    def "ConstantExpression with JSON column context plus customType renders ?::customType"() {
        given:
            def expr = new ConstantExpression(String.class, '{}')
            def context = new ColumnContext("custom_json", null, true)
        expect:
            assertSql(dialect.toSqlPart(expr, context), '?::custom_json', [['{}', Types.VARCHAR]])
    }

    def "ConstantExpression with non-JSON customType renders ?::customType"() {
        given:
            def expr = new ConstantExpression(String.class, "open")
            def context = new ColumnContext("account_status", null)
        expect:
            assertSql(
                    dialect.toSqlPart(expr, context),
                    '?::account_status',
                    [["open", Types.VARCHAR]]
            )
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

    def "BooleanConstantExpression(true) with non-null context routes through ConstantExpression"() {
        given:
            def context = new ColumnContext(null, null, false)
        expect:
            assertSql(
                    dialect.toSqlPart(new BooleanConstantExpression(true), context),
                    '?',
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
