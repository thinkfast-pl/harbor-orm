// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import io.github.thinkfastpl.harbororm.core.utils.HarborBeanUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
class ExpressionsBasedRecord implements Record {
    private final Map<Integer, Object> valuesMap;
    private final Map<String, Integer> labelColumnMap;
    private final Map<Expression<?>, Integer> expressionsColumnsMap;

    @Override
    public <T> T get(Expression<T> expression) {
        Integer column = expressionsColumnsMap.get(expression);
        if (column != null) {
            return get(column, expression.getJavaType());
        }

        String columnLabel = Objects.requireNonNull(expression.getResultColumnLabel(), "expression does not provide the result column label");
        return get(columnLabel, expression.getJavaType());
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

        for (Map.Entry<Expression<?>, Integer> entry : expressionsColumnsMap.entrySet()) {
            types[entry.getValue() - 1] = entry.getKey().getJavaType();
            values[entry.getValue() - 1] = valuesMap.get(entry.getValue());
        }

        return HarborBeanUtils.tryConstruct(clazz, types, values);
    }

    ExpressionsBasedRecord split(int leaveHereColumnsCount) {
        final Map<Integer, Object> otherValuesMap = new HashMap<>();
        {
            final Iterator<Map.Entry<Integer, Object>> iterator = valuesMap.entrySet().iterator();
            while (iterator.hasNext()) {
                final Map.Entry<Integer, Object> entry = iterator.next();
                if (entry.getKey() > leaveHereColumnsCount) {
                    otherValuesMap.put(entry.getKey(), entry.getValue());
                    iterator.remove();
                }
            }
        }

        final Map<String, Integer> otherLabelColumnMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        {
            final Iterator<Map.Entry<String, Integer>> iterator = labelColumnMap.entrySet().iterator();
            while (iterator.hasNext()) {
                final Map.Entry<String, Integer> entry = iterator.next();
                if (entry.getValue() > leaveHereColumnsCount) {
                    otherLabelColumnMap.put(entry.getKey(), entry.getValue());
                    iterator.remove();
                }
            }
        }

        final Map<Expression<?>, Integer> otherExpressionsColumnsMap = new HashMap<>();
        {
            final Iterator<Map.Entry<Expression<?>, Integer>> iterator = expressionsColumnsMap.entrySet().iterator();
            while (iterator.hasNext()) {
                final Map.Entry<Expression<?>, Integer> entry = iterator.next();
                if (entry.getValue() > leaveHereColumnsCount) {
                    otherExpressionsColumnsMap.put(entry.getKey(), entry.getValue());
                    iterator.remove();
                }
            }
        }

        return new ExpressionsBasedRecord(otherValuesMap, otherLabelColumnMap, otherExpressionsColumnsMap);
    }
}
