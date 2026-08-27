// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mysql.dialect;

import io.github.thinkfastpl.harbororm.core.sql.CustomSequenceGeneratorHandler;
import io.github.thinkfastpl.harbororm.core.sql.RdbmsSupport;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlDialect;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;
import lombok.NonNull;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * MySQL {@link RdbmsSupport} implementation.
 * <p>
 * Relies on the default JDBC-based LOB handling provided by {@link RdbmsSupport};
 * parameter binding and cell reading are adjusted for Connector/J gaps: no
 * {@code TIMESTAMP_WITH_TIMEZONE} SQL type, no {@link Character} conversion, and
 * no {@link UUID} support (MySQL has no uuid column type — values map to CHAR(36)).
 */
public class MySqlRdbmsSupport implements RdbmsSupport {
    private final CustomSequenceGeneratorHandler customSequenceGeneratorHandler;

    public MySqlRdbmsSupport() {
        this(null);
    }

    public MySqlRdbmsSupport(CustomSequenceGeneratorHandler customSequenceGeneratorHandler) {
        this.customSequenceGeneratorHandler = customSequenceGeneratorHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SqlDialect createDialect() {
        return new MySqlSqlDialect();
    }

    @Override
    public CustomSequenceGeneratorHandler getCustomSequenceGeneratorHandler() {
        return customSequenceGeneratorHandler;
    }

    /**
     * Connector/J refuses {@link Types#TIMESTAMP_WITH_TIMEZONE} as an explicit target type,
     * java-serializes {@link UUID} values into the column (MySQL has no uuid type; the schema
     * uses CHAR(36)), and cannot convert {@link Character}. All three are bound in a form the
     * driver understands; everything else uses the default binding.
     */
    @Override
    public void setStatementParameter(@NonNull Connection connection, @NonNull PreparedStatement ps, int parameterIndex, @NonNull SqlQuery.Param param) throws SQLException {
        final Object value = param.getValue();

        if (value instanceof UUID || value instanceof Character) {
            ps.setString(parameterIndex, value.toString());
            return;
        }

        if (param.getSqlType() == Types.TIMESTAMP_WITH_TIMEZONE) {
            if (value == null) {
                ps.setNull(parameterIndex, Types.TIMESTAMP);
            } else {
                ps.setObject(parameterIndex, value);
            }
            return;
        }

        RdbmsSupport.super.setStatementParameter(connection, ps, parameterIndex, param);
    }

    /**
     * Connector/J cannot produce {@link UUID} from a CHAR(36) column; read the string form
     * and parse it. Its raw {@code getObject()} also returns {@link java.time.LocalDateTime}
     * for DATETIME columns (the MariaDB driver returns {@link Timestamp}), which the default
     * conversion cannot turn into a requested {@link Timestamp} — read it as one directly.
     */
    @Override
    public <T> T readCell(@NonNull Connection connection, @NonNull ResultSet resultSet, int columnIndex, @NonNull Class<? extends T> clazz) throws SQLException {
        if (clazz == UUID.class) {
            final String value = resultSet.getString(columnIndex);
            @SuppressWarnings("unchecked") final T uuid = value != null ? (T) UUID.fromString(value) : null;
            return uuid;
        }

        if (clazz == Timestamp.class) {
            @SuppressWarnings("unchecked") final T timestamp = (T) resultSet.getTimestamp(columnIndex);
            return timestamp;
        }

        if (clazz == OffsetDateTime.class) {
            @SuppressWarnings("unchecked") final T offsetDateTime = (T) resultSet.getObject(columnIndex, OffsetDateTime.class);
            return offsetDateTime;
        }

        return RdbmsSupport.super.readCell(connection, resultSet, columnIndex, clazz);
    }

    /**
     * MySQL Connector/J's {@link java.sql.Statement#getGeneratedKeys()} returns a single-column
     * ResultSet labeled {@code GENERATED_KEY}, regardless of the actual auto-increment column name.
     * The default label-to-index map would only contain that synthetic label, causing lookups by the
     * real column name (e.g. {@code id}) to fail with "Invalid column label". We override the lookup
     * to always resolve to column 1 in the single-column generated-keys case.
     */
    @Override
    public Map<String, Integer> getColumnsLabels(@NonNull Connection connection, @NonNull ResultSet resultSet) throws SQLException {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final int columnCount = metaData.getColumnCount();

        if (columnCount == 1) {
            final String columnLabel = metaData.getColumnLabel(1);
            return new AbstractMap<>() {
                @Override
                public Integer get(Object key) {
                    return 1;
                }

                @Override
                public boolean containsKey(Object key) {
                    return true;
                }

                @Override
                public Set<Entry<String, Integer>> entrySet() {
                    return Set.of(Map.entry(columnLabel, 1));
                }
            };
        }

        return RdbmsSupport.super.getColumnsLabels(connection, resultSet);
    }
}
