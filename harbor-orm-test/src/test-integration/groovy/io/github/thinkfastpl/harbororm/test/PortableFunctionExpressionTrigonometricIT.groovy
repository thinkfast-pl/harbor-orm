// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL

class PortableFunctionExpressionTrigonometricIT extends AbstractHarborIT {

    def "select acos()"() {
        when:
            def result = session.select(DSL.acos(value)).fetchSingle()

        then:
            result - expected < 0.0001
            result - expected > -0.0001
            result.getClass() == Double.class

        where:
            session << allSessions

        combined:
            value || expected
            1.0 || 0.0
            0.0 || 1.5708
            -1.0 || 3.14159
            0.5 || 1.0472
            -0.5 || 2.0944
    }

    def "select asin()"() {
        when:
            def result = session.select(DSL.asin(value)).fetchSingle()

        then:
            result - expected < 0.0001
            result - expected > -0.0001
            result.getClass() == Double.class

        where:
            session << allSessions

        combined:
            value || expected
            1.0 || 1.5708
            0.0 || 0.0
            -1.0 || -1.5708
            0.5 || 0.5236
            -0.5 || -0.5236
    }

    def "select atan()"() {
        when:
            def result = session.select(DSL.atan(value)).fetchSingle()

        then:
            result - expected < 0.0001
            result - expected > -0.0001
            result.getClass() == Double.class

        where:
            session << allSessions

        combined:
            value || expected
            1.0 || 0.7854
            0.0 || 0.0
            -1.0 || -0.7854
            1.732 || 1.0472
            -1.732 || -1.0472
    }

    def "select atan2()"() {
        when:
            def result = session.select(DSL.atan2(y, x)).fetchSingle()

        then:
            result - expected < 0.0001
            result - expected > -0.0001
            result.getClass() == Double.class

        where:
            session << allSessions

        combined:
            y | x || expected
            1.0 | 0.0 || 1.5708
            0.0 | 1.0 || 0.0
            1.0 | 1.0 || 0.7854
            -1.0 | 0.0 || -1.5708
            -1.0 | -1.0 || -2.3562
    }

    def "select atan2(Expression, Double)"() {
        when:
            def result = session.select(DSL.atan2(DSL.constant(y), x)).fetchSingle()

        then:
            result - expected < 0.0001
            result - expected > -0.0001
            result.getClass() == Double.class

        where:
            session << allSessions

        combined:
            y | x || expected
            1.0 | 0.0 || 1.5708
            0.0 | 1.0 || 0.0
            1.0 | 1.0 || 0.7854
    }

    def "select cos()"() {
        when:
            def result = session.select(DSL.cos(value)).fetchSingle()

        then:
            result - expected < 0.0001
            result - expected > -0.0001
            result.getClass() == Double.class

        where:
            session << allSessions

        combined:
            value || expected
            0.0 || 1.0
            1.5708 || 0.0
            3.14159 || -1.0
            1.0472 || 0.5
            6.28318 || 1.0
    }

    def "select cot()"() {
        when:
            def result = session.select(DSL.cot(value)).fetchSingle()

        then:
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == Double.class

        where:
            session << allSessions

        combined:
            value || expected
            0.5 || 1.8305
            0.7854 || 1.0
            0.5236 || 1.732
            1.0472 || 0.5774
            2.3562 || -1.0
    }

    def "select sin()"() {
        when:
            def result = session.select(DSL.sin(value)).fetchSingle()

        then:
            result - expected < 0.0001
            result - expected > -0.0001
            result.getClass() == Double.class

        where:
            session << allSessions

        combined:
            value || expected
            1.0 || 0.8415
            0.0 || 0.0
            1.5708 || 1.0
            3.14159 || 0.0
            0.5236 || 0.5
    }

    def "select tan()"() {
        when:
            def result = session.select(DSL.tan(value)).fetchSingle()

        then:
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == Double.class

        where:
            session << allSessions

        combined:
            value || expected
            1.0 || 1.5574
            0.0 || 0.0
            0.7854 || 1.0
            0.5236 || 0.5774
            1.0472 || 1.732
    }
}
