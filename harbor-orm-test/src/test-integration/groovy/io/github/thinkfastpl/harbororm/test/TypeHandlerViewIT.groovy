// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.test.domain.QTypeHandlerSummary
import io.github.thinkfastpl.harbororm.test.domain.QTypeHandlerTestEntity
import io.github.thinkfastpl.harbororm.test.domain.TypeHandlerSummary
import io.github.thinkfastpl.harbororm.test.domain.TypeHandlerTestEntity

import java.time.Duration

/**
 * Integration tests for @TypeHandler annotation support on @View columns.
 */
class TypeHandlerViewIT extends AbstractHarborIT {

    def "should hydrate @TypeHandler fields when selecting from view"() {
        given:
            QTypeHandlerTestEntity qEntity = new QTypeHandlerTestEntity(null)
            session.insertEntity(qEntity, new TypeHandlerTestEntity(1L, "View Alpha", Duration.ofMillis(1500), null))
            session.insertEntity(qEntity, new TypeHandlerTestEntity(2L, "View Beta", Duration.ofSeconds(90), Duration.ofMillis(250)))

            def view = new QTypeHandlerSummary(null)

        when:
            List<TypeHandlerSummary> results = session.select(view)
                    .orderBy(view.name.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].name == "View Alpha"
            results[0].duration == Duration.ofMillis(1500)
            results[0].nullableDuration == null
            results[1].name == "View Beta"
            results[1].duration == Duration.ofSeconds(90)
            results[1].nullableDuration == Duration.ofMillis(250)

        where:
            session << allSessions
    }

    def "should filter view rows by @TypeHandler column"() {
        given:
            QTypeHandlerTestEntity qEntity = new QTypeHandlerTestEntity(null)
            session.insertEntity(qEntity, new TypeHandlerTestEntity(1L, "View Alpha", Duration.ofMillis(1500), null))
            session.insertEntity(qEntity, new TypeHandlerTestEntity(2L, "View Beta", Duration.ofSeconds(90), Duration.ofMillis(250)))

            def view = new QTypeHandlerSummary(null)

        when:
            List<TypeHandlerSummary> results = session.select(view)
                    .where(view.duration.eq(Duration.ofSeconds(90)))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].name == "View Beta"
            results[0].nullableDuration == Duration.ofMillis(250)

        where:
            session << allSessions
    }
}
