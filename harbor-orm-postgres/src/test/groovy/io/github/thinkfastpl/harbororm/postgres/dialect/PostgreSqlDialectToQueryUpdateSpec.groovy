// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import io.github.thinkfastpl.harbororm.api.expression.BinaryOperatorCondition
import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.data.UpdateQueryData
import spock.lang.Specification

import java.sql.Types

import static io.github.thinkfastpl.harbororm.postgres.dialect.PostgreSqlDialectTestSupport.*

class PostgreSqlDialectToQueryUpdateSpec extends Specification {

    def dialect = newDialect()

    def "single SET without WHERE (constant renders as ?)"() {
        given:
            def data = new UpdateQueryData(
                    table("t"),
                    [col(Integer, "a")],
                    [DSL.constant(5)],
                    [],
                    []
            )
        expect:
            assertSql(
                    dialect.toQuery(data),
                    'UPDATE "t" SET "a" = ?',
                    [[5, Types.INTEGER]]
            )
    }

    def "multiple SET with WHERE"() {
        given:
            def cond = new BinaryOperatorCondition(col(Long, "id"), DSL.constant(7L), BinaryOperatorCondition.Operator.EQ)
            def data = new UpdateQueryData(
                    table("t"),
                    [col(Integer, "a"), col(String, "b")],
                    [DSL.constant(1), DSL.constant("x")],
                    [cond],
                    []
            )
        expect:
            assertSql(
                    dialect.toQuery(data),
                    'UPDATE "t" SET "a" = ?, "b" = ? WHERE "id" = ?',
                    [[1, Types.INTEGER], ["x", Types.VARCHAR], [7L, Types.BIGINT]]
            )
    }

    def "inline RETURNING (PG uses inline, not FROM FINAL TABLE)"() {
        given:
            def data = new UpdateQueryData(
                    table("t"),
                    [col(Integer, "a")],
                    [DSL.constant(1)],
                    [],
                    [col(Integer, "id"), col(String, "name")]
            )
        expect:
            assertSql(
                    dialect.toQuery(data),
                    'UPDATE "t" SET "a" = ? RETURNING "id", "name"',
                    [[1, Types.INTEGER]]
            )
    }

    def "empty columns list throws IllegalArgumentException"() {
        given:
            def data = new UpdateQueryData(
                    table("t"),
                    [],
                    [],
                    [],
                    []
            )
        when:
            dialect.toQuery(data)
        then:
            def e = thrown(IllegalArgumentException)
            e.message == "No columns to update"
    }

    def "mismatched columns and values size throws IllegalArgumentException"() {
        given:
            def data = new UpdateQueryData(
                    table("t"),
                    [col(Integer, "a"), col(Integer, "b")],
                    [DSL.constant(1)],
                    [],
                    []
            )
        when:
            dialect.toQuery(data)
        then:
            def e = thrown(IllegalArgumentException)
            e.message == "Columns and values do not match"
    }
}
