// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.metadata.JoinColumnData;
import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.metadata.QEntity;
import io.github.thinkfastpl.harbororm.api.metadata.QEntityRelation;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import io.github.thinkfastpl.harbororm.core.utils.HarborListUtils;
import lombok.NonNull;

import java.util.*;

abstract class AbstractEntityRelationBackend<T, ID, R> {

    protected class TrackedEntityBatch {
        final List<T> trackedEntities = new ArrayList<>();
        Map<T, R> entityElements;

        R getElementForEntity(@NonNull T entity) {
            if (entityElements == null) {
                entityElements = fetchRelatedEntities();
            }
            if (!entityElements.containsKey(entity)) {
                throw new IllegalStateException("Entity not found in tracked batch");
            }
            return entityElements.get(entity);
        }

        private Map<T, R> fetchRelatedEntities() {
            final Map<T, Map<JoinColumnData, Object>> entityJoinColumnData =
                    JoinColumnUtils.getEntityJoinColumnData(qEntity, trackedEntities, entityRelation.getJoinColumns());

            final QEntity<?, ?> relatedQEntity = entityRelation.getRelatedQEntity();
            final AttributeExtractor relatedAttributeExtractor = new AttributeExtractor(relatedQEntity);

            final List<Record> records = new DefaultSelectQuery<>(executor, false, Record.class, r -> r, defaultStreamFetchSize)
                    .select(HarborListUtils.merge(
                            entityRelation.getJoinColumns().stream().<QColumn<?>>map(JoinColumnData::getQColumn).toList(),
                            relatedAttributeExtractor.getAllColumnsRecursive()
                    ))
                    .from(relatedQEntity.getTableName())
                    .where(JoinColumnUtils.getJoinConditions(entityRelation.getJoinColumns(), entityJoinColumnData))
                    .fetchAll();

            final RecordToEntityMapper<?, ?> relatedEntityRecordMapper =
                    new RecordToEntityMapper<>(executor, relatedQEntity, relatedAttributeExtractor, batchSize, defaultStreamFetchSize);

            final Map<T, R> result = new IdentityHashMap<>();
            for (Map.Entry<T, Map<JoinColumnData, Object>> entityDataEntry : entityJoinColumnData.entrySet()) {
                List<?> matchingRecords = records.stream()
                        .filter(record -> {
                            for (JoinColumnData joinColumn : entityRelation.getJoinColumns()) {
                                if (!Objects.equals(entityDataEntry.getValue().get(joinColumn), record.get(joinColumn.getQColumn()))) {
                                    return false;
                                }
                            }
                            return true;
                        })
                        .map(relatedEntityRecordMapper)
                        .toList();

                result.put(entityDataEntry.getKey(), mapMatchingRecords(matchingRecords, entityDataEntry.getKey()));
            }
            return result;
        }
    }

    @NonNull
    protected final QueryExecutor executor;

    @NonNull
    protected final QEntity<T, ID> qEntity;

    @NonNull
    protected final QEntityRelation<?, ?> entityRelation;

    protected final int batchSize;

    protected final int defaultStreamFetchSize;

    private TrackedEntityBatch currentBatch = new TrackedEntityBatch();

    protected AbstractEntityRelationBackend(@NonNull QueryExecutor executor, @NonNull QEntity<T, ID> qEntity, @NonNull QEntityRelation<?, ?> entityRelation, int batchSize, int defaultStreamFetchSize) {
        this.executor = executor;
        this.qEntity = qEntity;
        this.entityRelation = entityRelation;
        this.batchSize = batchSize;
        this.defaultStreamFetchSize = defaultStreamFetchSize;
    }

    protected abstract R mapMatchingRecords(List<?> matchingRecords, T entity);

    protected abstract Object createEntityWrapper(T entity, TrackedEntityBatch batch);

    Object trackEntity(@NonNull T entity) {
        if (currentBatch.trackedEntities.size() >= batchSize || currentBatch.entityElements != null) {
            currentBatch = new TrackedEntityBatch();
        }

        this.currentBatch.trackedEntities.add(entity);

        return createEntityWrapper(entity, this.currentBatch);
    }
}
