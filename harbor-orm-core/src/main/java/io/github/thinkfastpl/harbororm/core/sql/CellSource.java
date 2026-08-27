// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.sql.SqlTypeHandler;

import java.sql.SQLException;

/**
 * Source of raw cell values for {@link CellValueReader}, abstracting over a JDBC result
 * set row and an inner JSON array row of a multiset aggregation.
 */
interface CellSource {

    Object readRaw(Expression<?> expression, int columnIndex, Class<?> javaType) throws SQLException;

    Object readWithTypeHandler(SqlTypeHandler<?> handler, int columnIndex, Class<?> javaType) throws SQLException;

    String readJsonString(int columnIndex) throws SQLException;

    String readMultisetJson(int columnIndex) throws SQLException;
}
