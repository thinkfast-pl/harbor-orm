// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

class H2DialectSelectMultiplyIT extends H2DialectBaseIT {

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
            value     | aliasedValue | expression               | type          || result     | aliasedResult
            2         | 3            | DSL.constant(10)         | Integer.class || 20         | 30
            2L        | 3L           | DSL.constant(10L)        | Long.class    || 20L        | 30L
            (short) 2 | (short) 3    | DSL.constant((short) 10) | Short.class   || (short) 20 | (short) 30
            (byte) 2  | (byte) 3     | DSL.constant((byte) 10)  | Byte.class    || (byte) 20  | (byte) 30
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
    }
}
