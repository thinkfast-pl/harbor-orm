// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsByteaTable
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

/**
 * Extended integration tests for join operations, focusing on additional join scenarios
 * that complement the existing JoinOperationsIT and FullOuterJoinIT tests.
 *
 * This test class covers:
 * - CROSS JOIN with WHERE filter
 * - CROSS JOIN with empty table
 * - FULL OUTER JOIN with complex ON conditions (PostgreSQL only)
 * - FULL OUTER JOIN returning all rows from both tables including unmatched (PostgreSQL only)
 * - Multiple chained leftJoin operations
 * - Additional innerJoin variations
 *
 * Tests run on both H2 and PostgreSQL databases via allSessions unless specifically
 * marked for PostgreSQL only (e.g., FULL OUTER JOIN which H2 does not support).
 */
class JoinOperationsExtendedIT extends AbstractHarborIT {

    // ==================== CROSS JOIN ====================

    def "crossJoin() produces cartesian product"() {
        given: "two tables each with 2 rows"
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alpha", 10)
            fixtures.addBasic(2L, "Beta", 20)
            fixtures.addBasicBytea(100L, "X")
            fixtures.addBasicBytea(200L, "Y")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "performing cross join"
            List<Record> records = session.select(basics.id, basics.name, basicsBytea.id, basicsBytea.name)
                    .from(basics)
                    .crossJoin(basicsBytea)
                    .orderBy(basics.id.asc(), basicsBytea.id.asc())
                    .fetchAll()

        then: "cartesian product is returned (2 x 2 = 4 rows)"
            records.size() == 4

            // Alpha x X
            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Alpha"
                r.get(basicsBytea.id) == 100L
                r.get(basicsBytea.name) == "X"
            }
            // Alpha x Y
            with(records[1]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Alpha"
                r.get(basicsBytea.id) == 200L
                r.get(basicsBytea.name) == "Y"
            }
            // Beta x X
            with(records[2]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "Beta"
                r.get(basicsBytea.id) == 100L
                r.get(basicsBytea.name) == "X"
            }
            // Beta x Y
            with(records[3]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "Beta"
                r.get(basicsBytea.id) == 200L
                r.get(basicsBytea.name) == "Y"
            }

        where:
            session << allSessions
    }

    def "crossJoin() with WHERE filter"() {
        given: "two tables with rows having some matching names"
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Match", 10)
            fixtures.addBasic(2L, "NoMatch", 20)
            fixtures.addBasicBytea(100L, "Match")
            fixtures.addBasicBytea(200L, "Different")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "performing cross join with WHERE filter to get matching names"
            List<Record> records = session.select(basics.id, basics.name, basicsBytea.id, basicsBytea.name)
                    .from(basics)
                    .crossJoin(basicsBytea)
                    .where(basics.name.eq(basicsBytea.name))
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then: "only rows where names match are returned"
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Match"
                r.get(basicsBytea.id) == 100L
                r.get(basicsBytea.name) == "Match"
            }

        where:
            session << allSessions
    }

    def "crossJoin() with empty table returns empty result"() {
        given: "one table with data and one empty table"
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alpha", 10)
            fixtures.addBasic(2L, "Beta", 20)
            // basicsBytea table is empty

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "performing cross join with empty right table"
            List<Record> records = session.select(basics.id, basics.name, basicsBytea.id, basicsBytea.name)
                    .from(basics)
                    .crossJoin(basicsBytea)
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then: "cartesian product with empty table results in empty set"
            records.isEmpty()

        where:
            session << allSessions
    }

    // ==================== FULL OUTER JOIN (PostgreSQL only) ====================

    def "fullOuterJoin() returns all rows from both tables"() {
        given: "two tables with some matching and some non-matching rows"
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Bob", 20)
            fixtures.addBasicBytea(2L, "Bob")      // Matches Bob by name
            fixtures.addBasicBytea(3L, "Charlie")  // No match in basics

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "performing full outer join on name column"
            List<Record> records = session.select(basics.id, basics.name, basicsBytea.id, basicsBytea.name)
                    .from(basics)
                    .fullOuterJoin(basicsBytea).on(basics.name.eq(basicsBytea.name))
                    .orderBy(DSL.coalesce(basics.id, DSL.constant(9999L)).asc(), DSL.coalesce(basicsBytea.id, DSL.constant(9999L)).asc())
                    .fetchAll()

        then: "all rows from both tables are returned - matched and unmatched"
            records.size() == 3

            // Alice (no match in basicsBytea)
            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Alice"
                r.get(basicsBytea.id) == null
                r.get(basicsBytea.name) == null
            }
            // Bob (matches)
            with(records[1]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "Bob"
                r.get(basicsBytea.id) == 2L
                r.get(basicsBytea.name) == "Bob"
            }
            // Charlie (no match in basics)
            with(records[2]) { r ->
                r.get(basics.id) == null
                r.get(basics.name) == null
                r.get(basicsBytea.id) == 3L
                r.get(basicsBytea.name) == "Charlie"
            }

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "fullOuterJoin() with complex ON condition"() {
        given: "two tables with multiple columns for join conditions"
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alpha", 100)
            fixtures.addBasic(2L, "Beta", 200)
            fixtures.addBasic(3L, "Gamma", 300)
            fixtures.addBasicBytea(1L, "Alpha")  // Matches by id AND name
            fixtures.addBasicBytea(2L, "Delta")  // Matches by id only, not name
            fixtures.addBasicBytea(4L, "Epsilon") // No match

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "performing full outer join with multiple conditions"
            List<Record> records = session.select(basics.id, basics.name, basicsBytea.id, basicsBytea.name)
                    .from(basics)
                    .fullOuterJoin(basicsBytea).on(
                        basics.id.eq(basicsBytea.id)
                                .and(basics.name.eq(basicsBytea.name))
                    )
                    .orderBy(DSL.coalesce(basics.id, DSL.constant(9999L)).asc(), DSL.coalesce(basicsBytea.id, DSL.constant(9999L)).asc())
                    .fetchAll()

        then: "only rows matching BOTH conditions are joined"
            records.size() == 5

            // Alpha (id=1) matches both conditions
            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Alpha"
                r.get(basicsBytea.id) == 1L
                r.get(basicsBytea.name) == "Alpha"
            }
            // Beta (id=2) - no match (name doesn't match)
            with(records[1]) { r ->
                r.get(basics.id) == 2L
                r.get(basics.name) == "Beta"
                r.get(basicsBytea.id) == null
                r.get(basicsBytea.name) == null
            }
            // Gamma (id=3) - no match
            with(records[2]) { r ->
                r.get(basics.id) == 3L
                r.get(basics.name) == "Gamma"
                r.get(basicsBytea.id) == null
                r.get(basicsBytea.name) == null
            }
            // Delta (id=2 in basicsBytea) - no match (name doesn't match)
            with(records[3]) { r ->
                r.get(basics.id) == null
                r.get(basics.name) == null
                r.get(basicsBytea.id) == 2L
                r.get(basicsBytea.name) == "Delta"
            }
            // Epsilon (id=4 in basicsBytea) - no match
            with(records[4]) { r ->
                r.get(basics.id) == null
                r.get(basics.name) == null
                r.get(basicsBytea.id) == 4L
                r.get(basicsBytea.name) == "Epsilon"
            }

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    // ==================== INNER JOIN variations ====================

    def "innerJoin() explicit syntax"() {
        given: "two tables with matching rows"
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Match", 10)
            fixtures.addBasic(2L, "NoMatch", 20)
            fixtures.addBasicBytea(1L, "Match")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "using explicit innerJoin() method"
            List<Record> records = session.select(basics.id, basics.name, basicsBytea.id, basicsBytea.name)
                    .from(basics)
                    .innerJoin(basicsBytea).on(basics.name.eq(basicsBytea.name))
                    .fetchAll()

        then: "only matching rows are returned"
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Match"
                r.get(basicsBytea.id) == 1L
                r.get(basicsBytea.name) == "Match"
            }

        where:
            session << allSessions
    }

    def "join() defaults to INNER JOIN"() {
        given: "two tables with matching rows"
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Match", 10)
            fixtures.addBasic(2L, "NoMatch", 20)
            fixtures.addBasicBytea(1L, "Match")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "using plain join() method (should behave as inner join)"
            List<Record> records = session.select(basics.id, basics.name, basicsBytea.id, basicsBytea.name)
                    .from(basics)
                    .join(basicsBytea).on(basics.name.eq(basicsBytea.name))
                    .fetchAll()

        then: "only matching rows are returned (inner join behavior)"
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Match"
                r.get(basicsBytea.id) == 1L
                r.get(basicsBytea.name) == "Match"
            }

        where:
            session << allSessions
    }

    // ==================== Multiple joins ====================

    def "multiple joins in single query"() {
        given: "three instances of the same table to simulate a self-join chain"
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "First", 10)
            fixtures.addBasic(2L, "Second", 20)
            fixtures.addBasic(3L, "Third", 30)

            BasicsTable b1 = new BasicsTable("b1")
            BasicsTable b2 = new BasicsTable("b2")
            BasicsTable b3 = new BasicsTable("b3")

        when: "chaining multiple leftJoin operations"
            List<Record> records = session.select(
                        b1.id.as("id1"),
                        b1.name.as("name1"),
                        b2.id.as("id2"),
                        b2.name.as("name2"),
                        b3.id.as("id3"),
                        b3.name.as("name3")
                    )
                    .from(b1)
                    .leftJoin(b2).on(b1.id.eq(b2.id.subtract(1)))  // b2.id = b1.id + 1
                    .leftJoin(b3).on(b2.id.eq(b3.id.subtract(1)))  // b3.id = b2.id + 1
                    .orderBy(b1.id.asc())
                    .fetchAll()

        then: "chained joins are applied correctly"
            records.size() == 3

            // First (id=1) -> Second (id=2) -> Third (id=3)
            with(records[0]) { r ->
                r.get(b1.id.as("id1")) == 1L
                r.get(b1.name.as("name1")) == "First"
                r.get(b2.id.as("id2")) == 2L
                r.get(b2.name.as("name2")) == "Second"
                r.get(b3.id.as("id3")) == 3L
                r.get(b3.name.as("name3")) == "Third"
            }

            // Second (id=2) -> Third (id=3) -> NULL
            with(records[1]) { r ->
                r.get(b1.id.as("id1")) == 2L
                r.get(b1.name.as("name1")) == "Second"
                r.get(b2.id.as("id2")) == 3L
                r.get(b2.name.as("name2")) == "Third"
                r.get(b3.id.as("id3")) == null
                r.get(b3.name.as("name3")) == null
            }

            // Third (id=3) -> NULL -> NULL
            with(records[2]) { r ->
                r.get(b1.id.as("id1")) == 3L
                r.get(b1.name.as("name1")) == "Third"
                r.get(b2.id.as("id2")) == null
                r.get(b2.name.as("name2")) == null
                r.get(b3.id.as("id3")) == null
                r.get(b3.name.as("name3")) == null
            }

        where:
            session << allSessions
    }
}
