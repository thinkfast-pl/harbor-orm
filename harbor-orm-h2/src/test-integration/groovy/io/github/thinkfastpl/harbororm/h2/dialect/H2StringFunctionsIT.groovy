// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL

class H2StringFunctionsIT extends H2DialectBaseIT {

    def "SUBSTRING extracts substring from start"() {
        expect:
            session.select(DSL.substring("Hello World", 7)).fetchSingle() == "World"
    }

    def "SUBSTRING extracts substring with length"() {
        expect:
            session.select(DSL.substring("Hello World", 1, 5)).fetchSingle() == "Hello"
    }

    def "RIGHT returns rightmost characters"() {
        expect:
            session.select(DSL.right("Hello World", 5)).fetchSingle() == "World"
    }

    def "LTRIM removes leading whitespace"() {
        expect:
            session.select(DSL.ltrim("   Hello")).fetchSingle() == "Hello"
    }

    def "RTRIM removes trailing whitespace"() {
        expect:
            session.select(DSL.rtrim("Hello   ")).fetchSingle() == "Hello"
    }

    def "LPAD pads string on left"() {
        expect:
            session.select(DSL.lpad("42", 5, "0")).fetchSingle() == "00042"
    }

    def "RPAD pads string on right"() {
        expect:
            session.select(DSL.rpad("Hi", 5, "!")).fetchSingle() == "Hi!!!"
    }

    def "POSITION finds substring"() {
        expect:
            session.select(DSL.position("World", "Hello World")).fetchSingle() == 7
    }

    def "POSITION returns 0 when not found"() {
        expect:
            session.select(DSL.position("xyz", "Hello World")).fetchSingle() == 0
    }

    def "REPEAT repeats string"() {
        expect:
            session.select(DSL.repeat("ab", 3)).fetchSingle() == "ababab"
    }

    // Note: H2 does not support REVERSE function
}
