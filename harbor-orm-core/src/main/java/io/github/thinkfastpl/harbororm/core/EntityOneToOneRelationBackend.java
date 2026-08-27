// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.LazyRef;
import io.github.thinkfastpl.harbororm.api.metadata.QEntity;
import io.github.thinkfastpl.harbororm.api.metadata.QEntityRelation;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

class EntityOneToOneRelationBackend<T, ID> extends AbstractEntityRelationBackend<T, ID, Object> {

    private class LazyRefImpl<R> implements LazyRef<R>, TrackableLazyRef {

        @NonNull
        private final T entity;

        @NonNull
        private final TrackedEntityBatch batch;

        private boolean loaded = false;
        private R value;

        private LazyRefImpl(@NonNull T entity, @NonNull TrackedEntityBatch batch) {
            this.entity = entity;
            this.batch = batch;
        }

        @Override
        @SuppressWarnings("unchecked")
        public R get() {
            if (!loaded) {
                value = (R) batch.getElementForEntity(entity);
                loaded = true;
            }
            return value;
        }

        @Override
        public Optional<R> toOptional() {
            return Optional.ofNullable(get());
        }

        @Override
        public boolean isPotentiallyModified() {
            return loaded;
        }
    }

    EntityOneToOneRelationBackend(@NonNull QueryExecutor executor, @NonNull QEntity<T, ID> qEntity, @NonNull QEntityRelation<?, ?> entityRelation, int batchSize, int defaultStreamFetchSize) {
        super(executor, qEntity, entityRelation, batchSize, defaultStreamFetchSize);
    }

    @Override
    protected Object mapMatchingRecords(List<?> matchingRecords, T entity) {
        if (matchingRecords.size() > 1) {
            throw new IllegalStateException(
                    "@OneToOne relation '%s' on %s resolved to %d rows. Expected at most 1. Ensure a UNIQUE constraint exists on the foreign key column in the child table."
                            .formatted(entityRelation.getPropertyName(), qEntity.getBeanType().getSimpleName(), matchingRecords.size())
            );
        }
        return matchingRecords.isEmpty() ? null : matchingRecords.get(0);
    }

    @Override
    protected Object createEntityWrapper(T entity, TrackedEntityBatch batch) {
        return new LazyRefImpl<>(entity, batch);
    }
}
