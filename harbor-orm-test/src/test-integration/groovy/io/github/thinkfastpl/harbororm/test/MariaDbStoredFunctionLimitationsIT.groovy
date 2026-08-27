// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.test.domain.GetBasicsAboveFunction
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

/**
 * Verifies that MariaDB throws UnsupportedOperationException for unsupported
 * trailing clauses on @StoredFunction calls. The CALL emulation supports only
 * the bare {@code session.select(fn.call(args)).fetch*()} shape; any clause
 * that would require wrapping the CALL in a SELECT is rejected up front.
 */
class MariaDbStoredFunctionLimitationsIT extends AbstractHarborIT {

    def "session.select(fn.call(...)).where() throws on MariaDB"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            session.select(fn.call(5))
                    .where(fn.numero.gt(0))
                    .fetchAll()

        then:
            UnsupportedOperationException ex = thrown()
            ex.message.toLowerCase().contains("where")

        where:
            session << getSessionsExcept(DbType.H2, DbType.POSTGRES, DbType.MYSQL)
    }

    def "session.select(fn.call(...)).orderBy() throws on MariaDB"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            session.select(fn.call(5))
                    .orderBy(fn.numero.asc())
                    .fetchAll()

        then:
            UnsupportedOperationException ex = thrown()
            ex.message.toLowerCase().contains("order by")

        where:
            session << getSessionsExcept(DbType.H2, DbType.POSTGRES, DbType.MYSQL)
    }

    def "session.select(fn.call(...)).limit() throws on MariaDB"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            session.select(fn.call(5))
                    .limit(1)
                    .fetchAll()

        then:
            UnsupportedOperationException ex = thrown()
            ex.message.toLowerCase().contains("limit")

        where:
            session << getSessionsExcept(DbType.H2, DbType.POSTGRES, DbType.MYSQL)
    }

    def "session.select(fn.call(...)).offset() throws on MariaDB"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            session.select(fn.call(5))
                    .offset(1)
                    .fetchAll()

        then:
            UnsupportedOperationException ex = thrown()
            ex.message.toLowerCase().contains("offset")

        where:
            session << getSessionsExcept(DbType.H2, DbType.POSTGRES, DbType.MYSQL)
    }

    def "session.select(fn.call(...)).distinct() throws on MariaDB"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            session.select(fn.call(5))
                    .distinct()
                    .fetchAll()

        then:
            UnsupportedOperationException ex = thrown()
            ex.message.toLowerCase().contains("distinct")

        where:
            session << getSessionsExcept(DbType.H2, DbType.POSTGRES, DbType.MYSQL)
    }

    def "session.select(fn.call(...)).count() throws on MariaDB"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            session.select(fn.call(5)).count()

        then:
            thrown(UnsupportedOperationException)

        where:
            session << getSessionsExcept(DbType.H2, DbType.POSTGRES, DbType.MYSQL)
    }

    def "session.select(fn.call(...)).exists() throws on MariaDB"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            session.select(fn.call(5)).exists()

        then:
            thrown(UnsupportedOperationException)

        where:
            session << getSessionsExcept(DbType.H2, DbType.POSTGRES, DbType.MYSQL)
    }

    def "fn.call(...) used as FROM source throws on MariaDB"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)

            GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn")

        when:
            List<Record> records = session.select(fn.name, fn.numero)
                    .from(fn.call(5))
                    .fetchAll()

        then:
            thrown(UnsupportedOperationException)

        where:
            session << getSessionsExcept(DbType.H2, DbType.POSTGRES, DbType.MYSQL)
    }
}
