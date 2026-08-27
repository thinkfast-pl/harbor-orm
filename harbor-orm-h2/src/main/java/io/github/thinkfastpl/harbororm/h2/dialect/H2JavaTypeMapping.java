// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect;

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

class H2JavaTypeMapping {

    private static final Map<Class<?>, String> MAPPING;

    static {
        Map<Class<?>, String> mapping = new IdentityHashMap<>();
        mapping.put(Integer.class, "INT");
        mapping.put(Long.class, "BIGINT");
        mapping.put(Short.class, "SMALLINT");
        mapping.put(Byte.class, "TINYINT");
        mapping.put(Float.class, "REAL");
        mapping.put(Double.class, "DOUBLE PRECISION");
        mapping.put(Boolean.class, "BOOLEAN");
        mapping.put(Character.class, "CHAR");
        mapping.put(String.class, "VARCHAR");
        mapping.put(BigDecimal.class, "DEC");
        mapping.put(BigInteger.class, "NUMERIC");
        mapping.put(UUID.class, "UUID");
        mapping.put(LocalDate.class, "DATE");
        mapping.put(LocalTime.class, "TIME");
        mapping.put(LocalDateTime.class, "TIMESTAMP WITHOUT TIME ZONE");
        mapping.put(OffsetDateTime.class, "TIMESTAMP WITH TIME ZONE");
        mapping.put(byte[].class, "VARBINARY");
        MAPPING = Collections.unmodifiableMap(mapping);
    }

    public static String map(Class<?> javaType, Object value) {
        if (javaType == BigDecimal.class && value != null) {
            BigDecimal bigDecimal = (BigDecimal) value;
            return "DEC(%d, %d)".formatted(Math.max(bigDecimal.precision(), bigDecimal.scale()), bigDecimal.scale());
        }

        final String h2Type = MAPPING.get(javaType);
        if (h2Type == null) {
            throw new IllegalArgumentException("Unsupported java type: " + javaType.getName());
        }
        return h2Type;
    }
}
