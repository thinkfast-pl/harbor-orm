// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.query.result.Page
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class FetchPageIT extends AbstractHarborIT {

    private void setupTestData(TestFixtures fixtures, int count) {
        // Create test records with sequential IDs for predictable pagination
        (1L..count).each { id ->
            fixtures.addBasic(id, "Name${id}", id.intValue() * 10)
        }
    }

    // ============================================
    // Basic Page object tests
    // ============================================

    def "fetchPage returns Page object with content and metadata"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures, 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            Page<Record> page = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(3, 0)

        then:
            page != null
            page.rows != null
            page.rows.size() == 3
            page.pageSize == 3
            page.page == 0
            page.totalRows == 10
            page.totalPages == 4

        where:
            session << allSessions
    }

    def "Page contains correct total count across all pages"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures, 15)
            BasicsTable basics = new BasicsTable(null)

        when:
            Page<Record> page = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(4, 0)

        then:
            page.totalRows == 15
            page.totalPages == 4  // ceil(15/4) = 4

        where:
            session << allSessions
    }

    def "Page contains correct page number and size"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures, 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            Page<Record> page = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(5, 1)

        then:
            page.pageSize == 5
            page.page == 1

        where:
            session << allSessions
    }

    // ============================================
    // Pagination through multiple pages
    // ============================================

    def "pagination through multiple pages returns correct content"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures, 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            Page<Record> page0 = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(3, 0)

            Page<Record> page1 = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(3, 1)

            Page<Record> page2 = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(3, 2)

            Page<Record> page3 = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(3, 3)

        then:
            // Page 0: ids 1, 2, 3
            page0.rows.size() == 3
            page0.rows[0].get(basics.id) == 1L
            page0.rows[1].get(basics.id) == 2L
            page0.rows[2].get(basics.id) == 3L
            page0.page == 0
            page0.totalRows == 10
            page0.totalPages == 4

            // Page 1: ids 4, 5, 6
            page1.rows.size() == 3
            page1.rows[0].get(basics.id) == 4L
            page1.rows[1].get(basics.id) == 5L
            page1.rows[2].get(basics.id) == 6L
            page1.page == 1
            page1.totalRows == 10
            page1.totalPages == 4

            // Page 2: ids 7, 8, 9
            page2.rows.size() == 3
            page2.rows[0].get(basics.id) == 7L
            page2.rows[1].get(basics.id) == 8L
            page2.rows[2].get(basics.id) == 9L
            page2.page == 2
            page2.totalRows == 10
            page2.totalPages == 4

            // Page 3: only id 10 (partial page)
            page3.rows.size() == 1
            page3.rows[0].get(basics.id) == 10L
            page3.page == 3
            page3.totalRows == 10
            page3.totalPages == 4

        where:
            session << allSessions
    }

    // ============================================
    // Last page with fewer elements
    // ============================================

    def "last page with fewer elements handles partial pages correctly"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures, 7)
            BasicsTable basics = new BasicsTable(null)

        when:
            Page<Record> lastPage = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(3, 2)

        then:
            lastPage.rows.size() == 1  // Only 1 element on last page (7 % 3 = 1)
            lastPage.rows[0].get(basics.id) == 7L
            lastPage.pageSize == 3
            lastPage.page == 2
            lastPage.totalRows == 7
            lastPage.totalPages == 3  // ceil(7/3) = 3

        where:
            session << allSessions
    }

    def "page beyond last returns empty content but correct metadata"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures, 5)
            BasicsTable basics = new BasicsTable(null)

        when:
            Page<Record> beyondLastPage = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(3, 5)

        then:
            beyondLastPage.rows.isEmpty()
            beyondLastPage.pageSize == 3
            beyondLastPage.page == 5
            beyondLastPage.totalRows == 5
            beyondLastPage.totalPages == 2

        where:
            session << allSessions
    }

    // ============================================
    // Empty page when no results
    // ============================================

    def "empty page when no results match query"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures, 5)
            BasicsTable basics = new BasicsTable(null)

        when:
            Page<Record> emptyPage = session.select(basics.allColumns)
                    .from(basics)
                    .where(basics.id.lt(0))  // No records match
                    .orderBy(basics.id.asc())
                    .fetchPage(3, 0)

        then:
            emptyPage.rows.isEmpty()
            emptyPage.pageSize == 3
            emptyPage.page == 0
            emptyPage.totalRows == 0
            emptyPage.totalPages == 0

        where:
            session << allSessions
    }

    def "empty page when table has no data"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            // No data setup - table is empty

        when:
            Page<Record> emptyPage = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(10, 0)

        then:
            emptyPage.rows.isEmpty()
            emptyPage.pageSize == 10
            emptyPage.page == 0
            emptyPage.totalRows == 0
            emptyPage.totalPages == 0

        where:
            session << allSessions
    }

    // ============================================
    // fetchPage with WHERE clause
    // ============================================

    def "fetchPage with WHERE clause filters results correctly"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures, 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            // Filter to records where numero > 50 (ids 6,7,8,9,10 = 5 records)
            Page<Record> page = session.select(basics.allColumns)
                    .from(basics)
                    .where(basics.numero.gt(50))
                    .orderBy(basics.id.asc())
                    .fetchPage(2, 0)

        then:
            page.rows.size() == 2
            page.rows[0].get(basics.id) == 6L
            page.rows[0].get(basics.numero) == 60
            page.rows[1].get(basics.id) == 7L
            page.rows[1].get(basics.numero) == 70
            page.totalRows == 5
            page.totalPages == 3  // ceil(5/2) = 3

        where:
            session << allSessions
    }

    def "fetchPage with WHERE clause pagination through filtered results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures, 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            // Filter to records where numero >= 30 (ids 3-10 = 8 records)
            Page<Record> page0 = session.select(basics.allColumns)
                    .from(basics)
                    .where(basics.numero.ge(30))
                    .orderBy(basics.id.asc())
                    .fetchPage(3, 0)

            Page<Record> page1 = session.select(basics.allColumns)
                    .from(basics)
                    .where(basics.numero.ge(30))
                    .orderBy(basics.id.asc())
                    .fetchPage(3, 1)

            Page<Record> page2 = session.select(basics.allColumns)
                    .from(basics)
                    .where(basics.numero.ge(30))
                    .orderBy(basics.id.asc())
                    .fetchPage(3, 2)

        then:
            // Page 0: ids 3, 4, 5
            page0.rows.size() == 3
            page0.rows*.get(basics.id) == [3L, 4L, 5L]
            page0.totalRows == 8
            page0.totalPages == 3

            // Page 1: ids 6, 7, 8
            page1.rows.size() == 3
            page1.rows*.get(basics.id) == [6L, 7L, 8L]
            page1.totalRows == 8
            page1.totalPages == 3

            // Page 2: ids 9, 10 (partial page)
            page2.rows.size() == 2
            page2.rows*.get(basics.id) == [9L, 10L]
            page2.totalRows == 8
            page2.totalPages == 3

        where:
            session << allSessions
    }

    // ============================================
    // Single column fetchPage
    // ============================================

    def "fetchPage with single column selection"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures, 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            Page<Long> page = session.select(basics.id)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(4, 1)

        then:
            page.rows.size() == 4
            page.rows == [5L, 6L, 7L, 8L]
            page.pageSize == 4
            page.page == 1
            page.totalRows == 10
            page.totalPages == 3

        where:
            session << allSessions
    }

    // ============================================
    // fetchPage with descending order
    // ============================================

    def "fetchPage with descending order"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures, 10)
            BasicsTable basics = new BasicsTable(null)

        when:
            Page<Record> page = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.desc())
                    .fetchPage(3, 0)

        then:
            page.rows.size() == 3
            page.rows[0].get(basics.id) == 10L
            page.rows[1].get(basics.id) == 9L
            page.rows[2].get(basics.id) == 8L
            page.totalRows == 10
            page.totalPages == 4

        where:
            session << allSessions
    }

    // ============================================
    // fetchPage with table alias
    // ============================================

    def "fetchPage works with table alias"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures, 5)
            BasicsTable basics = new BasicsTable("b")

        when:
            Page<Record> page = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(2, 1)

        then:
            page.rows.size() == 2
            page.rows[0].get(basics.id) == 3L
            page.rows[1].get(basics.id) == 4L
            page.pageSize == 2
            page.page == 1
            page.totalRows == 5
            page.totalPages == 3

        where:
            session << allSessions
    }

    // ============================================
    // Page size edge cases
    // ============================================

    def "fetchPage with page size equal to total rows"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures, 5)
            BasicsTable basics = new BasicsTable(null)

        when:
            Page<Record> page = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(5, 0)

        then:
            page.rows.size() == 5
            page.pageSize == 5
            page.page == 0
            page.totalRows == 5
            page.totalPages == 1

        where:
            session << allSessions
    }

    def "fetchPage with page size larger than total rows"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures, 3)
            BasicsTable basics = new BasicsTable(null)

        when:
            Page<Record> page = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(10, 0)

        then:
            page.rows.size() == 3
            page.pageSize == 10
            page.page == 0
            page.totalRows == 3
            page.totalPages == 1

        where:
            session << allSessions
    }

    def "fetchPage with page size of 1"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures, 5)
            BasicsTable basics = new BasicsTable(null)

        when:
            Page<Record> page = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(1, 2)

        then:
            page.rows.size() == 1
            page.rows[0].get(basics.id) == 3L
            page.pageSize == 1
            page.page == 2
            page.totalRows == 5
            page.totalPages == 5

        where:
            session << allSessions
    }
}
