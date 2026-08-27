// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.*
import io.github.thinkfastpl.harbororm.test.domain.*
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

import java.time.Duration

import static io.github.thinkfastpl.harbororm.test.domain.EnumeratedTestEntity.Priority
import static io.github.thinkfastpl.harbororm.test.domain.EnumeratedTestEntity.Status

class MultisetAggIT extends AbstractHarborIT {

    def "multiset aggregation returns correct groups"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "A", 11)
            testFixtures.addBasic(2L, "A", 22)
            testFixtures.addBasic(3L, "B", 33)
            testFixtures.addBasic(4L, "B", 44)
            testFixtures.addBasic(5L, "B", 55)

            BasicsTable basics = new BasicsTable(null)

        when:
            Expression<Record[]> multisetAgg = DSL.multisetAgg(List.of(basics.numero, basics.id))
            List<Record> records = session.select(basics.name, multisetAgg)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { record ->
                record.get(basics.name) == "A"
                record.get(multisetAgg).length == 2
            }
            with(records[1]) { record ->
                record.get(basics.name) == "B"
                record.get(multisetAgg).length == 3
            }

        where:
            session << allSessions
    }

    def "multiset aggregation inner records accessible by expression"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "A", 11)
            testFixtures.addBasic(2L, "A", 22)
            testFixtures.addBasic(3L, "B", 33)

            BasicsTable basics = new BasicsTable(null)

        when:
            Expression<Record[]> multisetAgg = DSL.multisetAgg(List.of(basics.numero, basics.id))
            List<Record> records = session.select(basics.name, multisetAgg)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2

            Record[] groupA = records[0].get(multisetAgg)
            groupA.length == 2
            groupA[0].get(basics.numero) == 11
            groupA[0].get(basics.id) == 1L
            groupA[1].get(basics.numero) == 22
            groupA[1].get(basics.id) == 2L

            Record[] groupB = records[1].get(multisetAgg)
            groupB.length == 1
            groupB[0].get(basics.numero) == 33
            groupB[0].get(basics.id) == 3L

        where:
            session << allSessions
    }

    def "multiset aggregation inner records accessible by alias"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "A", 11)
            testFixtures.addBasic(2L, "A", 22)
            testFixtures.addBasic(3L, "B", 33)

            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> aliasedNumero = basics.numero.as("my_numero")
            Expression<Long> aliasedId = basics.id.as("my_id")

        when:
            Expression<Record[]> multisetAgg = DSL.multisetAgg(List.of(aliasedNumero, aliasedId))
            List<Record> records = session.select(basics.name, multisetAgg)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2

            Record[] groupA = records[0].get(multisetAgg)
            groupA.length == 2
            groupA[0].get("my_numero", Integer.class) == 11
            groupA[0].get("my_id", Long.class) == 1L
            groupA[1].get("my_numero", Integer.class) == 22
            groupA[1].get("my_id", Long.class) == 2L

            Record[] groupB = records[1].get(multisetAgg)
            groupB.length == 1
            groupB[0].get("my_numero", Integer.class) == 33
            groupB[0].get("my_id", Long.class) == 3L

        where:
            session << allSessions
    }

    def "parent record label access not corrupted by multiset"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "A", 11)
            testFixtures.addBasic(2L, "A", 22)

            BasicsTable basics = new BasicsTable(null)

        when:
            Expression<Record[]> multisetAgg = DSL.multisetAgg(List.of(basics.numero, basics.id))
            List<Record> records = session.select(basics.name, multisetAgg)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 1
            records[0].get("name", String.class) == "A"
            records[0].get(basics.name) == "A"

        where:
            session << allSessions
    }

    def "multiset aggregation converts enumerated columns"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            session.insertEntity(qEntity, new EnumeratedTestEntity(1L, "G1", Status.ACTIVE, Priority.LOW, null, null))
            session.insertEntity(qEntity, new EnumeratedTestEntity(2L, "G1", Status.INACTIVE, Priority.CRITICAL, null, null))

            EnumeratedTestTable enumerated = new EnumeratedTestTable(null)

        when:
            Expression<Record[]> multisetAgg = DSL.multisetAgg(List.of(enumerated.status, enumerated.priority, enumerated.secondaryStatus))
            List<Record> records = session.select(enumerated.name, multisetAgg)
                    .from(enumerated)
                    .groupBy(enumerated.name)
                    .fetchAll()

        then:
            records.size() == 1
            Record[] group = records[0].get(multisetAgg)
            group.length == 2
            group[0].get(enumerated.status) == Status.ACTIVE
            group[0].get(enumerated.priority) == Priority.LOW
            group[0].get(enumerated.secondaryStatus) == null
            group[1].get(enumerated.status) == Status.INACTIVE
            group[1].get(enumerated.priority) == Priority.CRITICAL

        where:
            session << allSessions
    }

    def "multiset aggregation converts columns with attribute converters"() {
        given:
            QConvertedFieldsEntity qEntity = new QConvertedFieldsEntity(null)
            session.insertEntity(qEntity, new ConvertedFieldsEntity(1L, "G1", true, false, new JsonMetadata("k1", "v1", 1)))
            session.insertEntity(qEntity, new ConvertedFieldsEntity(2L, "G1", false, true, new JsonMetadata("k2", "v2", 2)))

            ConvertedFieldsTable converted = new ConvertedFieldsTable(null)

        when:
            Expression<Record[]> multisetAgg = DSL.multisetAgg(List.of(converted.active, converted.metadata))
            List<Record> records = session.select(converted.name, multisetAgg)
                    .from(converted)
                    .groupBy(converted.name)
                    .fetchAll()

        then:
            records.size() == 1
            Record[] group = records[0].get(multisetAgg)
            group.length == 2
            group[0].get(converted.active) == Boolean.TRUE
            group[0].get(converted.metadata) == new JsonMetadata("k1", "v1", 1)
            group[1].get(converted.active) == Boolean.FALSE
            group[1].get(converted.metadata) == new JsonMetadata("k2", "v2", 2)

        where:
            session << allSessions
    }

    def "multiset aggregation converts type handler columns"() {
        given:
            QTypeHandlerTestEntity qEntity = new QTypeHandlerTestEntity(null)
            session.insertEntity(qEntity, new TypeHandlerTestEntity(1L, "G1", Duration.ofMillis(1500), null))
            session.insertEntity(qEntity, new TypeHandlerTestEntity(2L, "G1", Duration.ofSeconds(90), Duration.ofMillis(250)))

            TypeHandlerTestTable typeHandlers = new TypeHandlerTestTable(null)

        when:
            Expression<Record[]> multisetAgg = DSL.multisetAgg(List.of(typeHandlers.duration, typeHandlers.nullableDuration))
            List<Record> records = session.select(typeHandlers.name, multisetAgg)
                    .from(typeHandlers)
                    .groupBy(typeHandlers.name)
                    .fetchAll()

        then:
            records.size() == 1
            Record[] group = records[0].get(multisetAgg)
            group.length == 2
            group[0].get(typeHandlers.duration) == Duration.ofMillis(1500)
            group[0].get(typeHandlers.nullableDuration) == null
            group[1].get(typeHandlers.duration) == Duration.ofSeconds(90)
            group[1].get(typeHandlers.nullableDuration) == Duration.ofMillis(250)

        where:
            session << allSessions
    }

    def "multiset aggregation deserializes json columns"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(1L, '{"a": 1}', new ProfilePojo("Alice", 30, "Krakow"), new ProfilePojo("Bob", 25, "Warsaw")))

            JsonTestEntitiesTable jsonEntities = new JsonTestEntitiesTable(null)

        when:
            Expression<Record[]> multisetAgg = DSL.multisetAgg(List.of(jsonEntities.profile, jsonEntities.id))
            List<Record> records = session.select(jsonEntities.id, multisetAgg)
                    .from(jsonEntities)
                    .groupBy(jsonEntities.id)
                    .fetchAll()

        then:
            records.size() == 1
            Record[] group = records[0].get(multisetAgg)
            group.length == 1
            group[0].get(jsonEntities.profile) == new ProfilePojo("Alice", 30, "Krakow")
            group[0].get(jsonEntities.id) == 1L

        where:
            session << allJsonSessions
    }
}
