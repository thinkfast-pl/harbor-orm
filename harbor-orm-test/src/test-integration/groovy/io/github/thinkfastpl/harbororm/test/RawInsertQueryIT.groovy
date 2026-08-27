// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable

/**
 * Integration tests for raw INSERT queries using session.insert() / session.insertInto() API.
 * Tests run against both H2 and PostgreSQL databases.
 */
class RawInsertQueryIT extends AbstractHarborIT {

    def "insert single row with set() method"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "Test Name")
                    .set(basics.numero, 42)
                    .execute()

        then:
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 1

        and:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .fetchAll()
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Test Name"
                r.get(basics.numero) == 42
            }

        where:
            session << allSessions
    }

    def "insert row with multiple columns using chained set() calls"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insertInto(basics)
                    .set(basics.id, 100L)
                    .set(basics.name, "First Column")
                    .set(basics.numero, 999)
                    .execute()

        then:
            List<Record> records = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .fetchAll()
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.id) == 100L
                r.get(basics.name) == "First Column"
                r.get(basics.numero) == 999
            }

        where:
            session << allSessions
    }

    def "insert multiple rows using nextRow()"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "Row One")
                    .set(basics.numero, 10)
                    .nextRow()
                    .set(basics.id, 2L)
                    .set(basics.name, "Row Two")
                    .set(basics.numero, 20)
                    .nextRow()
                    .set(basics.id, 3L)
                    .set(basics.name, "Row Three")
                    .set(basics.numero, 30)
                    .execute()

        then:
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 3

        and:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchAll()
            records.size() == 3

            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Row One"
                r.get(basics.numero) == 10
            }
            with(records[1]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "Row Two"
                r.get(basics.numero) == 20
            }
            with(records[2]) { r ->
                r.get(basics.id) == 3L
                r.get(basics.name) == "Row Three"
                r.get(basics.numero) == 30
            }

        where:
            session << allSessions
    }

    def "insert multiple rows with mixed column order using nextRow()"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "First")
                    .set(basics.numero, 100)
                    .nextRow()
                    .set(basics.id, 2L)
                    .set(basics.numero, 200)  // different order
                    .set(basics.name, "Second")
                    .execute()

        then:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchAll()
            records.size() == 2

            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "First"
                r.get(basics.numero) == 100
            }
            with(records[1]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "Second"
                r.get(basics.numero) == 200
            }

        where:
            session << allSessions
    }

    def "insert with RETURNING clause fetches all inserted rows"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            List<Record> returnedRecords = session.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "Returned Row")
                    .set(basics.numero, 555)
                    .returning(basics.id, basics.name, basics.numero)
                    .executeAndFetchAll()

        then:
            returnedRecords.size() == 1
            with(returnedRecords[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Returned Row"
                r.get(basics.numero) == 555
            }

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "insert with RETURNING clause fetches single row"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            Record returned = session.insertInto(basics)
                    .set(basics.id, 42L)
                    .set(basics.name, "Single Return")
                    .set(basics.numero, 777)
                    .returning(basics.id, basics.name, basics.numero)
                    .executeAndFetchSingle()

        then:
            with(returned) { r ->
                r.get(basics.id) == 42L
                r.get(basics.name) == "Single Return"
                r.get(basics.numero) == 777
            }

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "insert multiple rows with RETURNING clause"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            List<Record> returnedRecords = session.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "First Returned")
                    .set(basics.numero, 11)
                    .nextRow()
                    .set(basics.id, 2L)
                    .set(basics.name, "Second Returned")
                    .set(basics.numero, 22)
                    .returning(basics.id, basics.name, basics.numero)
                    .executeAndFetchAll()

        then:
            returnedRecords.size() == 2
            with(returnedRecords[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "First Returned"
                r.get(basics.numero) == 11
            }
            with(returnedRecords[1]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "Second Returned"
                r.get(basics.numero) == 22
            }

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "insert with RETURNING clause returns only selected columns"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            Record returned = session.insertInto(basics)
                    .set(basics.id, 99L)
                    .set(basics.name, "Partial Return")
                    .set(basics.numero, 123)
                    .returning(basics.id, basics.name)  // only id and name
                    .executeAndFetchSingle()

        then:
            returned.get(basics.id) == 99L
            returned.get(basics.name) == "Partial Return"
            returned.columnsCount() == 2

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "insert and verify data integrity with select"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when: "insert data using raw insert"
            session.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "Verify Me")
                    .set(basics.numero, 12345)
                    .execute()

        then: "verify the data exists in the database"
            Long count = session.select(DSL.count())
                    .from(basics)
                    .where(basics.id.eq(1L))
                    .fetchSingle()
            count == 1

        and: "verify the correct values were inserted"
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .where(basics.id.eq(1L))
                    .fetchSingle()
            record.get(basics.id) == 1L
            record.get(basics.name) == "Verify Me"
            record.get(basics.numero) == 12345

        where:
            session << allSessions
    }

    def "insert using session.insert().into() method"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 7L)
                    .set(basics.name, "Via Insert Into")
                    .set(basics.numero, 777)
                    .execute()

        then:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .fetchAll()
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.id) == 7L
                r.get(basics.name) == "Via Insert Into"
                r.get(basics.numero) == 777
            }

        where:
            session << allSessions
    }

    def "insert empty string value"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "")
                    .set(basics.numero, 0)
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.id) == 1L
            record.get(basics.name) == ""
            record.get(basics.numero) == 0

        where:
            session << allSessions
    }

    def "insert negative numbers"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "Negative Test")
                    .set(basics.numero, -999)
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.numero) == -999

        where:
            session << allSessions
    }

    def "insert large number of rows using nextRow()"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            def insertQuery = session.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "Row 1")
                    .set(basics.numero, 1)

            for (int i = 2; i <= 10; i++) {
                insertQuery = insertQuery
                        .nextRow()
                        .set(basics.id, (long) i)
                        .set(basics.name, "Row " + i)
                        .set(basics.numero, i)
            }

        when:
            insertQuery.execute()

        then:
            Long count = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()
            count == 10

        and:
            Long sum = session.select(DSL.sum(basics.numero))
                    .from(basics)
                    .fetchSingle()
            sum == 55L  // 1+2+3+...+10 = 55

        where:
            session << allSessions
    }

    def "insert with special characters in string"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "Test's \"special\" <chars> & more")
                    .set(basics.numero, 1)
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.name) == "Test's \"special\" <chars> & more"

        where:
            session << allSessions
    }
}
