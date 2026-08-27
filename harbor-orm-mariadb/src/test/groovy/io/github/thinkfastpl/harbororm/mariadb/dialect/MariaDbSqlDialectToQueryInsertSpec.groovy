// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.dialect

import io.github.thinkfastpl.harbororm.api.expression.BinaryOperatorCondition
import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.metadata.QColumn
import io.github.thinkfastpl.harbororm.api.query.data.InsertQueryData
import io.github.thinkfastpl.harbororm.api.query.data.OnConflictData
import spock.lang.Specification

import java.sql.Types

import static io.github.thinkfastpl.harbororm.mariadb.dialect.MariaDbDialectTestSupport.*

class MariaDbSqlDialectToQueryInsertSpec extends Specification {

    def dialect = newDialect()

    def "single-row INSERT with three typed columns (column names backtick-escaped in MariaDB)"() {
        given:
            def idCol = col(Long, "id")
            def nameCol = col(String, "name")
            def flagCol = col(Boolean, "flag")
            Map<QColumn<?>, Expression<?>> row = new LinkedHashMap<>()
            row[idCol] = DSL.constant(1L)
            row[nameCol] = DSL.constant("n")
            row[flagCol] = DSL.constant(Boolean.class, Boolean.TRUE)
            def data = new InsertQueryData(
                    table("t"),
                    [idCol, nameCol, flagCol],
                    [row],
                    [],
                    null
            )
        expect:
            assertSql(
                    dialect.toQuery(data),
                    'INSERT INTO `t`(`id`, `name`, `flag`) VALUES (?, ?, ?)',
                    [[1L, Types.BIGINT], ["n", Types.VARCHAR], [true, Types.BOOLEAN]]
            )
    }

    def "multi-row INSERT renders comma-separated VALUES groups"() {
        given:
            def aCol = col(Integer, "a")
            Map<QColumn<?>, Expression<?>> row1 = new LinkedHashMap<>()
            row1[aCol] = DSL.constant(1)
            Map<QColumn<?>, Expression<?>> row2 = new LinkedHashMap<>()
            row2[aCol] = DSL.constant(2)
            def data = new InsertQueryData(
                    table("t"),
                    [aCol],
                    [row1, row2],
                    [],
                    null
            )
        expect:
            assertSql(
                    dialect.toQuery(data),
                    'INSERT INTO `t`(`a`) VALUES (?), (?)',
                    [[1, Types.INTEGER], [2, Types.INTEGER]]
            )
    }

    def "INSERT with empty rows omits column list and VALUES clause"() {
        given:
            def data = new InsertQueryData(
                    table("t"),
                    [],
                    [],
                    [],
                    null
            )
        expect:
            assertSql(dialect.toQuery(data), 'INSERT INTO `t`')
    }

    def "INSERT with inline RETURNING"() {
        given:
            def idCol = col(Integer, "id")
            Map<QColumn<?>, Expression<?>> row = new LinkedHashMap<>()
            row[idCol] = DSL.constant(42)
            def data = new InsertQueryData(
                    table("t"),
                    [idCol],
                    [row],
                    [col(Integer, "id"), col(String, "name")],
                    null
            )
        expect:
            assertSql(
                    dialect.toQuery(data),
                    'INSERT INTO `t`(`id`) VALUES (?) RETURNING `id`, `name`',
                    [[42, Types.INTEGER]]
            )
    }

    def "INSERT strips table alias via withoutAlias"() {
        given:
            def aCol = col(Integer, "a")
            Map<QColumn<?>, Expression<?>> row = new LinkedHashMap<>()
            row[aCol] = DSL.constant(1)
            def data = new InsertQueryData(
                    table("t", "alias_x"),
                    [aCol],
                    [row],
                    [],
                    null
            )
        when:
            def sql = dialect.toQuery(data).sql.toString()
        then:
            sql.startsWith('INSERT INTO `t`')
            !sql.contains("alias_x")
    }

    // ---------- ON CONFLICT (ON DUPLICATE KEY UPDATE on MariaDB) ----------

    def "ON CONFLICT DO NOTHING becomes ON DUPLICATE KEY UPDATE col = col"() {
        given:
            def idCol = col(Integer, "id")
            Map<QColumn<?>, Expression<?>> row = new LinkedHashMap<>()
            row[idCol] = DSL.constant(1)
            def onConflict = new OnConflictData(
                    [idCol],
                    OnConflictData.ConflictAction.DO_NOTHING,
                    [],
                    [],
                    null
            )
            def data = new InsertQueryData(
                    table("t"),
                    [idCol],
                    [row],
                    [],
                    onConflict
            )
        expect:
            assertSql(
                    dialect.toQuery(data),
                    'INSERT INTO `t`(`id`) VALUES (?) ON DUPLICATE KEY UPDATE `id` = `id`',
                    [[1, Types.INTEGER]]
            )
    }

    def "ON CONFLICT DO UPDATE SET single column"() {
        given:
            def idCol = col(Integer, "id")
            def nameCol = col(String, "name")
            Map<QColumn<?>, Expression<?>> row = new LinkedHashMap<>()
            row[idCol] = DSL.constant(1)
            row[nameCol] = DSL.constant("n")
            def onConflict = new OnConflictData(
                    [idCol],
                    OnConflictData.ConflictAction.DO_UPDATE,
                    [nameCol],
                    [DSL.constant("updated")] as List<Expression<?>>,
                    null
            )
            def data = new InsertQueryData(
                    table("t"),
                    [idCol, nameCol],
                    [row],
                    [],
                    onConflict
            )
        expect:
            assertSql(
                    dialect.toQuery(data),
                    'INSERT INTO `t`(`id`, `name`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `name` = ?',
                    [[1, Types.INTEGER], ["n", Types.VARCHAR], ["updated", Types.VARCHAR]]
            )
    }

    def "ON CONFLICT DO UPDATE SET multiple columns"() {
        given:
            def idCol = col(Integer, "id")
            def nameCol = col(String, "name")
            def ageCol = col(Integer, "age")
            Map<QColumn<?>, Expression<?>> row = new LinkedHashMap<>()
            row[idCol] = DSL.constant(1)
            def onConflict = new OnConflictData(
                    [idCol],
                    OnConflictData.ConflictAction.DO_UPDATE,
                    [nameCol, ageCol],
                    [DSL.constant("u"), DSL.constant(30)] as List<Expression<?>>,
                    null
            )
            def data = new InsertQueryData(
                    table("t"),
                    [idCol],
                    [row],
                    [],
                    onConflict
            )
        expect:
            assertSql(
                    dialect.toQuery(data),
                    'INSERT INTO `t`(`id`) VALUES (?) ON DUPLICATE KEY UPDATE `name` = ?, `age` = ?',
                    [[1, Types.INTEGER], ["u", Types.VARCHAR], [30, Types.INTEGER]]
            )
    }

    def "ON CONFLICT DO UPDATE SET ... WHERE throws UnsupportedOperationException on MariaDB"() {
        given:
            def idCol = col(Integer, "id")
            def nameCol = col(String, "name")
            def activeCol = col(Boolean, "active")
            Map<QColumn<?>, Expression<?>> row = new LinkedHashMap<>()
            row[idCol] = DSL.constant(1)
            def whereCond = new BinaryOperatorCondition(
                    activeCol,
                    DSL.constant(Boolean.class, Boolean.TRUE),
                    BinaryOperatorCondition.Operator.EQ
            )
            def onConflict = new OnConflictData(
                    [idCol],
                    OnConflictData.ConflictAction.DO_UPDATE,
                    [nameCol],
                    [DSL.constant("u")] as List<Expression<?>>,
                    whereCond
            )
            def data = new InsertQueryData(
                    table("t"),
                    [idCol],
                    [row],
                    [],
                    onConflict
            )
        when:
            dialect.toQuery(data)
        then:
            thrown(UnsupportedOperationException)
    }

    def "ON CONFLICT with multiple conflict columns DO_NOTHING self-assigns first conflict column"() {
        given:
            def idCol = col(Integer, "id")
            def tenantCol = col(Integer, "tenant_id")
            Map<QColumn<?>, Expression<?>> row = new LinkedHashMap<>()
            row[idCol] = DSL.constant(1)
            row[tenantCol] = DSL.constant(7)
            def onConflict = new OnConflictData(
                    [idCol, tenantCol],
                    OnConflictData.ConflictAction.DO_NOTHING,
                    [],
                    [],
                    null
            )
            def data = new InsertQueryData(
                    table("t"),
                    [idCol, tenantCol],
                    [row],
                    [],
                    onConflict
            )
        expect:
            assertSql(
                    dialect.toQuery(data),
                    'INSERT INTO `t`(`id`, `tenant_id`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `id` = `id`',
                    [[1, Types.INTEGER], [7, Types.INTEGER]]
            )
    }
}
