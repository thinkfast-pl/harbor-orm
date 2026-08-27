// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import java.util.List;

/**
 * Table metadata interface for pure data retrieval queries.
 *
 * <p>Generated {@code <TableName>Table} classes implement this interface. Unlike {@link QEntity},
 * table metadata carries no entity lifecycle information (no callbacks, no ID column, no bean type).
 * It is intended for read-only queries where entity identity and persistence lifecycle are not needed.
 *
 * <p>Example usage:
 * <pre>{@code
 * SomesTable SOMES = new SomesTable("s");
 * List<Record> records = session.select(SOMES.name, SOMES.id)
 *     .from(SOMES)
 *     .where(SOMES.name.eq("value"))
 *     .fetchAll();
 * }</pre>
 *
 * @see QEntity
 * @see QColumn
 */
public interface QTable {

    /**
     * Returns the qualified table name including optional schema and alias.
     *
     * @return the table name metadata
     */
    QTableName getTableName();

    /**
     * Returns all column references defined for this table.
     *
     * @return an immutable list of all columns
     */
    List<QColumn<?>> getAllColumns();
}
