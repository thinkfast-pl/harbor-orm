// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.expression.FunctionCallTableSource
import io.github.thinkfastpl.harbororm.api.metadata.QColumn
import io.github.thinkfastpl.harbororm.api.metadata.QTableName
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.GetBasicsAbove
import io.github.thinkfastpl.harbororm.test.domain.GetBasicsAboveFunction
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

/**
 * Integration tests for DSL.function() — calling user-defined scalar functions in expressions.
 *
 * Uses the multiply_value(int, int) function defined in domain-h2.sql and domain-postgres.sql.
 */
class StoredFunctionIT extends AbstractHarborIT {

    def "DSL.function() can call a scalar function in SELECT"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 7)

            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> multiplyExpr = DSL.function(
                    "multiply_value", Integer.class, List.of(basics.numero, DSL.constant(3)))

        when:
            List<Record> records = session.select(basics.name, multiplyExpr.as("result"))
                    .from(basics)
                    .fetchAll()

        then:
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get("result") == 21
            }

        where:
            session << allSessions
    }

    def "DSL.function() can be used in WHERE clause"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 3)
            fixtures.addBasic(2L, "B", 5)

            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> multiplyExpr = DSL.function(
                    "multiply_value", Integer.class, List.of(basics.numero, DSL.constant(10)))

        when:
            List<Record> records = session.select(basics.name, basics.numero)
                    .from(basics)
                    .where(multiplyExpr.gt(35))
                    .fetchAll()

        then:
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.name) == "B"
                r.get(basics.numero) == 5
            }

        where:
            session << allSessions
    }

    def "DSL.function() with no arguments calls built-in abs(-42)"() {
        given:
            Expression<Integer> absExpr = DSL.function(
                    "abs", Integer.class, List.of(DSL.constant(-42)))

        when:
            def result = session.select(absExpr.as("result")).fetchSingle()

        then:
            result == 42

        where:
            session << allSessions
    }

    def "session.callReturning() returns scalar function result"() {
        when:
            Integer result = session.callReturning("multiply_value", Integer.class, 6, 7)

        then:
            result == 42

        where:
            session << allSessions
    }

    def "session.call() executes procedure without error"() {
        when:
            session.call("do_nothing")

        then:
            noExceptionThrown()

        where:
            session << allSessions
    }

    def "FunctionCallTableSource can be used in FROM clause"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)
            fixtures.addBasic(3L, "C", 30)

            def fnTableName = new QTableName("get_basics_above", null, "fn")
            def idCol = QColumn.simple(Long.class, "fn", "id")
            def nameCol = QColumn.simple(String.class, "fn", "name")
            def numeroCol = QColumn.simple(Integer.class, "fn", "numero")
            def tableSource = new FunctionCallTableSource(fnTableName, List.of(idCol, nameCol, numeroCol), List.of(DSL.constant(15)), Record.class)

        when:
            List<Record> records = session.select(nameCol, numeroCol)
                    .from(tableSource)
                    .orderBy(numeroCol.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(nameCol) == "B"
            records[0].get(numeroCol) == 20
            records[1].get(nameCol) == "C"
            records[1].get(numeroCol) == 30

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "@StoredFunction generated class can be used as table source in FROM"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)
            fixtures.addBasic(3L, "C", 30)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            List<Record> records = session.select(fn.name, fn.numero)
                    .from(fn.call(15))
                    .orderBy(fn.numero.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(fn.name) == "B"
            records[0].get(fn.numero) == 20
            records[1].get(fn.name) == "C"
            records[1].get(fn.numero) == 30

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "session.select(fn.call()) returns typed instances with fetchAll"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)
            fixtures.addBasic(3L, "C", 30)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            List<GetBasicsAbove> results = session.select(fn.call(15))
                    .orderBy(fn.numero.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].name == "B"
            results[0].numero == 20
            results[1].name == "C"
            results[1].numero == 30

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "session.select(fn.call()) supports where filtering"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)
            fixtures.addBasic(3L, "C", 30)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            List<GetBasicsAbove> results = session.select(fn.call(15))
                    .where(fn.numero.gt(25))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].name == "C"
            results[0].numero == 30

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "session.select(fn.call()) supports fetchOne"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            Optional<GetBasicsAbove> result = session.select(fn.call(15))
                    .fetchOne()

        then:
            result.isPresent()
            result.get().name == "B"
            result.get().numero == 20

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "session.select(fn.call()) supports fetchSingle"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            GetBasicsAbove result = session.select(fn.call(15))
                    .fetchSingle()

        then:
            result.name == "B"
            result.numero == 20

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "session.select(fn.call()) supports count"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)
            fixtures.addBasic(3L, "C", 30)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            long count = session.select(fn.call(15)).count()

        then:
            count == 2

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "session.select(fn.call()) supports exists"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            boolean hasResults = session.select(fn.call(15)).exists()
            boolean noResults = session.select(fn.call(100)).exists()

        then:
            hasResults
            !noResults

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "session.select(fn.call()) supports limit and offset"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)
            fixtures.addBasic(3L, "C", 30)
            fixtures.addBasic(4L, "D", 40)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            List<GetBasicsAbove> results = session.select(fn.call(15))
                    .orderBy(fn.numero.asc())
                    .limit(1)
                    .offset(1)
                    .fetchAll()

        then:
            results.size() == 1
            results[0].name == "C"
            results[0].numero == 30

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "existing Record-based path still works after typed select addition"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            List<Record> records = session.select(fn.name, fn.numero)
                    .from(fn.call(15))
                    .orderBy(fn.numero.asc())
                    .fetchAll()

        then:
            records.size() == 1
            records[0].get(fn.name) == "B"
            records[0].get(fn.numero) == 20

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "expression-based call() accepts DSL.constant() expression"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)
            fixtures.addBasic(3L, "C", 30)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            List<GetBasicsAbove> results = session.select(fn.call(DSL.constant(15)))
                    .orderBy(fn.numero.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].name == "B"
            results[0].numero == 20
            results[1].name == "C"
            results[1].numero == 30

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "expression-based call() accepts column reference in LATERAL join"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)
            fixtures.addBasic(3L, "C", 30)

            BasicsTable basics = new BasicsTable("b")
            GetBasicsAboveFunction fn = new GetBasicsAboveFunction(null)

        when:
            def lateralSource = DSL.select(fn.id, fn.name, fn.numero)
                    .from(fn.call(basics.numero))
                    .asTableSource("lat")

            def latName = lateralSource.getColumn(fn.name, "name")
            def latNumero = lateralSource.getColumn(fn.numero, "numero")

            List<Record> records = session.select(basics.name.as("outer_name"), latName, latNumero)
                    .from(basics)
                    .crossJoinLateral(lateralSource)
                    .where(basics.numero.eq(10))
                    .orderBy(latNumero.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get("outer_name") == "A"
            records[0].get(latName) == "B"
            records[0].get(latNumero) == 20
            records[1].get("outer_name") == "A"
            records[1].get(latName) == "C"
            records[1].get(latNumero) == 30

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "expression-based call() accepts arithmetic expression"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)
            fixtures.addBasic(3L, "C", 30)
            fixtures.addBasic(4L, "D", 40)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            List<GetBasicsAbove> results = session.select(fn.call(DSL.constant(10).add(DSL.constant(15))))
                    .orderBy(fn.numero.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].name == "C"
            results[0].numero == 30
            results[1].name == "D"
            results[1].numero == 40

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }
}
