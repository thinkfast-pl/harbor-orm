// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.SelectExpressionTableSource
import io.github.thinkfastpl.harbororm.api.metadata.QColumn
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.AuthorsTable
import io.github.thinkfastpl.harbororm.query.PublicationsTable

/**
 * Integration tests for LATERAL JOIN functionality.
 * LATERAL is PostgreSQL-only (H2 does not support it).
 */
class SelectLateralJoinIT extends AbstractHarborIT {

    private void insertTestData(session) {
        AuthorsTable authors = new AuthorsTable(null)
        PublicationsTable publications = new PublicationsTable(null)

        // Author 1: Alice - 3 publications
        session.insertInto(authors)
                .set(authors.id, 1L)
                .set(authors.name, "Alice")
                .set(authors.email, "alice@example.com")
                .execute()
        session.insertInto(publications)
                .set(publications.id, 1L)
                .set(publications.authorId, 1L)
                .set(publications.title, "Book A1")
                .set(publications.publicationYear, 2020)
                .execute()
        session.insertInto(publications)
                .set(publications.id, 2L)
                .set(publications.authorId, 1L)
                .set(publications.title, "Book A2")
                .set(publications.publicationYear, 2022)
                .execute()
        session.insertInto(publications)
                .set(publications.id, 3L)
                .set(publications.authorId, 1L)
                .set(publications.title, "Book A3")
                .set(publications.publicationYear, 2021)
                .execute()

        // Author 2: Bob - 1 publication
        session.insertInto(authors)
                .set(authors.id, 2L)
                .set(authors.name, "Bob")
                .set(authors.email, "bob@example.com")
                .execute()
        session.insertInto(publications)
                .set(publications.id, 4L)
                .set(publications.authorId, 2L)
                .set(publications.title, "Book B1")
                .set(publications.publicationYear, 2023)
                .execute()

        // Author 3: Charlie - 0 publications
        session.insertInto(authors)
                .set(authors.id, 3L)
                .set(authors.name, "Charlie")
                .execute()
    }

    // ============================================
    // LEFT JOIN LATERAL
    // ============================================

    def "LEFT JOIN LATERAL should return latest publication per author including authors with no publications"() {
        given: "authors with varying numbers of publications"
            insertTestData(session)

            AuthorsTable authors = new AuthorsTable("a")
            PublicationsTable pubs = new PublicationsTable("p")

            SelectExpressionTableSource latestPub = DSL
                    .select(pubs.title, pubs.publicationYear)
                    .from(pubs)
                    .where(pubs.authorId.eq(authors.id))
                    .orderBy(pubs.publicationYear.desc())
                    .limit(1)
                    .asTableSource("lp")

            QColumn<String> pubTitle = latestPub.getColumn(pubs.title, "title")
            QColumn<Integer> pubYear = latestPub.getColumn(pubs.publicationYear, "publication_year")

        when: "performing LEFT JOIN LATERAL to get latest publication per author"
            List<Record> records = session.select(authors.name, pubTitle, pubYear)
                    .from(authors)
                    .leftJoinLateral(latestPub).on(DSL.TRUE)
                    .orderBy(authors.name.asc())
                    .fetchAll()

        then: "all authors returned, including those with no publications"
            records.size() == 3

            with(records[0]) { r ->
                r.get(authors.name) == "Alice"
                r.get(pubTitle) == "Book A2"
                r.get(pubYear) == 2022
            }
            with(records[1]) { r ->
                r.get(authors.name) == "Bob"
                r.get(pubTitle) == "Book B1"
                r.get(pubYear) == 2023
            }
            with(records[2]) { r ->
                r.get(authors.name) == "Charlie"
                r.get(pubTitle) == null
                r.get(pubYear) == null
            }

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    // ============================================
    // CROSS JOIN LATERAL
    // ============================================

    def "CROSS JOIN LATERAL should return top N publications per author excluding authors with none"() {
        given: "authors with varying numbers of publications"
            insertTestData(session)

            AuthorsTable authors = new AuthorsTable("a")
            PublicationsTable pubs = new PublicationsTable("p")

            SelectExpressionTableSource topPubs = DSL
                    .select(pubs.title, pubs.publicationYear)
                    .from(pubs)
                    .where(pubs.authorId.eq(authors.id))
                    .orderBy(pubs.publicationYear.desc())
                    .limit(2)
                    .asTableSource("tp")

            QColumn<String> pubTitle = topPubs.getColumn(pubs.title, "title")
            QColumn<Integer> pubYear = topPubs.getColumn(pubs.publicationYear, "publication_year")

        when: "performing CROSS JOIN LATERAL to get top 2 publications per author"
            List<Record> records = session.select(authors.name, pubTitle, pubYear)
                    .from(authors)
                    .crossJoinLateral(topPubs)
                    .orderBy(authors.name.asc(), pubYear.desc())
                    .fetchAll()

        then: "only authors with publications returned, max 2 per author"
            records.size() == 3

            // Alice: top 2 of her 3 publications
            with(records[0]) { r ->
                r.get(authors.name) == "Alice"
                r.get(pubTitle) == "Book A2"
                r.get(pubYear) == 2022
            }
            with(records[1]) { r ->
                r.get(authors.name) == "Alice"
                r.get(pubTitle) == "Book A3"
                r.get(pubYear) == 2021
            }

            // Bob: his 1 publication
            with(records[2]) { r ->
                r.get(authors.name) == "Bob"
                r.get(pubTitle) == "Book B1"
                r.get(pubYear) == 2023
            }

            // Charlie excluded - no publications

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    // ============================================
    // INNER JOIN LATERAL
    // ============================================

    def "INNER JOIN LATERAL should filter out rows where subquery returns no results"() {
        given: "authors with varying numbers of publications"
            insertTestData(session)

            AuthorsTable authors = new AuthorsTable("a")
            PublicationsTable pubs = new PublicationsTable("p")

            SelectExpressionTableSource latestPub = DSL
                    .select(pubs.title, pubs.publicationYear)
                    .from(pubs)
                    .where(pubs.authorId.eq(authors.id))
                    .orderBy(pubs.publicationYear.desc())
                    .limit(1)
                    .asTableSource("lp")

            QColumn<String> pubTitle = latestPub.getColumn(pubs.title, "title")
            QColumn<Integer> pubYear = latestPub.getColumn(pubs.publicationYear, "publication_year")

        when: "performing INNER JOIN LATERAL"
            List<Record> records = session.select(authors.name, pubTitle, pubYear)
                    .from(authors)
                    .innerJoinLateral(latestPub).on(DSL.TRUE)
                    .orderBy(authors.name.asc())
                    .fetchAll()

        then: "only authors with publications returned"
            records.size() == 2

            with(records[0]) { r ->
                r.get(authors.name) == "Alice"
                r.get(pubTitle) == "Book A2"
                r.get(pubYear) == 2022
            }
            with(records[1]) { r ->
                r.get(authors.name) == "Bob"
                r.get(pubTitle) == "Book B1"
                r.get(pubYear) == 2023
            }

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    // ============================================
    // LATERAL with WHERE filter on outer table
    // ============================================

    def "LEFT JOIN LATERAL should work with WHERE clause on outer table"() {
        given: "authors with publications"
            insertTestData(session)

            AuthorsTable authors = new AuthorsTable("a")
            PublicationsTable pubs = new PublicationsTable("p")

            SelectExpressionTableSource latestPub = DSL
                    .select(pubs.title)
                    .from(pubs)
                    .where(pubs.authorId.eq(authors.id))
                    .orderBy(pubs.publicationYear.desc())
                    .limit(1)
                    .asTableSource("lp")

            QColumn<String> pubTitle = latestPub.getColumn(pubs.title, "title")

        when: "filtering outer table and using LATERAL"
            List<Record> records = session.select(authors.name, pubTitle)
                    .from(authors)
                    .leftJoinLateral(latestPub).on(DSL.TRUE)
                    .where(authors.name.eq("Alice"))
                    .fetchAll()

        then: "only filtered author returned with lateral result"
            records.size() == 1
            with(records[0]) { r ->
                r.get(authors.name) == "Alice"
                r.get(pubTitle) == "Book A2"
            }

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    // ============================================
    // LATERAL with aggregate subquery
    // ============================================

    def "LEFT JOIN LATERAL should work with aggregate subquery"() {
        given: "authors with publications"
            insertTestData(session)

            AuthorsTable authors = new AuthorsTable("a")
            PublicationsTable pubs = new PublicationsTable("p")

            SelectExpressionTableSource countSub = DSL
                    .select(DSL.count())
                    .from(pubs)
                    .where(pubs.authorId.eq(authors.id))
                    .asTableSource("pc")

            QColumn<Long> pubCnt = countSub.getColumn(DSL.count(), "cnt")

        when: "performing LEFT JOIN LATERAL with count subquery"
            List<Record> records = session.select(authors.name, pubCnt)
                    .from(authors)
                    .leftJoinLateral(countSub).on(DSL.TRUE)
                    .orderBy(authors.name.asc())
                    .fetchAll()

        then: "all authors returned with their publication count"
            records.size() == 3
            with(records[0]) { r ->
                r.get(authors.name) == "Alice"
                r.get(pubCnt) == 3L
            }
            with(records[1]) { r ->
                r.get(authors.name) == "Bob"
                r.get(pubCnt) == 1L
            }
            with(records[2]) { r ->
                r.get(authors.name) == "Charlie"
                r.get(pubCnt) == 0L
            }

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }
}
