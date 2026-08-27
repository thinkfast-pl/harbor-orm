// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record

class H2DialectOperatorsIT extends H2DialectBaseIT {

    def "and"() {
        when:
            Record record = session.select([condition]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(condition) == result

        where:
            condition                                 || result
            DSL.constant(true).and(true).and(true)    || true
            DSL.constant(false).and(true).and(true)   || false
            DSL.constant(true).and(false).and(true)   || false
            DSL.constant(true).and(true).and(false)   || false
            DSL.constant(false).and(false).and(false) || false
    }

    def "between"() {
        when:
            Record record = session.select([condition]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(condition) == result

        where:
            condition                                                 || result
            DSL.constant(2).between(1, 3)                             || true
            DSL.constant(2).between(DSL.constant(1), 3)               || true
            DSL.constant(2).between(1, DSL.constant(3))               || true
            DSL.constant(2).between(DSL.constant(1), DSL.constant(3)) || true
            DSL.constant(2).between(3, 1)                             || false
            DSL.constant(2L).between(1L, 3L)                          || true
            DSL.constant(2L).between(3L, 1L)                          || false
            DSL.constant(2.0).between(1.0, 3.0)                       || true
            DSL.constant(2.0).between(3.0, 1.0)                       || false
    }

    def "betweenSymmetric"() {
        when:
            Record record = session.select([condition]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(condition) == result

        where:
            condition                                                          || result
            DSL.constant(2).betweenSymmetric(1, 3)                             || true
            DSL.constant(2).betweenSymmetric(3, 5)                             || false
            DSL.constant(2).betweenSymmetric(DSL.constant(1), 3)               || true
            DSL.constant(2).betweenSymmetric(1, DSL.constant(3))               || true
            DSL.constant(2).betweenSymmetric(DSL.constant(1), DSL.constant(3)) || true
            DSL.constant(2).betweenSymmetric(3, 1)                             || true
            DSL.constant(2L).betweenSymmetric(1L, 3L)                          || true
            DSL.constant(2L).betweenSymmetric(3L, 1L)                          || true
            DSL.constant(2L).betweenSymmetric(3L, 5L)                          || false
            DSL.constant(2.0).betweenSymmetric(1.0, 3.0)                       || true
            DSL.constant(2.0).betweenSymmetric(3.0, 1.0)                       || true
            DSL.constant(2.0).betweenSymmetric(3.0, 5.0)                       || false
    }

    def "equal"() {
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
            left                      | right                        | result
            DSL.constant(10)          | DSL.constant(10)             | true
            DSL.constant(10)          | DSL.constant(11)             | false
            DSL.constant(11)          | DSL.constant(10)             | false
            DSL.constant(11)          | DSL.constant((Integer) null) | null
            DSL.constant(10L)         | DSL.constant(10L)            | true
            DSL.constant(10L)         | DSL.constant(11L)            | false
            DSL.constant(11L)         | DSL.constant(10L)            | false
            DSL.constant(11L)         | DSL.constant((Long) null)    | null
            DSL.constant((short) 10)  | DSL.constant((short) 10)     | true
            DSL.constant((short) 10)  | DSL.constant((short) 11)     | false
            DSL.constant((short) 11)  | DSL.constant((short) 10)     | false
            DSL.constant((short) 11)  | DSL.constant((Short) null)   | null
            DSL.constant((byte) 10)   | DSL.constant((byte) 10)      | true
            DSL.constant((byte) 10)   | DSL.constant((byte) 11)      | false
            DSL.constant((byte) 11)   | DSL.constant((byte) 10)      | false
            DSL.constant((byte) 11)   | DSL.constant((Byte) null)    | null
            DSL.constant(10.1F)       | DSL.constant(10.1F)          | true
            DSL.constant(10.1F)       | DSL.constant(11.1F)          | false
            DSL.constant(11.1F)       | DSL.constant(10.1F)          | false
            DSL.constant(10.1D)       | DSL.constant(10.1D)          | true
            DSL.constant(10.1D)       | DSL.constant(11.1D)          | false
            DSL.constant(11.1D)       | DSL.constant(10.1D)          | false
            DSL.constant('a' as char) | DSL.constant('a' as char)    | true
            DSL.constant('a' as char) | DSL.constant('b' as char)    | false
            DSL.constant('b' as char) | DSL.constant('a' as char)    | false
            DSL.constant('aaa')       | DSL.constant('aaa')          | true
            DSL.constant('aaa')       | DSL.constant('bbb')          | false
            DSL.constant('bbb')       | DSL.constant('aaa')          | false
    }

    def "greater or equal"() {
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
            left                      | right                     | result
            DSL.constant(10)          | DSL.constant(10)          | true
            DSL.constant(10)          | DSL.constant(11)          | false
            DSL.constant(11)          | DSL.constant(10)          | true
            DSL.constant(10L)         | DSL.constant(10L)         | true
            DSL.constant(10L)         | DSL.constant(11L)         | false
            DSL.constant(11L)         | DSL.constant(10L)         | true
            DSL.constant((short) 10)  | DSL.constant((short) 10)  | true
            DSL.constant((short) 10)  | DSL.constant((short) 11)  | false
            DSL.constant((short) 11)  | DSL.constant((short) 10)  | true
            DSL.constant((byte) 10)   | DSL.constant((byte) 10)   | true
            DSL.constant((byte) 10)   | DSL.constant((byte) 11)   | false
            DSL.constant((byte) 11)   | DSL.constant((byte) 10)   | true
            DSL.constant(10.1F)       | DSL.constant(10.1F)       | true
            DSL.constant(10.1F)       | DSL.constant(11.1F)       | false
            DSL.constant(11.1F)       | DSL.constant(10.1F)       | true
            DSL.constant(10.1D)       | DSL.constant(10.1D)       | true
            DSL.constant(10.1D)       | DSL.constant(11.1D)       | false
            DSL.constant(11.1D)       | DSL.constant(10.1D)       | true
            DSL.constant('a' as char) | DSL.constant('a' as char) | true
            DSL.constant('a' as char) | DSL.constant('b' as char) | false
            DSL.constant('b' as char) | DSL.constant('a' as char) | true
            DSL.constant('aaa')       | DSL.constant('aaa')       | true
            DSL.constant('aaa')       | DSL.constant('bbb')       | false
            DSL.constant('bbb')       | DSL.constant('aaa')       | true
    }

    def "greater than"() {
        given:
            Expression<Boolean> expression = left.gt(right)

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(expression) == result

        where:
            left                      | right                     | result
            DSL.constant(10)          | DSL.constant(10)          | false
            DSL.constant(10)          | DSL.constant(11)          | false
            DSL.constant(11)          | DSL.constant(10)          | true
            DSL.constant(10L)         | DSL.constant(10L)         | false
            DSL.constant(10L)         | DSL.constant(11L)         | false
            DSL.constant(11L)         | DSL.constant(10L)         | true
            DSL.constant((short) 10)  | DSL.constant((short) 10)  | false
            DSL.constant((short) 10)  | DSL.constant((short) 11)  | false
            DSL.constant((short) 11)  | DSL.constant((short) 10)  | true
            DSL.constant((byte) 10)   | DSL.constant((byte) 10)   | false
            DSL.constant((byte) 10)   | DSL.constant((byte) 11)   | false
            DSL.constant((byte) 11)   | DSL.constant((byte) 10)   | true
            DSL.constant(10.1F)       | DSL.constant(10.1F)       | false
            DSL.constant(10.1F)       | DSL.constant(11.1F)       | false
            DSL.constant(11.1F)       | DSL.constant(10.1F)       | true
            DSL.constant(10.1D)       | DSL.constant(10.1D)       | false
            DSL.constant(10.1D)       | DSL.constant(11.1D)       | false
            DSL.constant(11.1D)       | DSL.constant(10.1D)       | true
            DSL.constant('a' as char) | DSL.constant('a' as char) | false
            DSL.constant('a' as char) | DSL.constant('b' as char) | false
            DSL.constant('b' as char) | DSL.constant('a' as char) | true
            DSL.constant('aaa')       | DSL.constant('aaa')       | false
            DSL.constant('aaa')       | DSL.constant('bbb')       | false
            DSL.constant('bbb')       | DSL.constant('aaa')       | true
    }

    def "is distinct from"() {
        given:
            Expression<Boolean> expression = left.isDistinctFrom(right)

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(expression) == result

        where:
            left                      | right                        | result
            DSL.constant(10)          | DSL.constant(10)             | false
            DSL.constant(10)          | DSL.constant(11)             | true
            DSL.constant(11)          | DSL.constant(10)             | true
            DSL.constant(11)          | DSL.constant((Integer) null) | true
            DSL.constant(10L)         | DSL.constant(10L)            | false
            DSL.constant(10L)         | DSL.constant(11L)            | true
            DSL.constant(11L)         | DSL.constant(10L)            | true
            DSL.constant(11L)         | DSL.constant((Long) null)    | true
            DSL.constant((short) 10)  | DSL.constant((short) 10)     | false
            DSL.constant((short) 10)  | DSL.constant((short) 11)     | true
            DSL.constant((short) 11)  | DSL.constant((short) 10)     | true
            DSL.constant((short) 11)  | DSL.constant((Short) null)   | true
            DSL.constant((byte) 10)   | DSL.constant((byte) 10)      | false
            DSL.constant((byte) 10)   | DSL.constant((byte) 11)      | true
            DSL.constant((byte) 11)   | DSL.constant((byte) 10)      | true
            DSL.constant((byte) 11)   | DSL.constant((Byte) null)    | true
            DSL.constant(10.1F)       | DSL.constant(10.1F)          | false
            DSL.constant(10.1F)       | DSL.constant(11.1F)          | true
            DSL.constant(11.1F)       | DSL.constant(10.1F)          | true
            DSL.constant(10.1D)       | DSL.constant(10.1D)          | false
            DSL.constant(10.1D)       | DSL.constant(11.1D)          | true
            DSL.constant(11.1D)       | DSL.constant(10.1D)          | true
            DSL.constant('a' as char) | DSL.constant('a' as char)    | false
            DSL.constant('a' as char) | DSL.constant('b' as char)    | true
            DSL.constant('b' as char) | DSL.constant('a' as char)    | true
            DSL.constant('aaa')       | DSL.constant('aaa')          | false
            DSL.constant('aaa')       | DSL.constant('bbb')          | true
            DSL.constant('bbb')       | DSL.constant('aaa')          | true
    }

    def "is distinct from 2"() {
        when:
            boolean result = session.select(DSL.constant(10).isDistinctFrom(11)).fetchSingle()

        then:
            result
    }

    def "is false"() {
        given:
            Expression<Boolean> expression = value.isFalse()

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(expression) == result

        where:
            value                       || result
            DSL.constant(Boolean.TRUE)  || false
            DSL.constant(Boolean.FALSE) || true
            DSL.nil(Boolean.class)      || false
    }

    def "is not distinct from"() {
        given:
            Expression<Boolean> expression = left.isNotDistinctFrom(right)

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(expression) == result

        where:
            left                      | right                        | result
            DSL.constant(10)          | DSL.constant(10)             | true
            DSL.constant(10)          | DSL.constant(11)             | false
            DSL.constant(11)          | DSL.constant(10)             | false
            DSL.constant(11)          | DSL.constant((Integer) null) | false
            DSL.constant(10L)         | DSL.constant(10L)            | true
            DSL.constant(10L)         | DSL.constant(11L)            | false
            DSL.constant(11L)         | DSL.constant(10L)            | false
            DSL.constant(11L)         | DSL.constant((Long) null)    | false
            DSL.constant((short) 10)  | DSL.constant((short) 10)     | true
            DSL.constant((short) 10)  | DSL.constant((short) 11)     | false
            DSL.constant((short) 11)  | DSL.constant((short) 10)     | false
            DSL.constant((short) 11)  | DSL.constant((Short) null)   | false
            DSL.constant((byte) 10)   | DSL.constant((byte) 10)      | true
            DSL.constant((byte) 10)   | DSL.constant((byte) 11)      | false
            DSL.constant((byte) 11)   | DSL.constant((byte) 10)      | false
            DSL.constant((byte) 11)   | DSL.constant((Byte) null)    | false
            DSL.constant(10.1F)       | DSL.constant(10.1F)          | true
            DSL.constant(10.1F)       | DSL.constant(11.1F)          | false
            DSL.constant(11.1F)       | DSL.constant(10.1F)          | false
            DSL.constant(10.1D)       | DSL.constant(10.1D)          | true
            DSL.constant(10.1D)       | DSL.constant(11.1D)          | false
            DSL.constant(11.1D)       | DSL.constant(10.1D)          | false
            DSL.constant('a' as char) | DSL.constant('a' as char)    | true
            DSL.constant('a' as char) | DSL.constant('b' as char)    | false
            DSL.constant('b' as char) | DSL.constant('a' as char)    | false
            DSL.constant('aaa')       | DSL.constant('aaa')          | true
            DSL.constant('aaa')       | DSL.constant('bbb')          | false
            DSL.constant('bbb')       | DSL.constant('aaa')          | false
    }

    def "is not distinct from 2"() {
        when:
            boolean result = session.select(DSL.constant(10).isNotDistinctFrom(11)).fetchSingle()

        then:
            !result
    }

    def "is not false"() {
        given:
            Expression<Boolean> expression = value.isNotFalse()

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(expression) == result

        where:
            value                       || result
            DSL.constant(Boolean.TRUE)  || true
            DSL.constant(Boolean.FALSE) || false
            DSL.nil(Boolean.class)      || true
    }

    def "is nil not null"() {
        given:
            Expression<Boolean> expression = DSL.nil(type).isNotNull()

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == false
            record.get(1).class == Boolean.class
            !record.get(expression)

        where:
            type            || _
            Byte.class      || _
            Short.class     || _
            Integer.class   || _
            Long.class      || _
            Float.class     || _
            Double.class    || _
            Boolean.class   || _
            Character.class || _
            String.class    || _
    }

    def "is value not null"() {
        given:
            Expression<Boolean> expression = valueExpression.isNotNull()

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == true
            record.get(1).class == Boolean.class
            record.get(expression)

        where:
            valueExpression           || _
            DSL.constant(10)          || _
            DSL.constant(11L)         || _
            DSL.constant((short) 12)  || _
            DSL.constant((byte) 13)   || _
            DSL.constant(14.1F)       || _
            DSL.constant(15.2)        || _
            DSL.constant(true)        || _
            DSL.constant(false)       || _
            DSL.constant('x' as char) || _
            DSL.constant('abc')       || _
    }

    def "is not true"() {
        given:
            Expression<Boolean> expression = value.isNotTrue()

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(expression) == result

        where:
            value                       || result
            DSL.constant(Boolean.TRUE)  || false
            DSL.constant(Boolean.FALSE) || true
            DSL.nil(Boolean.class)      || true
    }

    def "is not unknown"() {
        given:
            Expression<Boolean> expression = value.isNotUnknown()

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(expression) == result

        where:
            value                       || result
            DSL.constant(Boolean.TRUE)  || true
            DSL.constant(Boolean.FALSE) || true
            DSL.nil(Boolean.class)      || false
    }

    def "is nil null"() {
        given:
            Expression<Boolean> expression = DSL.nil(type).isNull()

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == true
            record.get(1).class == Boolean.class
            record.get(expression)

        where:
            type            || _
            Byte.class      || _
            Short.class     || _
            Integer.class   || _
            Long.class      || _
            Float.class     || _
            Double.class    || _
            Boolean.class   || _
            Character.class || _
            String.class    || _
    }

    def "is value null"() {
        given:
            Expression<Boolean> expression = valueExpression.isNull()

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == false
            record.get(1).class == Boolean.class
            !record.get(expression)

        where:
            valueExpression           || _
            DSL.constant(10)          || _
            DSL.constant(11L)         || _
            DSL.constant((short) 12)  || _
            DSL.constant((byte) 13)   || _
            DSL.constant(14.1F)       || _
            DSL.constant(15.2)        || _
            DSL.constant(true)        || _
            DSL.constant(false)       || _
            DSL.constant('x' as char) || _
            DSL.constant('abc')       || _
    }

    def "is true"() {
        given:
            Expression<Boolean> expression = value.isTrue()

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(expression) == result

        where:
            value                       || result
            DSL.constant(Boolean.TRUE)  || true
            DSL.constant(Boolean.FALSE) || false
            DSL.nil(Boolean.class)      || false
    }

    def "is unknown"() {
        given:
            Expression<Boolean> expression = value.isUnknown()

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(expression) == result

        where:
            value                       || result
            DSL.constant(Boolean.TRUE)  || false
            DSL.constant(Boolean.FALSE) || false
            DSL.nil(Boolean.class)      || true
    }

    def "less or equal"() {
        given:
            Expression<Boolean> expression = left.le(right)

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
            left                      | right                     | result
            DSL.constant(10)          | DSL.constant(10)          | true
            DSL.constant(10)          | DSL.constant(11)          | true
            DSL.constant(11)          | DSL.constant(10)          | false
            DSL.constant(10L)         | DSL.constant(10L)         | true
            DSL.constant(10L)         | DSL.constant(11L)         | true
            DSL.constant(11L)         | DSL.constant(10L)         | false
            DSL.constant((short) 10)  | DSL.constant((short) 10)  | true
            DSL.constant((short) 10)  | DSL.constant((short) 11)  | true
            DSL.constant((short) 11)  | DSL.constant((short) 10)  | false
            DSL.constant((byte) 10)   | DSL.constant((byte) 10)   | true
            DSL.constant((byte) 10)   | DSL.constant((byte) 11)   | true
            DSL.constant((byte) 11)   | DSL.constant((byte) 10)   | false
            DSL.constant(10.1F)       | DSL.constant(10.1F)       | true
            DSL.constant(10.1F)       | DSL.constant(11.1F)       | true
            DSL.constant(11.1F)       | DSL.constant(10.1F)       | false
            DSL.constant(10.1D)       | DSL.constant(10.1D)       | true
            DSL.constant(10.1D)       | DSL.constant(11.1D)       | true
            DSL.constant(11.1D)       | DSL.constant(10.1D)       | false
            DSL.constant('a' as char) | DSL.constant('a' as char) | true
            DSL.constant('a' as char) | DSL.constant('b' as char) | true
            DSL.constant('b' as char) | DSL.constant('a' as char) | false
            DSL.constant('aaa')       | DSL.constant('aaa')       | true
            DSL.constant('aaa')       | DSL.constant('bbb')       | true
            DSL.constant('bbb')       | DSL.constant('aaa')       | false
    }

    def "less than"() {
        given:
            Expression<Boolean> expression = left.lt(right)

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(expression) == result

        where:
            left                      | right                     | result
            DSL.constant(10)          | DSL.constant(10)          | false
            DSL.constant(10)          | DSL.constant(11)          | true
            DSL.constant(11)          | DSL.constant(10)          | false
            DSL.constant(10L)         | DSL.constant(10L)         | false
            DSL.constant(10L)         | DSL.constant(11L)         | true
            DSL.constant(11L)         | DSL.constant(10L)         | false
            DSL.constant((short) 10)  | DSL.constant((short) 10)  | false
            DSL.constant((short) 10)  | DSL.constant((short) 11)  | true
            DSL.constant((short) 11)  | DSL.constant((short) 10)  | false
            DSL.constant((byte) 10)   | DSL.constant((byte) 10)   | false
            DSL.constant((byte) 10)   | DSL.constant((byte) 11)   | true
            DSL.constant((byte) 11)   | DSL.constant((byte) 10)   | false
            DSL.constant(10.1F)       | DSL.constant(10.1F)       | false
            DSL.constant(10.1F)       | DSL.constant(11.1F)       | true
            DSL.constant(11.1F)       | DSL.constant(10.1F)       | false
            DSL.constant(10.1D)       | DSL.constant(10.1D)       | false
            DSL.constant(10.1D)       | DSL.constant(11.1D)       | true
            DSL.constant(11.1D)       | DSL.constant(10.1D)       | false
            DSL.constant('a' as char) | DSL.constant('a' as char) | false
            DSL.constant('a' as char) | DSL.constant('b' as char) | true
            DSL.constant('b' as char) | DSL.constant('a' as char) | false
            DSL.constant('aaa')       | DSL.constant('aaa')       | false
            DSL.constant('aaa')       | DSL.constant('bbb')       | true
            DSL.constant('bbb')       | DSL.constant('aaa')       | false
    }

    def "notBetween"() {
        when:
            Record record = session.select([condition]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(condition) == result

        where:
            condition                                                    || result
            DSL.constant(2).notBetween(1, 3)                             || false
            DSL.constant(2).notBetween(DSL.constant(1), 3)               || false
            DSL.constant(2).notBetween(1, DSL.constant(3))               || false
            DSL.constant(2).notBetween(DSL.constant(1), DSL.constant(3)) || false
            DSL.constant(2).notBetween(3, 1)                             || true
            DSL.constant(2L).notBetween(1L, 3L)                          || false
            DSL.constant(2L).notBetween(3L, 1L)                          || true
            DSL.constant(2.0).notBetween(1.0, 3.0)                       || false
            DSL.constant(2.0).notBetween(3.0, 1.0)                       || true
    }

    def "notBetweenSymmetric"() {
        when:
            Record record = session.select([condition]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(condition) == result

        where:
            condition                                                             || result
            DSL.constant(2).notBetweenSymmetric(1, 3)                             || false
            DSL.constant(2).notBetweenSymmetric(DSL.constant(1), 3)               || false
            DSL.constant(2).notBetweenSymmetric(1, DSL.constant(3))               || false
            DSL.constant(2).notBetweenSymmetric(DSL.constant(1), DSL.constant(3)) || false
            DSL.constant(2).notBetweenSymmetric(3, 1)                             || false
            DSL.constant(2).notBetweenSymmetric(3, 5)                             || true
            DSL.constant(2).notBetweenSymmetric(5, 3)                             || true
            DSL.constant(2L).notBetweenSymmetric(1L, 3L)                          || false
            DSL.constant(2L).notBetweenSymmetric(3L, 1L)                          || false
            DSL.constant(2L).notBetweenSymmetric(3L, 5L)                          || true
            DSL.constant(2L).notBetweenSymmetric(5L, 3L)                          || true
            DSL.constant(2.0).notBetweenSymmetric(1.0, 3.0)                       || false
            DSL.constant(2.0).notBetweenSymmetric(3.0, 1.0)                       || false
            DSL.constant(2.0).notBetweenSymmetric(3.0, 5.0)                       || true
            DSL.constant(2.0).notBetweenSymmetric(5.0, 3.0)                       || true
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
            left                      | right                     | result
            DSL.constant(10)          | DSL.constant(10)          | false
            DSL.constant(10)          | DSL.constant(11)          | true
            DSL.constant(11)          | DSL.constant(10)          | true
            DSL.constant(10L)         | DSL.constant(10L)         | false
            DSL.constant(10L)         | DSL.constant(11L)         | true
            DSL.constant(11L)         | DSL.constant(10L)         | true
            DSL.constant((short) 10)  | DSL.constant((short) 10)  | false
            DSL.constant((short) 10)  | DSL.constant((short) 11)  | true
            DSL.constant((short) 11)  | DSL.constant((short) 10)  | true
            DSL.constant((byte) 10)   | DSL.constant((byte) 10)   | false
            DSL.constant((byte) 10)   | DSL.constant((byte) 11)   | true
            DSL.constant((byte) 11)   | DSL.constant((byte) 10)   | true
            DSL.constant(10.1F)       | DSL.constant(10.1F)       | false
            DSL.constant(10.1F)       | DSL.constant(11.1F)       | true
            DSL.constant(11.1F)       | DSL.constant(10.1F)       | true
            DSL.constant(10.1D)       | DSL.constant(10.1D)       | false
            DSL.constant(10.1D)       | DSL.constant(11.1D)       | true
            DSL.constant(11.1D)       | DSL.constant(10.1D)       | true
            DSL.constant('a' as char) | DSL.constant('a' as char) | false
            DSL.constant('a' as char) | DSL.constant('b' as char) | true
            DSL.constant('b' as char) | DSL.constant('a' as char) | true
            DSL.constant('aaa')       | DSL.constant('aaa')       | false
            DSL.constant('aaa')       | DSL.constant('bbb')       | true
            DSL.constant('bbb')       | DSL.constant('aaa')       | true
    }

    def "select not"() {
        given:
            Expression<Boolean> expression = DSL.constant(param).not()

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).getClass() == Boolean.class
            record.get(1, Boolean.class) == result
            record.get(expression) == result

        where:
            param || result
            true  || false
            false || true
    }

    def "select not with alias"() {
        given:
            Expression<Boolean> expression = DSL.constant(param).not().as("my_alias")

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).getClass() == Boolean.class
            record.get(1, Boolean.class) == result
            record.get(expression) == result
            record.get("my_alias") == result
            record.get("my_alias", Boolean.class) == result

        where:
            param || result
            true  || false
            false || true
    }

    def "select double not"() {
        given:
            Expression<Boolean> expression = DSL.constant(param).not().not()

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).getClass() == Boolean.class
            record.get(1, Boolean.class) == result
            record.get(expression) == result

        where:
            param || result
            true  || true
            false || false
    }

    def "or"() {
        when:
            Record record = session.select([condition]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(condition) == result

        where:
            condition                               || result
            DSL.constant(true).or(true).or(true)    || true
            DSL.constant(false).or(true).or(true)   || true
            DSL.constant(true).or(false).or(true)   || true
            DSL.constant(true).or(true).or(false)   || true
            DSL.constant(false).or(false).or(false) || false
    }
}
