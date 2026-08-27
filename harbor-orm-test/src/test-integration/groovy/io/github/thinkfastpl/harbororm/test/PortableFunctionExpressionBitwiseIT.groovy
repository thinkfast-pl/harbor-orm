// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL

class PortableFunctionExpressionBitwiseIT extends AbstractHarborIT {

    def "select bitAnd() with Long values"() {
        when:
            def result = session.select(DSL.bitAnd(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Long.class

        where:
            session << allSessions

        combined:
            x | y || expected
            12L | 10L || 8L
            255L | 15L || 15L
            0L | 0L || 0L
            0L | 123L || 0L
            123L | 0L || 0L
            0xFFL | 0x0FL || 0x0FL
            0b1100L | 0b1010L || 0b1000L
            255L | 255L || 255L
            1023L | 511L || 511L
    }

    def "select bitAnd() with Integer values preserves type"() {
        when:
            def result = session.select(DSL.bitAnd(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            x | y || expected
            12 | 10 || 8
            255 | 15 || 15
            0b1100 | 0b1010 || 0b1000
            255 | 255 || 255
    }

    def "select bitAnd() composes inside arithmetic"() {
        when:
            Long result = session.select(DSL.bitAnd(DSL.constant(12L), DSL.constant(10L)).add(DSL.constant(1L))).fetchSingle()

        then:
            result == 9L

        where:
            session << allSessions
    }

    def "select bitOr() with Long values"() {
        when:
            def result = session.select(DSL.bitOr(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Long.class

        where:
            session << allSessions

        combined:
            x | y || expected
            12L | 10L || 14L
            255L | 15L || 255L
            0L | 0L || 0L
            0L | 123L || 123L
            123L | 0L || 123L
            0xF0L | 0x0FL || 0xFFL
            0b1100L | 0b1010L || 0b1110L
            255L | 255L || 255L
            512L | 511L || 1023L
    }

    def "select bitOr() with Integer values preserves type"() {
        when:
            def result = session.select(DSL.bitOr(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            x | y || expected
            12 | 10 || 14
            255 | 15 || 255
            0b1100 | 0b1010 || 0b1110
            255 | 255 || 255
    }

    def "select bitOr() composes inside arithmetic"() {
        when:
            Long result = session.select(DSL.bitOr(DSL.constant(12L), DSL.constant(10L)).add(DSL.constant(1L))).fetchSingle()

        then:
            result == 15L

        where:
            session << allSessions
    }

    def "select bitGet() with Integer values"() {
        when:
            def result = session.select(DSL.bitGet(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            x | y || expected
            12 | 0 || 0
            12 | 1 || 0
            12 | 2 || 1
            12 | 3 || 1
            15 | 0 || 1
            15 | 3 || 1
            0 | 5 || 0
            1 | 0 || 1
            8 | 3 || 1
            255 | 7 || 1
            255 | 8 || 0
    }

    def "select bitGet() composes inside arithmetic"() {
        when:
            Integer result = session.select(DSL.bitGet(DSL.constant(12), DSL.constant(2)).add(DSL.constant(5))).fetchSingle()

        then:
            result == 6

        where:
            session << allSessions
    }

    def "select bitSet() with Integer values"() {
        when:
            def result = session.select(DSL.bitSet(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            x | y || expected
            0 | 0 || 1
            0 | 1 || 2
            0 | 3 || 8
            4 | 1 || 6
            4 | 2 || 4
            8 | 0 || 9
            12 | 0 || 13
            12 | 1 || 14
            12 | 4 || 28
            255 | 8 || 511
    }

    def "select bitSet() composes inside arithmetic"() {
        when:
            Integer result = session.select(DSL.bitSet(DSL.constant(4), DSL.constant(1)).add(DSL.constant(1))).fetchSingle()

        then:
            result == 7

        where:
            session << allSessions
    }

    def "select bitXor() with Long values"() {
        when:
            def result = session.select(DSL.bitXor(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Long.class

        where:
            session << allSessions

        combined:
            x | y || expected
            12L | 10L || 6L
            255L | 15L || 240L
            0L | 0L || 0L
            0L | 123L || 123L
            123L | 0L || 123L
            0xFFL | 0x0FL || 0xF0L
            0b1100L | 0b1010L || 0b0110L
            255L | 255L || 0L
            1023L | 511L || 512L
    }

    def "select bitXor() with Integer values preserves type"() {
        when:
            def result = session.select(DSL.bitXor(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            x | y || expected
            12 | 10 || 6
            255 | 15 || 240
            0b1100 | 0b1010 || 0b0110
            255 | 255 || 0
    }

    def "select bitXor() composes inside arithmetic"() {
        when:
            Long result = session.select(DSL.bitXor(DSL.constant(12L), DSL.constant(10L)).add(DSL.constant(1L))).fetchSingle()

        then:
            result == 7L

        where:
            session << allSessions
    }

    def "select bitNot() with Long values"() {
        when:
            def result = session.select(DSL.bitNot(x)).fetchSingle()

        then:
            result == expected
            result.getClass() == Long.class

        where:
            session << allSessions

        combined:
            x || expected
            0L || -1L
            -1L || 0L
            12L || -13L
            255L || -256L
            0xFFL || -256L
            0b1010L || -11L
            1023L || -1024L
    }

    def "select bitNot() with Integer values preserves type"() {
        when:
            def result = session.select(DSL.bitNot(x)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            x || expected
            0 || -1
            -1 || 0
            12 || -13
            255 || -256
            0b1010 || -11
    }

    def "select bitNot() composes inside arithmetic"() {
        when:
            Long result = session.select(DSL.bitNot(DSL.constant(12L)).add(DSL.constant(1L))).fetchSingle()

        then:
            result == -12L

        where:
            session << allSessions
    }

    def "select bitNand() with Long values"() {
        when:
            def result = session.select(DSL.bitNand(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Long.class

        where:
            session << allSessions

        combined:
            x | y || expected
            12L | 10L || -9L
            255L | 15L || -16L
            0L | 0L || -1L
            0L | 123L || -1L
            123L | 0L || -1L
            0xFFL | 0x0FL || -16L
            0b1100L | 0b1010L || -9L
            255L | 255L || -256L
            1023L | 511L || -512L
    }

    def "select bitNand() with Integer values preserves type"() {
        when:
            def result = session.select(DSL.bitNand(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            x | y || expected
            12 | 10 || -9
            255 | 15 || -16
            0b1100 | 0b1010 || -9
            255 | 255 || -256
    }

    def "select bitNand() composes inside arithmetic"() {
        when:
            Long result = session.select(DSL.bitNand(DSL.constant(12L), DSL.constant(10L)).add(DSL.constant(1L))).fetchSingle()

        then:
            result == -8L

        where:
            session << allSessions
    }

    def "select bitNor() with Long values"() {
        when:
            def result = session.select(DSL.bitNor(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Long.class

        where:
            session << allSessions

        combined:
            x | y || expected
            12L | 10L || -15L
            255L | 15L || -256L
            0L | 0L || -1L
            0L | 123L || -124L
            123L | 0L || -124L
            0xFFL | 0x0FL || -256L
            0b1100L | 0b1010L || -15L
            255L | 255L || -256L
            1023L | 511L || -1024L
    }

    def "select bitNor() with Integer values preserves type"() {
        when:
            def result = session.select(DSL.bitNor(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            x | y || expected
            12 | 10 || -15
            255 | 15 || -256
            0b1100 | 0b1010 || -15
            255 | 255 || -256
    }

    def "select bitNor() composes inside arithmetic"() {
        when:
            Long result = session.select(DSL.bitNor(DSL.constant(12L), DSL.constant(10L)).add(DSL.constant(1L))).fetchSingle()

        then:
            result == -14L

        where:
            session << allSessions
    }

    def "select bitXNor() with Long values"() {
        when:
            def result = session.select(DSL.bitXNor(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Long.class

        where:
            session << allSessions

        combined:
            x | y || expected
            12L | 10L || -7L
            255L | 15L || -241L
            0L | 0L || -1L
            0L | 123L || -124L
            123L | 0L || -124L
            0xFFL | 0x0FL || -241L
            0b1100L | 0b1010L || -7L
            255L | 255L || -1L
            1023L | 511L || -513L
    }

    def "select bitXNor() with Integer values preserves type"() {
        when:
            def result = session.select(DSL.bitXNor(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            x | y || expected
            12 | 10 || -7
            255 | 15 || -241
            0b1100 | 0b1010 || -7
            255 | 255 || -1
    }

    def "select bitXNor() composes inside arithmetic"() {
        when:
            Long result = session.select(DSL.bitXNor(DSL.constant(12L), DSL.constant(10L)).add(DSL.constant(1L))).fetchSingle()

        then:
            result == -6L

        where:
            session << allSessions
    }

    def "select shl() with Long values"() {
        when:
            def result = session.select(DSL.shl(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Long.class

        where:
            session << allSessions

        combined:
            x | y || expected
            1L | 0 || 1L
            1L | 1 || 2L
            1L | 3 || 8L
            5L | 2 || 20L
            255L | 1 || 510L
            0L | 5 || 0L
            1024L | 10 || 1048576L
    }

    def "select shl() with Integer values preserves type"() {
        when:
            def result = session.select(DSL.shl(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            x | y || expected
            1 | 0 || 1
            1 | 3 || 8
            5 | 2 || 20
            0b10 | 3 || 16
            255 | 1 || 510
    }

    def "select shl() composes inside arithmetic"() {
        when:
            Long result = session.select(DSL.shl(DSL.constant(4L), DSL.constant(1)).add(DSL.constant(1L))).fetchSingle()

        then:
            result == 9L

        where:
            session << allSessions
    }

    def "select shr() with Long values"() {
        when:
            def result = session.select(DSL.shr(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Long.class

        where:
            session << allSessions

        combined:
            x | y || expected
            1L | 0 || 1L
            1L | 1 || 0L
            8L | 3 || 1L
            20L | 2 || 5L
            510L | 1 || 255L
            0L | 5 || 0L
            1048576L | 10 || 1024L
    }

    def "select shr() with Integer values preserves type"() {
        when:
            def result = session.select(DSL.shr(x, y)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            x | y || expected
            1 | 0 || 1
            1 | 3 || 0
            20 | 2 || 5
            0b10000 | 3 || 2
            510 | 1 || 255
    }

    def "select shr() composes inside arithmetic"() {
        when:
            Long result = session.select(DSL.shr(DSL.constant(4L), DSL.constant(1)).add(DSL.constant(1L))).fetchSingle()

        then:
            result == 3L

        where:
            session << allSessions
    }
}
