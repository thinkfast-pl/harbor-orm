// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.OffsetDateTimeTestTable
import io.github.thinkfastpl.harbororm.test.domain.OffsetDateTimeTestEntity
import io.github.thinkfastpl.harbororm.test.domain.QOffsetDateTimeTestEntity

import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Native OffsetDateTime mapping (TIMESTAMP WITH TIME ZONE columns, no type handler).
 *
 * MariaDB and MySQL have no time zone aware timestamp type and are covered by
 * {@link OffsetDateTimeTypeHandlerIT} through {@code OffsetDateTimeAsTimestampTypeHandler}.
 */
class OffsetDateTimeIT extends AbstractHarborIT {

    // microsecond precision only - nanoseconds do not survive TIMESTAMP(6) columns
    private static final OffsetDateTime EVENT_TIME =
            OffsetDateTime.of(2024, 6, 15, 10, 30, 45, 123_456_000, ZoneOffset.ofHours(5))

    def "should insert and read entity with OffsetDateTime preserving the instant"() {
        given:
            QOffsetDateTimeTestEntity q = new QOffsetDateTimeTestEntity(null)
            OffsetDateTime nullableEventTime = EVENT_TIME.plusDays(1).withOffsetSameInstant(ZoneOffset.ofHours(-7))

        when:
            session.insertEntity(q, new OffsetDateTimeTestEntity(1L, "test", EVENT_TIME, null))
            session.insertEntity(q, new OffsetDateTimeTestEntity(2L, "nullable-test", EVENT_TIME, nullableEventTime))

        then:
            with(session.selectEntity(q).orderBy(q.id.asc()).fetchAll()) { entities ->
                entities.size() == 2
                entities[0].eventTime.isEqual(EVENT_TIME)
                entities[0].nullableEventTime == null
                entities[1].eventTime.isEqual(EVENT_TIME)
                entities[1].nullableEventTime.isEqual(nullableEventTime)
            }

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "should filter by OffsetDateTime field in WHERE clause regardless of offset"() {
        given:
            QOffsetDateTimeTestEntity q = new QOffsetDateTimeTestEntity(null)
            session.insertEntity(q, new OffsetDateTimeTestEntity(10L, "a", EVENT_TIME.minusHours(1), null))
            session.insertEntity(q, new OffsetDateTimeTestEntity(11L, "b", EVENT_TIME, null))
            session.insertEntity(q, new OffsetDateTimeTestEntity(12L, "c", EVENT_TIME.plusHours(1), null))

        when: "filtering with the same instant expressed at a different offset"
            List<OffsetDateTimeTestEntity> results = session.selectEntity(q)
                    .where(q.eventTime.eq(EVENT_TIME.withOffsetSameInstant(ZoneOffset.ofHours(-3))))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].name == "b"

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "should read OffsetDateTime inside multiset aggregation"() {
        given:
            QOffsetDateTimeTestEntity q = new QOffsetDateTimeTestEntity(null)
            session.insertEntity(q, new OffsetDateTimeTestEntity(20L, "G1", EVENT_TIME, null))
            session.insertEntity(q, new OffsetDateTimeTestEntity(21L, "G1", EVENT_TIME.plusHours(1), EVENT_TIME.minusDays(1)))
            OffsetDateTimeTestTable t = new OffsetDateTimeTestTable(null)
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
                r.get(t.eventTime).isEqual(EVENT_TIME)
                r.get(t.nullableEventTime) == null
            }
            with(group.find { it.get(t.id) == 21L }) { r ->
                r.get(t.eventTime).isEqual(EVENT_TIME.plusHours(1))
                r.get(t.nullableEventTime).isEqual(EVENT_TIME.minusDays(1))
            }

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }
}
