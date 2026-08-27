// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.query.data;

import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

/**
 * Immutable data carrier for an {@code ON CONFLICT} clause attached to an INSERT statement.
 *
 * @see InsertQueryData
 */
@Value
public class OnConflictData {

    /**
     * The conflict action to take when a constraint violation occurs.
     */
    public enum ConflictAction {
        DO_NOTHING,
        DO_UPDATE
    }

    /** Columns that form the conflict target (e.g., the unique index columns). */
    @NonNull
    List<QColumn<?>> conflictColumns;

    /** The action to take on conflict. */
    @NonNull
    ConflictAction action;

    /** Columns to update in DO UPDATE SET (parallel with {@link #updateValues}). Empty for DO_NOTHING. */
    @NonNull
    List<QColumn<?>> updateColumns;

    /** Expressions for the update values (parallel with {@link #updateColumns}). Empty for DO_NOTHING. */
    @NonNull
    List<Expression<?>> updateValues;

    /** Optional WHERE condition on the DO UPDATE clause. Null if no WHERE. */
    Expression<Boolean> whereClause;
}
