// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.test.domain.GetBasicsFlags
import io.github.thinkfastpl.harbororm.test.domain.GetBasicsFlagsFunction
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

/**
 * Integration tests for @Convert annotation support on @StoredFunction result columns.
 *
 * Uses the get_basics_flags(min_numero) function/procedure defined in
 * domain-postgres.sql, domain-mysql.sql and domain-mariadb.sql.
 */
class StoredFunctionConverterIT extends AbstractHarborIT {

    def "should hydrate @Convert column when selecting from stored function"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)

            GetBasicsFlagsFunction fn = new GetBasicsFlagsFunction("fn")

        when:
            List<GetBasicsFlags> results = session.select(fn.call(15)).fetchAll()

        then:
            results.size() == 2
            with(results.sort { it.name }) { sorted ->
                sorted[0].name == "A"
                sorted[0].active == Boolean.FALSE
                sorted[1].name == "B"
                sorted[1].active == Boolean.TRUE
            }

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "should filter stored function rows by @Convert column"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)
            fixtures.addBasic(3L, "C", 30)

            GetBasicsFlagsFunction fn = new GetBasicsFlagsFunction("fn")

        when:
            List<GetBasicsFlags> results = session.select(fn.call(15))
                    .where(fn.active.eq(true))
                    .fetchAll()

        then:
            results.size() == 2
            results*.name.sort() == ["B", "C"]
            results*.active.every { it == Boolean.TRUE }

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }
}
