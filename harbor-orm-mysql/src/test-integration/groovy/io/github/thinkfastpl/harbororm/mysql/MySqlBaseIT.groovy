// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mysql

import groovy.util.logging.Slf4j
import io.github.thinkfastpl.harbororm.api.HarborSession
import io.github.thinkfastpl.harbororm.core.HarborSessionFactory
import io.github.thinkfastpl.harbororm.core.sql.SqlConnectionAccessor
import io.github.thinkfastpl.harbororm.mysql.dialect.MySqlRdbmsSupport
import org.testcontainers.mysql.MySQLContainer
import spock.lang.Specification

import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

@Slf4j
abstract class MySqlBaseIT extends Specification {

    @SuppressWarnings("rawtypes")
    private static final MySQLContainer mysql
    protected static final Connection connection
    protected static final HarborSession session

    static {
        final long startTime = System.currentTimeMillis()

        mysql = new MySQLContainer("mysql:8.4.11")
                .withDatabaseName("mysql-dialect-test")
                .withUsername("mysql-dialect-test")
                .withPassword("mysql-dialect-test")

        mysql.start()

        Properties props = new Properties()
        props.setProperty("user", mysql.getUsername())
        props.setProperty("password", mysql.getPassword())
        connection = DriverManager.getConnection(mysql.getJdbcUrl() + "?allowMultiQueries=true", props)
        connection.setAutoCommit(false)
        session = HarborSessionFactory.builder()
                .connectionAccessor(SqlConnectionAccessor.of(connection))
                .rdbmsSupport(new MySqlRdbmsSupport())
                .build()

        log.info("Session created in %d ms".formatted(System.currentTimeMillis() - startTime))
    }

    void loadScript(String scriptName) {
        ScriptLoader.load(connection, scriptName)
        connection.commit()
    }

    void dropAllObjects() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP DATABASE `mysql-dialect-test`")
            statement.executeUpdate("CREATE DATABASE `mysql-dialect-test`")
            statement.executeUpdate("USE `mysql-dialect-test`")
            connection.commit()
        }
    }

    void cleanup() {
        connection.rollback()
    }
}
