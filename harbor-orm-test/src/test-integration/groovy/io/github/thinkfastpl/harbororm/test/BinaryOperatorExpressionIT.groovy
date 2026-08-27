// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record

class BinaryOperatorExpressionIT extends AbstractHarborIT {

    def "addition"() {
        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(expression) == result
            if (result != null) {
                assert record.get(1).class == result_type
            }

        where:
            session << allSessions

        combined:
            expression | result | result_type
            DSL.constant(11).add(DSL.constant(5)) | 16 | Integer.class
            DSL.constant(11).add(DSL.constant(5)).add(-2) | 14 | Integer.class
            DSL.constant(11L).add(DSL.constant(5L)) | 16 | Long.class
            DSL.constant((byte) 11).add(DSL.constant((byte) 5)) | (byte) 16 | Byte.class
            DSL.constant((short) 11).add(DSL.constant((short) 5)) | (short) 16 | Short.class
            DSL.constant(11f).add(DSL.constant(5f)) | 16f | Float.class
            DSL.constant(11d).add(DSL.constant(5d)) | 16d | Double.class
            DSL.constant(BigInteger.valueOf(11)).add(DSL.constant(BigInteger.valueOf(5))) | BigInteger.valueOf(16) | BigInteger.class
            DSL.constant(BigDecimal.valueOf(11)).add(DSL.constant(BigDecimal.valueOf(5))) | BigDecimal.valueOf(16) | BigDecimal.class
    }

    def "subtraction"() {
        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(expression) == result
            if (result != null) {
                assert record.get(1).class == result_type
            }

        where:
            session << allSessions

        combined:
            expression | result | result_type
            DSL.constant(11).subtract(DSL.constant(5)) | 6 | Integer.class
            DSL.constant(11).subtract(DSL.constant(5)).subtract(-2) | 8 | Integer.class
            DSL.constant(11L).subtract(DSL.constant(5L)) | 6 | Long.class
            DSL.constant((byte) 11).subtract(DSL.constant((byte) 5)) | (byte) 6 | Byte.class
            DSL.constant((short) 11).subtract(DSL.constant((short) 5)) | (short) 6 | Short.class
            DSL.constant(11f).subtract(DSL.constant(5f)) | 6f | Float.class
            DSL.constant(11d).subtract(DSL.constant(5d)) | 6d | Double.class
            DSL.constant(BigInteger.valueOf(11)).subtract(DSL.constant(BigInteger.valueOf(5))) | BigInteger.valueOf(6) | BigInteger.class
            DSL.constant(BigDecimal.valueOf(11)).subtract(DSL.constant(BigDecimal.valueOf(5))) | BigDecimal.valueOf(6) | BigDecimal.class
    }

    def "division"() {
        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(expression) == result
            if (result != null) {
                assert record.get(1).class == result_type
            }

        where:
            session << allSessions

        combined:
            expression | result | result_type
            DSL.constant(6).divide(DSL.constant(2)) | 3 | Integer.class
            DSL.constant(66L).divide(DSL.constant(2L)).divide(3) | 11 | Long.class
            DSL.constant(6L).divide(DSL.constant(2L)) | 3 | Long.class
            DSL.constant((byte) 6).divide(DSL.constant((byte) 2)) | (byte) 3 | Byte.class
            DSL.constant((short) 6).divide(DSL.constant((short) 2)) | (short) 3 | Short.class
            DSL.constant(6f).divide(DSL.constant(2f)) | 3f | Float.class
            DSL.constant(6d).divide(DSL.constant(2d)) | 3d | Double.class
            DSL.constant(BigInteger.valueOf(6)).divide(DSL.constant(BigInteger.valueOf(2))) | BigInteger.valueOf(3) | BigInteger.class
            DSL.constant(BigDecimal.valueOf(6)).divide(DSL.constant(BigDecimal.valueOf(2))) | BigDecimal.valueOf(3) | BigDecimal.class
    }

    def "multiplication"() {
        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(expression) == result
            if (result != null) {
                assert record.get(1).class == result_type
            }

        where:
            session << allSessions

        combined:
            expression | result | result_type
            DSL.constant(6).multiply(DSL.constant(2)) | 12 | Integer.class
            DSL.constant(6).multiply(DSL.constant(2)).multiply(4) | 48 | Integer.class
            DSL.constant(6L).multiply(DSL.constant(2L)) | 12 | Long.class
            DSL.constant((byte) 6).multiply(DSL.constant((byte) 2)) | (byte) 12 | Byte.class
            DSL.constant((short) 6).multiply(DSL.constant((short) 2)) | (short) 12 | Short.class
            DSL.constant(6f).multiply(DSL.constant(2f)) | 12f | Float.class
            DSL.constant(6d).multiply(DSL.constant(2d)) | 12d | Double.class
            DSL.constant(BigInteger.valueOf(6)).multiply(DSL.constant(BigInteger.valueOf(2))) | BigInteger.valueOf(12) | BigInteger.class
            DSL.constant(BigDecimal.valueOf(6)).multiply(DSL.constant(BigDecimal.valueOf(2))) | BigDecimal.valueOf(12) | BigDecimal.class
    }

    def "modulo"() {
        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(expression) == result
            if (result != null) {
                assert record.get(1).class == result_type
            }

        where:
            session << allSessions

        combined:
            expression | result | result_type
            DSL.constant(13).modulo(DSL.constant(10)) | 3 | Integer.class
            DSL.constant(13L).modulo(DSL.constant(10L)) | 3 | Long.class
            DSL.constant((byte) 13).modulo(DSL.constant((byte) 10)) | (byte) 3 | Byte.class
            DSL.constant((short) 13).modulo(DSL.constant((short) 10)) | (short) 3 | Short.class
            DSL.constant(BigInteger.valueOf(13)).modulo(DSL.constant(BigInteger.valueOf(10))) | BigInteger.valueOf(3) | BigInteger.class
    }
}
