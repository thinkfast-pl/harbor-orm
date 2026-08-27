// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.DefaultWindowExpression
import io.github.thinkfastpl.harbororm.api.expression.FrameBound
import io.github.thinkfastpl.harbororm.api.expression.WindowExpression
import io.github.thinkfastpl.harbororm.api.query.result.Record

/**
 * Integration tests for window function frame bounds (ROWS BETWEEN, RANGE BETWEEN).
 * Tests the FrameBound class which provides frame boundary specifications for window functions.
 *
 * <p>Uses the employees table with 9 employees ordered by salary:
 * <ul>
 *   <li>Frank (55000), Diana (60000), Eve (65000), Charlie (70000), Alice (75000)</li>
 *   <li>Bob (80000), Sales VP (120000), CTO (150000), CEO (200000)</li>
 * </ul>
 */
class WindowFrameBoundsIT extends AbstractHarborIT {

    // Helper to create a window SUM expression with frame bounds
    private static WindowExpression<BigDecimal> sumOver(String columnName) {
        return new DefaultWindowExpression<>(
                DSL.sum(DSL.name(BigDecimal.class, columnName)),
                BigDecimal.class
        )
    }

    // Helper to create a window AVG expression with frame bounds
    private static WindowExpression<BigDecimal> avgOver(String columnName) {
        return new DefaultWindowExpression<>(
                DSL.avg(DSL.name(BigDecimal.class, columnName)),
                BigDecimal.class
        )
    }

    // Helper to create a window COUNT expression with frame bounds
    // Note: COUNT(*) in window context needs to use DSL.count() which returns LiteralExpression
    private static WindowExpression<Long> countOver() {
        return new DefaultWindowExpression<>(DSL.count(), Long.class)
    }

    def "ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW computes running total"() {
        given:
            WindowExpression<BigDecimal> runningTotal = sumOver("salary")
                .orderBy(DSL.asc("salary"))
                .rowsBetween(FrameBound.UNBOUNDED_PRECEDING, FrameBound.CURRENT_ROW)

        when:
            List<Record> result = session.select(
                    DSL.name("name"),
                    DSL.name("salary"),
                    runningTotal
            ).from("employees")
             .orderBy(DSL.asc("salary"))
             .fetchAll()

        then:
            result.size() == 9

            // Running total: cumulative sum from first row to current
            // Salaries ordered: 55000, 60000, 65000, 70000, 75000, 80000, 120000, 150000, 200000
            result[0].get(runningTotal) == 55000.0  // 55000
            result[1].get(runningTotal) == 115000.0 // 55000 + 60000
            result[2].get(runningTotal) == 180000.0 // + 65000
            result[3].get(runningTotal) == 250000.0 // + 70000
            result[4].get(runningTotal) == 325000.0 // + 75000
            result[5].get(runningTotal) == 405000.0 // + 80000
            result[6].get(runningTotal) == 525000.0 // + 120000
            result[7].get(runningTotal) == 675000.0 // + 150000
            result[8].get(runningTotal) == 875000.0 // + 200000 (total of all salaries)

        where:
            session << allSessions
    }

    def "ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING computes total for all rows"() {
        given:
            WindowExpression<BigDecimal> totalSum = sumOver("salary")
                .orderBy(DSL.asc("salary"))
                .rowsBetween(FrameBound.UNBOUNDED_PRECEDING, FrameBound.UNBOUNDED_FOLLOWING)

        when:
            List<Record> result = session.select(
                    DSL.name("name"),
                    DSL.name("salary"),
                    totalSum
            ).from("employees")
             .orderBy(DSL.asc("salary"))
             .fetchAll()

        then:
            result.size() == 9
            // Every row should have the total of all salaries
            BigDecimal totalSalary = 875000.0 // 55000+60000+65000+70000+75000+80000+120000+150000+200000
            result.every { r -> r.get(totalSum) == totalSalary }

        where:
            session << allSessions
    }

    def "ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING computes 3-row moving window"() {
        given:
            WindowExpression<BigDecimal> movingSum = sumOver("salary")
                .orderBy(DSL.asc("salary"))
                .rowsBetween(FrameBound.preceding(1), FrameBound.following(1))

        when:
            List<Record> result = session.select(
                    DSL.name("name"),
                    DSL.name("salary"),
                    movingSum
            ).from("employees")
             .orderBy(DSL.asc("salary"))
             .fetchAll()

        then:
            result.size() == 9

            // Salaries ordered: 55000, 60000, 65000, 70000, 75000, 80000, 120000, 150000, 200000
            // For first row: only current + next (55000 + 60000)
            result[0].get(movingSum) == 115000.0

            // Middle rows: prev + current + next
            result[1].get(movingSum) == 180000.0  // 55000 + 60000 + 65000
            result[4].get(movingSum) == 225000.0  // 70000 + 75000 + 80000

            // Last row: only prev + current (150000 + 200000)
            result[8].get(movingSum) == 350000.0

        where:
            session << allSessions
    }

    def "ROWS BETWEEN 2 PRECEDING AND CURRENT ROW computes trailing 3-row sum"() {
        given:
            WindowExpression<BigDecimal> trailingSum = sumOver("salary")
                .orderBy(DSL.asc("salary"))
                .rowsBetween(FrameBound.preceding(2), FrameBound.CURRENT_ROW)

        when:
            List<Record> result = session.select(
                    DSL.name("name"),
                    DSL.name("salary"),
                    trailingSum
            ).from("employees")
             .orderBy(DSL.asc("salary"))
             .fetchAll()

        then:
            result.size() == 9

            // Salaries ordered: 55000, 60000, 65000, 70000, 75000, 80000, 120000, 150000, 200000
            result[0].get(trailingSum) == 55000.0              // only current (first row)
            result[1].get(trailingSum) == 115000.0             // 55000 + 60000 (first 2 rows)
            result[2].get(trailingSum) == 180000.0             // 55000 + 60000 + 65000
            result[3].get(trailingSum) == 195000.0             // 60000 + 65000 + 70000
            result[8].get(trailingSum) == 470000.0             // 120000 + 150000 + 200000

        where:
            session << allSessions
    }

    def "ROWS BETWEEN CURRENT ROW AND 2 FOLLOWING computes leading 3-row sum"() {
        given:
            WindowExpression<BigDecimal> leadingSum = sumOver("salary")
                .orderBy(DSL.asc("salary"))
                .rowsBetween(FrameBound.CURRENT_ROW, FrameBound.following(2))

        when:
            List<Record> result = session.select(
                    DSL.name("name"),
                    DSL.name("salary"),
                    leadingSum
            ).from("employees")
             .orderBy(DSL.asc("salary"))
             .fetchAll()

        then:
            result.size() == 9

            // Salaries ordered: 55000, 60000, 65000, 70000, 75000, 80000, 120000, 150000, 200000
            result[0].get(leadingSum) == 180000.0 // 55000 + 60000 + 65000
            result[6].get(leadingSum) == 470000.0 // 120000 + 150000 + 200000
            result[7].get(leadingSum) == 350000.0 // 150000 + 200000 (only 2 rows left)
            result[8].get(leadingSum) == 200000.0 // only current (last row)

        where:
            session << allSessions
    }

    def "RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW computes running total"() {
        given:
            WindowExpression<BigDecimal> rangeRunningTotal = sumOver("salary")
                .orderBy(DSL.asc("salary"))
                .rangeBetween(FrameBound.UNBOUNDED_PRECEDING, FrameBound.CURRENT_ROW)

        when:
            List<Record> result = session.select(
                    DSL.name("name"),
                    DSL.name("salary"),
                    rangeRunningTotal
            ).from("employees")
             .orderBy(DSL.asc("salary"))
             .fetchAll()

        then:
            result.size() == 9

            // For RANGE with unique salary values, behaves like ROWS
            // Salaries ordered: 55000, 60000, 65000, 70000, 75000, 80000, 120000, 150000, 200000
            result[0].get(rangeRunningTotal) == 55000.0
            result[8].get(rangeRunningTotal) == 875000.0

        where:
            session << allSessions
    }

    def "AVG with ROWS frame computes moving average"() {
        given:
            WindowExpression<BigDecimal> movingAvg = avgOver("salary")
                .orderBy(DSL.asc("salary"))
                .rowsBetween(FrameBound.preceding(2), FrameBound.CURRENT_ROW)

        when:
            List<Record> result = session.select(
                    DSL.name("name"),
                    DSL.name("salary"),
                    movingAvg
            ).from("employees")
             .orderBy(DSL.asc("salary"))
             .fetchAll()

        then:
            result.size() == 9

            // Salaries ordered: 55000, 60000, 65000, 70000, 75000, 80000, 120000, 150000, 200000
            // First row: avg of 1 row = 55000
            // Third row onwards: avg of 3 rows
            with(result[0].get(movingAvg)) { avg ->
                avg == 55000.0 || avg == 55000
            }
            with(result[2].get(movingAvg)) { avg ->
                // (55000 + 60000 + 65000) / 3 = 60000
                avg == 60000.0 || avg == 60000
            }

        where:
            session << allSessions
    }

    def "COUNT with ROWS frame computes sliding window count"() {
        given:
            WindowExpression<Long> slidingCount = countOver()
                .orderBy(DSL.asc("salary"))
                .rowsBetween(FrameBound.preceding(1), FrameBound.following(1))

        when:
            List<Record> result = session.select(
                    DSL.name("name"),
                    DSL.name("salary"),
                    slidingCount
            ).from("employees")
             .orderBy(DSL.asc("salary"))
             .fetchAll()

        then:
            result.size() == 9

            // First row: current + 1 following = 2
            result[0].get(slidingCount) == 2L
            // Middle rows: 1 preceding + current + 1 following = 3
            result[4].get(slidingCount) == 3L
            // Last row: 1 preceding + current = 2
            result[8].get(slidingCount) == 2L

        where:
            session << allSessions
    }

    def "frame bounds with PARTITION BY"() {
        given:
            def departmentExpr = DSL.name("department")
            def salaryExpr = DSL.name("salary")
            def nameExpr = DSL.name("name")

            WindowExpression<BigDecimal> runningTotalByDept = sumOver("salary")
                .partitionBy(departmentExpr)
                .orderBy(DSL.asc("salary"))
                .rowsBetween(FrameBound.UNBOUNDED_PRECEDING, FrameBound.CURRENT_ROW)

        when:
            List<Record> result = session.select(
                    nameExpr,
                    departmentExpr,
                    salaryExpr,
                    runningTotalByDept
            ).from("employees")
             .orderBy(DSL.asc("department"), DSL.asc("salary"))
             .fetchAll()

        then:
            result.size() == 9

            // Engineering department: CTO (150000), Charlie (70000), Alice (75000), Bob (80000)
            // Ordered by salary within partition: 70000, 75000, 80000, 150000
            // Running totals: 70000, 145000, 225000, 375000

            // Sales department: Sales VP (120000), Frank (55000), Diana (60000), Eve (65000)
            // Ordered by salary within partition: 55000, 60000, 65000, 120000
            // Running totals: 55000, 115000, 180000, 300000

            // Executive: CEO (200000) - single row, running total = 200000

            // Check that running totals reset per partition
            // Find all Engineering rows and check their running totals
            def engineeringRows = result.findAll { it.get(departmentExpr) == "Engineering" }
            engineeringRows.size() == 4
            engineeringRows[-1].get(runningTotalByDept) == 375000.0 // Total for Engineering dept

            def salesRows = result.findAll { it.get(departmentExpr) == "Sales" }
            salesRows.size() == 4
            salesRows[-1].get(runningTotalByDept) == 300000.0 // Total for Sales dept

        where:
            session << allSessions
    }

    def "FrameBound.preceding validates non-negative offset"() {
        when:
            FrameBound.preceding(-1)

        then:
            thrown(IllegalArgumentException)
    }

    def "FrameBound.following validates non-negative offset"() {
        when:
            FrameBound.following(-1)

        then:
            thrown(IllegalArgumentException)
    }

    def "FrameBound.preceding with zero offset"() {
        given:
            FrameBound bound = FrameBound.preceding(0)

        expect:
            bound.toSql() == "0 PRECEDING"
    }

    def "FrameBound.following with zero offset"() {
        given:
            FrameBound bound = FrameBound.following(0)

        expect:
            bound.toSql() == "0 FOLLOWING"
    }

    def "FrameBound static constants have correct SQL"() {
        expect:
            FrameBound.UNBOUNDED_PRECEDING.toSql() == "UNBOUNDED PRECEDING"
            FrameBound.CURRENT_ROW.toSql() == "CURRENT ROW"
            FrameBound.UNBOUNDED_FOLLOWING.toSql() == "UNBOUNDED FOLLOWING"
    }

    def "FrameBound.preceding generates correct SQL"() {
        expect:
            FrameBound.preceding(5).toSql() == "5 PRECEDING"
            FrameBound.preceding(10).toSql() == "10 PRECEDING"
    }

    def "FrameBound.following generates correct SQL"() {
        expect:
            FrameBound.following(3).toSql() == "3 FOLLOWING"
            FrameBound.following(7).toSql() == "7 FOLLOWING"
    }
}
