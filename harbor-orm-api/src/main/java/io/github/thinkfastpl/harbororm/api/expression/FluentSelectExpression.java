// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.api.metadata.QTableSource;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Fluent API wrapper that delegates to {@link DefaultSelectExpression} and provides
 * a step-by-step method-chaining interface for building SQL SELECT queries.
 *
 * @param <T> the Java type of the query result rows
 */
@RequiredArgsConstructor
@Getter
public class FluentSelectExpression<T> implements
        SelectExpression.SelectStep<T>,
        SelectExpression.FromStep<T>,
        SelectExpression.JoinStep<T>,
        SelectExpression.JoinOnStep<T>,
        SelectExpression.WhereState<T>,
        SelectExpression.GroupByStep<T>,
        SelectExpression.HavingStep<T>,
        SelectExpression.CombinationStep<T>,
        SelectExpression.OrderStep<T>,
        SelectExpression.LimitStep<T>,
        SelectExpression.OffsetStep<T> {

    @NonNull
    private final DefaultSelectExpression<T> expression;

    private QTableSource joinTableSource;
    private Join.Type joinType;
    private boolean joinLateral;

    @Override
    public Class<T> getJavaType() {
        return expression.getJavaType();
    }

    @Override
    public SelectExpression.SelectStep<T> select(@NonNull Expression<?> expression) {
        this.expression.select(expression);
        return this;
    }

    @Override
    public SelectExpression.SelectStep<T> select(@NonNull List<? extends Expression<?>> expressions) {
        this.expression.select(expressions);
        return this;
    }

    @Override
    public SelectExpression.JoinStep<T> from(@NonNull QTableSource tableSource) {
        this.expression.from(tableSource);
        return this;
    }

    @Override
    public SelectExpression.JoinOnStep<T> join(@NonNull QTableSource tableSource) {
        this.flushJoin();
        this.joinTableSource = tableSource;
        this.joinType = Join.Type.INNER;
        this.joinLateral = false;
        return this;
    }

    @Override
    public SelectExpression.JoinOnStep<T> leftJoin(@NonNull QTableSource tableSource) {
        this.flushJoin();
        this.joinTableSource = tableSource;
        this.joinType = Join.Type.LEFT;
        this.joinLateral = false;
        return this;
    }

    @Override
    public SelectExpression.JoinOnStep<T> rightJoin(@NonNull QTableSource tableSource) {
        this.flushJoin();
        this.joinTableSource = tableSource;
        this.joinType = Join.Type.RIGHT;
        this.joinLateral = false;
        return this;
    }

    @Override
    public SelectExpression.JoinStep<T> crossJoin(@NonNull QTableSource tableSource) {
        this.flushJoin();
        this.joinTableSource = tableSource;
        this.joinType = Join.Type.CROSS;
        this.joinLateral = false;
        return this;
    }

    @Override
    public SelectExpression.JoinOnStep<T> innerJoinLateral(@NonNull QTableSource tableSource) {
        this.flushJoin();
        this.joinTableSource = tableSource;
        this.joinType = Join.Type.INNER;
        this.joinLateral = true;
        return this;
    }

    @Override
    public SelectExpression.JoinOnStep<T> leftJoinLateral(@NonNull QTableSource tableSource) {
        this.flushJoin();
        this.joinTableSource = tableSource;
        this.joinType = Join.Type.LEFT;
        this.joinLateral = true;
        return this;
    }

    @Override
    public SelectExpression.JoinStep<T> crossJoinLateral(@NonNull QTableSource tableSource) {
        this.flushJoin();
        this.joinTableSource = tableSource;
        this.joinType = Join.Type.CROSS;
        this.joinLateral = true;
        return this;
    }

    @Override
    public SelectExpression.JoinStep<T> on(@NonNull List<Condition> conditions) {
        if (this.joinTableSource == null || this.joinType == null) {
            throw new IllegalStateException("You must first call some join method");
        }
        this.expression.join(new Join(this.joinTableSource, this.joinType, DSL.and(conditions), this.joinLateral));
        this.joinTableSource = null;
        this.joinType = null;
        this.joinLateral = false;
        return this;
    }

    @Override
    public SelectExpression.WhereState<T> where(@NonNull List<Condition> conditions) {
        this.expression.where(conditions);
        return this;
    }

    @Override
    public SelectExpression.HavingStep<T> groupBy(@NonNull List<Expression<?>> expressions) {
        this.expression.groupBy(expressions);
        return this;
    }

    @Override
    public SelectExpression.CombinationStep<T> having(@NonNull List<Condition> conditions) {
        this.expression.having(conditions);
        return this;
    }

    @Override
    public SelectExpression.CombinationStep<T> combine(@NonNull SelectExpression<T> selectExpression, @NonNull SelectCombination combination, boolean all) {
        this.expression.combine(selectExpression, combination, all);
        return this;
    }

    @Override
    public SelectExpression.LimitStep<T> orderBy(@NonNull List<Order> orders) {
        this.expression.orderBy(orders);
        return this;
    }

    @Override
    public SelectExpression.OffsetStep<T> limit(@NonNull Integer limit) {
        this.expression.limit(limit);
        return this;
    }

    @Override
    public SelectExpression.CompleteStep<T> offset(@NonNull Integer offset) {
        this.expression.offset(offset);
        return this;
    }

    @Override
    public SelectExpression.CompleteStep<T> forUpdate() {
        this.expression.forUpdate();
        return this;
    }

    @Override
    public SelectExpression<T> asNonFluent() {
        this.flushJoin();
        return expression;
    }

    @Override
    public SelectExpressionTableSource asTableSource(@NonNull String alias) {
        this.flushJoin();
        return new DefaultSelectExpressionTableSource(expression, alias);
    }

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }

    private void flushJoin() {
        if (joinTableSource != null && joinType != null) {
            expression.join(new Join(joinTableSource, joinType, null, joinLateral));
            joinTableSource = null;
            joinType = null;
            joinLateral = false;
        }
    }
}
