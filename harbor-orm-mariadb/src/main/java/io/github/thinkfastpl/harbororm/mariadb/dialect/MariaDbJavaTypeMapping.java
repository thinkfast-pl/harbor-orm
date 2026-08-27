// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.dialect;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

class MariaDbJavaTypeMapping {

    private static final Map<Class<?>, String> MAPPING;

    static {
        Map<Class<?>, String> mapping = new IdentityHashMap<>();
        mapping.put(Integer.class, "SIGNED");
        mapping.put(Long.class, "SIGNED");
        mapping.put(Short.class, "SIGNED");
        mapping.put(Byte.class, "SIGNED");
        mapping.put(Float.class, "FLOAT");
        mapping.put(Double.class, "DOUBLE");
        mapping.put(Boolean.class, "UNSIGNED");
        mapping.put(Character.class, "CHAR");
        mapping.put(String.class, "CHAR");
        mapping.put(BigDecimal.class, "DECIMAL");
        mapping.put(BigInteger.class, "DECIMAL");
        mapping.put(UUID.class, "CHAR");
        mapping.put(LocalDate.class, "DATE");
        mapping.put(LocalTime.class, "TIME");
        mapping.put(LocalDateTime.class, "DATETIME");
        mapping.put(OffsetDateTime.class, "DATETIME");
        mapping.put(byte[].class, "BINARY");
        MAPPING = Collections.unmodifiableMap(mapping);
    }

    public static String map(Class<?> javaType, Object value) {
        if (javaType == BigDecimal.class && value != null) {
            BigDecimal bigDecimal = (BigDecimal) value;
            return "DECIMAL(%d, %d)".formatted(Math.max(bigDecimal.precision(), bigDecimal.scale()), bigDecimal.scale());
        }

        final String mariaType = MAPPING.get(javaType);
        if (mariaType == null) {
            throw new IllegalArgumentException("Unsupported java type: " + javaType.getName());
        }
        return mariaType;
    }
}
