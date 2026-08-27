// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql

import io.github.thinkfastpl.harbororm.api.query.data.SelectQueryData
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlDialect
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery
import spock.lang.Specification

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData

class SqlQueryExecutorCallTest extends Specification {

    def dialect = Mock(SqlDialect)
    def rdbmsSupport = Mock(RdbmsSupport)
    def connection = Mock(Connection)
    def preparedStatement = Mock(PreparedStatement)
    def resultSet = Mock(ResultSet)
    def resultSetMetaData = Mock(ResultSetMetaData)
    def connectionAccessor = SqlConnectionAccessor.of(connection)
    def monitor = Mock(SqlQueryMonitor)

    SqlQueryExecutor executor
    SqlQueryExecutor executorWithNoOpMonitor

    def setup() {
        connection.prepareStatement(_) >> preparedStatement
        preparedStatement.executeQuery() >> resultSet
        resultSet.getMetaData() >> resultSetMetaData
        resultSetMetaData.getColumnCount() >> 0

        executor = new SqlQueryExecutor(connectionAccessor, rdbmsSupport, dialect,
                new io.github.thinkfastpl.harbororm.core.converter.DefaultAttributeConverterSupplier(), null,
                monitor)
        executorWithNoOpMonitor = new SqlQueryExecutor(connectionAccessor, rdbmsSupport, dialect,
                new io.github.thinkfastpl.harbororm.core.converter.DefaultAttributeConverterSupplier(), null,
                new SqlQueryMonitor() {})
    }

    def "call() escapes procedure name via dialect.escapeKeyword()"() {
        when:
            executorWithNoOpMonitor.call("my_procedure", new Object[0])

        then:
            1 * dialect.escapeKeyword("my_procedure") >> '"my_procedure"'
            1 * connection.prepareStatement({ String sql -> sql.contains('"my_procedure"') }) >> preparedStatement
    }

    def "callReturning() escapes function name via dialect.escapeKeyword()"() {
        when:
            executorWithNoOpMonitor.callReturning("my_function", Integer.class, new Object[]{ 1, 2 })

        then:
            1 * dialect.escapeKeyword("my_function") >> '"my_function"'
            1 * connection.prepareStatement({ String sql ->
                sql.contains('"my_function"') && sql.contains('?, ?')
            }) >> preparedStatement
            resultSet.next() >> true
    }

    def "call() with name containing double quotes escapes them"() {
        when:
            executorWithNoOpMonitor.call('my"proc', new Object[0])

        then:
            1 * dialect.escapeKeyword('my"proc') >> '"my""proc"'
            1 * connection.prepareStatement({ String sql -> sql.contains('"my""proc"') }) >> preparedStatement
    }

    def "callReturning() with SQL injection attempt escapes the name"() {
        given:
            def malicious = "calc'; DROP TABLE users; --"

        when:
            executorWithNoOpMonitor.callReturning(malicious, Integer.class, new Object[]{ 1 })

        then:
            1 * dialect.escapeKeyword(malicious) >> '"calc\'; DROP TABLE users; --"'
            // The malicious string is wrapped in double quotes, not executed as raw SQL
            1 * connection.prepareStatement({ String sql -> sql.startsWith('SELECT "') }) >> preparedStatement
            resultSet.next() >> false
    }

    def "fetchStream() does not call afterQuery until stream is closed"() {
        given:
            def selectData = new SelectQueryData(null, false, false, [], null, null, null, null, null, null, null, null, null, false)
            def sqlQuery = new SqlQuery(new StringBuilder("SELECT 1"), [])
            dialect.toQuery(selectData) >> sqlQuery
            resultSet.next() >> false

        when: "stream is created but not closed"
            def stream = executor.fetchStream(selectData, 100, { r -> r })

        then: "beforeQuery is called but afterQuery is not"
            1 * monitor.beforeQuery(sqlQuery)
            0 * monitor.afterQuery(_, _, _)

        when: "stream is closed"
            stream.close()

        then: "afterQuery is called"
            1 * monitor.afterQuery(sqlQuery, _, null)
    }
}
