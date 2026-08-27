// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsByteaTable
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

/**
 * Integration tests for subquery support in HarborORM.
 * Tests various subquery patterns: IN and NOT IN.
 *
 * Note: EXISTS and NOT EXISTS subqueries are tested separately as the API
 * generates EXISTS(...) IS TRUE/FALSE which has limited database support.
 */
class SubqueryIT extends AbstractHarborIT {

    // ============================================
    // Subquery in WHERE with IN
    // ============================================

    def "should filter rows using IN subquery"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Diana")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "select rows where name is IN the names from another table"
            List<Record> records = session
                    .select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(basicsBytea.name)
                                            .from(basicsBytea)
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Alice"
                r.get(basics.numero) == 100
            }

        where:
            session << allSessions
    }

    def "should return empty result when IN subquery matches no rows"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            fixtures.addBasicBytea(1L, "Xavier")
            fixtures.addBasicBytea(2L, "Yolanda")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(basicsBytea.name)
                                            .from(basicsBytea)
                            )
                    )
                    .fetchAll()

        then:
            records.isEmpty()

        where:
            session << allSessions
    }

    def "should filter rows using IN subquery with multiple matches"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)
            fixtures.addBasic(4L, "Diana", 400)

            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Charlie")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(basicsBytea.name)
                                            .from(basicsBytea)
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Charlie"

        where:
            session << allSessions
    }

    // ============================================
    // Subquery in WHERE with NOT IN
    // ============================================

    def "should filter rows using NOT IN subquery"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Diana")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "select rows where name is NOT IN the names from another table"
            List<Record> records = session
                    .select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(basicsBytea.name)
                                            .from(basicsBytea)
                            ).not()
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(basics.name) == "Bob"
            records[1].get(basics.name) == "Charlie"

        where:
            session << allSessions
    }

    def "should return all rows when NOT IN subquery returns empty"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            // Empty basicsBytea table (no inserts)

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(basicsBytea.name)
                                            .from(basicsBytea)
                            ).not()
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Bob"

        where:
            session << allSessions
    }

    // ============================================
    // Combined conditions with subqueries
    // ============================================

    def "should combine IN subquery with other conditions"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)
            fixtures.addBasic(4L, "Alice", 400)

            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Bob")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "select rows where name IN subquery AND numero > 150"
            List<Record> records = session
                    .select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(basicsBytea.name)
                                            .from(basicsBytea)
                            ).and(basics.numero.gt(150))
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // Names in subquery: Alice, Bob
            // Alice records: id=1 (100), id=4 (400) - only id=4 has numero > 150
            // Bob records: id=2 (200) - has numero > 150
            records.size() == 2
            records[0].get(basics.name) == "Bob"
            records[0].get(basics.numero) == 200
            records[1].get(basics.name) == "Alice"
            records[1].get(basics.numero) == 400

        where:
            session << allSessions
    }

    def "should combine IN subquery with OR condition"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            fixtures.addBasicBytea(1L, "Alice")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "select rows where name IN subquery OR numero >= 300"
            List<Record> records = session
                    .select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(basicsBytea.name)
                                            .from(basicsBytea)
                            ).or(basics.numero.ge(300))
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // IN matches: Alice (id=1)
            // numero >= 300: Charlie (id=3)
            records.size() == 2
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Charlie"

        where:
            session << allSessions
    }

    // ============================================
    // Subquery with filtered results
    // ============================================

    def "should use IN subquery with WHERE clause in subquery"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Bob")
            fixtures.addBasicBytea(3L, "Charlie")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "select rows where name IN (subquery with WHERE clause)"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(basicsBytea.name)
                                            .from(basicsBytea)
                                            .where(basicsBytea.id.le(2L))
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // Subquery returns names where id <= 2: Alice, Bob
            records.size() == 2
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Bob"

        where:
            session << allSessions
    }

    // ============================================
    // Nested subqueries
    // ============================================

    def "should handle nested IN subqueries"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Bob")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")
            BasicsTable subBasics = new BasicsTable("sb")

        when: "select rows where name IN subquery that references another table"
            // Get names from basicsBytea that also exist in basics with numero > 50
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(basicsBytea.name)
                                            .from(basicsBytea)
                                            .where(
                                                    basicsBytea.name.in(
                                                            DSL.select(subBasics.name)
                                                                    .from(subBasics)
                                                                    .where(subBasics.numero.gt(50))
                                                    )
                                            )
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // subBasics with numero > 50: Alice(100), Bob(200), Charlie(300)
            // basicsBytea names: Alice, Bob (both exist in subBasics with numero > 50)
            // basics with those names: Alice(id=1), Bob(id=2)
            records.size() == 2
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Bob"

        where:
            session << allSessions
    }

    // ============================================
    // Edge cases
    // ============================================

    def "should handle IN subquery with empty result"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            // No data in basicsBytea - subquery returns empty
            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "IN subquery returns no rows"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(basicsBytea.name)
                                            .from(basicsBytea)
                            )
                    )
                    .fetchAll()

        then:
            // When subquery returns no rows, IN returns false for all
            records.isEmpty()

        where:
            session << allSessions
    }

    def "should handle NOT IN subquery with empty result"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            // No data in basicsBytea - subquery returns empty
            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "NOT IN subquery returns no rows"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(basicsBytea.name)
                                            .from(basicsBytea)
                            ).not()
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // When subquery returns no rows, NOT IN returns true for all
            records.size() == 2
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Bob"

        where:
            session << allSessions
    }

    def "should handle IN subquery with single column select"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subBasics = new BasicsTable("sb")

        when: "IN subquery selecting IDs"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subBasics.id)
                                            .from(subBasics)
                                            .where(subBasics.numero.lt(250))
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // subBasics with numero < 250: Alice(100), Bob(200)
            // IDs are 1, 2
            records.size() == 2
            records[0].get(basics.id) == 1L
            records[1].get(basics.id) == 2L

        where:
            session << allSessions
    }

    def "should handle IN subquery with numeric column"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 100)
            fixtures.addBasic(4L, "Diana", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subBasics = new BasicsTable("sb")

        when: "IN subquery on numero column"
            List<Record> records = session
                    .select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .where(
                            basics.numero.in(
                                    DSL.select(subBasics.numero)
                                            .from(subBasics)
                                            .where(subBasics.name.in(["Alice", "Bob"]))
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // subBasics for Alice/Bob: numero 100, 200
            // basics with numero in (100, 200): Alice(100), Bob(200), Charlie(100)
            records.size() == 3
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Bob"
            records[2].get(basics.name) == "Charlie"

        where:
            session << allSessions
    }

    // ============================================
    // Subquery with DISTINCT
    // ============================================

    def "should handle IN subquery with DISTINCT"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            // Add duplicates in basicsBytea
            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Alice")
            fixtures.addBasicBytea(3L, "Bob")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "IN subquery with DISTINCT to avoid duplicates"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.selectDistinct(basicsBytea.name)
                                            .from(basicsBytea)
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // Distinct names in basicsBytea: Alice, Bob
            records.size() == 2
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Bob"

        where:
            session << allSessions
    }

    // ============================================
    // Subquery with ORDER BY and LIMIT (if supported)
    // ============================================

    def "should handle IN subquery with ORDER BY"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            fixtures.addBasicBytea(1L, "Charlie")
            fixtures.addBasicBytea(2L, "Alice")
            fixtures.addBasicBytea(3L, "Bob")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "IN subquery with ORDER BY"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(basicsBytea.name)
                                            .from(basicsBytea)
                                            .orderBy(basicsBytea.name.asc())
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // All names are in basicsBytea, ordered by name doesn't change result
            records.size() == 3
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Bob"
            records[2].get(basics.name) == "Charlie"

        where:
            session << allSessions
    }
}
