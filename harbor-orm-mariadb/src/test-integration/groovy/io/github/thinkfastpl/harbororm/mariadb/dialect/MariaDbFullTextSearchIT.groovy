// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.mariadb.MariaDbBaseIT
import io.github.thinkfastpl.harbororm.mariadb.expression.MariaDbDSL

class MariaDbFullTextSearchIT extends MariaDbBaseIT {

    def setup() {
        loadScript("fulltext-test.sql")
    }

    def cleanup() {
        dropAllObjects()
    }

    def "againstBoolean with required term filters results"() {
        when:
            def records = session.select(DSL.name(Long, "id"), DSL.name(String, "title"))
                .from("fts_articles")
                .where(MariaDbDSL.match(DSL.name(String, "body"))
                        .matches(MariaDbDSL.againstBoolean("+PostgreSQL")))
                .fetchAll()

        then:
            records.size() >= 2
    }

    def "againstBoolean negation excludes matching documents"() {
        when:
            def records = session.select(DSL.name(Long, "id"), DSL.name(String, "title"))
                .from("fts_articles")
                .where(MariaDbDSL.match(DSL.name(String, "body"))
                        .matches(MariaDbDSL.againstBoolean("database -MySQL")))
                .fetchAll()

        then:
            !records.isEmpty()
            records.every { it.get("title", String) != "Introduction to MySQL" }
    }

    def "matchRank returns positive score for matching documents"() {
        given:
            def match = MariaDbDSL.match(DSL.name(String, "body"))
            def against = MariaDbDSL.againstBoolean("database")

        when:
            def records = session.select(DSL.name(Long, "id"), MariaDbDSL.matchRank(match, against).as("rank"))
                .from("fts_articles")
                .where(match.matches(against))
                .fetchAll()

        then:
            !records.isEmpty()
            records.every { ((Number) it.get("rank", Double.class)).doubleValue() > 0.0d }
    }

    def "match with multiple columns searches across title and body"() {
        when:
            def records = session.select(DSL.name(Long, "id"), DSL.name(String, "title"))
                .from("fts_articles")
                .where(MariaDbDSL.match(DSL.name(String, "title"), DSL.name(String, "body"))
                        .matches(MariaDbDSL.againstBoolean("MySQL")))
                .fetchAll()

        then:
            records.size() >= 1
    }

    def "againstWithQueryExpansion returns results"() {
        when:
            def records = session.select(DSL.name(Long, "id"), DSL.name(String, "title"))
                .from("fts_articles")
                .where(MariaDbDSL.match(DSL.name(String, "body"))
                        .matches(MariaDbDSL.againstWithQueryExpansion("database")))
                .fetchAll()

        then:
            !records.isEmpty()
    }

    def "against in natural language mode filters results"() {
        when:
            def records = session.select(DSL.name(Long, "id"), DSL.name(String, "title"))
                .from("fts_articles")
                .where(MariaDbDSL.match(DSL.name(String, "body"))
                        .matches(MariaDbDSL.against("PostgreSQL")))
                .fetchAll()

        then:
            records.size() >= 2
    }
}
