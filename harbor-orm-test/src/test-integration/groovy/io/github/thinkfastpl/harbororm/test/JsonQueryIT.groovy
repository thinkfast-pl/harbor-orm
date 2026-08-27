// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.test.domain.JsonTestEntity
import io.github.thinkfastpl.harbororm.test.domain.QJsonTestEntity

/**
 * Cross-database integration tests for advanced JSON operators on PostgreSQL and MariaDB.
 * Tests containment, multi-key existence, and JSON path extraction; H2 is verified to throw.
 */
class JsonQueryIT extends AbstractHarborIT {

    // --- Containment operators (PostgreSQL only) ---

    def "should use contains() to check JSONB containment"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(100L, '{"a":1,"b":2,"c":3}', null, null))
            session.insertEntity(qEntity, new JsonTestEntity(101L, '{"a":1}', null, null))

        when:
            List<JsonTestEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.data.contains(DSL.json('{"a":1,"b":2}')))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].id == 100L

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "should use containedIn() to check reverse JSONB containment"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(102L, '{"a":1}', null, null))
            session.insertEntity(qEntity, new JsonTestEntity(103L, '{"a":1,"b":2,"c":3}', null, null))

        when:
            List<JsonTestEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.data.containedIn(DSL.json('{"a":1,"b":2}')))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].id == 102L

        where:
            session << getSessionsExcept(DbType.H2)
    }

    // --- Multi-key existence ---

    def "should use hasAnyKey() to check if any key exists"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(104L, '{"x":1,"y":2}', null, null))
            session.insertEntity(qEntity, new JsonTestEntity(105L, '{"z":3}', null, null))

        when:
            List<JsonTestEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.data.hasAnyKey("x", "w"))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].id == 104L

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "should use hasAllKeys() to check if all keys exist"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(106L, '{"a":1,"b":2,"c":3}', null, null))
            session.insertEntity(qEntity, new JsonTestEntity(107L, '{"a":1,"b":2}', null, null))

        when:
            List<JsonTestEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.data.hasAllKeys("a", "b", "c"))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].id == 106L

        where:
            session << getSessionsExcept(DbType.H2)
    }

    // --- Path extraction ---

    def "should use extractPathText() for nested JSON access"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(108L, '{"user":{"address":{"city":"Berlin"}}}', null, null))

        when:
            List results = session.select(qEntity.data.extractPathText("user", "address", "city"))
                    .from(qEntity.getTableName())
                    .where(qEntity.id.eq(108L))
                    .fetchAll()

        then:
            results.size() == 1
            results[0] == "Berlin"

        where:
            session << getSessionsExcept(DbType.H2)
    }

    // --- H2 unsupported operation tests ---

    def "should throw UnsupportedOperationException for extractText() on H2"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(196L, '{"a":1}', null, null))

        when:
            session.selectEntity(qEntity)
                    .where(qEntity.data.extractText("a").eq("1"))
                    .fetchAll()

        then:
            thrown(UnsupportedOperationException)

        where:
            session << getSessionsExcept(DbType.POSTGRES, DbType.MARIADB, DbType.MYSQL)
    }

    def "should throw UnsupportedOperationException for extract() on H2"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(197L, '{"a":{"b":1}}', null, null))

        when:
            session.select(qEntity.data.extract("a"))
                    .from(qEntity.getTableName())
                    .where(qEntity.id.eq(197L))
                    .fetchAll()

        then:
            thrown(UnsupportedOperationException)

        where:
            session << getSessionsExcept(DbType.POSTGRES, DbType.MARIADB, DbType.MYSQL)
    }

    def "should throw UnsupportedOperationException for hasKey() on H2"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(198L, '{"a":1}', null, null))

        when:
            session.selectEntity(qEntity)
                    .where(qEntity.data.hasKey("a"))
                    .fetchAll()

        then:
            thrown(UnsupportedOperationException)

        where:
            session << getSessionsExcept(DbType.POSTGRES, DbType.MARIADB, DbType.MYSQL)
    }

    def "should throw UnsupportedOperationException for contains() on H2"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(200L, '{"a":1}', null, null))

        when:
            session.selectEntity(qEntity)
                    .where(qEntity.data.contains(DSL.json('{"a":1}')))
                    .fetchAll()

        then:
            thrown(UnsupportedOperationException)

        where:
            session << getSessionsExcept(DbType.POSTGRES, DbType.MARIADB, DbType.MYSQL)
    }

    def "should throw UnsupportedOperationException for containedIn() on H2"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(201L, '{"a":1}', null, null))

        when:
            session.selectEntity(qEntity)
                    .where(qEntity.data.containedIn(DSL.json('{"a":1}')))
                    .fetchAll()

        then:
            thrown(UnsupportedOperationException)

        where:
            session << getSessionsExcept(DbType.POSTGRES, DbType.MARIADB, DbType.MYSQL)
    }

    def "should throw UnsupportedOperationException for hasAnyKey() on H2"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(202L, '{"a":1}', null, null))

        when:
            session.selectEntity(qEntity)
                    .where(qEntity.data.hasAnyKey("a", "b"))
                    .fetchAll()

        then:
            thrown(UnsupportedOperationException)

        where:
            session << getSessionsExcept(DbType.POSTGRES, DbType.MARIADB, DbType.MYSQL)
    }

    def "should throw UnsupportedOperationException for hasAllKeys() on H2"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(203L, '{"a":1}', null, null))

        when:
            session.selectEntity(qEntity)
                    .where(qEntity.data.hasAllKeys("a", "b"))
                    .fetchAll()

        then:
            thrown(UnsupportedOperationException)

        where:
            session << getSessionsExcept(DbType.POSTGRES, DbType.MARIADB, DbType.MYSQL)
    }

    // --- Array length (PostgreSQL + MariaDB only) ---

    def "should use arrayLength() to count JSON array elements"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(300L, '[1,2,3,4,5]', null, null))

        when:
            List results = session.select(qEntity.data.arrayLength())
                    .from(qEntity.getTableName())
                    .where(qEntity.id.eq(300L))
                    .fetchAll()

        then:
            results.size() == 1
            results[0] == 5

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "should use DSL.jsonArrayLength() with a column reference"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(301L, '["a","b","c"]', null, null))

        when:
            List results = session.select(DSL.jsonArrayLength(qEntity.data))
                    .from(qEntity.getTableName())
                    .where(qEntity.id.eq(301L))
                    .fetchAll()

        then:
            results.size() == 1
            results[0] == 3

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "should use arrayLength() in WHERE clause"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(302L, '[1,2]', null, null))
            session.insertEntity(qEntity, new JsonTestEntity(303L, '[1,2,3,4]', null, null))

        when:
            List<JsonTestEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.data.arrayLength().gt(2))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].id == 303L

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "should use arrayLength() to count JSON array elements on H2"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(304L, '[1,2,3]', null, null))

        when:
            List results = session.select(qEntity.data.arrayLength())
                    .from(qEntity.getTableName())
                    .where(qEntity.id.eq(304L))
                    .fetchAll()

        then:
            results.size() == 1
            results[0] == 3

        where:
            session << getSessionsExcept(DbType.POSTGRES, DbType.MARIADB, DbType.MYSQL)
    }
}
