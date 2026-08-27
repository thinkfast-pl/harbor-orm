// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL

class H2DialectFunctionPowerIT extends H2DialectBaseIT {

    def "2 power 3"() {
        when:
            Object result = session.select(DSL.power(left, right)).fetchSingle()

        then:
            result == 8.0
            result.class == BigDecimal.class

        where:
            left                    | right
            DSL.constant(2)         | DSL.constant(3)
            DSL.constant(2L)        | DSL.constant(3L)
            DSL.constant((short) 2) | DSL.constant((short) 3)
            DSL.constant((byte) 2)  | DSL.constant((byte) 3)
            DSL.constant(2F)        | DSL.constant(3F)
            DSL.constant(2D)        | DSL.constant(3D)
            DSL.constant(2.0)       | DSL.constant(3.0)
    }

    def "string functions"() {
        expect:
            session.select(DSL.lower("Asd")).fetchSingle() == "asd"
            session.select(DSL.upper("Asd")).fetchSingle() == "ASD"
            session.select(DSL.length("Asd")).fetchSingle() == 3
            session.select(DSL.trim(" Asd  ")).fetchSingle() == "Asd"
            session.select(DSL.constant("Asd").eqIgnoreCase("ASD")).fetchSingle()
            !session.select(DSL.constant("Asd").eqIgnoreCase("ASD2")).fetchSingle()
            session.select(DSL.constant(123).eqIgnoreCase("123")).fetchSingle()
            !session.select(DSL.constant(123).eqIgnoreCase("1234")).fetchSingle()
            session.select(DSL.concat(DSL.constant("a"), DSL.constant("b"), DSL.constant("c"))).fetchSingle() == "abc"
            session.select(DSL.replace("abca", "a", "d")).fetchSingle() == "dbcd"
    }

    def "containsIgnoreCase"() {
        expect:
            session.select(DSL.containsIgnoreCase("abc", "a")).fetchSingle()
            session.select(DSL.containsIgnoreCase("abc", "b")).fetchSingle()
            session.select(DSL.containsIgnoreCase("abc", "A")).fetchSingle()
            session.select(DSL.containsIgnoreCase("abc", "B")).fetchSingle()
            !session.select(DSL.containsIgnoreCase("abc", "d")).fetchSingle()
            !session.select(DSL.containsIgnoreCase("abc", "D")).fetchSingle()
    }
}
