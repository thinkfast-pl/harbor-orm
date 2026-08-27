// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.expression.*;
import io.github.thinkfastpl.harbororm.api.metadata.QTableSource;
import io.github.thinkfastpl.harbororm.api.query.SelectQuery;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import io.github.thinkfastpl.harbororm.api.query.result.Page;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import lombok.NonNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DefaultSelectQuery<T> implements
        SelectQuery.WithStep<T>,
        SelectQuery.SelectStep<T>,
        SelectQuery.FromStep<T>,
        SelectQuery.JoinStep<T>,
        SelectQuery.JoinOnStep<T>,
        SelectQuery.WhereState<T>,
        SelectQuery.GroupByStep<T>,
        SelectQuery.HavingStep<T>,
        SelectQuery.CombinationStep<T>,
        SelectQuery.OrderStep<T>,
        SelectQuery.LimitStep<T>,
        SelectQuery.OffsetStep<T>,
        SelectQuery.FetchStep<T> {

    @NonNull
    private final QueryExecutor queryExecutor;

    @NonNull
    private final DefaultSelectExpression<T> expression;

    private final int defaultStreamFetchSize;

    private QTableSource joinTableSource;
    private Join.Type joinType;
    private boolean joinLateral;

    static <T> SelectQuery.SelectStep<T> ofSingleExpressionSelect(@NonNull QueryExecutor queryExecutor, boolean distinct, @NonNull Expression<T> expression, int defaultStreamFetchSize) {
        return new DefaultSelectQuery<>(queryExecutor, distinct, expression.getJavaType(), record -> record.get(expression), defaultStreamFetchSize)
                .select(List.of(expression));
    }

    DefaultSelectQuery(@NonNull QueryExecutor queryExecutor, boolean distinct, @NonNull Class<T> resultClass, @NonNull Function<Record, T> recordMapper, int defaultStreamFetchSize) {
        this.queryExecutor = queryExecutor;
        this.expression = new DefaultSelectExpression<>(distinct, recordMapper, resultClass);
        this.defaultStreamFetchSize = defaultStreamFetchSize;
    }

    @Override
    public SelectQuery.WithStep<T> with(@NonNull List<CommonTableExpression> ctes, boolean recursive) {
        this.expression.with(ctes, recursive);
        return this;
    }

    @Override
    public SelectQuery.SelectStep<T> select(@NonNull List<? extends Expression<?>> expressions) {
        this.expression.select(expressions);
        return this;
    }

    @Override
    public SelectQuery.JoinStep<T> from(@NonNull QTableSource tableSource) {
        this.expression.from(tableSource);
        return this;
    }

    @Override
    public SelectQuery.JoinOnStep<T> join(@NonNull QTableSource tableSource) {
        this.flushJoin();
        this.joinTableSource = tableSource;
        this.joinType = Join.Type.INNER;
        this.joinLateral = false;
        return this;
    }

    @Override
    public SelectQuery.JoinOnStep<T> leftJoin(@NonNull QTableSource tableSource) {
        this.flushJoin();
        this.joinTableSource = tableSource;
        this.joinType = Join.Type.LEFT;
        this.joinLateral = false;
        return this;
    }

    @Override
    public SelectQuery.JoinOnStep<T> rightJoin(@NonNull QTableSource tableSource) {
        this.flushJoin();
        this.joinTableSource = tableSource;
        this.joinType = Join.Type.RIGHT;
        this.joinLateral = false;
        return this;
    }

    @Override
    public SelectQuery.JoinOnStep<T> fullOuterJoin(@NonNull QTableSource tableSource) {
        this.flushJoin();
        this.joinTableSource = tableSource;
        this.joinType = Join.Type.FULL_OUTER;
        this.joinLateral = false;
        return this;
    }

    @Override
    public SelectQuery.JoinStep<T> crossJoin(@NonNull QTableSource tableSource) {
        this.flushJoin();
        this.joinTableSource = tableSource;
        this.joinType = Join.Type.CROSS;
        this.joinLateral = false;
        return this;
    }

    @Override
    public SelectQuery.JoinOnStep<T> innerJoinLateral(@NonNull QTableSource tableSource) {
        this.flushJoin();
        this.joinTableSource = tableSource;
        this.joinType = Join.Type.INNER;
        this.joinLateral = true;
        return this;
    }

    @Override
    public SelectQuery.JoinOnStep<T> leftJoinLateral(@NonNull QTableSource tableSource) {
        this.flushJoin();
        this.joinTableSource = tableSource;
        this.joinType = Join.Type.LEFT;
        this.joinLateral = true;
        return this;
    }

    @Override
    public SelectQuery.JoinStep<T> crossJoinLateral(@NonNull QTableSource tableSource) {
        this.flushJoin();
        this.joinTableSource = tableSource;
        this.joinType = Join.Type.CROSS;
        this.joinLateral = true;
        return this;
    }

    @Override
    public SelectQuery.JoinStep<T> on(@NonNull Condition condition) {
        if (this.joinTableSource == null || this.joinType == null) {
            throw new IllegalStateException("You must first call some join method");
        }
        this.expression.join(new Join(this.joinTableSource, this.joinType, condition, this.joinLateral));
        this.joinTableSource = null;
        this.joinType = null;
        this.joinLateral = false;
        return this;
    }

    @Override
    public SelectQuery.WhereState<T> where(@NonNull List<Condition> conditions) {
        this.expression.where(conditions);
        return this;
    }

    @Override
    public SelectQuery.HavingStep<T> groupBy(@NonNull List<Expression<?>> expressions) {
        this.expression.groupBy(expressions);
        return this;
    }

    @Override
    public SelectQuery.CombinationStep<T> having(@NonNull List<Condition> conditions) {
        this.expression.having(conditions);
        return this;
    }

    @Override
    public SelectQuery.CombinationStep<T> combine(@NonNull SelectExpression<T> selectExpression, @NonNull SelectCombination combination, boolean all) {
        this.expression.combine(selectExpression, combination, all);
        return this;
    }

    @Override
    public SelectQuery.LimitStep<T> orderBy(@NonNull List<Order> orders) {
        this.expression.orderBy(orders);
        return this;
    }

    @Override
    public SelectQuery.OffsetStep<T> limit(@NonNull Integer limit) {
        this.expression.limit(limit);
        return this;
    }

    @Override
    public SelectQuery.FetchStep<T> offset(@NonNull Integer offset) {
        this.expression.offset(offset);
        return this;
    }

    @Override
    public SelectQuery.FetchStep<T> forUpdate() {
        this.expression.forUpdate();
        return this;
    }

    @Override
    public List<T> fetchAll() {
        this.flushJoin();
        return queryExecutor.fetchAll(this.expression.toQueryData()).stream()
                .map(this.expression.getRecordMapper())
                .collect(Collectors.toList());
    }

    @Override
    public Stream<T> fetchStream() {
        return fetchStream(defaultStreamFetchSize);
    }

    @Override
    public Stream<T> fetchStream(int fetchSize) {
        this.flushJoin();
        return queryExecutor.fetchStream(this.expression.toQueryData(), fetchSize, this.expression.getRecordMapper());
    }

    @Override
    public Page<T> fetchPage(int pageSize, int page) {
        this.limit(pageSize);
        this.offset(page * pageSize);
        return PageFetcher.fetchPage(queryExecutor, this.expression, pageSize, page);
    }

    private void flushJoin() {
        if (joinTableSource != null && joinType != null) {
            // null condition is intentional — CROSS JOIN has no ON clause; other join types
            // set the condition via the on() method before the next flushJoin() call
            expression.join(new Join(joinTableSource, joinType, null, joinLateral));
            joinTableSource = null;
            joinType = null;
            joinLateral = false;
        }
    }
}
