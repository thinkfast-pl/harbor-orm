// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.postgres.PostgresBaseIT
import io.github.thinkfastpl.harbororm.postgres.expression.PostgresDSL

class PostgresFullTextSearchIT extends PostgresBaseIT {

    void setup() {
        loadScript("fulltext-test.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "toTsvector with explicit config and plainToTsquery filters matching rows"() {
        given:
            def tsvector = PostgresDSL.toTsvector("english", DSL.name(String, "body"))
            def tsquery = PostgresDSL.plainToTsquery("english", "database")

        when:
            def records = session.select(DSL.name(Long, "id"), DSL.name(String, "title"))
                .from("fts_articles")
                .where(tsvector.matches(tsquery))
                .orderBy(DSL.asc("id"))
                .fetchAll()

        then:
            !records.isEmpty()
    }

    def "default-config tsvector matches plain tsquery"() {
        given:
            def tsvector = PostgresDSL.toTsvector(DSL.name(String, "body"))
            def tsquery = PostgresDSL.plainToTsquery("PostgreSQL")

        when:
            def records = session.select(DSL.name(Long, "id"), DSL.name(String, "title"))
                .from("fts_articles")
                .where(tsvector.matches(tsquery))
                .fetchAll()

        then:
            records.size() >= 2
    }

    def "non-matching query returns no rows"() {
        given:
            def tsvector = PostgresDSL.toTsvector("english", DSL.name(String, "body"))
            def tsquery = PostgresDSL.plainToTsquery("english", "nonexistentterm")

        when:
            def records = session.select(DSL.name(Long, "id"))
                .from("fts_articles")
                .where(tsvector.matches(tsquery))
                .fetchAll()

        then:
            records.isEmpty()
    }

    def "toTsquery with raw syntax matches documents with both terms"() {
        given:
            def tsvector = PostgresDSL.toTsvector("english", DSL.name(String, "body"))
            def tsquery = PostgresDSL.toTsquery("english", "postgresql & database")

        when:
            def records = session.select(DSL.name(Long, "id"), DSL.name(String, "title"))
                .from("fts_articles")
                .where(tsvector.matches(tsquery))
                .fetchAll()

        then:
            !records.isEmpty()
            records.every { it.get("title", String.class).toLowerCase().contains("postgresql") }
    }

    def "phraseToTsquery matches exact phrase"() {
        given:
            def tsvector = PostgresDSL.toTsvector("english", DSL.name(String, "body"))
            def tsquery = PostgresDSL.phraseToTsquery("english", "open-source relational database")

        when:
            def records = session.select(DSL.name(Long, "id"), DSL.name(String, "title"))
                .from("fts_articles")
                .where(tsvector.matches(tsquery))
                .fetchAll()

        then:
            !records.isEmpty()
    }

    def "websearchToTsquery with negation excludes matching documents"() {
        given:
            def tsvector = PostgresDSL.toTsvector("english", DSL.name(String, "body"))
            def tsquery = PostgresDSL.websearchToTsquery("english", "database -MySQL")

        when:
            def records = session.select(DSL.name(Long, "id"), DSL.name(String, "title"))
                .from("fts_articles")
                .where(tsvector.matches(tsquery))
                .fetchAll()

        then:
            !records.isEmpty()
            records.every { it.get("title", String.class) != "Introduction to MySQL" }
    }

    def "tsRank returns positive score for matching documents"() {
        given:
            def tsvector = PostgresDSL.toTsvector("english", DSL.name(String, "body"))
            def tsquery = PostgresDSL.plainToTsquery("english", "database")

        when:
            def records = session.select(DSL.name(Long, "id"), PostgresDSL.tsRank(tsvector, tsquery).as("rank"))
                .from("fts_articles")
                .where(tsvector.matches(tsquery))
                .fetchAll()

        then:
            !records.isEmpty()
            records.every { ((Number) it.get("rank", Double.class)).doubleValue() > 0.0d }
    }

    def "tsRankCd returns positive score for matching documents"() {
        given:
            def tsvector = PostgresDSL.toTsvector("english", DSL.name(String, "body"))
            def tsquery = PostgresDSL.plainToTsquery("english", "database")

        when:
            def records = session.select(DSL.name(Long, "id"), PostgresDSL.tsRankCd(tsvector, tsquery).as("rank"))
                .from("fts_articles")
                .where(tsvector.matches(tsquery))
                .fetchAll()

        then:
            !records.isEmpty()
            records.every { ((Number) it.get("rank", Double.class)).doubleValue() > 0.0d }
    }

    def "tsRank can order results by relevance"() {
        given:
            def tsvector = PostgresDSL.toTsvector("english", DSL.name(String, "body"))
            def tsquery = PostgresDSL.plainToTsquery("english", "PostgreSQL database")

        when:
            def records = session.select(DSL.name(Long, "id"), DSL.name(String, "title"), PostgresDSL.tsRank(tsvector, tsquery).as("rank"))
                .from("fts_articles")
                .where(tsvector.matches(tsquery))
                .orderBy(PostgresDSL.tsRank(tsvector, tsquery).desc())
                .fetchAll()

        then:
            records.size() >= 1
    }

    def "toTsvector config with single quote does not cause SQL injection"() {
        given:
            def tsvector = PostgresDSL.toTsvector("english'", DSL.name(String, "body"))
            def tsquery = PostgresDSL.plainToTsquery("english", "database")

        when:
            session.select(DSL.name(Long, "id"))
                .from("fts_articles")
                .where(tsvector.matches(tsquery))
                .fetchAll()

        then:
            def ex = thrown(Exception)
            // Must be a "text search configuration not found" error, NOT a SQL syntax error
            ex.message.contains("text search configuration")
    }

    def "tsquery config with single quote does not cause SQL injection"() {
        given:
            def tsvector = PostgresDSL.toTsvector("english", DSL.name(String, "body"))
            def tsquery = PostgresDSL.plainToTsquery("english'", "database")

        when:
            session.select(DSL.name(Long, "id"))
                .from("fts_articles")
                .where(tsvector.matches(tsquery))
                .fetchAll()

        then:
            def ex = thrown(Exception)
            // Must be a "text search configuration not found" error, NOT a SQL syntax error
            ex.message.contains("text search configuration")
    }
}
