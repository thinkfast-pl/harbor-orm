// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.metadata.QTableName;
import io.github.thinkfastpl.harbororm.api.metadata.QTableSource;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

/**
 * A table source representing a function call in the FROM clause.
 * Renders as: [schema.]function_name(expr1, expr2, ...) AS alias
 *
 * @param <T> the Java type that rows from this function map to
 */
@Getter
public class FunctionCallTableSource<T> implements QTableSource {

    @NonNull
    private final QTableName tableName;

    @NonNull
    private final List<QColumn<?>> columns;

    @NonNull
    private final List<Expression<?>> params;

    @NonNull
    private final Class<T> beanType;

    public FunctionCallTableSource(@NonNull QTableName tableName,
                                   @NonNull List<QColumn<?>> columns,
                                   @NonNull List<Expression<?>> params,
                                   @NonNull Class<T> beanType) {
        this.tableName = tableName;
        this.columns = columns;
        this.params = params;
        this.beanType = beanType;
    }
}
