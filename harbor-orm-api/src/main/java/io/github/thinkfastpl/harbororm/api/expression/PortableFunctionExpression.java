// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PortableFunctionExpression<T> implements Expression<T> {

    public enum Function {
        // TEXT
        ASCII,
        BIT_LENGTH,
        BTRIM,
        CHR,
        CONCAT,
        CONCAT_WS,
        CHAR_LENGTH,
        LEFT,
        LOWER,
        LPAD,
        LTRIM,
        OCTET_LENGTH,
        POSITION,
        REPEAT,
        RIGHT,
        RPAD,
        RTRIM,
        SUBSTRING,
        TRIM,
        TO_HEX,
        UPPER,
        REPLACE,
        REGEXP_REPLACE,
        REVERSE,
        SPACE,

        // HASH
        MD5,

        // JSON
        JSON_OBJECT,
        JSON_ARRAY,
        JSON_ARRAY_LENGTH,

        // NUMERIC
        MOD,
        POWER,
        ROUND,
        CEIL,
        FLOOR,
        TRUNCATE,
        ABS,
        NEG,
        SIGN,
        SQRT,
        CBRT,
        SQUARE,
        DEGREES,
        RADIANS,
        E,
        EXP,
        GREATEST,
        LEAST,
        LN,
        LOG,
        LOG10,
        PI,

        // TRYGONOMETRIC
        ACOS,
        ASIN,
        ATAN,
        ATAN2,
        COS,
        COT,
        SIN,
        TAN,

        // HYPERBOLIC
        ACOSH,
        ASINH,
        ATANH,
        COSH,
        COTH,
        SINH,
        TANH,

        // NULLs
        COALESCE,
        NULLIF,
        IFNULL,
        NVL,
        NUM_NULLS,
        NUM_NON_NULLS,

        // CONDITIONAL
        IIF,
        EXISTS,

        // DATE, TIME
        CURRENT_TIME,
        CURRENT_DATE,
        CURRENT_TIMESTAMP,
        CURRENT_LOCALDATETIME,
        DATE,
        DATE_ADD,
        DATE_ADD_INTERVAL,
        DATE_SUB,
        DATE_SUB_INTERVAL,
        DATE_DIFF,
        EXTRACT_DAY,
        EXTRACT_DAY_OF_YEAR,
        EXTRACT_ISO_DAY_OF_WEEK,
        EXTRACT_HOUR,
        EXTRACT_MINUTE,
        EXTRACT_MONTH,
        EXTRACT_QUARTER,
        EXTRACT_SECOND,
        EXTRACT_YEAR,
        EPOCH,
        DATE_TRUNC_YEAR,
        DATE_TRUNC_QUARTER,
        DATE_TRUNC_MONTH,
        DATE_TRUNC_WEEK,
        DATE_TRUNC_DAY,
        DATE_TRUNC_HOUR,
        DATE_TRUNC_MINUTE,
        DATE_TRUNC_SECOND,
        DATE_TRUNC_MILLISECOND,
        DATE_TRUNC_MICROSECOND,

        // RANDOM
        RANDOM,
        UUID,

        // BITWISE
        BIT_AND,
        BIT_GET,
        BIT_NAND,
        BIT_NOR,
        BIT_NOT,
        BIT_OR,
        BIT_SET,
        BIT_XNOR,
        BIT_XOR,
        SHL,
        SHR,

        // WINDOW FUNCTIONS
        ROW_NUMBER,
        RANK,
        DENSE_RANK,
        NTILE,
        PERCENT_RANK,
        CUME_DIST,
        LAG,
        LEAD,
        FIRST_VALUE,
        LAST_VALUE,
        NTH_VALUE,
        PERCENTILE_CONT,
        PERCENTILE_DISC,
    }

    @NonNull
    @Getter
    private final Function function;

    @NonNull
    @Getter
    private final List<Expression<?>> params;

    @NonNull
    @Getter
    private final Class<T> javaType;

    @Getter
    private final List<Order> withinGroupOrders = new ArrayList<>();

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }
}
