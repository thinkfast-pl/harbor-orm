// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL

import java.sql.Date
import java.sql.Time

class H2DateTimeFunctionsIT extends H2DialectBaseIT {

    def setup() {
        loadScript("datetime-test.sql")
    }

    def cleanup() {
        dropAllObjects()
    }

    def "EXTRACT YEAR"() {
        when:
            def result = session.select(DSL.year(DSL.name(Date.class, "event_date")))
                .from("events")
                .where(DSL.name(Long.class, "id").eq(1L))
                .fetchSingle()

        then:
            result == 2024
    }

    def "EXTRACT MONTH"() {
        when:
            def result = session.select(DSL.month(DSL.name(Date.class, "event_date")))
                .from("events")
                .where(DSL.name(Long.class, "id").eq(1L))
                .fetchSingle()

        then:
            result == 6
    }



    def "EXTRACT SECOND"() {
        when:
            def result = session.select(DSL.second(DSL.name(Time.class, "event_time")))
                .from("events")
                .where(DSL.name(Long.class, "id").eq(1L))
                .fetchSingle()

        then:
            result == 45
    }
}
