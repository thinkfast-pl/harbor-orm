// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.dialect;

import io.github.thinkfastpl.harbororm.core.sql.RdbmsSupport;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlDialect;
import lombok.NonNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * MariaDB {@link RdbmsSupport} implementation.
 * <p>
 * Relies entirely on the default JDBC-based LOB handling and parameter binding
 * provided by {@link RdbmsSupport}; only {@link #createDialect()} is overridden
 * to supply the MariaDB-specific SQL dialect.
 */
public class MariaDbRdbmsSupport implements RdbmsSupport {

    /**
     * {@inheritDoc}
     */
    @Override
    public SqlDialect createDialect() {
        return new MariaDbSqlDialect();
    }

    /**
     * MariaDB's {@link java.sql.Statement#getGeneratedKeys()} returns a single-column ResultSet
     * labeled {@code insert_id}, regardless of the actual auto-increment column name. The default
     * label-to-index map would only contain that synthetic label, causing lookups by the real
     * column name (e.g. {@code id}) to fail with "Invalid column label". We override the lookup
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
