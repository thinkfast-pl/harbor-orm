// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.metadata.*;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class ElementCollectionQuery<T, ID> {

    @NonNull
    private final QueryExecutor executor;

    @NonNull
    private final QEntity<T, ID> qEntity;

    @NonNull
    private final QElementCollection<?> qElementCollection;

    @NonNull
    private final List<T> entities;

    private final int defaultStreamFetchSize;

    public Map<T, List<?>> fetch() {
        if (entities.isEmpty()) {
            return Collections.emptyMap();
        }

        if (qElementCollection.getAttribute() instanceof QColumn<?> qColumn) {
            return fetchForQColumn(qColumn);
        } else if (qElementCollection.getAttribute() instanceof QEmbeddable<?> qEmbeddable) {
            return fetchForQEmbeddable(qEmbeddable);
        } else {
            throw new IllegalStateException("Only supports QColumn and QEmbeddable as ElementCollection attribute");
        }
    }

    private Map<T, List<?>> fetchForQEmbeddable(QEmbeddable<?> qEmbeddable) {
        final Map<T, Map<JoinColumnData, Object>> entitiesData = JoinColumnUtils.getEntityJoinColumnData(qEntity, entities, qElementCollection.getJoinColumns());

        final AttributeExtractor attributeExtractor = new AttributeExtractor(qEmbeddable);

        final List<QColumn<?>> embeddableColumns = attributeExtractor.getAllColumnsRecursive();

        final List<QColumn<?>> columnsToSelect = new ArrayList<>(qElementCollection.getJoinColumns().size() + embeddableColumns.size());
        for (JoinColumnData joinColumn : qElementCollection.getJoinColumns()) {
            columnsToSelect.add(joinColumn.getQColumn());
        }
        columnsToSelect.addAll(embeddableColumns);

        final List<Record> records = getRecords(columnsToSelect, entitiesData);

        final Map<T, List<?>> result = new IdentityHashMap<>();
        for (Map.Entry<T, Map<JoinColumnData, Object>> entityDataEntry : entitiesData.entrySet()) {
            List<?> list = records.stream()
                    .filter(record -> {
                        for (JoinColumnData joinColumn : qElementCollection.getJoinColumns()) {
                            if (!Objects.equals(entityDataEntry.getValue().get(joinColumn), record.get(joinColumn.getQColumn()))) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .map(record -> EmbeddableHandler.readEmbeddable(record, qEmbeddable))
                    .collect(Collectors.toCollection(ArrayList::new));

            result.put(entityDataEntry.getKey(), list);
        }
        return result;
    }

    private Map<T, List<?>> fetchForQColumn(QColumn<?> qColumn) {
        final Map<T, Map<JoinColumnData, Object>> entitiesData = JoinColumnUtils.getEntityJoinColumnData(qEntity, entities, qElementCollection.getJoinColumns());

        final List<QColumn<?>> columnsToSelect = new ArrayList<>(qElementCollection.getJoinColumns().size() + 1);
        for (JoinColumnData joinColumn : qElementCollection.getJoinColumns()) {
            columnsToSelect.add(joinColumn.getQColumn());
        }
        columnsToSelect.add(qColumn);

        final List<Record> records = getRecords(columnsToSelect, entitiesData);

        final Map<T, List<?>> result = new IdentityHashMap<>();
        for (Map.Entry<T, Map<JoinColumnData, Object>> entityDataEntry : entitiesData.entrySet()) {
            List<?> list = records.stream()
                    .filter(record -> {
                        for (JoinColumnData joinColumn : qElementCollection.getJoinColumns()) {
                            if (!Objects.equals(entityDataEntry.getValue().get(joinColumn), record.get(joinColumn.getQColumn()))) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .map(record -> record.get(qColumn))
                    .collect(Collectors.toCollection(ArrayList::new));

            result.put(entityDataEntry.getKey(), list);
        }
        return result;
    }

    private List<Record> getRecords(List<QColumn<?>> columnsToSelect, Map<T, Map<JoinColumnData, Object>> entitiesData) {
        return new DefaultSelectQuery<>(executor, false, Record.class, r -> r, defaultStreamFetchSize)
                .select(columnsToSelect)
                .from(qElementCollection.getTableName(), qElementCollection.getTableSchemaName())
                .where(JoinColumnUtils.getJoinConditions(qElementCollection.getJoinColumns(), entitiesData))
                .fetchAll();
    }
}
