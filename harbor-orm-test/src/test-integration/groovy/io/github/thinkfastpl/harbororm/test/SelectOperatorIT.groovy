// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

class SelectOperatorIT extends AbstractHarborIT {

    def "select eq ignore case"() {
        when:
            Boolean value = session.select(DSL.constant("abs").eqIgnoreCase("ABS")).fetchSingle()

        then:
            value
            value instanceof Boolean

        when:
            value = session.select(DSL.constant("abs").eqIgnoreCase("AB")).fetchSingle()

        then:
            !value
            value instanceof Boolean

        where:
            session << allSessions
    }

    def "select containsIgnoreCase"() {
        when:
            Boolean value = session.select(DSL.constant("abs").containsIgnoreCase("AB")).fetchSingle()

        then:
            value
            value instanceof Boolean

        when:
            value = session.select(DSL.constant("abs").containsIgnoreCase("ZZ")).fetchSingle()

        then:
            !value
            value instanceof Boolean

        where:
            session << allSessions
    }

    def "select DSL.containsIgnoreCase(Expression, String)"() {
        when:
            Boolean value = session.select(DSL.containsIgnoreCase(DSL.constant("abs"), "AB")).fetchSingle()

        then:
            value
            value instanceof Boolean

        when:
            value = session.select(DSL.containsIgnoreCase(DSL.constant("abs"), "ZZ")).fetchSingle()

        then:
            !value
            value instanceof Boolean

        where:
            session << allSessions
    }


    def "select between"() {
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
            DSL.constant(2).between(1, 3) || true
            DSL.constant(2).between(DSL.constant(1), 3) || true
            DSL.constant(2).between(1, DSL.constant(3)) || true
            DSL.constant(2).between(DSL.constant(1), DSL.constant(3)) || true
            DSL.constant(2).between(3, 1) || false
            DSL.constant(2L).between(1L, 3L) || true
            DSL.constant(2L).between(3L, 1L) || false
            DSL.constant(2.0).between(1.0, 3.0) || true
            DSL.constant(2.0).between(3.0, 1.0) || false
    }

    def "select not between"() {
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
            DSL.constant(2).notBetween(1, 3) || false
            DSL.constant(2).notBetween(DSL.constant(1), 3) || false
            DSL.constant(2).notBetween(1, DSL.constant(3)) || false
            DSL.constant(2).notBetween(DSL.constant(1), DSL.constant(3)) || false
            DSL.constant(2).notBetween(3, 1) || true
            DSL.constant(2L).notBetween(1L, 3L) || false
            DSL.constant(2L).notBetween(3L, 1L) || true
            DSL.constant(2.0).notBetween(1.0, 3.0) || false
            DSL.constant(2.0).notBetween(3.0, 1.0) || true
    }

    def "select between symmetric"() {
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
            DSL.constant(2).betweenSymmetric(1, 3) || true
            DSL.constant(2).betweenSymmetric(3, 5) || false
            DSL.constant(2).betweenSymmetric(DSL.constant(1), 3) || true
            DSL.constant(2).betweenSymmetric(1, DSL.constant(3)) || true
            DSL.constant(2).betweenSymmetric(DSL.constant(1), DSL.constant(3)) || true
            DSL.constant(2).betweenSymmetric(3, 1) || true
            DSL.constant(2L).betweenSymmetric(1L, 3L) || true
            DSL.constant(2L).betweenSymmetric(3L, 1L) || true
            DSL.constant(2L).betweenSymmetric(3L, 5L) || false
            DSL.constant(2.0).betweenSymmetric(1.0, 3.0) || true
            DSL.constant(2.0).betweenSymmetric(3.0, 1.0) || true
            DSL.constant(2.0).betweenSymmetric(3.0, 5.0) || false
    }

    def "select not between symmetric"() {
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
            DSL.constant(2).notBetweenSymmetric(1, 3) || false
            DSL.constant(2).notBetweenSymmetric(DSL.constant(1), 3) || false
            DSL.constant(2).notBetweenSymmetric(1, DSL.constant(3)) || false
            DSL.constant(2).notBetweenSymmetric(DSL.constant(1), DSL.constant(3)) || false
            DSL.constant(2).notBetweenSymmetric(3, 1) || false
            DSL.constant(2).notBetweenSymmetric(3, 5) || true
            DSL.constant(2).notBetweenSymmetric(5, 3) || true
            DSL.constant(2L).notBetweenSymmetric(1L, 3L) || false
            DSL.constant(2L).notBetweenSymmetric(3L, 1L) || false
            DSL.constant(2L).notBetweenSymmetric(3L, 5L) || true
            DSL.constant(2L).notBetweenSymmetric(5L, 3L) || true
            DSL.constant(2.0).notBetweenSymmetric(1.0, 3.0) || false
            DSL.constant(2.0).notBetweenSymmetric(3.0, 1.0) || false
            DSL.constant(2.0).notBetweenSymmetric(3.0, 5.0) || true
            DSL.constant(2.0).notBetweenSymmetric(5.0, 3.0) || true
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
            session << allSessions
        combined:
            left | right | result
            DSL.constant(10) | DSL.constant(10) | false
            DSL.constant(10) | DSL.constant(11) | true
            DSL.constant(11) | DSL.constant(10) | true
            DSL.constant(11) | DSL.constant((Integer) null) | true
            DSL.constant(10L) | DSL.constant(10L) | false
            DSL.constant(10L) | DSL.constant(11L) | true
            DSL.constant(11L) | DSL.constant(10L) | true
            DSL.constant(11L) | DSL.constant((Long) null) | true
            DSL.constant((short) 10) | DSL.constant((short) 10) | false
            DSL.constant((short) 10) | DSL.constant((short) 11) | true
            DSL.constant((short) 11) | DSL.constant((short) 10) | true
            DSL.constant((short) 11) | DSL.constant((Short) null) | true
            DSL.constant((byte) 10) | DSL.constant((byte) 10) | false
            DSL.constant((byte) 10) | DSL.constant((byte) 11) | true
            DSL.constant((byte) 11) | DSL.constant((byte) 10) | true
            DSL.constant((byte) 11) | DSL.constant((Byte) null) | true
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
            session << allSessions
        combined:
            left | right | result
            DSL.constant(10) | DSL.constant(10) | true
            DSL.constant(10) | DSL.constant(11) | false
            DSL.constant(11) | DSL.constant(10) | false
            DSL.constant(11) | DSL.constant((Integer) null) | false
            DSL.constant(10L) | DSL.constant(10L) | true
            DSL.constant(10L) | DSL.constant(11L) | false
            DSL.constant(11L) | DSL.constant(10L) | false
            DSL.constant(11L) | DSL.constant((Long) null) | false
            DSL.constant((short) 10) | DSL.constant((short) 10) | true
            DSL.constant((short) 10) | DSL.constant((short) 11) | false
            DSL.constant((short) 11) | DSL.constant((short) 10) | false
            DSL.constant((short) 11) | DSL.constant((Short) null) | false
            DSL.constant((byte) 10) | DSL.constant((byte) 10) | true
            DSL.constant((byte) 10) | DSL.constant((byte) 11) | false
            DSL.constant((byte) 11) | DSL.constant((byte) 10) | false
            DSL.constant((byte) 11) | DSL.constant((Byte) null) | false
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
            session << allSessions

        combined:
            value || result
            DSL.constant(Boolean.TRUE) || false
            DSL.constant(Boolean.FALSE) || true
            DSL.nil(Boolean.class) || false
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
            session << allSessions

        combined:
            value || result
            DSL.constant(Boolean.TRUE) || true
            DSL.constant(Boolean.FALSE) || false
            DSL.nil(Boolean.class) || true
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
            session << allSessions

        combined:
            type || _
            Byte.class || _
            Short.class || _
            Integer.class || _
            Long.class || _
            Float.class || _
            Double.class || _
            Boolean.class || _
            Character.class || _
            String.class || _
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
            session << allSessions

        combined:
            valueExpression || _
            DSL.constant(10) || _
            DSL.constant(11L) || _
            DSL.constant((short) 12) || _
            DSL.constant((byte) 13) || _
            DSL.constant(14.1F) || _
            DSL.constant(15.2) || _
            DSL.constant(true) || _
            DSL.constant(false) || _
            DSL.constant('x' as char) || _
            DSL.constant('abc') || _
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
            session << allSessions

        combined:
            type || _
            String.class || _
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
            session << allSessions

        combined:
            valueExpression || _
            DSL.constant(10) || _
            DSL.constant(11L) || _
            DSL.constant((short) 12) || _
            DSL.constant((byte) 13) || _
            DSL.constant(14.1F) || _
            DSL.constant(15.2) || _
            DSL.constant(true) || _
            DSL.constant(false) || _
            DSL.constant('x' as char) || _
            DSL.constant('abc') || _
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
            session << allSessions

        combined:
            value || result
            DSL.constant(Boolean.TRUE) || true
            DSL.constant(Boolean.FALSE) || false
            DSL.nil(Boolean.class) || false
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
            session << allSessions

        combined:
            value || result
            DSL.constant(Boolean.TRUE) || false
            DSL.constant(Boolean.FALSE) || true
            DSL.nil(Boolean.class) || true
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
            session << allSessions

        combined:
            value || result
            DSL.constant(Boolean.TRUE) || false
            DSL.constant(Boolean.FALSE) || false
            DSL.nil(Boolean.class) || true
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
            session << allSessions

        combined:
            value || result
            DSL.constant(Boolean.TRUE) || true
            DSL.constant(Boolean.FALSE) || true
            DSL.nil(Boolean.class) || false
    }

    def "add two constant whole numbers"() {
        given:
            Expression<?> addExpression = expression.add(value)
            Expression<?> aliasedaddExpression = expression.add(aliasedValue).as("my_alias")

        when:
            List<Record> records = session.select(addExpression, aliasedaddExpression).fetchAll()

        then:
            records.size() == 1
            with(records.get(0)) { r ->
                r.get(1) == add
                r.get(1).getClass() == type
                r.get(addExpression) == add
                r.get(addExpression).getClass() == type

                r.get(2) == aliasedadd
                r.get(2).getClass() == type
                r.get(aliasedaddExpression) == aliasedadd
                r.get(aliasedaddExpression).getClass() == type
                r.get("my_alias") == aliasedadd
                r.get("my_alias").getClass() == type
            }

        where:
            session << allSessions

        combined:
            value | aliasedValue | expression | type || add | aliasedadd
            1 | 2 | DSL.constant(10) | Integer.class || 11 | 12
            1L | 2L | DSL.constant(10L) | Long.class || 11L | 12L
            (short) 1 | (short) 2 | DSL.constant((short) 10) | Short.class || (short) 11 | (short) 12
            (byte) 1 | (byte) 2 | DSL.constant((byte) 10) | Byte.class || (byte) 11 | (byte) 12
    }

    def "add two constant floats"() {
        given:
            Expression<Float> addExpression = DSL.constant(10.1F).add(1.1F)
            Expression<Float> aliasedaddExpression = DSL.constant(10.1F).add(2.2F).as("my_alias")
            float error = 0.001F

        when:
            List<Record> records = session.select(addExpression, aliasedaddExpression).fetchAll()

        then:
            records.size() == 1
            Record r = records[0]
            r.get(1).class == Float.class
            that((Float) r.get(1), closeTo(11.2F, error))
            r.get(addExpression).getClass() == Float.class
            that(r.get(addExpression), closeTo(11.2F, error))

            r.get(2).class == Float.class
            that((Float) r.get(2), closeTo(12.3F, error))
            that(r.get(aliasedaddExpression), closeTo(12.3F, error))
            r.get(aliasedaddExpression).getClass() == Float.class
            r.get("my_alias").class == Float.class
            that((Float) r.get("my_alias"), closeTo(12.3F, error))

        where:
            session << allSessions
    }

    def "add two constant doubles"() {
        given:
            Expression<Double> addExpression = DSL.constant(10.1D).add(1.1D)
            Expression<Double> aliasedaddExpression = DSL.constant(10.1D).add(2.2D).as("my_alias")
            double error = 0.001D

        when:
            List<Record> records = session.select(addExpression, aliasedaddExpression).fetchAll()

        then:
            records.size() == 1
            Record r = records[0]
            r.get(1).class == Double.class
            that((Double) r.get(1), closeTo(11.2D, error))
            r.get(addExpression).getClass() == Double.class
            that(r.get(addExpression), closeTo(11.2D, error))

            r.get(2).class == Double.class
            that((Double) r.get(2), closeTo(12.3D, error))
            that(r.get(aliasedaddExpression), closeTo(12.3D, error))
            r.get(aliasedaddExpression).getClass() == Double.class
            r.get("my_alias").class == Double.class
            that((Double) r.get("my_alias"), closeTo(12.3D, error))

        where:
            session << allSessions
    }

    def "subtract two constant whole numbers"() {
        given:
            Expression<?> sumExpression = expression.subtract(value)
            Expression<?> aliasedSumExpression = expression.subtract(aliasedValue).as("my_alias")

        when:
            List<Record> records = session.select(sumExpression, aliasedSumExpression).fetchAll()

        then:
            records.size() == 1
            with(records.get(0)) { r ->
                r.get(1) == sum
                r.get(1).getClass() == type
                r.get(sumExpression) == sum
                r.get(sumExpression).getClass() == type

                r.get(2) == aliasedSum
                r.get(2).getClass() == type
                r.get(aliasedSumExpression) == aliasedSum
                r.get(aliasedSumExpression).getClass() == type
                r.get("my_alias") == aliasedSum
                r.get("my_alias").getClass() == type
            }

        where:
            session << allSessions

        combined:
            value | aliasedValue | expression | type || sum | aliasedSum
            1 | 2 | DSL.constant(10) | Integer.class || 9 | 8
            1L | 2L | DSL.constant(10L) | Long.class || 9L | 8L
            (short) 1 | (short) 2 | DSL.constant((short) 10) | Short.class || (short) 9 | (short) 8
            (byte) 1 | (byte) 2 | DSL.constant((byte) 10) | Byte.class || (byte) 9 | (byte) 8
    }

    def "subtract two constant floats"() {
        given:
            Expression<Float> sumExpression = DSL.constant(10.1F).subtract(1.2F)
            Expression<Float> aliasedSumExpression = DSL.constant(10.1F).subtract(2.2F).as("my_alias")
            float error = 0.001F

        when:
            List<Record> records = session.select(sumExpression, aliasedSumExpression).fetchAll()

        then:
            records.size() == 1
            Record r = records[0]
            r.get(1).class == Float.class
            that((Float) r.get(1), closeTo(8.9F, error))
            r.get(sumExpression).getClass() == Float.class
            that(r.get(sumExpression), closeTo(8.9F, error))

            r.get(2).class == Float.class
            that((Float) r.get(2), closeTo(7.9F, error))
            that(r.get(aliasedSumExpression), closeTo(7.9F, error))
            r.get(aliasedSumExpression).getClass() == Float.class
            r.get("my_alias").class == Float.class
            that((Float) r.get("my_alias"), closeTo(7.9F, error))

        where:
            session << allSessions
    }

    def "subtract two constant doubles"() {
        given:
            Expression<Double> sumExpression = DSL.constant(10.1D).subtract(1.2D)
            Expression<Double> aliasedSumExpression = DSL.constant(10.1D).subtract(2.2D).as("my_alias")
            double error = 0.001D

        when:
            List<Record> records = session.select(sumExpression, aliasedSumExpression).fetchAll()

        then:
            records.size() == 1
            Record r = records[0]
            r.get(1).class == Double.class
            that((Double) r.get(1), closeTo(8.9D, error))
            r.get(sumExpression).getClass() == Double.class
            that(r.get(sumExpression), closeTo(8.9D, error))

            r.get(2).class == Double.class
            that((Double) r.get(2), closeTo(7.9D, error))
            that(r.get(aliasedSumExpression), closeTo(7.9D, error))
            r.get(aliasedSumExpression).getClass() == Double.class
            r.get("my_alias").class == Double.class
            that((Double) r.get("my_alias"), closeTo(7.9D, error))

        where:
            session << allSessions
    }

    def "multiply two constant whole numbers"() {
        given:
            Expression<?> sumExpression = expression.multiply(value)
            Expression<?> aliasedSumExpression = expression.multiply(aliasedValue).as("my_alias")

        when:
            List<Record> records = session.select(sumExpression, aliasedSumExpression).fetchAll()

        then:
            records.size() == 1
            with(records.get(0)) { r ->
                r.get(1) == result
                r.get(1).getClass() == type
                r.get(sumExpression) == result
                r.get(sumExpression).getClass() == type

                r.get(2) == aliasedResult
                r.get(2).getClass() == type
                r.get(aliasedSumExpression) == aliasedResult
                r.get(aliasedSumExpression).getClass() == type
                r.get("my_alias") == aliasedResult
                r.get("my_alias").getClass() == type
            }

        where:
            session << allSessions

        combined:
            value | aliasedValue | expression | type || result | aliasedResult
            2 | 3 | DSL.constant(10) | Integer.class || 20 | 30
            2L | 3L | DSL.constant(10L) | Long.class || 20L | 30L
            (short) 2 | (short) 3 | DSL.constant((short) 10) | Short.class || (short) 20 | (short) 30
            (byte) 2 | (byte) 3 | DSL.constant((byte) 10) | Byte.class || (byte) 20 | (byte) 30
    }

    def "multiply two constant floats"() {
        given:
            Expression<Float> sumExpression = DSL.constant(10.1F).multiply(2.2F)
            Expression<Float> aliasedSumExpression = DSL.constant(10.1F).multiply(3.3F).as("my_alias")
            float error = 0.001F

        when:
            List<Record> records = session.select(sumExpression, aliasedSumExpression).fetchAll()

        then:
            records.size() == 1
            Record r = records[0]
            r.get(1).class == Float.class
            that((Float) r.get(1), closeTo(22.22F, error))
            r.get(sumExpression).getClass() == Float.class
            that(r.get(sumExpression), closeTo(22.22F, error))

            r.get(2).class == Float.class
            that((Float) r.get(2), closeTo(33.33F, error))
            that(r.get(aliasedSumExpression), closeTo(33.33F, error))
            r.get(aliasedSumExpression).getClass() == Float.class
            r.get("my_alias").class == Float.class
            that((Float) r.get("my_alias"), closeTo(33.33F, error))

        where:
            session << allSessions
    }

    def "multiply two constant doubles"() {
        given:
            Expression<Double> sumExpression = DSL.constant(10.1D).multiply(2.2D)
            Expression<Double> aliasedSumExpression = DSL.constant(10.1D).multiply(3.3D).as("my_alias")
            double error = 0.001D

        when:
            List<Record> records = session.select(sumExpression, aliasedSumExpression).fetchAll()

        then:
            records.size() == 1
            Record r = records[0]
            r.get(1).class == Double.class
            that((Double) r.get(1), closeTo(22.22D, error))
            r.get(sumExpression).getClass() == Double.class
            that(r.get(sumExpression), closeTo(22.22D, error))

            r.get(2).class == Double.class
            that((Double) r.get(2), closeTo(33.33D, error))
            that(r.get(aliasedSumExpression), closeTo(33.33D, error))
            r.get(aliasedSumExpression).getClass() == Double.class
            r.get("my_alias").class == Double.class
            that((Double) r.get("my_alias"), closeTo(33.33D, error))

        where:
            session << allSessions
    }

    def "divide two constant whole numbers"() {
        given:
            Expression<?> sumExpression = expression.divide(value)
            Expression<?> aliasedSumExpression = expression.divide(aliasedValue).as("my_alias")

        when:
            List<Record> records = session.select(sumExpression, aliasedSumExpression).fetchAll()

        then:
            records.size() == 1
            with(records.get(0)) { r ->
                r.get(1) == result
                r.get(1).getClass() == type
                r.get(sumExpression) == result
                r.get(sumExpression).getClass() == type

                r.get(2) == aliasedResult
                r.get(2).getClass() == type
                r.get(aliasedSumExpression) == aliasedResult
                r.get(aliasedSumExpression).getClass() == type
                r.get("my_alias") == aliasedResult
                r.get("my_alias").getClass() == type
            }

        where:
            session << allSessions

        combined:
            value | aliasedValue | expression | type || result | aliasedResult
            2 | 3 | DSL.constant(6) | Integer.class || 3 | 2
            2L | 3L | DSL.constant(6L) | Long.class || 3L | 2L
            (short) 2 | (short) 3 | DSL.constant((short) 6) | Short.class || (short) 3 | (short) 2
            (byte) 2 | (byte) 3 | DSL.constant((byte) 6) | Byte.class || (byte) 3 | (byte) 2
    }

    def "divide two constant floats"() {
        given:
            Expression<Float> sumExpression = DSL.constant(66.66F).divide(2.2F)
            Expression<Float> aliasedSumExpression = DSL.constant(66.66F).divide(3.3F).as("my_alias")
            float error = 0.001F

        when:
            List<Record> records = session.select(sumExpression, aliasedSumExpression).fetchAll()

        then:
            records.size() == 1
            Record r = records[0]
            r.get(1).class == Float.class
            that((Float) r.get(1), closeTo(30.3F, error))
            r.get(sumExpression).getClass() == Float.class
            that(r.get(sumExpression), closeTo(30.3F, error))

            r.get(2).class == Float.class
            that((Float) r.get(2), closeTo(20.2F, error))
            that(r.get(aliasedSumExpression), closeTo(20.2F, error))
            r.get(aliasedSumExpression).getClass() == Float.class
            r.get("my_alias").class == Float.class
            that((Float) r.get("my_alias"), closeTo(20.2F, error))

        where:
            session << allSessions
    }

    def "divide two constant doubles"() {
        given:
            Expression<Double> sumExpression = DSL.constant(66.66D).divide(2.2D)
            Expression<Double> aliasedSumExpression = DSL.constant(66.66D).divide(3.3D).as("my_alias")
            double error = 0.001D

        when:
            List<Record> records = session.select(sumExpression, aliasedSumExpression).fetchAll()

        then:
            records.size() == 1
            Record r = records[0]
            r.get(1).class == Double.class
            that((Double) r.get(1), closeTo(30.3D, error))
            r.get(sumExpression).getClass() == Double.class
            that(r.get(sumExpression), closeTo(30.3D, error))

            r.get(2).class == Double.class
            that((Double) r.get(2), closeTo(20.2D, error))
            that(r.get(aliasedSumExpression), closeTo(20.2D, error))
            r.get(aliasedSumExpression).getClass() == Double.class
            r.get("my_alias").class == Double.class
            that((Double) r.get("my_alias"), closeTo(20.2D, error))

        where:
            session << allSessions
    }

    def "complex boolean expression"() {
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
            DSL.constant(true).and(DSL.constant(true).or(false)) || true
            DSL.constant(true).and(DSL.constant(true).or(false)).not() || false
            DSL.constant(true).and(false).or(true) || true
            DSL.constant(true).and(false).or(false) || false
            DSL.constant(true).or(DSL.constant(true).and(false)) || true
            DSL.constant(false).or(DSL.constant(true).and(false)) || false
    }

    def "case when then else"() {
        when:
            Boolean result = session
                    .select(
                            DSL
                                    .caseWhen(left.eq(right), DSL.TRUE)
                                    .else_(DSL.FALSE)
                    )
                    .fetchSingle()

        then:
            result == expectedResult

        where:
            session << allSessions

        combined:
            left | right || expectedResult
            DSL.constant(1) | DSL.constant(1) || true
            DSL.constant(1) | DSL.constant(2) || false
    }

    def "case when then"() {
        when:
            Boolean result = session
                    .select(
                            DSL
                                    .caseWhen(left.eq(right), DSL.TRUE).end()
                    )
                    .fetchSingle()

        then:
            result == expectedResult

        where:
            session << allSessions

        combined:
            left | right || expectedResult
            DSL.constant(1) | DSL.constant(1) || true
            DSL.constant(1) | DSL.constant(2) || null
    }

    def "case when then multiple"() {
        when:
            Integer result = session
                    .select(
                            DSL
                                    .caseWhen(left.eq(DSL.constant(1)), DSL.constant(11))
                                    .whenThen(left.eq(DSL.constant(2)), DSL.constant(22))
                                    .whenThen(left.eq(DSL.constant(3)), DSL.constant(33))
                                    .end()
                    )
                    .fetchSingle()

        then:
            result == expectedResult

        where:
            session << allSessions

        combined:
            left | expectedResult
            DSL.constant(1) | 11
            DSL.constant(2) | 22
            DSL.constant(3) | 33
            DSL.constant(4) | null

    }

    def "case when then null"() {
        when:
            Integer result = session
                    .select(
                            DSL
                                    .caseWhen(left.eq(DSL.constant(1)), DSL.constant(11))
                                    .whenThen(left.eq(DSL.constant(2)), null)
                                    .else_(DSL.constant(99))
                    )
                    .fetchSingle()

        then:
            result == expectedResult

        where:
            session << allSessions

        combined:
            left              || expectedResult
            DSL.constant(1)   || 11
            DSL.constant(2)   || null
            DSL.constant(3)   || 99
    }

    def "case when else null"() {
        when:
            Integer result = session
                    .select(
                            DSL
                                    .caseWhen(left.eq(DSL.constant(1)), DSL.constant(11))
                                    .else_(null)
                    )
                    .fetchSingle()

        then:
            result == expectedResult

        where:
            session << allSessions

        combined:
            left              || expectedResult
            DSL.constant(1)   || 11
            DSL.constant(2)   || null
    }
}
