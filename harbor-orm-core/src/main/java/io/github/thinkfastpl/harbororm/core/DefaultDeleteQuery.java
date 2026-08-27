// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.QTableName;
import io.github.thinkfastpl.harbororm.api.query.DeleteQuery;
import io.github.thinkfastpl.harbororm.api.query.data.DeleteQueryData;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
class DefaultDeleteQuery implements
        DeleteQuery.FromStep,
        DeleteQuery.WhereStep,
        DeleteQuery.ReturningStep,
        DeleteQuery.ExecuteStep {

    @NonNull
    private final QueryExecutor queryExecutor;

    private QTableName table;
    private final List<Condition> conditions = new ArrayList<>();
    private final List<Expression<?>> returningExpressions = new ArrayList<>();

    @Override
    public DeleteQuery.WhereStep from(@NonNull QTableName table) {
        this.table = table;
        return this;
    }

    @Override
    public DeleteQuery.WhereStep where(@NonNull List<Condition> conditions) {
        this.conditions.addAll(conditions);
        return this;
    }

    @Override
    public DeleteQuery.ExecuteFetchStep<Record> returning(@NonNull List<Expression<?>> expressions) {
        this.returningExpressions.addAll(expressions);
        return new DeleteQuery.ExecuteFetchStep<>() {

            @Override
            public List<Record> executeAndFetchAll() {
                return queryExecutor.deleteReturning(toData());
            }

            @Override
            public int execute() {
                return DefaultDeleteQuery.this.execute();
            }
        };
    }

    @Override
    public int execute() {
        return queryExecutor.delete(toData());
    }

    private DeleteQueryData toData() {
        return new DeleteQueryData(
                table,
                conditions,
                returningExpressions
        );
    }
}
