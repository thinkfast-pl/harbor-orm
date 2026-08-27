// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.expression.DSL;
import io.github.thinkfastpl.harbororm.api.expression.FunctionCallTableSource;
import io.github.thinkfastpl.harbororm.api.expression.Order;
import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.metadata.QTableSource;
import io.github.thinkfastpl.harbororm.api.metadata.QView;
import io.github.thinkfastpl.harbororm.api.query.ViewQuery;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Runtime implementation of {@link ViewQuery} for both {@code @View} classes and
 * table-returning {@code @StoredFunction} results.
 *
 * <p>Builds a SELECT query from the view/function's column definitions and table source,
 * then delegates SQL generation and execution to the underlying {@link QueryExecutor}.
 * Results are mapped to typed Java instances via {@link RecordToViewMapper}.
 *
 * @param <T> the view or stored-function result type
 */
class DefaultViewQuery<T> implements ViewQuery<T> {

    @NonNull
    private final QueryExecutor executor;

    @NonNull
    private final Class<T> beanType;

    @NonNull
    private final List<QColumn<?>> columns;

    @NonNull
    private final QTableSource tableSource;

    private final int defaultStreamFetchSize;

    private boolean distinct;
    private List<Condition> conditions;
    private List<Order> orders;
    private Integer limit;
    private Integer offset;

    /** Constructs a query targeting a {@code @View} class. */
    DefaultViewQuery(@NonNull QueryExecutor executor, @NonNull QView<T> qView, int defaultStreamFetchSize) {
        this(executor, qView.getBeanType(), qView.getAllColumns(), qView.getTableName(), defaultStreamFetchSize);
    }

    /** Constructs a query targeting a table-returning {@code @StoredFunction}. */
    DefaultViewQuery(@NonNull QueryExecutor executor, @NonNull FunctionCallTableSource<T> fnCall, int defaultStreamFetchSize) {
        this(executor, fnCall.getBeanType(), fnCall.getColumns(), fnCall, defaultStreamFetchSize);
    }

    private DefaultViewQuery(@NonNull QueryExecutor executor, @NonNull Class<T> beanType,
                             @NonNull List<QColumn<?>> columns, @NonNull QTableSource tableSource, int defaultStreamFetchSize) {
        this.executor = executor;
        this.beanType = beanType;
        this.columns = columns;
        this.tableSource = tableSource;
        this.defaultStreamFetchSize = defaultStreamFetchSize;
    }

    @Override
    public ViewQuery<T> where(@NonNull List<Condition> conditions) {
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
    public ViewQuery<T> orderBy(@NonNull List<Order> orders) {
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
    public ViewQuery<T> limit(@NonNull Integer limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public ViewQuery<T> offset(@NonNull Integer offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public ViewQuery<T> distinct() {
        this.distinct = true;
        return this;
    }

    @Override
    public boolean exists() {
        return DefaultSelectQuery.ofSingleExpressionSelect(executor, false, DSL.exists(
                DSL.selectAsterisk()
                        .from(tableSource)
                        .where(conditions != null ? conditions : Collections.emptyList())
        ), defaultStreamFetchSize).fetchSingle();
    }

    @Override
    public long count() {
        return DefaultSelectQuery.ofSingleExpressionSelect(executor, false, DSL.count(), defaultStreamFetchSize)
                .from(tableSource)
                .where(conditions != null ? conditions : Collections.emptyList())
                .fetchSingle();
    }

    @Override
    public List<T> fetchAll() {
        var query = new DefaultSelectQuery<>(executor, distinct, beanType, new RecordToViewMapper<>(beanType, columns), defaultStreamFetchSize)
                .select(new ArrayList<>(columns))
                .from(tableSource)
                .where(conditions != null ? conditions : Collections.emptyList())
                .orderBy(orders != null ? orders : Collections.emptyList());

        if (limit != null) {
            query.limit(limit);
        }
        if (offset != null) {
            query.offset(offset);
        }

        return query.fetchAll();
    }

    @Override
    public Stream<T> streamAll() {
        return streamAll(defaultStreamFetchSize);
    }

    @Override
    public Stream<T> streamAll(int fetchSize) {
        var query = new DefaultSelectQuery<>(executor, distinct, beanType, new RecordToViewMapper<>(beanType, columns), defaultStreamFetchSize)
                .select(new ArrayList<>(columns))
                .from(tableSource)
                .where(conditions != null ? conditions : Collections.emptyList())
                .orderBy(orders != null ? orders : Collections.emptyList());

        if (limit != null) {
            query.limit(limit);
        }
        if (offset != null) {
            query.offset(offset);
        }

        return query.fetchStream(fetchSize);
    }
}
