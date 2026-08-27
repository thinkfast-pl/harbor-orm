// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.metadata.QTableName;
import io.github.thinkfastpl.harbororm.api.query.InsertQuery;
import io.github.thinkfastpl.harbororm.api.query.data.OnConflictData;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
class FluentInsertQuery implements
        InsertQuery.IntoStep,
        InsertQuery.SetStep,
        InsertQuery.ReturningStep,
        InsertQuery.ExecuteStep {

    @NonNull
    private final DefaultInsertQuery insertQuery;

    @Override
    public InsertQuery.SetStep into(@NonNull QTableName table) {
        insertQuery.into(table);
        return this;
    }

    @Override
    public <C> InsertQuery.SetStep set(@NonNull QColumn<C> column, @NonNull Expression<C> value) {
        insertQuery.set(column, value);
        return this;
    }

    @Override
    public InsertQuery.SetStep nextRow() {
        insertQuery.nextRow();
        return this;
    }

    @Override
    public InsertQuery.OnConflictColumnsStep onConflict(@NonNull QColumn<?>... columns) {
        final List<QColumn<?>> conflictColumns = Arrays.asList(columns);

        return new InsertQuery.OnConflictColumnsStep() {

            @Override
            public InsertQuery.OnConflictFinalStep doNothing() {
                insertQuery.setOnConflictData(new OnConflictData(
                        conflictColumns,
                        OnConflictData.ConflictAction.DO_NOTHING,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null
                ));
                return new OnConflictFinalStepImpl();
            }

            @Override
            public InsertQuery.OnConflictUpdateStep doUpdate() {
                return new OnConflictUpdateStepImpl(conflictColumns);
            }
        };
    }

    @Override
    public InsertQuery.ExecuteFetchStep<Record> returning(@NonNull List<Expression<?>> expressions) {
        insertQuery.returning(expressions);
        return newExecuteFetchStep();
    }

    @Override
    public void execute() {
        insertQuery.execute();
    }

    @Override
    public InsertQuery asNonFluent() {
        return insertQuery;
    }

    private InsertQuery.ExecuteFetchStep<Record> newExecuteFetchStep() {
        return new InsertQuery.ExecuteFetchStep<>() {

            @Override
            public List<Record> executeAndFetchAll() {
                return insertQuery.executeAndFetchAll();
            }

            @Override
            public void execute() {
                insertQuery.execute();
            }

            @Override
            public InsertQuery asNonFluent() {
                return insertQuery;
            }
        };
    }

    private class OnConflictFinalStepImpl implements InsertQuery.OnConflictFinalStep {

        @Override
        public InsertQuery.ExecuteFetchStep<Record> returning(@NonNull List<Expression<?>> expressions) {
            insertQuery.returning(expressions);
            return newExecuteFetchStep();
        }

        @Override
        public void execute() {
            insertQuery.execute();
        }

        @Override
        public InsertQuery asNonFluent() {
            return insertQuery;
        }
    }

    private class OnConflictUpdateStepImpl implements InsertQuery.OnConflictUpdateStep {

        private final List<QColumn<?>> conflictColumns;
        private final List<QColumn<?>> updateColumns = new ArrayList<>();
        private final List<Expression<?>> updateValues = new ArrayList<>();

        OnConflictUpdateStepImpl(List<QColumn<?>> conflictColumns) {
            this.conflictColumns = conflictColumns;
        }

        @Override
        public <T> InsertQuery.OnConflictUpdateStep set(@NonNull QColumn<T> column, @NonNull Expression<T> expression) {
            updateColumns.add(column);
            updateValues.add(expression);
            applyData(null);
            return this;
        }

        @Override
        public InsertQuery.OnConflictFinalStep where(@NonNull Expression<Boolean> condition) {
            applyData(condition);
            return new OnConflictFinalStepImpl();
        }

        @Override
        public InsertQuery.ExecuteFetchStep<Record> returning(@NonNull List<Expression<?>> expressions) {
            applyData(null);
            insertQuery.returning(expressions);
            return newExecuteFetchStep();
        }

        @Override
        public void execute() {
            applyData(null);
            insertQuery.execute();
        }

        @Override
        public InsertQuery asNonFluent() {
            applyData(null);
            return insertQuery;
        }

        private void applyData(Expression<Boolean> whereClause) {
            insertQuery.setOnConflictData(new OnConflictData(
                    conflictColumns,
                    OnConflictData.ConflictAction.DO_UPDATE,
                    updateColumns,
                    updateValues,
                    whereClause
            ));
        }
    }
}
