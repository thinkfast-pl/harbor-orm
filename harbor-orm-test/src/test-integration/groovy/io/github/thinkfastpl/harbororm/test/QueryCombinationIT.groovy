// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.SelectCombination
import io.github.thinkfastpl.harbororm.api.expression.SelectExpression
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsByteaTable
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

/**
 * Integration tests for SelectExpression.CombinationStep interface.
 *
 * These tests focus on exercising the set operation methods in CombinationStep:
 * - intersect(SelectExpression) - intersection removing duplicates
 * - intersectAll(SelectExpression) - intersection keeping duplicates
 * - except(SelectExpression) - difference removing duplicates
 * - exceptAll(SelectExpression) - difference keeping duplicates
 * - combine(SelectExpression, SelectCombination, boolean all) - generic combine method
 *
 * Test variations include:
 * - Using CompleteStep with asNonFluent()
 * - Chained combinations: query1.union(query2).intersect(query3)
 * - Combination with orderBy, limit, and offset
 */
class QueryCombinationIT extends AbstractHarborIT {

    // ============================================
    // intersect() with SelectExpression
    // ============================================

    def "intersect with SelectExpression removes duplicates"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Alice", 300)

            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Charlie")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .intersect(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .fetchAll()

        then:
            names.size() == 1
            names[0] == "Alice"

        where:
            session << allSessions
    }

    def "intersect with CompleteStep using asNonFluent"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Charlie")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            SelectExpression.CompleteStep<String> subquery = DSL.select(basicsBytea.name)
                    .from(basicsBytea)

            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .intersect(subquery)
                    .fetchAll()

        then:
            names.size() == 1
            names[0] == "Alice"

        where:
            session << allSessions
    }

    def "intersect with boolean all parameter"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)

            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Alice")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "intersect with all=false removes duplicates"
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .intersect(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea),
                            false
                    )
                    .fetchAll()

        then:
            names.size() == 1
            names[0] == "Alice"

        where:
            session << allSessions
    }

    // ============================================
    // intersectAll() tests
    // ============================================

    def "intersectAll with SelectExpression keeps duplicates"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Bob", 300)

            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Alice")
            fixtures.addBasicBytea(3L, "Alice")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .intersectAll(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            // basics has 2 Alice, basicsBytea has 3 Alice
            // INTERSECT ALL returns min(2, 3) = 2 Alice values
            names.size() == 2
            names.every { it == "Alice" }

        where:
            // H2 does not support INTERSECT ALL
            session << getSessionsExcept(DbType.H2)
    }

    def "intersectAll with CompleteStep using asNonFluent"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Bob", 300)

            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Alice")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            SelectExpression.CompleteStep<String> subquery = DSL.select(basicsBytea.name)
                    .from(basicsBytea)

            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .intersectAll(subquery)
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            names.size() == 2
            names.every { it == "Alice" }

        where:
            session << getSessionsExcept(DbType.H2)
    }

    // ============================================
    // except() tests
    // ============================================

    def "except with SelectExpression removes matching rows"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Diana")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .except(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            // basics: Alice, Bob, Charlie
            // minus basicsBytea: Alice, Diana
            // result: Bob, Charlie
            names.size() == 2
            names[0] == "Bob"
            names[1] == "Charlie"

        where:
            session << allSessions
    }

    def "except with CompleteStep using asNonFluent"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            fixtures.addBasicBytea(1L, "Alice")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            SelectExpression.CompleteStep<String> subquery = DSL.select(basicsBytea.name)
                    .from(basicsBytea)

            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .except(subquery)
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            names.size() == 2
            names[0] == "Bob"
            names[1] == "Charlie"

        where:
            session << allSessions
    }

    def "except with boolean all parameter"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Bob", 300)

            fixtures.addBasicBytea(1L, "Alice")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "except with all=false removes duplicates"
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .except(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea),
                            false
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            // Without ALL, duplicate Alice removed first, then EXCEPT
            names.size() == 1
            names[0] == "Bob"

        where:
            session << allSessions
    }

    // ============================================
    // exceptAll() tests
    // ============================================

    def "exceptAll with SelectExpression keeps remaining duplicates"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Alice", 300)
            fixtures.addBasic(4L, "Bob", 400)

            fixtures.addBasicBytea(1L, "Alice")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .exceptAll(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            // basics: 3 Alice, 1 Bob
            // minus basicsBytea: 1 Alice
            // result: 2 Alice, 1 Bob
            names.size() == 3
            names.count { it == "Alice" } == 2
            names.count { it == "Bob" } == 1

        where:
            // H2 does not support EXCEPT ALL
            session << getSessionsExcept(DbType.H2)
    }

    def "exceptAll with CompleteStep using asNonFluent"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Bob", 300)

            fixtures.addBasicBytea(1L, "Alice")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            SelectExpression.CompleteStep<String> subquery = DSL.select(basicsBytea.name)
                    .from(basicsBytea)

            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .exceptAll(subquery)
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            names.size() == 2
            names.count { it == "Alice" } == 1
            names.count { it == "Bob" } == 1

        where:
            session << getSessionsExcept(DbType.H2)
    }

    // ============================================
    // combine() generic method tests
    // ============================================

    def "combine with SelectCombination.INTERSECT and all=false"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Charlie")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .combine(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea),
                            SelectCombination.INTERSECT,
                            false
                    )
                    .fetchAll()

        then:
            names.size() == 1
            names[0] == "Alice"

        where:
            session << allSessions
    }

    def "combine with SelectCombination.EXCEPT and all=false"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            fixtures.addBasicBytea(1L, "Alice")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .combine(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea),
                            SelectCombination.EXCEPT,
                            false
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            names.size() == 2
            names[0] == "Bob"
            names[1] == "Charlie"

        where:
            session << allSessions
    }

    def "combine with SelectCombination.UNION and all=true"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Charlie")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .combine(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea),
                            SelectCombination.UNION,
                            true
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            // UNION ALL keeps all rows including duplicate Alice
            names.size() == 4
            names[0] == "Alice"
            names[1] == "Alice"
            names[2] == "Bob"
            names[3] == "Charlie"

        where:
            session << allSessions
    }

    def "combine with CompleteStep parameter"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            fixtures.addBasicBytea(1L, "Alice")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            SelectExpression.CompleteStep<String> subquery = DSL.select(basicsBytea.name)
                    .from(basicsBytea)

            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .combine(subquery, SelectCombination.INTERSECT, false)
                    .fetchAll()

        then:
            names.size() == 1
            names[0] == "Alice"

        where:
            session << allSessions
    }

    // ============================================
    // Chained combinations tests
    // ============================================

    def "chained union then intersect"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            // Set A: Alice, Bob
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            // Set B: Alice, Charlie
            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Charlie")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "A UNION B INTERSECT A"
            // (A UNION B) = Alice, Bob, Charlie
            // Then INTERSECT with A = Alice, Bob
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .union(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .intersect(
                            DSL.select(basics.name)
                                    .from(basics)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            names.size() == 2
            names[0] == "Alice"
            names[1] == "Bob"

        where:
            session << allSessions
    }

    def "chained union then except"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            // Set A: Alice, Bob
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            // Set B: Charlie, Diana
            fixtures.addBasicBytea(1L, "Charlie")
            fixtures.addBasicBytea(2L, "Diana")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "A UNION B EXCEPT A"
            // (A UNION B) = Alice, Bob, Charlie, Diana
            // Then EXCEPT A = Charlie, Diana
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .union(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .except(
                            DSL.select(basics.name)
                                    .from(basics)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            names.size() == 2
            names[0] == "Charlie"
            names[1] == "Diana"

        where:
            session << allSessions
    }

    def "chained intersect then union"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            // Set A: Alice, Bob, Charlie
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            // Set B: Alice, Diana
            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Diana")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "A INTERSECT B UNION B"
            // (A INTERSECT B) = Alice
            // Then UNION B = Alice, Diana
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .intersect(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .union(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            names.size() == 2
            names[0] == "Alice"
            names[1] == "Diana"

        where:
            session << allSessions
    }

    def "chained except then union"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            // Set A: Alice, Bob, Charlie
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            // Set B: Alice
            fixtures.addBasicBytea(1L, "Alice")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "A EXCEPT B UNION B"
            // (A EXCEPT B) = Bob, Charlie
            // Then UNION B = Alice, Bob, Charlie
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .except(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .union(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            names.size() == 3
            names[0] == "Alice"
            names[1] == "Bob"
            names[2] == "Charlie"

        where:
            session << allSessions
    }

    def "triple chained combinations"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            // Set A: Alice, Bob
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            // Set B: Alice, Charlie
            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Charlie")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "A UNION B INTERSECT A UNION B"
            // (A UNION B) = Alice, Bob, Charlie
            // INTERSECT A = Alice, Bob
            // UNION B = Alice, Bob, Charlie
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .union(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .intersect(
                            DSL.select(basics.name)
                                    .from(basics)
                    )
                    .union(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            names.size() == 3
            names[0] == "Alice"
            names[1] == "Bob"
            names[2] == "Charlie"

        where:
            session << allSessions
    }

    // ============================================
    // Combination with orderBy, limit, offset
    // ============================================

    def "combination with orderBy ascending"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Charlie", 100)
            fixtures.addBasic(2L, "Alice", 200)

            fixtures.addBasicBytea(1L, "Bob")
            fixtures.addBasicBytea(2L, "Diana")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .union(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            names.size() == 4
            names[0] == "Alice"
            names[1] == "Bob"
            names[2] == "Charlie"
            names[3] == "Diana"

        where:
            session << allSessions
    }

    def "combination with orderBy descending"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            fixtures.addBasicBytea(1L, "Charlie")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .union(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.desc(1))
                    .fetchAll()

        then:
            names.size() == 3
            names[0] == "Charlie"
            names[1] == "Bob"
            names[2] == "Alice"

        where:
            session << allSessions
    }

    def "combination with limit"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            fixtures.addBasicBytea(1L, "Diana")
            fixtures.addBasicBytea(2L, "Eve")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .union(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .limit(3)
                    .fetchAll()

        then:
            names.size() == 3
            names[0] == "Alice"
            names[1] == "Bob"
            names[2] == "Charlie"

        where:
            session << allSessions
    }

    def "combination with limit and offset"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            fixtures.addBasicBytea(1L, "Diana")
            fixtures.addBasicBytea(2L, "Eve")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .union(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .limit(2)
                    .offset(2)
                    .fetchAll()

        then:
            // Skip Alice, Bob - take Charlie, Diana
            names.size() == 2
            names[0] == "Charlie"
            names[1] == "Diana"

        where:
            session << allSessions
    }

    def "intersect with orderBy"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Charlie")
            fixtures.addBasicBytea(3L, "Diana")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .intersect(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.desc(1))
                    .fetchAll()

        then:
            // Intersection: Alice, Charlie (ordered descending)
            names.size() == 2
            names[0] == "Charlie"
            names[1] == "Alice"

        where:
            session << allSessions
    }

    def "except with orderBy and limit"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)
            fixtures.addBasic(4L, "Diana", 400)

            fixtures.addBasicBytea(1L, "Alice")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .except(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .limit(2)
                    .fetchAll()

        then:
            // Except: Bob, Charlie, Diana (limit 2)
            names.size() == 2
            names[0] == "Bob"
            names[1] == "Charlie"

        where:
            session << allSessions
    }

    // ============================================
    // Combination with multiple columns
    // ============================================

    def "intersect with multiple columns"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Bob", 100)

            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Bob")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .intersect(
                            DSL.select(basicsBytea.id, basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            // Intersection on (id, name) pairs:
            // basics: (1, Alice), (2, Alice), (3, Bob)
            // basicsBytea: (1, Alice), (2, Bob)
            // Common: (1, Alice)
            records.size() == 1
            records[0].get(basics.id) == 1L
            records[0].get(basics.name) == "Alice"

        where:
            session << allSessions
    }

    def "except with multiple columns"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            fixtures.addBasicBytea(1L, "Alice")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .except(
                            DSL.select(basicsBytea.id, basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            // basics: (1, Alice), (2, Bob), (3, Charlie)
            // minus basicsBytea: (1, Alice)
            // result: (2, Bob), (3, Charlie)
            records.size() == 2
            records[0].get(basics.id) == 2L
            records[0].get(basics.name) == "Bob"
            records[1].get(basics.id) == 3L
            records[1].get(basics.name) == "Charlie"

        where:
            session << allSessions
    }

    // ============================================
    // Edge cases
    // ============================================

    def "intersect with empty result"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            fixtures.addBasicBytea(1L, "Charlie")
            fixtures.addBasicBytea(2L, "Diana")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .intersect(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .fetchAll()

        then:
            // No common elements
            names.isEmpty()

        where:
            session << allSessions
    }

    def "except with empty result when all match"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)

            fixtures.addBasicBytea(1L, "Alice")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .except(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .fetchAll()

        then:
            // All elements removed
            names.isEmpty()

        where:
            session << allSessions
    }

    def "combination with empty first query"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasicBytea(1L, "Alice")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "union with empty first set"
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .union(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .fetchAll()

        then:
            // Empty UNION {Alice} = {Alice}
            names.size() == 1
            names[0] == "Alice"

        where:
            session << allSessions
    }

    def "combination with empty second query"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "union with empty second set"
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .union(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .fetchAll()

        then:
            // {Alice} UNION Empty = {Alice}
            names.size() == 1
            names[0] == "Alice"

        where:
            session << allSessions
    }

    // ============================================
    // Combinations using column by name in orderBy
    // ============================================

    def "combination with orderBy using column name"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Charlie", 100)
            fixtures.addBasic(2L, "Alice", 200)

            fixtures.addBasicBytea(1L, "Bob")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .union(
                            DSL.select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc("name"))
                    .fetchAll()

        then:
            names.size() == 3
            names[0] == "Alice"
            names[1] == "Bob"
            names[2] == "Charlie"

        where:
            session << allSessions
    }
}
