// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

class H2DialectSelectAddIT extends H2DialectBaseIT {

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
            value     | aliasedValue | expression               | type          || add        | aliasedadd
            1         | 2            | DSL.constant(10)         | Integer.class || 11         | 12
            1L        | 2L           | DSL.constant(10L)        | Long.class    || 11L        | 12L
            (short) 1 | (short) 2    | DSL.constant((short) 10) | Short.class   || (short) 11 | (short) 12
            (byte) 1  | (byte) 2     | DSL.constant((byte) 10)  | Byte.class    || (byte) 11  | (byte) 12
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
    }
}
