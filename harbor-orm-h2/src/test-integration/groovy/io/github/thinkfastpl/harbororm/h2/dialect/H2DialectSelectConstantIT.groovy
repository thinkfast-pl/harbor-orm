// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record

class H2DialectSelectConstantIT extends H2DialectBaseIT {

    def "select one constant without alias"() {
        when:
            List<Record> records = session.select([expression]).fetchAll()

        then:
            records.size() == 1
            with(records.get(0)) { r ->
                r.get(1) == value
                r.get(1).getClass() == type
                r.get(expression) == value
                r.get(expression).getClass() == type
            }

        where:
            value                     | expression                              | type
            10                        | DSL.constant(10)                        | Integer.class
            11L                       | DSL.constant(11L)                       | Long.class
            (short) 12                | DSL.constant((short) 12)                | Short.class
            (byte) 13                 | DSL.constant((byte) 13)                 | Byte.class
            14.1F                     | DSL.constant(14.1F)                     | Float.class
            15.2D                     | DSL.constant(15.2D)                     | Double.class
            true                      | DSL.constant(true)                      | Boolean.class
            false                     | DSL.constant(false)                     | Boolean.class
            'x'                       | DSL.constant('x' as char)               | Character.class
            'abc'                     | DSL.constant('abc')                     | String.class
            BigInteger.valueOf(1234L) | DSL.constant(BigInteger.valueOf(1234L)) | BigInteger.class
            new BigDecimal("123.45")  | DSL.constant(new BigDecimal("123.45"))  | BigDecimal.class
    }

    def "select one constant with alias"() {
        when:
            List<Record> records = session.select([expression]).fetchAll()

        then:
            records.size() == 1
            with(records.get(0)) { r ->
                r.get(1) == value
                r.get(1).getClass() == type
                r.get(expression) == value
                r.get(expression).getClass() == type
                r.get("my_alias") == value
                r.get("my_alias").getClass() == type
            }

        where:
            value                     | expression                                             | type
            10                        | DSL.constant(10).as("my_alias")                        | Integer.class
            11L                       | DSL.constant(11L).as("my_alias")                       | Long.class
            (short) 12                | DSL.constant((short) 12).as("my_alias")                | Short.class
            (byte) 13                 | DSL.constant((byte) 13).as("my_alias")                 | Byte.class
            14.1F                     | DSL.constant(14.1F).as("my_alias")                     | Float.class
            15.2D                     | DSL.constant(15.2D).as("my_alias")                     | Double.class
            true                      | DSL.constant(true).as("my_alias")                      | Boolean.class
            false                     | DSL.constant(false).as("my_alias")                     | Boolean.class
            'x'                       | DSL.constant('x' as char).as("my_alias")               | Character.class
            'abc'                     | DSL.constant('abc').as("my_alias")                     | String.class
            BigInteger.valueOf(1234L) | DSL.constant(BigInteger.valueOf(1234L)).as("my_alias") | BigInteger.class
            new BigDecimal("123.45")  | DSL.constant(new BigDecimal("123.45")).as("my_alias")  | BigDecimal.class
    }

    def "select big integer constant larger than Long.MAX_VALUE"() {
        given:
            BigInteger bigValue = new BigInteger("99999999999999999999999999999")

        when:
            BigInteger result = session.select(DSL.constant(bigValue)).fetchSingle()

        then:
            result == bigValue
            result instanceof BigInteger
    }

    def "select multiple without alias"() {
        given:
            Expression<Integer> integerExpression = DSL.constant(10)
            Expression<Long> longExpression = DSL.constant(11L)
            Expression<Short> shortExpression = DSL.constant((short) 12)
            Expression<Byte> byteExpression = DSL.constant((byte) 13)
            Expression<Float> floatExpression = DSL.constant(14.1F)
            Expression<Double> doubleExpression = DSL.constant(15.2D)
            Expression<Boolean> booleanExpressionTrue = DSL.constant(true)
            Expression<Boolean> booleanExpressionFalse = DSL.constant(false)
            Expression<Character> characterExpression = DSL.constant('x' as char)
            Expression<String> stringExpression = DSL.constant('abc')

        when:
            List<Record> records = session.select(
                integerExpression,
                longExpression,
                shortExpression,
                byteExpression,
                floatExpression,
                doubleExpression,
                booleanExpressionTrue,
                booleanExpressionFalse,
                characterExpression,
                stringExpression
            ).fetchAll()

        then:
            records.size() == 1
            with(records.get(0)) { r ->
                r.get(1) == 10
                r.get(1).getClass() == Integer.class
                r.get(integerExpression) == 10
                r.get(integerExpression).getClass() == Integer.class

                r.get(2) == 11L
                r.get(2).getClass() == Long.class
                r.get(longExpression) == 11L
                r.get(longExpression).getClass() == Long.class

                r.get(3) == (short) 12
                r.get(3).getClass() == Short.class
                r.get(shortExpression) == (short) 12
                r.get(shortExpression).getClass() == Short.class

                r.get(4) == (byte) 13
                r.get(4).getClass() == Byte.class
                r.get(byteExpression) == (byte) 13
                r.get(byteExpression).getClass() == Byte.class

                r.get(5) == 14.1F
                r.get(5).getClass() == Float.class
                r.get(floatExpression) == 14.1F
                r.get(floatExpression).getClass() == Float.class

                r.get(6) == 15.2
                r.get(6).getClass() == Double.class
                r.get(doubleExpression) == 15.2
                r.get(doubleExpression).getClass() == Double.class

                r.get(7) == true
                r.get(7).getClass() == Boolean.class
                r.get(booleanExpressionTrue)
                r.get(booleanExpressionTrue).getClass() == Boolean.class

                r.get(8) == false
                r.get(8).getClass() == Boolean.class
                !r.get(booleanExpressionFalse)
                r.get(booleanExpressionFalse).getClass() == Boolean.class

                r.get(9) == 'x' as char
                r.get(9).getClass() == Character.class
                r.get(characterExpression) == 'x' as char
                r.get(characterExpression).getClass() == Character.class

                r.get(10) == 'abc'
                r.get(10).getClass() == String.class
                r.get(stringExpression) == 'abc'
                r.get(stringExpression).getClass() == String.class
            }
    }

    def "select multiple with alias"() {
        given:
            Expression<Integer> integerExpression = DSL.constant(10).as("a1")
            Expression<Long> longExpression = DSL.constant(11L).as("a2")
            Expression<Short> shortExpression = DSL.constant((short) 12).as("a3")
            Expression<Byte> byteExpression = DSL.constant((byte) 13).as("a4")
            Expression<Float> floatExpression = DSL.constant(14.1F).as("a5")
            Expression<Double> doubleExpression = DSL.constant(15.2D).as("a6")
            Expression<Boolean> booleanExpressionTrue = DSL.constant(true).as("a7")
            Expression<Boolean> booleanExpressionFalse = DSL.constant(false).as("a8")
            Expression<Character> characterExpression = DSL.constant('x' as char).as("a9")
            Expression<String> stringExpression = DSL.constant('abc').as("a10")

        when:
            List<Record> records = session.select(
                integerExpression,
                longExpression,
                shortExpression,
                byteExpression,
                floatExpression,
                doubleExpression,
                booleanExpressionTrue,
                booleanExpressionFalse,
                characterExpression,
                stringExpression
            ).fetchAll()

        then:
            records.size() == 1
            with(records.get(0)) { r ->
                r.get(1) == 10
                r.get(1).getClass() == Integer.class
                r.get(integerExpression) == 10
                r.get(integerExpression).getClass() == Integer.class
                r.get("a1") == 10
                r.get("a1").getClass() == Integer.class

                r.get(2) == 11L
                r.get(2).getClass() == Long.class
                r.get(longExpression) == 11L
                r.get(longExpression).getClass() == Long.class
                r.get("a2") == 11L
                r.get("a2").getClass() == Long.class

                r.get(3) == (short) 12
                r.get(3).getClass() == Short.class
                r.get(shortExpression) == (short) 12
                r.get(shortExpression).getClass() == Short.class
                r.get("a3") == (short) 12
                r.get("a3").getClass() == Short.class

                r.get(4) == (byte) 13
                r.get(4).getClass() == Byte.class
                r.get(byteExpression) == (byte) 13
                r.get(byteExpression).getClass() == Byte.class
                r.get("a4") == (short) (byte) 13
                r.get("a4").getClass() == Byte.class

                r.get(5) == 14.1F
                r.get(5).getClass() == Float.class
                r.get(floatExpression) == 14.1F
                r.get(floatExpression).getClass() == Float.class
                r.get("a5") == 14.1F
                r.get("a5").getClass() == Float.class

                r.get(6) == 15.2
                r.get(6).getClass() == Double.class
                r.get(doubleExpression) == 15.2
                r.get(doubleExpression).getClass() == Double.class
                r.get("a6") == 15.2
                r.get("a6").getClass() == Double.class

                r.get(7) == true
                r.get(7).getClass() == Boolean.class
                r.get(booleanExpressionTrue)
                r.get(booleanExpressionTrue).getClass() == Boolean.class
                r.get("a7")
                r.get("a7").getClass() == Boolean.class

                r.get(8) == false
                r.get(8).getClass() == Boolean.class
                !r.get(booleanExpressionFalse)
                r.get(booleanExpressionFalse).getClass() == Boolean.class
                !r.get("a8")
                r.get("a8").getClass() == Boolean.class

                r.get(9) == 'x' as char
                r.get(9).getClass() == Character.class
                r.get(characterExpression) == 'x' as char
                r.get(characterExpression).getClass() == Character.class
                r.get("a9") == 'x' as char
                r.get("a9").getClass() == Character.class

                r.get(10) == 'abc'
                r.get(10).getClass() == String.class
                r.get(stringExpression) == 'abc'
                r.get(stringExpression).getClass() == String.class
                r.get("a10") == 'abc'
                r.get("a10").getClass() == String.class
            }
    }
}
