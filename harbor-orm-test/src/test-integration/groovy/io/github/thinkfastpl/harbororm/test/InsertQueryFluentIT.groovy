// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.AuditedEntitiesTable
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.query.RolesTable

import java.time.LocalDateTime

/**
 * Integration tests for InsertQuery fluent API using session.insert().into() pattern.
 * Tests run against both H2 and PostgreSQL databases.
 *
 * This test class focuses on:
 * - Fluent API: session.insert().into(QTable).set().execute()
 * - Expression values (DSL.constant, DSL.currentDateTime)
 * - Null value handling
 * - RETURNING clause variations
 * - executeAndFetchOne() method
 */
class InsertQueryFluentIT extends AbstractHarborIT {

    // ==================== FLUENT API TESTS (session.insert().into()) ====================

    def "insert using fluent API with into(tableName) by string name"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insert()
                    .into("basics")
                    .set(basics.id, 1L)
                    .set(basics.name, "String Table Name")
                    .set(basics.numero, 100)
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.id) == 1L
            record.get(basics.name) == "String Table Name"
            record.get(basics.numero) == 100

        where:
            session << allSessions
    }

    def "insert using fluent API with into(QTableName)"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 2L)
                    .set(basics.name, "QTableName Insert")
                    .set(basics.numero, 200)
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.id) == 2L
            record.get(basics.name) == "QTableName Insert"
            record.get(basics.numero) == 200

        where:
            session << allSessions
    }

    def "insert using fluent API with RETURNING and fetchSingle"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            Record returned = session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 3L)
                    .set(basics.name, "Fluent Return Single")
                    .set(basics.numero, 300)
                    .returning(basics.id, basics.name, basics.numero)
                    .executeAndFetchSingle()

        then:
            returned.get(basics.id) == 3L
            returned.get(basics.name) == "Fluent Return Single"
            returned.get(basics.numero) == 300

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "insert using fluent API with RETURNING and fetchAll"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            List<Record> returned = session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 4L)
                    .set(basics.name, "Fluent Return All")
                    .set(basics.numero, 400)
                    .returning(basics.id, basics.name)
                    .executeAndFetchAll()

        then:
            returned.size() == 1
            with(returned[0]) { r ->
                r.get(basics.id) == 4L
                r.get(basics.name) == "Fluent Return All"
            }

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "insert using fluent API with RETURNING and fetchOne returns Optional"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            Optional<Record> returned = session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 5L)
                    .set(basics.name, "Fluent Return Optional")
                    .set(basics.numero, 500)
                    .returning(basics.id, basics.name)
                    .executeAndFetchOne()

        then:
            returned.isPresent()
            with(returned.get()) { r ->
                r.get(basics.id) == 5L
                r.get(basics.name) == "Fluent Return Optional"
            }

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    // ==================== EXPRESSION VALUE TESTS ====================

    def "insert with DSL.constant expression for column value"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, DSL.constant(Long.class, 10L))
                    .set(basics.name, DSL.constant(String.class, "Constant Expression"))
                    .set(basics.numero, DSL.constant(Integer.class, 1000))
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.id) == 10L
            record.get(basics.name) == "Constant Expression"
            record.get(basics.numero) == 1000

        where:
            session << allSessions
    }

    def "insert with mixed constant expressions and literal values"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 11L)
                    .set(basics.name, DSL.constant(String.class, "Mixed Values"))
                    .set(basics.numero, 1100)
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.id) == 11L
            record.get(basics.name) == "Mixed Values"
            record.get(basics.numero) == 1100

        where:
            session << allSessions
    }

    def "insert with DSL.currentDateTime() expression for timestamp column"() {
        given:
            AuditedEntitiesTable auditedEntities = new AuditedEntitiesTable(null)
            LocalDateTime beforeInsert = LocalDateTime.now().minusSeconds(1)

        when:
            List<Record> returned = session.insert()
                    .into(auditedEntities.getTableName())
                    .set(auditedEntities.name, "Now Test")
                    .set(auditedEntities.insertCounter, 1)
                    .set(auditedEntities.updateCounter, 0)
                    .returning(auditedEntities.id, auditedEntities.name)
                    .executeAndFetchAll()

        then:
            returned.size() == 1
            returned[0].get(auditedEntities.name) == "Now Test"

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "insert with computed expression using arithmetic"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 12L)
                    .set(basics.name, "Computed Value")
                    .set(basics.numero, DSL.constant(Integer.class, 50).add(DSL.constant(Integer.class, 50)))
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.id) == 12L
            record.get(basics.numero) == 100

        where:
            session << allSessions
    }

    // ==================== NULL VALUE TESTS ====================

    def "insert with null value for nullable column"() {
        given:
            AuditedEntitiesTable auditedEntities = new AuditedEntitiesTable(null)

        when:
            List<Record> returned = session.insert()
                    .into(auditedEntities.getTableName())
                    .set(auditedEntities.name, "Null Test")
                    .set(auditedEntities.createdAt, (LocalDateTime) null)
                    .set(auditedEntities.updatedAt, (LocalDateTime) null)
                    .set(auditedEntities.insertCounter, (Integer) null)
                    .set(auditedEntities.updateCounter, (Integer) null)
                    .returning(auditedEntities.id, auditedEntities.name, auditedEntities.createdAt)
                    .executeAndFetchAll()

        then:
            returned.size() == 1
            returned[0].get(auditedEntities.name) == "Null Test"
            returned[0].get(auditedEntities.createdAt) == null

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "insert with explicit null using DSL.constant"() {
        given:
            AuditedEntitiesTable auditedEntities = new AuditedEntitiesTable(null)

        when:
            List<Record> returned = session.insert()
                    .into(auditedEntities.getTableName())
                    .set(auditedEntities.name, "Constant Null Test")
                    .set(auditedEntities.createdAt, DSL.constant(LocalDateTime.class, null))
                    .set(auditedEntities.insertCounter, DSL.constant(Integer.class, null))
                    .returning(auditedEntities.id, auditedEntities.name, auditedEntities.insertCounter)
                    .executeAndFetchAll()

        then:
            returned.size() == 1
            returned[0].get(auditedEntities.name) == "Constant Null Test"
            returned[0].get(auditedEntities.insertCounter) == null

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    // ==================== MULTIPLE ROWS WITH FLUENT API ====================

    def "insert multiple rows using fluent API with nextRow()"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 20L)
                    .set(basics.name, "Row A")
                    .set(basics.numero, 2000)
                    .nextRow()
                    .set(basics.id, 21L)
                    .set(basics.name, "Row B")
                    .set(basics.numero, 2100)
                    .nextRow()
                    .set(basics.id, 22L)
                    .set(basics.name, "Row C")
                    .set(basics.numero, 2200)
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
                r.get(basics.id) == 20L
                r.get(basics.name) == "Row A"
            }
            with(records[1]) { r ->
                r.get(basics.id) == 21L
                r.get(basics.name) == "Row B"
            }
            with(records[2]) { r ->
                r.get(basics.id) == 22L
                r.get(basics.name) == "Row C"
            }

        where:
            session << allSessions
    }

    def "insert multiple rows with RETURNING clause using fluent API"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            List<Record> returned = session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 30L)
                    .set(basics.name, "Multi Return A")
                    .set(basics.numero, 3000)
                    .nextRow()
                    .set(basics.id, 31L)
                    .set(basics.name, "Multi Return B")
                    .set(basics.numero, 3100)
                    .returning(basics.id, basics.name)
                    .executeAndFetchAll()

        then:
            returned.size() == 2
            with(returned[0]) { r ->
                r.get(basics.id) == 30L
                r.get(basics.name) == "Multi Return A"
            }
            with(returned[1]) { r ->
                r.get(basics.id) == 31L
                r.get(basics.name) == "Multi Return B"
            }

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    // ==================== RETURNING CLAUSE VARIATIONS ====================

    def "insert with RETURNING single column"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            Record returned = session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 40L)
                    .set(basics.name, "Single Column Return")
                    .set(basics.numero, 4000)
                    .returning(basics.id)
                    .executeAndFetchSingle()

        then:
            returned.get(basics.id) == 40L
            returned.columnsCount() == 1

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "insert with RETURNING all columns using list"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            Record returned = session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 41L)
                    .set(basics.name, "All Columns Return")
                    .set(basics.numero, 4100)
                    .returning(basics.allColumns)
                    .executeAndFetchSingle()

        then:
            returned.get(basics.id) == 41L
            returned.get(basics.name) == "All Columns Return"
            returned.get(basics.numero) == 4100
            returned.columnsCount() == 3

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "insert with RETURNING expression (computed value)"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            Record returned = session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 42L)
                    .set(basics.name, "Expression Return")
                    .set(basics.numero, 4200)
                    .returning(basics.id, basics.numero.multiply(DSL.constant(Integer.class, 2)))
                    .executeAndFetchSingle()

        then:
            returned.get(basics.id) == 42L
            // The second column is the computed value
            returned.columnsCount() == 2

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    // ==================== AUTO-INCREMENT / SEQUENCE ID TESTS ====================

    def "insert with auto-increment ID and RETURNING generated ID"() {
        given:
            RolesTable roles = new RolesTable(null)

        when:
            Record returned = session.insert()
                    .into(roles.getTableName())
                    .set(roles.name, "Auto ID Role")
                    .returning(roles.id, roles.name)
                    .executeAndFetchSingle()

        then:
            returned.get(roles.id) != null
            returned.get(roles.id) > 0
            returned.get(roles.name) == "Auto ID Role"

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "insert multiple rows with auto-increment ID and verify generated IDs"() {
        given:
            RolesTable roles = new RolesTable(null)

        when:
            List<Record> returned = session.insert()
                    .into(roles.getTableName())
                    .set(roles.name, "Role One")
                    .nextRow()
                    .set(roles.name, "Role Two")
                    .nextRow()
                    .set(roles.name, "Role Three")
                    .returning(roles.id, roles.name)
                    .executeAndFetchAll()

        then:
            returned.size() == 3
            returned[0].get(roles.id) != null
            returned[1].get(roles.id) != null
            returned[2].get(roles.id) != null

            // IDs should be sequential or at least unique
            Set<Long> ids = returned.collect { it.get(roles.id) } as Set
            ids.size() == 3

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    // ==================== EDGE CASES ====================

    def "insert with zero value"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 0L)
                    .set(basics.name, "Zero ID Test")
                    .set(basics.numero, 0)
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.id) == 0L
            record.get(basics.numero) == 0

        where:
            session << allSessions
    }

    def "insert with maximum and minimum integer values"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 50L)
                    .set(basics.name, "Max Int")
                    .set(basics.numero, Integer.MAX_VALUE)
                    .nextRow()
                    .set(basics.id, 51L)
                    .set(basics.name, "Min Int")
                    .set(basics.numero, Integer.MIN_VALUE)
                    .execute()

        then:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchAll()
            records.size() == 2
            records[0].get(basics.numero) == Integer.MAX_VALUE
            records[1].get(basics.numero) == Integer.MIN_VALUE

        where:
            session << allSessions
    }

    def "insert with unicode characters in string"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 60L)
                    .set(basics.name, "Unicode: \u4e2d\u6587 \u0410\u0411\u0412 \ud83d\ude00")
                    .set(basics.numero, 6000)
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.name) == "Unicode: \u4e2d\u6587 \u0410\u0411\u0412 \ud83d\ude00"

        where:
            session << allSessions
    }

    def "insert with very long string"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            String longString = "A" * 100  // 100 characters, within varchar(100) limit

        when:
            session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 61L)
                    .set(basics.name, longString)
                    .set(basics.numero, 6100)
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.name) == longString
            record.get(basics.name).length() == 100

        where:
            session << allSessions
    }

    // ==================== CHAINING TESTS ====================

    def "verify set() returns SetStep for method chaining"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            def setStep = session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 70L)

            // Continue chaining
            setStep.set(basics.name, "Chain Test")
                    .set(basics.numero, 7000)
                    .execute()

        then:
            Record record = session.select(basics.allColumns)
                    .from(basics)
                    .fetchSingle()
            record.get(basics.id) == 70L
            record.get(basics.name) == "Chain Test"
            record.get(basics.numero) == 7000

        where:
            session << allSessions
    }

    def "verify returning() returns ExecuteFetchStep for method chaining"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            def executeFetchStep = session.insert()
                    .into(basics.getTableName())
                    .set(basics.id, 71L)
                    .set(basics.name, "Returning Chain")
                    .set(basics.numero, 7100)
                    .returning(basics.id, basics.name)

            Record result = executeFetchStep.executeAndFetchSingle()

        then:
            result.get(basics.id) == 71L
            result.get(basics.name) == "Returning Chain"

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }
}
