// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.metadata.*;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import io.github.thinkfastpl.harbororm.core.utils.HarborBeanUtils;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

class ElementCollectionHandler {

    static <T, ID> void insert(@NonNull QueryExecutor executor, @NonNull QEntity<T, ID> qEntity, @NonNull T entity, @NonNull QElementCollection<?> qElementCollection, int defaultStreamFetchSize) {
        @SuppressWarnings("unchecked") final List<Object> elements = (List<Object>) HarborBeanUtils.getPropertyValue(entity, qElementCollection.getPropertyName());
        if (elements == null || elements.isEmpty()) {
            return;
        }

        final Map<JoinColumnData, Object> entityJoinColumnData = JoinColumnUtils.getEntityJoinColumnData(qEntity, entity, qElementCollection.getJoinColumns());

        final DefaultInsertQuery insertQuery = new DefaultInsertQuery(executor);
        insertQuery.into(qElementCollection.getTableName(), qElementCollection.getTableSchemaName());

        for (Object element : elements) {
            insertQuery.nextRow();

            for (JoinColumnData joinColumn : qElementCollection.getJoinColumns()) {
                @SuppressWarnings("unchecked") final QColumn<Object> qColumnCast = (QColumn<Object>) joinColumn.getQColumn();
                insertQuery.set(qColumnCast, entityJoinColumnData.get(joinColumn));
            }

            if (qElementCollection.getAttribute() instanceof QColumn<?> qColumn) {
                @SuppressWarnings("unchecked") final QColumn<Object> qColumnCast = (QColumn<Object>) qColumn;
                insertQuery.set(qColumnCast, element);
            } else if (qElementCollection.getAttribute() instanceof QEmbeddable<?> qEmbeddable) {
                AttributeHolderHandler.setColumns(executor, new AttributeExtractor(qEmbeddable), element, true, defaultStreamFetchSize, insertQuery::set);
            } else {
                throw new IllegalStateException("Only supports QColumn and QEmbeddable as ElementCollection attribute");
            }
        }

        insertQuery.execute();
    }

    static <T, ID> void delete(@NonNull QueryExecutor executor, @NonNull QEntity<T, ID> qEntity, @NonNull T entity) {
        for (QAttribute qAttribute : qEntity.getAllAttributes()) {
            if (qAttribute instanceof QElementCollection<?> qElementCollection) {
                delete(executor, qEntity, entity, qElementCollection);
            }
        }
    }

    static <T, ID> void delete(@NonNull QueryExecutor executor, @NonNull QEntity<T, ID> qEntity, @NonNull Collection<T> entities) {
        for (QAttribute qAttribute : qEntity.getAllAttributes()) {
            if (qAttribute instanceof QElementCollection<?> qElementCollection) {
                delete(executor, qEntity, entities, qElementCollection);
            }
        }
    }

    static <T, ID> void delete(@NonNull QueryExecutor executor, @NonNull QEntity<T, ID> qEntity, @NonNull T entity, @NonNull QElementCollection<?> qElementCollection) {
        delete(executor, qEntity, List.of(entity), qElementCollection);
    }

    static <T, ID> void delete(@NonNull QueryExecutor executor, @NonNull QEntity<T, ID> qEntity, @NonNull Collection<T> entities, @NonNull QElementCollection<?> qElementCollection) {
        new DefaultDeleteQuery(executor)
                .from(qElementCollection.getTableName(), qElementCollection.getTableSchemaName())
                .where(JoinColumnUtils.getJoinConditions(qElementCollection.getJoinColumns(), JoinColumnUtils.getEntityJoinColumnData(qEntity, entities, qElementCollection.getJoinColumns())))
                .execute();
    }

    static <T, ID> boolean isPotentiallyModified(@NonNull QEntity<T, ID> qEntity, @NonNull T entity, @NonNull QElementCollection<?> qElementCollection) {
        @SuppressWarnings("unchecked") final List<Object> elements = (List<Object>) HarborBeanUtils.getPropertyValue(entity, qElementCollection.getPropertyName());
        if (elements == null) {
            return true;
        }

        if (elements instanceof LazyList lazyList) {
            return lazyList.isPotentiallyModified();
        }

        return true;
    }
}
