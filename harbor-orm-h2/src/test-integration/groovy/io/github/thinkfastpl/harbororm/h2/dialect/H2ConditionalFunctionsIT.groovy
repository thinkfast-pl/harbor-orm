// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL

class H2ConditionalFunctionsIT extends H2DialectBaseIT {

    def "NULLIF returns null when equal"() {
        expect:
            session.select(DSL.nullif(DSL.constant(1), DSL.constant(1))).fetchSingle() == null
    }

    def "NULLIF returns first value when not equal"() {
        expect:
            session.select(DSL.nullif(DSL.constant(1), DSL.constant(2))).fetchSingle() == 1
    }

    def "IFNULL returns value when not null"() {
        expect:
            session.select(DSL.ifNull(DSL.constant("Hello"), "Default")).fetchSingle() == "Hello"
    }

    def "IFNULL returns default when null"() {
        expect:
            session.select(DSL.ifNull(DSL.nil(String), "Default")).fetchSingle() == "Default"
    }

    def "NVL returns value when not null"() {
        expect:
            session.select(DSL.nvl(DSL.constant("Hello"), "Default")).fetchSingle() == "Hello"
    }

    def "NVL returns default when null"() {
        expect:
            session.select(DSL.nvl(DSL.nil(String), "Default")).fetchSingle() == "Default"
    }
}
