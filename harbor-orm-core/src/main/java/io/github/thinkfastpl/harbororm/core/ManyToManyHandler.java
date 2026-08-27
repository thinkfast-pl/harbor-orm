// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.metadata.*;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import io.github.thinkfastpl.harbororm.core.utils.HarborBeanUtils;
import lombok.NonNull;

import java.util.*;

class ManyToManyHandler {

    @SuppressWarnings("unchecked")
    static <T, ID> void insert(@NonNull QueryExecutor executor, @NonNull QEntity<T, ID> qEntity, @NonNull T entity, @NonNull QManyToMany<?> qManyToMany) {
        Set<Object> elements = (Set<Object>) HarborBeanUtils.getPropertyValue(entity, qManyToMany.getPropertyName());
        if (elements == null || elements.isEmpty()) {
            return;
        }

        Map<JoinColumnData, Object> ownerJoinData = JoinColumnUtils.getEntityJoinColumnData(qEntity, entity, qManyToMany.getJoinColumns());

        DefaultInsertQuery insertQuery = new DefaultInsertQuery(executor);
        insertQuery.into(qManyToMany.getTableName(), qManyToMany.getTableSchemaName());

        for (Object element : elements) {
            insertQuery.nextRow();
            setJoinColumns(insertQuery, ownerJoinData, qManyToMany);
            setInverseJoinColumns(insertQuery, element, qManyToMany);
        }

        insertQuery.execute();
    }

    @SuppressWarnings("unchecked")
    static <T, ID> void update(@NonNull QueryExecutor executor, @NonNull QEntity<T, ID> qEntity, @NonNull T entity, @NonNull QManyToMany<?> qManyToMany) {
        Set<Object> elements = (Set<Object>) HarborBeanUtils.getPropertyValue(entity, qManyToMany.getPropertyName());
        if (elements == null) {
            return;
        }

        if (!(elements instanceof LazySet lazySet)) {
            // Not a proxy — was replaced entirely, delete all and reinsert
            delete(executor, qEntity, entity, qManyToMany);
            insert(executor, qEntity, entity, qManyToMany);
            return;
        }

        if (!lazySet.isPotentiallyModified()) {
            return;
        }

        Map<JoinColumnData, Object> ownerJoinData = JoinColumnUtils.getEntityJoinColumnData(qEntity, entity, qManyToMany.getJoinColumns());

        // Delete removed elements
        Set<Object> removed = lazySet.getRemovedElements();
        if (!removed.isEmpty()) {
            for (Object element : removed) {
                deleteJoinRow(executor, ownerJoinData, element, qManyToMany);
            }
        }

        // Insert added elements
        Set<Object> added = lazySet.getAddedElements();
        if (!added.isEmpty()) {
            DefaultInsertQuery insertQuery = new DefaultInsertQuery(executor);
            insertQuery.into(qManyToMany.getTableName(), qManyToMany.getTableSchemaName());

            for (Object element : added) {
                insertQuery.nextRow();
                setJoinColumns(insertQuery, ownerJoinData, qManyToMany);
                setInverseJoinColumns(insertQuery, element, qManyToMany);
            }

            insertQuery.execute();
        }
    }

    static <T, ID> void delete(@NonNull QueryExecutor executor, @NonNull QEntity<T, ID> qEntity, @NonNull T entity, @NonNull QManyToMany<?> qManyToMany) {
        delete(executor, qEntity, List.of(entity), qManyToMany);
    }

    static <T, ID> void delete(@NonNull QueryExecutor executor, @NonNull QEntity<T, ID> qEntity, @NonNull Collection<T> entities, @NonNull QManyToMany<?> qManyToMany) {
        if (entities.isEmpty()) {
            return;
        }
        new DefaultDeleteQuery(executor)
                .from(qManyToMany.getTableName(), qManyToMany.getTableSchemaName())
                .where(JoinColumnUtils.getJoinConditions(qManyToMany.getJoinColumns(), JoinColumnUtils.getEntityJoinColumnData(qEntity, entities, qManyToMany.getJoinColumns())))
                .execute();
    }

    static <T, ID> void delete(@NonNull QueryExecutor executor, @NonNull QEntity<T, ID> qEntity, @NonNull Collection<T> entities) {
        for (QAttribute qAttribute : qEntity.getAllAttributes()) {
            if (qAttribute instanceof QManyToMany<?> qManyToMany) {
                delete(executor, qEntity, entities, qManyToMany);
            }
        }
    }

    private static void setJoinColumns(DefaultInsertQuery insertQuery, Map<JoinColumnData, Object> ownerJoinData, QManyToMany<?> qManyToMany) {
        for (JoinColumnData joinColumn : qManyToMany.getJoinColumns()) {
            @SuppressWarnings("unchecked")
            QColumn<Object> qColumn = (QColumn<Object>) joinColumn.getQColumn();
            insertQuery.set(qColumn, ownerJoinData.get(joinColumn));
        }
    }

    private static void setInverseJoinColumns(DefaultInsertQuery insertQuery, Object element, QManyToMany<?> qManyToMany) {
        inverseJoinColumnValues(element, qManyToMany).forEach((joinColumn, value) -> {
            @SuppressWarnings("unchecked")
            QColumn<Object> qColumn = (QColumn<Object>) joinColumn.getQColumn();
            insertQuery.set(qColumn, value);
        });
    }

    private static void deleteJoinRow(QueryExecutor executor, Map<JoinColumnData, Object> ownerJoinData, Object element, QManyToMany<?> qManyToMany) {
        var deleteQuery = new DefaultDeleteQuery(executor)
                .from(qManyToMany.getTableName(), qManyToMany.getTableSchemaName());

        // Build condition: owner_id = ? AND related_id = ? (all columns of both sides)
        Condition condition = null;
        for (Map.Entry<JoinColumnData, Object> entry : inverseJoinColumnValues(element, qManyToMany).entrySet()) {
            @SuppressWarnings("unchecked")
            QColumn<Object> inverseColumn = (QColumn<Object>) entry.getKey().getQColumn();
            Condition eq = inverseColumn.eq(entry.getValue());
            condition = condition == null ? eq : condition.and(eq);
        }
        for (JoinColumnData joinColumn : qManyToMany.getJoinColumns()) {
            @SuppressWarnings("unchecked")
            QColumn<Object> jcColumn = (QColumn<Object>) joinColumn.getQColumn();
            condition = condition.and(jcColumn.eq(ownerJoinData.get(joinColumn)));
        }

        deleteQuery.where(condition).execute();
    }

    /**
     * The attribute holder describing the related-side ID when it is composite: the element
     * embeddable for ID-only variants, or the related entity's embeddable ID column.
     * Null when the related ID is a scalar.
     */
    static QAttributeHolder inverseIdHolder(QManyToMany<?> qManyToMany) {
        if (qManyToMany.isIdOnly()) {
            return qManyToMany.getElementEmbeddable();
        }
        return qManyToMany.getRelatedQEntity().getIdColumn() instanceof QEmbeddable<?> qEmbeddable ? qEmbeddable : null;
    }

    static Map<JoinColumnData, Object> inverseJoinColumnValues(Object element, QManyToMany<?> qManyToMany) {
        final Object idValue = qManyToMany.isIdOnly()
                ? element
                : HarborBeanUtils.getPropertyValue(element, qManyToMany.getRelatedQEntity().getIdColumn().getPropertyName());

        final List<JoinColumnData> inverseJoinColumns = qManyToMany.getInverseJoinColumns();
        final QAttributeHolder idHolder = inverseIdHolder(qManyToMany);

        if (idHolder == null) {
            if (inverseJoinColumns.size() != 1) {
                throw new IllegalStateException(
                        "Multiple inverse join columns require a composite (embeddable) related ID");
            }
            return Collections.singletonMap(inverseJoinColumns.get(0), idValue);
        }

        final Map<JoinColumnData, Object> values = new IdentityHashMap<>();
        JoinColumnUtils.resolveInverseIdPaths(idHolder, inverseJoinColumns).forEach((joinColumn, path) ->
                values.put(joinColumn, idValue == null ? null : HarborBeanUtils.getPropertyValue(idValue, path)));
        return values;
    }
}
