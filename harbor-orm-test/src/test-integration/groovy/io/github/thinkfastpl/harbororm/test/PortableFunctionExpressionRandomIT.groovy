// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL

class PortableFunctionExpressionRandomIT extends AbstractHarborIT {

    def "random()"() {
        when:
            Double result = session.select(DSL.random()).fetchSingle()

        then:
            result != null
            result >= 0.0
            result < 1.0

        where:
            session << allSessions
    }

    def "UUID generates a non-null UUID"() {
        expect:
            session.select(DSL.uuid()).fetchSingle() != null

        where:
            session << allSessions
    }

    def "UUID generates distinct values on successive calls"() {
        when:
            UUID first = session.select(DSL.uuid()).fetchSingle()
            UUID second = session.select(DSL.uuid()).fetchSingle()

        then:
            first != null
            second != null
            first != second

        where:
            session << allSessions
    }
}
