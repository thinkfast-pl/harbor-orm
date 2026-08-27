// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.query.result;

import java.util.List;

/**
 * The result of executing an INSERT statement.
 *
 * <p>Contains the number of rows affected and any auto-generated keys returned by the database
 * (e.g., from identity or serial columns).
 *
 * @param rowsUpdated   the number of rows inserted
 * @param generatedKeys records containing the auto-generated key values, one per inserted row;
 *                      empty if the table has no generated keys
 */
public record InsertResult(
        int rowsUpdated,
        List<Record> generatedKeys
) {
}
