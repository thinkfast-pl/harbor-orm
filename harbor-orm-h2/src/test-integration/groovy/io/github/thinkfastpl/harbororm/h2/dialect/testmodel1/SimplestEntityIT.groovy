// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect.testmodel1

import io.github.thinkfastpl.harbororm.h2.dialect.H2DialectBaseIT

class SimplestEntityIT extends H2DialectBaseIT {

    void setup() {
        loadScript("simples.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "should insert and select all primitive and boxed types"() {
        given:
            QSimplestEntity qSimplestEntity = new QSimplestEntity("s")
            SimplestEntity simplestEntity = new SimplestEntity()
                .setId(1L)
                .setMyBoolean(true)
                .setMyBigBoolean(Boolean.FALSE)
                .setMySmallByte((byte) 2)
                .setMyBigByte((byte) 3)
                .setMyShort((short) 4)
                .setMyBigShort((short) 5)
                .setMyInt(6)
                .setMyBigInt(7)
                .setMyLong(8L)
                .setMyBigLong(9L)
                .setMyFloat(10.1f)
                .setMyBigFloat(11.2f)
                .setMyDouble(12.1D)
                .setMyBigDouble(13.2D)
                .setMyBigInteger(BigInteger.valueOf(14))
                .setMyBigDecimal(new BigDecimal("15.16"))
                .setVarcharValue("abc")

        when:
            session.insertEntity(qSimplestEntity, simplestEntity)

        then:
            session.selectEntity(qSimplestEntity).count() == 1

        when:
            SimplestEntity readEntity = session.selectEntity(qSimplestEntity)
                .where(qSimplestEntity.id.eq(1L))
                .fetchSingle()

        then:
            readEntity != null
            with(readEntity) { e ->
                e.id == 1L
                e.myBoolean
                e.myBigBoolean == Boolean.FALSE
                e.mySmallByte == (byte) 2
                e.myBigByte == (byte) 3
                e.myShort == (short) 4
                e.myBigShort == (short) 5
                e.myInt == 6
                e.myBigInt == 7
                e.myLong == 8L
                e.myBigLong == 9L
                e.myFloat == 10.1f
                e.myBigFloat == 11.2f
                e.myDouble == 12.1D
                e.myBigDouble == 13.2D
                e.myBigInteger == BigInteger.valueOf(14)
                e.myBigDecimal == new BigDecimal("15.16")
                e.varcharValue == "abc"
            }
    }

    def "should update all primitive and boxed types"() {
        given:
            QSimplestEntity qSimplestEntity = new QSimplestEntity("s")
            SimplestEntity simplestEntity = new SimplestEntity()
                .setId(1L)
                .setMyBoolean(true)
                .setMyBigBoolean(Boolean.FALSE)
                .setMySmallByte((byte) 2)
                .setMyBigByte((byte) 3)
                .setMyShort((short) 4)
                .setMyBigShort((short) 5)
                .setMyInt(6)
                .setMyBigInt(7)
                .setMyLong(8L)
                .setMyBigLong(9L)
                .setMyFloat(10.1f)
                .setMyBigFloat(11.2f)
                .setMyDouble(12.1D)
                .setMyBigDouble(13.2D)
                .setMyBigInteger(BigInteger.valueOf(14))
                .setMyBigDecimal(new BigDecimal("15.16"))
                .setVarcharValue("abc")
            session.insertEntity(qSimplestEntity, simplestEntity)

        when:
            simplestEntity
                .setMyBoolean(false)
                .setMyBigBoolean(Boolean.TRUE)
                .setMySmallByte((byte) 3)
                .setMyBigByte((byte) 4)
                .setMyShort((short) 5)
                .setMyBigShort((short) 6)
                .setMyInt(7)
                .setMyBigInt(8)
                .setMyLong(9L)
                .setMyBigLong(10L)
                .setMyFloat(11.1f)
                .setMyBigFloat(12.2f)
                .setMyDouble(13.1D)
                .setMyBigDouble(14.2D)
                .setMyBigInteger(BigInteger.valueOf(15))
                .setMyBigDecimal(new BigDecimal("16.17"))
                .setVarcharValue("abcd")
            session.updateEntity(qSimplestEntity, simplestEntity)

        then:
            with(
                session.selectEntity(qSimplestEntity)
                    .where(qSimplestEntity.id.eq(1L))
                    .fetchSingle()
            ) { e ->
                e.id == 1L
                !e.myBoolean
                e.myBigBoolean == Boolean.TRUE
                e.mySmallByte == (byte) 3
                e.myBigByte == (byte) 4
                e.myShort == (short) 5
                e.myBigShort == (short) 6
                e.myInt == 7
                e.myBigInt == 8
                e.myLong == 9L
                e.myBigLong == 10L
                e.myFloat == 11.1f
                e.myBigFloat == 12.2f
                e.myDouble == 13.1D
                e.myBigDouble == 14.2D
                e.myBigInteger == BigInteger.valueOf(15)
                e.myBigDecimal == new BigDecimal("16.17")
                e.varcharValue == "abcd"
            }
    }

    def "should not update when db is empty"() {
        given:
            QSimplestEntity qSimplestEntity = new QSimplestEntity("s")
            SimplestEntity simplestEntity = new SimplestEntity()
                .setId(1L)
                .setMyBoolean(true)
                .setMyBigBoolean(Boolean.FALSE)
                .setMySmallByte((byte) 2)
                .setMyBigByte((byte) 3)
                .setMyShort((short) 4)
                .setMyBigShort((short) 5)
                .setMyInt(6)
                .setMyBigInt(7)
                .setMyLong(8L)
                .setMyBigLong(9L)
                .setMyFloat(10.1f)
                .setMyBigFloat(11.2f)
                .setMyDouble(12.1D)
                .setMyBigDouble(13.2D)
                .setMyBigInteger(BigInteger.valueOf(14))
                .setMyBigDecimal(new BigDecimal("15.16"))
                .setVarcharValue("abc")

        when:
            session.updateEntity(qSimplestEntity, simplestEntity)

        then:
            session.selectEntity(qSimplestEntity).count() == 0
    }
}
