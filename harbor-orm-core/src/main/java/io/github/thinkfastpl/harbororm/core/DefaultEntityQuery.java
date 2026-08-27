// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.expression.DSL;
import io.github.thinkfastpl.harbororm.api.expression.Order;
import io.github.thinkfastpl.harbororm.api.metadata.QEntity;
import io.github.thinkfastpl.harbororm.api.query.EntityQuery;
import io.github.thinkfastpl.harbororm.api.query.SelectQuery;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
class DefaultEntityQuery<T, ID> implements EntityQuery<T, ID> {
    @NonNull
    private final QueryExecutor executor;

    @NonNull
    private final QEntity<T, ID> qEntity;

    private final int lazyLoadBatchSize;

    private final int defaultStreamFetchSize;

    private List<Condition> conditions;

    private List<Order> orders;

    private Integer limit;

    private Integer offset;

    private boolean forUpdate;

    @Override
    public EntityQuery<T, ID> where(@NonNull List<Condition> conditions) {
        if (!conditions.isEmpty()) {
            if (this.conditions == null) {
                this.conditions = new ArrayList<>(conditions);
            } else {
                this.conditions.addAll(conditions);
            }
        }
        return this;
    }

    @Override
    public EntityQuery<T, ID> whereIdEq(@NonNull ID id) {
        return where(qEntity.getIdColumn().eq(id));
    }

    @Override
    public EntityQuery<T, ID> whereIdIn(@NonNull Collection<ID> id) {
        return where(qEntity.getIdColumn().in(id));
    }

    @Override
    public EntityQuery<T, ID> orderBy(@NonNull List<Order> orders) {
        if (!orders.isEmpty()) {
            if (this.orders == null) {
                this.orders = new ArrayList<>(orders);
            } else {
                this.orders.addAll(orders);
            }
        }
        return this;
    }

    @Override
    public EntityQuery<T, ID> limit(@NonNull Integer limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public EntityQuery<T, ID> offset(@NonNull Integer offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public EntityQuery<T, ID> forUpdate() {
        this.forUpdate = true;
        return this;
    }

    @Override
    public boolean exists() {
        return DefaultSelectQuery.ofSingleExpressionSelect(executor, false, DSL.exists(
                DSL.selectAsterisk()
                        .from(qEntity.getTableName())
                        .where(conditions != null ? conditions : Collections.emptyList())
        ), defaultStreamFetchSize).fetchSingle();
    }

    @Override
    public long count() {
        return DefaultSelectQuery.ofSingleExpressionSelect(executor, false, DSL.count(), defaultStreamFetchSize)
                .from(qEntity.getTableName())
                .where(conditions != null ? conditions : Collections.emptyList())
                .fetchSingle();
    }

    @Override
    public List<T> fetchAll() {
        return buildEntityQuery().fetchAll();
    }

    @Override
    public Stream<T> streamAll() {
        return streamAll(defaultStreamFetchSize);
    }

    @Override
    public Stream<T> streamAll(int fetchSize) {
        return buildEntityQuery().fetchStream(fetchSize);
    }

    private SelectQuery.FetchStep<T> buildEntityQuery() {
        final AttributeExtractor extractor = new AttributeExtractor(qEntity);

        var query = new DefaultSelectQuery<>(executor, false, qEntity.getBeanType(), new RecordToEntityMapper<>(executor, qEntity, extractor, lazyLoadBatchSize, defaultStreamFetchSize), defaultStreamFetchSize)
                .select(extractor.getAllColumnsRecursive())
                .from(qEntity.getTableName())
                .where(conditions != null ? conditions : Collections.emptyList())
                .orderBy(orders != null ? orders : Collections.emptyList());

        if (limit != null) {
            query.limit(limit);
        }

        if (offset != null) {
            query.offset(offset);
        }

        if (forUpdate) {
            query.forUpdate();
        }

        return query;
    }
}
