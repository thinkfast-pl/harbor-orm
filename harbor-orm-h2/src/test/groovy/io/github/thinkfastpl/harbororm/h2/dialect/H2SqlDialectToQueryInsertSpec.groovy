// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.metadata.QColumn
import io.github.thinkfastpl.harbororm.api.query.data.InsertQueryData
import io.github.thinkfastpl.harbororm.api.query.data.OnConflictData
import spock.lang.Specification

import java.sql.Types

import static io.github.thinkfastpl.harbororm.h2.dialect.H2SqlDialectTestSupport.*

class H2SqlDialectToQueryInsertSpec extends Specification {

    def dialect = newDialect()

    def "single-row INSERT with three typed columns"() {
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
                    'INSERT INTO "t"("id", "name", "flag") VALUES (CAST(? AS BIGINT), CAST(? AS VARCHAR), CAST(? AS BOOLEAN))',
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
                    'INSERT INTO "t"("a") VALUES (CAST(? AS INT)), (CAST(? AS INT))',
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
            assertSql(dialect.toQuery(data), 'INSERT INTO "t"')
    }

    def "INSERT with RETURNING wraps in SELECT FROM FINAL TABLE"() {
        given:
            def idCol = col(Integer, "id")
            Map<QColumn<?>, Expression<?>> row = new LinkedHashMap<>()
            row[idCol] = DSL.constant(42)
            def data = new InsertQueryData(
                    table("t"),
                    [idCol],
                    [row],
                    [col(Integer, "id")],
                    null
            )
        expect:
            assertSql(
                    dialect.toQuery(data),
                    'SELECT "id" FROM FINAL TABLE (INSERT INTO "t"("id") VALUES (CAST(? AS INT)))',
                    [[42, Types.INTEGER]]
            )
    }

    def "INSERT with non-null OnConflictData throws UnsupportedOperationException"() {
        given:
            def onConflict = new OnConflictData(
                    [col(Integer, "id")],
                    OnConflictData.ConflictAction.DO_NOTHING,
                    [],
                    [],
                    null
            )
            def data = new InsertQueryData(
                    table("t"),
                    [col(Integer, "id")],
                    [],
                    [],
                    onConflict
            )
        when:
            dialect.toQuery(data)
        then:
            def e = thrown(UnsupportedOperationException)
            e.message == "ON CONFLICT clause is not supported by H2 dialect"
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
            sql.startsWith('INSERT INTO "t"')
            !sql.contains("alias_x")
    }
}
