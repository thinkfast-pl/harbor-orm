// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.query.ProductsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class PortableFunctionExpressionNumericIT extends AbstractHarborIT {

    def "mod() on entity column"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 15)
            fixtures.addBasic(3L, "C", 20)

            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> modExpression = DSL.mod(basics.numero, DSL.constant(3))

        when:
            List<Record> records = session.select(basics.name, modExpression.as("remainder"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get("remainder") == 1  // 10 % 3 = 1
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get("remainder") == 0  // 15 % 3 = 0
            }
            with(records[2]) { r ->
                r.get(basics.name) == "C"
                r.get("remainder") == 2  // 20 % 3 = 2
            }

        where:
            session << allSessions
    }

    def "mod() with direct constant overloads"() {
        when:
            def result = session.select(DSL.mod(dividend, divisor)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            dividend | divisor | type || expected
            (byte) 17 | (byte) 5 | Byte.class || (byte) 2
            (byte) 20 | (byte) 7 | Byte.class || (byte) 6
            (short) 100 | (short) 30 | Short.class || (short) 10
            (short) 45 | (short) 13 | Short.class || (short) 6
            (Integer) 29 | (Integer) 7 | Integer.class || (Integer) 1
            (Integer) 50 | (Integer) 8 | Integer.class || (Integer) 2
            (Long) 123L | (Long) 17L | Long.class || (Long) 4L
            (Long) 1000L | (Long) 33L | Long.class || (Long) 10L
            new BigInteger("999") | new BigInteger("100") | BigInteger.class || new BigInteger("99")
            new BigInteger("1234") | new BigInteger("56") | BigInteger.class || new BigInteger("2")
            new BigDecimal("15.5") | new BigDecimal("4.0") | BigDecimal.class || new BigDecimal("3.5")
            new BigDecimal("23.7") | new BigDecimal("5.5") | BigDecimal.class || new BigDecimal("1.7")
    }

    def "select mod()"() {
        when:
            def result = session.select(DSL.mod(type, y, x)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            y | x | type || expected
            (byte) 9 | (byte) 4 | Byte.class || (byte) 1
            (byte) 10 | (byte) 3 | Byte.class || (byte) 1
            (short) 17 | (short) 5 | Short.class || (short) 2
            (short) 100 | (short) 7 | Short.class || (short) 2
            (int) 9 | (int) 4 | Integer.class || (int) 1
            (int) 17 | (int) 5 | Integer.class || (int) 2
            (long) 9 | (long) 4 | Long.class || (long) 1
            (long) 17 | (long) 5 | Long.class || (long) 2
            (BigInteger) 9 | (BigInteger) 4 | BigInteger.class || (BigInteger) 1
            (BigInteger) 17 | (BigInteger) 5 | BigInteger.class || (BigInteger) 2
            (BigDecimal) 9.5 | (BigDecimal) 2.0 | BigDecimal.class || (BigDecimal) 1.5
            (BigDecimal) 11.0 | (BigDecimal) 3.0 | BigDecimal.class || (BigDecimal) 2.0
    }

    def "mod() with constant integer values"() {
        when:
            def result = session.select(DSL.mod(type, dividend, divisor)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            dividend | divisor | type || expected
            (byte) 10 | (byte) 3 | Byte.class || (byte) 1
            (byte) 15 | (byte) 4 | Byte.class || (byte) 3
            (short) 100 | (short) 9 | Short.class || (short) 1
            (short) 50 | (short) 7 | Short.class || (short) 1
            (int) 17 | (int) 5 | Integer.class || (int) 2
            (int) 20 | (int) 6 | Integer.class || (int) 2
            (long) 100 | (long) 9 | Long.class || (long) 1
            (long) 50 | (long) 13 | Long.class || (long) 11
    }

    // ===========================================
    // POWER - Exponentiation
    // ===========================================

    def "power() with various base and exponent values"() {
        when:
            def result = session.select(DSL.power(DSL.constant(base), DSL.constant(exponent))).fetchSingle()

        then:
            result - expected < 0.0001
            result - expected > -0.0001
            result.getClass() == BigDecimal.class

        where:
            session << allSessions

        combined:
            base | exponent || expected
            2 | 3 || 8.0
            2 | 0 || 1.0
            2 | 1 || 2.0
            3 | 2 || 9.0
            5 | 2 || 25.0
            10 | 3 || 1000.0
            2.0 | 0.5 || 1.4142  // square root of 2
            4.0 | 0.5 || 2.0     // square root of 4
    }

    def "power() on entity column"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 2)
            fixtures.addBasic(2L, "B", 3)
            fixtures.addBasic(3L, "C", 4)

            BasicsTable basics = new BasicsTable(null)
            Expression<BigDecimal> powerExpression = DSL.power(basics.numero, DSL.constant(2))

        when:
            List<Record> records = session.select(basics.name, powerExpression.as("squared"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get("squared") == new BigDecimal("4.0")  // 2^2
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get("squared") == new BigDecimal("9.0")  // 3^2
            }
            with(records[2]) { r ->
                r.get(basics.name) == "C"
                r.get("squared") == new BigDecimal("16.0")  // 4^2
            }

        where:
            session << allSessions
    }

    def "2 power 3"() {
        when:
            def result = session.select(DSL.power(left, right)).fetchSingle()

        then:
            result == 8.0
            result.class == BigDecimal.class

        where:
            session << allSessions

        combined:
            left | right
            DSL.constant(2) | DSL.constant(3)
            DSL.constant(2L) | DSL.constant(3L)
            DSL.constant((short) 2) | DSL.constant((short) 3)
            DSL.constant((byte) 2) | DSL.constant((byte) 3)
            DSL.constant(2F) | DSL.constant(3F)
            DSL.constant(2D) | DSL.constant(3D)
            DSL.constant(2.0) | DSL.constant(3.0)
    }

    // ===========================================
    // ROUND - Round to Nearest
    // ===========================================

    def "round() with constant values"() {
        when:
            def result = session.select(DSL.round(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 1.2 | Float.class || (float) 1.0
            (float) 1.6 | Float.class || (float) 2.0
            (float) 1.9 | Float.class || (float) 2.0
            (float) -1.2 | Float.class || (float) -1.0
            (float) -1.9 | Float.class || (float) -2.0
            (double) 2.3 | Double.class || (double) 2.0
            (double) 2.7 | Double.class || (double) 3.0
            (BigDecimal) 3.49 | BigDecimal.class || (BigDecimal) 3.0
            (BigDecimal) 3.51 | BigDecimal.class || (BigDecimal) 4.0
    }

    def "round() with scale parameter"() {
        when:
            def result = session.select(DSL.round(value, scale)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | scale | type || expected
            (float) 3.14159 | 2 | Float.class || (float) 3.14
            (float) 3.14159 | 3 | Float.class || (float) 3.142
            (double) 2.71828 | 2 | Double.class || (double) 2.72
            (double) 2.71828 | 3 | Double.class || (double) 2.718
            (BigDecimal) 123.456 | 1 | BigDecimal.class || (BigDecimal) 123.5
            (BigDecimal) 123.456 | 2 | BigDecimal.class || (BigDecimal) 123.46
    }

    def "select round()"() {
        when:
            def result = session.select(DSL.round(type, value)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 42.4 | Float.class || (float) 42.0
            (double) 42.4 | Double.class || (double) 42.0
            (BigDecimal) 42.4 | BigDecimal.class || (BigDecimal) 42.0
    }

    def "select round() with scale"() {
        when:
            def result = session.select(DSL.round(type, value, s)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | s | type || expected
            (float) 1234.56 | -1 | Float.class || (float) 1230
            (double) 1234.56 | -1 | Double.class || (double) 1230
            (BigDecimal) 1234.56 | -1 | BigDecimal.class || (BigDecimal) 1230
    }

    // ===========================================
    // CEIL - Round Up
    // ===========================================

    def "ceil() with constant values"() {
        when:
            def result = session.select(DSL.ceil(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 1.1 | Float.class || (float) 2.0
            (float) 1.9 | Float.class || (float) 2.0
            (float) -1.1 | Float.class || (float) -1.0
            (float) -1.9 | Float.class || (float) -1.0
            (double) 2.3 | Double.class || (double) 3.0
            (double) -2.3 | Double.class || (double) -2.0
            (BigDecimal) 3.14 | BigDecimal.class || (BigDecimal) 4.0
            (BigDecimal) -3.14 | BigDecimal.class || (BigDecimal) -3.0
    }

    def "select ceil()"() {
        when:
            def result = session.select(DSL.ceil(type, value)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 29.9 | Float.class || (float) 30.0
            (double) 29.9 | Double.class || (double) 30.0
            (BigDecimal) 29.9 | BigDecimal.class || (BigDecimal) 30.0
    }

    // ===========================================
    // FLOOR - Round Down
    // ===========================================

    def "floor() with constant values"() {
        when:
            def result = session.select(DSL.floor(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 1.1 | Float.class || (float) 1.0
            (float) 1.9 | Float.class || (float) 1.0
            (float) -1.1 | Float.class || (float) -2.0
            (float) -1.9 | Float.class || (float) -2.0
            (double) 2.3 | Double.class || (double) 2.0
            (double) -2.3 | Double.class || (double) -3.0
            (BigDecimal) 3.14 | BigDecimal.class || (BigDecimal) 3.0
            (BigDecimal) -3.14 | BigDecimal.class || (BigDecimal) -4.0
    }

    def "select floor()"() {
        when:
            def result = session.select(DSL.floor(type, value)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 29.9 | Float.class || (float) 29.0
            (double) 29.9 | Double.class || (double) 29.0
            (BigDecimal) 29.9 | BigDecimal.class || (BigDecimal) 29.0
    }

    // ===========================================
    // TRUNC - Truncate
    // ===========================================

    def "trunc() with positive values"() {
        when:
            def result = session.select(DSL.trunc(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 3.14159 | Float.class || (float) 3.0
            (float) 3.99999 | Float.class || (float) 3.0
            (double) 5.12345 | Double.class || (double) 5.0
            (double) 5.99999 | Double.class || (double) 5.0
            (BigDecimal) 7.654321 | BigDecimal.class || (BigDecimal) 7.0
            (BigDecimal) 7.999999 | BigDecimal.class || (BigDecimal) 7.0
    }

    def "trunc() with negative values"() {
        when:
            def result = session.select(DSL.trunc(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) -3.14159 | Float.class || (float) -3.0
            (float) -3.99999 | Float.class || (float) -3.0
            (double) -5.12345 | Double.class || (double) -5.0
            (double) -5.99999 | Double.class || (double) -5.0
            (BigDecimal) -7.654321 | BigDecimal.class || (BigDecimal) -7.0
            (BigDecimal) -7.999999 | BigDecimal.class || (BigDecimal) -7.0
    }

    def "select trunc() - Truncates to integer (towards zero)"() {
        when:
            def result = session.select(DSL.trunc(type, value)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)

        combined:
            value | type || expected
            (float) 42.8 | Float.class || (float) 42
            (double) 42.8 | Double.class || (double) 42
            (BigDecimal) 42.8 | BigDecimal.class || (BigDecimal) 42
            (float) -42.8 | Float.class || (float) -42
            (double) -42.8 | Double.class || (double) -42
            (BigDecimal) -42.8 | BigDecimal.class || (BigDecimal) -42
            (float) 42.1 | Float.class || (float) 42
            (double) 42.1 | Double.class || (double) 42
            (BigDecimal) 42.1 | BigDecimal.class || (BigDecimal) 42
            (float) -42.1 | Float.class || (float) -42
            (double) -42.1 | Double.class || (double) -42
            (BigDecimal) -42.1 | BigDecimal.class || (BigDecimal) -42
            (float) 1.9 | Float.class || (float) 1
            (double) 1.9 | Double.class || (double) 1
            (BigDecimal) 1.9 | BigDecimal.class || (BigDecimal) 1
            (float) -1.9 | Float.class || (float) -1
            (double) -1.9 | Double.class || (double) -1
            (BigDecimal) -1.9 | BigDecimal.class || (BigDecimal) -1
            (float) 100.999 | Float.class || (float) 100
            (double) 100.999 | Double.class || (double) 100
            (BigDecimal) 100.999 | BigDecimal.class || (BigDecimal) 100
            (float) -100.999 | Float.class || (float) -100
            (double) -100.999 | Double.class || (double) -100
            (BigDecimal) -100.999 | BigDecimal.class || (BigDecimal) -100
    }

    // ===========================================
    // ABS - Absolute Value
    // ===========================================

    def "abs() on entity column with positive values"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)

            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> absExpression = DSL.abs(basics.numero)

        when:
            List<Record> records = session.select(basics.name, absExpression.as("abs_value"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get("abs_value") == 10
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get("abs_value") == 20
            }

        where:
            session << allSessions
    }

    def "abs() on entity column with negative values"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", -15)
            fixtures.addBasic(2L, "B", -25)

            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> absExpression = DSL.abs(basics.numero)

        when:
            List<Record> records = session.select(basics.name, absExpression.as("abs_value"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get("abs_value") == 15
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get("abs_value") == 25
            }

        where:
            session << allSessions
    }

    def "abs() on entity column with mixed positive and negative values"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", -10)
            fixtures.addBasic(2L, "B", 20)
            fixtures.addBasic(3L, "C", -30)

            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> absExpression = DSL.abs(basics.numero)

        when:
            List<Record> records = session.select(basics.name, absExpression.as("abs_value"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get("abs_value") == 10
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get("abs_value") == 20
            }
            with(records[2]) { r ->
                r.get(basics.name) == "C"
                r.get("abs_value") == 30
            }

        where:
            session << allSessions
    }

    def "select abs()"() {
        when:
            def result = session.select(DSL.abs(type, value)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (byte) 2 | Byte.class || 2
            (byte) -3 | Byte.class || 3
            (short) 2 | Short.class || 2
            (short) -3 | Short.class || 3
            (int) 2 | Integer.class || 2
            (int) -3 | Integer.class || 3
            (long) 2 | Long.class || 2
            (long) -3 | Long.class || 3
            (float) 2.1 | Float.class || (float) 2.1
            (float) -3.1 | Float.class || (float) 3.1
            (double) 2.1 | Double.class || (double) 2.1
            (double) -3.1 | Double.class || (double) 3.1
            (BigDecimal) 2.1 | BigDecimal.class || (BigDecimal) 2.1
            (BigDecimal) -3.1 | BigDecimal.class || (BigDecimal) 3.1
            (BigInteger) 2 | BigInteger.class || (BigInteger) 2
            (BigInteger) -3 | BigInteger.class || (BigInteger) 3
    }

    def "using abs() in order by clause"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", -50)
            fixtures.addBasic(2L, "B", 10)
            fixtures.addBasic(3L, "C", -30)
            fixtures.addBasic(4L, "D", 20)

            BasicsTable basics = new BasicsTable(null)

        when:
            // Order by absolute value of numero
            List<Record> records = session.select(basics.name, basics.numero)
                    .from(basics)
                    .orderBy(DSL.abs(basics.numero).asc())
                    .fetchAll()

        then:
            records.size() == 4
            records[0].get(basics.numero) == 10   // abs(10) = 10
            records[1].get(basics.numero) == 20   // abs(20) = 20
            records[2].get(basics.numero) == -30  // abs(-30) = 30
            records[3].get(basics.numero) == -50  // abs(-50) = 50

        where:
            session << allSessions
    }

    def "abs() with direct constant overloads"() {
        when:
            def result = session.select(DSL.abs(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (byte) -5 | Byte.class || (byte) 5
            (byte) 5 | Byte.class || (byte) 5
            (short) -100 | Short.class || (short) 100
            (short) 100 | Short.class || (short) 100
            (Integer) -42 | Integer.class || (Integer) 42
            (Integer) 42 | Integer.class || (Integer) 42
            (Long) -1000L | Long.class || (Long) 1000L
            (Long) 1000L | Long.class || (Long) 1000L
            (Float) -3.14f | Float.class || (Float) 3.14f
            (Float) 3.14f | Float.class || (Float) 3.14f
            (Double) -2.718d | Double.class || (Double) 2.718d
            (Double) 2.718d | Double.class || (Double) 2.718d
            new BigDecimal("-123.456") | BigDecimal.class || new BigDecimal("123.456")
            new BigDecimal("123.456") | BigDecimal.class || new BigDecimal("123.456")
            new BigInteger("-999") | BigInteger.class || new BigInteger("999")
            new BigInteger("999") | BigInteger.class || new BigInteger("999")
    }

    def "select neg() composes inside arithmetic"() {
        when:
            Integer result = session.select(DSL.constant(10).add(DSL.neg(DSL.constant(3)))).fetchSingle()

        then:
            result == 7

        where:
            session << allSessions
    }

    def "select neg()"() {
        when:
            def result = session.select(DSL.neg(type, value)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (byte) 2 | Byte.class || (byte) -2
            (byte) -3 | Byte.class || (byte) 3
            (byte) 0 | Byte.class || (byte) 0
            (short) 2 | Short.class || (short) -2
            (short) -3 | Short.class || (short) 3
            (int) 2 | Integer.class || (int) -2
            (int) -3 | Integer.class || (int) 3
            (long) 2 | Long.class || (long) -2
            (long) -3 | Long.class || (long) 3
            (float) 2.5 | Float.class || (float) -2.5
            (float) -3.25 | Float.class || (float) 3.25
            (double) 2.5 | Double.class || (double) -2.5
            (double) -3.25 | Double.class || (double) 3.25
            (BigDecimal) 2.5 | BigDecimal.class || (BigDecimal) -2.5
            (BigDecimal) -3.25 | BigDecimal.class || (BigDecimal) 3.25
            (BigInteger) 2 | BigInteger.class || (BigInteger) -2
            (BigInteger) -3 | BigInteger.class || (BigInteger) 3
    }

    // ===========================================
    // SIGN - Sign Function
    // ===========================================

    def "sign() with constant values"() {
        when:
            def result = session.select(DSL.sign(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 10.0 | Integer.class || 1
            (float) -10.0 | Integer.class || -1
            (float) 0.0 | Integer.class || 0
            (double) 42.0 | Integer.class || 1
            (double) -42.0 | Integer.class || -1
            (double) 0.0 | Integer.class || 0
            (BigDecimal) 100 | Integer.class || 1
            (BigDecimal) -100 | Integer.class || -1
            (BigDecimal) 0 | Integer.class || 0
    }

    def "select sign()"() {
        when:
            def result = session.select(DSL.sign(inputType, value)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            value | inputType || expected
            (float) -8.4 | Float.class || -1
            (float) 0.0 | Float.class || 0
            (float) 8.4 | Float.class || 1
            (double) -8.4 | Double.class || -1
            (double) 0.0 | Double.class || 0
            (double) 8.4 | Double.class || 1
            (BigDecimal) -8.4 | BigDecimal.class || -1
            (BigDecimal) 0.0 | BigDecimal.class || 0
            (BigDecimal) 8.4 | BigDecimal.class || 1
    }


    // ===========================================
    // SQRT - Square Root
    // ===========================================

    def "sqrt() with constant values"() {
        when:
            def result = session.select(DSL.sqrt(value)).fetchSingle()

        then:
            result - expected < 0.0001
            result - expected > -0.0001
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 1.0 | Float.class || (float) 1.0
            (float) 4.0 | Float.class || (float) 2.0
            (float) 9.0 | Float.class || (float) 3.0
            (float) 16.0 | Float.class || (float) 4.0
            (float) 25.0 | Float.class || (float) 5.0
            (double) 100.0 | Double.class || (double) 10.0
            (double) 144.0 | Double.class || (double) 12.0
            (BigDecimal) 81.0 | BigDecimal.class || (BigDecimal) 9.0
            (BigDecimal) 121.0 | BigDecimal.class || (BigDecimal) 11.0
    }

    def "select sqrt()"() {
        when:
            def result = session.select(DSL.sqrt(type, value)).fetchSingle()

        then:
            result - expected < 0.00001
            result - expected > -0.00001
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 2.0 | Float.class || (float) 1.414213
            (double) 2.0 | Double.class || (double) 1.414213
            (BigDecimal) 2.0 | BigDecimal.class || (BigDecimal) 1.414213
            (float) 4.0 | Float.class || (float) 2.0
            (double) 4.0 | Double.class || (double) 2.0
            (BigDecimal) 4.0 | BigDecimal.class || (BigDecimal) 2.0
            (float) 9.0 | Float.class || (float) 3.0
            (double) 9.0 | Double.class || (double) 3.0
            (BigDecimal) 9.0 | BigDecimal.class || (BigDecimal) 3.0
            (float) 16.0 | Float.class || (float) 4.0
            (double) 16.0 | Double.class || (double) 4.0
            (BigDecimal) 16.0 | BigDecimal.class || (BigDecimal) 4.0
            (float) 64.0 | Float.class || (float) 8.0
            (double) 64.0 | Double.class || (double) 8.0
            (BigDecimal) 64.0 | BigDecimal.class || (BigDecimal) 8.0
    }

    // ===========================================
    // CBRT - Cube Root
    // ===========================================

    def "cbrt() with Float constant value"() {
        when:
            def result = session.select(DSL.cbrt((float) 27.0)).fetchSingle()

        then:
            (result - (float) 3.0).abs() < 0.0001
            result.getClass() == Float.class

        where:
            session << allSessions
    }

    def "cbrt() with Double constant value"() {
        when:
            def result = session.select(DSL.cbrt((double) 125.0)).fetchSingle()

        then:
            (result - (double) 5.0).abs() < 0.0001
            result.getClass() == Double.class

        where:
            session << allSessions
    }

    def "cbrt() with BigDecimal constant value"() {
        when:
            def result = session.select(DSL.cbrt(new BigDecimal("8.0"))).fetchSingle()

        then:
            (result - new BigDecimal("2.0")).abs() < 0.0001
            result.getClass() == BigDecimal.class

        where:
            session << allSessions
    }

    def "cbrt() with negative constant values"() {
        when:
            def result = session.select(DSL.cbrt(type, value)).fetchSingle()

        then:
            (result - expected).abs() < 0.0001
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value                        | type            || expected
            (double) -8.0                | Double.class    || (double) -2.0
            (double) -27.0               | Double.class    || (double) -3.0
            (double) -125.0              | Double.class    || (double) -5.0
            (float) -8.0                 | Float.class     || (float) -2.0
            (BigDecimal) -27.0           | BigDecimal.class || (BigDecimal) -3.0
    }

    def "cbrt() on entity column with negative values"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", -8)     // cbrt(-8) = -2
            fixtures.addBasic(2L, "B", -27)    // cbrt(-27) = -3
            fixtures.addBasic(3L, "C", -64)    // cbrt(-64) = -4

            BasicsTable basics = new BasicsTable(null)
            Expression<BigDecimal> cbrtExpression = DSL.cbrt(basics.numero.cast(BigDecimal.class))

        when:
            List<Record> records = session.select(basics.name, cbrtExpression.as("cube_root"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) {
                get(basics.name) == "A"
                (get("cube_root") as BigDecimal - new BigDecimal("-2.0")).abs() < 0.01
            }
            with(records[1]) {
                get(basics.name) == "B"
                (get("cube_root") as BigDecimal - new BigDecimal("-3.0")).abs() < 0.01
            }
            with(records[2]) {
                get(basics.name) == "C"
                (get("cube_root") as BigDecimal - new BigDecimal("-4.0")).abs() < 0.01
            }

        where:
            session << allSessions
    }

    def "cbrt() on entity column with perfect cubes"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 8)     // cbrt(8) = 2
            fixtures.addBasic(2L, "B", 27)    // cbrt(27) = 3
            fixtures.addBasic(3L, "C", 64)    // cbrt(64) = 4

            BasicsTable basics = new BasicsTable(null)
            // Cast to BigDecimal for cross-database compatibility (both H2 and PostgreSQL support numeric)
            Expression<BigDecimal> cbrtExpression = DSL.cbrt(basics.numero.cast(BigDecimal.class))

        when:
            List<Record> records = session.select(basics.name, cbrtExpression.as("cube_root"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                def result = r.get("cube_root") as Number
                (result.doubleValue() - 2.0).abs() < 0.0001
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                def result = r.get("cube_root") as Number
                (result.doubleValue() - 3.0).abs() < 0.0001
            }
            with(records[2]) { r ->
                r.get(basics.name) == "C"
                def result = r.get("cube_root") as Number
                (result.doubleValue() - 4.0).abs() < 0.0001
            }

        where:
            session << allSessions
    }

    def "cbrt() on BigDecimal entity column"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addProductA()  // priceGross=1.23
            fixtures.addProductB()  // priceGross=1.85

            ProductsTable products = new ProductsTable(null)
            Expression<BigDecimal> cbrtExpression = DSL.cbrt(products.priceGross)

        when:
            List<Record> records = session.select(products.name, cbrtExpression.as("price_cbrt"))
                    .from(products)
                    .orderBy(products.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(products.name) == "A"
                def result = r.get("price_cbrt") as BigDecimal
                // cbrt(1.23) approx 1.0714
                (result - new BigDecimal("1.0714")).abs() < 0.001
            }
            with(records[1]) { r ->
                r.get(products.name) == "B"
                def result = r.get("price_cbrt") as BigDecimal
                // cbrt(1.85) approx 1.2282
                (result - new BigDecimal("1.2282")).abs() < 0.001
            }

        where:
            session << allSessions
    }

    def "select cbrt()"() {
        when:
            def result = session.select(DSL.cbrt(type, value)).fetchSingle()

        then:
            result - expected < 0.00001
            result - expected > -0.00001
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 64.0 | Float.class || (float) 4.0
            (double) 64.0 | Double.class || (double) 4.0
            (BigDecimal) 64.0 | BigDecimal.class || (BigDecimal) 4.0
            (float) -27.0 | Float.class || (float) -3.0
            (double) -27.0 | Double.class || (double) -3.0
            (BigDecimal) -27.0 | BigDecimal.class || (BigDecimal) -3.0
            (double) 0.0 | Double.class || (double) 0.0
    }

    def "cbrt() with various constant values"() {
        when:
        def result = session.select(DSL.cbrt(type, value)).fetchSingle()

        then:
        (result - expected).abs() < 0.0001
        result.getClass() == type

        where:
        session << allSessions

        combined:
        value                        | type           || expected
        (float) 1.0                  | Float.class    || (float) 1.0
        (float) 8.0                  | Float.class    || (float) 2.0
        (float) 27.0                 | Float.class    || (float) 3.0
        (double) 64.0                | Double.class   || (double) 4.0
        (double) 125.0               | Double.class   || (double) 5.0
        (BigDecimal) 216.0           | BigDecimal.class || (BigDecimal) 6.0
    }

    def "order by cbrt() ascending"() {
        given:
        TestFixtures fixtures = new TestFixtures(session)
        fixtures.addBasic(1L, "C", 64)   // cbrt(64) = 4
        fixtures.addBasic(2L, "A", 8)    // cbrt(8) = 2
        fixtures.addBasic(3L, "D", 125)  // cbrt(125) = 5
        fixtures.addBasic(4L, "B", 27)   // cbrt(27) = 3

        BasicsTable basics = new BasicsTable(null)

        when:
        List<Record> records = session.select(basics.name, basics.numero)
                .from(basics)
                .orderBy(DSL.cbrt(basics.numero).asc())
                .fetchAll()

        then:
        records.size() == 4
        records[0].get(basics.numero) == 8    // cbrt = 2
        records[1].get(basics.numero) == 27   // cbrt = 3
        records[2].get(basics.numero) == 64   // cbrt = 4
        records[3].get(basics.numero) == 125  // cbrt = 5

        where:
        session << allSessions
    }

    def "filter using cbrt() in WHERE clause"() {
        given:
        TestFixtures fixtures = new TestFixtures(session)
        fixtures.addBasic(1L, "A", 8)    // cbrt(8) = 2
        fixtures.addBasic(2L, "B", 27)   // cbrt(27) = 3
        fixtures.addBasic(3L, "C", 64)   // cbrt(64) = 4
        fixtures.addBasic(4L, "D", 125)  // cbrt(125) = 5

        BasicsTable basics = new BasicsTable(null)

        when:
        // Select records where cbrt(numero) >= 3
        List<Record> records = session.select(basics.name, basics.numero)
                .from(basics)
                .where(DSL.cbrt(basics.numero).ge(3.0d))
                .orderBy(basics.numero.asc())
                .fetchAll()

        then:
        records.size() == 3
        records[0].get(basics.numero) == 27
        records[1].get(basics.numero) == 64
        records[2].get(basics.numero) == 125

        where:
        session << allSessions
    }

    def "select square() for BigDecimal"() {
        when:
            BigDecimal result = session.select(DSL.square(value)).fetchSingle()

        then:
            result.compareTo(expected) == 0
            result.getClass() == BigDecimal.class

        where:
            session << allSessions

        combined:
            value || expected
            (BigDecimal) 0 || new BigDecimal("0")
            (BigDecimal) 2.5 || new BigDecimal("6.25")
            (BigDecimal) -1.5 || new BigDecimal("2.25")
            new BigDecimal("4") || new BigDecimal("16")
    }

    def "select square()"() {
        when:
            def result = session.select(DSL.square(type, value)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (byte) 0 | Byte.class || (byte) 0
            (byte) 3 | Byte.class || (byte) 9
            (byte) -4 | Byte.class || (byte) 16
            (short) 5 | Short.class || (short) 25
            (short) -6 | Short.class || (short) 36
            (int) 6 | Integer.class || (int) 36
            (int) -7 | Integer.class || (int) 49
            (long) 8 | Long.class || (long) 64
            (long) -9 | Long.class || (long) 81
            (float) 2.5 | Float.class || (float) 6.25
            (float) -1.5 | Float.class || (float) 2.25
            (double) 1.5 | Double.class || (double) 2.25
            (double) -2.5 | Double.class || (double) 6.25
            (BigInteger) 9 | BigInteger.class || (BigInteger) 81
            (BigInteger) -10 | BigInteger.class || (BigInteger) 100
    }


    def "select square() composes inside arithmetic"() {
        when:
            Integer result = session.select(DSL.constant(10).add(DSL.square(DSL.constant(3)))).fetchSingle()

        then:
            result == 19

        where:
            session << allSessions
    }

    def "degrees() with direct constant overloads"() {
        when:
            def result = session.select(DSL.degrees(value)).fetchSingle()

        then:
            // PI radians = 180 degrees
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (Float) Math.PI.floatValue() | Float.class || (Float) 180.0f
            (Float) (Math.PI / 2).floatValue() | Float.class || (Float) 90.0f
            (Double) Math.PI | Double.class || (Double) 180.0d
            (Double) (Math.PI / 2) | Double.class || (Double) 90.0d
            new BigDecimal(Math.PI) | BigDecimal.class || new BigDecimal("180.0")
            new BigDecimal(Math.PI / 2) | BigDecimal.class || new BigDecimal("90.0")
    }

    def "select degrees()"() {
        when:
            def result = session.select(DSL.degrees(type, value)).fetchSingle()

        then:
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 0.5 | Float.class || (float) 28.648
            (double) 0.5 | Double.class || (double) 28.648
            (BigDecimal) 0.5 | BigDecimal.class || (BigDecimal) 28.648
    }

    def "select radians()"() {
        when:
            def result = session.select(DSL.radians(type, value)).fetchSingle()

        then:
            result - expected < 0.0001
            result - expected > -0.0001
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 45.0 | Float.class || (float) 0.7853
            (double) 45.0 | Double.class || (double) 0.7853
            (BigDecimal) 45.0 | BigDecimal.class || (BigDecimal) 0.7853
    }


    def "radians() with direct constant overloads"() {
        when:
            def result = session.select(DSL.radians(value)).fetchSingle()

        then:
            // 180 degrees = PI radians
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (Float) 180.0f | Float.class || (Float) Math.PI.floatValue()
            (Float) 90.0f | Float.class || (Float) (Math.PI / 2).floatValue()
            (Double) 180.0d | Double.class || (Double) Math.PI
            (Double) 90.0d | Double.class || (Double) (Math.PI / 2)
            new BigDecimal("180.0") | BigDecimal.class || new BigDecimal(Math.PI)
            new BigDecimal("90.0") | BigDecimal.class || new BigDecimal(Math.PI / 2)
    }

    def "select e()"() {
        when:
            def result = session.select(DSL.e()).fetchSingle()

        then:
            result - 2.71828 < 0.0001
            result - 2.71828 > -0.0001
            result.getClass() == Double.class

        where:
            session << allSessions
    }

    def "exp() with various constant values"() {
        when:
            def result = session.select(DSL.exp(type, value)).fetchSingle()

        then:
            (result - expected).abs() < 0.001
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value                        | type           || expected
            (float) 0.0                  | Float.class    || (float) 1.0
            (float) 1.0                  | Float.class    || (float) 2.718
            (double) 0.0                 | Double.class   || (double) 1.0
            (double) 2.0                 | Double.class   || (double) 7.389
            (BigDecimal) 0.0             | BigDecimal.class || (BigDecimal) 1.0
            (BigDecimal) 1.0             | BigDecimal.class || (BigDecimal) 2.718
    }

    def "exp() with constant values"() {
        when:
            def result = session.select(DSL.exp(value)).fetchSingle()

        then:
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 0.0 | Float.class || (float) 1.0
            (float) 1.0 | Float.class || (float) 2.718
            (float) 2.0 | Float.class || (float) 7.389
            (double) 0.0 | Double.class || (double) 1.0
            (double) 1.0 | Double.class || (double) 2.718
            (double) 2.0 | Double.class || (double) 7.389
            (BigDecimal) 0.0 | BigDecimal.class || (BigDecimal) 1.0
            (BigDecimal) 1.0 | BigDecimal.class || (BigDecimal) 2.718
    }

    def "select exp()"() {
        when:
            def result = session.select(DSL.exp(type, value)).fetchSingle()

        then:
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 0.0 | Float.class || (float) 1.0
            (double) 0.0 | Double.class || (double) 1.0
            (BigDecimal) 0.0 | BigDecimal.class || (BigDecimal) 1.0
            (float) 1.0 | Float.class || (float) 2.718
            (double) 1.0 | Double.class || (double) 2.718
            (BigDecimal) 1.0 | BigDecimal.class || (BigDecimal) 2.718
            (float) 2.0 | Float.class || (float) 7.389
            (double) 2.0 | Double.class || (double) 7.389
            (BigDecimal) 2.0 | BigDecimal.class || (BigDecimal) 7.389
    }

    def "exp() with Float constant value"() {
        when:
        def result = session.select(DSL.exp((float) 0.0)).fetchSingle()

        then:
        (result - (float) 1.0).abs() < 0.0001
        result.getClass() == Float.class

        where:
        session << allSessions
    }

    def "exp() with Double constant value"() {
        when:
        def result = session.select(DSL.exp((double) 1.0)).fetchSingle()

        then:
        (result - (double) 2.718).abs() < 0.001
        result.getClass() == Double.class

        where:
        session << allSessions
    }

    def "exp() with BigDecimal constant value"() {
        when:
        def result = session.select(DSL.exp(new BigDecimal("2.0"))).fetchSingle()

        then:
        // e^2 approx 7.389
        (result - new BigDecimal("7.389")).abs() < 0.001
        result.getClass() == BigDecimal.class

        where:
        session << allSessions
    }

    def "exp() on entity column"() {
        given:
        TestFixtures fixtures = new TestFixtures(session)
        fixtures.addBasic(1L, "A", 0)  // exp(0) = 1
        fixtures.addBasic(2L, "B", 1)  // exp(1) approx 2.718
        fixtures.addBasic(3L, "C", 2)  // exp(2) approx 7.389

        BasicsTable basics = new BasicsTable(null)
        // Cast to BigDecimal for cross-database compatibility (both H2 and PostgreSQL support numeric)
        Expression<BigDecimal> expExpression = DSL.exp(basics.numero.cast(BigDecimal.class))

        when:
        List<Record> records = session.select(basics.name, expExpression.as("exp_value"))
                .from(basics)
                .orderBy(basics.name.asc())
                .fetchAll()

        then:
        records.size() == 3
        with(records[0]) { r ->
            r.get(basics.name) == "A"
            def result = r.get("exp_value") as Number
            (result.doubleValue() - 1.0).abs() < 0.0001
        }
        with(records[1]) { r ->
            r.get(basics.name) == "B"
            def result = r.get("exp_value") as Number
            (result.doubleValue() - 2.718).abs() < 0.001
        }
        with(records[2]) { r ->
            r.get(basics.name) == "C"
            def result = r.get("exp_value") as Number
            (result.doubleValue() - 7.389).abs() < 0.001
        }

        where:
        session << allSessions
    }

    def "exp() on BigDecimal entity column"() {
        given:
        TestFixtures fixtures = new TestFixtures(session)
        fixtures.addProductA()  // priceNet=1.00
        fixtures.addProductB()  // priceNet=1.50

        ProductsTable products = new ProductsTable(null)
        Expression<BigDecimal> expExpression = DSL.exp(products.priceNet)

        when:
        List<Record> records = session.select(products.name, expExpression.as("exp_price"))
                .from(products)
                .orderBy(products.name.asc())
                .fetchAll()

        then:
        records.size() == 2
        with(records[0]) { r ->
            r.get(products.name) == "A"
            def result = r.get("exp_price") as BigDecimal
            // exp(1.00) approx 2.718
            (result - new BigDecimal("2.718")).abs() < 0.001
        }
        with(records[1]) { r ->
            r.get(products.name) == "B"
            def result = r.get("exp_price") as BigDecimal
            // exp(1.50) approx 4.482
            (result - new BigDecimal("4.482")).abs() < 0.001
        }

        where:
        session << allSessions
    }

    def "order by exp() descending"() {
        given:
        TestFixtures fixtures = new TestFixtures(session)
        fixtures.addBasic(1L, "A", 0)
        fixtures.addBasic(2L, "B", 1)
        fixtures.addBasic(3L, "C", 2)
        fixtures.addBasic(4L, "D", 3)

        BasicsTable basics = new BasicsTable(null)

        when:
        List<Record> records = session.select(basics.name, basics.numero)
                .from(basics)
                .orderBy(DSL.exp(basics.numero).desc())
                .fetchAll()

        then:
        records.size() == 4
        records[0].get(basics.numero) == 3  // exp(3) largest
        records[1].get(basics.numero) == 2
        records[2].get(basics.numero) == 1
        records[3].get(basics.numero) == 0  // exp(0) = 1 smallest

        where:
        session << allSessions
    }

    def "filter using exp() in WHERE clause"() {
        given:
        TestFixtures fixtures = new TestFixtures(session)
        fixtures.addBasic(1L, "A", 0)  // exp(0) = 1
        fixtures.addBasic(2L, "B", 1)  // exp(1) approx 2.718
        fixtures.addBasic(3L, "C", 2)  // exp(2) approx 7.389
        fixtures.addBasic(4L, "D", 3)  // exp(3) approx 20.086

        BasicsTable basics = new BasicsTable(null)

        when:
        // Select records where exp(numero) > 5
        List<Record> records = session.select(basics.name, basics.numero)
                .from(basics)
                .where(DSL.exp(basics.numero).gt(5.0d))
                .orderBy(basics.numero.asc())
                .fetchAll()

        then:
        records.size() == 2
        records[0].get(basics.numero) == 2
        records[1].get(basics.numero) == 3

        where:
        session << allSessions
    }

    def "select greatest with two integer args"() {
        when:
            Integer value = session.select(DSL.greatest(DSL.constant(left), DSL.constant(right))).fetchSingle()

        then:
            value == expected
            (value instanceof Integer)

        where:
            session << allSessions

        combined:
            left | right || expected
            5 | 3 || 5
            3 | 5 || 5
            -1 | -10 || -1
            0 | 0 || 0
            7 | 7 || 7
    }

    def "select greatest with multiple integer args"() {
        when:
            Integer value = session.select(DSL.greatest(DSL.constant(5), DSL.constant(3), DSL.constant(8))).fetchSingle()

        then:
            value == 8
            (value instanceof Integer)

        where:
            session << allSessions
    }

    def "select greatest with string args"() {
        when:
            String value = session.select(DSL.greatest(DSL.constant("apple"), DSL.constant("banana"), DSL.constant("cherry"))).fetchSingle()

        then:
            value == "cherry"

        where:
            session << allSessions
    }

    def "select greatest with BigDecimal args"() {
        when:
            BigDecimal value = session.select(DSL.greatest(DSL.constant(new BigDecimal("1.5")), DSL.constant(new BigDecimal("2.75")), DSL.constant(new BigDecimal("0.25")))).fetchSingle()

        then:
            value == new BigDecimal("2.75")

        where:
            session << allSessions
    }

    def "select greatest with no args throws"() {
        when:
            DSL.greatest()

        then:
            thrown(IllegalArgumentException)
    }

    def "select least with two integer args"() {
        when:
            Integer value = session.select(DSL.least(DSL.constant(left), DSL.constant(right))).fetchSingle()

        then:
            value == expected
            (value instanceof Integer)

        where:
            session << allSessions

        combined:
            left | right || expected
            5 | 3 || 3
            3 | 5 || 3
            -1 | -10 || -10
            0 | 0 || 0
            7 | 7 || 7
    }

    def "select least with multiple integer args"() {
        when:
            Integer value = session.select(DSL.least(DSL.constant(5), DSL.constant(3), DSL.constant(8))).fetchSingle()

        then:
            value == 3
            (value instanceof Integer)

        where:
            session << allSessions
    }

    def "select least with string args"() {
        when:
            String value = session.select(DSL.least(DSL.constant("apple"), DSL.constant("banana"), DSL.constant("cherry"))).fetchSingle()

        then:
            value == "apple"

        where:
            session << allSessions
    }

    def "select least with BigDecimal args"() {
        when:
            BigDecimal value = session.select(DSL.least(DSL.constant(new BigDecimal("1.5")), DSL.constant(new BigDecimal("2.75")), DSL.constant(new BigDecimal("0.25")))).fetchSingle()

        then:
            value == new BigDecimal("0.25")

        where:
            session << allSessions
    }

    def "select least with no args throws"() {
        when:
            DSL.least()

        then:
            thrown(IllegalArgumentException)
    }

    // ===========================================
    // LN - Natural Logarithm
    // ===========================================

    def "ln() with constant values"() {
        when:
            def result = session.select(DSL.ln(value)).fetchSingle()

        then:
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 1.0 | Float.class || (float) 0.0
            (float) 2.718 | Float.class || (float) 1.0
            (float) 7.389 | Float.class || (float) 2.0
            (double) 1.0 | Double.class || (double) 0.0
            (double) 2.718 | Double.class || (double) 1.0
            (double) 10.0 | Double.class || (double) 2.303
            (BigDecimal) 1.0 | BigDecimal.class || (BigDecimal) 0.0
            (BigDecimal) 2.718 | BigDecimal.class || (BigDecimal) 1.0
    }

    def "ln() with Float constant value"() {
        when:
            def result = session.select(DSL.ln((float) 1.0)).fetchSingle()

        then:
            (result - (float) 0.0).abs() < 0.0001
            result.getClass() == Float.class

        where:
            session << allSessions
    }

    def "ln() with Double constant value"() {
        when:
            def result = session.select(DSL.ln((double) Math.E)).fetchSingle()

        then:
            (result - (double) 1.0).abs() < 0.0001
            result.getClass() == Double.class

        where:
            session << allSessions
    }

    def "ln() with BigDecimal constant value"() {
        when:
            def result = session.select(DSL.ln(new BigDecimal("2.718281828"))).fetchSingle()

        then:
            (result - new BigDecimal("1.0")).abs() < 0.001
            result.getClass() == BigDecimal.class

        where:
            session << allSessions
    }

    def "ln() on entity column"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 1)   // ln(1) = 0
            fixtures.addBasic(2L, "B", 10)  // ln(10) approx 2.303

            BasicsTable basics = new BasicsTable(null)
            // Cast to BigDecimal for cross-database compatibility (both H2 and PostgreSQL support numeric)
            Expression<BigDecimal> lnExpression = DSL.ln(basics.numero.cast(BigDecimal.class))

        when:
            List<Record> records = session.select(basics.name, lnExpression.as("ln_value"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                def result = r.get("ln_value") as Number
                result.doubleValue().abs() < 0.0001  // ln(1) = 0
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                def result = r.get("ln_value") as Number
                (result.doubleValue() - 2.303).abs() < 0.001  // ln(10) approx 2.303
            }

        where:
            session << allSessions
    }

    def "ln() on BigDecimal entity column"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addProductA()  // priceGross=1.23
            fixtures.addProductB()  // priceGross=1.85

            ProductsTable products = new ProductsTable(null)
            Expression<BigDecimal> lnExpression = DSL.ln(products.priceGross)

        when:
            List<Record> records = session.select(products.name, lnExpression.as("ln_price"))
                    .from(products)
                    .orderBy(products.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(products.name) == "A"
                def result = r.get("ln_price") as BigDecimal
                // ln(1.23) approx 0.2070
                (result - new BigDecimal("0.2070")).abs() < 0.001
            }
            with(records[1]) { r ->
                r.get(products.name) == "B"
                def result = r.get("ln_price") as BigDecimal
                // ln(1.85) approx 0.6152
                (result - new BigDecimal("0.6152")).abs() < 0.001
            }

        where:
            session << allSessions
    }

    def "select ln()"() {
        when:
            def result = session.select(DSL.ln(type, value)).fetchSingle()

        then:
            result - expected < 0.0001
            result - expected > -0.0001
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 1.0 | Float.class || (float) 0.0
            (float) 2.0 | Float.class || (float) 0.6931
            (float) Math.E | Float.class || (float) 1.0
            (double) 1.0 | Double.class || (double) 0.0
            (double) 2.0 | Double.class || (double) 0.6931
            (double) Math.E | Double.class || (double) 1.0
            (BigDecimal) 1.0 | BigDecimal.class || (BigDecimal) 0.0
            (BigDecimal) 2.0 | BigDecimal.class || (BigDecimal) 0.6931
            new BigDecimal("10.0") | BigDecimal.class || (BigDecimal) 2.302585
    }

    def "ln() with various constant values"() {
        when:
        def result = session.select(DSL.ln(type, value)).fetchSingle()

        then:
        (result - expected).abs() < 0.001
        result.getClass() == type

        where:
        session << allSessions

        combined:
        value                        | type           || expected
        (float) 1.0                  | Float.class    || (float) 0.0
        (float) 2.0                  | Float.class    || (float) 0.6931
        (double) 1.0                 | Double.class   || (double) 0.0
        (double) 10.0                | Double.class   || (double) 2.303
        (BigDecimal) 1.0             | BigDecimal.class || (BigDecimal) 0.0
        (BigDecimal) 100.0           | BigDecimal.class || (BigDecimal) 4.605
    }

    // ===========================================
    // LOG - Logarithm with Custom Base
    // ===========================================

    def "log() with custom base"() {
        when:
            def result = session.select(DSL.log(DSL.constant(type, base), DSL.constant(type, value))).fetchSingle()

        then:
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == type

        where:
            session << allSessions

        combined:
            base | value | type || expected
            (float) 10.0 | (float) 100.0 | Float.class || (float) 2.0
            (float) 10.0 | (float) 1000.0 | Float.class || (float) 3.0
            (float) 2.0 | (float) 8.0 | Float.class || (float) 3.0
            (double) 10.0 | (double) 100.0 | Double.class || (double) 2.0
            (double) 2.0 | (double) 16.0 | Double.class || (double) 4.0
            (double) 3.0 | (double) 27.0 | Double.class || (double) 3.0
            (BigDecimal) 10.0 | (BigDecimal) 100.0 | BigDecimal.class || (BigDecimal) 2.0
            (BigDecimal) 2.0 | (BigDecimal) 32.0 | BigDecimal.class || (BigDecimal) 5.0
    }

    def "select log()"() {
        when:
            def result = session.select(DSL.log(type, base, x)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            base | x | type || expected
            (float) 2.0 | (float) 64.0 | Float.class || (float) 6.0
            (double) 2.0 | (double) 64.0 | Double.class || (double) 6.0
            (BigDecimal) 2.0 | (BigDecimal) 64.0 | BigDecimal.class || (BigDecimal) 6.0
    }

    def "log() with direct constant overloads"() {
        when:
            def result = session.select(DSL.log(base, x)).fetchSingle()

        then:
            result - expected < 0.001
            result - expected > -0.001
            result.getClass() == type

        where:
            session << allSessions

        combined:
            base | x | type || expected
            (Float) 10.0f | (Float) 100.0f | Float.class || (Float) 2.0f
            (Float) 2.0f | (Float) 8.0f | Float.class || (Float) 3.0f
            (Float) 10.0f | (Float) 1000.0f | Float.class || (Float) 3.0f
            (Double) 10.0d | (Double) 100.0d | Double.class || (Double) 2.0d
            (Double) 2.0d | (Double) 16.0d | Double.class || (Double) 4.0d
            (Double) 3.0d | (Double) 27.0d | Double.class || (Double) 3.0d
            new BigDecimal("10.0") | new BigDecimal("1000.0") | BigDecimal.class || new BigDecimal("3.0")
            new BigDecimal("2.0") | new BigDecimal("64.0") | BigDecimal.class || new BigDecimal("6.0")
    }

    // ===========================================
    // LOG10 - Base-10 Logarithm
    // ===========================================

    def "log10() with Float constant value"() {
        when:
            def result = session.select(DSL.log10((float) 100.0)).fetchSingle()

        then:
            result == (float) 2.0
            result.getClass() == Float.class

        where:
            session << allSessions
    }

    def "log10() with Double constant value"() {
        when:
            def result = session.select(DSL.log10((double) 1000.0)).fetchSingle()

        then:
            result == (double) 3.0
            result.getClass() == Double.class

        where:
            session << allSessions
    }

    def "log10() with BigDecimal constant value"() {
        when:
            def result = session.select(DSL.log10(new BigDecimal("10000.0"))).fetchSingle()

        then:
            result == new BigDecimal("4.0")
            result.getClass() == BigDecimal.class

        where:
            session << allSessions
    }

    def "log10() on entity column with powers of 10"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)    // log10(10) = 1
            fixtures.addBasic(2L, "B", 100)   // log10(100) = 2
            fixtures.addBasic(3L, "C", 1000)  // log10(1000) = 3

            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> log10Expression = DSL.log10(basics.numero)

        when:
            List<Record> records = session.select(basics.name, log10Expression.as("log_value"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                def result = r.get("log_value") as Number
                (result.doubleValue() - 1.0).abs() < 0.0001
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                def result = r.get("log_value") as Number
                (result.doubleValue() - 2.0).abs() < 0.0001
            }
            with(records[2]) { r ->
                r.get(basics.name) == "C"
                def result = r.get("log_value") as Number
                (result.doubleValue() - 3.0).abs() < 0.0001
            }

        where:
            session << allSessions
    }

    def "log10() on BigDecimal entity column"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addProductA()  // priceNet=1.00
            fixtures.addProductB()  // priceNet=1.50

            ProductsTable products = new ProductsTable(null)
            Expression<BigDecimal> log10Expression = DSL.log10(products.priceNet)

        when:
            List<Record> records = session.select(products.name, log10Expression.as("log_price"))
                    .from(products)
                    .orderBy(products.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(products.name) == "A"
                def result = r.get("log_price") as BigDecimal
                // log10(1.00) = 0
                result.abs() < 0.0001
            }
            with(records[1]) { r ->
                r.get(products.name) == "B"
                def result = r.get("log_price") as BigDecimal
                // log10(1.50) approx 0.1761
                (result - new BigDecimal("0.1761")).abs() < 0.001
            }

        where:
            session << allSessions
    }

    def "select log10()"() {
        when:
            def result = session.select(DSL.log10(type, value)).fetchSingle()

        then:
            result == expected
            result.getClass() == type

        where:
            session << allSessions

        combined:
            value | type || expected
            (float) 1000.0 | Float.class || (float) 3.0
            (double) 1000.0 | Double.class || (double) 3.0
            (BigDecimal) 1000.0 | BigDecimal.class || (BigDecimal) 3.0
    }

    def "log10() with various constant values"() {
        when:
        def result = session.select(DSL.log10(type, value)).fetchSingle()

        then:
        (result - expected).abs() < 0.0001
        result.getClass() == type

        where:
        session << allSessions

        combined:
        value                        | type           || expected
        (float) 1.0                  | Float.class    || (float) 0.0
        (float) 10.0                 | Float.class    || (float) 1.0
        (float) 100.0                | Float.class    || (float) 2.0
        (double) 1000.0              | Double.class   || (double) 3.0
        (double) 10000.0             | Double.class   || (double) 4.0
        (BigDecimal) 100000.0        | BigDecimal.class || (BigDecimal) 5.0
    }

    def "order by log10() ascending"() {
        given:
        TestFixtures fixtures = new TestFixtures(session)
        fixtures.addBasic(1L, "D", 1000)
        fixtures.addBasic(2L, "B", 10)
        fixtures.addBasic(3L, "C", 100)
        fixtures.addBasic(4L, "A", 1)

        BasicsTable basics = new BasicsTable(null)

        when:
        List<Record> records = session.select(basics.name, basics.numero)
                .from(basics)
                .orderBy(DSL.log10(basics.numero).asc())
                .fetchAll()

        then:
        records.size() == 4
        records[0].get(basics.numero) == 1     // log10(1) = 0
        records[1].get(basics.numero) == 10    // log10(10) = 1
        records[2].get(basics.numero) == 100   // log10(100) = 2
        records[3].get(basics.numero) == 1000  // log10(1000) = 3

        where:
        session << allSessions
    }

    def "filter using log10() in WHERE clause"() {
        given:
        TestFixtures fixtures = new TestFixtures(session)
        fixtures.addBasic(1L, "A", 1)     // log10(1) = 0
        fixtures.addBasic(2L, "B", 10)    // log10(10) = 1
        fixtures.addBasic(3L, "C", 100)   // log10(100) = 2
        fixtures.addBasic(4L, "D", 1000)  // log10(1000) = 3
        fixtures.addBasic(5L, "E", 10000) // log10(10000) = 4

        BasicsTable basics = new BasicsTable(null)

        when:
        // Select records where log10(numero) >= 2 (i.e., numero >= 100)
        List<Record> records = session.select(basics.name, basics.numero)
                .from(basics)
                .where(DSL.log10(basics.numero).ge(2.0d))
                .orderBy(basics.numero.asc())
                .fetchAll()

        then:
        records.size() == 3
        records[0].get(basics.numero) == 100
        records[1].get(basics.numero) == 1000
        records[2].get(basics.numero) == 10000

        where:
        session << allSessions
    }

    def "select pi()"() {
        when:
            def result = session.select(DSL.pi()).fetchSingle()

        then:
            result - 3.1416 < 0.0001
            result - 3.1416 > -0.0001
            result.getClass() == Double.class

        where:
            session << allSessions
    }

    // ===========================================
    // Combined Math Operations
    // ===========================================

    def "combined math operations: sqrt(power(numero, 2))"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 5)
            fixtures.addBasic(2L, "B", 10)
            fixtures.addBasic(3L, "C", 15)

            BasicsTable basics = new BasicsTable(null)
            // sqrt(numero^2) should equal abs(numero)
            Expression<BigDecimal> powerExpr = DSL.power(basics.numero, DSL.constant(2))
            Expression<BigDecimal> sqrtPowerExpr = DSL.sqrt(powerExpr.cast(BigDecimal.class))

        when:
            List<Record> records = session.select(basics.name, basics.numero, sqrtPowerExpr.as("result"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                def result = r.get("result") as BigDecimal
                result.intValue() == 5
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                def result = r.get("result") as BigDecimal
                result.intValue() == 10
            }
            with(records[2]) { r ->
                r.get(basics.name) == "C"
                def result = r.get("result") as BigDecimal
                result.intValue() == 15
            }

        where:
            session << allSessions
    }

    // ===========================================
    // Combined Math Operations
    // ===========================================

    def "combined: ln(exp(x)) should return x (identity)"() {
        given:
        TestFixtures fixtures = new TestFixtures(session)
        fixtures.addBasic(1L, "A", 1)
        fixtures.addBasic(2L, "B", 2)
        fixtures.addBasic(3L, "C", 3)

        BasicsTable basics = new BasicsTable(null)
        // ln(exp(x)) = x
        Expression<Integer> expExpr = DSL.exp(basics.numero)
        Expression<BigDecimal> lnExpExpr = DSL.ln(expExpr.cast(BigDecimal.class))

        when:
        List<Record> records = session.select(basics.name, basics.numero, lnExpExpr.as("result"))
                .from(basics)
                .orderBy(basics.name.asc())
                .fetchAll()

        then:
        records.size() == 3
        records.each { r ->
            def original = r.get(basics.numero) as Number
            def result = r.get("result") as BigDecimal
            (result.doubleValue() - original.doubleValue()).abs() < 0.0001
        }

        where:
        session << allSessions
    }

    def "combined: cbrt(numero * numero * numero) returns numero (for positive values)"() {
        given:
        TestFixtures fixtures = new TestFixtures(session)
        fixtures.addBasic(1L, "A", 2)
        fixtures.addBasic(2L, "B", 3)
        fixtures.addBasic(3L, "C", 4)

        BasicsTable basics = new BasicsTable(null)
        // cbrt(x^3) = x - cast to BigDecimal for cross-database compatibility
        Expression<Integer> cubedExpr = basics.numero.multiply(basics.numero).multiply(basics.numero)
        Expression<BigDecimal> cbrtCubedExpr = DSL.cbrt(cubedExpr.cast(BigDecimal.class))

        when:
        List<Record> records = session.select(basics.name, basics.numero, cbrtCubedExpr.as("result"))
                .from(basics)
                .orderBy(basics.name.asc())
                .fetchAll()

        then:
        records.size() == 3
        with(records[0]) { r ->
            r.get(basics.name) == "A"
            def result = r.get("result") as Number
            (result.doubleValue() - 2.0).abs() < 0.0001
        }
        with(records[1]) { r ->
            r.get(basics.name) == "B"
            def result = r.get("result") as Number
            (result.doubleValue() - 3.0).abs() < 0.0001
        }
        with(records[2]) { r ->
            r.get(basics.name) == "C"
            def result = r.get("result") as Number
            (result.doubleValue() - 4.0).abs() < 0.0001
        }

        where:
        session << allSessions
    }

    def "combined: log10(10^numero) returns numero"() {
        given:
        TestFixtures fixtures = new TestFixtures(session)
        fixtures.addBasic(1L, "A", 1)  // 10^1 = 10, log10(10) = 1
        fixtures.addBasic(2L, "B", 2)  // 10^2 = 100, log10(100) = 2
        fixtures.addBasic(3L, "C", 3)  // 10^3 = 1000, log10(1000) = 3

        BasicsTable basics = new BasicsTable(null)
        Expression<BigDecimal> powerOf10 = DSL.power(DSL.constant(10), basics.numero)
        Expression<BigDecimal> log10Result = DSL.log10(powerOf10)

        when:
        List<Record> records = session.select(basics.name, basics.numero, log10Result.as("result"))
                .from(basics)
                .orderBy(basics.name.asc())
                .fetchAll()

        then:
        records.size() == 3
        records.each { r ->
            def original = r.get(basics.numero) as Number
            def result = r.get("result") as BigDecimal
            (result.doubleValue() - original.doubleValue()).abs() < 0.0001
        }

        where:
        session << allSessions
    }
}
