// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.HarborSession
import io.github.thinkfastpl.harbororm.api.metadata.QTableName
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.core.HarborSessionFactory
import io.github.thinkfastpl.harbororm.core.sql.SqlConnectionAccessor
import io.github.thinkfastpl.harbororm.core.sql.SqlQueryMonitor
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlQuery
import io.github.thinkfastpl.harbororm.h2.dialect.H2RdbmsSupport
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

/**
 * Integration tests for {@link SqlQueryMonitor} callback interface.
 * Verifies that beforeQuery and afterQuery hooks are invoked correctly
 * for real SQL operations.
 */
class SqlQueryMonitorIT extends AbstractHarborIT {

    /**
     * Simple capturing monitor that records all beforeQuery and afterQuery invocations.
     */
    private static class CapturingMonitor implements SqlQueryMonitor {

        final List<SqlQuery> beforeQueries = Collections.synchronizedList(new ArrayList<>())
        final List<SqlQuery> afterQueries = Collections.synchronizedList(new ArrayList<>())
        final List<Long> afterTimings = Collections.synchronizedList(new ArrayList<>())
        final List<Exception> afterExceptions = Collections.synchronizedList(new ArrayList<>())

        @Override
        void beforeQuery(SqlQuery query) {
            beforeQueries.add(query)
        }

        @Override
        void afterQuery(SqlQuery query, long executionTimeMs, Exception exception) {
            afterQueries.add(query)
            afterTimings.add(executionTimeMs)
            afterExceptions.add(exception)
        }
    }

    private CapturingMonitor monitor
    private HarborSession monitoredSession

    def setup() {
        monitor = new CapturingMonitor()
        monitoredSession = HarborSessionFactory.builder()
                .connectionAccessor(SqlConnectionAccessor.of(connectionH2))
                .rdbmsSupport(new H2RdbmsSupport())
                .sqlQueryMonitor(monitor)
                .build()
    }

    def "beforeQuery is called before SQL execution"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(monitoredSession)
            fixtures.addBasic(1L, "Monitor Test", 42)

        when:
            monitoredSession.select(basics.allColumns)
                    .from(basics)
                    .fetchAll()

        then: "beforeQuery was called and received a query containing SELECT"
            monitor.beforeQueries.any { it.sql.toString().toUpperCase().contains("SELECT") }
    }

    def "afterQuery is called with timing and null exception on success"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(monitoredSession)
            fixtures.addBasic(1L, "Timing Test", 99)

            // Clear monitor state from the insert
            monitor.afterQueries.clear()
            monitor.afterTimings.clear()
            monitor.afterExceptions.clear()

        when:
            monitoredSession.select(basics.allColumns)
                    .from(basics)
                    .fetchAll()

        then: "afterQuery was called"
            monitor.afterQueries.size() == 1

        and: "execution time is non-negative"
            monitor.afterTimings[0] >= 0

        and: "exception is null on success"
            monitor.afterExceptions[0] == null
    }

    def "afterQuery is called with exception on failure and exception is still rethrown"() {
        given:
            QTableName nonExistentTable = new QTableName("nonexistent_table_xyz", null, null)

        when:
            monitoredSession.delete(nonExistentTable).execute()

        then: "the exception is rethrown to the caller"
            thrown(Exception)

        and: "afterQuery was called with the exception"
            monitor.afterExceptions.any { it != null }

        and: "afterQuery received a query object"
            monitor.afterQueries.any { it.sql.toString().toUpperCase().contains("DELETE") }
    }

    def "monitor receives calls for insert, update, and delete operations"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when: "perform a full CRUD cycle"
            // INSERT
            monitoredSession.insertInto(basics)
                    .set(basics.id, 1L)
                    .set(basics.name, "CRUD Test")
                    .set(basics.numero, 10)
                    .execute()

            // SELECT
            monitoredSession.select(basics.allColumns)
                    .from(basics)
                    .fetchAll()

            // UPDATE
            monitoredSession.update(basics)
                    .set(basics.name, "Updated CRUD")
                    .where(basics.id.eq(1L))
                    .execute()

            // DELETE
            monitoredSession.delete(basics)
                    .where(basics.id.eq(1L))
                    .execute()

        then: "all operation types appear in captured queries"
            List<String> allSql = monitor.beforeQueries.collect { it.sql.toString().toUpperCase() }

            allSql.any { it.contains("INSERT") }
            allSql.any { it.contains("SELECT") }
            allSql.any { it.contains("UPDATE") }
            allSql.any { it.contains("DELETE") }

        and: "beforeQuery and afterQuery are called the same number of times"
            monitor.beforeQueries.size() == monitor.afterQueries.size()

        and: "all afterQuery calls report success"
            monitor.afterExceptions.every { it == null }
    }

    def "session without monitor works normally"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "No Monitor", 77)

        when:
            List<Record> records = session.select(basics.allColumns)
                    .from(basics)
                    .fetchAll()

        then:
            records.size() == 1
            records[0].get(basics.name) == "No Monitor"

        where:
            session << allSessions
    }
}
