// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record

class BinaryOperatorConditionIT extends AbstractHarborIT {

    def "select less than"() {
        given:
            Expression<Boolean> expression = left.lt(right)

        when:
            Boolean value = session.select(expression).fetchSingle()

        then:
            value == result

        when:
            Record record = session.select(expression, DSL.constant(1)).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(expression) == result

        where:
            session << allSessions

        combined:
            left | right | result
            DSL.constant(10) | DSL.constant(10) | false
            DSL.constant(10) | DSL.constant(11) | true
            DSL.constant(11) | DSL.constant(10) | false
            DSL.constant(10L) | DSL.constant(10L) | false
            DSL.constant(10L) | DSL.constant(11L) | true
            DSL.constant(11L) | DSL.constant(10L) | false
            DSL.constant((short) 10) | DSL.constant((short) 10) | false
            DSL.constant((short) 10) | DSL.constant((short) 11) | true
            DSL.constant((short) 11) | DSL.constant((short) 10) | false
            DSL.constant((byte) 10) | DSL.constant((byte) 10) | false
            DSL.constant((byte) 10) | DSL.constant((byte) 11) | true
            DSL.constant((byte) 11) | DSL.constant((byte) 10) | false
            DSL.constant(10.1F) | DSL.constant(10.1F) | false
            DSL.constant(10.1F) | DSL.constant(11.1F) | true
            DSL.constant(11.1F) | DSL.constant(10.1F) | false
            DSL.constant(10.1D) | DSL.constant(10.1D) | false
            DSL.constant(10.1D) | DSL.constant(11.1D) | true
            DSL.constant(11.1D) | DSL.constant(10.1D) | false
            DSL.constant('a' as char) | DSL.constant('a' as char) | false
            DSL.constant('a' as char) | DSL.constant('b' as char) | true
            DSL.constant('b' as char) | DSL.constant('a' as char) | false
            DSL.constant('aaa') | DSL.constant('aaa') | false
            DSL.constant('aaa') | DSL.constant('bbb') | true
            DSL.constant('bbb') | DSL.constant('aaa') | false
    }

    def "select greater than"() {
        given:
            Expression<Boolean> expression = left.gt(right)

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(expression) == result

        where:
            session << allSessions
        combined:
            left | right | result
            DSL.constant(10) | DSL.constant(10) | false
            DSL.constant(10) | DSL.constant(11) | false
            DSL.constant(11) | DSL.constant(10) | true
            DSL.constant(10L) | DSL.constant(10L) | false
            DSL.constant(10L) | DSL.constant(11L) | false
            DSL.constant(11L) | DSL.constant(10L) | true
            DSL.constant((short) 10) | DSL.constant((short) 10) | false
            DSL.constant((short) 10) | DSL.constant((short) 11) | false
            DSL.constant((short) 11) | DSL.constant((short) 10) | true
            DSL.constant((byte) 10) | DSL.constant((byte) 10) | false
            DSL.constant((byte) 10) | DSL.constant((byte) 11) | false
            DSL.constant((byte) 11) | DSL.constant((byte) 10) | true
            DSL.constant(10.1F) | DSL.constant(10.1F) | false
            DSL.constant(10.1F) | DSL.constant(11.1F) | false
            DSL.constant(11.1F) | DSL.constant(10.1F) | true
            DSL.constant(10.1D) | DSL.constant(10.1D) | false
            DSL.constant(10.1D) | DSL.constant(11.1D) | false
            DSL.constant(11.1D) | DSL.constant(10.1D) | true
            DSL.constant('a' as char) | DSL.constant('a' as char) | false
            DSL.constant('a' as char) | DSL.constant('b' as char) | false
            DSL.constant('b' as char) | DSL.constant('a' as char) | true
            DSL.constant('aaa') | DSL.constant('aaa') | false
            DSL.constant('aaa') | DSL.constant('bbb') | false
            DSL.constant('bbb') | DSL.constant('aaa') | true
    }

    def "select less than or equal"() {
        given:
            Expression<Boolean> expression = left.le(right)

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(expression) == result

        where:
            session << allSessions

        combined:
            left | right | result
            DSL.constant(10) | DSL.constant(10) | true
            DSL.constant(10) | DSL.constant(11) | true
            DSL.constant(11) | DSL.constant(10) | false
            DSL.constant(10L) | DSL.constant(10L) | true
            DSL.constant(10L) | DSL.constant(11L) | true
            DSL.constant(11L) | DSL.constant(10L) | false
            DSL.constant((short) 10) | DSL.constant((short) 10) | true
            DSL.constant((short) 10) | DSL.constant((short) 11) | true
            DSL.constant((short) 11) | DSL.constant((short) 10) | false
            DSL.constant((byte) 10) | DSL.constant((byte) 10) | true
            DSL.constant((byte) 10) | DSL.constant((byte) 11) | true
            DSL.constant((byte) 11) | DSL.constant((byte) 10) | false
            DSL.constant(10.1F) | DSL.constant(10.1F) | true
            DSL.constant(10.1F) | DSL.constant(11.1F) | true
            DSL.constant(11.1F) | DSL.constant(10.1F) | false
            DSL.constant(10.1D) | DSL.constant(10.1D) | true
            DSL.constant(10.1D) | DSL.constant(11.1D) | true
            DSL.constant(11.1D) | DSL.constant(10.1D) | false
            DSL.constant('a' as char) | DSL.constant('a' as char) | true
            DSL.constant('a' as char) | DSL.constant('b' as char) | true
            DSL.constant('b' as char) | DSL.constant('a' as char) | false
            DSL.constant('aaa') | DSL.constant('aaa') | true
            DSL.constant('aaa') | DSL.constant('bbb') | true
            DSL.constant('bbb') | DSL.constant('aaa') | false
    }

    def "select greater than or equal"() {
        given:
            Expression<Boolean> expression = left.ge(right)

        when:
            Boolean value = session.select(expression).fetchSingle()

        then:
            value == result

        when:
            Record record = session.select(expression, DSL.constant(1)).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(expression) == result

        where:
            session << allSessions
        combined:
            left | right | result
            DSL.constant(10) | DSL.constant(10) | true
            DSL.constant(10) | DSL.constant(11) | false
            DSL.constant(11) | DSL.constant(10) | true
            DSL.constant(10L) | DSL.constant(10L) | true
            DSL.constant(10L) | DSL.constant(11L) | false
            DSL.constant(11L) | DSL.constant(10L) | true
            DSL.constant((short) 10) | DSL.constant((short) 10) | true
            DSL.constant((short) 10) | DSL.constant((short) 11) | false
            DSL.constant((short) 11) | DSL.constant((short) 10) | true
            DSL.constant((byte) 10) | DSL.constant((byte) 10) | true
            DSL.constant((byte) 10) | DSL.constant((byte) 11) | false
            DSL.constant((byte) 11) | DSL.constant((byte) 10) | true
            DSL.constant(10.1F) | DSL.constant(10.1F) | true
            DSL.constant(10.1F) | DSL.constant(11.1F) | false
            DSL.constant(11.1F) | DSL.constant(10.1F) | true
            DSL.constant(10.1D) | DSL.constant(10.1D) | true
            DSL.constant(10.1D) | DSL.constant(11.1D) | false
            DSL.constant(11.1D) | DSL.constant(10.1D) | true
            DSL.constant('a' as char) | DSL.constant('a' as char) | true
            DSL.constant('a' as char) | DSL.constant('b' as char) | false
            DSL.constant('b' as char) | DSL.constant('a' as char) | true
            DSL.constant('aaa') | DSL.constant('aaa') | true
            DSL.constant('aaa') | DSL.constant('bbb') | false
            DSL.constant('bbb') | DSL.constant('aaa') | true
    }

    def "select and"() {
        when:
            Record record = session.select([condition]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(condition) == result

        where:
            session << allSessions

        combined:
            condition || result
            DSL.constant(true).and(true).and(true) || true
            DSL.constant(false).and(true).and(true) || false
            DSL.constant(true).and(false).and(true) || false
            DSL.constant(true).and(true).and(false) || false
            DSL.constant(false).and(false).and(false) || false
    }

    def "select or"() {
        when:
            Record record = session.select([condition]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(condition) == result

        where:
            session << allSessions

        combined:
            condition || result
            DSL.constant(true).or(true).or(true) || true
            DSL.constant(false).or(true).or(true) || true
            DSL.constant(true).or(false).or(true) || true
            DSL.constant(true).or(true).or(false) || true
            DSL.constant(false).or(false).or(false) || false
    }

    def "select eq"() {
        given:
            Expression<Boolean> expression = left.eq(right)

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(expression) == result
            if (result != null) {
                assert record.get(1).class == Boolean.class
            }

        where:
            session << allSessions
        combined:
            left | right | result
            DSL.constant(10) | DSL.constant(10) | true
            DSL.constant(10) | DSL.constant(11) | false
            DSL.constant(11) | DSL.constant(10) | false
            DSL.constant(11) | DSL.constant((Integer) null) | null
            DSL.constant(10L) | DSL.constant(10L) | true
            DSL.constant(10L) | DSL.constant(11L) | false
            DSL.constant(11L) | DSL.constant(10L) | false
            DSL.constant(11L) | DSL.constant((Long) null) | null
            DSL.constant((short) 10) | DSL.constant((short) 10) | true
            DSL.constant((short) 10) | DSL.constant((short) 11) | false
            DSL.constant((short) 11) | DSL.constant((short) 10) | false
            DSL.constant((short) 11) | DSL.constant((Short) null) | null
            DSL.constant((byte) 10) | DSL.constant((byte) 10) | true
            DSL.constant((byte) 10) | DSL.constant((byte) 11) | false
            DSL.constant((byte) 11) | DSL.constant((byte) 10) | false
            DSL.constant((byte) 11) | DSL.constant((Byte) null) | null
            DSL.constant(10.1F) | DSL.constant(10.1F) | true
            DSL.constant(10.1F) | DSL.constant(11.1F) | false
            DSL.constant(11.1F) | DSL.constant(10.1F) | false
            DSL.constant(10.1D) | DSL.constant(10.1D) | true
            DSL.constant(10.1D) | DSL.constant(11.1D) | false
            DSL.constant(11.1D) | DSL.constant(10.1D) | false
            DSL.constant('a' as char) | DSL.constant('a' as char) | true
            DSL.constant('a' as char) | DSL.constant('b' as char) | false
            DSL.constant('b' as char) | DSL.constant('a' as char) | false
            DSL.constant('aaa') | DSL.constant('aaa') | true
            DSL.constant('aaa') | DSL.constant('bbb') | false
            DSL.constant('bbb') | DSL.constant('aaa') | false
    }

    def "not equal"() {
        given:
            Expression<Boolean> expression = left.notEq(right)

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(expression) == result

        where:
            session << allSessions

        combined:
            left | right | result
            DSL.constant(10) | DSL.constant(10) | false
            DSL.constant(10) | DSL.constant(11) | true
            DSL.constant(11) | DSL.constant(10) | true
            DSL.constant(10L) | DSL.constant(10L) | false
            DSL.constant(10L) | DSL.constant(11L) | true
            DSL.constant(11L) | DSL.constant(10L) | true
            DSL.constant((short) 10) | DSL.constant((short) 10) | false
            DSL.constant((short) 10) | DSL.constant((short) 11) | true
            DSL.constant((short) 11) | DSL.constant((short) 10) | true
            DSL.constant((byte) 10) | DSL.constant((byte) 10) | false
            DSL.constant((byte) 10) | DSL.constant((byte) 11) | true
            DSL.constant((byte) 11) | DSL.constant((byte) 10) | true
            DSL.constant(10.1F) | DSL.constant(10.1F) | false
            DSL.constant(10.1F) | DSL.constant(11.1F) | true
            DSL.constant(11.1F) | DSL.constant(10.1F) | true
            DSL.constant(10.1D) | DSL.constant(10.1D) | false
            DSL.constant(10.1D) | DSL.constant(11.1D) | true
            DSL.constant(11.1D) | DSL.constant(10.1D) | true
            DSL.constant('a' as char) | DSL.constant('a' as char) | false
            DSL.constant('a' as char) | DSL.constant('b' as char) | true
            DSL.constant('b' as char) | DSL.constant('a' as char) | true
            DSL.constant('aaa') | DSL.constant('aaa') | false
            DSL.constant('aaa') | DSL.constant('bbb') | true
            DSL.constant('bbb') | DSL.constant('aaa') | true
    }
}
