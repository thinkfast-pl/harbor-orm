// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL

class PortableFunctionExpressionHyperbolicIT extends AbstractHarborIT {

    def "select sinh()"() {
        when:
            def result = session.select(DSL.sinh(value)).fetchSingle()

        then:
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == Double.class

        where:
            session << allSessions

        combined:
            value || expected
            1.0 || 1.1752
            0.0 || 0.0
            -1.0 || -1.1752
            2.0 || 3.6269
            0.5 || 0.5211
    }

    def "select cosh()"() {
        when:
            def result = session.select(DSL.cosh(value)).fetchSingle()

        then:
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == Double.class

        where:
            session << allSessions

        combined:
            value || expected
            0.0 || 1.0
            1.0 || 1.5431
            -1.0 || 1.5431
            2.0 || 3.7622
            0.5 || 1.1276
    }

    def "select tanh()"() {
        when:
            def result = session.select(DSL.tanh(value)).fetchSingle()

        then:
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == Double.class

        where:
            session << allSessions

        combined:
            value || expected
            1.0 || 0.7616
            0.0 || 0.0
            -1.0 || -0.7616
            2.0 || 0.9640
            0.5 || 0.4621
    }

    def "select asinh()"() {
        when:
            def result = session.select(DSL.asinh(value)).fetchSingle()

        then:
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == Double.class

        where:
            session << allSessions

        combined:
            value || expected
            1.0 || 0.8814
            0.0 || 0.0
            -1.0 || -0.8814
            2.0 || 1.4436
            0.5 || 0.4812
    }

    def "select acosh()"() {
        when:
            def result = session.select(DSL.acosh(value)).fetchSingle()

        then:
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == Double.class

        where:
            session << allSessions

        combined:
            value || expected
            1.0 || 0.0
            2.0 || 1.3170
            3.0 || 1.7627
            5.0 || 2.2924
            1.5 || 0.9624
    }

    def "select atanh()"() {
        when:
            def result = session.select(DSL.atanh(value)).fetchSingle()

        then:
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == Double.class

        where:
            session << allSessions

        combined:
            value || expected
            0.5 || 0.5493
            0.0 || 0.0
            -0.5 || -0.5493
            0.25 || 0.2554
            0.75 || 0.9730
    }

    def "select coth()"() {
        when:
            def result = session.select(DSL.coth(value)).fetchSingle()

        then:
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == Double.class

        where:
            session << allSessions

        combined:
            value || expected
            1.0 || 1.3130
            -1.0 || -1.3130
            2.0 || 1.0373
            0.5 || 2.1640
            3.0 || 1.0050
    }
}
