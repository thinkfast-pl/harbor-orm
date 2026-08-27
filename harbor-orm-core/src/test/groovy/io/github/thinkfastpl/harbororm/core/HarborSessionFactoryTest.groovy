// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core


import io.github.thinkfastpl.harbororm.api.lob.PortableLobSupport
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor
import io.github.thinkfastpl.harbororm.core.sql.RdbmsSupport
import io.github.thinkfastpl.harbororm.core.sql.SqlConnectionAccessor
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlDialect
import spock.lang.Specification

class HarborSessionFactoryTest extends Specification {

    def "of() creates session with default settings"() {
        given:
            def executor = Mock(QueryExecutor)
            def lobFactory = Mock(PortableLobSupport)

        when:
            def session = HarborSessionFactory.of(executor, lobFactory)

        then:
            session instanceof DefaultHarborSession
            def s = session as DefaultHarborSession
            s.defaultInsertBatchSize == 50
            s.defaultLazyLoadBatchSize == 50
            s.defaultStreamFetchSize == 1000
    }

    def "builder creates session with defaults when not set"() {
        given:
            def connection = Mock(java.sql.Connection)
            def dialect = Mock(SqlDialect)
            def rdbmsSupport = Mock(RdbmsSupport) { createDialect() >> dialect }

        when:
            def session = HarborSessionFactory.builder()
                    .connectionAccessor(SqlConnectionAccessor.of(connection))
                    .rdbmsSupport(rdbmsSupport)
                    .build()

        then:
            session instanceof DefaultHarborSession
            def s = session as DefaultHarborSession
            s.defaultInsertBatchSize == 50
            s.defaultLazyLoadBatchSize == 50
            s.defaultStreamFetchSize == 1000
    }

    def "builder creates session with custom settings"() {
        given:
            def connection = Mock(java.sql.Connection)
            def dialect = Mock(SqlDialect)
            def rdbmsSupport = Mock(RdbmsSupport) { createDialect() >> dialect }

        when:
            def session = HarborSessionFactory.builder()
                    .connectionAccessor(SqlConnectionAccessor.of(connection))
                    .rdbmsSupport(rdbmsSupport)
                    .defaultInsertBatchSize(100)
                    .defaultLazyLoadBatchSize(25)
                    .defaultStreamFetchSize(500)
                    .build()

        then:
            session instanceof DefaultHarborSession
            def s = session as DefaultHarborSession
            s.defaultInsertBatchSize == 100
            s.defaultLazyLoadBatchSize == 25
            s.defaultStreamFetchSize == 500
    }

    def "builder rejects non-positive insert batch size: #size"() {
        when:
            HarborSessionFactory.builder().defaultInsertBatchSize(size)

        then:
            thrown(IllegalArgumentException)

        where:
            size << [0, -1, -100]
    }

    def "builder rejects non-positive lazy-load batch size: #size"() {
        when:
            HarborSessionFactory.builder().defaultLazyLoadBatchSize(size)

        then:
            thrown(IllegalArgumentException)

        where:
            size << [0, -1, -100]
    }

    def "builder rejects non-positive stream fetch size: #size"() {
        when:
            HarborSessionFactory.builder().defaultStreamFetchSize(size)

        then:
            thrown(IllegalArgumentException)

        where:
            size << [0, -1, -100]
    }
}
