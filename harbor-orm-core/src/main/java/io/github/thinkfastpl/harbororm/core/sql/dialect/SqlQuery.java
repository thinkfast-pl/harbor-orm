// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql.dialect;

import io.github.thinkfastpl.harbororm.api.sql.SqlTypeHandler;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

/**
 * Immutable value object representing a parameterized SQL query — the SQL text
 * together with its ordered bind parameters.
 */
@Value
public class SqlQuery {

    /**
     * A single bind parameter carrying its value, JDBC SQL type, and an optional
     * {@link SqlTypeHandler} for custom type serialization.
     */
    @Value
    public static class Param {
        /** The parameter value, or {@code null} for SQL NULL. */
        Object value;

        /** The JDBC SQL type constant from {@link java.sql.Types}. */
        int sqlType;

        /** Optional custom type handler for non-standard JDBC binding, or {@code null} for default JDBC handling. */
        SqlTypeHandler<?> typeHandler;

        /**
         * Creates a parameter with standard JDBC binding (no custom type handler).
         *
         * @param value   the parameter value, or {@code null} for SQL NULL
         * @param sqlType the JDBC SQL type constant from {@link java.sql.Types}
         */
        public Param(Object value, int sqlType) {
            this(value, sqlType, null);
        }

        /**
         * Creates a parameter with an optional custom type handler.
         *
         * @param value       the parameter value, or {@code null} for SQL NULL
         * @param sqlType     the JDBC SQL type constant from {@link java.sql.Types}
         * @param typeHandler custom type handler for serialization, or {@code null} for standard JDBC binding
         */
        public Param(Object value, int sqlType, SqlTypeHandler<?> typeHandler) {
            this.value = value;
            this.sqlType = sqlType;
            this.typeHandler = typeHandler;
        }
    }

    /** The SQL query text. */
    @NonNull
    CharSequence sql;

    /** The ordered list of bind parameters for the query. */
    @NonNull
    List<Param> params;
}
