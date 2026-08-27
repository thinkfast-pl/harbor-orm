// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import lombok.NonNull;
import lombok.Value;

/**
 * Immutable value class holding a fully qualified table reference: name, optional schema, and optional alias.
 *
 * <p>Used by {@link QEntity#getTableName()} and {@link QTable#getTableName()} to identify
 * the database table. The alias is applied in SQL as {@code "table_name" alias} and is used
 * to qualify column references in multi-table queries.
 *
 * @see QTableSource
 * @see QEntity
 * @see QTable
 */
@Value
public class QTableName implements QTableSource {

    /**
     * The database table name.
     */
    @NonNull
    String name;

    /**
     * The database schema name, or {@code null} if not specified.
     */
    String schema;

    /**
     * The table alias used in SQL queries, or {@code null} for no alias.
     */
    String alias;

    /**
     * Returns a copy of this table name with the alias removed.
     *
     * @return a new {@code QTableName} with the same name and schema but no alias
     */
    public QTableName withoutAlias() {
        return new QTableName(name, schema, null);
    }
}
