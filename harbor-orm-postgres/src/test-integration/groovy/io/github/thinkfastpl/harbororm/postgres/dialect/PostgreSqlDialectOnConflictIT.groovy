// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.metadata.QColumn
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.postgres.PostgresBaseIT
import io.github.thinkfastpl.harbororm.query.BasicsTable

class PostgreSqlDialectOnConflictIT extends PostgresBaseIT {

    void setupSpec() {
        loadScript("basics.sql")
    }

    void cleanupSpec() {
        dropAllObjects()
    }

    def "DO NOTHING skips conflicting row"() {
        given:
            BasicsTable t = new BasicsTable(null)
            session.insertInto(t).set(t.id, 1L).set(t.name, "Alice").set(t.num, 10).execute()

        when:
            session.insertInto(t)
                    .set(t.id, 1L)
                    .set(t.name, "Bob")
                    .set(t.num, 20)
                    .onConflict(t.id as QColumn)
                    .doNothing()
                    .execute()

        then:
            List<Record> records = session.select(t.allColumns).from(t).fetchAll()
            records.size() == 1
            records[0].get(t.name) == "Alice"
            records[0].get(t.num) == 10
    }

    def "DO NOTHING inserts non-conflicting row normally"() {
        given:
            BasicsTable t = new BasicsTable(null)
            session.insertInto(t).set(t.id, 1L).set(t.name, "Alice").set(t.num, 10).execute()

        when:
            session.insertInto(t)
                    .set(t.id, 2L)
                    .set(t.name, "Bob")
                    .set(t.num, 20)
                    .onConflict(t.id as QColumn)
                    .doNothing()
                    .execute()

        then:
            Long count = session.select(DSL.count()).from(t).fetchSingle()
            count == 2
    }

    def "DO UPDATE SET with literal values"() {
        given:
            BasicsTable t = new BasicsTable(null)
            session.insertInto(t).set(t.id, 1L).set(t.name, "Alice").set(t.num, 10).execute()

        when:
            session.insertInto(t)
                    .set(t.id, 1L)
                    .set(t.name, "Bob")
                    .set(t.num, 99)
                    .onConflict(t.id as QColumn)
                    .doUpdate()
                    .set(t.name, "Updated")
                    .set(t.num, 42)
                    .execute()

        then:
            List<Record> records = session.select(t.allColumns).from(t).fetchAll()
            records.size() == 1
            records[0].get(t.name) == "Updated"
            records[0].get(t.num) == 42
    }

    def "DO UPDATE SET with DSL.excluded() uses proposed row values"() {
        given:
            BasicsTable t = new BasicsTable(null)
            session.insertInto(t).set(t.id, 1L).set(t.name, "Alice").set(t.num, 10).execute()

        when:
            session.insertInto(t)
                    .set(t.id, 1L)
                    .set(t.name, "Bob")
                    .set(t.num, 99)
                    .onConflict(t.id as QColumn)
                    .doUpdate()
                    .set(t.name, DSL.excluded(t.name))
                    .execute()

        then:
            List<Record> records = session.select(t.allColumns).from(t).fetchAll()
            records.size() == 1
            records[0].get(t.name) == "Bob"
    }

    def "DO UPDATE SET with arithmetic expression using EXCLUDED"() {
        given:
            BasicsTable t = new BasicsTable("basics")
            session.insertInto(t).set(t.id, 1L).set(t.name, "Alice").set(t.num, 10).execute()

        when:
            session.insertInto(t)
                    .set(t.id, 1L)
                    .set(t.name, "Alice")
                    .set(t.num, 5)
                    .onConflict(t.id as QColumn)
                    .doUpdate()
                    .set(t.num, t.num.add(DSL.excluded(t.num)))
                    .execute()

        then:
            List<Record> records = session.select(t.allColumns).from(t).fetchAll()
            records.size() == 1
            records[0].get(t.num) == 15  // 10 + 5
    }

    def "DO UPDATE with WHERE - condition met, row updated"() {
        given:
            BasicsTable t = new BasicsTable("basics")
            session.insertInto(t).set(t.id, 1L).set(t.name, "Alice").set(t.num, 10).execute()

        when:
            session.insertInto(t)
                    .set(t.id, 1L)
                    .set(t.name, "Bob")
                    .set(t.num, 99)
                    .onConflict(t.id as QColumn)
                    .doUpdate()
                    .set(t.name, DSL.excluded(t.name))
                    .where(t.num.lt(50))
                    .execute()

        then:
            List<Record> records = session.select(t.allColumns).from(t).fetchAll()
            records.size() == 1
            records[0].get(t.name) == "Bob"  // updated because num (10) < 50
    }

    def "DO UPDATE with WHERE - condition not met, row unchanged"() {
        given:
            BasicsTable t = new BasicsTable("basics")
            session.insertInto(t).set(t.id, 1L).set(t.name, "Alice").set(t.num, 100).execute()

        when:
            session.insertInto(t)
                    .set(t.id, 1L)
                    .set(t.name, "Bob")
                    .set(t.num, 99)
                    .onConflict(t.id as QColumn)
                    .doUpdate()
                    .set(t.name, DSL.excluded(t.name))
                    .where(t.num.lt(50))
                    .execute()

        then:
            List<Record> records = session.select(t.allColumns).from(t).fetchAll()
            records.size() == 1
            records[0].get(t.name) == "Alice"  // unchanged because num (100) >= 50
    }

    def "ON CONFLICT with RETURNING returns the resulting row"() {
        given:
            BasicsTable t = new BasicsTable(null)
            session.insertInto(t).set(t.id, 1L).set(t.name, "Alice").set(t.num, 10).execute()

        when:
            Record record = session.insertInto(t)
                    .set(t.id, 1L)
                    .set(t.name, "Bob")
                    .set(t.num, 20)
                    .onConflict(t.id as QColumn)
                    .doUpdate()
                    .set(t.name, DSL.excluded(t.name))
                    .set(t.num, DSL.excluded(t.num))
                    .returning(t.id, t.name, t.num)
                    .executeAndFetchSingle()

        then:
            record.get(t.id) == 1L
            record.get(t.name) == "Bob"
            record.get(t.num) == 20
    }

    def "multi-row insert with ON CONFLICT - some rows conflict, some do not"() {
        given:
            BasicsTable t = new BasicsTable(null)
            session.insertInto(t).set(t.id, 1L).set(t.name, "Alice").set(t.num, 10).execute()

        when:
            session.insertInto(t)
                    .set(t.id, 1L)
                    .set(t.name, "Alice-updated")
                    .set(t.num, 11)
                    .nextRow()
                    .set(t.id, 2L)
                    .set(t.name, "Bob")
                    .set(t.num, 20)
                    .onConflict(t.id as QColumn)
                    .doUpdate()
                    .set(t.name, DSL.excluded(t.name))
                    .set(t.num, DSL.excluded(t.num))
                    .execute()

        then:
            List<Record> records = session.select(t.allColumns).from(t).orderBy(t.id.asc()).fetchAll()
            records.size() == 2
            records[0].get(t.id) == 1L
            records[0].get(t.name) == "Alice-updated"
            records[0].get(t.num) == 11
            records[1].get(t.id) == 2L
            records[1].get(t.name) == "Bob"
            records[1].get(t.num) == 20
    }

    def "DO NOTHING with RETURNING returns empty for conflicting row"() {
        given:
            BasicsTable t = new BasicsTable(null)
            session.insertInto(t).set(t.id, 1L).set(t.name, "Alice").set(t.num, 10).execute()

        when:
            List<Record> records = session.insertInto(t)
                    .set(t.id, 1L)
                    .set(t.name, "Bob")
                    .set(t.num, 20)
                    .onConflict(t.id as QColumn)
                    .doNothing()
                    .returning(t.id)
                    .executeAndFetchAll()

        then:
            records.isEmpty()
    }
}
