// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class DistinctSelectIT extends AbstractHarborIT {

    def "select distinct on single column"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            // Setup data with duplicate names
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Alice", 20)
            fixtures.addBasic(3L, "Bob", 10)
            fixtures.addBasic(4L, "Bob", 30)
            fixtures.addBasic(5L, "Charlie", 40)

            BasicsTable basics = new BasicsTable(null)

        when:
            List<String> names = session.selectDistinct(basics.name)
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            names.size() == 3
            names[0] == "Alice"
            names[1] == "Bob"
            names[2] == "Charlie"

        where:
            session << allSessions
    }

    def "select distinct on single column with alias"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Alice", 20)
            fixtures.addBasic(3L, "Bob", 10)

            BasicsTable basics = new BasicsTable("b")

        when:
            List<String> names = session.selectDistinct(basics.name)
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            names.size() == 2
            names[0] == "Alice"
            names[1] == "Bob"

        where:
            session << allSessions
    }

    def "select distinct on multiple columns"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            // Setup data where (name, numero) combinations have duplicates
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Alice", 10)  // duplicate of (Alice, 10)
            fixtures.addBasic(3L, "Alice", 20)
            fixtures.addBasic(4L, "Bob", 10)
            fixtures.addBasic(5L, "Bob", 10)    // duplicate of (Bob, 10)

            BasicsTable basics = new BasicsTable(null)

        when:
            List<Record> records = session.selectDistinct(basics.name, basics.numero)
                    .from(basics)
                    .orderBy(basics.name.asc(), basics.numero.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "Alice"
                r.get(basics.numero) == 10
            }
            with(records[1]) { r ->
                r.get(basics.name) == "Alice"
                r.get(basics.numero) == 20
            }
            with(records[2]) { r ->
                r.get(basics.name) == "Bob"
                r.get(basics.numero) == 10
            }

        where:
            session << allSessions
    }

    def "select distinct with order by"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Charlie", 30)
            fixtures.addBasic(2L, "Alice", 10)
            fixtures.addBasic(3L, "Alice", 20)
            fixtures.addBasic(4L, "Bob", 15)
            fixtures.addBasic(5L, "Bob", 25)

            BasicsTable basics = new BasicsTable(null)

        when:
            List<String> namesAsc = session.selectDistinct(basics.name)
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            namesAsc.size() == 3
            namesAsc[0] == "Alice"
            namesAsc[1] == "Bob"
            namesAsc[2] == "Charlie"

        when:
            List<String> namesDesc = session.selectDistinct(basics.name)
                    .from(basics)
                    .orderBy(basics.name.desc())
                    .fetchAll()

        then:
            namesDesc.size() == 3
            namesDesc[0] == "Charlie"
            namesDesc[1] == "Bob"
            namesDesc[2] == "Alice"

        where:
            session << allSessions
    }

    def "select distinct with where clause"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Alice", 20)
            fixtures.addBasic(3L, "Bob", 15)
            fixtures.addBasic(4L, "Bob", 25)
            fixtures.addBasic(5L, "Charlie", 5)

            BasicsTable basics = new BasicsTable(null)

        when:
            List<String> names = session.selectDistinct(basics.name)
                    .from(basics)
                    .where(basics.numero.ge(15))
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            names.size() == 2
            names[0] == "Alice"
            names[1] == "Bob"

        where:
            session << allSessions
    }

    def "select distinct on integer column"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            // Setup data with duplicate numero values
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Bob", 20)
            fixtures.addBasic(3L, "Charlie", 10)  // duplicate numero
            fixtures.addBasic(4L, "Diana", 30)
            fixtures.addBasic(5L, "Eve", 20)      // duplicate numero

            BasicsTable basics = new BasicsTable(null)

        when:
            List<Integer> numeros = session.selectDistinct(basics.numero)
                    .from(basics)
                    .orderBy(basics.numero.asc())
                    .fetchAll()

        then:
            numeros.size() == 3
            numeros[0] == 10
            numeros[1] == 20
            numeros[2] == 30

        where:
            session << allSessions
    }

    def "select distinct returns all rows when no duplicates"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Bob", 20)
            fixtures.addBasic(3L, "Charlie", 30)

            BasicsTable basics = new BasicsTable(null)

        when:
            List<String> names = session.selectDistinct(basics.name)
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            names.size() == 3
            names[0] == "Alice"
            names[1] == "Bob"
            names[2] == "Charlie"

        where:
            session << allSessions
    }

    def "select distinct with limit"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Alice", 20)
            fixtures.addBasic(3L, "Bob", 10)
            fixtures.addBasic(4L, "Charlie", 30)
            fixtures.addBasic(5L, "Diana", 40)

            BasicsTable basics = new BasicsTable(null)

        when:
            List<String> names = session.selectDistinct(basics.name)
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .limit(2)
                    .fetchAll()

        then:
            names.size() == 2
            names[0] == "Alice"
            names[1] == "Bob"

        where:
            session << allSessions
    }

    def "select distinct with offset and limit"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Alice", 20)
            fixtures.addBasic(3L, "Bob", 10)
            fixtures.addBasic(4L, "Charlie", 30)
            fixtures.addBasic(5L, "Diana", 40)

            BasicsTable basics = new BasicsTable(null)

        when:
            List<String> names = session.selectDistinct(basics.name)
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .limit(2)
                    .offset(1)
                    .fetchAll()

        then:
            names.size() == 2
            names[0] == "Bob"
            names[1] == "Charlie"

        where:
            session << allSessions
    }

    def "count distinct using DSL.countDistinct"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Alice", 20)
            fixtures.addBasic(3L, "Bob", 10)
            fixtures.addBasic(4L, "Bob", 30)
            fixtures.addBasic(5L, "Charlie", 40)

            BasicsTable basics = new BasicsTable(null)

        when:
            Long count = session.select(DSL.countDistinct(basics.name))
                    .from(basics)
                    .fetchSingle()

        then:
            count == 3

        where:
            session << allSessions
    }
}
