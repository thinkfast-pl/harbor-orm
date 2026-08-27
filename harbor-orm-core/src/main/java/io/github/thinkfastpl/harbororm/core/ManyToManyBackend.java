// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.metadata.*;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import io.github.thinkfastpl.harbororm.core.utils.HarborBeanUtils;
import lombok.NonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;

class ManyToManyBackend<T, ID> {

    private class TrackedEntityBatch {
        final List<T> trackedEntities = new ArrayList<>();
        Map<T, Set<?>> entityElements;

        Set<?> getElementsForEntity(@NonNull T entity) {
            if (entityElements == null) {
                entityElements = fetchRelatedElements();
            }
            return entityElements.computeIfAbsent(
                    entity,
                    k -> { throw new IllegalStateException("Entity not found in tracked batch"); }
            );
        }

        private Map<T, Set<?>> fetchRelatedElements() {
            if (trackedEntities.isEmpty()) {
                return Collections.emptyMap();
            }

            final Map<T, Map<JoinColumnData, Object>> entityJoinColumnData =
                    JoinColumnUtils.getEntityJoinColumnData(qEntity, trackedEntities, qManyToMany.getJoinColumns());

            // Query join table: SELECT join_cols, inverse_join_cols FROM join_table WHERE join_cols IN (...)
            List<QColumn<?>> selectColumns = new ArrayList<>();
            qManyToMany.getJoinColumns().forEach(jc -> selectColumns.add(jc.getQColumn()));
            qManyToMany.getInverseJoinColumns().forEach(jc -> selectColumns.add(jc.getQColumn()));

            final List<Record> joinRecords = new DefaultSelectQuery<>(executor, false, Record.class, r -> r, defaultStreamFetchSize)
                    .select(selectColumns)
                    .from(qManyToMany.getTableName(), qManyToMany.getTableSchemaName())
                    .where(JoinColumnUtils.getJoinConditions(qManyToMany.getJoinColumns(), entityJoinColumnData))
                    .fetchAll();

            if (qManyToMany.isIdOnly()) {
                return mapToIdSets(joinRecords, entityJoinColumnData);
            } else {
                return mapToEntitySets(joinRecords, entityJoinColumnData);
            }
        }

        private Map<T, Set<?>> mapToIdSets(List<Record> joinRecords, Map<T, Map<JoinColumnData, Object>> entityJoinColumnData) {
            // For ID-only: extract inverse join column values directly
            final Map<T, Set<?>> result = new IdentityHashMap<>();
            for (Map.Entry<T, Map<JoinColumnData, Object>> entry : entityJoinColumnData.entrySet()) {
                Set<Object> ids = joinRecords.stream()
                        .filter(record -> matchesJoinColumns(record, entry.getValue()))
                        .map(ManyToManyBackend.this::readInverseId)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                result.put(entry.getKey(), ids);
            }
            return result;
        }

        private Map<T, Set<?>> mapToEntitySets(List<Record> joinRecords, Map<T, Map<JoinColumnData, Object>> entityJoinColumnData) {
            // Collect all inverse IDs across all parents
            Set<Object> allInverseIds = new LinkedHashSet<>();
            for (Record record : joinRecords) {
                allInverseIds.add(readInverseId(record));
            }

            if (allInverseIds.isEmpty()) {
                Map<T, Set<?>> result = new IdentityHashMap<>();
                entityJoinColumnData.keySet().forEach(entity -> result.put(entity, new LinkedHashSet<>()));
                return result;
            }

            // Batch-load full entities: SELECT * FROM related_table WHERE id IN (...)
            List<?> relatedEntities = loadRelatedEntities(allInverseIds);

            // Index entities by ID
            Map<Object, Object> entitiesById = new LinkedHashMap<>();
            String idPropertyName = qManyToMany.getRelatedQEntity().getIdColumn().getPropertyName();
            for (Object entity : relatedEntities) {
                Object id = HarborBeanUtils.getPropertyValue(entity, idPropertyName);
                entitiesById.put(id, entity);
            }

            // Map back to parent entities
            Map<T, Set<?>> result = new IdentityHashMap<>();
            for (Map.Entry<T, Map<JoinColumnData, Object>> entry : entityJoinColumnData.entrySet()) {
                Set<Object> entitySet = joinRecords.stream()
                        .filter(record -> matchesJoinColumns(record, entry.getValue()))
                        .map(record -> entitiesById.get(readInverseId(record)))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                result.put(entry.getKey(), entitySet);
            }
            return result;
        }

        private boolean matchesJoinColumns(Record record, Map<JoinColumnData, Object> expectedValues) {
            for (JoinColumnData joinColumn : qManyToMany.getJoinColumns()) {
                if (!Objects.equals(expectedValues.get(joinColumn), record.get(joinColumn.getQColumn()))) {
                    return false;
                }
            }
            return true;
        }
    }

    private class SetProxy implements InvocationHandler, LazySet {

        @NonNull
        private final T entity;

        @NonNull
        private final TrackedEntityBatch batch;

        private Set<Object> elements;
        private Set<Object> originalSnapshot;

        private SetProxy(@NonNull T entity, @NonNull TrackedEntityBatch batch) {
            this.entity = entity;
            this.batch = batch;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == LazySet.class) {
                return method.invoke(this, args);
            }
            if (this.elements == null) {
                @SuppressWarnings("unchecked")
                Set<Object> loaded = (Set<Object>) batch.getElementsForEntity(this.entity);
                this.elements = loaded;
                this.originalSnapshot = new LinkedHashSet<>(loaded);
            }
            return method.invoke(this.elements, args);
        }

        @Override
        public boolean isPotentiallyModified() {
            return this.elements != null;
        }

        @Override
        public Set<Object> getAddedElements() {
            if (elements == null || originalSnapshot == null) {
                return Collections.emptySet();
            }
            Set<Object> added = new LinkedHashSet<>(elements);
            if (qManyToMany.isIdOnly()) {
                added.removeAll(originalSnapshot);
            } else {
                String idProp = qManyToMany.getRelatedQEntity().getIdColumn().getPropertyName();
                Set<Object> originalIds = originalSnapshot.stream()
                        .map(e -> HarborBeanUtils.getPropertyValue(e, idProp))
                        .collect(Collectors.toSet());
                added.removeIf(e -> originalIds.contains(HarborBeanUtils.getPropertyValue(e, idProp)));
            }
            return added;
        }

        @Override
        public Set<Object> getRemovedElements() {
            if (elements == null || originalSnapshot == null) {
                return Collections.emptySet();
            }
            Set<Object> removed = new LinkedHashSet<>(originalSnapshot);
            if (qManyToMany.isIdOnly()) {
                removed.removeAll(elements);
            } else {
                String idProp = qManyToMany.getRelatedQEntity().getIdColumn().getPropertyName();
                Set<Object> currentIds = elements.stream()
                        .map(e -> HarborBeanUtils.getPropertyValue(e, idProp))
                        .collect(Collectors.toSet());
                removed.removeIf(e -> currentIds.contains(HarborBeanUtils.getPropertyValue(e, idProp)));
            }
            return removed;
        }
    }

    @NonNull
    private final QueryExecutor executor;

    @NonNull
    private final QEntity<T, ID> qEntity;

    @NonNull
    private final QManyToMany<?> qManyToMany;

    private final int batchSize;

    private final int defaultStreamFetchSize;

    private TrackedEntityBatch currentBatch = new TrackedEntityBatch();

    ManyToManyBackend(@NonNull QueryExecutor executor, @NonNull QEntity<T, ID> qEntity, @NonNull QManyToMany<?> qManyToMany, int batchSize, int defaultStreamFetchSize) {
        this.executor = executor;
        this.qEntity = qEntity;
        this.qManyToMany = qManyToMany;
        this.batchSize = batchSize;
        this.defaultStreamFetchSize = defaultStreamFetchSize;
    }

    @SuppressWarnings("unchecked")
    Set<?> trackEntity(@NonNull T entity) {
        if (currentBatch.trackedEntities.size() >= batchSize || currentBatch.entityElements != null) {
            currentBatch = new TrackedEntityBatch();
        }

        this.currentBatch.trackedEntities.add(entity);

        return (Set<?>) Proxy.newProxyInstance(
                SetProxy.class.getClassLoader(),
                new Class[]{Set.class, LazySet.class},
                new SetProxy(entity, this.currentBatch)
        );
    }

    /**
     * Reads the related-side ID from a join table record: a single scalar column value, or a
     * reassembled composite ID instance when the related ID is an embeddable.
     */
    private Object readInverseId(Record record) {
        final List<JoinColumnData> inverseJoinColumns = qManyToMany.getInverseJoinColumns();
        final QAttributeHolder idHolder = ManyToManyHandler.inverseIdHolder(qManyToMany);

        if (idHolder == null) {
            return record.get(inverseJoinColumns.get(0).getQColumn());
        }

        final Class<?> idClass = ((QEmbeddable<?>) idHolder).getJavaType();
        final Object id = HarborBeanUtils.newInstance(idClass);
        JoinColumnUtils.resolveInverseIdPaths(idHolder, inverseJoinColumns).forEach((joinColumn, path) -> {
            if (path.size() != 1) {
                throw new UnsupportedOperationException(
                        "Nested embeddables inside a composite ID are not supported for @ManyToMany");
            }
            HarborBeanUtils.setPropertyValue(id, path.get(0), record.get(joinColumn.getQColumn()));
        });
        return id;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<?> loadRelatedEntities(Set<Object> inverseIds) {
        QEntity relatedQEntity = qManyToMany.getRelatedQEntity();
        AttributeExtractor relatedExtractor = new AttributeExtractor(relatedQEntity);
        RecordToEntityMapper relatedMapper = new RecordToEntityMapper<>(executor, relatedQEntity, relatedExtractor, batchSize, defaultStreamFetchSize);

        QComparableAttribute<Object> relatedIdColumn = (QComparableAttribute<Object>) relatedQEntity.getIdColumn();

        List<Object> idList = new ArrayList<>(inverseIds);
        return new DefaultSelectQuery<>(executor, false, relatedQEntity.getBeanType(), relatedMapper, defaultStreamFetchSize)
                .select(relatedExtractor.getAllColumnsRecursive())
                .from(relatedQEntity.getTableName())
                .where(idList.size() == 1 ? relatedIdColumn.eq(idList.get(0)) : relatedIdColumn.in(idList))
                .fetchAll();
    }
}
