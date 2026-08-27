// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.BasicEntity
import io.github.thinkfastpl.harbororm.test.domain.QBasicEntity
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

import java.util.stream.Collectors
import java.util.stream.Stream

class StreamOperationsIT extends AbstractHarborIT {

    private void setupTestData(TestFixtures fixtures) {
        // Create 10 records with sequential IDs for meaningful streaming tests
        (1L..10L).each { id ->
            fixtures.addBasic(id, "Name${id}", id.intValue() * 10)
        }
    }

    def "streamAll should return a stream of all entities"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<BasicEntity> results
            try (Stream<BasicEntity> stream = session.selectEntity(qEntity).streamAll()) {
                results = stream.collect(Collectors.toList())
            }

        then:
            results.size() == 10
            results.each { entity ->
                entity.id != null
                entity.name.startsWith("Name")
            }

        where:
            session << allSessions
    }

    def "stream with WHERE clause should filter results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<BasicEntity> results
            try (Stream<BasicEntity> stream = session.selectEntity(qEntity)
                    .where(qEntity.numero.gt(50))
                    .streamAll()) {
                results = stream.collect(Collectors.toList())
            }

        then:
            // Records with numero > 50 are ids 6,7,8,9,10 (5 records)
            results.size() == 5
            results.every { it.numero > 50 }

        where:
            session << allSessions
    }

    def "stream with ORDER BY should return ordered results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<BasicEntity> ascResults
            List<BasicEntity> descResults
            try (Stream<BasicEntity> ascStream = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .streamAll()) {
                ascResults = ascStream.collect(Collectors.toList())
            }
            try (Stream<BasicEntity> descStream = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.desc())
                    .streamAll()) {
                descResults = descStream.collect(Collectors.toList())
            }

        then:
            ascResults.size() == 10
            descResults.size() == 10
            ascResults[0].id == 1L
            ascResults[9].id == 10L
            descResults[0].id == 10L
            descResults[9].id == 1L

        where:
            session << allSessions
    }

    def "stream should process all records correctly"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            long streamCount
            try (Stream<BasicEntity> stream = session.selectEntity(qEntity).streamAll()) {
                streamCount = stream.count()
            }

        then:
            streamCount == 10L
            streamCount == session.selectEntity(qEntity).count()

        where:
            session << allSessions
    }

    def "stream with limit should restrict result count"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<BasicEntity> results
            try (Stream<BasicEntity> stream = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .limit(5)
                    .streamAll()) {
                results = stream.collect(Collectors.toList())
            }

        then:
            results.size() == 5
            results*.id == [1L, 2L, 3L, 4L, 5L]

        where:
            session << allSessions
    }

    def "stream with limit and offset should paginate correctly"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<BasicEntity> results
            try (Stream<BasicEntity> stream = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .limit(3)
                    .offset(3)
                    .streamAll()) {
                results = stream.collect(Collectors.toList())
            }

        then:
            results.size() == 3
            results*.id == [4L, 5L, 6L]

        where:
            session << allSessions
    }

    def "stream map operation should transform entities"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<String> names
            try (Stream<BasicEntity> stream = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .limit(3)
                    .streamAll()) {
                names = stream.map { it.name }.collect(Collectors.toList())
            }

        then:
            names == ["Name1", "Name2", "Name3"]

        where:
            session << allSessions
    }

    def "stream filter operation should filter entities"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<BasicEntity> results
            try (Stream<BasicEntity> stream = session.selectEntity(qEntity).streamAll()) {
                results = stream.filter { it.numero >= 80 }.collect(Collectors.toList())
            }

        then:
            // Records with numero >= 80 are ids 8,9,10
            results.size() == 3
            results.every { it.numero >= 80 }

        where:
            session << allSessions
    }

    def "stream combined map and filter operations"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<Integer> numeros
            try (Stream<BasicEntity> stream = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .streamAll()) {
                numeros = stream
                        .filter { it.id <= 5 }
                        .map { it.numero }
                        .collect(Collectors.toList())
            }

        then:
            numeros == [10, 20, 30, 40, 50]

        where:
            session << allSessions
    }

    def "empty result stream should return empty stream"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            // No test data setup - empty table

        when:
            List<BasicEntity> results
            try (Stream<BasicEntity> stream = session.selectEntity(qEntity).streamAll()) {
                results = stream.collect(Collectors.toList())
            }

        then:
            results.isEmpty()

        where:
            session << allSessions
    }

    def "stream from query with no matches should return empty stream"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<BasicEntity> results
            try (Stream<BasicEntity> stream = session.selectEntity(qEntity)
                    .where(qEntity.numero.gt(1000))
                    .streamAll()) {
                results = stream.collect(Collectors.toList())
            }

        then:
            results.isEmpty()

        where:
            session << allSessions
    }

    def "stream should preserve entity data integrity"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            BasicEntity entity
            try (Stream<BasicEntity> stream = session.selectEntity(qEntity)
                    .where(qEntity.id.eq(5L))
                    .streamAll()) {
                entity = stream.findFirst().orElse(null)
            }

        then:
            entity != null
            with(entity) {
                id == 5L
                name == "Name5"
                numero == 50
            }

        where:
            session << allSessions
    }

    def "stream findFirst should return first entity"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            Optional<BasicEntity> first
            try (Stream<BasicEntity> stream = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .streamAll()) {
                first = stream.findFirst()
            }

        then:
            first.isPresent()
            first.get().id == 1L

        where:
            session << allSessions
    }

    def "stream findAny should return an entity"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            Optional<BasicEntity> any
            try (Stream<BasicEntity> stream = session.selectEntity(qEntity).streamAll()) {
                any = stream.findAny()
            }

        then:
            any.isPresent()
            any.get().id != null

        where:
            session << allSessions
    }

    def "stream anyMatch should correctly evaluate predicate"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            boolean hasHighNumero
            boolean hasVeryHighNumero
            try (Stream<BasicEntity> stream1 = session.selectEntity(qEntity).streamAll()) {
                hasHighNumero = stream1.anyMatch { it.numero == 100 }
            }
            try (Stream<BasicEntity> stream2 = session.selectEntity(qEntity).streamAll()) {
                hasVeryHighNumero = stream2.anyMatch { it.numero > 1000 }
            }

        then:
            hasHighNumero == true  // id=10 has numero=100
            hasVeryHighNumero == false

        where:
            session << allSessions
    }

    def "stream allMatch should correctly evaluate predicate"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            boolean allPositive
            boolean allAbove50
            try (Stream<BasicEntity> stream1 = session.selectEntity(qEntity).streamAll()) {
                allPositive = stream1.allMatch { it.numero > 0 }
            }
            try (Stream<BasicEntity> stream2 = session.selectEntity(qEntity).streamAll()) {
                allAbove50 = stream2.allMatch { it.numero > 50 }
            }

        then:
            allPositive == true
            allAbove50 == false

        where:
            session << allSessions
    }

    def "stream noneMatch should correctly evaluate predicate"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            boolean noNegative
            boolean noPositive
            try (Stream<BasicEntity> stream1 = session.selectEntity(qEntity).streamAll()) {
                noNegative = stream1.noneMatch { it.numero < 0 }
            }
            try (Stream<BasicEntity> stream2 = session.selectEntity(qEntity).streamAll()) {
                noPositive = stream2.noneMatch { it.numero > 0 }
            }

        then:
            noNegative == true
            noPositive == false

        where:
            session << allSessions
    }

    def "stream reduce operation should aggregate values"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            int totalNumero
            try (Stream<BasicEntity> stream = session.selectEntity(qEntity).streamAll()) {
                totalNumero = stream.mapToInt { it.numero }.sum()
            }

        then:
            // Sum of 10+20+30+40+50+60+70+80+90+100 = 550
            totalNumero == 550

        where:
            session << allSessions
    }

    def "stream with multiple WHERE conditions"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<BasicEntity> results
            try (Stream<BasicEntity> stream = session.selectEntity(qEntity)
                    .where(qEntity.numero.ge(30))
                    .where(qEntity.numero.le(70))
                    .orderBy(qEntity.id.asc())
                    .streamAll()) {
                results = stream.collect(Collectors.toList())
            }

        then:
            // Records with 30 <= numero <= 70 are ids 3,4,5,6,7
            results.size() == 5
            results*.id == [3L, 4L, 5L, 6L, 7L]

        where:
            session << allSessions
    }

    def "stream distinct operation on mapped values"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            // Add entities with same numero values
            fixtures.addBasic(1L, "Alpha", 10)
            fixtures.addBasic(2L, "Beta", 20)
            fixtures.addBasic(3L, "Gamma", 10)  // duplicate numero
            fixtures.addBasic(4L, "Delta", 20)  // duplicate numero
            fixtures.addBasic(5L, "Epsilon", 30)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<Integer> distinctNumeros
            try (Stream<BasicEntity> stream = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .streamAll()) {
                distinctNumeros = stream
                        .map { it.numero }
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList())
            }

        then:
            distinctNumeros == [10, 20, 30]

        where:
            session << allSessions
    }
}
