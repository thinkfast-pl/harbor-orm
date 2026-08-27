// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Order
import io.github.thinkfastpl.harbororm.api.query.result.Record

class PortableFunctionExpressionWindowIT extends AbstractHarborIT {

    def salaryExpr = DSL.name(BigDecimal, "salary")

    def "percentileCont computes interpolated median"() {
        when:
            def results = session.select(DSL.percentileCont(0.5, new Order(salaryExpr, true)))
                    .from("employees")
                    .fetchAll()

        then:
            // window function: one value per input row, all equal to the global median
            results.size() == 9
            results.every { (it as Number).doubleValue() == 75000.0d }

        where:
            session << getSessionsExcept(DbType.POSTGRES, DbType.MYSQL)
    }

    def "percentileCont computes quartile"() {
        when:
            def results = session.select(DSL.percentileCont(0.25, new Order(salaryExpr, true)))
                    .from("employees")
                    .fetchAll()

        then:
            results.every { (it as Number).doubleValue() == 65000.0d }

        where:
            session << getSessionsExcept(DbType.POSTGRES, DbType.MYSQL)
    }

    def "percentileDisc returns actual value from dataset"() {
        when:
            def results = session.select(DSL.percentileDisc(0.5, new Order(salaryExpr, true)))
                    .from("employees")
                    .fetchAll()

        then:
            results.every { (it as Number).doubleValue() == 75000.0d }

        where:
            session << getSessionsExcept(DbType.POSTGRES, DbType.MYSQL)
    }

    def "percentileDisc with descending order"() {
        when:
            def results = session.select(DSL.percentileDisc(0.25, new Order(salaryExpr, false)))
                    .from("employees")
                    .fetchAll()

        then:
            // sorted desc: 200000, 150000, 120000, ... -> 0.25 -> 3rd value
            results.every { (it as Number).doubleValue() == 120000.0d }

        where:
            session << getSessionsExcept(DbType.POSTGRES, DbType.MYSQL)
    }

    def "percentileCont accepts expression fraction"() {
        when:
            def results = session.select(DSL.percentileCont(DSL.constant(0.5d), new Order(salaryExpr, true)))
                    .from("employees")
                    .fetchAll()

        then:
            results.every { (it as Number).doubleValue() == 75000.0d }

        where:
            session << getSessionsExcept(DbType.POSTGRES, DbType.MYSQL)
    }

    def "percentileCont with PARTITION BY computes per-department median"() {
        given:
            def deptExpr = DSL.name(String, "department")
            def median = DSL.percentileCont(0.5, new Order(salaryExpr, true))
                    .partitionBy(deptExpr)
                    .as("median")

            def expectedByDept = [
                    "Executive"  : 200000.0d,
                    "Engineering": 77500.0d,   // (75000 + 80000) / 2
                    "Sales"      : 62500.0d    // (60000 + 65000) / 2
            ]

        when:
            List<Record> records = session.select(deptExpr.as("dept"), median)
                    .from("employees")
                    .fetchAll()

        then:
            records.size() == 9
            records.every { r ->
                double expected = expectedByDept.getOrDefault(r.get("dept"), -1.0d)
                (r.get("median") as Number).doubleValue() == expected
            }

        where:
            session << getSessionsExcept(DbType.POSTGRES, DbType.MYSQL)
    }

    def "percentileCont throws UnsupportedOperationException on PostgreSQL"() {
        when:
            session.select(DSL.percentileCont(0.5, new Order(salaryExpr, true)))
                    .from("employees")
                    .fetchAll()

        then:
            thrown(UnsupportedOperationException)

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "percentileDisc throws UnsupportedOperationException on PostgreSQL"() {
        when:
            session.select(DSL.percentileDisc(0.5, new Order(salaryExpr, true)))
                    .from("employees")
                    .fetchAll()

        then:
            thrown(UnsupportedOperationException)

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }
}
