// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.api.converter.AttributeConverterSupplier;
import io.github.thinkfastpl.harbororm.api.converter.JsonSerializer;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlDialect;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ExpressionsBasedRecordFactory {
    private final ResultSet resultSet;
    private final Map<String, Integer> labelColumnMap;
    private final Map<Expression<?>, Integer> expressionsColumnsMap;
    private final CellValueReader cellValueReader;

    ExpressionsBasedRecordFactory(
            @NonNull Connection connection,
            @NonNull RdbmsSupport rdbmsSupport,
            @NonNull SqlDialect dialect,
            @NonNull ResultSet resultSet,
            @NonNull List<? extends Expression<?>> expressions,
            @NonNull AttributeConverterSupplier attributeConverterSupplier,
            JsonSerializer jsonSerializer
    ) throws SQLException {
        this.resultSet = resultSet;
        this.labelColumnMap = rdbmsSupport.getColumnsLabels(connection, resultSet);
        this.expressionsColumnsMap = createExpressionsColumnsMap(expressions);
        this.cellValueReader = new CellValueReader(dialect.getName(), attributeConverterSupplier, jsonSerializer,
                new ResultSetCellSource(connection, rdbmsSupport, resultSet));
    }

    @SneakyThrows
    ExpressionsBasedRecord fetchNextRecord() {
        if (!resultSet.next()) {
            return null;
        }

        final Map<Integer, Object> map = new HashMap<>();
        for (Map.Entry<Expression<?>, Integer> entry : expressionsColumnsMap.entrySet()) {
            map.put(entry.getValue(), cellValueReader.read(entry.getKey(), entry.getValue()));
        }
        return new ExpressionsBasedRecord(map, labelColumnMap, expressionsColumnsMap);
    }

    List<ExpressionsBasedRecord> fetchAllRecords() {
        final List<ExpressionsBasedRecord> records = new ArrayList<>();
        ExpressionsBasedRecord r;
        while ((r = fetchNextRecord()) != null) {
            records.add(r);
        }
        return records;
    }

    static Map<Expression<?>, Integer> createExpressionsColumnsMap(List<? extends Expression<?>> expressions) {
        final Map<Expression<?>, Integer> map = new HashMap<>();
        int index = 0;
        for (Expression<?> expression : expressions) {
            map.put(expression, ++index);
        }
        return map;
    }
}
