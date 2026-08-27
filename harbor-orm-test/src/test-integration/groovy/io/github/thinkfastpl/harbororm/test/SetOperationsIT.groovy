// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsByteaTable
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class SetOperationsIT extends AbstractHarborIT {

    // ========================================
    // UNION operations
    // ========================================

    def "union() removes duplicates"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Bob", 20)
            fixtures.addBasicBytea(1L, "Alice")  // duplicate name
            fixtures.addBasicBytea(2L, "Charlie")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> records = session.select(basics.name)
                    .from(basics)
                    .union(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            records.size() == 3  // Alice, Bob, Charlie (deduplicated)
            records[0] == "Alice"
            records[1] == "Bob"
            records[2] == "Charlie"

        where:
            session << allSessions
    }

    def "unionAll() keeps duplicates"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Bob", 20)
            fixtures.addBasicBytea(1L, "Alice")  // duplicate
            fixtures.addBasicBytea(2L, "Charlie")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> records = session.select(basics.name)
                    .from(basics)
                    .unionAll(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            records.size() == 4  // Alice, Alice, Bob, Charlie (duplicates kept)
            records[0] == "Alice"
            records[1] == "Alice"
            records[2] == "Bob"
            records[3] == "Charlie"

        where:
            session << allSessions
    }

    def "union() with subquery uses DSL.select()"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alpha", 100)
            fixtures.addBasic(2L, "Beta", 200)
            fixtures.addBasicBytea(1L, "Alpha")  // duplicate (same id and name)
            fixtures.addBasicBytea(3L, "Gamma")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<Record> records = session.select(basics.id, basics.name)
                    .from(basics)
                    .union(
                            DSL.select(basicsBytea.id, basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))  // order by id
                    .fetchAll()

        then:
            // (1, Alpha), (2, Beta), (3, Gamma) - (1, Alpha) deduplicated
            records.size() == 3
            records[0].get(basics.id) == 1L
            records[0].get(basics.name) == "Alpha"
            records[1].get(basics.id) == 2L
            records[1].get(basics.name) == "Beta"
            records[2].get(basics.id) == 3L
            records[2].get(basics.name) == "Gamma"

        where:
            session << allSessions
    }

    // ========================================
    // INTERSECT operations
    // ========================================

    def "intersect() returns common rows"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Common", 10)
            fixtures.addBasic(2L, "OnlyBasics", 20)
            fixtures.addBasic(3L, "Common", 30)
            fixtures.addBasicBytea(1L, "Common")
            fixtures.addBasicBytea(2L, "OnlyBytea")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> records = session.select(basics.name)
                    .from(basics)
                    .intersect(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .fetchAll()

        then:
            records.size() == 1  // Only "Common" exists in both
            records[0] == "Common"

        where:
            session << allSessions
    }

    def "intersect() with subquery uses DSL.select()"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Shared", 100)
            fixtures.addBasic(2L, "Unique1", 200)
            fixtures.addBasicBytea(1L, "Shared")
            fixtures.addBasicBytea(2L, "Unique2")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<Record> records = session.select(basics.id, basics.name)
                    .from(basics)
                    .intersect(
                            DSL.select(basicsBytea.id, basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .fetchAll()

        then:
            records.size() == 1  // Only (1, "Shared") is in both
            records[0].get(basics.id) == 1L
            records[0].get(basics.name) == "Shared"

        where:
            session << allSessions
    }

    def "intersectAll() keeps duplicates in intersection"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            // basics has "Dup" twice
            fixtures.addBasic(1L, "Dup", 10)
            fixtures.addBasic(2L, "Dup", 20)
            fixtures.addBasic(3L, "OnlyBasics", 30)
            // bytea has "Dup" twice as well
            fixtures.addBasicBytea(1L, "Dup")
            fixtures.addBasicBytea(2L, "Dup")
            fixtures.addBasicBytea(3L, "OnlyBytea")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> records = session.select(basics.name)
                    .from(basics)
                    .intersectAll(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            // INTERSECT ALL preserves duplicates: min(2, 2) = 2 "Dup" rows
            records.size() == 2
            records[0] == "Dup"
            records[1] == "Dup"

        where:
            // H2 does not support INTERSECT ALL
            session << getSessionsExcept(DbType.H2)
    }

    // ========================================
    // EXCEPT operations
    // ========================================

    def "except() returns rows in first but not second"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Keep1", 10)
            fixtures.addBasic(2L, "Remove", 20)
            fixtures.addBasic(3L, "Keep2", 30)
            fixtures.addBasicBytea(1L, "Remove")
            fixtures.addBasicBytea(2L, "Other")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> records = session.select(basics.name)
                    .from(basics)
                    .except(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            records.size() == 2  // Keep1, Keep2 (Remove is excluded)
            records[0] == "Keep1"
            records[1] == "Keep2"

        where:
            session << allSessions
    }

    def "except() with subquery uses DSL.select()"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)
            fixtures.addBasic(3L, "C", 30)
            fixtures.addBasicBytea(2L, "B")  // This will be excluded

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<Record> records = session.select(basics.id, basics.name)
                    .from(basics)
                    .except(
                            DSL.select(basicsBytea.id, basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            records.size() == 2  // (1, A) and (3, C) - (2, B) excluded
            records[0].get(basics.id) == 1L
            records[0].get(basics.name) == "A"
            records[1].get(basics.id) == 3L
            records[1].get(basics.name) == "C"

        where:
            session << allSessions
    }

    def "exceptAll() preserves duplicates in difference"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            // basics has "Val" three times
            fixtures.addBasic(1L, "Val", 10)
            fixtures.addBasic(2L, "Val", 20)
            fixtures.addBasic(3L, "Val", 30)
            fixtures.addBasic(4L, "Other", 40)
            // bytea has "Val" once
            fixtures.addBasicBytea(1L, "Val")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> records = session.select(basics.name)
                    .from(basics)
                    .exceptAll(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            // EXCEPT ALL: 3 "Val" - 1 "Val" = 2 "Val", plus 1 "Other"
            records.size() == 3
            records[0] == "Other"
            records[1] == "Val"
            records[2] == "Val"

        where:
            // H2 does not support EXCEPT ALL
            session << getSessionsExcept(DbType.H2)
    }

    // ========================================
    // Chained and ORDER BY operations
    // ========================================

    def "chained union and intersect operations"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            // Set up data for chained operations
            // basics: Alice, Bob, Charlie
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Bob", 20)
            fixtures.addBasic(3L, "Charlie", 30)
            // bytea: Bob, David
            fixtures.addBasicBytea(1L, "Bob")
            fixtures.addBasicBytea(2L, "David")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            // (basics UNION bytea) gives: Alice, Bob, Charlie, David
            // Then we chain another operation
            List<String> records = session.select(basics.name)
                    .from(basics)
                    .union(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .union(
                            DSL.select(DSL.constant("Extra"))
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            // Alice, Bob, Charlie, David, Extra
            records.size() == 5
            records[0] == "Alice"
            records[1] == "Bob"
            records[2] == "Charlie"
            records[3] == "David"
            records[4] == "Extra"

        where:
            session << allSessions
    }

    def "set operation with final ORDER BY"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Zebra", 10)
            fixtures.addBasic(2L, "Apple", 20)
            fixtures.addBasicBytea(1L, "Mango")
            fixtures.addBasicBytea(2L, "Banana")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            // Combine two queries and apply ORDER BY at the end
            List<String> records = session.select(basics.name)
                    .from(basics)
                    .unionAll(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))  // ORDER BY applied to combined result
                    .fetchAll()

        then:
            records.size() == 4
            // Alphabetical order
            records[0] == "Apple"
            records[1] == "Banana"
            records[2] == "Mango"
            records[3] == "Zebra"

        when:
            // Test descending order
            List<String> descRecords = session.select(basics.name)
                    .from(basics)
                    .unionAll(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.desc(1))
                    .fetchAll()

        then:
            descRecords.size() == 4
            descRecords[0] == "Zebra"
            descRecords[1] == "Mango"
            descRecords[2] == "Banana"
            descRecords[3] == "Apple"

        where:
            session << allSessions
    }
}
