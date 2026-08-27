// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb

import groovy.util.logging.Slf4j
import io.github.thinkfastpl.harbororm.api.HarborSession
import io.github.thinkfastpl.harbororm.core.HarborSessionFactory
import io.github.thinkfastpl.harbororm.core.sql.SqlConnectionAccessor
import io.github.thinkfastpl.harbororm.mariadb.dialect.MariaDbRdbmsSupport
import org.testcontainers.mariadb.MariaDBContainer
import spock.lang.Specification

import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

@Slf4j
abstract class MariaDbBaseIT extends Specification {

    @SuppressWarnings("rawtypes")
    private static final MariaDBContainer mariadb
    private static final Connection connection
    protected static final HarborSession session

    static {
        final long startTime = System.currentTimeMillis()

        mariadb = new MariaDBContainer("mariadb:12.3")
                .withDatabaseName("mariadb-dialect-test")
                .withUsername("mariadb-dialect-test")
                .withPassword("mariadb-dialect-test")

        mariadb.start()

        Properties props = new Properties()
        props.setProperty("user", mariadb.getUsername())
        props.setProperty("password", mariadb.getPassword())
        connection = DriverManager.getConnection(mariadb.getJdbcUrl() + "?allowMultiQueries=true&preserveInstants=true", props)
        connection.setAutoCommit(false)
        session = HarborSessionFactory.builder()
                .connectionAccessor(SqlConnectionAccessor.of(connection))
                .rdbmsSupport(new MariaDbRdbmsSupport())
                .build()

        log.info("Session created in %d ms".formatted(System.currentTimeMillis() - startTime))
    }

    void loadScript(String scriptName) {
        ScriptLoader.load(connection, scriptName)
        connection.commit()
    }

    void dropAllObjects() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP DATABASE `mariadb-dialect-test`")
            statement.executeUpdate("CREATE DATABASE `mariadb-dialect-test`")
            statement.executeUpdate("USE `mariadb-dialect-test`")
            connection.commit()
        }
    }

    void cleanup() {
        connection.rollback()
    }
}
