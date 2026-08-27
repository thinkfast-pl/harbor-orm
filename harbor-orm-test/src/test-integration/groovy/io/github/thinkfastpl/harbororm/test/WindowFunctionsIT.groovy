// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.FrameBound
import io.github.thinkfastpl.harbororm.api.expression.WindowExpression
import io.github.thinkfastpl.harbororm.api.query.result.Record

class WindowFunctionsIT extends AbstractHarborIT {

    def "ROW_NUMBER returns sequential numbers"() {
        given:
            WindowExpression<Long> rowNum = DSL.rowNumber().orderBy(DSL.asc("salary"))

        when:
            List<Record> result = session.select(DSL.name("id"), rowNum)
                .from("employees")
                .orderBy(DSL.asc("salary"))
                .fetchAll()

        then:
            // 9 employees in the hierarchy: CEO, CTO, Sales VP, Alice, Bob, Charlie, Diana, Eve, Frank
            result.size() == 9
            result.collect { r -> r.get(rowNum) } == [1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L]

        where:
            session << allSessions
    }

    def "RANK returns rank with gaps"() {
        given:
            WindowExpression<Long> rankExpr = DSL.rank().orderBy(DSL.asc("department"))

        when:
            List<Record> result = session.select(DSL.name("id"), rankExpr)
                .from("employees")
                .fetchAll()

        then:
            // 9 employees in the hierarchy
            result.size() == 9

        where:
            session << allSessions
    }

    def "DENSE_RANK returns rank without gaps"() {
        given:
            WindowExpression<Long> denseRankExpr = DSL.denseRank().orderBy(DSL.asc("department"))

        when:
            List<Record> result = session.select(DSL.name("id"), denseRankExpr)
                .from("employees")
                .fetchAll()

        then:
            // 9 employees in the hierarchy
            result.size() == 9

        where:
            session << allSessions
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
            // 9 employees: 1 Executive (CEO), 4 Engineering (CTO, Alice, Bob, Charlie), 4 Sales (Sales VP, Diana, Eve, Frank)
            result.size() == 9
            // Partition sizes: Executive=1, Engineering=4, Sales=4
            // Row numbers within each partition: Executive [1], Engineering [1,2,3,4], Sales [1,2,3,4]
            result.collect { r -> r.get(rowNum) }.sort() == [1L, 1L, 1L, 2L, 2L, 3L, 3L, 4L, 4L]

        where:
            session << allSessions
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
            // 9 employees in the hierarchy
            result.size() == 9
            result[0].get(lagExpr) == null

        where:
            session << allSessions
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
            // 9 employees in the hierarchy
            result.size() == 9
            result[8].get(leadExpr) == null

        where:
            session << allSessions
    }

    def "LAG preserves column Java type (H-13, L-28)"() {
        given:
            WindowExpression<BigDecimal> lagExpr = DSL.lag(DSL.name(BigDecimal.class, "salary")).orderBy(DSL.asc("id"))

        when:
            List<Record> result = session.select(DSL.name("id"), lagExpr)
                .from("employees").orderBy(DSL.asc("id")).fetchAll()

        then:
            lagExpr.getJavaType() == BigDecimal.class
            result.size() == 9
            result[0].get(lagExpr) == null
            (result[1].get(lagExpr) instanceof BigDecimal)

        where:
            session << allSessions
    }

    def "LEAD preserves column Java type (H-13, L-28)"() {
        given:
            WindowExpression<BigDecimal> leadExpr = DSL.lead(DSL.name(BigDecimal.class, "salary")).orderBy(DSL.asc("id"))

        when:
            List<Record> result = session.select(DSL.name("id"), leadExpr)
                .from("employees").orderBy(DSL.asc("id")).fetchAll()

        then:
            leadExpr.getJavaType() == BigDecimal.class
            result.size() == 9
            result[8].get(leadExpr) == null
            (result[0].get(leadExpr) instanceof BigDecimal)

        where:
            session << allSessions
    }

    def "NTILE divides ordered rows into buckets"() {
        given:
            WindowExpression<Integer> ntileExpr = DSL.ntile(3).orderBy(DSL.asc("salary"))

        when:
            List<Record> result = session.select(DSL.name("id"), ntileExpr)
                .from("employees")
                .orderBy(DSL.asc("salary"))
                .fetchAll()

        then:
            // 9 rows / 3 buckets = 3 rows per bucket: [1,1,1,2,2,2,3,3,3]
            result.size() == 9
            result.collect { r -> r.get(ntileExpr) } == [1, 1, 1, 2, 2, 2, 3, 3, 3]

        where:
            session << allSessions
    }

    def "PERCENT_RANK returns relative rank between 0 and 1"() {
        given:
            WindowExpression<Double> percentRankExpr = DSL.percentRank().orderBy(DSL.asc("salary"))

        when:
            List<Record> result = session.select(DSL.name("id"), percentRankExpr)
                .from("employees")
                .orderBy(DSL.asc("salary"))
                .fetchAll()

        then:
            result.size() == 9
            // With 9 distinct salaries, percent_rank = (rank - 1) / (9 - 1)
            // First row: 0.0, last row: 1.0
            result[0].get(percentRankExpr) == 0.0d
            result[8].get(percentRankExpr) == 1.0d

        where:
            session << allSessions
    }

    def "CUME_DIST returns cumulative distribution"() {
        given:
            WindowExpression<Double> cumeDistExpr = DSL.cumeDist().orderBy(DSL.asc("salary"))

        when:
            List<Record> result = session.select(DSL.name("id"), cumeDistExpr)
                .from("employees")
                .orderBy(DSL.asc("salary"))
                .fetchAll()

        then:
            result.size() == 9
            // With 9 distinct salaries: first row has 1/9, last row has 9/9 = 1.0
            Math.abs(result[0].get(cumeDistExpr) - (1.0d / 9.0d)) < 0.0001d
            result[8].get(cumeDistExpr) == 1.0d

        where:
            session << allSessions
    }

    def "FIRST_VALUE returns first value of partition window"() {
        given:
            def deptExpr = DSL.name("department")
            WindowExpression<BigDecimal> firstSalary = DSL.firstValue(DSL.name(BigDecimal.class, "salary"))
                .partitionBy(deptExpr)
                .orderBy(DSL.asc("salary"))

        when:
            List<Record> result = session.select(deptExpr, firstSalary)
                .from("employees")
                .fetchAll()

        then:
            result.size() == 9
            // Engineering lowest: Charlie 70000 → all Engineering rows return 70000
            def engRows = result.findAll { it.get(deptExpr) == "Engineering" }
            engRows.size() == 4
            engRows.every { it.get(firstSalary) == 70000.0g }

            // Sales lowest: Frank 55000 → all Sales rows return 55000
            def salesRows = result.findAll { it.get(deptExpr) == "Sales" }
            salesRows.size() == 4
            salesRows.every { it.get(firstSalary) == 55000.0g }

        where:
            session << allSessions
    }

    def "LAST_VALUE with full frame returns last value of partition"() {
        given:
            def deptExpr = DSL.name("department")
            WindowExpression<BigDecimal> lastSalary = DSL.lastValue(DSL.name(BigDecimal.class, "salary"))
                .partitionBy(deptExpr)
                .orderBy(DSL.asc("salary"))
                .rowsBetween(FrameBound.UNBOUNDED_PRECEDING, FrameBound.UNBOUNDED_FOLLOWING)

        when:
            List<Record> result = session.select(deptExpr, lastSalary)
                .from("employees")
                .fetchAll()

        then:
            result.size() == 9
            // Engineering highest: CTO 150000 → all Engineering rows return 150000
            def engRows = result.findAll { it.get(deptExpr) == "Engineering" }
            engRows.size() == 4
            engRows.every { it.get(lastSalary) == 150000.0g }

            // Sales highest: Sales VP 120000 → all Sales rows return 120000
            def salesRows = result.findAll { it.get(deptExpr) == "Sales" }
            salesRows.size() == 4
            salesRows.every { it.get(lastSalary) == 120000.0g }

        where:
            session << allSessions
    }

    def "NTH_VALUE returns value at given position in frame"() {
        given:
            WindowExpression<BigDecimal> secondSalary = DSL.nthValue(DSL.name(BigDecimal.class, "salary"), 2)
                .orderBy(DSL.asc("salary"))
                .rowsBetween(FrameBound.UNBOUNDED_PRECEDING, FrameBound.UNBOUNDED_FOLLOWING)

        when:
            List<Record> result = session.select(DSL.name("id"), secondSalary)
                .from("employees")
                .orderBy(DSL.asc("salary"))
                .fetchAll()

        then:
            result.size() == 9
            // 2nd lowest salary = Diana's 60000 → returned for every row when frame covers all rows
            result.every { it.get(secondSalary) == 60000.0g }

        where:
            session << allSessions
    }

    def "SUM with OVER and PARTITION BY via Expression.over()"() {
        given:
            def nameExpr = DSL.name("name")
            def deptExpr = DSL.name("department")
            def salaryExpr = DSL.name("salary")
            def sumExpr = DSL.sum(DSL.name(BigDecimal.class, "salary"))
                .over()
                .partitionBy(DSL.name("department"))
                .orderBy(DSL.asc("salary"))

        when:
            List<Record> result = session.select(
                nameExpr,
                deptExpr,
                salaryExpr,
                sumExpr
            ).from("employees")
             .orderBy(DSL.asc("department"), DSL.asc("salary"))
             .fetchAll()

        then:
            result.size() == 9

            // Engineering: 70000, 75000, 80000, 150000 → running totals: 70000, 145000, 225000, 375000
            def engRows = result.findAll { it.get(deptExpr) == "Engineering" }
            engRows.size() == 4
            engRows[-1].get(sumExpr) == 375000.0

            // Sales: 55000, 60000, 65000, 120000 → running totals: 55000, 115000, 180000, 300000
            def salesRows = result.findAll { it.get(deptExpr) == "Sales" }
            salesRows.size() == 4
            salesRows[-1].get(sumExpr) == 300000.0

        where:
            session << allSessions
    }
}
