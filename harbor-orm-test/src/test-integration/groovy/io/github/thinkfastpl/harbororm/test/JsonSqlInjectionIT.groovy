// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.test.domain.JsonTestEntity
import io.github.thinkfastpl.harbororm.test.domain.QJsonTestEntity

/**
 * Tests that JSON key/path values containing single quotes are properly escaped,
 * preventing SQL injection (audit finding C-4).
 */
class JsonSqlInjectionIT extends AbstractHarborIT {

    // --- extractText (->> operator) with single quote in key ---

    def "extractText should handle key containing single quote"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(300L, '{"it\'s":"works","normal":"data"}', null, null))

        when:
            List results = session.select(qEntity.data.extractText("it's"))
                    .from(qEntity.getTableName())
                    .where(qEntity.id.eq(300L))
                    .fetchAll()

        then:
            results.size() == 1
            results[0] == "works"

        where:
            session << getSessionsExcept(DbType.H2)
    }

    // --- extract (-> operator) with single quote in key ---

    def "extract should handle key containing single quote"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(301L, '{"it\'s":{"nested":"value"}}', null, null))

        when:
            List results = session.select(qEntity.data.extract("it's").extractText("nested"))
                    .from(qEntity.getTableName())
                    .where(qEntity.id.eq(301L))
                    .fetchAll()

        then:
            results.size() == 1
            results[0] == "value"

        where:
            session << getSessionsExcept(DbType.H2)
    }

    // --- extractPath (#> operator) with single quote in path element ---

    def "extractPath should handle path element containing single quote"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(311L, '{"it\'s":{"key":"deep"}}', null, null))

        when:
            List results = session.select(qEntity.data.extractPath("it's").extractText("key"))
                    .from(qEntity.getTableName())
                    .where(qEntity.id.eq(311L))
                    .fetchAll()

        then:
            results.size() == 1
            results[0] == "deep"

        where:
            session << getSessionsExcept(DbType.H2)
    }

    // --- extractPathText (#>> operator) with single quote in path element ---

    def "extractPathText should handle path element containing single quote"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(302L, '{"it\'s":{"key":"deep"}}', null, null))

        when:
            List results = session.select(qEntity.data.extractPathText("it's", "key"))
                    .from(qEntity.getTableName())
                    .where(qEntity.id.eq(302L))
                    .fetchAll()

        then:
            results.size() == 1
            results[0] == "deep"

        where:
            session << getSessionsExcept(DbType.H2)
    }

    // --- hasKey (? operator) with single quote in key ---

    def "hasKey should handle key containing single quote"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(303L, '{"it\'s":1,"normal":2}', null, null))
            session.insertEntity(qEntity, new JsonTestEntity(304L, '{"other":3}', null, null))

        when:
            List<JsonTestEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.data.hasKey("it's"))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].id == 303L

        where:
            session << getSessionsExcept(DbType.H2)
    }

    // --- hasAnyKey (?| operator) with single quote in keys ---

    def "hasAnyKey should handle keys containing single quotes"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(305L, '{"it\'s":1,"normal":2}', null, null))
            session.insertEntity(qEntity, new JsonTestEntity(306L, '{"other":3}', null, null))

        when:
            List<JsonTestEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.data.hasAnyKey("it's", "missing"))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].id == 305L

        where:
            session << getSessionsExcept(DbType.H2)
    }

    // --- hasAllKeys (?& operator) with single quote in keys ---

    def "hasAllKeys should handle keys containing single quotes"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(307L, '{"it\'s":1,"that\'s":2}', null, null))
            session.insertEntity(qEntity, new JsonTestEntity(308L, '{"it\'s":1}', null, null))

        when:
            List<JsonTestEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.data.hasAllKeys("it's", "that's"))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].id == 307L

        where:
            session << getSessionsExcept(DbType.H2)
    }

    // --- SQL injection attempt ---

    def "should safely handle SQL injection attempt in extractText key"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(309L, '{"normal":"data"}', null, null))

        when:
            List results = session.select(qEntity.data.extractText("'; DELETE FROM json_test_entities WHERE 1=1; --"))
                    .from(qEntity.getTableName())
                    .where(qEntity.id.eq(309L))
                    .fetchAll()

        then: "query executes safely - injection payload treated as literal key name"
            results.size() == 1
            results[0] == null

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "should safely handle SQL injection attempt in hasKey"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(310L, '{"normal":"data"}', null, null))

        when:
            List<JsonTestEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.data.hasKey("'; DELETE FROM json_test_entities WHERE 1=1; --"))
                    .fetchAll()

        then: "query executes safely - no rows match the bogus key"
            results.size() == 0

        where:
            session << getSessionsExcept(DbType.H2)
    }
}
