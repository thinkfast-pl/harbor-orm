// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import com.github.openjson.JSONArray;
import lombok.NonNull;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

class ResultSetUtils {

    static Map<String, Integer> getLabelColumnIndexMap(@NonNull ResultSet resultSet) throws SQLException {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final int columnCount = metaData.getColumnCount();

        final Map<String, Integer> labelColumnMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 1; i <= columnCount; i++) {
            labelColumnMap.put(metaData.getColumnLabel(i), i);
        }
        return labelColumnMap;
    }

    static <T> T getObject(ResultSet resultSet, int columnIndex, Class<? extends T> clazz) throws SQLException {
        final Object convertedValue;

        if (clazz == Boolean.class) {
            convertedValue = resultSet.getBoolean(columnIndex);
            if (resultSet.wasNull()) {
                return null;
            }
        } else if (clazz == String.class) {
            convertedValue = resultSet.getString(columnIndex);
        } else if (useObjectValueConverter(clazz)) {
            convertedValue = ObjectValueConverter.convertToClass(resultSet.getObject(columnIndex), clazz);
        } else if (clazz == UUID.class) {
            convertedValue = resultSet.getObject(columnIndex, UUID.class);
        } else if (clazz == byte[].class) {
            convertedValue = resultSet.getBytes(columnIndex);
        } else {
            convertedValue = convertFromObject(resultSet.getObject(columnIndex), clazz);
        }

        return clazz.cast(convertedValue);
    }

    private static boolean useObjectValueConverter(Class<?> clazz) {
        return clazz == Integer.class
                || clazz == Long.class
                || clazz == BigDecimal.class
                || clazz == BigInteger.class
                || clazz == Byte.class
                || clazz == Short.class
                || clazz == Float.class
                || clazz == Double.class
                || clazz == Character.class
                || clazz == OffsetDateTime.class
                || clazz == LocalDateTime.class
                || clazz == LocalDate.class
                || clazz == LocalTime.class;
    }

    static <T> T getObject(JSONArray jsonArray, int columnIndex2, Class<? extends T> clazz) throws SQLException {
        final int jsonColumnIndex = columnIndex2 - 1;
        if (jsonArray.isNull(jsonColumnIndex)) {
            return null;
        }

        final Object convertedValue;

        if (clazz == Boolean.class) {
            convertedValue = jsonArray.getBoolean(jsonColumnIndex);
        } else if (clazz == String.class) {
            convertedValue = jsonArray.getString(jsonColumnIndex);
        } else if (clazz == Integer.class) {
            convertedValue = jsonArray.getInt(jsonColumnIndex);
        } else if (clazz == Long.class) {
            convertedValue = jsonArray.getLong(jsonColumnIndex);
        } else if (clazz == BigDecimal.class) {
            convertedValue = new BigDecimal(jsonArray.getString(jsonColumnIndex));
        } else if (clazz == BigInteger.class) {
            convertedValue = BigInteger.valueOf(jsonArray.getLong(jsonColumnIndex));
        } else if (clazz == Byte.class || clazz == Short.class || clazz == Float.class || clazz == Double.class) {
            convertedValue = ObjectValueConverter.convertToClass(jsonArray.getString(jsonColumnIndex), clazz);
        } else if (clazz == Character.class || clazz == OffsetDateTime.class || clazz == LocalDateTime.class || clazz == LocalDate.class) {
            convertedValue = ObjectValueConverter.convertToClass(jsonArray.getString(jsonColumnIndex), clazz);
        } else if (clazz == UUID.class) {
            convertedValue = UUID.fromString(jsonArray.getString(jsonColumnIndex));
        } else if (clazz == byte[].class) {
            throw new IllegalArgumentException("Cannot convert to byte");
        } else {
            convertedValue = convertFromObject(jsonArray.get(jsonColumnIndex), clazz);
        }

        return clazz.cast(convertedValue);
    }

    private static Object convertFromObject(Object object, Class<?> clazz) throws SQLException {
        if (object == null) {
            return null;
        }

        if (clazz.isAssignableFrom(object.getClass())) {
            return object;
        } else if (clazz.isArray()) {
            return convertToArray(object, clazz);
        } else {
            throw new IllegalArgumentException("Can't convert %s of type %s to %s".formatted(object, object.getClass(), clazz));
        }
    }

    private static Object convertToArray(Object object, Class<?> targetArrayClass) throws SQLException {
        if (object instanceof java.sql.Array sqlArray) {
            Object[] source = (Object[]) sqlArray.getArray();
            if (targetArrayClass.isAssignableFrom(source.getClass())) {
                return source;
            }

            Class<?> componentType = targetArrayClass.getComponentType();
            Object[] result = (Object[]) Array.newInstance(componentType, source.length);

            for (int i = 0; i < source.length; i++) {
                result[i] = componentType.cast(source[i]);
            }

            return result;
        }

        throw new IllegalArgumentException("Can't convert %s to Array".formatted(object));
    }
}
