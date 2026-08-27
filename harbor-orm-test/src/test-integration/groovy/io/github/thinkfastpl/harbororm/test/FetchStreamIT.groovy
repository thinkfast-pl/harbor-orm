// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

import java.util.stream.Collectors
import java.util.stream.Stream

class FetchStreamIT extends AbstractHarborIT {

    private static final BasicsTable TABLE = new BasicsTable(null)

    private void setupTestData(TestFixtures fixtures) {
        (1L..10L).each { id ->
            fixtures.addBasic(id, "Name${id}", id.intValue() * 10)
        }
    }

    def "fetchStream returns all records"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)

        when:
            List<Long> ids
            try (Stream<Long> stream = session.select(TABLE.id).from(TABLE).fetchStream()) {
                ids = stream.collect(Collectors.toList())
            }

        then:
            ids.size() == 10

        where:
            session << allSessions
    }

    def "fetchStream with custom fetch size works"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)

        when:
            List<Long> ids
            try (Stream<Long> stream = session.select(TABLE.id).from(TABLE).orderBy(TABLE.id.asc()).fetchStream(2)) {
                ids = stream.limit(5).collect(Collectors.toList())
            }

        then:
            ids.size() == 5
            ids == [1L, 2L, 3L, 4L, 5L]

        where:
            session << allSessions
    }

    def "fetchStream with WHERE clause filters correctly"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)

        when:
            List<Long> ids
            try (Stream<Long> stream = session.select(TABLE.id)
                    .from(TABLE)
                    .where(TABLE.numero.gt(50))
                    .orderBy(TABLE.id.asc())
                    .fetchStream()) {
                ids = stream.collect(Collectors.toList())
            }

        then:
            ids.size() == 5
            ids == [6L, 7L, 8L, 9L, 10L]

        where:
            session << allSessions
    }

    def "fetchStream handles empty result set"() {
        given:
            // No test data

        when:
            List<Long> ids
            try (Stream<Long> stream = session.select(TABLE.id).from(TABLE).fetchStream()) {
                ids = stream.collect(Collectors.toList())
            }

        then:
            ids.isEmpty()

        where:
            session << allSessions
    }

    def "fetchStream can be partially consumed"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)

        when:
            Long firstId
            try (Stream<Long> stream = session.select(TABLE.id)
                    .from(TABLE)
                    .orderBy(TABLE.id.asc())
                    .fetchStream()) {
                firstId = stream.findFirst().orElse(null)
            }

        then:
            firstId == 1L

        where:
            session << allSessions
    }

    def "fetchStream supports map transformation"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)

        when:
            List<String> names
            try (Stream<String> stream = session.select(TABLE.name)
                    .from(TABLE)
                    .orderBy(TABLE.id.asc())
                    .limit(3)
                    .fetchStream()) {
                names = stream.map { it.toUpperCase() }.collect(Collectors.toList())
            }

        then:
            names == ["NAME1", "NAME2", "NAME3"]

        where:
            session << allSessions
    }
}
