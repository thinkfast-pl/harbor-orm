// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.JsonTestEntity
import io.github.thinkfastpl.harbororm.test.domain.ProfilePojo
import io.github.thinkfastpl.harbororm.test.domain.QJsonTestEntity

/**
 * Cross-database integration tests for @Json annotation and JSON query support.
 * Tests CRUD operations and JSON extraction operators on both H2 and PostgreSQL.
 */
class JsonIT extends AbstractHarborIT {

    // --- CRUD with @Json String field (no JsonSerializer needed) ---

    def "should insert and select entity with @Json String field"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            JsonTestEntity entity = new JsonTestEntity(1L, '{"key":"value","num":42}', null, null)

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { loaded ->
                loaded.id == 1L
                loaded.data != null
                loaded.data.contains('"key"')
                loaded.data.contains('"value"')
            }

        where:
            session << allSessions
    }

    def "should update @Json String field"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(2L, '{"original":true}', null, null))

        when:
            JsonTestEntity loaded = session.selectEntity(qEntity).where(qEntity.id.eq(2L)).fetchSingle()
            loaded.setData('{"updated":true}')
            session.updateEntity(qEntity, loaded)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(2L)).fetchSingle()) { updated ->
                updated.data.contains('"updated"')
            }

        where:
            session << allSessions
    }

    def "should delete entity with @Json fields"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(3L, '{"delete":"me"}', null, null))

        expect:
            session.selectEntity(qEntity).where(qEntity.id.eq(3L)).count() == 1

        when:
            session.deleteEntityById(qEntity, 3L)

        then:
            session.selectEntity(qEntity).where(qEntity.id.eq(3L)).count() == 0

        where:
            session << allSessions
    }

    def "should handle null @Json String field"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(4L, null, null, null))

        when:
            JsonTestEntity loaded = session.selectEntity(qEntity).where(qEntity.id.eq(4L)).fetchSingle()

        then:
            loaded.id == 4L
            loaded.data == null

        where:
            session << allSessions
    }

    // --- CRUD with @Json POJO field (requires JsonSerializer) ---

    def "should insert and select entity with @Json POJO field using JsonSerializer"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            ProfilePojo profile = new ProfilePojo("Alice", 30, "Berlin")
            JsonTestEntity entity = new JsonTestEntity(10L, null, profile, null)

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(10L)).fetchSingle()) { loaded ->
                loaded.id == 10L
                loaded.profile != null
                loaded.profile.name == "Alice"
                loaded.profile.age == 30
                loaded.profile.city == "Berlin"
            }

        where:
            session << allJsonSessions
    }

    def "should insert and select entity with @Json @Convert POJO field"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            ProfilePojo config = new ProfilePojo("Bob", 25, "Munich")
            JsonTestEntity entity = new JsonTestEntity(11L, null, null, config)

        when:
            // @Convert works with regular sessions (no JsonSerializer needed)
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(11L)).fetchSingle()) { loaded ->
                loaded.id == 11L
                loaded.config != null
                loaded.config.name == "Bob"
                loaded.config.age == 25
                loaded.config.city == "Munich"
            }

        where:
            session << allSessions
    }

    def "should handle null @Json POJO field"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            JsonTestEntity entity = new JsonTestEntity(12L, null, null, null)

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(12L)).fetchSingle()) { loaded ->
                loaded.id == 12L
                loaded.profile == null
                loaded.config == null
            }

        where:
            session << allJsonSessions
    }

    def "should update @Json POJO field"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(13L, null, new ProfilePojo("Old", 20, "London"), null))

        when:
            JsonTestEntity loaded = session.selectEntity(qEntity).where(qEntity.id.eq(13L)).fetchSingle()
            loaded.setProfile(new ProfilePojo("New", 35, "Paris"))
            session.updateEntity(qEntity, loaded)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(13L)).fetchSingle()) { updated ->
                updated.profile.name == "New"
                updated.profile.age == 35
                updated.profile.city == "Paris"
            }

        where:
            session << allJsonSessions
    }

    // --- JSON extraction operators (PostgreSQL only — H2 lacks JSON_VALUE/JSON_QUERY) ---

    def "should use extractText() to get a JSON field as text"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(20L, '{"name":"Charlie","age":28}', null, null))

        when:
            List results = session.select(qEntity.data.extractText("name"))
                    .from(qEntity.getTableName())
                    .where(qEntity.id.eq(20L))
                    .fetchAll()

        then:
            results.size() == 1
            results[0] == "Charlie"

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "should use extractText() in WHERE clause"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(21L, '{"status":"active","name":"D1"}', null, null))
            session.insertEntity(qEntity, new JsonTestEntity(22L, '{"status":"inactive","name":"D2"}', null, null))

        when:
            List<JsonTestEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.data.extractText("status").eq("active"))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].id == 21L

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "should use extract() chained with extractText()"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(23L, '{"nested":{"inner":"deep"}}', null, null))

        when:
            List results = session.select(qEntity.data.extract("nested").extractText("inner"))
                    .from(qEntity.getTableName())
                    .where(qEntity.id.eq(23L))
                    .fetchAll()

        then:
            results.size() == 1
            results[0] == "deep"

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "should use hasKey() condition"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(24L, '{"key1":"a","key2":"b"}', null, null))
            session.insertEntity(qEntity, new JsonTestEntity(25L, '{"key1":"c"}', null, null))

        when:
            List<JsonTestEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.data.hasKey("key2"))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].id == 24L

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "should use extractText() with a key containing special characters"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(30L, '{"full name":"Eve","stats":{"win.rate":0.75}}', null, null))

        when:
            List firstName = session.select(qEntity.data.extractText("full name"))
                    .from(qEntity.getTableName())
                    .where(qEntity.id.eq(30L))
                    .fetchAll()
            List winRate = session.select(qEntity.data.extract("stats").extractText("win.rate"))
                    .from(qEntity.getTableName())
                    .where(qEntity.id.eq(30L))
                    .fetchAll()

        then:
            firstName.size() == 1
            firstName[0] == "Eve"
            winRate.size() == 1
            winRate[0] == "0.75"

        where:
            session << getSessionsExcept(DbType.H2)
    }
}
