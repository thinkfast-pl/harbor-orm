// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.test.domain.EnumeratedTestEntity
import io.github.thinkfastpl.harbororm.test.domain.GetEnumeratedSummary
import io.github.thinkfastpl.harbororm.test.domain.GetEnumeratedSummaryFunction
import io.github.thinkfastpl.harbororm.test.domain.QEnumeratedTestEntity

import static io.github.thinkfastpl.harbororm.test.domain.EnumeratedTestEntity.Priority
import static io.github.thinkfastpl.harbororm.test.domain.EnumeratedTestEntity.Status

/**
 * Integration tests for @Enumerated annotation support on @StoredFunction result columns.
 *
 * Uses the get_enumerated_summary() function/procedure defined in
 * domain-postgres.sql, domain-mysql.sql and domain-mariadb.sql.
 */
class StoredFunctionEnumeratedIT extends AbstractHarborIT {

    def "should hydrate @Enumerated STRING and ORDINAL columns when selecting from stored function"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            session.insertEntity(qEntity, new EnumeratedTestEntity(
                    1L, "Fn Alpha", Status.ACTIVE, Priority.HIGH, null, null
            ))
            session.insertEntity(qEntity, new EnumeratedTestEntity(
                    2L, "Fn Beta", Status.PENDING, Priority.LOW, null, null
            ))

            GetEnumeratedSummaryFunction fn = new GetEnumeratedSummaryFunction("fn")

        when:
            List<GetEnumeratedSummary> results = session.select(fn.call()).fetchAll()

        then:
            results.size() == 2
            with(results.sort { it.name }) { sorted ->
                sorted[0].name == "Fn Alpha"
                sorted[0].status == Status.ACTIVE
                sorted[0].priority == Priority.HIGH
                sorted[0].active == Boolean.TRUE
                sorted[1].name == "Fn Beta"
                sorted[1].status == Status.PENDING
                sorted[1].priority == Priority.LOW
                sorted[1].active == Boolean.FALSE
            }

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "should filter stored function rows by @Enumerated column"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            session.insertEntity(qEntity, new EnumeratedTestEntity(
                    1L, "Filter Active", Status.ACTIVE, Priority.CRITICAL, null, null
            ))
            session.insertEntity(qEntity, new EnumeratedTestEntity(
                    2L, "Filter Inactive", Status.INACTIVE, Priority.LOW, null, null
            ))

            GetEnumeratedSummaryFunction fn = new GetEnumeratedSummaryFunction("fn")

        when:
            List<GetEnumeratedSummary> results = session.select(fn.call())
                    .where(fn.status.eq(Status.ACTIVE))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].name == "Filter Active"
            results[0].priority == Priority.CRITICAL
            results[0].active == Boolean.TRUE

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }
}
