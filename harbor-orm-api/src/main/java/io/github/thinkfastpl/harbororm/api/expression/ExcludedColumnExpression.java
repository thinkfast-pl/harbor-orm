// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Expression representing a reference to a column in the {@code EXCLUDED} pseudo-table,
 * used in PostgreSQL {@code ON CONFLICT ... DO UPDATE SET} clauses.
 *
 * <p>Renders as {@code EXCLUDED."column_name"} in SQL.
 *
 * @param <T> the Java type of the column
 * @see io.github.thinkfastpl.harbororm.api.expression.DSL#excluded(QColumn)
 */
@RequiredArgsConstructor
public final class ExcludedColumnExpression<T> implements Expression<T> {

    @NonNull
    @Getter
    private final QColumn<T> column;

    @Override
    public Class<T> getJavaType() {
        return column.getJavaType();
    }

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return column.getColumnContext(dialectName);
    }
}
