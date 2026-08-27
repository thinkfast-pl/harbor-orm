// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import io.github.thinkfastpl.harbororm.core.utils.HarborBeanUtils;
import lombok.NonNull;

import java.util.List;
import java.util.function.Function;

/**
 * Maps a {@link Record} to a view (or stored-function result) instance by reflectively
 * setting each {@link QColumn}'s value onto the corresponding field of the target bean.
 *
 * <p>Used internally by {@link DefaultViewQuery} to convert raw query results into
 * typed Java instances of {@link io.github.thinkfastpl.harbororm.api.annotations.View @View} or
 * {@link io.github.thinkfastpl.harbororm.api.annotations.StoredFunction @StoredFunction} classes.
 *
 * @param <T> the target bean type
 */
class RecordToViewMapper<T> implements Function<Record, T> {

    @NonNull
    private final Class<T> beanType;

    @NonNull
    private final List<QColumn<?>> columns;

    RecordToViewMapper(@NonNull Class<T> beanType, @NonNull List<QColumn<?>> columns) {
        this.beanType = beanType;
        this.columns = columns;
    }

    @Override
    public T apply(Record record) {
        final T instance = HarborBeanUtils.newInstance(beanType);
        for (QColumn<?> column : columns) {
            HarborBeanUtils.setPropertyValue(instance, column.getPropertyName(), record.get(column));
        }
        return instance;
    }
}
