// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.lob.PortableBlob;
import io.github.thinkfastpl.harbororm.api.lob.PortableClob;
import io.github.thinkfastpl.harbororm.api.sql.SqlTypeHandler;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

/**
 * {@link CellSource} backed by a single inner {@link JSONArray} row of a multiset
 * aggregation. Values arrive in their database representation as JSON scalars; JSON-typed
 * database columns may arrive as nested JSON nodes.
 */
@RequiredArgsConstructor
class JsonCellSource implements CellSource {

    private final JSONArray row;

    @Override
    public Object readRaw(Expression<?> expression, int columnIndex, Class<?> javaType) throws SQLException {
        if (PortableBlob.class.isAssignableFrom(javaType) || PortableClob.class.isAssignableFrom(javaType)) {
            throw new UnsupportedOperationException("BLOB/CLOB values are not supported inside multiset aggregations");
        }
        return ResultSetUtils.getObject(row, columnIndex, javaType);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object readWithTypeHandler(SqlTypeHandler<?> handler, int columnIndex, Class<?> javaType) {
        final int jsonIndex = columnIndex - 1;
        final Object rawJsonValue = row.isNull(jsonIndex) ? null : row.get(jsonIndex);
        return ((SqlTypeHandler) handler).readJsonValue(rawJsonValue, (Class) javaType);
    }

    @Override
    public String readJsonString(int columnIndex) {
        final int jsonIndex = columnIndex - 1;
        if (row.isNull(jsonIndex)) {
            return null;
        }
        final Object value = row.get(jsonIndex);
        if (value instanceof JSONObject || value instanceof JSONArray) {
            return value.toString();
        }
        return row.getString(jsonIndex);
    }

    @Override
    public String readMultisetJson(int columnIndex) {
        throw new UnsupportedOperationException("Nested multiset aggregations are not supported");
    }
}
