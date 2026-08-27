// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.api.sql.SqlTypeHandler;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Type handler that maps {@link OffsetDateTime} to a plain SQL {@code TIMESTAMP} column.
 *
 * <p>Values are written as the {@link java.time.Instant} they represent, so the original
 * offset is not stored. When reading, the timestamp is interpreted as an instant and
 * returned with the {@link ZoneOffset#UTC UTC} offset. The point in time is always
 * preserved; the offset is normalized to UTC.
 *
 * <p>Use this handler (via {@link io.github.thinkfastpl.harbororm.api.annotations.TypeHandler}) for databases
 * or columns that lack a {@code TIMESTAMP WITH TIME ZONE} type.
 *
 * @see SqlTypeHandler
 */
public class OffsetDateTimeAsTimestampTypeHandler implements SqlTypeHandler<OffsetDateTime> {

    /**
     * Returns {@link Types#TIMESTAMP}.
     */
    @Override
    public int sqlType() {
        return Types.TIMESTAMP;
    }

    /**
     * Binds the value as a {@link Timestamp} built from its instant,
     * discarding the offset information.
     */
    @Override
    public void setStatementParameter(Connection connection, PreparedStatement ps, int parameterIndex, OffsetDateTime value) throws SQLException {
        if (value == null) {
            ps.setNull(parameterIndex, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(parameterIndex, Timestamp.from(value.toInstant()));
        }
    }

    /**
     * Reads the column as a {@link Timestamp} and returns its instant
     * at the {@link ZoneOffset#UTC UTC} offset, or {@code null} for SQL NULL.
     */
    @Override
    public OffsetDateTime readCell(Connection connection, ResultSet resultSet, int columnIndex, Class<? extends OffsetDateTime> clazz) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(columnIndex);
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }

    /**
     * Reads the value from a multiset JSON scalar. Databases render {@code TIMESTAMP} columns inside
     * JSON aggregations as text ({@code "2024-06-15 10:30:45.123456"} or with a {@code 'T'} separator).
     * The text is interpreted the same way {@link #readCell} interprets the column — as a wall-clock
     * time in the JVM default zone — and returned at the {@link ZoneOffset#UTC UTC} offset.
     */
    @Override
    public OffsetDateTime readJsonValue(Object rawJsonValue, Class<? extends OffsetDateTime> clazz) {
        if (rawJsonValue == null) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.parse(rawJsonValue.toString().replace(' ', 'T'));
        return Timestamp.valueOf(localDateTime).toInstant().atOffset(ZoneOffset.UTC);
    }
}
