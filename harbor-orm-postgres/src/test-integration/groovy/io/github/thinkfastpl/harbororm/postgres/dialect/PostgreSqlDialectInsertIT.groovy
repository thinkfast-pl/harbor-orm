// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.postgres.PostgresBaseIT
import io.github.thinkfastpl.harbororm.query.BasicsTable

class PostgreSqlDialectInsertIT extends PostgresBaseIT {

    void setup() {
        loadScript("basics.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "insert one row"() {
        given:
            BasicsTable basics = new BasicsTable(null)

            session.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "My name")
                    .set(basics.num, 1)
                    .execute()

        when:
            Long insertedRows = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()

        then:
            insertedRows == 1

        when:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .fetchAll()

        then:
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "My name"
                r.get(basics.num) == 1
            }
    }

    def "insert multiple rows"() {
        given:
            BasicsTable basics = new BasicsTable(null)

            session.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "My name")
                    .set(basics.num, 11)
                    .nextRow()
                    .set(basics.id, 2L)
                    .set(basics.name, "My house")
                    .set(basics.num, 22)
                    .execute()

        when:
            Long insertedRows = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()

        then:
            insertedRows == 2

        when:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "My name"
                r.get(basics.num) == 11
            }
            with(records[1]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "My house"
                r.get(basics.num) == 22
            }

    }

    def "insert multiple rows mixed columns order"() {
        given:
            BasicsTable basics = new BasicsTable(null)

            session.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "My name")
                    .set(basics.num, 11)
                    .nextRow()
                    .set(basics.id, 2L)
                    .set(basics.num, 22)
                    .set(basics.name, "My house")
                    .execute()

        when:
            Long insertedRows = session.select(DSL.count())
                    .from(basics)
                    .fetchSingle()

        then:
            insertedRows == 2

        when:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "My name"
                r.get(basics.num) == 11
            }
            with(records[1]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "My house"
                r.get(basics.num) == 22
            }

    }

    def "insert one row returning fetch all"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            List<Record> records = session.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "My name")
                    .set(basics.num, 123)
                    .returning(basics.id, basics.name, basics.num)
                    .executeAndFetchAll()

        then:
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "My name"
                r.get(basics.num) == 123
            }
    }

    def "insert one row returning fetch single"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            Record record = session.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "My name")
                    .set(basics.num, 123)
                    .returning(basics.id, basics.name, basics.num)
                    .executeAndFetchSingle()

        then:
            with(record) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "My name"
                r.get(basics.num) == 123
            }
    }

    def "insert multiple rows returning fetch all"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            List<Record> records = session.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "My name")
                    .set(basics.num, 11)
                    .nextRow()
                    .set(basics.id, 2L)
                    .set(basics.num, 22)
                    .set(basics.name, "My house")
                    .returning(basics.id, basics.name, basics.num)
                    .executeAndFetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "My name"
                r.get(basics.num) == 11
            }
            with(records[1]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "My house"
                r.get(basics.num) == 22
            }
    }
}
