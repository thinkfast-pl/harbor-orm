// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.api.metadata.QTableSource;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Common Table Expression (CTE) for use in SQL {@code WITH} clauses.
 * A CTE defines a named temporary result set with typed columns that can be referenced
 * in the main query.
 */
@Getter
public class CommonTableExpression implements QTableSource {

    @RequiredArgsConstructor
    @Getter
    public static class Column<T> implements Expression<T> {

        @NonNull
        private final String tableName;

        @NonNull
        private final String name;

        @NonNull
        private final Class<T> javaType;

        @Override
        public ColumnContext getColumnContext(@NonNull String dialectName) {
            return null;
        }
    }

    private final String tableName;
    private final List<Column<?>> columns = new ArrayList<>();

    private SelectExpression<?> expression;

    public CommonTableExpression(@NonNull String tableName) {
        this.tableName = tableName;
    }

    public CommonTableExpression as(@NonNull SelectExpression<?> expression) {
        if (this.expression != null) {
            throw new IllegalStateException("Expression has been already set");
        }
        this.expression = expression;
        return this;
    }

    public CommonTableExpression as(@NonNull SelectExpression.CompleteStep<?> completeStep) {
        return as(completeStep.asNonFluent());
    }

    public <T> CommonTableExpression.Column<T> column(@NonNull String name, @NonNull Class<T> javaType) {
        final Column<T> column = new Column<>(this.tableName, name, javaType);
        columns.add(column);
        return column;
    }

    public <T> CommonTableExpression.Column<T> column(@NonNull String name, @NonNull SelectableExpression<T> referencedColumn) {
        return column(name, referencedColumn.getJavaType());
    }
}
