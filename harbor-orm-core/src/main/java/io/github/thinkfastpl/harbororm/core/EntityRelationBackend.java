// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.metadata.QEntity;
import io.github.thinkfastpl.harbororm.api.metadata.QEntityRelation;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import io.github.thinkfastpl.harbororm.core.utils.HarborBeanUtils;
import lombok.NonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;

class EntityRelationBackend<T, ID> extends AbstractEntityRelationBackend<T, ID, List<?>> {

    private class InstanceProxy implements InvocationHandler, LazyList {

        @NonNull
        private final T entity;

        @NonNull
        private final TrackedEntityBatch batch;

        private List<?> relatedEntities;

        private Set<Object> allRelatedEntities;

        private InstanceProxy(@NonNull T entity, @NonNull TrackedEntityBatch batch) {
            this.entity = entity;
            this.batch = batch;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == LazyList.class) {
                return method.invoke(this, args);
            }
            if (this.relatedEntities == null) {
                this.relatedEntities = batch.getElementForEntity(this.entity);

                if (this.relatedEntities.isEmpty()) {
                    allRelatedEntities = Collections.emptySet();
                } else {
                    allRelatedEntities = new HashSet<>(this.relatedEntities);
                }
            }
            return method.invoke(this.relatedEntities, args);
        }

        @Override
        public boolean isPotentiallyModified() {
            return this.relatedEntities != null;
        }

        @Override
        public Set<Object> getMissingEntities() {
            if (allRelatedEntities == null || allRelatedEntities.isEmpty()) {
                return Collections.emptySet();
            }

            String idPropertyName = entityRelation.getRelatedQEntity().getIdColumn().getPropertyName();
            Set<Object> currentIds = this.relatedEntities.stream()
                    .map(e -> HarborBeanUtils.getPropertyValue(e, idPropertyName))
                    .collect(Collectors.toSet());

            return allRelatedEntities.stream()
                    .filter(e -> !currentIds.contains(HarborBeanUtils.getPropertyValue(e, idPropertyName)))
                    .collect(Collectors.toSet());
        }
    }

    EntityRelationBackend(@NonNull QueryExecutor executor, @NonNull QEntity<T, ID> qEntity, @NonNull QEntityRelation<?, ?> entityRelation, int batchSize, int defaultStreamFetchSize) {
        super(executor, qEntity, entityRelation, batchSize, defaultStreamFetchSize);
    }

    @Override
    protected List<?> mapMatchingRecords(List<?> matchingRecords, T entity) {
        return matchingRecords.stream().collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    protected Object createEntityWrapper(T entity, TrackedEntityBatch batch) {
        return Proxy.newProxyInstance(
                InstanceProxy.class.getClassLoader(),
                new Class[]{List.class, LazyList.class},
                new InstanceProxy(entity, batch)
        );
    }
}
