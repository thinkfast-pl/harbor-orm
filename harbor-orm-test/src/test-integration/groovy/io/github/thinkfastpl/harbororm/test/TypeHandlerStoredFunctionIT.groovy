// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.test.domain.GetTypeHandlerSummary
import io.github.thinkfastpl.harbororm.test.domain.GetTypeHandlerSummaryFunction
import io.github.thinkfastpl.harbororm.test.domain.QTypeHandlerTestEntity
import io.github.thinkfastpl.harbororm.test.domain.TypeHandlerTestEntity

import java.time.Duration

/**
 * Integration tests for @TypeHandler annotation support on @StoredFunction result columns.
 *
 * Uses the get_type_handler_summary() function/procedure defined in
 * domain-postgres.sql, domain-mysql.sql and domain-mariadb.sql.
 */
class TypeHandlerStoredFunctionIT extends AbstractHarborIT {

    def "should hydrate @TypeHandler columns when selecting from stored function"() {
        given:
            QTypeHandlerTestEntity qEntity = new QTypeHandlerTestEntity(null)
            session.insertEntity(qEntity, new TypeHandlerTestEntity(1L, "Fn Alpha", Duration.ofMillis(1500), null))
            session.insertEntity(qEntity, new TypeHandlerTestEntity(2L, "Fn Beta", Duration.ofSeconds(90), Duration.ofMillis(250)))

            GetTypeHandlerSummaryFunction fn = new GetTypeHandlerSummaryFunction("fn")

        when:
            List<GetTypeHandlerSummary> results = session.select(fn.call()).fetchAll()

        then:
            results.size() == 2
            with(results.sort { it.name }) { sorted ->
                sorted[0].name == "Fn Alpha"
                sorted[0].duration == Duration.ofMillis(1500)
                sorted[0].nullableDuration == null
                sorted[1].name == "Fn Beta"
                sorted[1].duration == Duration.ofSeconds(90)
                sorted[1].nullableDuration == Duration.ofMillis(250)
            }

        where:
            session << getSessionsExcept(DbType.H2)
    }
}
