// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.AuthorsTable
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.query.EmployeesTable
import io.github.thinkfastpl.harbororm.query.PublicationsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

/**
 * Integration tests for advanced subquery patterns in HarborORM.
 * Tests scalar subqueries, correlated subqueries, EXISTS/NOT EXISTS.
 *
 * Note: Basic IN/NOT IN subqueries are tested in SubqueryIT.
 * Note: Derived tables (subqueries in FROM clause) are not yet supported by the API.
 */
class AdvancedSubqueriesIT extends AbstractHarborIT {

    // ============================================
    // Scalar Subquery in SELECT Clause
    // ============================================

    def "should use scalar subquery in SELECT clause to count related records"() {
        given: "authors with different numbers of publications"
            AuthorsTable authors = new AuthorsTable("a")
            PublicationsTable publications = new PublicationsTable("p")

            // Author 1: 2 publications
            session.insertInto(authors)
                    .set(authors.id, 1L)
                    .set(authors.name, "Alice")
                    .set(authors.email, "alice@example.com")
                    .execute()
            session.insertInto(publications)
                    .set(publications.id, 1L)
                    .set(publications.authorId, 1L)
                    .set(publications.title, "Book A")
                    .set(publications.publicationYear, 2020)
                    .execute()
            session.insertInto(publications)
                    .set(publications.id, 2L)
                    .set(publications.authorId, 1L)
                    .set(publications.title, "Book B")
                    .set(publications.publicationYear, 2021)
                    .execute()

            // Author 2: 1 publication
            session.insertInto(authors)
                    .set(authors.id, 2L)
                    .set(authors.name, "Bob")
                    .set(authors.email, "bob@example.com")
                    .execute()
            session.insertInto(publications)
                    .set(publications.id, 3L)
                    .set(publications.authorId, 2L)
                    .set(publications.title, "Book C")
                    .set(publications.publicationYear, 2022)
                    .execute()

            // Author 3: 0 publications
            session.insertInto(authors)
                    .set(authors.id, 3L)
                    .set(authors.name, "Charlie")
                    .execute()

        when: "selecting authors with publication count via scalar subquery"
            List<Record> records = session
                    .select(
                            authors.id,
                            authors.name,
                            DSL.select(DSL.count())
                                    .from(publications)
                                    .where(publications.authorId.eq(authors.id))
                    )
                    .from(authors)
                    .orderBy(authors.id.asc())
                    .fetchAll()

        then: "each author shows correct publication count"
            records.size() == 3

            with(records[0]) { r ->
                r.get(authors.id) == 1L
                r.get(authors.name) == "Alice"
                r.get(3, Long.class) == 2L  // 2 publications
            }

            with(records[1]) { r ->
                r.get(authors.id) == 2L
                r.get(authors.name) == "Bob"
                r.get(3, Long.class) == 1L  // 1 publication
            }

            with(records[2]) { r ->
                r.get(authors.id) == 3L
                r.get(authors.name) == "Charlie"
                r.get(3, Long.class) == 0L  // 0 publications
            }

        where:
            session << allSessions
    }


    def "should use scalar subquery with aggregate function in SELECT clause"() {
        given: "authors with publications from different years"
            AuthorsTable authors = new AuthorsTable("a")
            PublicationsTable publications = new PublicationsTable("p")

            session.insertInto(authors)
                    .set(authors.id, 1L)
                    .set(authors.name, "Alice")
                    .set(authors.email, "alice@example.com")
                    .execute()
            session.insertInto(publications)
                    .set(publications.id, 1L)
                    .set(publications.authorId, 1L)
                    .set(publications.title, "First Book")
                    .set(publications.publicationYear, 2020)
                    .execute()
            session.insertInto(publications)
                    .set(publications.id, 2L)
                    .set(publications.authorId, 1L)
                    .set(publications.title, "Latest Book")
                    .set(publications.publicationYear, 2023)
                    .execute()

        when: "selecting author with most recent publication year"
            List<Record> records = session
                    .select(
                            authors.name,
                            DSL.select(DSL.max(publications.publicationYear))
                                    .from(publications)
                                    .where(publications.authorId.eq(authors.id))
                    )
                    .from(authors)
                    .fetchAll()

        then: "returns author with maximum publication year from subquery"
            records.size() == 1
            records[0].get(authors.name) == "Alice"
            records[0].get(2, Integer.class) == 2023

        where:
            session << allSessions
    }


    // ============================================
    // Correlated Subquery in WHERE Clause
    // ============================================

    def "should use correlated subquery to filter based on related table"() {
        given: "authors where some have publications after 2021"
            AuthorsTable authors = new AuthorsTable("a")
            PublicationsTable publications = new PublicationsTable("p")

            // Alice: has publication in 2020 and 2022 (should match)
            session.insertInto(authors)
                    .set(authors.id, 1L)
                    .set(authors.name, "Alice")
                    .set(authors.email, "alice@example.com")
                    .execute()
            session.insertInto(publications)
                    .set(publications.id, 1L)
                    .set(publications.authorId, 1L)
                    .set(publications.title, "Old Book")
                    .set(publications.publicationYear, 2020)
                    .execute()
            session.insertInto(publications)
                    .set(publications.id, 2L)
                    .set(publications.authorId, 1L)
                    .set(publications.title, "New Book")
                    .set(publications.publicationYear, 2022)
                    .execute()

            // Bob: has publication only in 2019 (should not match)
            session.insertInto(authors)
                    .set(authors.id, 2L)
                    .set(authors.name, "Bob")
                    .set(authors.email, "bob@example.com")
                    .execute()
            session.insertInto(publications)
                    .set(publications.id, 3L)
                    .set(publications.authorId, 2L)
                    .set(publications.title, "Ancient Book")
                    .set(publications.publicationYear, 2019)
                    .execute()

        when: "selecting authors with at least one publication after 2021"
            List<Record> records = session
                    .select(authors.id, authors.name)
                    .from(authors)
                    .where(
                            DSL.exists(
                                    DSL.select(publications.id)
                                            .from(publications)
                                            .where(
                                                    publications.authorId.eq(authors.id)
                                                            .and(publications.publicationYear.gt(2021))
                                            )
                            )  // EXISTS returns boolean condition
                    )
                    .fetchAll()

        then: "only authors with recent publications are returned"
            records.size() == 1
            records[0].get(authors.name) == "Alice"

        where:
            session << allSessions
    }

    def "should use correlated subquery with comparison to scalar result"() {
        given: "employees with salaries to compare against department average"
            // Data already inserted in schema - using existing employees table
            EmployeesTable emp = new EmployeesTable("e")
            EmployeesTable empDept = new EmployeesTable("ed")

        when: "selecting employees earning above their department average"
            List<Record> records = session
                    .select(emp.name, emp.department, emp.salary)
                    .from(emp)
                    .where(
                            emp.salary.gt(
                                    DSL.select(DSL.avg(empDept.salary))
                                            .from(empDept)
                                            .where(empDept.department.eq(emp.department))
                            )
                    )
                    .orderBy(emp.name.asc())
                    .fetchAll()

        then: "returns employees above department average salary"
            // Engineering avg: (150000 + 75000 + 80000 + 70000) / 4 = 93750 -> CTO (150000)
            // Sales avg: (120000 + 60000 + 65000 + 55000) / 4 = 75000 -> Sales VP (120000)
            // Executive avg: 200000 -> CEO (200000) not greater
            records.size() == 2
            records[0].get(emp.name) == "CTO"
            records[0].get(emp.department) == "Engineering"
            records[1].get(emp.name) == "Sales VP"
            records[1].get(emp.department) == "Sales"

        where:
            session << allSessions
    }


    // ============================================
    // EXISTS with Correlated Subquery
    // ============================================

    def "should use EXISTS to find records with related data"() {
        given: "authors where some have publications and some don't"
            AuthorsTable authors = new AuthorsTable("a")
            PublicationsTable publications = new PublicationsTable("p")

            session.insertInto(authors)
                    .set(authors.id, 1L)
                    .set(authors.name, "Alice")
                    .set(authors.email, "alice@example.com")
                    .execute()
            session.insertInto(publications)
                    .set(publications.id, 1L)
                    .set(publications.authorId, 1L)
                    .set(publications.title, "Book A")
                    .set(publications.publicationYear, 2020)
                    .execute()

            session.insertInto(authors)
                    .set(authors.id, 2L)
                    .set(authors.name, "Bob")
                    .set(authors.email, "bob@example.com")
                    .execute()
            // Bob has no publications

        when: "selecting authors that have at least one publication"
            List<Record> records = session
                    .select(authors.id, authors.name)
                    .from(authors)
                    .where(
                            DSL.exists(
                                    DSL.select(publications.id)
                                            .from(publications)
                                            .where(publications.authorId.eq(authors.id))
                            )
                    )
                    .fetchAll()

        then: "only authors with publications are returned"
            records.size() == 1
            records[0].get(authors.name) == "Alice"

        where:
            session << allSessions
    }

    def "should use EXISTS with multiple conditions in correlated subquery"() {
        given: "authors with publications in specific year ranges"
            AuthorsTable authors = new AuthorsTable("a")
            PublicationsTable publications = new PublicationsTable("p")

            session.insertInto(authors)
                    .set(authors.id, 1L)
                    .set(authors.name, "Alice")
                    .set(authors.email, "alice@example.com")
                    .execute()
            session.insertInto(publications)
                    .set(publications.id, 1L)
                    .set(publications.authorId, 1L)
                    .set(publications.title, "Classic")
                    .set(publications.publicationYear, 2000)
                    .execute()
            session.insertInto(publications)
                    .set(publications.id, 2L)
                    .set(publications.authorId, 1L)
                    .set(publications.title, "Modern")
                    .set(publications.publicationYear, 2020)
                    .execute()

            session.insertInto(authors)
                    .set(authors.id, 2L)
                    .set(authors.name, "Bob")
                    .set(authors.email, "bob@example.com")
                    .execute()
            session.insertInto(publications)
                    .set(publications.id, 3L)
                    .set(publications.authorId, 2L)
                    .set(publications.title, "Old Only")
                    .set(publications.publicationYear, 2005)
                    .execute()

        when: "selecting authors with publications between 2015 and 2025"
            List<Record> records = session
                    .select(authors.name)
                    .from(authors)
                    .where(
                            DSL.exists(
                                    DSL.select(publications.id)
                                            .from(publications)
                                            .where(
                                                    publications.authorId.eq(authors.id)
                                                            .and(publications.publicationYear.ge(2015))
                                                            .and(publications.publicationYear.le(2025))
                                            )
                            )
                    )
                    .fetchAll()

        then: "only authors with publications in the range are returned"
            records.size() == 1
            records[0] == "Alice"

        where:
            session << allSessions
    }

    // ============================================
    // NOT EXISTS with Correlated Subquery
    // ============================================

    def "should use NOT EXISTS to find records without related data"() {
        given: "authors where some have publications and some don't"
            AuthorsTable authors = new AuthorsTable("a")
            PublicationsTable publications = new PublicationsTable("p")

            session.insertInto(authors)
                    .set(authors.id, 1L)
                    .set(authors.name, "Alice")
                    .set(authors.email, "alice@example.com")
                    .execute()
            session.insertInto(publications)
                    .set(publications.id, 1L)
                    .set(publications.authorId, 1L)
                    .set(publications.title, "Book A")
                    .set(publications.publicationYear, 2020)
                    .execute()

            session.insertInto(authors)
                    .set(authors.id, 2L)
                    .set(authors.name, "Bob")
                    .set(authors.email, "bob@example.com")
                    .execute()
            // Bob has no publications

            session.insertInto(authors)
                    .set(authors.id, 3L)
                    .set(authors.name, "Charlie")
                    .execute()
            // Charlie has no publications

        when: "selecting authors that have no publications"
            List<Record> records = session
                    .select(authors.id, authors.name)
                    .from(authors)
                    .where(
                            DSL.exists(
                                    DSL.select(publications.id)
                                            .from(publications)
                                            .where(publications.authorId.eq(authors.id))
                            ).not()
                    )
                    .orderBy(authors.id.asc())
                    .fetchAll()

        then: "only authors without publications are returned"
            records.size() == 2
            records[0].get(authors.name) == "Bob"
            records[1].get(authors.name) == "Charlie"

        where:
            session << allSessions
    }

    def "should use NOT EXISTS with additional filter condition"() {
        given: "authors with different publication patterns"
            AuthorsTable authors = new AuthorsTable("a")
            PublicationsTable publications = new PublicationsTable("p")

            // Alice: has publications but none after 2020
            session.insertInto(authors)
                    .set(authors.id, 1L)
                    .set(authors.name, "Alice")
                    .set(authors.email, "alice@example.com")
                    .execute()
            session.insertInto(publications)
                    .set(publications.id, 1L)
                    .set(publications.authorId, 1L)
                    .set(publications.title, "Old Book")
                    .set(publications.publicationYear, 2018)
                    .execute()

            // Bob: has recent publication
            session.insertInto(authors)
                    .set(authors.id, 2L)
                    .set(authors.name, "Bob")
                    .set(authors.email, "bob@example.com")
                    .execute()
            session.insertInto(publications)
                    .set(publications.id, 2L)
                    .set(publications.authorId, 2L)
                    .set(publications.title, "New Book")
                    .set(publications.publicationYear, 2022)
                    .execute()

            // Charlie: no publications at all
            session.insertInto(authors)
                    .set(authors.id, 3L)
                    .set(authors.name, "Charlie")
                    .execute()

        when: "selecting authors without publications after 2020"
            List<Record> records = session
                    .select(authors.name)
                    .from(authors)
                    .where(
                            DSL.exists(
                                    DSL.select(publications.id)
                                            .from(publications)
                                            .where(
                                                    publications.authorId.eq(authors.id)
                                                            .and(publications.publicationYear.gt(2020))
                                            )
                            ).not()
                    )
                    .orderBy(authors.name.asc())
                    .fetchAll()

        then: "authors without recent publications are returned"
            records.size() == 2
            records[0] == "Alice"
            records[1] == "Charlie"

        where:
            session << allSessions
    }

    // ============================================
    // Subquery with Aggregate Functions
    // ============================================

    def "should use subquery with MAX to find records matching maximum value"() {
        given: "basics with different numero values"
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 300)
            fixtures.addBasic(3L, "Charlie", 200)
            fixtures.addBasic(4L, "Diana", 300)  // Same as max

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subBasics = new BasicsTable("sb")

        when: "selecting records where numero equals the maximum numero"
            List<Record> records = session
                    .select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .where(
                            basics.numero.eq(
                                    DSL.select(DSL.max(subBasics.numero))
                                            .from(subBasics)
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then: "returns all records with maximum numero value"
            records.size() == 2
            records[0].get(basics.name) == "Bob"
            records[0].get(basics.numero) == 300
            records[1].get(basics.name) == "Diana"
            records[1].get(basics.numero) == 300

        where:
            session << allSessions
    }


    def "should use subquery with MIN to find records below minimum in another set"() {
        given: "publications with different years"
            AuthorsTable authors = new AuthorsTable("a")
            PublicationsTable pubs = new PublicationsTable("p")
            PublicationsTable subPubs = new PublicationsTable("sp")

            session.insertInto(authors)
                    .set(authors.id, 1L)
                    .set(authors.name, "Alice")
                    .set(authors.email, "alice@example.com")
                    .execute()
            session.insertInto(pubs)
                    .set(pubs.id, 1L)
                    .set(pubs.authorId, 1L)
                    .set(pubs.title, "Very Old")
                    .set(pubs.publicationYear, 2010)
                    .execute()
            session.insertInto(pubs)
                    .set(pubs.id, 2L)
                    .set(pubs.authorId, 1L)
                    .set(pubs.title, "Old")
                    .set(pubs.publicationYear, 2015)
                    .execute()
            session.insertInto(pubs)
                    .set(pubs.id, 3L)
                    .set(pubs.authorId, 1L)
                    .set(pubs.title, "Recent")
                    .set(pubs.publicationYear, 2020)
                    .execute()

        when: "selecting publications older than the average year"
            List<Record> records = session
                    .select(pubs.title, pubs.publicationYear)
                    .from(pubs)
                    .where(
                            pubs.publicationYear.lt(
                                    DSL.select(DSL.avg(subPubs.publicationYear))
                                            .from(subPubs)
                            )
                    )
                    .orderBy(pubs.publicationYear.asc())
                    .fetchAll()

        then: "returns publications below average year"
            // Avg year: (2010 + 2015 + 2020) / 3 = 2015.0
            // Below average: 2010
            records.size() == 1
            records[0].get(pubs.title) == "Very Old"
            records[0].get(pubs.publicationYear) == 2010

        where:
            session << allSessions
    }


    // ============================================
    // Multiple Subqueries in Single Query
    // ============================================

    def "should combine multiple subqueries in WHERE clause"() {
        given: "test data"
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 250)
            fixtures.addBasic(3L, "Charlie", 300)
            fixtures.addBasic(4L, "Diana", 150)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable sub1 = new BasicsTable("s1")
            BasicsTable sub2 = new BasicsTable("s2")

        when: "selecting with multiple subquery conditions"
            // Find records where numero is between min and max of other records
            List<Record> records = session
                    .select(basics.name, basics.numero)
                    .from(basics)
                    .where(
                            basics.numero.ge(
                                    DSL.select(DSL.min(sub1.numero))
                                            .from(sub1)
                                            .where(sub1.id.notEq(basics.id))
                            ).and(
                                    basics.numero.le(
                                            DSL.select(DSL.max(sub2.numero))
                                                    .from(sub2)
                                                    .where(sub2.id.notEq(basics.id))
                                    )
                            )
                    )
                    .orderBy(basics.numero.asc())
                    .fetchAll()

        then: "only records between min and max of the other records match"
            // Alice (100): min of others is 150 -> 100 >= 150 fails
            // Diana (150): min of others 100, max of others 300 -> matches
            // Bob (250): min of others 100, max of others 300 -> matches
            // Charlie (300): max of others is 250 -> 300 <= 250 fails
            records.size() == 2
            records[0].get(basics.name) == "Diana"
            records[1].get(basics.name) == "Bob"

        where:
            session << allSessions
    }


    def "should use subquery in both SELECT and WHERE clauses"() {
        given: "authors with publications"
            AuthorsTable authors = new AuthorsTable("a")
            PublicationsTable pubsCount = new PublicationsTable("pc")
            PublicationsTable pubsFilter = new PublicationsTable("pf")

            session.insertInto(authors)
                    .set(authors.id, 1L)
                    .set(authors.name, "Alice")
                    .set(authors.email, "alice@example.com")
                    .execute()
            session.insertInto(pubsCount)
                    .set(pubsCount.id, 1L)
                    .set(pubsCount.authorId, 1L)
                    .set(pubsCount.title, "Book A")
                    .set(pubsCount.publicationYear, 2020)
                    .execute()
            session.insertInto(pubsCount)
                    .set(pubsCount.id, 2L)
                    .set(pubsCount.authorId, 1L)
                    .set(pubsCount.title, "Book B")
                    .set(pubsCount.publicationYear, 2022)
                    .execute()

            session.insertInto(authors)
                    .set(authors.id, 2L)
                    .set(authors.name, "Bob")
                    .set(authors.email, "bob@example.com")
                    .execute()
            session.insertInto(pubsCount)
                    .set(pubsCount.id, 3L)
                    .set(pubsCount.authorId, 2L)
                    .set(pubsCount.title, "Book C")
                    .set(pubsCount.publicationYear, 2021)
                    .execute()

        when: "selecting with subquery in SELECT and WHERE"
            List<Record> records = session
                    .select(
                            authors.name,
                            // Subquery in SELECT: count publications
                            DSL.select(DSL.count())
                                    .from(pubsCount)
                                    .where(pubsCount.authorId.eq(authors.id))
                    )
                    .from(authors)
                    .where(
                            // Subquery in WHERE: has publication after 2020
                            DSL.exists(
                                    DSL.select(pubsFilter.id)
                                            .from(pubsFilter)
                                            .where(
                                                    pubsFilter.authorId.eq(authors.id)
                                                            .and(pubsFilter.publicationYear.gt(2020))
                                            )
                            )
                    )
                    .orderBy(authors.name.asc())
                    .fetchAll()

        then: "returns authors with recent publications and their counts"
            records.size() == 2
            records[0].get(1, String.class) == "Alice"
            records[0].get(2, Long.class) == 2L
            records[1].get(1, String.class) == "Bob"
            records[1].get(2, Long.class) == 1L

        where:
            session << allSessions
    }


    // ============================================
    // Complex Correlated Subquery Patterns
    // ============================================

    def "should use correlated subquery referencing outer query multiple times"() {
        given: "employees with hierarchical structure"
            // Data already exists in schema from employees table
            EmployeesTable emp = new EmployeesTable("e")
            EmployeesTable mgr = new EmployeesTable("m")

        when: "finding employees who manage others in the same department"
            List<Record> records = session
                    .select(emp.name, emp.department)
                    .from(emp)
                    .where(
                            DSL.exists(
                                    DSL.select(mgr.id)
                                            .from(mgr)
                                            .where(
                                                    mgr.managerId.eq(emp.id)
                                                            .and(mgr.department.eq(emp.department))
                                            )
                            )
                    )
                    .orderBy(emp.name.asc())
                    .fetchAll()

        then: "returns managers who have subordinates in same department"
            // CTO (Engineering) manages Alice, Bob, Charlie (all Engineering)
            // Sales VP (Sales) manages Diana, Eve, Frank (all Sales)
            records.size() == 2
            records[0].get(emp.name) == "CTO"
            records[0].get(emp.department) == "Engineering"
            records[1].get(emp.name) == "Sales VP"
            records[1].get(emp.department) == "Sales"

        where:
            session << allSessions
    }
}
