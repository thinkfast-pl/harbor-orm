// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class GroupByHavingIT extends AbstractHarborIT {

    private void setupTestData(TestFixtures fixtures) {
        // Create test data for grouping scenarios:
        // Group "A": 3 records with numero 10, 20, 30 (count=3, sum=60, avg=20)
        // Group "B": 2 records with numero 15, 25 (count=2, sum=40, avg=20)
        // Group "C": 1 record with numero 100 (count=1, sum=100, avg=100)
        fixtures.addBasic(1L, "A", 10)
        fixtures.addBasic(2L, "A", 20)
        fixtures.addBasic(3L, "A", 30)
        fixtures.addBasic(4L, "B", 15)
        fixtures.addBasic(5L, "B", 25)
        fixtures.addBasic(6L, "C", 100)
    }

    // ============================================
    // GROUP BY single column tests
    // ============================================

    def "GROUP BY single column with COUNT(*) counts records per group"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)
            Expression<Long> countExpr = DSL.count()

        when:
            List<Record> records = session.select(basics.name, countExpr)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(countExpr) == 3L
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(countExpr) == 2L
            }
            with(records[2]) { r ->
                r.get(basics.name) == "C"
                r.get(countExpr) == 1L
            }

        where:
            session << allSessions
    }

    def "GROUP BY single column with SUM() calculates sum per group"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> sumExpr = DSL.sum(basics.numero)

        when:
            List<Record> records = session.select(basics.name, sumExpr)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(sumExpr) == 60
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(sumExpr) == 40
            }
            with(records[2]) { r ->
                r.get(basics.name) == "C"
                r.get(sumExpr) == 100
            }

        where:
            session << allSessions
    }

    def "GROUP BY single column with AVG() calculates average per group"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)
            Expression<Double> avgExpr = DSL.avg(basics.numero)

        when:
            List<Record> records = session.select(basics.name, avgExpr)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(avgExpr) == 20.0d
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(avgExpr) == 20.0d
            }
            with(records[2]) { r ->
                r.get(basics.name) == "C"
                r.get(avgExpr) == 100.0d
            }

        where:
            session << allSessions
    }

    def "GROUP BY single column with MIN() finds minimum per group"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> minExpr = DSL.min(basics.numero)

        when:
            List<Record> records = session.select(basics.name, minExpr)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(minExpr) == 10
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(minExpr) == 15
            }
            with(records[2]) { r ->
                r.get(basics.name) == "C"
                r.get(minExpr) == 100
            }

        where:
            session << allSessions
    }

    def "GROUP BY single column with MAX() finds maximum per group"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> maxExpr = DSL.max(basics.numero)

        when:
            List<Record> records = session.select(basics.name, maxExpr)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(maxExpr) == 30
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(maxExpr) == 25
            }
            with(records[2]) { r ->
                r.get(basics.name) == "C"
                r.get(maxExpr) == 100
            }

        where:
            session << allSessions
    }

    // ============================================
    // GROUP BY with HAVING filter tests
    // ============================================

    def "GROUP BY with HAVING filters groups by COUNT() > threshold"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)
            Expression<Long> countExpr = DSL.count()

        when: "select only groups with more than 1 record"
            List<Record> records = session.select(basics.name, countExpr)
                    .from(basics)
                    .groupBy(basics.name)
                    .having(countExpr.gt(1L))
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(countExpr) == 3L
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(countExpr) == 2L
            }

        where:
            session << allSessions
    }

    def "GROUP BY with HAVING filters groups by SUM() >= threshold"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> sumExpr = DSL.sum(basics.numero)

        when: "select only groups with sum >= 50"
            List<Record> records = session.select(basics.name, sumExpr)
                    .from(basics)
                    .groupBy(basics.name)
                    .having(sumExpr.ge(50))
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(sumExpr) == 60
            }
            with(records[1]) { r ->
                r.get(basics.name) == "C"
                r.get(sumExpr) == 100
            }

        where:
            session << allSessions
    }

    def "GROUP BY with HAVING filters groups by AVG() > threshold"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)
            Expression<Double> avgExpr = DSL.avg(basics.numero)

        when: "select only groups with average > 50"
            List<Record> records = session.select(basics.name, avgExpr)
                    .from(basics)
                    .groupBy(basics.name)
                    .having(avgExpr.gt(50.0d))
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.name) == "C"
                r.get(avgExpr) == 100.0d
            }

        where:
            session << allSessions
    }

    def "GROUP BY with HAVING filters groups by MIN() < threshold"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> minExpr = DSL.min(basics.numero)

        when: "select only groups with minimum < 20"
            List<Record> records = session.select(basics.name, minExpr)
                    .from(basics)
                    .groupBy(basics.name)
                    .having(minExpr.lt(20))
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(minExpr) == 10
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(minExpr) == 15
            }

        where:
            session << allSessions
    }

    def "GROUP BY with HAVING filters groups by MAX() == value"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> maxExpr = DSL.max(basics.numero)

        when: "select only groups with maximum == 100"
            List<Record> records = session.select(basics.name, maxExpr)
                    .from(basics)
                    .groupBy(basics.name)
                    .having(maxExpr.eq(100))
                    .fetchAll()

        then:
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.name) == "C"
                r.get(maxExpr) == 100
            }

        where:
            session << allSessions
    }

    // ============================================
    // GROUP BY multiple columns tests
    // ============================================

    def "GROUP BY multiple columns groups by combination of values"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            // Create data with same name but different numero to test multi-column grouping
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "A", 10)
            fixtures.addBasic(3L, "A", 20)
            fixtures.addBasic(4L, "B", 10)

            BasicsTable basics = new BasicsTable(null)
            Expression<Long> countExpr = DSL.count()

        when:
            List<Record> records = session.select(basics.name, basics.numero, countExpr)
                    .from(basics)
                    .groupBy(basics.name, basics.numero)
                    .orderBy(basics.name.asc(), basics.numero.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(basics.numero) == 10
                r.get(countExpr) == 2L
            }
            with(records[1]) { r ->
                r.get(basics.name) == "A"
                r.get(basics.numero) == 20
                r.get(countExpr) == 1L
            }
            with(records[2]) { r ->
                r.get(basics.name) == "B"
                r.get(basics.numero) == 10
                r.get(countExpr) == 1L
            }

        where:
            session << allSessions
    }

    def "GROUP BY multiple columns with HAVING filters combined groups"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "A", 10)
            fixtures.addBasic(3L, "A", 20)
            fixtures.addBasic(4L, "B", 10)

            BasicsTable basics = new BasicsTable(null)
            Expression<Long> countExpr = DSL.count()

        when: "select only (name, numero) groups with count > 1"
            List<Record> records = session.select(basics.name, basics.numero, countExpr)
                    .from(basics)
                    .groupBy(basics.name, basics.numero)
                    .having(countExpr.gt(1L))
                    .fetchAll()

        then:
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(basics.numero) == 10
                r.get(countExpr) == 2L
            }

        where:
            session << allSessions
    }

    // ============================================
    // Combined scenarios
    // ============================================

    def "GROUP BY with multiple aggregates in SELECT"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)
            Expression<Long> countExpr = DSL.count()
            Expression<Integer> sumExpr = DSL.sum(basics.numero)
            Expression<Double> avgExpr = DSL.avg(basics.numero)
            Expression<Integer> minExpr = DSL.min(basics.numero)
            Expression<Integer> maxExpr = DSL.max(basics.numero)

        when:
            List<Record> records = session.select(basics.name, countExpr, sumExpr, avgExpr, minExpr, maxExpr)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(countExpr) == 3L
                r.get(sumExpr) == 60
                r.get(avgExpr) == 20.0d
                r.get(minExpr) == 10
                r.get(maxExpr) == 30
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(countExpr) == 2L
                r.get(sumExpr) == 40
                r.get(avgExpr) == 20.0d
                r.get(minExpr) == 15
                r.get(maxExpr) == 25
            }
            with(records[2]) { r ->
                r.get(basics.name) == "C"
                r.get(countExpr) == 1L
                r.get(sumExpr) == 100
                r.get(avgExpr) == 100.0d
                r.get(minExpr) == 100
                r.get(maxExpr) == 100
            }

        where:
            session << allSessions
    }

    def "GROUP BY with WHERE clause filters before grouping"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)
            Expression<Long> countExpr = DSL.count()
            Expression<Integer> sumExpr = DSL.sum(basics.numero)

        when: "filter records with numero > 15, then group"
            List<Record> records = session.select(basics.name, countExpr, sumExpr)
                    .from(basics)
                    .where(basics.numero.gt(15))
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // After filtering numero > 15:
            // "A": records with numero 20, 30 (count=2, sum=50)
            // "B": record with numero 25 (count=1, sum=25)
            // "C": record with numero 100 (count=1, sum=100)
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(countExpr) == 2L
                r.get(sumExpr) == 50
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(countExpr) == 1L
                r.get(sumExpr) == 25
            }
            with(records[2]) { r ->
                r.get(basics.name) == "C"
                r.get(countExpr) == 1L
                r.get(sumExpr) == 100
            }

        where:
            session << allSessions
    }

    def "GROUP BY with WHERE and HAVING combines pre and post group filtering"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)
            Expression<Long> countExpr = DSL.count()
            Expression<Integer> sumExpr = DSL.sum(basics.numero)

        when: "filter records with numero > 15, group, then filter groups with count > 1"
            List<Record> records = session.select(basics.name, countExpr, sumExpr)
                    .from(basics)
                    .where(basics.numero.gt(15))
                    .groupBy(basics.name)
                    .having(countExpr.gt(1L))
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // After WHERE numero > 15 and HAVING count > 1:
            // Only "A" has count=2 (records with numero 20, 30), sum=50
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(countExpr) == 2L
                r.get(sumExpr) == 50
            }

        where:
            session << allSessions
    }

    def "GROUP BY with HAVING using compound condition"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupTestData(fixtures)
            BasicsTable basics = new BasicsTable(null)
            Expression<Long> countExpr = DSL.count()
            Expression<Integer> sumExpr = DSL.sum(basics.numero)

        when: "select groups where count >= 2 AND sum >= 50"
            List<Record> records = session.select(basics.name, countExpr, sumExpr)
                    .from(basics)
                    .groupBy(basics.name)
                    .having(countExpr.ge(2L).and(sumExpr.ge(50)))
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // "A": count=3, sum=60 (matches both conditions)
            // "B": count=2, sum=40 (count matches, sum doesn't)
            // "C": count=1, sum=100 (sum matches, count doesn't)
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(countExpr) == 3L
                r.get(sumExpr) == 60
            }

        where:
            session << allSessions
    }

    def "GROUP BY with COUNT DISTINCT"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            // Create data with duplicate numero values
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "A", 10)
            fixtures.addBasic(3L, "A", 20)
            fixtures.addBasic(4L, "B", 15)
            fixtures.addBasic(5L, "B", 15)

            BasicsTable basics = new BasicsTable(null)
            Expression<Long> countDistinctExpr = DSL.countDistinct(basics.numero)

        when:
            List<Record> records = session.select(basics.name, countDistinctExpr)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(countDistinctExpr) == 2L  // distinct values: 10, 20
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(countDistinctExpr) == 1L  // distinct values: 15
            }

        where:
            session << allSessions
    }
}
