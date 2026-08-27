// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

class H2DialectSelectSubtractIT extends H2DialectBaseIT {

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
            value     | aliasedValue | expression               | type          || sum       | aliasedSum
            1         | 2            | DSL.constant(10)         | Integer.class || 9         | 8
            1L        | 2L           | DSL.constant(10L)        | Long.class    || 9L        | 8L
            (short) 1 | (short) 2    | DSL.constant((short) 10) | Short.class   || (short) 9 | (short) 8
            (byte) 1  | (byte) 2     | DSL.constant((byte) 10)  | Byte.class    || (byte) 9  | (byte) 8
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
    }
}
