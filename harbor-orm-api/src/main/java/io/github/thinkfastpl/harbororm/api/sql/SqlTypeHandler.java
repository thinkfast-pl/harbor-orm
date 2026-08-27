// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Low-level JDBC type handler for reading and writing column values.
 *
 * <p>Implementations control exactly how a value is set on a {@link PreparedStatement}
 * and read from a {@link ResultSet}. Use this when the default JDBC type mapping is
 * insufficient (e.g., PostgreSQL custom types, composite types, or specialized binary formats).
 *
 * <p>Implementations must:
 * <ul>
 *   <li>Have a public no-argument constructor</li>
 *   <li>Be stateless and thread-safe</li>
 * </ul>
 *
 * <p>Instances are cached and reused across all operations.
 *
 * @param <T> the Java type this handler manages
 * @see io.github.thinkfastpl.harbororm.api.annotations.TypeHandler
 */
public interface SqlTypeHandler<T> {

    /**
     * Returns the {@link java.sql.Types} constant for parameter binding.
     * Used when setting null values via {@code PreparedStatement.setNull(index, sqlType)}.
     */
    int sqlType();

    /**
     * Sets a non-null value on the prepared statement.
     *
     * <p>Null values are handled by the framework using {@link #sqlType()},
     * so this method is only called with non-null values.
     *
     * @param connection     the active JDBC connection
     * @param ps             the prepared statement
     * @param parameterIndex the 1-based parameter index
     * @param value          the non-null value to set
     */
    void setStatementParameter(Connection connection, PreparedStatement ps,
                               int parameterIndex, T value) throws SQLException;

    /**
     * Reads a value from the result set at the given column index.
     *
     * @param connection  the active JDBC connection
     * @param resultSet   the result set positioned on the current row
     * @param columnIndex the 1-based column index
     * @param clazz       the expected Java type
     * @return the read value, or {@code null} if the database value is NULL
     */
    T readCell(Connection connection, ResultSet resultSet,
               int columnIndex, Class<? extends T> clazz) throws SQLException;

    /**
     * Reads a value from a raw JSON scalar produced by a multiset aggregation.
     *
     * <p>Inside {@code DSL.multisetAgg}, column values are transported as JSON scalars in
     * their database representation (e.g. a {@code BIGINT}-backed column arrives as a JSON
     * number). Implementations convert that raw scalar to the handled Java type.
     *
     * <p>The default implementation throws {@link UnsupportedOperationException}; override
     * it to support reading this handler's columns inside multiset aggregations.
     *
     * @param rawJsonValue the raw JSON scalar (String, Number or Boolean), or {@code null}
     *                     if the database value was NULL
     * @param clazz        the expected Java type
     * @return the converted value, or {@code null}
     */
    default T readJsonValue(Object rawJsonValue, Class<? extends T> clazz) {
        throw new UnsupportedOperationException("Type handler " + getClass().getName()
                + " does not support reading values inside multiset aggregations; override readJsonValue() to enable it");
    }
}
