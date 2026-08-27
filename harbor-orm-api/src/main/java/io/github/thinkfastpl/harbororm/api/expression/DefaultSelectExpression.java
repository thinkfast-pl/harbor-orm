// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.api.metadata.QTableSource;
import io.github.thinkfastpl.harbororm.api.query.data.SelectQueryData;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Default mutable implementation of {@link SelectExpression}.
 * Accumulates all SQL SELECT clauses (WITH, SELECT, FROM, JOIN, WHERE, GROUP BY, HAVING,
 * ORDER BY, LIMIT, OFFSET) and converts them to a {@link io.github.thinkfastpl.harbororm.api.query.data.SelectQueryData}.
 *
 * @param <T> the Java type of the query result rows
 */
@RequiredArgsConstructor
public class DefaultSelectExpression<T> implements SelectExpression<T> {

    private final boolean distinct;

    @NonNull
    @Getter
    private final Function<Record, T> recordMapper;

    @NonNull
    @Getter
    private final Class<T> javaType;

    private List<CommonTableExpression> ctes;
    private boolean withRecursive = false;
    private final List<Expression<?>> selectExpressions = new ArrayList<>();
    private QTableSource from;
    private List<Join> joins;
    private List<Condition> whereConditions;
    private List<Expression<?>> groupByExpressions;
    private List<Condition> havingConditions;
    private List<SelectQueryData.Combination> combinations;
    private List<Order> orders;
    private Integer limit;
    private Integer offset;
    private boolean forUpdate;

    @Override
    public void with(@NonNull List<CommonTableExpression> ctes, boolean recursive) {
        if (!ctes.isEmpty()) {
            if (this.ctes == null) {
                this.ctes = new ArrayList<>(ctes);
            } else {
                this.ctes.addAll(ctes);
            }

            if (recursive) {
                this.withRecursive = true;
            }
        }
    }

    @Override
    public void select(@NonNull Expression<?> expression) {
        this.selectExpressions.add(expression);
    }

    @Override
    public void select(@NonNull List<? extends Expression<?>> expressions) {
        this.selectExpressions.addAll(expressions);
    }

    @Override
    public void from(@NonNull QTableSource tableSource) {
        this.from = tableSource;
    }

    @Override
    public void join(@NonNull Join join) {
        if (joins == null) {
            joins = new ArrayList<>();
        }
        joins.add(join);
    }

    @Override
    public void where(@NonNull List<Condition> conditions) {
        if (!conditions.isEmpty()) {
            if (whereConditions == null) {
                whereConditions = new ArrayList<>();
            }
            whereConditions.addAll(conditions);
        }
    }

    @Override
    public void groupBy(@NonNull List<Expression<?>> expressions) {
        if (!expressions.isEmpty()) {
            if (groupByExpressions == null) {
                groupByExpressions = new ArrayList<>();
            }
            groupByExpressions.addAll(expressions);
        }
    }

    @Override
    public void having(@NonNull List<Condition> conditions) {
        if (!conditions.isEmpty()) {
            if (havingConditions == null) {
                havingConditions = new ArrayList<>(conditions);
            } else {
                havingConditions.addAll(conditions);
            }
        }
    }

    @Override
    public void combine(@NonNull SelectExpression<T> selectExpression, @NonNull SelectCombination combination, boolean all) {
        if (this.combinations == null) {
            this.combinations = new ArrayList<>();
        }

        this.combinations.add(new SelectQueryData.Combination(
                selectExpression,
                combination,
                all
        ));
    }

    @Override
    public void orderBy(@NonNull List<Order> orders) {
        if (!orders.isEmpty()) {
            if (this.orders == null) {
                this.orders = new ArrayList<>(orders);
            } else {
                this.orders.addAll(orders);
            }
        }
    }

    @Override
    public void limit(@NonNull Integer limit) {
        this.limit = limit;
    }

    @Override
    public void offset(@NonNull Integer offset) {
        this.offset = offset;
    }

    @Override
    public void forUpdate() {
        this.forUpdate = true;
    }

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }

    public SelectQueryData toQueryData() {
        return new SelectQueryData(
                ctes,
                withRecursive,
                distinct,
                selectExpressions,
                from,
                joins,
                whereConditions,
                groupByExpressions,
                havingConditions,
                combinations,
                orders,
                limit,
                offset,
                forUpdate
        );
    }
}
