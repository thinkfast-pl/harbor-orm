// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import io.github.thinkfastpl.harbororm.core.utils.HarborBeanUtils;
import io.github.thinkfastpl.harbororm.core.utils.HarborStringUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
class GeneratedKeysRecord implements Record {

    static List<GeneratedKeysRecord> ofResultSet(
            @NonNull RdbmsSupport rdbmsSupport,
            @NonNull Connection connection,
            @NonNull ResultSet resultSet
    ) throws SQLException {
        final Map<String, Integer> labelColumnMap = rdbmsSupport.getColumnsLabels(connection, resultSet);

        final List<GeneratedKeysRecord> records = new ArrayList<>();
        final int columnCount = resultSet.getMetaData().getColumnCount();

        while (resultSet.next()) {
            final Map<Integer, Object> valuesMap = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                valuesMap.put(i, resultSet.getObject(i));
            }
            records.add(new GeneratedKeysRecord(labelColumnMap, valuesMap));
        }

        return records;
    }

    private final Map<String, Integer> labelColumnMap;
    private final Map<Integer, Object> valuesMap;

    @Override
    public <T> T get(Expression<T> expression) {
        if (HarborStringUtils.isNotBlank(expression.getAlias())) {
            return get(expression.getAlias(), expression.getJavaType());
        } else if (expression instanceof QColumn<?> qColumn) {
            return get(qColumn.getColumnName(), expression.getJavaType());
        }

        throw new IllegalArgumentException("Cannot get for expression " + expression);
    }

    @Override
    public Object get(int column) {
        if (!valuesMap.containsKey(column)) {
            throw new IllegalArgumentException("Invalid column index " + column);
        }
        return valuesMap.get(column);
    }

    @Override
    public Object get(String columnLabel) {
        Integer column = labelColumnMap.get(columnLabel);
        if (column == null) {
            throw new IllegalArgumentException("Invalid column label " + columnLabel);
        }
        return get(column);
    }

    @Override
    public <T> T get(int column, Class<T> clazz) {
        return ObjectValueConverter.convertToClass(get(column), clazz);
    }

    @Override
    public <T> T get(String columnLabel, Class<T> clazz) {
        return ObjectValueConverter.convertToClass(get(columnLabel), clazz);
    }

    @Override
    public int columnsCount() {
        return valuesMap.size();
    }

    @Override
    public <C> C getAsInstanceOf(@NonNull Class<C> clazz) {
        final Class<?>[] types = new Class<?>[valuesMap.size()];
        final Object[] values = new Object[valuesMap.size()];

        for (Map.Entry<String, Integer> entry : labelColumnMap.entrySet()) {
            values[entry.getValue() - 1] = valuesMap.get(entry.getValue());
            types[entry.getValue() - 1] = values[entry.getValue() - 1] != null
                    ? values[entry.getValue() - 1].getClass()
                    : Object.class;
        }

        return HarborBeanUtils.tryConstruct(clazz, types, values);
    }
}
