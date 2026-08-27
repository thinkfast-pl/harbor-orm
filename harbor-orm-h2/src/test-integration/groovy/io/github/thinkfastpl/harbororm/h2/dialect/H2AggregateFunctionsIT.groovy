// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL

class H2AggregateFunctionsIT extends H2DialectBaseIT {

    def setup() {
        loadScript("window-functions-test.sql")
    }

    def cleanup() {
        dropAllObjects()
    }

    def "AVG calculates average"() {
        when:
            def result = session.select(DSL.avg(DSL.name(BigDecimal, "salary")))
                .from("employees")
                .fetchSingle()

        then:
            result != null
            (result as BigDecimal) == 67500.0
    }

    def "STDDEV_POP calculates population standard deviation"() {
        when:
            def result = session.select(DSL.stddevPop(DSL.name(BigDecimal, "salary")))
                .from("employees")
                .fetchSingle()

        then:
            result != null
            Math.abs((result as double) - 8539.13d) < 1.0d
    }

    def "STDDEV_SAMP calculates sample standard deviation"() {
        when:
            def result = session.select(DSL.stddevSamp(DSL.name(BigDecimal, "salary")))
                .from("employees")
                .fetchSingle()

        then:
            result != null
            Math.abs((result as double) - 9354.14d) < 1.0d
    }

    def "VAR_POP calculates population variance"() {
        when:
            def result = session.select(DSL.varPop(DSL.name(BigDecimal, "salary")))
                .from("employees")
                .fetchSingle()

        then:
            result != null
            Math.abs((result as double) - 72916666.67d) < 1.0d
    }

    def "VAR_SAMP calculates sample variance"() {
        when:
            def result = session.select(DSL.varSamp(DSL.name(BigDecimal, "salary")))
                .from("employees")
                .fetchSingle()

        then:
            result != null
            Math.abs((result as double) - 87500000.0d) < 1.0d
    }
}
