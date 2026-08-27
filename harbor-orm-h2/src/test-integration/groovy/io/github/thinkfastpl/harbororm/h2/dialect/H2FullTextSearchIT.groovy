// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.h2.expression.H2DSL

class H2FullTextSearchIT extends H2DialectBaseIT {

    def setup() {
        loadScript("fulltext-test.sql")
    }

    def cleanup() {
        dropAllObjects()
    }

    def "ftSearch filters rows matching the search query"() {
        when:
            def records = session.select(DSL.name(Long, "id"), DSL.name(String, "title"))
                .from("fts_articles")
                .where(H2DSL.ftSearch(DSL.name(Long, "id"), "FTS_ARTICLES", "PostgreSQL"))
                .orderBy(DSL.asc("id"))
                .fetchAll()

        then:
            records.size() >= 2
    }

    def "ftSearch returns no rows for a non-matching query"() {
        when:
            def records = session.select(DSL.name(Long, "id"))
                .from("fts_articles")
                .where(H2DSL.ftSearch(DSL.name(Long, "id"), "FTS_ARTICLES", "nonexistentterm"))
                .fetchAll()

        then:
            records.isEmpty()
    }

    def "ftSearch multi-word query matches rows containing all words"() {
        when:
            def records = session.select(DSL.name(Long, "id"), DSL.name(String, "title"))
                .from("fts_articles")
                .where(H2DSL.ftSearch(DSL.name(Long, "id"), "FTS_ARTICLES", "relational database"))
                .fetchAll()

        then:
            !records.isEmpty()
    }
}
