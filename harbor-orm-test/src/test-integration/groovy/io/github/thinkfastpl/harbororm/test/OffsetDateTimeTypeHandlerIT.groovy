// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.OffsetDateTimeHandlerTestTable
import io.github.thinkfastpl.harbororm.test.domain.OffsetDateTimeTypeHandlerTestEntity
import io.github.thinkfastpl.harbororm.test.domain.QOffsetDateTimeTypeHandlerTestEntity

import java.time.OffsetDateTime
import java.time.ZoneOffset

class OffsetDateTimeTypeHandlerIT extends AbstractHarborIT {

    // microsecond precision only - nanoseconds do not survive TIMESTAMP(6)/DATETIME(6) columns
    private static final OffsetDateTime EVENT_TIME =
            OffsetDateTime.of(2024, 6, 15, 10, 30, 45, 123_456_000, ZoneOffset.ofHours(5))

    def "should insert and read entity with OffsetDateTime normalized to UTC"() {
        given:
            QOffsetDateTimeTypeHandlerTestEntity q = new QOffsetDateTimeTypeHandlerTestEntity(null)
            OffsetDateTimeTypeHandlerTestEntity entity = new OffsetDateTimeTypeHandlerTestEntity(1L, "test", EVENT_TIME, null)

        when:
            session.insertEntity(q, entity)

        then:
            with(session.selectEntity(q).fetchAll()) { entities ->
                entities.size() == 1
                with(entities[0]) { e ->
                    e.id == 1L
                    e.name == "test"
                    e.eventTime.offset == ZoneOffset.UTC
                    e.eventTime == EVENT_TIME.withOffsetSameInstant(ZoneOffset.UTC)
                    e.eventTime.isEqual(EVENT_TIME)
                    e.nullableEventTime == null
                }
            }

        where:
            session << allSessions
    }

    def "should insert and read entity with nullable OffsetDateTime field"() {
        given:
            QOffsetDateTimeTypeHandlerTestEntity q = new QOffsetDateTimeTypeHandlerTestEntity(null)
            OffsetDateTime nullableEventTime = EVENT_TIME.plusDays(1).withOffsetSameInstant(ZoneOffset.ofHours(-7))
            OffsetDateTimeTypeHandlerTestEntity entity =
                    new OffsetDateTimeTypeHandlerTestEntity(2L, "nullable-test", EVENT_TIME, nullableEventTime)

        when:
            session.insertEntity(q, entity)

        then:
            with(session.selectEntity(q).fetchAll()) { entities ->
                entities.size() == 1
                with(entities[0]) { e ->
                    e.id == 2L
                    e.nullableEventTime == nullableEventTime.withOffsetSameInstant(ZoneOffset.UTC)
                }
            }

        where:
            session << allSessions
    }

    def "should filter by OffsetDateTime field in WHERE clause regardless of offset"() {
        given:
            QOffsetDateTimeTypeHandlerTestEntity q = new QOffsetDateTimeTypeHandlerTestEntity(null)
            session.insertEntity(q, new OffsetDateTimeTypeHandlerTestEntity(10L, "a", EVENT_TIME.minusHours(1), null))
            session.insertEntity(q, new OffsetDateTimeTypeHandlerTestEntity(11L, "b", EVENT_TIME, null))
            session.insertEntity(q, new OffsetDateTimeTypeHandlerTestEntity(12L, "c", EVENT_TIME.plusHours(1), null))

        when: "filtering with the same instant expressed at a different offset"
            List<OffsetDateTimeTypeHandlerTestEntity> results = session.selectEntity(q)
                    .where(q.eventTime.eq(EVENT_TIME.withOffsetSameInstant(ZoneOffset.ofHours(-3))))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].name == "b"

        where:
            session << allSessions
    }

    def "should update entity with OffsetDateTime field"() {
        given:
            QOffsetDateTimeTypeHandlerTestEntity q = new QOffsetDateTimeTypeHandlerTestEntity(null)
            OffsetDateTimeTypeHandlerTestEntity entity = new OffsetDateTimeTypeHandlerTestEntity(3L, "update-test", EVENT_TIME, null)
            session.insertEntity(q, entity)
            OffsetDateTime updatedTime = EVENT_TIME.plusDays(3).withOffsetSameInstant(ZoneOffset.ofHours(9))

        when:
            entity.setEventTime(updatedTime)
            session.updateEntity(q, entity)

        then:
            with(session.selectEntity(q).fetchAll()) { entities ->
                entities.size() == 1
                entities[0].eventTime == updatedTime.withOffsetSameInstant(ZoneOffset.UTC)
            }

        where:
            session << allSessions
    }

    def "should read OffsetDateTime inside multiset aggregation"() {
        given:
            QOffsetDateTimeTypeHandlerTestEntity q = new QOffsetDateTimeTypeHandlerTestEntity(null)
            session.insertEntity(q, new OffsetDateTimeTypeHandlerTestEntity(20L, "G1", EVENT_TIME, null))
            session.insertEntity(q, new OffsetDateTimeTypeHandlerTestEntity(21L, "G1", EVENT_TIME.plusHours(1), EVENT_TIME.minusDays(1)))
            OffsetDateTimeHandlerTestTable t = new OffsetDateTimeHandlerTestTable(null)
            Expression<Record[]> multisetAgg = DSL.multisetAgg(List.of(t.id, t.eventTime, t.nullableEventTime))

        when:
            List<Record> records = session.select(t.name, multisetAgg)
                    .from(t)
                    .groupBy(t.name)
                    .fetchAll()

        then:
            records.size() == 1
            Record[] group = records[0].get(multisetAgg)
            group.length == 2
            with(group.find { it.get(t.id) == 20L }) { r ->
                r.get(t.eventTime) == EVENT_TIME.withOffsetSameInstant(ZoneOffset.UTC)
                r.get(t.nullableEventTime) == null
            }
            with(group.find { it.get(t.id) == 21L }) { r ->
                r.get(t.eventTime) == EVENT_TIME.plusHours(1).withOffsetSameInstant(ZoneOffset.UTC)
                r.get(t.nullableEventTime) == EVENT_TIME.minusDays(1).withOffsetSameInstant(ZoneOffset.UTC)
            }

        where:
            session << allSessions
    }
}
