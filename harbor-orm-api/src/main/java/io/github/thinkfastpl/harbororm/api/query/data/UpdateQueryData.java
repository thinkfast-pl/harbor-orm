// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.query.data;

import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.metadata.QTableName;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

/**
 * Immutable data carrier for a SQL UPDATE statement.
 *
 * <p>Holds the target table, columns to set, their new values, WHERE conditions,
 * and optional RETURNING expressions that together describe a fully built UPDATE
 * query ready for execution by a {@link io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor}.
 *
 * @see io.github.thinkfastpl.harbororm.api.query.UpdateQuery
 */
@Value
public class UpdateQueryData {

    /** Target table to update rows in. */
    @NonNull
    QTableName table;

    /** Columns to update (parallel with {@link #values}). */
    @NonNull
    List<QColumn<?>> columns;

    /** New values for the columns (parallel with {@link #columns}). */
    @NonNull
    List<Expression<?>> values;

    /** WHERE conditions that restrict which rows are updated. May be empty for unconditional updates. */
    @NonNull
    List<Condition> conditions;

    /** Expressions to return from updated rows (RETURNING clause). Empty if no RETURNING. */
    @NonNull
    List<Expression<?>> returningExpressions;
}
