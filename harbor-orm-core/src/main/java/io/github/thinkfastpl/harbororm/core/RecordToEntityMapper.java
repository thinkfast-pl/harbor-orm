// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.metadata.*;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import io.github.thinkfastpl.harbororm.core.utils.HarborBeanUtils;
import lombok.NonNull;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

class RecordToEntityMapper<T, ID> implements Function<Record, T> {
    private final QEntity<T, ID> qEntity;
    private final AttributeExtractor extractor;

    private final Map<QElementCollection<?>, ElementCollectionBackend<T, ID>> elementCollectionBackends = new IdentityHashMap<>();
    private final Map<QEntityRelation<?, ?>, EntityRelationBackend<T, ID>> entityRelationBackends = new IdentityHashMap<>();
    private final Map<QEntityRelation<?, ?>, EntityOneToOneRelationBackend<T, ID>> entityOneToOneRelationBackends = new IdentityHashMap<>();
    private final Map<QManyToMany<?>, ManyToManyBackend<T, ID>> manyToManyBackends = new IdentityHashMap<>();

    RecordToEntityMapper(@NonNull QueryExecutor executor, @NonNull QEntity<T, ID> qEntity, @NonNull AttributeExtractor extractor, int lazyLoadBatchSize, int defaultStreamFetchSize) {
        this.qEntity = qEntity;
        this.extractor = extractor;

        for (QElementCollection<?> elementCollection : this.extractor.getElementCollections()) {
            elementCollectionBackends.put(elementCollection, new ElementCollectionBackend<>(executor, qEntity, elementCollection, lazyLoadBatchSize, defaultStreamFetchSize));
        }
        for (QEntityRelation<?, ?> entityRelation : this.extractor.getEntityRelations()) {
            switch (entityRelation.getType()) {
                case ONE_TO_MANY -> entityRelationBackends.put(entityRelation, new EntityRelationBackend<>(executor, qEntity, entityRelation, lazyLoadBatchSize, defaultStreamFetchSize));
                case ONE_TO_ONE -> entityOneToOneRelationBackends.put(entityRelation, new EntityOneToOneRelationBackend<>(executor, qEntity, entityRelation, lazyLoadBatchSize, defaultStreamFetchSize));
            }
        }
        for (QManyToMany<?> manyToMany : this.extractor.getManyToManyRelations()) {
            manyToManyBackends.put(manyToMany, new ManyToManyBackend<>(executor, qEntity, manyToMany, lazyLoadBatchSize, defaultStreamFetchSize));
        }
    }

    @Override
    public T apply(Record record) {
        final T entity = HarborBeanUtils.newInstance(qEntity.getBeanType());
        for (QColumn<?> qColumn : extractor.getColumns()) {
            HarborBeanUtils.setPropertyValue(entity, qColumn.getPropertyName(), record.get(qColumn));
        }
        for (QEmbeddable<?> qEmbeddable : extractor.getEmbeddables()) {
            HarborBeanUtils.setPropertyValue(entity, qEmbeddable.getPropertyName(), EmbeddableHandler.readEmbeddable(record, qEmbeddable));
        }
        for (QElementCollection<?> elementCollection : extractor.getElementCollections()) {
            HarborBeanUtils.setPropertyValue(
                    entity,
                    elementCollection.getPropertyName(),
                    elementCollectionBackends.get(elementCollection).trackEntity(entity)
            );
        }
        for (QEntityRelation<?, ?> entityRelation : extractor.getEntityRelations()) {
            switch (entityRelation.getType()) {
                case ONE_TO_MANY -> HarborBeanUtils.setPropertyValue(
                        entity,
                        entityRelation.getPropertyName(),
                        entityRelationBackends.get(entityRelation).trackEntity(entity)
                );
                case ONE_TO_ONE -> HarborBeanUtils.setPropertyValue(
                        entity,
                        entityRelation.getPropertyName(),
                        entityOneToOneRelationBackends.get(entityRelation).trackEntity(entity)
                );
            }
        }
        for (QManyToMany<?> manyToMany : extractor.getManyToManyRelations()) {
            HarborBeanUtils.setPropertyValue(
                    entity,
                    manyToMany.getPropertyName(),
                    manyToManyBackends.get(manyToMany).trackEntity(entity)
            );
        }
        return entity;
    }
}
