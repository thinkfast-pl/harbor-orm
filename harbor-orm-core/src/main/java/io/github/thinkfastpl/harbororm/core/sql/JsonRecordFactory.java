// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import com.github.openjson.JSONArray;
import io.github.thinkfastpl.harbororm.api.converter.AttributeConverterSupplier;
import io.github.thinkfastpl.harbororm.api.converter.JsonSerializer;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import io.github.thinkfastpl.harbororm.core.utils.HarborStringUtils;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RequiredArgsConstructor
class JsonRecordFactory {
    private final String dialectName;
    private final AttributeConverterSupplier attributeConverterSupplier;
    private final JsonSerializer jsonSerializer;

    Record[] ofJson(List<Expression<?>> expressions, String jsonArrayString) throws SQLException {
        if (jsonArrayString == null) {
            return null;
        }

        final Map<Expression<?>, Integer> expressionsColumnsMap = ExpressionsBasedRecordFactory.createExpressionsColumnsMap(expressions);
        final Map<String, Integer> labelColumnMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 1; i <= expressions.size(); i++) {
            final String alias = expressions.get(i - 1).getAlias();
            if (HarborStringUtils.isNotBlank(alias)) {
                labelColumnMap.put(alias, i);
            }
        }

        final JSONArray jsonArray = new JSONArray(jsonArrayString);
        final Record[] records = new Record[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONArray row = jsonArray.getJSONArray(i);
            final CellValueReader cellValueReader =
                    new CellValueReader(dialectName, attributeConverterSupplier, jsonSerializer, new JsonCellSource(row));

            final Map<Integer, Object> valuesMap = new HashMap<>();
            int columnIndex = 0;
            for (Expression<?> expression : expressions) {
                columnIndex++;
                valuesMap.put(columnIndex, cellValueReader.read(expression, columnIndex));
            }

            records[i] = new ExpressionsBasedRecord(valuesMap, labelColumnMap, expressionsColumnsMap);
        }

        return records;
    }
}
