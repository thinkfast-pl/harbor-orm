// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect;

import lombok.NonNull;
import lombok.Value;

/**
 * Carrier object for PostgreSQL {@code = ANY(?)} array parameters.
 * <p>
 * Created by {@link PostgreSqlDialect} when rewriting {@code IN} to {@code = ANY},
 * and consumed by {@link PostgreSqlRdbmsSupport#setStatementParameter} to create
 * a {@link java.sql.Array} via {@link java.sql.Connection#createArrayOf}.
 */
@Value
class PostgreSqlArrayValue {
    /** The PostgreSQL type name for {@code createArrayOf}, e.g. {@code "text"}, {@code "int4"}. */
    @NonNull String pgTypeName;
    /** The converted constant values (already run through any attribute converter). */
    @NonNull Object[] values;
}
