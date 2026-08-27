// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

/**
 * Integration tests for raw DELETE queries using session.delete() API.
 * Tests run against both H2 and PostgreSQL databases.
 */
class RawDeleteQueryIT extends AbstractHarborIT {

    def "delete single row with WHERE clause"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "To Delete", 100)
            fixtures.addBasic(2L, "Keep This", 200)

        when:
            session.delete(basics)
                    .where(basics.id.eq(1L))
                    .execute()

        then:
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 1

        and:
            Record remaining = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            remaining.get(basics.id) == 2L
            remaining.get(basics.name) == "Keep This"

        where:
            session << allSessions
    }

    def "delete with AND condition in WHERE clause"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Name A", 100)
            fixtures.addBasic(2L, "Name A", 200)
            fixtures.addBasic(3L, "Name B", 100)

        when:
            session.delete(basics)
                    .where(basics.name.eq("Name A").and(basics.numero.eq(100)))
                    .execute()

        then:
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 2

        and:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchAll()
            records.size() == 2

            with(records[0]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "Name A"
                r.get(basics.numero) == 200
            }
            with(records[1]) { r ->
                r.get(basics.id) == 3L
                r.get(basics.name) == "Name B"
                r.get(basics.numero) == 100
            }

        where:
            session << allSessions
    }

    def "delete with OR condition in WHERE clause"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Name A", 100)
            fixtures.addBasic(2L, "Name B", 200)
            fixtures.addBasic(3L, "Name C", 300)

        when:
            session.delete(basics)
                    .where(basics.id.eq(1L).or(basics.id.eq(3L)))
                    .execute()

        then:
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 1

        and:
            Record remaining = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            remaining.get(basics.id) == 2L
            remaining.get(basics.name) == "Name B"

        where:
            session << allSessions
    }

    def "delete with complex AND/OR conditions"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "A", 20)
            fixtures.addBasic(3L, "B", 10)
            fixtures.addBasic(4L, "B", 20)

        when: "delete where (name = 'A' AND numero = 10) OR (name = 'B' AND numero = 20)"
            session.delete(basics)
                    .where(
                        basics.name.eq("A").and(basics.numero.eq(10))
                                .or(basics.name.eq("B").and(basics.numero.eq(20)))
                    )
                    .execute()

        then:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchAll()
            records.size() == 2

            with(records[0]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "A"
            }
            with(records[1]) { r ->
                r.get(basics.id) == 3L
                r.get(basics.name) == "B"
            }

        where:
            session << allSessions
    }

    def "delete all rows when no WHERE clause is specified"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Name A", 100)
            fixtures.addBasic(2L, "Name B", 200)
            fixtures.addBasic(3L, "Name C", 300)

        when:
            session.delete(basics)
                    .execute()

        then:
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 0

        where:
            session << allSessions
    }

    def "delete with RETURNING clause fetches all deleted rows"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "To Return", 100)
            fixtures.addBasic(2L, "Keep This", 200)

        when:
            List<Record> returnedRecords = session.delete(basics)
                    .where(basics.id.eq(1L))
                    .returning(basics.id, basics.name, basics.numero)
                    .executeAndFetchAll()

        then:
            returnedRecords.size() == 1
            with(returnedRecords[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "To Return"
                r.get(basics.numero) == 100
            }

        and: "verify the row was actually deleted"
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 1

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "delete with RETURNING clause fetches single row"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(42L, "Single Return Test", 777)

        when:
            Record returned = session.delete(basics)
                    .where(basics.id.eq(42L))
                    .returning(basics.id, basics.name, basics.numero)
                    .executeAndFetchSingle()

        then:
            with(returned) { r ->
                r.get(basics.id) == 42L
                r.get(basics.name) == "Single Return Test"
                r.get(basics.numero) == 777
            }

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "delete multiple rows with RETURNING clause"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "First", 10)
            fixtures.addBasic(2L, "Second", 20)
            fixtures.addBasic(3L, "Third", 30)

        when:
            List<Record> returnedRecords = session.delete(basics)
                    .where(basics.id.le(2L))
                    .returning(basics.id, basics.name)
                    .executeAndFetchAll()

        then:
            returnedRecords.size() == 2
            returnedRecords.collect { it.get(basics.id) }.sort() == [1L, 2L]

        and: "only one row remains"
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 1

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "delete with RETURNING clause returns only selected columns"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(99L, "Partial Return Test", 123)

        when:
            Record returned = session.delete(basics)
                    .where(basics.id.eq(99L))
                    .returning(basics.id, basics.numero)
                    .executeAndFetchSingle()

        then:
            returned.get(basics.id) == 99L
            returned.get(basics.numero) == 123
            returned.columnsCount() == 2

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "delete with IN clause"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Name A", 100)
            fixtures.addBasic(2L, "Name B", 200)
            fixtures.addBasic(3L, "Name C", 300)
            fixtures.addBasic(4L, "Name D", 400)
            fixtures.addBasic(5L, "Name E", 500)

        when:
            session.delete(basics)
                    .where(basics.id.in([1L, 3L, 5L]))
                    .execute()

        then:
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 2

        and:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchAll()
            records.size() == 2
            records[0].get(basics.id) == 2L
            records[1].get(basics.id) == 4L

        where:
            session << allSessions
    }

    def "delete non-existent rows does not fail and affects no rows"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Existing", 100)

        when:
            session.delete(basics)
                    .where(basics.id.eq(999L))
                    .execute()

        then:
            noExceptionThrown()

        and: "original data is unchanged"
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 1

        and:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.name) == "Existing"

        where:
            session << allSessions
    }

    def "delete with greater than condition"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Low", 10)
            fixtures.addBasic(2L, "Medium", 50)
            fixtures.addBasic(3L, "High", 100)

        when:
            session.delete(basics)
                    .where(basics.numero.gt(30))
                    .execute()

        then:
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 1

        and:
            Record remaining = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            remaining.get(basics.id) == 1L
            remaining.get(basics.name) == "Low"

        where:
            session << allSessions
    }

    def "delete with less than or equal condition"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "One", 1)
            fixtures.addBasic(2L, "Two", 2)
            fixtures.addBasic(3L, "Three", 3)

        when:
            session.delete(basics)
                    .where(basics.numero.le(2))
                    .execute()

        then:
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 1

        and:
            Record remaining = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            remaining.get(basics.id) == 3L

        where:
            session << allSessions
    }

    def "delete with BETWEEN condition"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "One", 1)
            fixtures.addBasic(2L, "Two", 2)
            fixtures.addBasic(3L, "Three", 3)
            fixtures.addBasic(4L, "Four", 4)
            fixtures.addBasic(5L, "Five", 5)

        when:
            session.delete(basics)
                    .where(basics.numero.between(2, 4))
                    .execute()

        then:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchAll()
            records.size() == 2
            records[0].get(basics.id) == 1L
            records[1].get(basics.id) == 5L

        where:
            session << allSessions
    }

    def "delete and verify data integrity"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "First", 100)
            fixtures.addBasic(2L, "Second", 200)
            fixtures.addBasic(3L, "Third", 300)

        when: "delete specific row"
            session.delete(basics)
                    .where(basics.id.eq(2L))
                    .execute()

        then: "verify deleted row is gone"
            Long countDeleted = session.select(DSL.count())
                    .from(basics)
                    .where(basics.id.eq(2L))
                    .fetchSingle()
            countDeleted == 0

        and: "verify other rows are unchanged"
            List<Record> remaining = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchAll()
            remaining.size() == 2

            with(remaining[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "First"
                r.get(basics.numero) == 100
            }
            with(remaining[1]) { r ->
                r.get(basics.id) == 3L
                r.get(basics.name) == "Third"
                r.get(basics.numero) == 300
            }

        where:
            session << allSessions
    }

    def "delete using session.delete().from() method"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Via From", 777)

        when:
            session.delete()
                    .from(basics.getTableName())
                    .where(basics.id.eq(1L))
                    .execute()

        then:
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 0

        where:
            session << allSessions
    }

    def "delete with empty IN clause list deletes nothing"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Name A", 100)
            fixtures.addBasic(2L, "Name B", 200)

        when:
            session.delete(basics)
                    .where(basics.id.in([]))
                    .execute()

        then:
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 2

        where:
            session << allSessions
    }

    def "delete with RETURNING and no matching rows returns empty list"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Existing", 100)

        when:
            List<Record> returnedRecords = session.delete(basics)
                    .where(basics.id.eq(999L))
                    .returning(basics.id, basics.name)
                    .executeAndFetchAll()

        then:
            returnedRecords.isEmpty()

        and: "original data is unchanged"
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 1

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "delete all rows with RETURNING clause"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "First", 10)
            fixtures.addBasic(2L, "Second", 20)
            fixtures.addBasic(3L, "Third", 30)

        when:
            List<Record> returnedRecords = session.delete(basics)
                    .returning(basics.id, basics.name)
                    .executeAndFetchAll()

        then:
            returnedRecords.size() == 3
            returnedRecords.collect { it.get(basics.id) }.sort() == [1L, 2L, 3L]

        and: "table is empty"
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 0

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "delete with multiple WHERE conditions combined"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alpha", 10)
            fixtures.addBasic(2L, "Beta", 20)
            fixtures.addBasic(3L, "Alpha", 30)
            fixtures.addBasic(4L, "Beta", 40)
            fixtures.addBasic(5L, "Gamma", 50)

        when: "delete where name = 'Alpha' OR (name = 'Beta' AND numero > 30)"
            session.delete(basics)
                    .where(
                        basics.name.eq("Alpha")
                                .or(basics.name.eq("Beta").and(basics.numero.gt(30)))
                    )
                    .execute()

        then:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchAll()
            records.size() == 2

            with(records[0]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "Beta"
                r.get(basics.numero) == 20
            }
            with(records[1]) { r ->
                r.get(basics.id) == 5L
                r.get(basics.name) == "Gamma"
                r.get(basics.numero) == 50
            }

        where:
            session << allSessions
    }

    def "delete with RETURNING and executeAndFetchOne returns Optional of record"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Existing Row", 100)

        when: "deleting existing row returns Optional.of(record)"
            Optional<Record> result = session.delete(basics)
                    .where(basics.id.eq(1L))
                    .returning(basics.id, basics.name, basics.numero)
                    .executeAndFetchOne()

        then:
            result.isPresent()
            with(result.get()) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Existing Row"
                r.get(basics.numero) == 100
            }

        when: "deleting non-existent row returns Optional.empty()"
            Optional<Record> emptyResult = session.delete(basics)
                    .where(basics.id.eq(999L))
                    .returning(basics.id, basics.name)
                    .executeAndFetchOne()

        then:
            !emptyResult.isPresent()

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "executeAndFetchSingle throws exception when multiple rows deleted"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Row One", 100)
            fixtures.addBasic(2L, "Row Two", 200)
            fixtures.addBasic(3L, "Row Three", 300)

        when:
            session.delete(basics)
                    .where(basics.id.le(2L))
                    .returning(basics.id, basics.name)
                    .executeAndFetchSingle()

        then:
            IllegalStateException ex = thrown()
            ex.message.contains("Expected only 1 result")

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }
}
