// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import io.github.thinkfastpl.harbororm.postgres.PostgresBaseIT
import io.github.thinkfastpl.harbororm.postgres.repository.set_in_to_any.InToAnyEntity
import io.github.thinkfastpl.harbororm.postgres.repository.set_in_to_any.QInToAnyEntity

/**
 * Integration tests for PostgreSQL IN-to-ANY(?) optimization.
 * Verifies that .in() queries produce correct results when rewritten
 * from IN (?, ?, ?) to = ANY(?).
 */
class PostgreSqlDialectInToAnyIT extends PostgresBaseIT {

    def setup() {
        loadScript('in-to-any.sql')
        loadScript('in-to-any-data.sql')
    }

    def cleanup() {
        dropAllObjects()
    }

    def "IN with plain string values returns matching rows"() {
        given:
            QInToAnyEntity q = new QInToAnyEntity(null)

        when:
            List<InToAnyEntity> results = session.selectEntity(q)
                    .where(q.name.in(List.of('Alice', 'Carol', 'Eve')))
                    .orderBy(q.id.asc())
                    .fetchAll()

        then:
            results.size() == 3
            results[0].name == 'Alice'
            results[1].name == 'Carol'
            results[2].name == 'Eve'
    }

    def "IN with @Enumerated(STRING) values returns matching rows"() {
        given:
            QInToAnyEntity q = new QInToAnyEntity(null)

        when:
            List<InToAnyEntity> results = session.selectEntity(q)
                    .where(q.status.in(List.of(InToAnyEntity.Status.ACTIVE, InToAnyEntity.Status.PENDING)))
                    .orderBy(q.id.asc())
                    .fetchAll()

        then:
            results.size() == 3
            results[0].id == 1L
            results[0].status == InToAnyEntity.Status.ACTIVE
            results[1].id == 3L
            results[1].status == InToAnyEntity.Status.PENDING
            results[2].id == 4L
            results[2].status == InToAnyEntity.Status.ACTIVE
    }

    def "IN with @Enumerated(ORDINAL) values returns matching rows"() {
        given:
            QInToAnyEntity q = new QInToAnyEntity(null)

        when:
            List<InToAnyEntity> results = session.selectEntity(q)
                    .where(q.priority.in(List.of(InToAnyEntity.Priority.HIGH, InToAnyEntity.Priority.LOW)))
                    .orderBy(q.id.asc())
                    .fetchAll()

        then:
            results.size() == 3
            results.collect { it.id } == [1L, 3L, 4L]
    }

    def "IN with Long id values returns matching rows"() {
        given:
            QInToAnyEntity q = new QInToAnyEntity(null)

        when:
            List<InToAnyEntity> results = session.selectEntity(q)
                    .where(q.id.in(List.of(2L, 4L)))
                    .orderBy(q.id.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].id == 2L
            results[1].id == 4L
    }

    def "IN with single value returns matching row"() {
        given:
            QInToAnyEntity q = new QInToAnyEntity(null)

        when:
            List<InToAnyEntity> results = session.selectEntity(q)
                    .where(q.status.in(List.of(InToAnyEntity.Status.PENDING)))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].name == 'Carol'
    }

    def "IN combined with other conditions"() {
        given:
            QInToAnyEntity q = new QInToAnyEntity(null)

        when:
            List<InToAnyEntity> results = session.selectEntity(q)
                    .where(q.status.in(List.of(InToAnyEntity.Status.ACTIVE, InToAnyEntity.Status.INACTIVE))
                            .and(q.id.gt(2L)))
                    .orderBy(q.id.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].id == 4L
            results[1].id == 5L
    }
}
