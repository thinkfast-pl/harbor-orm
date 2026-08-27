// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.EnumeratedSummary
import io.github.thinkfastpl.harbororm.test.domain.EnumeratedTestEntity
import io.github.thinkfastpl.harbororm.test.domain.QEnumeratedSummary
import io.github.thinkfastpl.harbororm.test.domain.QEnumeratedTestEntity

import static io.github.thinkfastpl.harbororm.test.domain.EnumeratedTestEntity.Priority
import static io.github.thinkfastpl.harbororm.test.domain.EnumeratedTestEntity.Status

/**
 * Integration tests for @Enumerated and @Convert annotation support on @View columns.
 */
class EnumeratedViewIT extends AbstractHarborIT {

    def "should hydrate @Enumerated STRING and ORDINAL fields when selecting from view"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            session.insertEntity(qEntity, new EnumeratedTestEntity(
                    1L, "View Alpha", Status.ACTIVE, Priority.HIGH, null, null
            ))
            session.insertEntity(qEntity, new EnumeratedTestEntity(
                    2L, "View Beta", Status.PENDING, Priority.LOW, null, null
            ))

            def view = new QEnumeratedSummary(null)

        when:
            List<EnumeratedSummary> results = session.select(view)
                    .orderBy(view.name.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].name == "View Alpha"
            results[0].status == Status.ACTIVE
            results[0].priority == Priority.HIGH
            results[0].active == Boolean.TRUE
            results[1].name == "View Beta"
            results[1].status == Status.PENDING
            results[1].priority == Priority.LOW
            results[1].active == Boolean.FALSE

        where:
            session << allSessions
    }

    def "should filter view rows by @Enumerated column"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            session.insertEntity(qEntity, new EnumeratedTestEntity(
                    1L, "Filter Active", Status.ACTIVE, Priority.CRITICAL, null, null
            ))
            session.insertEntity(qEntity, new EnumeratedTestEntity(
                    2L, "Filter Inactive", Status.INACTIVE, Priority.LOW, null, null
            ))

            def view = new QEnumeratedSummary(null)

        when:
            List<EnumeratedSummary> results = session.select(view)
                    .where(view.status.eq(Status.ACTIVE))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].name == "Filter Active"
            results[0].priority == Priority.CRITICAL
            results[0].active == Boolean.TRUE

        where:
            session << allSessions
    }

    def "should filter view rows by @Convert column applied alongside @Column"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            session.insertEntity(qEntity, new EnumeratedTestEntity(
                    1L, "Converted Active", Status.ACTIVE, Priority.HIGH, null, null
            ))
            session.insertEntity(qEntity, new EnumeratedTestEntity(
                    2L, "Converted Inactive", Status.INACTIVE, Priority.LOW, null, null
            ))

            def view = new QEnumeratedSummary(null)

        when:
            List<EnumeratedSummary> results = session.select(view)
                    .where(view.active.eq(true))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].name == "Converted Active"
            results[0].active == Boolean.TRUE

        where:
            session << allSessions
    }
}
