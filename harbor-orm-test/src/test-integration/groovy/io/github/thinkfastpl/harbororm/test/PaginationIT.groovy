// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.BasicEntity
import io.github.thinkfastpl.harbororm.test.domain.QBasicEntity
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class PaginationIT extends AbstractHarborIT {

    private void setupTestData(TestFixtures fixtures) {
        // Create 10 records with sequential IDs for predictable pagination
        (1L..10L).each { id ->
            fixtures.addBasic(id, "Name${id}", id.intValue() * 10)
        }
    }

    // ============================================
    // Raw SELECT query tests
    // ============================================

    def "raw select with limit only should restrict result count"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)

        when:
            List<Record> records = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .limit(3)
                    .fetchAll()

        then:
            records.size() == 3
            records[0].get(basics.id) == 1L
            records[1].get(basics.id) == 2L
            records[2].get(basics.id) == 3L

        where:
            session << allSessions
    }

    def "raw select with offset only should skip initial results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)

        when:
            List<Record> records = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .offset(5)
                    .fetchAll()

        then:
            records.size() == 5
            records[0].get(basics.id) == 6L
            records[1].get(basics.id) == 7L
            records[2].get(basics.id) == 8L
            records[3].get(basics.id) == 9L
            records[4].get(basics.id) == 10L

        where:
            session << allSessions
    }

    def "raw select with limit and offset combined should implement standard pagination"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)

        when: "page 2 with 3 items per page (offset=3, limit=3)"
            List<Record> records = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .limit(3)
                    .offset(3)
                    .fetchAll()

        then:
            records.size() == 3
            records[0].get(basics.id) == 4L
            records[1].get(basics.id) == 5L
            records[2].get(basics.id) == 6L

        where:
            session << allSessions
    }

    def "raw select with limit(0) should return empty results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)

        when:
            List<Record> records = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .limit(0)
                    .fetchAll()

        then:
            records.size() == 0

        where:
            session << allSessions
    }

    def "raw select with offset beyond available results should return empty"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)

        when:
            List<Record> records = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .offset(100)
                    .fetchAll()

        then:
            records.size() == 0

        where:
            session << allSessions
    }

    def "raw select with limit larger than result set should return all available"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)

        when:
            List<Record> records = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .limit(100)
                    .fetchAll()

        then:
            records.size() == 10

        where:
            session << allSessions
    }

    def "raw select pagination should work with descending order"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)

        when:
            List<Record> records = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .orderBy(basics.id.desc())
                    .limit(3)
                    .offset(2)
                    .fetchAll()

        then:
            records.size() == 3
            records[0].get(basics.id) == 8L
            records[1].get(basics.id) == 7L
            records[2].get(basics.id) == 6L

        where:
            session << allSessions
    }

    def "raw select pagination should work with where clause"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)

        when: "paginate over records where numero > 30"
            List<Record> records = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .where(basics.numero.gt(30))
                    .orderBy(basics.id.asc())
                    .limit(3)
                    .offset(2)
                    .fetchAll()

        then:
            // Records with numero > 30 are ids 4,5,6,7,8,9,10 (7 records)
            // Skip 2, take 3 should give ids 6,7,8
            records.size() == 3
            records[0].get(basics.id) == 6L
            records[1].get(basics.id) == 7L
            records[2].get(basics.id) == 8L

        where:
            session << allSessions
    }

    // ============================================
    // Entity query tests
    // ============================================

    def "entity select with limit only should restrict result count"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<BasicEntity> entities = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .limit(3)
                    .fetchAll()

        then:
            entities.size() == 3
            entities[0].id == 1L
            entities[1].id == 2L
            entities[2].id == 3L

        where:
            session << allSessions
    }

    def "entity select with offset only should skip initial results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<BasicEntity> entities = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .offset(5)
                    .fetchAll()

        then:
            entities.size() == 5
            entities[0].id == 6L
            entities[1].id == 7L
            entities[2].id == 8L
            entities[3].id == 9L
            entities[4].id == 10L

        where:
            session << allSessions
    }

    def "entity select with limit and offset combined should implement standard pagination"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when: "page 3 with 2 items per page (offset=4, limit=2)"
            List<BasicEntity> entities = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .limit(2)
                    .offset(4)
                    .fetchAll()

        then:
            entities.size() == 2
            entities[0].id == 5L
            entities[1].id == 6L

        where:
            session << allSessions
    }

    def "entity select with limit(0) should return empty results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<BasicEntity> entities = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .limit(0)
                    .fetchAll()

        then:
            entities.size() == 0

        where:
            session << allSessions
    }

    def "entity select with offset beyond available results should return empty"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<BasicEntity> entities = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .offset(100)
                    .fetchAll()

        then:
            entities.size() == 0

        where:
            session << allSessions
    }

    def "entity select with limit larger than result set should return all available"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<BasicEntity> entities = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .limit(100)
                    .fetchAll()

        then:
            entities.size() == 10

        where:
            session << allSessions
    }

    def "entity select pagination should work with where clause"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when: "paginate over records where name starts with 'Name' and numero >= 50"
            List<BasicEntity> entities = session.selectEntity(qEntity)
                    .where(qEntity.numero.ge(50))
                    .orderBy(qEntity.id.asc())
                    .limit(3)
                    .offset(1)
                    .fetchAll()

        then:
            // Records with numero >= 50 are ids 5,6,7,8,9,10 (6 records)
            // Skip 1, take 3 should give ids 6,7,8
            entities.size() == 3
            entities[0].id == 6L
            entities[1].id == 7L
            entities[2].id == 8L

        where:
            session << allSessions
    }

    def "entity select pagination should verify entity data integrity"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<BasicEntity> entities = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .limit(2)
                    .offset(3)
                    .fetchAll()

        then:
            entities.size() == 2
            with(entities[0]) { e ->
                e.id == 4L
                e.name == "Name4"
                e.numero == 40
            }
            with(entities[1]) { e ->
                e.id == 5L
                e.name == "Name5"
                e.numero == 50
            }

        where:
            session << allSessions
    }

    // ============================================
    // Additional pagination scenarios
    // ============================================

    def "pagination should handle last page with fewer items"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)

        when: "requesting last page when items don't divide evenly (10 items, page size 3, page 4)"
            List<Record> records = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .limit(3)
                    .offset(9)
                    .fetchAll()

        then:
            records.size() == 1
            records[0].get(basics.id) == 10L

        where:
            session << allSessions
    }

    def "pagination with single record on last page"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<BasicEntity> entities = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.desc())
                    .limit(1)
                    .offset(9)
                    .fetchAll()

        then:
            entities.size() == 1
            entities[0].id == 1L

        where:
            session << allSessions
    }

    def "multiple pagination calls should return consistent results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            List<BasicEntity> page1 = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .limit(3)
                    .offset(0)
                    .fetchAll()

            List<BasicEntity> page2 = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .limit(3)
                    .offset(3)
                    .fetchAll()

            List<BasicEntity> page3 = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .limit(3)
                    .offset(6)
                    .fetchAll()

            List<BasicEntity> page4 = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .limit(3)
                    .offset(9)
                    .fetchAll()

        then:
            page1.size() == 3
            page1*.id == [1L, 2L, 3L]

            page2.size() == 3
            page2*.id == [4L, 5L, 6L]

            page3.size() == 3
            page3*.id == [7L, 8L, 9L]

            page4.size() == 1
            page4*.id == [10L]

        where:
            session << allSessions
    }

    // ============================================
    // Page.map() transformation tests
    // ============================================

    def "Page.map() transforms content while preserving metadata"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)

        when:
            def page = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(3, 0)

            def mappedPage = page.map { record -> record.get(basics.name) }

        then:
            mappedPage.rows.size() == 3
            mappedPage.rows == ["Name1", "Name2", "Name3"]
            mappedPage.pageSize == page.pageSize
            mappedPage.page == page.page
            mappedPage.totalRows == page.totalRows
            mappedPage.totalPages == page.totalPages

        where:
            session << allSessions
    }

    def "Page.map() to custom DTO"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)

        when:
            def page = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(3, 1)

            def mappedPage = page.map { record ->
                [id: record.get(basics.id), label: "Item-${record.get(basics.name)}"]
            }

        then:
            mappedPage.rows.size() == 3
            mappedPage.rows[0].id == 4L
            mappedPage.rows[0].label == "Item-Name4"
            mappedPage.rows[1].id == 5L
            mappedPage.rows[1].label == "Item-Name5"
            mappedPage.rows[2].id == 6L
            mappedPage.rows[2].label == "Item-Name6"

            mappedPage.pageSize == page.pageSize
            mappedPage.page == page.page
            mappedPage.totalRows == page.totalRows
            mappedPage.totalPages == page.totalPages

        where:
            session << allSessions
    }

    def "Page.map() on empty page returns empty mapped page"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)

        when: "request a page beyond available data"
            def page = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(3, 10)

            def mappedPage = page.map { record -> record.get(basics.name).toUpperCase() }

        then:
            mappedPage.rows.isEmpty()
            mappedPage.pageSize == page.pageSize
            mappedPage.page == page.page
            mappedPage.totalRows == page.totalRows
            mappedPage.totalPages == page.totalPages

        where:
            session << allSessions
    }
}
