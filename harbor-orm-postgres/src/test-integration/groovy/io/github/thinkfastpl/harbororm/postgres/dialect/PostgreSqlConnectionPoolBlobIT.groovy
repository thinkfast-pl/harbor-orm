// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import groovy.util.logging.Slf4j
import io.github.thinkfastpl.harbororm.api.HarborSession
import io.github.thinkfastpl.harbororm.core.HarborSessionFactory
import io.github.thinkfastpl.harbororm.core.sql.SqlConnectionAccessor
import io.github.thinkfastpl.harbororm.postgres.ScriptLoader
import io.github.thinkfastpl.harbororm.postgres.repository.oid.BasicsBlobEntity
import io.github.thinkfastpl.harbororm.postgres.repository.oid.QBasicsBlobEntity
import org.testcontainers.postgresql.PostgreSQLContainer
import spock.lang.Specification

import java.sql.Connection
import java.sql.Statement

/**
 * Integration test that verifies BLOB operations work through a connection pool (HikariCP).
 * <p>
 * Reproduces H-6: {@code asPgConnection()} only checks {@code instanceof PgConnection},
 * which fails when the connection is wrapped by a pool proxy.
 */
@Slf4j
class PostgreSqlConnectionPoolBlobIT extends Specification {

    private static final PostgreSQLContainer postgres
    private static final HikariDataSource dataSource
    private static Connection pooledConnection
    private static HarborSession session

    static {
        postgres = new PostgreSQLContainer("postgres:14-alpine")
                .withDatabaseName("pool-blob-test")
                .withUsername("pool-blob-test")
                .withPassword("pool-blob-test")

        postgres.start()

        HikariConfig config = new HikariConfig()
        config.setJdbcUrl(postgres.getJdbcUrl())
        config.setUsername(postgres.getUsername())
        config.setPassword(postgres.getPassword())
        config.setMaximumPoolSize(2)
        config.setAutoCommit(false)

        dataSource = new HikariDataSource(config)
    }

    void setup() {
        pooledConnection = dataSource.getConnection()
        session = HarborSessionFactory.builder()
                .connectionAccessor(SqlConnectionAccessor.of(pooledConnection))
                .rdbmsSupport(new PostgreSqlRdbmsSupport())
                .build()

        ScriptLoader.load(pooledConnection, "basics-blob.sql")
        pooledConnection.commit()
    }

    void cleanup() {
        try (Statement statement = pooledConnection.createStatement()) {
            statement.executeUpdate("drop schema public cascade")
            statement.executeUpdate("create schema public")
            statement.executeUpdate("DELETE FROM pg_largeobject_metadata")
            pooledConnection.commit()
        }
        pooledConnection.close()
    }

    def "insert and read blob through connection pool"() {
        given:
            byte[] data = new byte[]{1, 2, 3}
            QBasicsBlobEntity qBasicsBlobEntity = new QBasicsBlobEntity(null)
            BasicsBlobEntity entity = new BasicsBlobEntity(
                    1L,
                    'Gwen',
                    session.createBlob(new ByteArrayInputStream(data), data.length)
            )

        when:
            session.insertEntity(qBasicsBlobEntity, entity)

        then:
            with(session.selectEntity(qBasicsBlobEntity).fetchSingle()) { e ->
                e.id == 1
                e.name == 'Gwen'
                e.data.isPresent()
                session.readBlobAllBytes(e.data) == data
            }
    }
}
