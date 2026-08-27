// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.lob.PortableBlob;
import io.github.thinkfastpl.harbororm.api.lob.PortableClob;
import io.github.thinkfastpl.harbororm.api.sql.SqlTypeHandler;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link CellSource} backed by a JDBC {@link ResultSet} positioned on the current row.
 */
@RequiredArgsConstructor
class ResultSetCellSource implements CellSource {

    private final Connection connection;
    private final RdbmsSupport rdbmsSupport;
    private final ResultSet resultSet;

    @Override
    public Object readRaw(Expression<?> expression, int columnIndex, Class<?> javaType) throws SQLException {
        if (PortableBlob.class.isAssignableFrom(javaType)) {
            return rdbmsSupport.readBlobCell(connection, resultSet, columnIndex);
        } else if (PortableClob.class.isAssignableFrom(javaType)) {
            return rdbmsSupport.readClobCell(connection, resultSet, columnIndex);
        } else {
            return rdbmsSupport.readCell(connection, resultSet, columnIndex, javaType);
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object readWithTypeHandler(SqlTypeHandler<?> handler, int columnIndex, Class<?> javaType) throws SQLException {
        return ((SqlTypeHandler) handler).readCell(connection, resultSet, columnIndex, (Class) javaType);
    }

    @Override
    public String readJsonString(int columnIndex) throws SQLException {
        return (String) rdbmsSupport.readCell(connection, resultSet, columnIndex, String.class);
    }

    @Override
    public String readMultisetJson(int columnIndex) throws SQLException {
        return resultSet.getString(columnIndex);
    }
}
