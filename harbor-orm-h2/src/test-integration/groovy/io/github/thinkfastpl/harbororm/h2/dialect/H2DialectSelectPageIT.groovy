// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.query.result.Page
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable

class H2DialectSelectPageIT extends H2DialectBaseIT {

    void setup() {
        loadScript("basics.sql")
        loadScript("basics-data.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "select records with window function"() {
        given:
            BasicsTable basics = new BasicsTable(a)

        when:
            Page<Record> page = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(2, 0)

        then:
            page.rows.size() == 2
            page.rows[0].columnsCount() == basics.allColumns.size()
            page.rows[0].get(basics.id) == 1L
            page.rows[0].get(basics.name) == "Ania"
            page.rows[1].columnsCount() == basics.allColumns.size()
            page.rows[1].get(basics.id) == 2L
            page.rows[1].get(basics.name) == "Asia"

            page.pageSize == 2
            page.page == 0
            page.totalRows == 3
            page.totalPages == 2

        where:
            a << [null, 'a']
    }

    def "select single column with window function"() {
        given:
            BasicsTable basics = new BasicsTable(a)

        when:
            Page<Long> page = session.select(basics.id)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(2, 0)

        then:
            page.rows.size() == 2
            page.rows[0] == 1L
            page.rows[1] == 2L

            page.pageSize == 2
            page.page == 0
            page.totalRows == 3
            page.totalPages == 2

        where:
            a << [null, 'a']
    }

    def "select records with separate query (empty result)"() {
        given:
            BasicsTable basics = new BasicsTable(a)

        when:
            Page<Record> page = session.select(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(2, 2)

        then:
            page.rows.isEmpty()
            page.pageSize == 2
            page.page == 2
            page.totalRows == 3
            page.totalPages == 2

        where:
            a << [null, 'a']
    }

    def "select single column with separate query (empty result)"() {
        given:
            BasicsTable basics = new BasicsTable(a)

        when:
            Page<Long> page = session.select(basics.id)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(2, 2)

        then:
            page.rows.isEmpty()
            page.pageSize == 2
            page.page == 2
            page.totalRows == 3
            page.totalPages == 2

        where:
            a << [null, 'a']
    }


    def "select records with no separate query (empty result)"() {
        given:
            BasicsTable basics = new BasicsTable(a)

        when:
            Page<Record> page = session.select(basics.allColumns)
                    .from(basics)
                    .where(basics.id.lt(0))
                    .orderBy(basics.id.asc())
                    .fetchPage(2, 0)

        then:
            page.rows.isEmpty()
            page.pageSize == 2
            page.page == 0
            page.totalRows == 0
            page.totalPages == 0

        where:
            a << [null, 'a']
    }

    def "select single column with no separate query (empty result)"() {
        given:
            BasicsTable basics = new BasicsTable(a)

        when:
            Page<Long> page = session.select(basics.id)
                    .from(basics)
                    .where(basics.id.lt(0))
                    .orderBy(basics.id.asc())
                    .fetchPage(2, 0)

        then:
            page.rows.isEmpty()
            page.pageSize == 2
            page.page == 0
            page.totalRows == 0
            page.totalPages == 0

        where:
            a << [null, 'a']
    }


    def "select distinct records"() {
        given:
            BasicsTable basics = new BasicsTable(a)

        when:
            Page<Record> page = session.selectDistinct(basics.allColumns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(2, 0)

        then:
            page.rows.size() == 2
            page.rows[0].columnsCount() == basics.allColumns.size()
            page.rows[0].get(basics.id) == 1L
            page.rows[0].get(basics.name) == "Ania"
            page.rows[1].columnsCount() == basics.allColumns.size()
            page.rows[1].get(basics.id) == 2L
            page.rows[1].get(basics.name) == "Asia"

            page.pageSize == 2
            page.page == 0
            page.totalRows == 3
            page.totalPages == 2

        where:
            a << [null, 'a']
    }

    def "select distinct single column"() {
        given:
            BasicsTable basics = new BasicsTable(a)

        when:
            Page<Long> page = session.selectDistinct(basics.id)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchPage(2, 0)

        then:
            page.rows.size() == 2
            page.rows[0] == 1L
            page.rows[1] == 2L

            page.pageSize == 2
            page.page == 0
            page.totalRows == 3
            page.totalPages == 2

        where:
            a << [null, 'a']
    }
}
