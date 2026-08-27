// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.metadata.QTableName;
import io.github.thinkfastpl.harbororm.api.query.InsertQuery;
import io.github.thinkfastpl.harbororm.api.query.data.InsertQueryData;
import io.github.thinkfastpl.harbororm.api.query.data.OnConflictData;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import io.github.thinkfastpl.harbororm.api.query.result.InsertResult;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
class DefaultInsertQuery implements InsertQuery {

    @NonNull
    private final QueryExecutor queryExecutor;

    private QTableName table;

    private final List<QColumn<?>> columns = new ArrayList<>();
    private final List<Map<QColumn<?>, Expression<?>>> rows = new ArrayList<>();
    private Map<QColumn<?>, Expression<?>> currentRow;
    private final List<Expression<?>> returningExpressions = new ArrayList<>();

    // ON CONFLICT state
    private OnConflictData onConflictData;

    @Override
    public void into(@NonNull QTableName table) {
        this.table = table;
    }

    @Override
    public <C> void set(@NonNull QColumn<C> column, @NonNull Expression<C> value) {
        if (currentRow == null) {
            nextRow();
        }

        if (rows.size() == 1) {
            columns.add(column);
        }

        this.currentRow.put(column, value);

    }

    @Override
    public void nextRow() {
        currentRow = new IdentityHashMap<>();
        rows.add(currentRow);
    }

    @Override
    public void returning(@NonNull List<Expression<?>> expressions) {
        this.returningExpressions.addAll(expressions);
    }

    @Override
    public List<Record> executeAndFetchAll() {
        return queryExecutor.insertReturning(toData());
    }

    @Override
    public InsertResult execute() {
        return queryExecutor.insert(toData());
    }

    void setOnConflictData(OnConflictData onConflictData) {
        this.onConflictData = onConflictData;
    }

    private InsertQueryData toData() {
        return new InsertQueryData(
                table,
                columns,
                rows,
                returningExpressions,
                onConflictData
        );
    }
}
