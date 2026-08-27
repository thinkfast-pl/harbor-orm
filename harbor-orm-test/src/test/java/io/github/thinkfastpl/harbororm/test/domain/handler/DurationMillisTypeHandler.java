// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain.handler;

import io.github.thinkfastpl.harbororm.api.sql.SqlTypeHandler;

import java.sql.*;
import java.time.Duration;

/**
 * Stores Duration as BIGINT (milliseconds).
 */
public class DurationMillisTypeHandler implements SqlTypeHandler<Duration> {

    @Override
    public int sqlType() {
        return Types.BIGINT;
    }

    @Override
    public void setStatementParameter(Connection connection, PreparedStatement ps,
                                      int parameterIndex, Duration value) throws SQLException {
        ps.setLong(parameterIndex, value.toMillis());
    }

    @Override
    public Duration readCell(Connection connection, ResultSet resultSet,
                             int columnIndex, Class<? extends Duration> clazz) throws SQLException {
        long millis = resultSet.getLong(columnIndex);
        return resultSet.wasNull() ? null : Duration.ofMillis(millis);
    }

    @Override
    public Duration readJsonValue(Object rawJsonValue, Class<? extends Duration> clazz) {
        return rawJsonValue == null ? null : Duration.ofMillis(((Number) rawJsonValue).longValue());
    }
}
