// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.query.data;

import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.QTableName;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

/**
 * Immutable data carrier for a SQL DELETE statement.
 *
 * <p>Holds the target table, WHERE conditions, and optional RETURNING expressions
 * that together describe a fully built DELETE query ready for execution by a
 * {@link io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor}.
 *
 * @see io.github.thinkfastpl.harbororm.api.query.DeleteQuery
 */
@Value
public class DeleteQueryData {

    /** Target table to delete rows from. */
    @NonNull
    QTableName table;

    /** WHERE conditions that restrict which rows are deleted. May be empty for unconditional deletes. */
    @NonNull
    List<Condition> conditions;

    /** Expressions to return from deleted rows (RETURNING clause). Empty if no RETURNING. */
    @NonNull
    List<Expression<?>> returningExpressions;
}
