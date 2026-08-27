// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.api.converter.AttributeConverterSupplier;
import io.github.thinkfastpl.harbororm.api.converter.JsonSerializer;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.lob.PortableBlob;
import io.github.thinkfastpl.harbororm.api.lob.PortableClob;
import io.github.thinkfastpl.harbororm.api.query.result.InsertResult;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import io.github.thinkfastpl.harbororm.api.sql.SqlTypeHandler;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlDialect;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;
import lombok.NonNull;

import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("SqlSourceToSinkFlow")
class PreparedStatementUtils {

    static int executeAndReturnModifiedRows(
            @NonNull RdbmsSupport rdbmsSupport,
            @NonNull Connection connection,
            @NonNull SqlQuery query
    ) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query.getSql().toString())) {
            setParameters(rdbmsSupport, connection, preparedStatement, query);

            if (preparedStatement.execute()) {
                throw new IllegalStateException("Database returned some records");
            } else {
                return preparedStatement.getUpdateCount();
            }
        }
    }

    static List<ExpressionsBasedRecord> executeAndReturnRecords(
            @NonNull RdbmsSupport rdbmsSupport,
            @NonNull SqlDialect dialect,
            @NonNull Connection connection,
            @NonNull SqlQuery query,
            @NonNull List<Expression<?>> expressionsToReturn,
            @NonNull AttributeConverterSupplier attributeConverterSupplier,
            JsonSerializer jsonSerializer
    ) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query.getSql().toString())) {
            setParameters(rdbmsSupport, connection, preparedStatement, query);

            if (preparedStatement.execute()) {
                try (ResultSet resultSet = preparedStatement.getResultSet()) {
                    ExpressionsBasedRecordFactory recordFactory = new ExpressionsBasedRecordFactory(connection, rdbmsSupport, dialect, resultSet, expressionsToReturn, attributeConverterSupplier, jsonSerializer);
                    return recordFactory.fetchAllRecords();
                }
            } else {
                throw new IllegalStateException("Database did not return any records");
            }
        }
    }

    static <T> Stream<T> executeAndReturnStream(
            @NonNull RdbmsSupport rdbmsSupport,
            @NonNull SqlDialect dialect,
            @NonNull Connection connection,
            @NonNull SqlQuery query,
            @NonNull List<Expression<?>> expressionsToReturn,
            @NonNull AttributeConverterSupplier attributeConverterSupplier,
            JsonSerializer jsonSerializer,
            int fetchSize,
            @NonNull Function<Record, T> recordMapper
    ) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(query.getSql().toString());
        stmt.setFetchSize(fetchSize);
        PreparedStatementUtils.setParameters(rdbmsSupport, connection, stmt, query);

        ResultSet rs = stmt.executeQuery();
        ExpressionsBasedRecordFactory factory = new ExpressionsBasedRecordFactory(
                connection, rdbmsSupport, dialect, rs, expressionsToReturn, attributeConverterSupplier, jsonSerializer
        );

        Iterator<T> iterator = new RecordIterator<>(factory, recordMapper, fetchSize);

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        ).onClose(() -> {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
        });
    }

    static InsertResult executeAndReturnModifiedRowsWithGeneratedKeys(
            @NonNull RdbmsSupport rdbmsSupport,
            @NonNull Connection connection,
            @NonNull SqlQuery query
    ) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query.getSql().toString(), Statement.RETURN_GENERATED_KEYS)) {
            setParameters(rdbmsSupport, connection, preparedStatement, query);

            if (preparedStatement.execute()) {
                throw new IllegalStateException("Database returned some records");
            } else {
                try (ResultSet generatedKeysResultSet = preparedStatement.getGeneratedKeys()) {
                    return new InsertResult(
                            preparedStatement.getUpdateCount(),
                            castRecords(GeneratedKeysRecord.ofResultSet(rdbmsSupport, connection, generatedKeysResultSet))
                    );
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setParameters(RdbmsSupport rdbmsSupport, Connection connection, PreparedStatement preparedStatement, SqlQuery query) throws SQLException {
        int paramIndex = 1;
        for (SqlQuery.Param param : query.getParams()) {
            if (param.getTypeHandler() != null) {
                if (param.getValue() == null) {
                    preparedStatement.setNull(paramIndex++, param.getSqlType());
                } else {
                    SqlTypeHandler handler = param.getTypeHandler();
                    handler.setStatementParameter(connection, preparedStatement, paramIndex++, param.getValue());
                }
            } else if (param.getValue() instanceof PortableBlob portableBlob) {
                rdbmsSupport.setStatementBlobParameter(connection, preparedStatement, paramIndex++, portableBlob);
            } else if (param.getValue() instanceof PortableClob portableClob) {
                rdbmsSupport.setStatementClobParameter(connection, preparedStatement, paramIndex++, portableClob);
            } else {
                rdbmsSupport.setStatementParameter(connection, preparedStatement, paramIndex++, param);
            }
        }
    }

    static void executeCall(
            @NonNull RdbmsSupport rdbmsSupport,
            @NonNull Connection connection,
            @NonNull SqlQuery query
    ) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(query.getSql().toString())) {
            setParameters(rdbmsSupport, connection, ps, query);
            ps.execute();
        }
    }

    static <T> T executeCallReturning(
            @NonNull RdbmsSupport rdbmsSupport,
            @NonNull Connection connection,
            @NonNull SqlQuery query,
            @NonNull Class<T> returnType
    ) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(query.getSql().toString())) {
            setParameters(rdbmsSupport, connection, ps, query);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return ResultSetUtils.getObject(rs, 1, returnType);
                }
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    static List<io.github.thinkfastpl.harbororm.api.query.result.Record> castRecords(List<? extends Record> source) {
        return (List<Record>) source;
    }
}
