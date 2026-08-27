// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.HarborSession;
import io.github.thinkfastpl.harbororm.api.converter.AttributeConverterSupplier;
import io.github.thinkfastpl.harbororm.api.converter.JsonSerializer;
import io.github.thinkfastpl.harbororm.api.lob.PortableLobSupport;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import io.github.thinkfastpl.harbororm.core.converter.DefaultAttributeConverterSupplier;
import io.github.thinkfastpl.harbororm.core.sql.*;
import lombok.NonNull;

import java.util.Objects;

/**
 * Factory for creating {@link HarborSession} instances.
 * <p>
 * Use the static {@link #of(QueryExecutor, PortableLobSupport)} method for direct construction
 * or the {@link #builder()} for step-by-step configuration of connection management,
 * dialect support, attribute converters, and query monitoring.
 *
 * @see HarborSession
 * @see Builder
 */
public class HarborSessionFactory {
    static final int DEFAULT_INSERT_BATCH_SIZE = 50;
    static final int DEFAULT_LAZY_LOAD_BATCH_SIZE = 50;
    static final int DEFAULT_STREAM_FETCH_SIZE = 1000;

    /**
     * Creates a new {@link HarborSession} from pre-built components.
     *
     * @param queryExecutor      the executor responsible for running SQL queries
     * @param portableLobSupport the factory for creating BLOBs and CLOBs
     * @return a new session instance
     */
    public static HarborSession of(
            @NonNull QueryExecutor queryExecutor,
            @NonNull PortableLobSupport portableLobSupport
    ) {
        return new DefaultHarborSession(queryExecutor, portableLobSupport, DEFAULT_INSERT_BATCH_SIZE, DEFAULT_LAZY_LOAD_BATCH_SIZE, DEFAULT_STREAM_FETCH_SIZE);
    }

    /**
     * Returns a new builder for configuring and creating a {@link HarborSession}.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for assembling a {@link HarborSession} with required and optional components.
     * <p>
     * Required: {@link #connectionAccessor(SqlConnectionAccessor)} and {@link #rdbmsSupport(RdbmsSupport)}.
     * Optional: attribute converter supplier, JSON serializer, and query monitor.
     */
    public static class Builder {

        private SqlConnectionAccessor connectionAccessor;
        private RdbmsSupport rdbmsSupport;
        private AttributeConverterSupplier attributeConverterSupplier;
        private JsonSerializer jsonSerializer;
        private SqlQueryMonitor sqlQueryMonitor;
        private int defaultInsertBatchSize = DEFAULT_INSERT_BATCH_SIZE;
        private int defaultLazyLoadBatchSize = DEFAULT_LAZY_LOAD_BATCH_SIZE;
        private int defaultStreamFetchSize = DEFAULT_STREAM_FETCH_SIZE;

        private Builder() {
        }

        /**
         * Sets the connection manager used for acquiring and releasing database connections.
         *
         * @param connectionAccessor the connection manager (required)
         * @return this builder instance
         */
        public Builder connectionAccessor(@NonNull SqlConnectionAccessor connectionAccessor) {
            this.connectionAccessor = connectionAccessor;
            return this;
        }

        /**
         * Sets the database dialect support used for SQL generation and type mapping.
         *
         * @param rdbmsSupport the database dialect support (required)
         * @return this builder instance
         */
        public Builder rdbmsSupport(@NonNull RdbmsSupport rdbmsSupport) {
            this.rdbmsSupport = rdbmsSupport;
            return this;
        }

        /**
         * Sets a custom attribute converter supplier for type conversions.
         * If not set, {@link DefaultAttributeConverterSupplier} is used.
         *
         * @param attributeConverterSupplier the converter supplier (optional)
         * @return this builder instance
         */
        public Builder attributeConverterSupplier(@NonNull AttributeConverterSupplier attributeConverterSupplier) {
            this.attributeConverterSupplier = attributeConverterSupplier;
            return this;
        }

        /**
         * Sets the JSON serializer used for fields annotated with {@code @Json}.
         *
         * @param jsonSerializer the JSON serializer (optional)
         * @return this builder instance
         */
        public Builder jsonSerializer(@NonNull JsonSerializer jsonSerializer) {
            this.jsonSerializer = jsonSerializer;
            return this;
        }

        /**
         * Sets a query monitor for observing SQL query execution.
         *
         * @param sqlQueryMonitor the query monitor (optional)
         * @return this builder instance
         */
        public Builder sqlQueryMonitor(@NonNull SqlQueryMonitor sqlQueryMonitor) {
            this.sqlQueryMonitor = sqlQueryMonitor;
            return this;
        }

        /**
         * Sets the default batch size for {@link HarborSession#insertEntityBatch}.
         * If not set, defaults to {@value HarborSessionFactory#DEFAULT_INSERT_BATCH_SIZE}.
         *
         * @param defaultInsertBatchSize the batch size (must be positive)
         * @return this builder instance
         * @throws IllegalArgumentException if batch size is not positive
         */
        public Builder defaultInsertBatchSize(int defaultInsertBatchSize) {
            if (defaultInsertBatchSize <= 0) {
                throw new IllegalArgumentException("defaultInsertBatchSize must be positive, got: " + defaultInsertBatchSize);
            }
            this.defaultInsertBatchSize = defaultInsertBatchSize;
            return this;
        }

        /**
         * Sets the default batch window size for lazy-loading entity relations and element collections.
         * If not set, defaults to {@value HarborSessionFactory#DEFAULT_LAZY_LOAD_BATCH_SIZE}.
         *
         * @param defaultLazyLoadBatchSize the batch size (must be positive)
         * @return this builder instance
         * @throws IllegalArgumentException if batch size is not positive
         */
        public Builder defaultLazyLoadBatchSize(int defaultLazyLoadBatchSize) {
            if (defaultLazyLoadBatchSize <= 0) {
                throw new IllegalArgumentException("defaultLazyLoadBatchSize must be positive, got: " + defaultLazyLoadBatchSize);
            }
            this.defaultLazyLoadBatchSize = defaultLazyLoadBatchSize;
            return this;
        }

        /**
         * Sets the default fetch size for streaming query results.
         * If not set, defaults to {@value HarborSessionFactory#DEFAULT_STREAM_FETCH_SIZE}.
         *
         * @param defaultStreamFetchSize the fetch size (must be positive)
         * @return this builder instance
         * @throws IllegalArgumentException if fetch size is not positive
         */
        public Builder defaultStreamFetchSize(int defaultStreamFetchSize) {
            if (defaultStreamFetchSize <= 0) {
                throw new IllegalArgumentException("defaultStreamFetchSize must be positive, got: " + defaultStreamFetchSize);
            }
            this.defaultStreamFetchSize = defaultStreamFetchSize;
            return this;
        }

        /**
         * Builds a new {@link HarborSession} from the configured components.
         * Both {@link #connectionAccessor(SqlConnectionAccessor)} and {@link #rdbmsSupport(RdbmsSupport)}
         * must be set before calling this method.
         *
         * @return a new session instance
         * @throws NullPointerException if connectionAccessor or rdbmsSupport is not set
         */
        public HarborSession build() {
            Objects.requireNonNull(connectionAccessor, "connectionAccessor is required");
            Objects.requireNonNull(rdbmsSupport, "rdbmsSupport is required");

            AttributeConverterSupplier converterSupplier = this.attributeConverterSupplier != null
                    ? this.attributeConverterSupplier
                    : new DefaultAttributeConverterSupplier();

            SqlQueryExecutor executor = new SqlQueryExecutor(connectionAccessor, rdbmsSupport, rdbmsSupport.createDialect(), converterSupplier, jsonSerializer, sqlQueryMonitor);

            return new DefaultHarborSession(
                    executor,
                    new SqlPortableLobSupport(connectionAccessor, rdbmsSupport),
                    defaultInsertBatchSize,
                    defaultLazyLoadBatchSize,
                    defaultStreamFetchSize
            );
        }
    }
}
