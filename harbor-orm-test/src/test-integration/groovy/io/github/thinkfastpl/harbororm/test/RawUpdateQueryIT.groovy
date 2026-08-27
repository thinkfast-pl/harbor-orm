// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

/**
 * Integration tests for raw UPDATE queries using session.update() API.
 * Tests run against both H2 and PostgreSQL databases.
 */
class RawUpdateQueryIT extends AbstractHarborIT {

    def "update single column with set() method"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Original Name", 100)

        when:
            session.update(basics)
                    .set(basics.name, "Updated Name")
                    .where(basics.id.eq(1L))
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .where(basics.id.eq(1L))
                    .fetchSingle()
            record.get(basics.id) == 1L
            record.get(basics.name) == "Updated Name"
            record.get(basics.numero) == 100

        where:
            session << allSessions
    }

    def "update multiple columns with chained set() calls"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Original Name", 100)

        when:
            session.update(basics)
                    .set(basics.name, "New Name")
                    .set(basics.numero, 200)
                    .where(basics.id.eq(1L))
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .where(basics.id.eq(1L))
                    .fetchSingle()
            record.get(basics.id) == 1L
            record.get(basics.name) == "New Name"
            record.get(basics.numero) == 200

        where:
            session << allSessions
    }

    def "update with AND condition in WHERE clause"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Name A", 100)
            fixtures.addBasic(2L, "Name A", 200)
            fixtures.addBasic(3L, "Name B", 100)

        when:
            session.update(basics)
                    .set(basics.name, "Updated")
                    .where(basics.name.eq("Name A").and(basics.numero.eq(100)))
                    .execute()

        then:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchAll()
            records.size() == 3

            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Updated"
                r.get(basics.numero) == 100
            }
            with(records[1]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "Name A"
                r.get(basics.numero) == 200
            }
            with(records[2]) { r ->
                r.get(basics.id) == 3L
                r.get(basics.name) == "Name B"
                r.get(basics.numero) == 100
            }

        where:
            session << allSessions
    }

    def "update with OR condition in WHERE clause"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Name A", 100)
            fixtures.addBasic(2L, "Name B", 200)
            fixtures.addBasic(3L, "Name C", 300)

        when:
            session.update(basics)
                    .set(basics.name, "Updated")
                    .where(basics.id.eq(1L).or(basics.id.eq(3L)))
                    .execute()

        then:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchAll()
            records.size() == 3

            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Updated"
            }
            with(records[1]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "Name B"
            }
            with(records[2]) { r ->
                r.get(basics.id) == 3L
                r.get(basics.name) == "Updated"
            }

        where:
            session << allSessions
    }

    def "update with complex AND/OR conditions"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "A", 20)
            fixtures.addBasic(3L, "B", 10)
            fixtures.addBasic(4L, "B", 20)

        when: "update where (name = 'A' AND numero = 10) OR (name = 'B' AND numero = 20)"
            session.update(basics)
                    .set(basics.name, "Updated")
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

            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Updated"
            }
            with(records[1]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "A"
            }
            with(records[2]) { r ->
                r.get(basics.id) == 3L
                r.get(basics.name) == "B"
            }
            with(records[3]) { r ->
                r.get(basics.id) == 4L
                r.get(basics.name) == "Updated"
            }

        where:
            session << allSessions
    }

    def "update all rows when no WHERE clause is specified"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Name A", 100)
            fixtures.addBasic(2L, "Name B", 200)
            fixtures.addBasic(3L, "Name C", 300)

        when:
            session.update(basics)
                    .set(basics.name, "All Updated")
                    .execute()

        then:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchAll()
            records.size() == 3
            records.every { it.get(basics.name) == "All Updated" }

        where:
            session << allSessions
    }

    def "update with RETURNING clause fetches all updated rows"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Name A", 100)
            fixtures.addBasic(2L, "Name B", 200)

        when:
            List<Record> returnedRecords = session.update(basics)
                    .set(basics.name, "Returned Update")
                    .where(basics.id.eq(1L))
                    .returning(basics.id, basics.name, basics.numero)
                    .executeAndFetchAll()

        then:
            returnedRecords.size() == 1
            with(returnedRecords[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Returned Update"
                r.get(basics.numero) == 100
            }

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "update with RETURNING clause fetches single row"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(42L, "Single Return Test", 777)

        when:
            Record returned = session.update(basics)
                    .set(basics.name, "Single Returned")
                    .where(basics.id.eq(42L))
                    .returning(basics.id, basics.name, basics.numero)
                    .executeAndFetchSingle()

        then:
            with(returned) { r ->
                r.get(basics.id) == 42L
                r.get(basics.name) == "Single Returned"
                r.get(basics.numero) == 777
            }

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "update multiple rows with RETURNING clause"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "First", 10)
            fixtures.addBasic(2L, "Second", 20)
            fixtures.addBasic(3L, "Third", 30)

        when:
            List<Record> returnedRecords = session.update(basics)
                    .set(basics.name, "Batch Updated")
                    .where(basics.id.le(2L))
                    .returning(basics.id, basics.name)
                    .executeAndFetchAll()

        then:
            returnedRecords.size() == 2
            returnedRecords.collect { it.get(basics.id) }.sort() == [1L, 2L]
            returnedRecords.every { it.get(basics.name) == "Batch Updated" }

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "update with RETURNING clause returns only selected columns"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(99L, "Partial Return Test", 123)

        when:
            Record returned = session.update(basics)
                    .set(basics.numero, 456)
                    .where(basics.id.eq(99L))
                    .returning(basics.id, basics.numero)
                    .executeAndFetchSingle()

        then:
            returned.get(basics.id) == 99L
            returned.get(basics.numero) == 456
            returned.columnsCount() == 2

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "update with expression incrementing numeric column"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Counter", 100)

        when:
            session.update(basics)
                    .set(basics.numero, basics.numero.add(1))
                    .where(basics.id.eq(1L))
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .where(basics.id.eq(1L))
                    .fetchSingle()
            record.get(basics.numero) == 101

        where:
            session << allSessions
    }

    def "update with expression decrementing numeric column"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Counter", 100)

        when:
            session.update(basics)
                    .set(basics.numero, basics.numero.subtract(10))
                    .where(basics.id.eq(1L))
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .where(basics.id.eq(1L))
                    .fetchSingle()
            record.get(basics.numero) == 90

        where:
            session << allSessions
    }

    def "update with expression multiplying numeric column"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Multiplier", 10)

        when:
            session.update(basics)
                    .set(basics.numero, basics.numero.multiply(5))
                    .where(basics.id.eq(1L))
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .where(basics.id.eq(1L))
                    .fetchSingle()
            record.get(basics.numero) == 50

        where:
            session << allSessions
    }

    def "update with greater than condition"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Low", 10)
            fixtures.addBasic(2L, "Medium", 50)
            fixtures.addBasic(3L, "High", 100)

        when:
            session.update(basics)
                    .set(basics.name, "Above Threshold")
                    .where(basics.numero.gt(30))
                    .execute()

        then:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchAll()

            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Low"
            }
            with(records[1]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "Above Threshold"
            }
            with(records[2]) { r ->
                r.get(basics.id) == 3L
                r.get(basics.name) == "Above Threshold"
            }

        where:
            session << allSessions
    }

    def "update with BETWEEN condition"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "One", 1)
            fixtures.addBasic(2L, "Two", 2)
            fixtures.addBasic(3L, "Three", 3)
            fixtures.addBasic(4L, "Four", 4)
            fixtures.addBasic(5L, "Five", 5)

        when:
            session.update(basics)
                    .set(basics.name, "In Range")
                    .where(basics.numero.between(2, 4))
                    .execute()

        then:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchAll()

            records[0].get(basics.name) == "One"
            records[1].get(basics.name) == "In Range"
            records[2].get(basics.name) == "In Range"
            records[3].get(basics.name) == "In Range"
            records[4].get(basics.name) == "Five"

        where:
            session << allSessions
    }

    def "update no matching rows does not fail"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Existing", 100)

        when:
            session.update(basics)
                    .set(basics.name, "Non-existent Update")
                    .where(basics.id.eq(999L))
                    .execute()

        then:
            noExceptionThrown()

        and: "original data is unchanged"
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .where(basics.id.eq(1L))
                    .fetchSingle()
            record.get(basics.name) == "Existing"

        where:
            session << allSessions
    }

    def "update with empty string value"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Non-empty", 100)

        when:
            session.update(basics)
                    .set(basics.name, "")
                    .where(basics.id.eq(1L))
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.name) == ""

        where:
            session << allSessions
    }

    def "update with special characters in string"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Plain", 100)

        when:
            session.update(basics)
                    .set(basics.name, "Test's \"special\" <chars> & more")
                    .where(basics.id.eq(1L))
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.name) == "Test's \"special\" <chars> & more"

        where:
            session << allSessions
    }

    def "update with negative number"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Test", 100)

        when:
            session.update(basics)
                    .set(basics.numero, -999)
                    .where(basics.id.eq(1L))
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.numero) == -999

        where:
            session << allSessions
    }

    def "update and verify data integrity with select"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Original", 100)
            fixtures.addBasic(2L, "Keep This", 200)

        when: "update specific row"
            session.update(basics)
                    .set(basics.name, "Modified")
                    .set(basics.numero, 150)
                    .where(basics.id.eq(1L))
                    .execute()

        then: "verify updated row"
            Record updated = session.select(basics.allColumns)
                    .from(basics)
                    .where(basics.id.eq(1L))
                    .fetchSingle()
            updated.get(basics.name) == "Modified"
            updated.get(basics.numero) == 150

        and: "verify other row is unchanged"
            Record unchanged = session.select(basics.allColumns)
                    .from(basics)
                    .where(basics.id.eq(2L))
                    .fetchSingle()
            unchanged.get(basics.name) == "Keep This"
            unchanged.get(basics.numero) == 200

        where:
            session << allSessions
    }

    def "update using session.update().table() method"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Via Table", 777)

        when:
            session.update()
                    .table(basics.getTableName())
                    .set(basics.name, "Via Update Table")
                    .where(basics.id.eq(1L))
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.name) == "Via Update Table"

        where:
            session << allSessions
    }
}
