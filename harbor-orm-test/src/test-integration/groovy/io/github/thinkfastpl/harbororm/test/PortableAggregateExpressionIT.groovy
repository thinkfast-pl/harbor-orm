// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import com.google.gson.Gson
import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.expression.Order
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class PortableAggregateExpressionIT extends AbstractHarborIT {

    private static final BasicsTable basics = new BasicsTable(null)

    private void setupStandardData(TestFixtures fixtures) {
        fixtures.addBasic(1L, "A", 10)
        fixtures.addBasic(2L, "A", 20)
        fixtures.addBasic(3L, "B", 30)
        fixtures.addBasic(4L, "B", 30)
        fixtures.addBasic(5L, "C", 50)
    }

    private void setupBitData(TestFixtures fixtures) {
        fixtures.addBasic(1L, "X", 12)
        fixtures.addBasic(2L, "X", 10)
    }

    private static final Gson GSON = new Gson()

    private static Object parseJson(String json) {
        return GSON.fromJson(json, Object.class)
    }

    // ============================================
    // any_value
    // ============================================

    def "anyValue() returns the single value when all rows agree"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            def result = session.select(DSL.anyValue(basics.numero))
                    .from(basics)
                    .where(basics.name.eq("B"))
                    .fetchSingle()

        then:
            result == 30

        where:
            session << allSessions
    }

    def "anyValueWhere() filters rows before picking a value"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            def result = session.select(DSL.anyValueWhere(basics.numero, basics.numero.gt(40)))
                    .from(basics)
                    .fetchSingle()

        then:
            result == 50

        where:
            session << allSessions
    }

    def "array_agg"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "A", 11)
            testFixtures.addBasic(2L, "A", 22)
            testFixtures.addBasic(3L, "B", 33)
            testFixtures.addBasic(4L, "B", 44)
            testFixtures.addBasic(5L, "B", 55)

            BasicsTable basics = new BasicsTable(null)
            Expression<Integer[]> numArray
            List<Record> records

        when:
            numArray = DSL.arrayAgg(basics.numero)
            records = session.select(basics.name, numArray)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { record ->
                record.get(basics.name) == "A"
                record.get(numArray).length == 2
            }
            with(records[1]) { record ->
                record.get(basics.name) == "B"
                record.get(numArray).length == 3
            }

        when:
            numArray = DSL.arrayAgg(basics.numero, List.of(basics.numero.asc()))
            records = session.select(basics.name, numArray)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { record ->
                record.get(basics.name) == "A"
                with(record.get(numArray)) { nums ->
                    nums.length == 2
                    nums[0] == 11
                    nums[1] == 22
                }
            }
            with(records[1]) { record ->
                record.get(basics.name) == "B"
                with(record.get(numArray)) { nums ->
                    nums.length == 3
                    nums[0] == 33
                    nums[1] == 44
                    nums[2] == 55
                }
            }

        when:
            numArray = DSL.arrayAggWhere(
                    basics.numero,
                    List.of(basics.numero.modulo(2).eq(0)),
                    List.of(basics.numero.asc())
            )

            records = session.select(basics.name, numArray)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { record ->
                record.get(basics.name) == "A"
                with(record.get(numArray)) { nums ->
                    nums.length == 1
                    nums[0] == 22
                }
            }
            with(records[1]) { record ->
                record.get(basics.name) == "B"
                with(record.get(numArray)) { nums ->
                    nums.length == 1
                    nums[0] == 44
                }
            }

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    // ============================================
    // avg
    // ============================================

    def "avg() returns the average as Double"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            def result = session.select(DSL.avg(basics.numero)).from(basics).fetchSingle()

        then:
            result == 28.0d
            result.getClass() == Double.class

        where:
            session << allSessions
    }

    def "avgDistinct() averages only distinct values"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            def result = session.select(DSL.avgDistinct(basics.numero)).from(basics).fetchSingle()

        then:
            result == 27.5d

        where:
            session << allSessions
    }

    def "avgWhere() averages only matching rows"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            def result = session.select(DSL.avgWhere(basics.numero, basics.numero.le(20)))
                    .from(basics)
                    .fetchSingle()

        then:
            result == 15.0d

        where:
            session << allSessions
    }

    def "AVG calculates average"() {
        when:
            def result = session.select(DSL.avg(DSL.name(BigDecimal, "salary")))
                    .from("employees")
                    .fetchSingle()

        then:
            result != null
            // 875000 / 9 = 97222.2222...
            Math.abs((result as Number).doubleValue() - 97222.22) < 0.01

        where:
            session << allSessions
    }

    // ============================================
    // bit aggregates
    // ============================================

    def "bit aggregate functions compute expected values"() {
        given:
            setupBitData(new TestFixtures(session))

        when:
            def result = session.select(aggregate.call(basics.numero)).from(basics).fetchSingle()

        then:
            result == expected

        where:
            session << allSessions

        combined:
            aggregate                                     || expected
            ({ e -> DSL.bitAndAgg(e) })                   || 8
            ({ e -> DSL.bitOrAgg(e) })                    || 14
            ({ e -> DSL.bitXorAgg(e) })                   || 6
            ({ e -> DSL.bitNandAgg(e) })                  || -9
            ({ e -> DSL.bitNorAgg(e) })                   || -15
            ({ e -> DSL.bitXnorAgg(e) })                  || -7
    }

    def "bit aggregate Where variants filter rows"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            setupBitData(fixtures)
            fixtures.addBasic(3L, "Y", 255)

        when:
            def result = session.select(aggregate.call(basics.numero, basics.numero.lt(100)))
                    .from(basics)
                    .fetchSingle()

        then:
            result == expected

        where:
            session << allSessions

        combined:
            aggregate                                              || expected
            ({ e, c -> DSL.bitAndAggWhere(e, c) })                 || 8
            ({ e, c -> DSL.bitOrAggWhere(e, c) })                  || 14
            ({ e, c -> DSL.bitXorAggWhere(e, c) })                 || 6
            ({ e, c -> DSL.bitNandAggWhere(e, c) })                || -9
            ({ e, c -> DSL.bitNorAggWhere(e, c) })                 || -15
            ({ e, c -> DSL.bitXnorAggWhere(e, c) })                || -7
    }

    // ============================================
    // bool_and / bool_or / every
    // ============================================

    def "boolAnd(), every() and boolOr() evaluate conditions across rows"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            def result = session.select(aggregate.call()).from(basics).fetchSingle()

        then:
            result == expected
            result.getClass() == Boolean.class

        where:
            session << allSessions

        combined:
            aggregate                                        || expected
            ({ -> DSL.boolAnd(basics.numero.gt(5)) })        || true
            ({ -> DSL.boolAnd(basics.numero.gt(10)) })       || false
            ({ -> DSL.every(basics.numero.gt(5)) })          || true
            ({ -> DSL.every(basics.numero.gt(10)) })         || false
            ({ -> DSL.boolOr(basics.numero.gt(40)) })        || true
            ({ -> DSL.boolOr(basics.numero.gt(100)) })       || false
    }

    def "boolAndWhere(), everyWhere() and boolOrWhere() filter rows first"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            def result = session.select(aggregate.call()).from(basics).fetchSingle()

        then:
            result == expected

        where:
            session << allSessions

        combined:
            aggregate                                                                      || expected
            ({ -> DSL.boolAndWhere(basics.numero.gt(15), basics.numero.ge(30)) })          || true
            ({ -> DSL.boolAndWhere(basics.numero.gt(30), basics.numero.ge(30)) })          || false
            ({ -> DSL.everyWhere(basics.numero.gt(15), basics.numero.ge(30)) })            || true
            ({ -> DSL.boolOrWhere(basics.numero.gt(40), basics.numero.ge(30)) })           || true
            ({ -> DSL.boolOrWhere(basics.numero.gt(60), basics.numero.ge(30)) })           || false
    }

    // ============================================
    // count
    // ============================================

    def "count(), count(expr), countDistinct() and Where variants"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            def result = session.select(aggregate.call()).from(basics).fetchSingle()

        then:
            result == expected
            result.getClass() == Long.class

        where:
            session << allSessions

        combined:
            aggregate                                                       || expected
            ({ -> DSL.count() })                                            || 5L
            ({ -> DSL.count(basics.numero) })                               || 5L
            ({ -> DSL.countDistinct(basics.numero) })                       || 4L
            ({ -> DSL.countWhere(basics.numero.gt(20)) })                   || 3L
            ({ -> DSL.countDistinctWhere(basics.numero, basics.numero.gt(20)) })  || 2L
    }

    // ============================================
    // group_concat
    // ============================================

    def "groupConcat() concatenates ordered values with the separator"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            def result = session.select(DSL.groupConcat(basics.numero, ",", basics.numero.asc()))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "10,20,30,30,50"
            result.getClass() == String.class

        where:
            session << allSessions
    }

    def "groupConcat() honors descending order and a custom separator"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            def result = session.select(DSL.groupConcat(basics.numero, " - ", basics.numero.desc()))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "50 - 30 - 30 - 20 - 10"

        where:
            session << allSessions
    }

    def "groupConcat() concatenates string values"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            def result = session.select(DSL.groupConcat(basics.name, ",", basics.id.asc()))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "A,A,B,B,C"

        where:
            session << allSessions
    }

    def "groupConcatDistinct() deduplicates values"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            def result = session.select(DSL.groupConcatDistinct(basics.numero, ","))
                    .from(basics)
                    .where(basics.name.eq("B"))
                    .fetchSingle()

        then:
            result == "30"

        where:
            session << allSessions
    }

    def "groupConcatWhere() filters rows before concatenating"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            def result = session.select(DSL.groupConcatWhere(basics.numero, ",", basics.numero.gt(30)))
                    .from(basics)
                    .fetchSingle()

        then:
            result == "50"

        where:
            session << allSessions
    }

    def "STRING_AGG concatenates values with delimiter"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alpha", 10)
            fixtures.addBasic(2L, "Beta", 20)
            fixtures.addBasic(3L, "Gamma", 30)

            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(
                    DSL.stringAgg(basics.name, DSL.constant(","), new Order(basics.name, true))
                            .as("names")
            )
                    .from(basics)
                    .fetchSingle()

        then:
            result == "Alpha,Beta,Gamma"

        where:
            session << allSessions
    }

    def "STRING_AGG with descending ORDER BY reverses concatenation order"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "X", 30)
            fixtures.addBasic(2L, "Y", 10)
            fixtures.addBasic(3L, "Z", 20)

            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(
                    DSL.stringAgg(basics.name, DSL.constant(" | "), new Order(basics.name, false))
                            .as("names")
            )
                    .from(basics)
                    .fetchSingle()

        then:
            result == "Z | Y | X"

        where:
            session << allSessions
    }

    def "jsonAgg builds a JSON array of column values"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alpha", 10)
            fixtures.addBasic(2L, "Beta", 20)
            fixtures.addBasic(3L, "Gamma", 30)

            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(DSL.jsonArrayAgg(basics.name).as("names"))
                    .from(basics)
                    .fetchSingle()
            def parsed = parseJson(result)

        then:
            parsed instanceof List
            (parsed as Set) == ["Alpha", "Beta", "Gamma"] as Set

        where:
            session << allSessions
    }

    def "jsonAgg with inner ORDER BY preserves ordering"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "C", 30)
            fixtures.addBasic(2L, "A", 10)
            fixtures.addBasic(3L, "B", 20)

            BasicsTable basics = new BasicsTable(null)

        when:
            def agg = DSL.jsonArrayAgg(basics.name, new Order(basics.numero, true))

            String result = session.select(agg.as("names"))
                    .from(basics)
                    .fetchSingle()
            def parsed = parseJson(result)

        then:
            parsed == ["A", "B", "C"]

        where:
            session << allSessions
    }

    def "jsonArrayAgg with multiple inner ORDER BY keys sorts ties deterministically"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 20)
            fixtures.addBasic(2L, "B", 10)
            fixtures.addBasic(3L, "C", 20)
            fixtures.addBasic(4L, "D", 10)

        when:
            def agg = DSL.jsonArrayAgg(basics.name, List.of(new Order(basics.numero, true), new Order(basics.id, false)))
            String result = session.select(agg.as("names"))
                    .from(basics)
                    .fetchSingle()

        then:
            parseJson(result) == ["D", "B", "C", "A"]

        where:
            session << allSessions
    }

    def "jsonArrayAggWhere drops rows not matching the filter"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            String result = session.select(DSL.jsonArrayAggWhere(basics.name, basics.numero.gt(20)).as("names"))
                    .from(basics)
                    .fetchSingle()
            def parsed = parseJson(result)

        then:
            parsed instanceof List
            parsed.sort() == ["B", "B", "C"]

        where:
            session << allSessions
    }

    def "jsonArrayAggWhere combines a condition list with AND"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            String result = session.select(DSL.jsonArrayAggWhere(basics.name, List.of(basics.numero.ge(20), basics.numero.lt(50))).as("names"))
                    .from(basics)
                    .fetchSingle()
            def parsed = parseJson(result)

        then:
            parsed instanceof List
            parsed.sort() == ["A", "B", "B"]

        where:
            session << allSessions
    }

    def "jsonArrayAggWhere condition and order overloads filter and sort"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            String result = session.select(aggregate.call().as("names"))
                    .from(basics)
                    .fetchSingle()

        then:
            parseJson(result) == expected

        where:
            session << allSessions

        combined:
            aggregate                                                                                                                              || expected
            ({ -> DSL.jsonArrayAggWhere(basics.name, basics.numero.ge(20), new Order(basics.id, true)) })                                          || ["A", "B", "B", "C"]
            ({ -> DSL.jsonArrayAggWhere(basics.name, basics.numero.ge(20), List.of(new Order(basics.id, false))) })                                || ["C", "B", "B", "A"]
            ({ -> DSL.jsonArrayAggWhere(basics.name, List.of(basics.numero.ge(20), basics.numero.lt(50)), new Order(basics.id, true)) })           || ["A", "B", "B"]
            ({ -> DSL.jsonArrayAggWhere(basics.name, List.of(basics.numero.ge(20), basics.numero.lt(50)), List.of(new Order(basics.id, false))) }) || ["B", "B", "A"]
    }

    // ============================================
    // json_object_agg
    // ============================================

    def "jsonObjectAgg builds a JSON object from key-value pairs"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "k1", 100)
            fixtures.addBasic(2L, "k2", 200)
            fixtures.addBasic(3L, "k3", 300)

        when:
            String result = session.select(DSL.jsonObjectAgg(basics.name, basics.numero).as("obj"))
                    .from(basics)
                    .fetchSingle()
            def parsed = parseJson(result)

        then:
            parsed == [k1: 100.0d, k2: 200.0d, k3: 300.0d]

        where:
            session << allSessions
    }

    def "jsonObjectAgg composes with a query WHERE clause"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "k1", 100)
            fixtures.addBasic(2L, "k2", 200)
            fixtures.addBasic(3L, "k3", 300)

        when:
            String result = session.select(DSL.jsonObjectAgg(basics.name, basics.numero).as("obj"))
                    .from(basics)
                    .where(basics.numero.gt(100))
                    .fetchSingle()
            def parsed = parseJson(result)

        then:
            parsed == [k2: 200.0d, k3: 300.0d]

        where:
            session << allSessions
    }

    def "jsonObjectAgg with duplicate keys keeps the key with one of its values"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "A", 20)
            fixtures.addBasic(3L, "B", 30)

        when:
            String result = session.select(DSL.jsonObjectAgg(basics.name, basics.numero).as("obj"))
                    .from(basics)
                    .fetchSingle()
            def parsed = parseJson(result)

        then:
            // databases differ in duplicate-key emission; Gson parses last-wins
            parsed.keySet() == ["A", "B"] as Set
            parsed["A"] in [10.0d, 20.0d]
            parsed["B"] == 30.0d

        where:
            session << allSessions
    }

    def "jsonObjectAgg stringifies non-string key expressions"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alpha", 10)
            fixtures.addBasic(2L, "Beta", 20)

        when:
            String result = session.select(DSL.jsonObjectAgg(basics.numero, basics.name).as("obj"))
                    .from(basics)
                    .fetchSingle()
            def parsed = parseJson(result)

        then:
            parsed == ["10": "Alpha", "20": "Beta"]

        where:
            session << allSessions
    }

    def "jsonObjectAgg over no rows returns SQL NULL"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "k1", 100)

        when:
            String result = session.select(DSL.jsonObjectAgg(basics.name, basics.numero).as("obj"))
                    .from(basics)
                    .where(basics.numero.gt(1000))
                    .fetchSingle()

        then:
            result == null

        where:
            session << allSessions
    }

    // ============================================
    // min / max
    // ============================================

    def "min() and max() work on numeric and string expressions"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            def result = session.select(aggregate.call()).from(basics).fetchSingle()

        then:
            result == expected

        where:
            session << allSessions

        combined:
            aggregate                                                    || expected
            ({ -> DSL.min(basics.numero) })                              || 10
            ({ -> DSL.max(basics.numero) })                              || 50
            ({ -> DSL.min(basics.name) })                                || "A"
            ({ -> DSL.max(basics.name) })                                || "C"
            ({ -> DSL.minWhere(basics.numero, basics.numero.gt(10)) })   || 20
            ({ -> DSL.maxWhere(basics.numero, basics.numero.lt(50)) })   || 30
    }

    // ============================================
    // sum
    // ============================================

    def "sum(), sumDistinct() and sumWhere() compute expected totals"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            def result = session.select(aggregate.call()).from(basics).fetchSingle()

        then:
            result == expected

        where:
            session << allSessions

        combined:
            aggregate                                                    || expected
            ({ -> DSL.sum(basics.numero) })                              || 140
            ({ -> DSL.sumDistinct(basics.numero) })                      || 110
            ({ -> DSL.sumWhere(basics.numero, basics.numero.ge(30)) })   || 110
    }

    def "DistinctWhere variants combine DISTINCT with filtering"() {
        given:
            setupStandardData(new TestFixtures(session))

        when:
            def result = session.select(aggregate.call()).from(basics).fetchSingle()

        then:
            result == expected

        where:
            session << allSessions

        combined:
            aggregate                                                                          || expected
            ({ -> DSL.avgDistinctWhere(basics.numero, basics.numero.ge(30)) })                 || 40.0d
            ({ -> DSL.sumDistinctWhere(basics.numero, basics.numero.ge(30)) })                 || 80
            ({ -> DSL.groupConcatDistinctWhere(basics.numero, ",", basics.numero.eq(30)) })    || "30"
    }

    def "STDDEV_POP calculates population standard deviation"() {
        when:
            def result = session.select(DSL.stddevPop(DSL.name(BigDecimal, "salary")))
                    .from("employees")
                    .fetchSingle()

        then:
            result != null
            // sqrt(VAR_POP) over 9 employee salaries
            Math.abs((result as Number).doubleValue() - 46673.28) < 0.01

        where:
            session << allSessions
    }

    def "STDDEV_SAMP calculates sample standard deviation"() {
        when:
            def result = session.select(DSL.stddevSamp(DSL.name(BigDecimal, "salary")))
                    .from("employees")
                    .fetchSingle()

        then:
            result != null
            // sqrt(VAR_SAMP) over 9 employee salaries
            Math.abs((result as Number).doubleValue() - 49504.49) < 0.01

        where:
            session << allSessions
    }

    def "VAR_POP calculates population variance"() {
        when:
            def result = session.select(DSL.varPop(DSL.name(BigDecimal, "salary")))
                    .from("employees")
                    .fetchSingle()

        then:
            result != null
            // sum((x - mean)^2) / n over 9 employee salaries
            Math.abs((result as Number).doubleValue() - 2178395061.73) < 1.0

        where:
            session << allSessions
    }

    def "VAR_SAMP calculates sample variance"() {
        when:
            def result = session.select(DSL.varSamp(DSL.name(BigDecimal, "salary")))
                    .from("employees")
                    .fetchSingle()

        then:
            result != null
            // sum((x - mean)^2) / (n-1) over 9 employee salaries
            Math.abs((result as Number).doubleValue() - 2450694444.44) < 1.0

        where:
            session << allSessions
    }

    // ============================================
    // grouped queries
    // ============================================

    def "aggregates combine with GROUP BY"() {
        given:
            setupStandardData(new TestFixtures(session))
            Expression<Long> countExpr = DSL.count()
            Expression<Integer> sumExpr = DSL.sum(basics.numero)
            Expression<String> concatExpr = DSL.groupConcat(basics.numero, ",", basics.numero.asc())

        when:
            List<Record> records = session.select(basics.name, countExpr, sumExpr, concatExpr)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(countExpr) == 2L
                r.get(sumExpr) == 30
                r.get(concatExpr) == "10,20"
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(countExpr) == 2L
                r.get(sumExpr) == 60
                r.get(concatExpr) == "30,30"
            }
            with(records[2]) { r ->
                r.get(basics.name) == "C"
                r.get(countExpr) == 1L
                r.get(sumExpr) == 50
                r.get(concatExpr) == "50"
            }

        where:
            session << allSessions
    }

    def "filtered aggregates combine with GROUP BY"() {
        given:
            setupStandardData(new TestFixtures(session))
            Expression<Long> countExpr = DSL.countWhere(basics.numero.gt(10))
            Expression<Integer> sumExpr = DSL.sumWhere(basics.numero, basics.numero.gt(10))

        when:
            List<Record> records = session.select(basics.name, countExpr, sumExpr)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 3
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(countExpr) == 1L
                r.get(sumExpr) == 20
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(countExpr) == 2L
                r.get(sumExpr) == 60
            }
            with(records[2]) { r ->
                r.get(basics.name) == "C"
                r.get(countExpr) == 1L
                r.get(sumExpr) == 50
            }

        where:
            session << allSessions
    }
}
