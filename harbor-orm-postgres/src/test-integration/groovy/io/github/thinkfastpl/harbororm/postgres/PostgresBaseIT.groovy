// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres

import groovy.util.logging.Slf4j
import io.github.thinkfastpl.harbororm.api.HarborSession
import io.github.thinkfastpl.harbororm.core.HarborSessionFactory
import io.github.thinkfastpl.harbororm.core.sql.SqlConnectionAccessor
import io.github.thinkfastpl.harbororm.postgres.dialect.PostgreSqlRdbmsSupport
import org.testcontainers.postgresql.PostgreSQLContainer
import spock.lang.Specification

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

@Slf4j
abstract class PostgresBaseIT extends Specification {

    @SuppressWarnings("rawtypes")
    private static final PostgreSQLContainer postgres
    private static final Connection connection
    protected static final HarborSession session

    static {
        final long startTime = System.currentTimeMillis();

        postgres = new PostgreSQLContainer("postgres:18-alpine")
                .withDatabaseName("postgres-dialect-test")
                .withUsername("postgres-dialect-test")
                .withPassword("postgres-dialect-test")

        postgres.start()

        Properties props = new Properties()
        props.setProperty("user", postgres.getUsername())
        props.setProperty("password", postgres.getPassword())
        connection = DriverManager.getConnection(postgres.getJdbcUrl(), props)
        connection.setAutoCommit(false);
        session = HarborSessionFactory.builder()
                .connectionAccessor(SqlConnectionAccessor.of(connection))
                .rdbmsSupport(new PostgreSqlRdbmsSupport())
                .build()

        log.info("Session created in %d ms".formatted(System.currentTimeMillis() - startTime))
    }

    void loadScript(String scriptName) {
        ScriptLoader.load(connection, scriptName)
        connection.commit();
    }

    void dropAllObjects() {
        // a test may have aborted the transaction with a server-side error; DDL below needs a clean one
        connection.rollback()
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("drop schema public cascade")
            statement.executeUpdate("create schema public")
            statement.executeUpdate("DELETE FROM pg_largeobject_metadata")
            connection.commit()
        }
    }

    void cleanup() {
        connection.rollback()
    }

    protected long countPgLargeObjects() {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM pg_largeobject_metadata")) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        }
    }
}
