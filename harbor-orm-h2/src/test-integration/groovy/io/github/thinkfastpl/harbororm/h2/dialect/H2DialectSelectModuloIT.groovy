// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

class H2DialectSelectModuloIT extends H2DialectBaseIT {

    def "modulo two constant whole numbers"() {
        given:
            Expression<?> sumExpression = expression.modulo(value)
            Expression<?> aliasedSumExpression = expression.modulo(aliasedValue).as("my_alias")

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
            value     | aliasedValue | expression               | type          || sum        | aliasedSum
            4         | 3            | DSL.constant(10)         | Integer.class || 2         | 1
            4L        | 3L           | DSL.constant(10L)        | Long.class    || 2L        | 1L
            (short) 4 | (short) 3    | DSL.constant((short) 10) | Short.class   || (short) 2 | (short) 1
            (byte) 4  | (byte) 3     | DSL.constant((byte) 10)  | Byte.class    || (byte) 2  | (byte) 1
    }

    def "modulo two constant whole floats"() {
        given:
            Expression<Float> sumExpression = DSL.constant(33.2F).modulo(6F)
            Expression<Float> aliasedSumExpression = DSL.constant(33.2F).modulo(6.1F).as("my_alias")
            float error = 0.001F

        when:
            List<Record> records = session.select(sumExpression, aliasedSumExpression).fetchAll()

        then:
            records.size() == 1
            Record r = records[0]
            r.get(1).class == Float.class
            that((Float) r.get(1), closeTo(3.2F, error))
            r.get(sumExpression).getClass() == Float.class
            that(r.get(sumExpression), closeTo(3.2F, error))

            r.get(2).class == Float.class
            that((Float) r.get(2), closeTo(2.7F, error))
            that(r.get(aliasedSumExpression), closeTo(2.7F, error))
            r.get(aliasedSumExpression).getClass() == Float.class
            r.get("my_alias").class == Float.class
            that((Float) r.get("my_alias"), closeTo(2.7F, error))
    }

    def "modulo two constant whole doubles"() {
        given:
            Expression<Double> sumExpression = DSL.constant(33.2D).modulo(6D)
            Expression<Double> aliasedSumExpression = DSL.constant(33.2D).modulo(6.1D).as("my_alias")
            Double error = 0.001D

        when:
            List<Record> records = session.select(sumExpression, aliasedSumExpression).fetchAll()

        then:
            records.size() == 1
            Record r = records[0]
            r.get(1).class == Double.class
            that((Double) r.get(1), closeTo(3.2D, error))
            r.get(sumExpression).getClass() == Double.class
            that(r.get(sumExpression), closeTo(3.2D, error))

            r.get(2).class == Double.class
            that((Double) r.get(2), closeTo(2.7D, error))
            that(r.get(aliasedSumExpression), closeTo(2.7D, error))
            r.get(aliasedSumExpression).getClass() == Double.class
            r.get("my_alias").class == Double.class
            that((Double) r.get("my_alias"), closeTo(2.7D, error))
    }
}
