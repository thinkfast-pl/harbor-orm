// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

class H2DialectSelectDivideIT extends H2DialectBaseIT {

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
            value     | aliasedValue | expression              | type          || result    | aliasedResult
            2         | 3            | DSL.constant(6)         | Integer.class || 3         | 2
            2L        | 3L           | DSL.constant(6L)        | Long.class    || 3L        | 2L
            (short) 2 | (short) 3    | DSL.constant((short) 6) | Short.class   || (short) 3 | (short) 2
            (byte) 2  | (byte) 3     | DSL.constant((byte) 6)  | Byte.class    || (byte) 3  | (byte) 2
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
    }
}
