// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain.handler;

import io.github.thinkfastpl.harbororm.api.sql.SqlTypeHandler;

import java.sql.*;
import java.time.Duration;

/**
 * Stores Duration as BIGINT (seconds). Used for dialect-specific resolution test.
 */
public class DurationSecondsTypeHandler implements SqlTypeHandler<Duration> {

    @Override
    public int sqlType() {
        return Types.BIGINT;
    }

    @Override
    public void setStatementParameter(Connection connection, PreparedStatement ps,
                                      int parameterIndex, Duration value) throws SQLException {
        ps.setLong(parameterIndex, value.getSeconds());
    }

    @Override
    public Duration readCell(Connection connection, ResultSet resultSet,
                             int columnIndex, Class<? extends Duration> clazz) throws SQLException {
        long seconds = resultSet.getLong(columnIndex);
        return resultSet.wasNull() ? null : Duration.ofSeconds(seconds);
    }

    @Override
    public Duration readJsonValue(Object rawJsonValue, Class<? extends Duration> clazz) {
        return rawJsonValue == null ? null : Duration.ofSeconds(((Number) rawJsonValue).longValue());
    }
}
