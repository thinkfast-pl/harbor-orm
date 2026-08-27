// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;

/**
 * Maps Java class types to their corresponding JDBC {@link java.sql.Types} constants.
 * <p>
 * Used when setting prepared statement parameters to supply the correct SQL type hint.
 */
public class HarborSQLTypesUtils {

    /**
     * Returns the {@link java.sql.Types} constant corresponding to the given Java class.
     * Returns {@link java.sql.Types#OTHER} for unrecognized types and
     * {@link java.sql.Types#NULL} if the class is {@code null}.
     *
     * @param clazz the Java class to map to a SQL type
     * @return the matching {@link java.sql.Types} constant
     */
    public static int getSqlTypeByClass(Class<?> clazz) {
        if (clazz == null) return Types.NULL;
        if (clazz == Boolean.class) return Types.BOOLEAN;
        if (clazz == Integer.class) return Types.INTEGER;
        if (clazz == Byte.class) return Types.TINYINT;
        if (clazz == Short.class) return Types.SMALLINT;
        if (clazz == Long.class) return Types.BIGINT;
        if (clazz == Float.class) return Types.REAL;
        if (clazz == Double.class) return Types.DOUBLE;
        if (clazz == BigInteger.class) return Types.NUMERIC;
        if (clazz == BigDecimal.class) return Types.NUMERIC;
        if (clazz == Character.class) return Types.CHAR;
        if (clazz == String.class) return Types.VARCHAR;
        if (clazz == java.sql.Date.class) return Types.DATE;
        if (clazz == java.util.Date.class) return Types.DATE;
        if (clazz == java.time.LocalDate.class) return Types.DATE;
        if (clazz == java.sql.Time.class) return Types.TIME;
        if (clazz == java.time.LocalTime.class) return Types.TIME;
        if (clazz == java.sql.Timestamp.class) return Types.TIMESTAMP;
        if (clazz == java.time.LocalDateTime.class) return Types.TIMESTAMP;
        if (clazz == java.time.OffsetDateTime.class) return Types.TIMESTAMP_WITH_TIMEZONE;
        if (byte[].class.isAssignableFrom(clazz)) return Types.BINARY;
        if (clazz.isArray()) return Types.ARRAY;
        return Types.OTHER;
    }
}
