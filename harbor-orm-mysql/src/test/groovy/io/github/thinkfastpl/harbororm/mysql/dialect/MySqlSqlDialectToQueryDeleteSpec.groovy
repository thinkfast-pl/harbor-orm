// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mysql.dialect

import io.github.thinkfastpl.harbororm.api.expression.BinaryOperatorCondition
import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.data.DeleteQueryData
import spock.lang.Specification

import java.sql.Types

import static io.github.thinkfastpl.harbororm.mysql.dialect.MySqlDialectTestSupport.*

class MySqlSqlDialectToQueryDeleteSpec extends Specification {

    def dialect = newDialect()

    def "unconditional DELETE"() {
        given:
            def data = new DeleteQueryData(
                    table("t"),
                    [],
                    []
            )
        expect:
            assertSql(dialect.toQuery(data), 'DELETE FROM `t`')
    }

    def "DELETE with single WHERE"() {
        given:
            def cond = new BinaryOperatorCondition(col(Long, "id"), DSL.constant(5L), BinaryOperatorCondition.Operator.EQ)
            def data = new DeleteQueryData(
                    table("t"),
                    [cond],
                    []
            )
        expect:
            assertSql(
                    dialect.toQuery(data),
                    'DELETE FROM `t` WHERE `id` = ?',
                    [[5L, Types.BIGINT]]
            )
    }

    def "DELETE with two WHERE AND-chained"() {
        given:
            def c1 = new BinaryOperatorCondition(col(Long, "id"), DSL.constant(5L), BinaryOperatorCondition.Operator.EQ)
            def c2 = new BinaryOperatorCondition(col(Boolean, "active"), DSL.constant(Boolean.class, Boolean.TRUE), BinaryOperatorCondition.Operator.EQ)
            def data = new DeleteQueryData(
                    table("t"),
                    [c1, c2],
                    []
            )
        expect:
            assertSql(
                    dialect.toQuery(data),
                    'DELETE FROM `t` WHERE `id` = ? AND `active` = ?',
                    [[5L, Types.BIGINT], [true, Types.BOOLEAN]]
            )
    }

    def "DELETE with inline RETURNING"() {
        given:
            def data = new DeleteQueryData(
                    table("t"),
                    [],
                    [col(Integer, "id"), col(String, "name")]
            )
        when:
            dialect.toQuery(data)
        then:
            def e = thrown(UnsupportedOperationException)
            e.message == "RETURNING is not supported by MySQL dialect"
    }
}
