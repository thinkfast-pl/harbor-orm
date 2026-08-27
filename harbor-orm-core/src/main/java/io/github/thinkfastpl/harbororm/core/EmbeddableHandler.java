// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.metadata.QAttribute;
import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.metadata.QEmbeddable;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import io.github.thinkfastpl.harbororm.core.utils.HarborBeanUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class EmbeddableHandler {

    private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = Map.of(
            boolean.class, false,
            byte.class, (byte) 0,
            short.class, (short) 0,
            int.class, 0,
            long.class, 0L,
            float.class, 0.0f,
            double.class, 0.0d,
            char.class, '\0'
    );

    public static Object readEmbeddable(Record record, QEmbeddable<?> qEmbeddable) {
        final List<QAttribute> attributes = qEmbeddable.getAllAttributes();

        final Class<?>[] types = new Class<?>[attributes.size()];
        final Object[] values = new Object[attributes.size()];
        final String[] propertyNames = new String[attributes.size()];

        int index = 0;
        for (QAttribute attribute : attributes) {
            if (attribute instanceof QColumn<?> column) {
                types[index] = column.getJavaType();
                values[index] = record.get(column);
                propertyNames[index] = column.getPropertyName();
            } else if (attribute instanceof QEmbeddable<?> qEmbeddableInner) {
                types[index] = qEmbeddableInner.getJavaType();
                values[index] = readEmbeddable(record, qEmbeddableInner);
                propertyNames[index] = qEmbeddableInner.getPropertyName();
            } else {
                throw new IllegalStateException("Unsupported attribute type in embedded value: " + attribute.getClass().getName());
            }

            index++;
        }

        Optional<Constructor<?>> allArgsConstructor = HarborBeanUtils.findConstructor(qEmbeddable.getJavaType(), types);
        if (allArgsConstructor.isPresent()) {
            try {
                Constructor<?> constructor = allArgsConstructor.get();
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                for (int i = 0; i < values.length; i++) {
                    if (values[i] == null && parameterTypes[i].isPrimitive()) {
                        values[i] = PRIMITIVE_DEFAULTS.get(parameterTypes[i]);
                    }
                }
                return constructor.newInstance(values);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            final Object embeddable = HarborBeanUtils.newInstance(qEmbeddable.getJavaType());
            for (int i = 0; i < propertyNames.length; i++) {
                HarborBeanUtils.setPropertyValue(embeddable, propertyNames[i], values[i]);
            }
            return embeddable;
        }
    }
}
