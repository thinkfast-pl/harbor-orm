// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.metadata.QTableName;
import io.github.thinkfastpl.harbororm.api.query.UpdateQuery;
import io.github.thinkfastpl.harbororm.api.query.data.UpdateQueryData;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
class DefaultUpdateQuery implements
        UpdateQuery.TableStep,
        UpdateQuery.SetStep,
        UpdateQuery.WhereStep,
        UpdateQuery.ReturningStep {

    @NonNull
    private final QueryExecutor queryExecutor;

    private QTableName table;
    private final List<QColumn<?>> columns = new ArrayList<>();
    private final List<Expression<?>> values = new ArrayList<>();
    private final List<Condition> conditions = new ArrayList<>();
    private final List<Expression<?>> returningExpressions = new ArrayList<>();

    @Override
    public UpdateQuery.SetStep table(@NonNull QTableName table) {
        this.table = table;
        return this;
    }

    @Override
    public <C> UpdateQuery.SetStep set(@NonNull QColumn<C> column, @NonNull Expression<C> expression) {
        this.columns.add(column);
        this.values.add(expression);
        return this;
    }

    @Override
    public UpdateQuery.WhereStep where(@NonNull List<Condition> conditions) {
        this.conditions.addAll(conditions);
        return this;
    }

    @Override
    public UpdateQuery.ExecuteFetchStep<Record> returning(@NonNull List<Expression<?>> expressions) {
        this.returningExpressions.addAll(expressions);
        return new UpdateQuery.ExecuteFetchStep<>() {

            @Override
            public List<Record> executeAndFetchAll() {
                return queryExecutor.updateReturning(toData());
            }

            @Override
            public int execute() {
                return DefaultUpdateQuery.this.execute();
            }
        };
    }

    @Override
    public int execute() {
        return queryExecutor.update(toData());
    }

    /**
     * Non-generic set method for internal use to avoid ambiguity when C=Object.
     */
    void setRaw(QColumn<?> column, Expression<?> expression) {
        this.columns.add(column);
        this.values.add(expression);
    }

    private UpdateQueryData toData() {
        return new UpdateQueryData(
                table,
                columns,
                values,
                conditions,
                returningExpressions
        );
    }
}
