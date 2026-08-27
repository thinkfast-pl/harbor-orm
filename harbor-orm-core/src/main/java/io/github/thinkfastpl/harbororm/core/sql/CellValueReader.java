// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.api.converter.AttributeConverter;
import io.github.thinkfastpl.harbororm.api.converter.AttributeConverterSupplier;
import io.github.thinkfastpl.harbororm.api.converter.JsonSerializer;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.expression.MultisetAggExpression;
import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.api.metadata.ConverterData;
import io.github.thinkfastpl.harbororm.api.sql.SqlTypeHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Materializes a single cell value for an expression, applying the column's type handler,
 * attribute converter (including built-in enum converters) or JSON deserialization.
 * Shared by the top-level result set path and the multiset JSON path.
 */
@RequiredArgsConstructor
class CellValueReader {

    private final String dialectName;
    @NonNull
    private final AttributeConverterSupplier attributeConverterSupplier;
    private final JsonSerializer jsonSerializer;
    @NonNull
    private final CellSource source;

    @SuppressWarnings({"rawtypes", "unchecked"})
    Object read(Expression<?> expression, int columnIndex) throws SQLException {
        if (expression instanceof MultisetAggExpression multisetAggExpression) {
            final String jsonArray = source.readMultisetJson(columnIndex);
            return new JsonRecordFactory(dialectName, attributeConverterSupplier, jsonSerializer)
                    .ofJson(multisetAggExpression.getExpressions(), jsonArray);
        }

        final ColumnContext columnContext = expression.getColumnContext(dialectName);
        final Class<?> typeHandlerClass = Optional.ofNullable(columnContext).map(ColumnContext::getTypeHandlerClass).orElse(null);
        final ConverterData converterData = Optional.ofNullable(columnContext).map(ColumnContext::getConverterData).orElse(null);

        if (typeHandlerClass != null) {
            final SqlTypeHandler<?> handler = TypeHandlerRegistry.get(typeHandlerClass);
            return source.readWithTypeHandler(handler, columnIndex, expression.getJavaType());
        } else if (converterData != null) {
            final AttributeConverter converter = converterData.getConverterInstance() != null
                    ? converterData.getConverterInstance()
                    : attributeConverterSupplier.supply(converterData.getConverterClass());
            return converter.convertToEntityAttribute(source.readRaw(expression, columnIndex, converterData.getConverterClassDbType()));
        } else if (columnContext != null && columnContext.isJson() && jsonSerializer != null
                && expression.getJavaType() != String.class) {
            final String jsonString = source.readJsonString(columnIndex);
            return jsonString != null ? jsonSerializer.deserialize(jsonString, expression.getJavaType()) : null;
        } else {
            return source.readRaw(expression, columnIndex, expression.getJavaType());
        }
    }
}
