// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class PortableFunctionExpressionTextIT extends AbstractHarborIT {

    def "select ascii()"() {
        when:
            def result = session.select(DSL.ascii(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            value || expected
            'x' || 120
            'a' || 97
            'A' || 65
            'z' || 122
            'Z' || 90
            '0' || 48
            '9' || 57
            ' ' || 32
    }

    def "select bitLength()"() {
        when:
            def result = session.select(DSL.bitLength(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            value || expected
            'jose' || 32
            'hello' || 40
            'a' || 8
            'ab' || 16
            'test' || 32
            '' || 0
            ' ' || 8
            '12345' || 40
            'Hello World' || 88
    }

    def "select btrim() with characters"() {
        when:
            def result = session.select(DSL.btrim(string, characters)).fetchSingle()

        then:
            result == expected
            result.getClass() == String.class

        where:
            session << allSessions

        combined:
            string | characters || expected
            'abcdefghi' | 'abc' || 'defghi'
            'xyzHelloxyz' | 'xyz' || 'Hello'
            '###test###' | '#' || 'test'
            '...word...' | '.' || 'word'
            'nochange' | 'xyz' || 'nochange'
    }

    def "select btrim() with character set"() {
        when:
            def result = session.select(DSL.btrim(string, characters)).fetchSingle()

        then:
            result == expected
            result.getClass() == String.class

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)

        combined:
            string | characters || expected
            'xyxtrimyyx' | 'xyz' || 'trim'
            'aaabbbccc' | 'ac' || 'bbb'
            '123data321' | '123' || 'data'
    }

    def "select btrim() without characters"() {
        when:
            def result = session.select(DSL.btrim(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == String.class

        where:
            session << allSessions

        combined:
            value || expected
            '   trim   ' || 'trim'
            '  hello' || 'hello'
            'world  ' || 'world'
            '   test   spaces   ' || 'test   spaces'
            'noSpaces' || 'noSpaces'
            '  ' || ''
            ' a ' || 'a'
    }

    def "select chr()"() {
        when:
            def result = session.select(DSL.chr(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == String.class

        where:
            session << allSessions

        combined:
            value || expected
            65 || 'A'
            97 || 'a'
            120 || 'x'
            122 || 'z'
            90 || 'Z'
            48 || '0'
            57 || '9'
            32 || ' '
    }

    def "select concat()"() {
        when:
            def result = session.select(DSL.concat(expressions as Expression<String>[])).fetchSingle()

        then:
            result == expected
            result.getClass() == String.class

        where:
            session << allSessions

        combined:
            expressions || expected
            [] || ''
            [DSL.constant('Hello'), DSL.constant(' '), DSL.constant('World')] || 'Hello World'
            [DSL.constant('A'), DSL.constant('B'), DSL.constant('C')] || 'ABC'
            [DSL.constant('Hello')] || 'Hello'
            [DSL.constant(''), DSL.constant('test')] || 'test'
            [DSL.constant('foo'), DSL.constant(''), DSL.constant('bar')] || 'foobar'
            [DSL.constant('1'), DSL.constant('2'), DSL.constant('3'), DSL.constant('4')] || '1234'
    }

    def "select concatWs()"() {
        when:
            def result = session.select(DSL.concatWs(separator, values as Expression<String>[])).fetchSingle()

        then:
            result == expected
            result.getClass() == String.class

        where:
            session << allSessions

        combined:
            separator | values || expected
            ',' | [DSL.constant('abcde'), DSL.constant('2'), DSL.constant('22')] || 'abcde,2,22'
            ',' | [DSL.constant('A'), DSL.constant('B'), DSL.constant('C')] || 'A,B,C'
            ' ' | [DSL.constant('Hello'), DSL.constant('World')] || 'Hello World'
            '-' | [DSL.constant('foo'), DSL.constant('bar'), DSL.constant('baz')] || 'foo-bar-baz'
            '|' | [DSL.constant('one'), DSL.constant('two'), DSL.constant('three'), DSL.constant('four')] || 'one|two|three|four'
            ',' | [DSL.constant('single')] || 'single'
            ', ' | [DSL.constant('apple'), DSL.constant('banana'), DSL.constant('cherry')] || 'apple, banana, cherry'
            '' | [DSL.constant('no'), DSL.constant('separator')] || 'noseparator'
            '::' | [DSL.constant('double'), DSL.constant('colon')] || 'double::colon'
            ',' | [DSL.constant(''), DSL.constant('empty'), DSL.constant('first')] || ',empty,first'
    }

    def "CONCAT_WS with String array overload"() {
        expect:
            session.select(DSL.concatWs("-", "one", "two", "three")).fetchSingle() == "one-two-three"

        where:
            session << allSessions
    }

    def "select charLength()"() {
        when:
            def result = session.select(DSL.charLength(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            value || expected
            'josé' || 4
            'hello' || 5
            'a' || 1
            'ab' || 2
            'test' || 4
            '' || 0
            ' ' || 1
            '12345' || 5
            'Hello World' || 11
            'josé maría' || 10
    }

    def "select left()"() {
        when:
            def result = session.select(DSL.left(string, n)).fetchSingle()

        then:
            result == expected
            result.getClass() == String.class

        where:
            session << allSessions

        combined:
            string | n || expected
            'abcde' | 2 || 'ab'
            'hello' | 3 || 'hel'
            'Hello World' | 5 || 'Hello'
            'test' | 1 || 't'
            'abcde' | 0 || ''
            'abcde' | 10 || 'abcde'
            '' | 2 || ''
            'a' | 1 || 'a'
    }

    def "LEFT returns first n characters from string literal"() {
        expect:
        session.select(DSL.left("Hello World", 5)).fetchSingle() == "Hello"

        where:
        session << allSessions
    }

    def "LEFT returns first n characters from expression"() {
        expect:
        session.select(DSL.left(DSL.constant("Hello World"), DSL.constant(5))).fetchSingle() == "Hello"

        where:
        session << allSessions
    }

    def "LEFT(Expression, Integer) returns first n characters"() {
        expect:
        session.select(DSL.left(DSL.constant("Hello World"), Integer.valueOf(5))).fetchSingle() == "Hello"

        where:
        session << allSessions
    }

    def "LEFT with negative n returns all but last n characters"() {
        // PostgreSQL-specific: negative n means "all but last |n| characters"
        // H2 returns empty string for negative n
        expect:
        session.select(DSL.left("Hello World", -6)).fetchSingle() == "Hello"

        where:
        session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "LEFT with n greater than string length returns entire string"() {
        expect:
        session.select(DSL.left("Hi", 10)).fetchSingle() == "Hi"

        where:
        session << allSessions
    }

    def "LEFT with zero returns empty string"() {
        expect:
        session.select(DSL.left("Hello", 0)).fetchSingle() == ""

        where:
        session << allSessions
    }

    def "select lower()"() {
        when:
            def result = session.select(DSL.lower(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == String.class

        where:
            session << allSessions

        combined:
            value || expected
            'HELLO' || 'hello'
            'Hello World' || 'hello world'
            'UPPERCASE' || 'uppercase'
            'MiXeD CaSe' || 'mixed case'
            'already lowercase' || 'already lowercase'
            'ABC123' || 'abc123'
            '123' || '123'
            '' || ''
            'A' || 'a'
            'Test123Test' || 'test123test'
    }

    // ==================== LPAD ====================

    def "LPAD pads string on left to specified length"() {
        expect:
            session.select(DSL.lpad("42", 5, "0")).fetchSingle() == "00042"

        where:
            session << allSessions
    }

    def "LPAD with expression arguments pads correctly"() {
        expect:
            session.select(DSL.lpad(DSL.constant("7"), DSL.constant(3), DSL.constant("0"))).fetchSingle() == "007"

        where:
            session << allSessions
    }

    def "LPAD with single character fill repeats correctly"() {
        expect:
            session.select(DSL.lpad("x", 5, "*")).fetchSingle() == "****x"

        where:
            session << allSessions
    }

    def "LPAD truncates when string is longer than target length"() {
        expect:
            session.select(DSL.lpad("Hello World", 5, "x")).fetchSingle() == "Hello"

        where:
            session << allSessions
    }

    def "LPAD with string already at target length returns unchanged"() {
        expect:
            session.select(DSL.lpad("Hello", 5, "x")).fetchSingle() == "Hello"

        where:
            session << allSessions
    }

    def "LPAD with Expression and String fill"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "42", 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(DSL.lpad(basics.name, 5, "0"))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "00042"

        where:
            session << allSessions
    }

    def "LPAD with Expression and Expression fill"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "AB", 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(DSL.lpad(basics.name, 5, DSL.constant("*")))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "***AB"

        where:
            session << allSessions
    }

    def "LPAD pads string on left"() {
        expect:
            session.select(DSL.lpad("42", 5, "0")).fetchSingle() == "00042"

        where:
            session << allSessions
    }

    // ==================== LTRIM ====================

    def "LTRIM removes leading whitespace from string literal"() {
        expect:
            session.select(DSL.ltrim("   Hello")).fetchSingle() == "Hello"

        where:
            session << allSessions
    }

    def "LTRIM removes leading whitespace from expression"() {
        expect:
            session.select(DSL.ltrim(DSL.constant("   World"))).fetchSingle() == "World"

        where:
            session << allSessions
    }

    def "LTRIM preserves trailing whitespace"() {
        expect:
            session.select(DSL.ltrim("   Hello   ")).fetchSingle() == "Hello   "

        where:
            session << allSessions
    }

    def "LTRIM on string without leading whitespace returns unchanged"() {
        expect:
            session.select(DSL.ltrim("Hello")).fetchSingle() == "Hello"

        where:
            session << allSessions
    }

    def "LTRIM with characters removes specified leading characters"() {
        expect:
            session.select(DSL.ltrim(DSL.constant("xxxHello"), "x")).fetchSingle() == "Hello"

        where:
            session << allSessions
    }

    def "LTRIM with characters expression removes specified leading characters"() {
        expect:
            session.select(DSL.ltrim(DSL.constant("aaabbbHello"), DSL.constant("ab"))).fetchSingle() == "Hello"

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "LTRIM with characters expression removes specified leading characters 2"() {
        expect:
            session.select(DSL.ltrim(DSL.constant("aaabbbHello"), DSL.constant("aaabbb"))).fetchSingle() == "Hello"

        where:
            session << allSessions
    }

    def "LTRIM with characters removes specified characters"() {
        expect:
            session.select(DSL.ltrim(DSL.constant("xxxHello"), "x")).fetchSingle() == "Hello"

        where:
            session << allSessions
    }

    def "LTRIM removes leading whitespace"() {
        expect:
            session.select(DSL.ltrim("   Hello")).fetchSingle() == "Hello"

        where:
            session << allSessions
    }


    def "RTRIM with characters removes specified characters"() {
        expect:
            session.select(DSL.rtrim(DSL.constant("Helloxxx"), "x")).fetchSingle() == "Hello"

        where:
            session << allSessions
    }

    def "RTRIM removes trailing whitespace"() {
        expect:
            session.select(DSL.rtrim("Hello   ")).fetchSingle() == "Hello"

        where:
            session << allSessions
    }

    def "RTRIM preserves leading whitespace"() {
        expect:
            session.select(DSL.rtrim("  hello  ")).fetchSingle() == "  hello"

        where:
            session << allSessions
    }

    def "select octetLength()"() {
        when:
            def result = session.select(DSL.octetLength(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            value || expected
            'jose' || 4
            'hello' || 5
            'a' || 1
            'ab' || 2
            'test' || 4
            '' || 0
            ' ' || 1
            '12345' || 5
            'Hello World' || 11
    }

    def "POSITION finds substring"() {
        expect:
            session.select(DSL.position(DSL.constant("World"), DSL.constant("Hello World"))).fetchSingle() == 7

        where:
            session << allSessions
    }

    def "POSITION returns 0 when not found"() {
        expect:
            session.select(DSL.position(DSL.constant("xyz"), DSL.constant("Hello World"))).fetchSingle() == 0

        where:
            session << allSessions
    }

    def "POSITION with String and Expression"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Hello World", 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            Integer result = session.select(DSL.position("World", basics.name))
                    .from(basics)
                    .fetchSingle()

        then:
            result == 7

        where:
            session << allSessions
    }

    // ==================== REPEAT ====================

    def "REPEAT repeats string literal specified number of times"() {
        expect:
            session.select(DSL.repeat("ab", 3)).fetchSingle() == "ababab"

        where:
            session << allSessions
    }

    def "REPEAT with expression arguments"() {
        expect:
            session.select(DSL.repeat(DSL.constant("xyz"), DSL.constant(2))).fetchSingle() == "xyzxyz"

        where:
            session << allSessions
    }

    def "REPEAT with count of zero returns empty string"() {
        expect:
            session.select(DSL.repeat("Hello", 0)).fetchSingle() == ""

        where:
            session << allSessions
    }

    def "REPEAT with count of one returns original string"() {
        expect:
            session.select(DSL.repeat("Single", 1)).fetchSingle() == "Single"

        where:
            session << allSessions
    }

    def "REPEAT with empty string returns empty string"() {
        expect:
            session.select(DSL.repeat("", 5)).fetchSingle() == ""

        where:
            session << allSessions
    }

    def "REPEAT with Expression parameter"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "ab", 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(DSL.repeat(basics.name, 3))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "ababab"

        where:
            session << allSessions
    }

    def "REPEAT repeats string"() {
        expect:
            session.select(DSL.repeat("ab", 3)).fetchSingle() == "ababab"

        where:
            session << allSessions
    }

    // ==================== RIGHT ====================

    def "RIGHT returns last n characters from string literal"() {
        expect:
            session.select(DSL.right("Hello World", 5)).fetchSingle() == "World"

        where:
            session << allSessions
    }

    def "RIGHT returns last n characters from expression"() {
        expect:
            session.select(DSL.right(DSL.constant("Hello World"), DSL.constant(5))).fetchSingle() == "World"

        where:
            session << allSessions
    }

    def "RIGHT(Expression, Integer) returns last n characters"() {
        expect:
            session.select(DSL.right(DSL.constant("Hello World"), Integer.valueOf(5))).fetchSingle() == "World"

        where:
            session << allSessions
    }

    def "RIGHT with n greater than string length returns entire string"() {
        expect:
            session.select(DSL.right("Hi", 10)).fetchSingle() == "Hi"

        where:
            session << allSessions
    }

    def "RIGHT with zero returns empty string"() {
        expect:
            session.select(DSL.right("Hello", 0)).fetchSingle() == ""

        where:
            session << allSessions
    }

    def "RIGHT returns rightmost characters"() {
        expect:
            session.select(DSL.right("Hello World", 5)).fetchSingle() == "World"

        where:
            session << allSessions
    }

    def "RIGHT returns rightmost characters from expression"() {
        expect:
            session.select(DSL.right(DSL.constant("Hello World"), DSL.constant(5))).fetchSingle() == "World"

        where:
            session << allSessions
    }

    def "RIGHT with Expression parameter"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Hello World", 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(DSL.right(basics.name, 5))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "World"

        where:
            session << allSessions
    }

    // ==================== RPAD ====================

    def "RPAD pads string on right to specified length"() {
        expect:
            session.select(DSL.rpad("Hi", 5, "!")).fetchSingle() == "Hi!!!"

        where:
            session << allSessions
    }

    def "RPAD with expression arguments pads correctly"() {
        expect:
            session.select(DSL.rpad(DSL.constant("Go"), DSL.constant(4), DSL.constant("-"))).fetchSingle() == "Go--"

        where:
            session << allSessions
    }

    def "RPAD with single character fill repeats correctly"() {
        expect:
            session.select(DSL.rpad("x", 5, "*")).fetchSingle() == "x****"

        where:
            session << allSessions
    }

    def "RPAD truncates when string is longer than target length"() {
        expect:
            session.select(DSL.rpad("Hello World", 5, "x")).fetchSingle() == "Hello"

        where:
            session << allSessions
    }

    def "RPAD with string already at target length returns unchanged"() {
        expect:
            session.select(DSL.rpad("Hello", 5, "x")).fetchSingle() == "Hello"

        where:
            session << allSessions
    }

    def "RPAD with Expression and String fill"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Hi", 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(DSL.rpad(basics.name, 5, "!"))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "Hi!!!"

        where:
            session << allSessions
    }

    def "RPAD with Expression and Expression fill"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "End", 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(DSL.rpad(basics.name, 6, DSL.constant(".")))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "End..."

        where:
            session << allSessions
    }

    def "RPAD pads string on right"() {
        expect:
            session.select(DSL.rpad("Hi", 5, "!")).fetchSingle() == "Hi!!!"

        where:
            session << allSessions
    }

    def "RPAD pads 'hello' to length 10 with '.'"() {
        expect:
            session.select(DSL.rpad("hello", 10, ".")).fetchSingle() == "hello....."

        where:
            session << allSessions
    }

    // ==================== RTRIM ====================

    def "RTRIM removes trailing whitespace from string literal"() {
        expect:
            session.select(DSL.rtrim("Hello   ")).fetchSingle() == "Hello"

        where:
            session << allSessions
    }

    def "RTRIM removes trailing whitespace from expression"() {
        expect:
            session.select(DSL.rtrim(DSL.constant("World   "))).fetchSingle() == "World"

        where:
            session << allSessions
    }

    def "RTRIM on string without trailing whitespace returns unchanged"() {
        expect:
            session.select(DSL.rtrim("Hello")).fetchSingle() == "Hello"

        where:
            session << allSessions
    }

    def "RTRIM with characters removes specified trailing characters"() {
        expect:
            session.select(DSL.rtrim(DSL.constant("Helloxxx"), "x")).fetchSingle() == "Hello"

        where:
            session << allSessions
    }

    def "RTRIM with characters expression removes specified trailing characters"() {
        expect:
            session.select(DSL.rtrim(DSL.constant("Helloaaabbb"), DSL.constant("ab"))).fetchSingle() == "Hello"

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "RTRIM with characters expression removes specified trailing characters 2"() {
        expect:
            session.select(DSL.rtrim(DSL.constant("Helloaaabbb"), DSL.constant("aaabbb"))).fetchSingle() == "Hello"

        where:
            session << allSessions
    }

    def "SUBSTRING with Expression from position"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Hello World", 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(DSL.substring(basics.name, 7))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "World"

        where:
            session << allSessions
    }

    def "SUBSTRING with Expression from position with length"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Hello World", 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(DSL.substring(basics.name, 1, 5))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "Hello"

        where:
            session << allSessions
    }

    def "SUBSTRING extracts substring from start"() {
        expect:
            session.select(DSL.substring("Hello World", 7)).fetchSingle() == "World"

        where:
            session << allSessions
    }

    def "SUBSTRING extracts substring with length"() {
        expect:
            session.select(DSL.substring("Hello World", 1, 5)).fetchSingle() == "Hello"

        where:
            session << allSessions
    }

    def "select trim()"() {
        when:
            def result = session.select(DSL.trim(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == String.class

        where:
            session << allSessions

        combined:
            value || expected
            '   hello   ' || 'hello'
            '  world' || 'world'
            'test  ' || 'test'
            '   spaces   on   both   ' || 'spaces   on   both'
            'no spaces' || 'no spaces'
            '  ' || ''
            ' a ' || 'a'
            '   ' || ''
            '  Hello World  ' || 'Hello World'
            '    multiple    spaces    ' || 'multiple    spaces'
    }

    def "TRIM removes leading and trailing whitespace"() {
        expect:
            session.select(DSL.trim("  hello  ")).fetchSingle() == "hello"

        where:
            session << allSessions
    }

    def "TRIM with no surrounding whitespace returns same string"() {
        expect:
            session.select(DSL.trim("hello")).fetchSingle() == "hello"

        where:
            session << allSessions
    }

    def "TRIM with only whitespace returns empty string"() {
        expect:
            session.select(DSL.trim("   ")).fetchSingle() == ""

        where:
            session << allSessions
    }

    def "TRIM preserves internal whitespace"() {
        expect:
            session.select(DSL.trim("  hello world  ")).fetchSingle() == "hello world"

        where:
            session << allSessions
    }

    def "TRIM with Expression parameter"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "  padded  ", 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(DSL.trim(basics.name))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "padded"

        where:
            session << allSessions
    }

    def "TRIM with custom characters removes them from both sides"() {
        expect:
            session.select(DSL.trim(DSL.constant("xxhelloxx"), "x")).fetchSingle() == "hello"

        where:
            session << allSessions
    }

    def "TRIM with custom characters Expression overload"() {
        expect:
            session.select(DSL.trim(DSL.constant("##hello##"), DSL.constant("#"))).fetchSingle() == "hello"

        where:
            session << allSessions
    }

    def "TRIM with custom characters leaves string unchanged when characters absent"() {
        expect:
            session.select(DSL.trim(DSL.constant("hello"), "x")).fetchSingle() == "hello"

        where:
            session << allSessions
    }

    def "TO_HEX"() {
        expect:
            session.select(DSL.toHex(2147483647)).fetchSingle() == "7FFFFFFF"
            session.select(DSL.toHex((long) 2147483647)).fetchSingle() == "7FFFFFFF"

        where:
            session << allSessions
    }

    def "select upper()"() {
        when:
            def result = session.select(DSL.upper(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == String.class

        where:
            session << allSessions

        combined:
            value || expected
            'hello' || 'HELLO'
            'Hello World' || 'HELLO WORLD'
            'lowercase' || 'LOWERCASE'
            'MiXeD CaSe' || 'MIXED CASE'
            'ALREADY UPPERCASE' || 'ALREADY UPPERCASE'
            'abc123' || 'ABC123'
            '123' || '123'
            '' || ''
            'a' || 'A'
            'test123test' || 'TEST123TEST'
    }

    def "REPLACE string"() {
        expect:
            session.select(DSL.replace("abca", "a", "d")).fetchSingle() == "dbcd"

        where:
            session << allSessions
    }

    def "REPLACE(Expression, String, Expression)"() {
        expect:
            session.select(DSL.replace(DSL.constant("abca"), "a", DSL.constant("d"))).fetchSingle() == "dbcd"

        where:
            session << allSessions
    }

    def "REPLACE(Expression, Expression, String)"() {
        expect:
            session.select(DSL.replace(DSL.constant("abca"), DSL.constant("a"), "d")).fetchSingle() == "dbcd"

        where:
            session << allSessions
    }

    def "REGEXP_REPLACE replaces all matches of a literal pattern"() {
        expect:
            session.select(DSL.regexpReplaceAll("hello", "l", "")).fetchSingle() == "heo"

        where:
            session << allSessions
    }

    def "REGEXP_REPLACE replaces all matches with a non-empty single character"() {
        expect:
            session.select(DSL.regexpReplaceAll("hello", "l", "X")).fetchSingle() == "heXXo"

        where:
            session << allSessions
    }

    def "REGEXP_REPLACE replaces all matches with a multi-character string"() {
        expect:
            session.select(DSL.regexpReplaceAll("a-b-c", "-", "::")).fetchSingle() == "a::b::c"

        where:
            session << allSessions
    }

    def "REGEXP_REPLACE replaces all matches of a character class"() {
        expect:
            session.select(DSL.regexpReplaceAll("a1b2c3", "[0-9]", "")).fetchSingle() == "abc"

        where:
            session << allSessions
    }

    def "REGEXP_REPLACE replaces character class with non-empty replacement"() {
        expect:
            session.select(DSL.regexpReplaceAll("a1b2c3", "[0-9]", "#")).fetchSingle() == "a#b#c#"

        where:
            session << allSessions
    }

    def "REGEXP_REPLACE collapses contiguous matches with quantifier"() {
        expect:
            session.select(DSL.regexpReplaceAll("foo   bar   baz", " +", " ")).fetchSingle() == "foo bar baz"

        where:
            session << allSessions
    }

    def "REGEXP_REPLACE with Expression parameters"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "hello", 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(DSL.regexpReplaceAll(basics.name, DSL.constant("l"), DSL.constant("")))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "heo"

        where:
            session << allSessions
    }

    def "REGEXP_REPLACE with Expression parameters and non-empty replacement"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "banana", 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(DSL.regexpReplaceAll(basics.name, DSL.constant("a"), DSL.constant("oo")))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "boonoonoo"

        where:
            session << allSessions
    }

    def "REGEXP_REPLACE(Expression, String, Expression)"() {
        expect:
            session.select(DSL.regexpReplaceAll(DSL.constant("hello"), "l", DSL.constant("X"))).fetchSingle() == "heXXo"

        where:
            session << allSessions
    }

    def "REGEXP_REPLACE(Expression, Expression, String)"() {
        expect:
            session.select(DSL.regexpReplaceAll(DSL.constant("hello"), DSL.constant("l"), "X")).fetchSingle() == "heXXo"

        where:
            session << allSessions
    }

    // ==================== REVERSE ====================

    def "REVERSE reverses string literal"() {
        expect:
            session.select(DSL.reverse("Hello")).fetchSingle() == "olleH"

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "REVERSE reverses string expression"() {
        expect:
            session.select(DSL.reverse(DSL.constant("World"))).fetchSingle() == "dlroW"

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "REVERSE of palindrome returns same string"() {
        expect:
            session.select(DSL.reverse("racecar")).fetchSingle() == "racecar"

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "REVERSE of single character returns same character"() {
        expect:
            session.select(DSL.reverse("A")).fetchSingle() == "A"

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "REVERSE of empty string returns empty string"() {
        expect:
            session.select(DSL.reverse("")).fetchSingle() == ""

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "REVERSE with Expression parameter"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Hello", 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(DSL.reverse(basics.name))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "olleH"

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "REVERSE reverses string"() {
        expect:
            session.select(DSL.reverse("Hello")).fetchSingle() == "olleH"

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "SPACE produces spaces of given length"() {
        expect:
            session.select(DSL.space(3)).fetchSingle() == "   "

        where:
            session << allSessions
    }

    def "SPACE with zero returns empty string"() {
        expect:
            session.select(DSL.space(0)).fetchSingle() == ""

        where:
            session << allSessions
    }

    def "SPACE with Expression parameter"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "x", 4)
            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(DSL.space(basics.numero))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "    "

        where:
            session << allSessions
    }

    def "combining LEFT and RIGHT extracts middle portion"() {
        expect:
        // "Hello World" -> right 8 = "lo World" -> left 2 = "lo"
        session.select(DSL.left(DSL.right("Hello World", 8), DSL.constant(2))).fetchSingle() == "lo"

        where:
        session << allSessions
    }

    def "combining LPAD and LTRIM restores original string"() {
        expect:
        session.select(DSL.ltrim(DSL.lpad("test", 8, " "))).fetchSingle() == "test"

        where:
        session << allSessions
    }

    def "combining RPAD and RTRIM restores original string"() {
        expect:
        session.select(DSL.rtrim(DSL.rpad("test", 8, " "))).fetchSingle() == "test"

        where:
        session << allSessions
    }

    def "chaining REPEAT and LEFT limits repeated string"() {
        expect:
        // repeat "ab" 5 times = "ababababab" -> left 7 = "abababa"
        session.select(DSL.left(DSL.repeat("ab", 5), DSL.constant(7))).fetchSingle() == "abababa"

        where:
        session << allSessions
    }
}
