// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.api.converter.AttributeConverterSupplier;
import io.github.thinkfastpl.harbororm.api.converter.JsonSerializer;
import io.github.thinkfastpl.harbororm.api.expression.DSL;
import io.github.thinkfastpl.harbororm.api.expression.DefaultSelectExpression;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.query.data.DeleteQueryData;
import io.github.thinkfastpl.harbororm.api.query.data.InsertQueryData;
import io.github.thinkfastpl.harbororm.api.query.data.SelectQueryData;
import io.github.thinkfastpl.harbororm.api.query.data.UpdateQueryData;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import io.github.thinkfastpl.harbororm.api.query.result.InsertResult;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import io.github.thinkfastpl.harbororm.core.converter.DefaultAttributeConverterSupplier;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlDialect;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery;
import io.github.thinkfastpl.harbororm.core.utils.HarborListUtils;
import io.github.thinkfastpl.harbororm.core.utils.HarborSQLTypesUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Core {@link QueryExecutor} implementation that translates query data objects into SQL
 * using a {@link SqlDialect}, executes them via JDBC, and maps results back to {@link Record} instances.
 * <p>
 * Supports SELECT (fetch, stream), INSERT, UPDATE, DELETE, and stored procedure/function calls.
 * All query executions are reported to an optional {@link SqlQueryMonitor} for observability.
 *
 * @see SqlDialect
 * @see SqlConnectionAccessor
 * @see RdbmsSupport
 */
@Slf4j
public class SqlQueryExecutor implements QueryExecutor {

    private static final SqlQueryMonitor NO_OP_MONITOR = new SqlQueryMonitor() {
    };

    @NonNull
    private final SqlConnectionAccessor connectionAccessor;

    @NonNull
    private final RdbmsSupport rdbmsSupport;

    @NonNull
    private final SqlDialect dialect;

    @NonNull
    private final AttributeConverterSupplier attributeConverterSupplier;

    private final JsonSerializer jsonSerializer;

    @NonNull
    private final SqlQueryMonitor monitor;

    public SqlQueryExecutor(
            @NonNull SqlConnectionAccessor connectionAccessor,
            @NonNull RdbmsSupport rdbmsSupport,
            @NonNull SqlDialect dialect,
            AttributeConverterSupplier attributeConverterSupplier,
            JsonSerializer jsonSerializer,
            SqlQueryMonitor monitor
    ) {
        this.connectionAccessor = connectionAccessor;
        this.rdbmsSupport = rdbmsSupport;
        this.dialect = dialect;
        this.attributeConverterSupplier = attributeConverterSupplier != null ? attributeConverterSupplier : new DefaultAttributeConverterSupplier();
        this.jsonSerializer = jsonSerializer;
        this.monitor = monitor != null ? monitor : NO_OP_MONITOR;
        this.dialect.init(this.attributeConverterSupplier, jsonSerializer);
    }

    @Override
    public List<Record> fetchAll(@NonNull SelectQueryData data) {
        final SqlQuery query = dialect.toQuery(data);

        log.info("Fetch all: {}", query);

        monitor.beforeQuery(query);
        long startTime = System.currentTimeMillis();
        try {
            List<Record> result = connectionAccessor.execute(connection -> {
                final List<ExpressionsBasedRecord> records = PreparedStatementUtils.executeAndReturnRecords(rdbmsSupport, dialect, connection, query, data.getSelectExpressions(), attributeConverterSupplier, jsonSerializer);
                return PreparedStatementUtils.castRecords(records);
            });
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, null);
            return result;
        } catch (Exception e) {
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, e);
            throw e;
        }
    }

    @Override
    public List<RecordWithExtra> fetchAll(@NonNull SelectQueryData data, @NonNull List<Expression<?>> extraSelectExpressions) {
        final SelectQueryData extraData = new SelectQueryData(
                data.getCtes(),
                data.isWithRecursive(),
                data.isDistinct(),
                HarborListUtils.merge(data.getSelectExpressions(), extraSelectExpressions),
                data.getFrom(),
                data.getJoins(),
                data.getWhereConditions(),
                data.getGroupByExpressions(),
                data.getHavingConditions(),
                data.getCombinations(),
                data.getOrders(),
                data.getLimit(),
                data.getOffset(),
                data.isForUpdate()
        );

        final SqlQuery query = dialect.toQuery(extraData);

        log.info("Fetch all with extra: {}", query);

        monitor.beforeQuery(query);
        long startTime = System.currentTimeMillis();
        try {
            List<RecordWithExtra> result = connectionAccessor.execute(connection ->
                    PreparedStatementUtils.executeAndReturnRecords(rdbmsSupport, dialect, connection, query, extraData.getSelectExpressions(), attributeConverterSupplier, jsonSerializer).stream()
                            .map(r -> new RecordWithExtra(
                                    r,
                                    r.split(data.getSelectExpressions().size())
                            ))
                            .toList()
            );
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, null);
            return result;
        } catch (Exception e) {
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, e);
            throw e;
        }
    }

    @Override
    public <T> Stream<T> fetchStream(@NonNull SelectQueryData data, int fetchSize, @NonNull Function<Record, T> recordMapper) {
        final SqlQuery query = dialect.toQuery(data);
        log.info("Fetch stream: {}", query);

        monitor.beforeQuery(query);
        long startTime = System.currentTimeMillis();

        return connectionAccessor.execute(connection -> {
            try {
                Stream<T> result = PreparedStatementUtils.executeAndReturnStream(
                        rdbmsSupport,
                        dialect,
                        connection,
                        query,
                        data.getSelectExpressions(),
                        attributeConverterSupplier,
                        jsonSerializer,
                        fetchSize,
                        recordMapper
                );
                return result.onClose(() -> monitor.afterQuery(query, System.currentTimeMillis() - startTime, null));
            } catch (SQLException e) {
                monitor.afterQuery(query, System.currentTimeMillis() - startTime, e);
                throw new RuntimeException(e);
            } catch (Exception e) {
                monitor.afterQuery(query, System.currentTimeMillis() - startTime, e);
                throw e;
            }
        });
    }

    @Override
    public InsertResult insert(@NonNull InsertQueryData data) {
        final SqlQuery query = dialect.toQuery(data);

        log.info("Insert: {}", query);

        monitor.beforeQuery(query);
        long startTime = System.currentTimeMillis();
        try {
            InsertResult result = connectionAccessor.execute(connection -> PreparedStatementUtils.executeAndReturnModifiedRowsWithGeneratedKeys(rdbmsSupport, connection, query));
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, null);
            return result;
        } catch (Exception e) {
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, e);
            throw e;
        }
    }

    @Override
    public List<Record> insertReturning(@NonNull InsertQueryData data) {
        final SqlQuery query = dialect.toQuery(data);

        log.info("Insert: {}", query);

        monitor.beforeQuery(query);
        long startTime = System.currentTimeMillis();
        try {
            List<Record> result = connectionAccessor.execute(connection -> {
                final List<ExpressionsBasedRecord> records = PreparedStatementUtils.executeAndReturnRecords(rdbmsSupport, dialect, connection, query, data.getReturningExpressions(), attributeConverterSupplier, jsonSerializer);
                return PreparedStatementUtils.castRecords(records);
            });
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, null);
            return result;
        } catch (Exception e) {
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, e);
            throw e;
        }
    }

    @Override
    public int update(@NonNull UpdateQueryData data) {
        final SqlQuery query = dialect.toQuery(data);

        log.info("Update: {}", query);

        monitor.beforeQuery(query);
        long startTime = System.currentTimeMillis();
        try {
            int result = connectionAccessor.execute(connection -> PreparedStatementUtils.executeAndReturnModifiedRows(rdbmsSupport, connection, query));
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, null);
            return result;
        } catch (Exception e) {
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, e);
            throw e;
        }
    }

    @Override
    public List<Record> updateReturning(@NonNull UpdateQueryData data) {
        final SqlQuery query = dialect.toQuery(data);

        log.info("Update: {}", query);

        monitor.beforeQuery(query);
        long startTime = System.currentTimeMillis();
        try {
            List<Record> result = connectionAccessor.execute(connection -> {
                final List<ExpressionsBasedRecord> records = PreparedStatementUtils.executeAndReturnRecords(rdbmsSupport, dialect, connection, query, data.getReturningExpressions(), attributeConverterSupplier, jsonSerializer);
                return PreparedStatementUtils.castRecords(records);
            });
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, null);
            return result;
        } catch (Exception e) {
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, e);
            throw e;
        }
    }

    @Override
    public int delete(@NonNull DeleteQueryData data) {
        final SqlQuery query = dialect.toQuery(data);

        log.info("Delete: {}", query);

        monitor.beforeQuery(query);
        long startTime = System.currentTimeMillis();
        try {
            int result = connectionAccessor.execute(connection -> PreparedStatementUtils.executeAndReturnModifiedRows(rdbmsSupport, connection, query));
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, null);
            return result;
        } catch (Exception e) {
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, e);
            throw e;
        }
    }

    @Override
    public List<Record> deleteReturning(@NonNull DeleteQueryData data) {
        final SqlQuery query = dialect.toQuery(data);

        log.info("Delete: {}", query);

        monitor.beforeQuery(query);
        long startTime = System.currentTimeMillis();
        try {
            List<Record> result = connectionAccessor.execute(connection -> {
                final List<ExpressionsBasedRecord> records = PreparedStatementUtils.executeAndReturnRecords(rdbmsSupport, dialect, connection, query, data.getReturningExpressions(), attributeConverterSupplier, jsonSerializer);
                return PreparedStatementUtils.castRecords(records);
            });
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, null);
            return result;
        } catch (Exception e) {
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, e);
            throw e;
        }
    }

    @Override
    public void call(@NonNull String procedureName, @NonNull Object[] params) {
        final SqlQuery query = buildCallQuery(procedureName, params);

        log.info("Call: {}", query);

        monitor.beforeQuery(query);
        long startTime = System.currentTimeMillis();
        try {
            connectionAccessor.execute(connection -> {
                PreparedStatementUtils.executeCall(rdbmsSupport, connection, query);
                return null;
            });
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, null);
        } catch (Exception e) {
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, e);
            throw e;
        }
    }

    @Override
    public <T> T callReturning(@NonNull String functionName, @NonNull Class<T> returnType, @NonNull Object[] params) {
        final SqlQuery query = buildSelectFunctionQuery(functionName, params);

        log.info("Call returning: {}", query);

        monitor.beforeQuery(query);
        long startTime = System.currentTimeMillis();
        try {
            T result = connectionAccessor.execute(connection ->
                    PreparedStatementUtils.executeCallReturning(rdbmsSupport, connection, query, returnType)
            );
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, null);
            return result;
        } catch (Exception e) {
            monitor.afterQuery(query, System.currentTimeMillis() - startTime, e);
            throw e;
        }
    }

    private SqlQuery buildCallQuery(String name, Object[] params) {
        StringBuilder sql = new StringBuilder("CALL ");
        sql.append(dialect.escapeKeyword(name));
        appendParamPlaceholders(sql, params);
        return new SqlQuery(sql, buildParams(params));
    }

    private SqlQuery buildSelectFunctionQuery(String name, Object[] params) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(dialect.escapeKeyword(name));
        appendParamPlaceholders(sql, params);
        return new SqlQuery(sql, buildParams(params));
    }

    private void appendParamPlaceholders(StringBuilder sql, Object[] params) {
        sql.append('(');
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append('?');
        }
        sql.append(')');
    }

    private List<SqlQuery.Param> buildParams(Object[] params) {
        return java.util.Arrays.stream(params)
                .map(p -> new SqlQuery.Param(p, p == null ? java.sql.Types.NULL : HarborSQLTypesUtils.getSqlTypeByClass(p.getClass())))
                .toList();
    }

    @Override
    public void executeOnSingleConnection(@NonNull Consumer<QueryExecutor> singleConnectionQueryExecutorConsumer) {
        singleConnectionQueryExecutorConsumer.accept(this);
    }

    @Override
    public Long nextSequenceValue(@NonNull String sequenceName) {
        CustomSequenceGeneratorHandler customSequenceGeneratorHandler = rdbmsSupport.getCustomSequenceGeneratorHandler();
        if (customSequenceGeneratorHandler != null) {
            return connectionAccessor.execute(connection -> customSequenceGeneratorHandler.nextVal(connection, sequenceName));
        }

        final var query = new DefaultSelectExpression<>(false, record -> record.get(1, Long.class), Long.class);
        query.select(DSL.nextval(sequenceName));
        List<Long> resultAsList = fetchAll(query.toQueryData()).stream().map(r -> query.getRecordMapper().apply(r)).toList();
        if (resultAsList.size() != 1) {
            throw new IllegalStateException("Expected one row when generating new sequence value: " + sequenceName);
        }
        return resultAsList.get(0);
    }
}
