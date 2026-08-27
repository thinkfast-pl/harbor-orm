// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.metadata.QElementCollection;
import io.github.thinkfastpl.harbororm.api.metadata.QEntity;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
class ElementCollectionBackend<T, ID> {

    private class TrackedEntityBatch {
        final List<T> trackedEntities = new ArrayList<>();
        Map<T, List<?>> entityElements;

        List<?> getElementsForEntity(@NonNull T entity) {
            if (entityElements == null) {
                entityElements = new ElementCollectionQuery<>(executor, qEntity, qElementCollection, trackedEntities, defaultStreamFetchSize).fetch();
            }
            return entityElements.computeIfAbsent(
                    entity,
                    k -> {
                        throw new IllegalStateException("Element collection not found for entity");
                    }
            );
        }
    }

    @RequiredArgsConstructor
    private class InstanceProxy implements InvocationHandler, LazyList {

        @NonNull
        private final T entity;

        @NonNull
        private final TrackedEntityBatch batch;

        private List<?> elements;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == LazyList.class) {
                return method.invoke(this, args);
            }

            if (this.elements == null) {
                this.elements = batch.getElementsForEntity(this.entity);
            }
            return method.invoke(this.elements, args);
        }

        @Override
        public boolean isPotentiallyModified() {
            return this.elements != null;
        }

        @Override
        public Set<Object> getMissingEntities() {
            throw new UnsupportedOperationException("Element collections don't have ids");
        }
    }


    @NonNull
    private final QueryExecutor executor;

    @NonNull
    private final QEntity<T, ID> qEntity;

    @NonNull
    private final QElementCollection<?> qElementCollection;

    private final int batchSize;

    private final int defaultStreamFetchSize;

    private TrackedEntityBatch currentBatch = new TrackedEntityBatch();

    List<?> trackEntity(@NonNull T entity) {
        if (currentBatch.trackedEntities.size() >= batchSize || currentBatch.entityElements != null) {
            currentBatch = new TrackedEntityBatch();
        }

        this.currentBatch.trackedEntities.add(entity);

        return (List<?>) Proxy.newProxyInstance(
                InstanceProxy.class.getClassLoader(),
                new Class[]{List.class, LazyList.class},
                new InstanceProxy(entity, this.currentBatch)
        );
    }
}
