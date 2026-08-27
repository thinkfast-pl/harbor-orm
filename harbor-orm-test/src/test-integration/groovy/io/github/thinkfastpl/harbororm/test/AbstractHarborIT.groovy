// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.thinkfastpl.harbororm.api.HarborSession
import io.github.thinkfastpl.harbororm.api.converter.JsonSerializer
import io.github.thinkfastpl.harbororm.core.HarborSessionFactory
import io.github.thinkfastpl.harbororm.core.sql.SqlConnectionAccessor
import io.github.thinkfastpl.harbororm.h2.dialect.H2RdbmsSupport
import io.github.thinkfastpl.harbororm.mariadb.dialect.MariaDbRdbmsSupport
import io.github.thinkfastpl.harbororm.mysql.dialect.MySqlRdbmsSupport
import io.github.thinkfastpl.harbororm.mysql.dialect.MySqlStoredProcedureSequenceGeneratorHandler
import io.github.thinkfastpl.harbororm.postgres.dialect.PostgreSqlRdbmsSupport
import lombok.NonNull
import org.testcontainers.lifecycle.Startables
import org.testcontainers.mariadb.MariaDBContainer
import org.testcontainers.mysql.MySQLContainer
import org.testcontainers.postgresql.PostgreSQLContainer
import spock.lang.Specification

import java.sql.Connection
import java.sql.DriverManager

abstract class  AbstractHarborIT extends Specification {

    protected static enum DbType {
        H2,
        POSTGRES,
        MARIADB,
        MYSQL,
    }

    private static class TestJsonSerializer implements JsonSerializer {

        private final Gson gson = new GsonBuilder().create()

        @Override
        String serialize(@NonNull Object object) {
            if (object == null) return null
            return gson.toJson(object);
        }

        @Override
        <T> T deserialize(@NonNull String json, @NonNull Class<T> type) {
            if (json == null) return null
            return gson.fromJson(json, type);
        }
    }

    protected static final JsonSerializer testJsonSerializer = new TestJsonSerializer()

    protected static final Connection connectionH2 = DriverManager.getConnection("jdbc:h2:mem:AbstractHarborIT;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DATABASE_TO_UPPER=false;QUERY_CACHE_SIZE=0")
    private static final HarborSession sessionH2 = HarborSessionFactory.builder()
            .connectionAccessor(SqlConnectionAccessor.of(connectionH2))
            .rdbmsSupport(new H2RdbmsSupport())
            .build()
    private static final HarborSession sessionH2Json = HarborSessionFactory.builder()
            .connectionAccessor(SqlConnectionAccessor.of(connectionH2))
            .rdbmsSupport(new H2RdbmsSupport())
            .jsonSerializer(testJsonSerializer)
            .build()

    @SuppressWarnings("rawtypes")
    private static final PostgreSQLContainer containerPostgres
    protected static final Connection connectionPostgres
    private static final HarborSession sessionPostgres
    private static final HarborSession sessionPostgresJson

    @SuppressWarnings("rawtypes")
    private static final MariaDBContainer containerMariadb
    protected static final Connection connectionMariadb
    private static final HarborSession sessionMariadb
    private static final HarborSession sessionMariadbJson

    @SuppressWarnings("rawtypes")
    private static final MySQLContainer containerMysql
    protected static final Connection connectionMysql
    private static final HarborSession sessionMysql
    private static final HarborSession sessionMysqlJson

    protected static List<HarborSession> allSessions
    protected static List<HarborSession> allJsonSessions

    static {
        // H2
        ScriptLoader.load(connectionH2, "domain-h2.sql")
        connectionH2.setAutoCommit(false)

        containerPostgres = new PostgreSQLContainer("postgres:18-alpine")
                .withDatabaseName("postgres-dialect-test")
                .withUsername("postgres-dialect-test")
                .withPassword("postgres-dialect-test")

        containerMariadb = new MariaDBContainer("mariadb:12.3")
                .withDatabaseName("mariadb-dialect-test")
                .withUsername("mariadb-dialect-test")
                .withPassword("mariadb-dialect-test")

        containerMysql = new MySQLContainer("mysql:8.4.11")
                .withDatabaseName("mysql-dialect-test")
                .withUsername("mysql-dialect-test")
                .withPassword("mysql-dialect-test")
                .withUrlParam("allowMultiQueries", "true")
                .withCommand("--log-bin-trust-function-creators=1")

        Startables.deepStart(containerPostgres, containerMariadb, containerMysql).join()

        // PG
        Properties pgProps = new Properties()
        pgProps.setProperty("user", containerPostgres.getUsername())
        pgProps.setProperty("password", containerPostgres.getPassword())

        String pgJdbcUrl = containerPostgres.getJdbcUrl();

        connectionPostgres = DriverManager.getConnection(pgJdbcUrl, pgProps)
        ScriptLoader.load(connectionPostgres, "domain-postgres.sql")
        connectionPostgres.setAutoCommit(false);

        sessionPostgres = HarborSessionFactory.builder()
                .connectionAccessor(SqlConnectionAccessor.of(connectionPostgres))
                .rdbmsSupport(new PostgreSqlRdbmsSupport())
                .build()

        sessionPostgresJson = HarborSessionFactory.builder()
                .connectionAccessor(SqlConnectionAccessor.of(connectionPostgres))
                .rdbmsSupport(new PostgreSqlRdbmsSupport())
                .jsonSerializer(testJsonSerializer)
                .build()

        // MariaDB
        Properties mariaProps = new Properties()
        mariaProps.setProperty("user", containerMariadb.getUsername())
        mariaProps.setProperty("password", containerMariadb.getPassword())

        String mariaJdbcUrl = containerMariadb.getJdbcUrl() + "?allowMultiQueries=true&preserveInstants=true"

        connectionMariadb = DriverManager.getConnection(mariaJdbcUrl, mariaProps)
        ScriptLoader.load(connectionMariadb, "domain-mariadb.sql")
        connectionMariadb.setAutoCommit(false)
//        connectionMariadb.createStatement().withCloseable { stmt ->
//            stmt.execute("SET time_zone = '+00:00'")
//        }

        sessionMariadb = HarborSessionFactory.builder()
                .connectionAccessor(SqlConnectionAccessor.of(connectionMariadb))
                .rdbmsSupport(new MariaDbRdbmsSupport())
                .build()

        sessionMariadbJson = HarborSessionFactory.builder()
                .connectionAccessor(SqlConnectionAccessor.of(connectionMariadb))
                .rdbmsSupport(new MariaDbRdbmsSupport())
                .jsonSerializer(testJsonSerializer)
                .build()

        // MySQL
        Properties mysqlProps = new Properties()
        mysqlProps.setProperty("user", containerMysql.getUsername())
        mysqlProps.setProperty("password", containerMysql.getPassword())

        String mysqlJdbcUrl = containerMysql.getJdbcUrl()

        connectionMysql = DriverManager.getConnection(mysqlJdbcUrl, mysqlProps)
        ScriptLoader.load(connectionMysql, "domain-mysql.sql")
        connectionMysql.setAutoCommit(false)

        sessionMysql = HarborSessionFactory.builder()
                .connectionAccessor(SqlConnectionAccessor.of(connectionMysql))
                .rdbmsSupport(new MySqlRdbmsSupport(new MySqlStoredProcedureSequenceGeneratorHandler("harbor_sequence_nextval")))
                .build()

        sessionMysqlJson = HarborSessionFactory.builder()
                .connectionAccessor(SqlConnectionAccessor.of(connectionMysql))
                .rdbmsSupport(new MySqlRdbmsSupport(new MySqlStoredProcedureSequenceGeneratorHandler("harbor_sequence_nextval")))
                .jsonSerializer(testJsonSerializer)
                .build()

        allSessions = [sessionH2, sessionPostgres, sessionMariadb, sessionMysql]
        allJsonSessions = [sessionH2Json, sessionPostgresJson, sessionMariadbJson, sessionMysqlJson]
    }

    void cleanup() {
        connectionH2.rollback()
        connectionPostgres.rollback()
        connectionMariadb.rollback()
        connectionMysql.rollback()
    }

    protected List<HarborSession> getSessionsExcept(DbType... types) {
        if (types.length == 0) {
            return allSessions
        }

        Set<DbType> typesSet = EnumSet.copyOf(Arrays.asList(types))

        List<HarborSession> result = new ArrayList<>(allSessions.size())

        if (!typesSet.contains(DbType.H2)) {
            result.add(sessionH2)
        }

        if (!typesSet.contains(DbType.POSTGRES)) {
            result.add(sessionPostgres)
        }

        if (!typesSet.contains(DbType.MARIADB)) {
            result.add(sessionMariadb)
        }

        if (!typesSet.contains(DbType.MYSQL)) {
            result.add(sessionMysql)
        }

        return result
    }
}
