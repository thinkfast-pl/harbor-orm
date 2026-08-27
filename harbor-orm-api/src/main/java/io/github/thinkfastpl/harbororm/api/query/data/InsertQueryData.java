// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.query.data;

import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.metadata.QTableName;
import lombok.NonNull;
import lombok.Value;

import java.util.List;
import java.util.Map;

/**
 * Immutable data carrier for a SQL INSERT statement.
 *
 * <p>Holds the target table, column list, row values, optional RETURNING expressions,
 * and optional ON CONFLICT handling that together describe a fully built INSERT query
 * ready for execution by a {@link io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor}.
 *
 * @see io.github.thinkfastpl.harbororm.api.query.InsertQuery
 * @see OnConflictData
 */
@Value
public class InsertQueryData {

    /** Target table to insert rows into. */
    @NonNull
    QTableName table;

    /** Columns included in the INSERT column list. */
    @NonNull
    List<QColumn<?>> columns;

    /** Row data to insert. Each map entry pairs a column with its value expression. */
    @NonNull
    List<Map<QColumn<?>, Expression<?>>> rows;

    /** Expressions to return from inserted rows (RETURNING clause). Empty if no RETURNING. */
    @NonNull
    List<Expression<?>> returningExpressions;

    /** ON CONFLICT clause data. Null means no conflict handling (regular INSERT). */
    OnConflictData onConflictData;
}
