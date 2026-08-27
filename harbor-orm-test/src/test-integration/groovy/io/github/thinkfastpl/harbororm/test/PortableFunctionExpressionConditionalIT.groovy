// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class PortableFunctionExpressionConditionalIT extends AbstractHarborIT {

    // ==================== IIF TESTS ====================

    def "select iif() returns then-branch when condition is true"() {
        when:
            Integer result = session.select(
                    DSL.iif(DSL.constant(1).eq(DSL.constant(1)), DSL.constant(3), DSL.constant(4))
            ).fetchSingle()

        then:
            result == 3

        where:
            session << allSessions
    }

    def "select iif() returns else-branch when condition is false"() {
        when:
            Integer result = session.select(
                    DSL.iif(DSL.constant(1).eq(DSL.constant(2)), DSL.constant(3), DSL.constant(4))
            ).fetchSingle()

        then:
            result == 4

        where:
            session << allSessions
    }

    def "select iif() with string branches"() {
        when:
            String result = session.select(
                    DSL.iif(DSL.constant(10).gt(DSL.constant(5)), DSL.constant("big"), DSL.constant("small"))
            ).fetchSingle()

        then:
            result == "big"

        where:
            session << allSessions
    }

    def "select iif(Condition, Expression, value) returns thenValue expression when condition true"() {
        when:
            Integer result = session.select(
                    DSL.iif(DSL.constant(1).eq(DSL.constant(1)), DSL.constant(3), 4)
            ).fetchSingle()

        then:
            result == 3

        where:
            session << allSessions
    }

    def "select iif(Condition, Expression, value) returns constant elseValue when condition false"() {
        when:
            Integer result = session.select(
                    DSL.iif(DSL.constant(1).eq(DSL.constant(2)), DSL.constant(3), 4)
            ).fetchSingle()

        then:
            result == 4

        where:
            session << allSessions
    }

    def "select iif(Condition, value, Expression) returns constant thenValue when condition true"() {
        when:
            Integer result = session.select(
                    DSL.iif(DSL.constant(1).eq(DSL.constant(1)), 3, DSL.constant(4))
            ).fetchSingle()

        then:
            result == 3

        where:
            session << allSessions
    }

    def "select iif(Condition, value, Expression) returns elseValue expression when condition false"() {
        when:
            Integer result = session.select(
                    DSL.iif(DSL.constant(1).eq(DSL.constant(2)), 3, DSL.constant(4))
            ).fetchSingle()

        then:
            result == 4

        where:
            session << allSessions
    }

    def "select exists() returns true when subquery has rows"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 11)
            BasicsTable basics = new BasicsTable(null)

        when:
            Boolean result = session.select(
                    DSL.exists(DSL.select(basics.id).from(basics))
            ).fetchSingle()

        then:
            result
            result instanceof Boolean

        where:
            session << allSessions
    }

    def "select exists() returns false when subquery has no rows"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            Boolean result = session.select(
                    DSL.exists(DSL.select(basics.id).from(basics).where(basics.id.eq(999L)))
            ).fetchSingle()

        then:
            !result
            result instanceof Boolean

        where:
            session << allSessions
    }

    def "select exists() with filtering subquery"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 11)
            fixtures.addBasic(2L, "B", 22)
            BasicsTable basics = new BasicsTable(null)

        when:
            Boolean matching = session.select(
                    DSL.exists(DSL.select(basics.id).from(basics).where(basics.name.eq("A")))
            ).fetchSingle()

        and:
            Boolean nonMatching = session.select(
                    DSL.exists(DSL.select(basics.id).from(basics).where(basics.name.eq("Z")))
            ).fetchSingle()

        then:
            matching
            !nonMatching

        where:
            session << allSessions
    }

    def "exists() used in WHERE clause of outer query"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 11)
            BasicsTable basics = new BasicsTable(null)
            BasicsTable other = new BasicsTable("o")

        when:
            List<Record> records = session.select(basics.id, basics.name)
                    .from(basics)
                    .where(DSL.exists(DSL.select(other.id).from(other).where(other.numero.gt(10))))
                    .fetchAll()

        then:
            records.size() == 1
            records[0].get(basics.name) == "A"

        where:
            session << allSessions
    }
}
