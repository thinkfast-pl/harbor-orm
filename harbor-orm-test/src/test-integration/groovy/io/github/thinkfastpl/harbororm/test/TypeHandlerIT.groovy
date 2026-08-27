// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.QTypeHandlerDialectTestEntity
import io.github.thinkfastpl.harbororm.test.domain.QTypeHandlerTestEntity
import io.github.thinkfastpl.harbororm.test.domain.TypeHandlerDialectTestEntity
import io.github.thinkfastpl.harbororm.test.domain.TypeHandlerTestEntity

import java.time.Duration

class TypeHandlerIT extends AbstractHarborIT {

    def "should insert and read entity with @TypeHandler"() {
        given:
            QTypeHandlerTestEntity q = new QTypeHandlerTestEntity(null)
            Duration duration = Duration.ofHours(2).plusMinutes(30)
            TypeHandlerTestEntity entity = new TypeHandlerTestEntity(1L, "test", duration, null)

        when:
            session.insertEntity(q, entity)

        then:
            with(session.selectEntity(q).fetchAll()) { entities ->
                entities.size() == 1
                with(entities[0]) { e ->
                    e.id == 1L
                    e.name == "test"
                    e.duration == duration
                    e.nullableDuration == null
                }
            }

        where:
            session << allSessions
    }

    def "should insert and read entity with nullable @TypeHandler field"() {
        given:
            QTypeHandlerTestEntity q = new QTypeHandlerTestEntity(null)
            Duration duration = Duration.ofMinutes(45)
            Duration nullableDuration = Duration.ofSeconds(123)
            TypeHandlerTestEntity entity = new TypeHandlerTestEntity(2L, "nullable-test", duration, nullableDuration)

        when:
            session.insertEntity(q, entity)

        then:
            with(session.selectEntity(q).fetchAll()) { entities ->
                entities.size() == 1
                with(entities[0]) { e ->
                    e.id == 2L
                    e.nullableDuration == nullableDuration
                }
            }

        where:
            session << allSessions
    }

    def "should update entity with @TypeHandler field"() {
        given:
            QTypeHandlerTestEntity q = new QTypeHandlerTestEntity(null)
            TypeHandlerTestEntity entity = new TypeHandlerTestEntity(3L, "update-test", Duration.ofMinutes(10), null)
            session.insertEntity(q, entity)

        when:
            entity.setDuration(Duration.ofMinutes(20))
            session.updateEntity(q, entity)

        then:
            with(session.selectEntity(q).fetchAll()) { entities ->
                entities.size() == 1
                entities[0].duration == Duration.ofMinutes(20)
            }

        where:
            session << allSessions
    }

    def "should filter by @TypeHandler field in WHERE clause"() {
        given:
            QTypeHandlerTestEntity q = new QTypeHandlerTestEntity(null)
            session.insertEntity(q, new TypeHandlerTestEntity(10L, "a", Duration.ofMinutes(10), null))
            session.insertEntity(q, new TypeHandlerTestEntity(11L, "b", Duration.ofMinutes(20), null))
            session.insertEntity(q, new TypeHandlerTestEntity(12L, "c", Duration.ofMinutes(30), null))

        when:
            List<TypeHandlerTestEntity> results = session.selectEntity(q)
                    .where(q.duration.eq(Duration.ofMinutes(20)))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].name == "b"

        where:
            session << allSessions
    }

    def "should use dialect-specific handler on PostgreSQL and fallback on H2"() {
        given:
            QTypeHandlerDialectTestEntity q = new QTypeHandlerDialectTestEntity(null)
            Duration duration = Duration.ofSeconds(120)
            TypeHandlerDialectTestEntity entity = new TypeHandlerDialectTestEntity(1L, "dialect-test", duration)

        when:
            session.insertEntity(q, entity)

        then: "roundtrip preserves the duration on both databases"
            with(session.selectEntity(q).fetchAll()) { entities ->
                entities.size() == 1
                entities[0].duration == duration
            }

        where:
            session << allSessions
    }

    def "should delete entity with @TypeHandler field"() {
        given:
            QTypeHandlerTestEntity q = new QTypeHandlerTestEntity(null)
            TypeHandlerTestEntity entity = new TypeHandlerTestEntity(20L, "delete-test", Duration.ofMinutes(5), null)
            session.insertEntity(q, entity)

        when:
            session.deleteEntity(q, entity)

        then:
            session.selectEntity(q).fetchAll().isEmpty()

        where:
            session << allSessions
    }
}
