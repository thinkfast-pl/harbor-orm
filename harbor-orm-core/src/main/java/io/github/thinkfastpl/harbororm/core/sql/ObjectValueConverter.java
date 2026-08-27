// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.UUID;

class ObjectValueConverter {

    // ISO local date-time followed by an offset that may omit the minutes ("+02", "+02:00", "Z")
    private static final DateTimeFormatter OFFSET_DATE_TIME_PARSER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .appendOffset("+HH:mm:ss", "Z")
            .toFormatter();

    static <T> T convertToClass(Object object, Class<? extends T> clazz) {
        if (object == null) {
            return null;
        }

        final Object convertedValue;

        if (clazz.isAssignableFrom(object.getClass())) {
            convertedValue = object;
        } else if (clazz == Boolean.class) {
            convertedValue = Boolean.valueOf(object.toString());
        } else if (clazz == String.class) {
            convertedValue = object.toString();
        } else if (clazz == Integer.class) {
            convertedValue = convertToInteger(object);
        } else if (clazz == Long.class) {
            convertedValue = convertToLong(object);
        } else if (clazz == BigDecimal.class) {
            convertedValue = convertToBigDecimal(object);
        } else if (clazz == BigInteger.class) {
            convertedValue = convertToBigInteger(object);
        } else if (clazz == Byte.class) {
            convertedValue = convertToByte(object);
        } else if (clazz == Short.class) {
            convertedValue = convertToShort(object);
        } else if (clazz == Float.class) {
            convertedValue = convertToFloat(object);
        } else if (clazz == Double.class) {
            convertedValue = convertToDouble(object);
        } else if (clazz == Character.class) {
            convertedValue = convertToCharacter(object);
        } else if (clazz == OffsetDateTime.class) {
            convertedValue = convertToOffsetDateTime(object);
        } else if (clazz == LocalDateTime.class) {
            convertedValue = convertToLocalDateTime(object);
        } else if (clazz == LocalDate.class) {
            convertedValue = convertToLocalDate(object);
        } else if (clazz == LocalTime.class) {
            convertedValue = convertToLocalTime(object);
        } else if (clazz == UUID.class) {
            convertedValue = UUID.fromString(object.toString());
        } else {
            throw new IllegalArgumentException("Can't convert %s of type %s to %s".formatted(object, object.getClass(), clazz));
        }

        return clazz.cast(convertedValue);
    }

    private static Byte convertToByte(Object object) {
        if (object instanceof Number number) {
            return number.byteValue();
        } else if (object instanceof String s) {
            return Byte.valueOf(s);
        }

        throw new IllegalArgumentException("Can't convert %s to Byte".formatted(object));
    }

    private static Short convertToShort(Object object) {
        if (object instanceof Number number) {
            return number.shortValue();
        } else if (object instanceof String s) {
            return Short.valueOf(s);
        }

        throw new IllegalArgumentException("Can't convert %s to Short".formatted(object));
    }

    private static Integer convertToInteger(Object object) {
        if (object instanceof Number number) {
            return number.intValue();
        } else if (object instanceof String s) {
            return Integer.valueOf(s);
        }

        throw new IllegalArgumentException("Can't convert %s to Integer".formatted(object));
    }

    private static Long convertToLong(Object object) {
        if (object instanceof Number number) {
            return number.longValue();
        } else if (object instanceof String s) {
            return Long.valueOf(s);
        }

        throw new IllegalArgumentException("Can't convert %s to Long".formatted(object));
    }

    private static Float convertToFloat(Object object) {
        if (object instanceof Number number) {
            return number.floatValue();
        } else if (object instanceof String s) {
            return Float.valueOf(s);
        }

        throw new IllegalArgumentException("Can't convert %s to Float".formatted(object));
    }

    private static Double convertToDouble(Object object) {
        if (object instanceof Number number) {
            return number.doubleValue();
        } else if (object instanceof String s) {
            return Double.valueOf(s);
        }

        throw new IllegalArgumentException("Can't convert %s to Double".formatted(object));
    }

    private static BigInteger convertToBigInteger(Object object) {
        if (object instanceof Number number) {
            if (
                    number instanceof Integer
                            || number instanceof Long
                            || number instanceof Short
                            || number instanceof Byte
            ) {
                return BigInteger.valueOf(number.longValue());
            }
        }

        if (object instanceof BigDecimal bd) {
            return bd.toBigIntegerExact();
        }

        throw new IllegalArgumentException("Can't convert %s of type %s to BigInteger".formatted(object, object.getClass().getName()));
    }

    private static BigDecimal convertToBigDecimal(Object object) {
        if (object instanceof Number number) {
            if (
                    number instanceof Integer
                            || number instanceof Long
                            || number instanceof Short
                            || number instanceof Byte
            ) {
                return BigDecimal.valueOf(number.longValue());
            } else {
                return new BigDecimal(number.toString());
            }
        } else if (object instanceof String s) {
            return new BigDecimal(s);
        }

        throw new IllegalArgumentException("Can't convert %s to BigDecimal".formatted(object));
    }

    private static Character convertToCharacter(Object object) {
        if (object instanceof String s && s.length() == 1) {
            return s.charAt(0);
        }

        throw new IllegalArgumentException("Can't convert %s to Character".formatted(object));
    }

    private static OffsetDateTime convertToOffsetDateTime(Object object) {
        if (object instanceof java.sql.Timestamp t) {
            return OffsetDateTime.ofInstant(t.toInstant(), ZoneId.of("UTC"));
        }
        if (object instanceof String s) {
            // JSON aggregations render TIMESTAMP WITH TIME ZONE columns as text with an offset,
            // e.g. "2024-06-15T07:30:45.123456+02:00" (PostgreSQL) or "2024-06-15T10:30:45.123456+05" (H2).
            return OffsetDateTime.parse(s.replace(' ', 'T'), OFFSET_DATE_TIME_PARSER);
        }

        throw new IllegalArgumentException("Can't convert %s to OffsetDateTime. Object type: %s".formatted(object, object.getClass().getName()));
    }

    private static LocalDateTime convertToLocalDateTime(Object object) {
        if (object instanceof java.sql.Timestamp t) {
            return t.toLocalDateTime();
        }
        if (object instanceof String s) {
            // PostgreSQL JDBC returns a textual timestamp ("yyyy-MM-dd HH:mm:ss[.fff]") for
            // expressions whose result column lacks a typed OID (e.g. `(? + interval ...)`).
            return LocalDateTime.parse(s.replace(' ', 'T'));
        }
        if (object instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalDateTime();
        }

        throw new IllegalArgumentException("Can't convert %s (type: %s) to LocalDateTime".formatted(object, object.getClass().getName()));
    }

    private static LocalTime convertToLocalTime(Object object) {
        if (object instanceof java.sql.Time t) {
            return t.toLocalTime();
        }
        if (object instanceof String ts) {
            return LocalTime.parse(ts);
        }
        if (object instanceof OffsetTime offsetTime) {
            return offsetTime.toLocalTime();
        }
        throw new IllegalArgumentException("Can't convert %s to LocalTime".formatted(object));
    }

    private static LocalDate convertToLocalDate(Object object) {
        if (object instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        if (object instanceof String ts) {
            return LocalDate.parse(ts);
        }
        if (object instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        throw new IllegalArgumentException("Can't convert %s to LocalDate".formatted(object));
    }
}
