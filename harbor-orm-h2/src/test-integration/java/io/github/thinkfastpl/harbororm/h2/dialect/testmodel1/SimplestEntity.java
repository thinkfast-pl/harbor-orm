// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect.testmodel1;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.BigInteger;

@Entity(table = "simples")
@Data
@Accessors(chain = true)
public class SimplestEntity {

    @Id
    private Long id;

    @Column(name = "my_boolean", nullable = false)
    private boolean myBoolean;

    @Column(name = "my_big_boolean", nullable = true)
    private Boolean myBigBoolean;

    @Column(name = "my_small_byte", nullable = false)
    private byte mySmallByte;

    @Column(name = "my_big_byte", nullable = true)
    private Byte myBigByte;

    @Column(name = "my_short", nullable = false)
    private short myShort;

    @Column(name = "my_big_short", nullable = true)
    private Short myBigShort;

    @Column(name = "my_int", nullable = false)
    private int myInt;

    @Column(name = "my_big_int", nullable = true)
    private Integer myBigInt;

    @Column(name = "my_long", nullable = false)
    private long myLong;

    @Column(name = "my_big_long", nullable = true)
    private Long myBigLong;

    @Column(name = "my_float", nullable = false)
    private float myFloat;

    @Column(name = "my_big_float", nullable = true)
    private Float myBigFloat;

    @Column(name = "my_double", nullable = false)
    private double myDouble;

    @Column(name = "my_big_double", nullable = true)
    private Double myBigDouble;

    @Column(name = "my_big_integer", nullable = true)
    private BigInteger myBigInteger;

    @Column(name = "my_big_decimal", nullable = true)
    private BigDecimal myBigDecimal;

    @Column(name = "varchar_value", nullable = false)
    private String varcharValue;
}
