// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.BasicSummary
import io.github.thinkfastpl.harbororm.test.domain.QBasicSummary
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class ViewSelectIT extends AbstractHarborIT {

    def "select from view returns typed instances"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alpha", 10)
            fixtures.addBasic(2L, "Beta", 20)

            def view = new QBasicSummary(null)

        when:
            List<BasicSummary> results = session.select(view)
                    .orderBy(view.total.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].name == "Alpha"
            results[0].total == 10
            results[1].name == "Beta"
            results[1].total == 20

        where:
            session << allSessions
    }

    def "select from view with where clause filters rows"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alpha", 10)
            fixtures.addBasic(2L, "Beta", 20)
            fixtures.addBasic(3L, "Gamma", 30)

            def view = new QBasicSummary(null)

        when:
            List<BasicSummary> results = session.select(view)
                    .where(view.total.gt(15))
                    .orderBy(view.name.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].name == "Beta"
            results[1].name == "Gamma"

        where:
            session << allSessions
    }

    def "select from view with limit and offset"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 1)
            fixtures.addBasic(2L, "B", 2)
            fixtures.addBasic(3L, "C", 3)

            def view = new QBasicSummary(null)

        when:
            List<BasicSummary> results = session.select(view)
                    .orderBy(view.total.asc())
                    .limit(2)
                    .offset(1)
                    .fetchAll()

        then:
            results.size() == 2
            results[0].name == "B"
            results[1].name == "C"

        where:
            session << allSessions
    }

    def "fetchOne returns Optional for single row"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Only", 42)

            def view = new QBasicSummary(null)

        when:
            Optional<BasicSummary> result = session.select(view).fetchOne()

        then:
            result.isPresent()
            result.get().name == "Only"
            result.get().total == 42

        where:
            session << allSessions
    }

    def "fetchOne returns empty Optional when no rows"() {
        given:
            def view = new QBasicSummary(null)

        when:
            Optional<BasicSummary> result = session.select(view).fetchOne()

        then:
            result.isEmpty()

        where:
            session << allSessions
    }

    def "count returns number of view rows"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 1)
            fixtures.addBasic(2L, "B", 2)

            def view = new QBasicSummary(null)

        when:
            long count = session.select(view).count()

        then:
            count == 2

        where:
            session << allSessions
    }

    def "exists returns true when rows present"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 1)

            def view = new QBasicSummary(null)

        when:
            boolean result = session.select(view).exists()

        then:
            result

        where:
            session << allSessions
    }

    def "exists returns false when no rows"() {
        given:
            def view = new QBasicSummary(null)

        when:
            boolean result = session.select(view).exists()

        then:
            !result

        where:
            session << allSessions
    }
}
