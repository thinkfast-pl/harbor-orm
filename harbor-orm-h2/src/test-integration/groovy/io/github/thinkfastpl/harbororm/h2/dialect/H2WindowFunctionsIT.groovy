// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.WindowExpression
import io.github.thinkfastpl.harbororm.api.query.result.Record

class H2WindowFunctionsIT extends H2DialectBaseIT {

    def setup() {
        loadScript("window-functions-test.sql")
    }

    def cleanup() {
        dropAllObjects()
    }

    def "ROW_NUMBER returns sequential numbers"() {
        given:
            WindowExpression<Long> rowNum = DSL.rowNumber().orderBy(DSL.asc("salary"))

        when:
            List<Record> result = session.select(DSL.name("id"), rowNum)
                .from("employees")
                .orderBy(DSL.asc("salary"))
                .fetchAll()

        then:
            result.size() == 6
            result.collect { r -> r.get(rowNum) } == [1L, 2L, 3L, 4L, 5L, 6L]
    }

    def "RANK returns rank with gaps"() {
        given:
            WindowExpression<Long> rankExpr = DSL.rank().orderBy(DSL.asc("department"))

        when:
            List<Record> result = session.select(DSL.name("id"), rankExpr)
                .from("employees")
                .fetchAll()

        then:
            result.size() == 6
            result.collect { r -> r.get(rankExpr) }.sort() == [1L, 1L, 1L, 4L, 4L, 4L]
    }

    def "DENSE_RANK returns rank without gaps"() {
        given:
            WindowExpression<Long> denseRankExpr = DSL.denseRank().orderBy(DSL.asc("department"))

        when:
            List<Record> result = session.select(DSL.name("id"), denseRankExpr)
                .from("employees")
                .fetchAll()

        then:
            result.size() == 6
            result.collect { r -> r.get(denseRankExpr) }.sort() == [1L, 1L, 1L, 2L, 2L, 2L]
    }

    def "ROW_NUMBER with PARTITION BY"() {
        given:
            WindowExpression<Long> rowNum = DSL.rowNumber()
                .partitionBy(DSL.name("department"))
                .orderBy(DSL.desc("salary"))

        when:
            List<Record> result = session.select(DSL.name("id"), rowNum)
                .from("employees")
                .fetchAll()

        then:
            result.size() == 6
            // Each department should have 1, 2, 3
            result.collect { r -> r.get(rowNum) }.sort() == [1L, 1L, 2L, 2L, 3L, 3L]
    }

    def "LAG returns previous row value"() {
        given:
            WindowExpression lagExpr = DSL.lag(DSL.name("salary")).orderBy(DSL.asc("id"))

        when:
            List<Record> result = session.select(
                DSL.name("id"),
                DSL.name("salary"),
                lagExpr
            ).from("employees").orderBy(DSL.asc("id")).fetchAll()

        then:
            result.size() == 6
            result[0].get(lagExpr) == null  // First row has no previous
    }

    def "LEAD returns next row value"() {
        given:
            WindowExpression leadExpr = DSL.lead(DSL.name("salary")).orderBy(DSL.asc("id"))

        when:
            List<Record> result = session.select(
                DSL.name("id"),
                DSL.name("salary"),
                leadExpr
            ).from("employees").orderBy(DSL.asc("id")).fetchAll()

        then:
            result.size() == 6
            result[5].get(leadExpr) == null  // Last row has no next
    }
}
