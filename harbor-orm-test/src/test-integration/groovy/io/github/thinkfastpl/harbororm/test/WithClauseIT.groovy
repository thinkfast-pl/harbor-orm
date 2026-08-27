// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.CommonTableExpression
import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.query.EmployeesTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

/**
 * Integration tests for WITH clause (Common Table Expressions) functionality.
 * Tests session.with() and session.withRecursive() methods as well as
 * the SelectQuery.WithStep interface.
 */
class WithClauseIT extends AbstractHarborIT {

    // ==================== session.with() tests ====================

    def "with single CTE - basic select from CTE"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "Alpha", 10)
            testFixtures.addBasic(2L, "Beta", 20)
            testFixtures.addBasic(3L, "Gamma", 30)

            BasicsTable basics = new BasicsTable("bt")

            CommonTableExpression basicsCte = new CommonTableExpression("basics_cte")
                    .as(
                            DSL.select(basics.id, basics.name, basics.numero)
                                    .from(basics)
                                    .where(basics.numero.gt(15))
                    )

            CommonTableExpression.Column<Long> cteId = basicsCte.column("cte_id", basics.id)
            CommonTableExpression.Column<String> cteName = basicsCte.column("cte_name", basics.name)
            CommonTableExpression.Column<Integer> cteNumero = basicsCte.column("cte_numero", basics.numero)

        when:
            List<Record> records = session
                    .with(basicsCte)
                    .select(cteId, cteName, cteNumero)
                    .from(basicsCte)
                    .orderBy(cteId.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records.get(0)) { r ->
                r.get(cteId) == 2L
                r.get(cteName) == "Beta"
                r.get(cteNumero) == 20
            }
            with(records.get(1)) { r ->
                r.get(cteId) == 3L
                r.get(cteName) == "Gamma"
                r.get(cteNumero) == 30
            }

        where:
            session << allSessions
    }

    def "with varargs - multiple CTEs passed as varargs"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "One", 100)
            testFixtures.addBasic(2L, "Two", 200)
            testFixtures.addBasic(3L, "Three", 300)

            BasicsTable basics = new BasicsTable("bt")

            // First CTE: filter by numero > 150, select only id and name
            CommonTableExpression highNumero = new CommonTableExpression("high_numero")
                    .as(
                            DSL.select(basics.id, basics.name)
                                    .from(basics)
                                    .where(basics.numero.gt(150))
                    )

            CommonTableExpression.Column<Long> highId = highNumero.column("high_id", basics.id)
            CommonTableExpression.Column<String> highName = highNumero.column("high_name", basics.name)

            // Second CTE: filter by numero < 250, select only id and name
            CommonTableExpression lowNumero = new CommonTableExpression("low_numero")
                    .as(
                            DSL.select(basics.id, basics.name)
                                    .from(basics)
                                    .where(basics.numero.lt(250))
                    )

            CommonTableExpression.Column<Long> lowId = lowNumero.column("low_id", basics.id)
            CommonTableExpression.Column<String> lowName = lowNumero.column("low_name", basics.name)

        when:
            // Use with() with varargs - both CTEs at once
            List<Record> records = session
                    .with(highNumero, lowNumero)
                    .select(highId, highName)
                    .from(highNumero)
                    .join(lowNumero).on(highId.eq(lowId))
                    .orderBy(highId.asc())
                    .fetchAll()

        then:
            // Only id=2 (numero=200) satisfies both conditions: > 150 AND < 250
            records.size() == 1
            with(records.get(0)) { r ->
                r.get(highId) == 2L
                r.get(highName) == "Two"
            }

        where:
            session << allSessions
    }

    def "with list - multiple CTEs passed as list"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(10L, "Ten", 10)
            testFixtures.addBasic(20L, "Twenty", 20)
            testFixtures.addBasic(30L, "Thirty", 30)

            BasicsTable basics = new BasicsTable("bt")

            CommonTableExpression cte1 = new CommonTableExpression("cte_one")
                    .as(
                            DSL.select(basics.id.as("cid"))
                                    .from(basics)
                                    .where(basics.id.le(20L))
                    )

            CommonTableExpression.Column<Long> cte1Id = cte1.column("id1", basics.id)

            CommonTableExpression cte2 = new CommonTableExpression("cte_two")
                    .as(
                            DSL.select(basics.id.as("cid"))
                                    .from(basics)
                                    .where(basics.id.ge(20L))
                    )

            CommonTableExpression.Column<Long> cte2Id = cte2.column("id2", basics.id)

            List<CommonTableExpression> cteList = [cte1, cte2]

        when:
            // Use with() with List parameter
            List<Record> records = session
                    .with(cteList)
                    .select(cte1Id, cte2Id)
                    .from(cte1)
                    .join(cte2).on(cte1Id.eq(cte2Id))
                    .fetchAll()

        then:
            // Only id=20 is in both CTEs
            records.size() == 1
            with(records.get(0)) { r ->
                r.get(cte1Id) == 20L
                r.get(cte2Id) == 20L
            }

        where:
            session << allSessions
    }

    def "with chained - multiple with() calls in sequence"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "A", 1)
            testFixtures.addBasic(2L, "B", 2)
            testFixtures.addBasic(3L, "C", 3)

            BasicsTable basics = new BasicsTable("bt")

            CommonTableExpression first = new CommonTableExpression("first_cte")
                    .as(
                            DSL.select(basics.id, basics.name)
                                    .from(basics)
                    )

            CommonTableExpression.Column<Long> firstId = first.column("f_id", basics.id)
            CommonTableExpression.Column<String> firstName = first.column("f_name", basics.name)

            // Second CTE references the first CTE
            CommonTableExpression second = new CommonTableExpression("second_cte")
                    .as(
                            DSL.select(firstId)
                                    .from(first)
                                    .where(firstId.gt(1L))
                    )

            CommonTableExpression.Column<Long> secondId = second.column("s_id", firstId)

        when:
            // Chain with() calls
            List<Record> records = session
                    .with(first)
                    .with(second)
                    .select(secondId)
                    .from(second)
                    .orderBy(secondId.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records.get(0)) { r ->
                r.get(secondId) == 2L
            }
            with(records.get(1)) { r ->
                r.get(secondId) == 3L
            }

        where:
            session << allSessions
    }

    // ==================== session.withRecursive() tests ====================

    def "withRecursive single CTE - generate number sequence"() {
        given:
            CommonTableExpression numbers = new CommonTableExpression("numbers")
            CommonTableExpression.Column<Integer> n = numbers.column("n", Integer.class)

            numbers.as(
                    DSL.select(DSL.constant(1))
                            .unionAll(
                                    DSL.select(n.add(1))
                                            .from(numbers)
                                            .where(n.lt(10))
                            )
            )

        when:
            List<Record> records = session
                    .withRecursive(numbers)
                    .select(n)
                    .from(numbers)
                    .orderBy(n.asc())
                    .fetchAll()

        then:
            records.size() == 10
            records.collect { it.get(n) } == [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

        where:
            session << allSessions
    }

    def "withRecursive varargs - multiple recursive CTEs"() {
        given:
            // First recursive CTE: powers of 2 (2, 4, 8, 16, 32)
            CommonTableExpression powersOfTwo = new CommonTableExpression("powers_of_two")
            CommonTableExpression.Column<Integer> pow2 = powersOfTwo.column("val", Integer.class)

            powersOfTwo.as(
                    DSL.select(DSL.constant(2))
                            .unionAll(
                                    DSL.select(pow2.multiply(2))
                                            .from(powersOfTwo)
                                            .where(pow2.lt(32))
                            )
            )

            // Second recursive CTE: powers of 3 (3, 9, 27)
            CommonTableExpression powersOfThree = new CommonTableExpression("powers_of_three")
            CommonTableExpression.Column<Integer> pow3 = powersOfThree.column("val", Integer.class)

            powersOfThree.as(
                    DSL.select(DSL.constant(3))
                            .unionAll(
                                    DSL.select(pow3.multiply(3))
                                            .from(powersOfThree)
                                            .where(pow3.lt(27))
                            )
            )

        when:
            // Use withRecursive with varargs
            List<Record> records = session
                    .withRecursive(powersOfTwo, powersOfThree)
                    .select(pow2)
                    .from(powersOfTwo)
                    .orderBy(pow2.asc())
                    .fetchAll()

        then:
            records.size() == 5
            records.collect { it.get(pow2) } == [2, 4, 8, 16, 32]

        where:
            session << allSessions
    }

    def "withRecursive list - recursive CTEs passed as list"() {
        given:
            // Countdown from 5 to 1
            CommonTableExpression countdown = new CommonTableExpression("countdown")
            CommonTableExpression.Column<Integer> countVal = countdown.column("val", Integer.class)

            countdown.as(
                    DSL.select(DSL.constant(5))
                            .unionAll(
                                    DSL.select(countVal.subtract(1))
                                            .from(countdown)
                                            .where(countVal.gt(1))
                            )
            )

            List<CommonTableExpression> cteList = [countdown]

        when:
            List<Record> records = session
                    .withRecursive(cteList)
                    .select(countVal)
                    .from(countdown)
                    .orderBy(countVal.desc())
                    .fetchAll()

        then:
            records.size() == 5
            records.collect { it.get(countVal) } == [5, 4, 3, 2, 1]

        where:
            session << allSessions
    }

    def "withRecursive hierarchical query - employee org chart traversal"() {
        given:
            // Use pre-populated employees table with hierarchy:
            // CEO (1) -> CTO (2) -> Alice (3), Bob (4), Charlie (5)
            // CEO (1) -> Sales VP (6) -> Diana (7), Eve (8), Frank (9)

            EmployeesTable employees = new EmployeesTable("emp")

            // Recursive CTE to find all employees under CTO (id=2)
            CommonTableExpression orgChart = new CommonTableExpression("org_chart")
            CommonTableExpression.Column<Long> ocId = orgChart.column("id", Long.class)
            CommonTableExpression.Column<String> ocName = orgChart.column("name", String.class)
            CommonTableExpression.Column<Long> ocManagerId = orgChart.column("manager_id", Long.class)
            CommonTableExpression.Column<Integer> ocLevel = orgChart.column("level", Integer.class)

            orgChart.as(
                    // Base case: start with CTO (id=2)
                    DSL.select(employees.id, employees.name, employees.managerId, DSL.constant(0))
                            .from(employees)
                            .where(employees.id.eq(2L))
                            .unionAll(
                                    // Recursive case: find direct reports
                                    DSL.select(employees.id, employees.name, employees.managerId, ocLevel.add(1))
                                            .from(employees)
                                            .join(orgChart).on(employees.managerId.eq(ocId))
                            )
            )

        when:
            List<Record> records = session
                    .withRecursive(orgChart)
                    .select(ocId, ocName, ocLevel)
                    .from(orgChart)
                    .orderBy(ocLevel.asc(), ocId.asc())
                    .fetchAll()

        then:
            records.size() == 4  // CTO + Alice, Bob, Charlie
            with(records.get(0)) { r ->
                r.get(ocId) == 2L
                r.get(ocName) == "CTO"
                r.get(ocLevel) == 0
            }
            // Level 1: direct reports of CTO
            records.findAll { it.get(ocLevel) == 1 }.size() == 3
            records.findAll { it.get(ocLevel) == 1 }.collect { it.get(ocName) }.toSet() == ["Alice", "Bob", "Charlie"].toSet()

        where:
            session << allSessions
    }

    // ==================== CTE column definition tests ====================

    def "CTE with column type from class reference"() {
        given:
            CommonTableExpression constants = new CommonTableExpression("constants")
            // Define column using Class reference instead of SelectableExpression
            CommonTableExpression.Column<String> strCol = constants.column("str_value", String.class)
            CommonTableExpression.Column<Integer> intCol = constants.column("int_value", Integer.class)

            constants.as(
                    DSL.select(DSL.constant("hello"), DSL.constant(42))
            )

        when:
            List<Record> records = session
                    .with(constants)
                    .select(strCol, intCol)
                    .from(constants)
                    .fetchAll()

        then:
            records.size() == 1
            with(records.get(0)) { r ->
                r.get(strCol) == "hello"
                r.get(intCol) == 42
            }

        where:
            session << allSessions
    }

    def "CTE with column type from referenced column"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "Test", 99)

            BasicsTable basics = new BasicsTable("bt")

            CommonTableExpression cte = new CommonTableExpression("typed_cte")
                    .as(
                            DSL.select(basics.id, basics.name, basics.numero)
                                    .from(basics)
                    )

            // Define columns using referenced SelectableExpression
            CommonTableExpression.Column<Long> idCol = cte.column("id_col", basics.id)
            CommonTableExpression.Column<String> nameCol = cte.column("name_col", basics.name)
            CommonTableExpression.Column<Integer> numCol = cte.column("num_col", basics.numero)

        when:
            List<Record> records = session
                    .with(cte)
                    .select(idCol, nameCol, numCol)
                    .from(cte)
                    .fetchAll()

        then:
            records.size() == 1
            with(records.get(0)) { r ->
                r.get(idCol) == 1L
                r.get(nameCol) == "Test"
                r.get(numCol) == 99
            }

        where:
            session << allSessions
    }

    // ==================== WithStep chaining tests ====================

    def "WithStep chains correctly to SelectStep"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "Item1", 10)
            testFixtures.addBasic(2L, "Item2", 20)

            BasicsTable basics = new BasicsTable("bt")

            CommonTableExpression cte = new CommonTableExpression("simple_cte")
                    .as(
                            DSL.select(basics.id, basics.name)
                                    .from(basics)
                    )

            CommonTableExpression.Column<Long> cteId = cte.column("id", basics.id)
            CommonTableExpression.Column<String> cteName = cte.column("name", basics.name)

        when:
            // Test that WithStep correctly implements SelectStep interface
            List<Record> records = session
                    .with(cte)
                    .select(cteId, cteName)  // Using varargs select
                    .from(cte)
                    .where(cteId.eq(1L))
                    .fetchAll()

        then:
            records.size() == 1
            records.get(0).get(cteName) == "Item1"

        where:
            session << allSessions
    }

    def "WithStep select with single expression"() {
        given:
            CommonTableExpression numbers = new CommonTableExpression("single_num")
            CommonTableExpression.Column<Integer> num = numbers.column("num", Integer.class)

            numbers.as(
                    DSL.select(DSL.constant(100))
            )

        when:
            // Test single expression select
            List<Record> records = session
                    .with(numbers)
                    .select(num)
                    .from(numbers)
                    .fetchAll()

        then:
            records.size() == 1
            records.get(0).get(num) == 100

        where:
            session << allSessions
    }

    def "WithStep select with list of expressions"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "ListTest", 55)

            BasicsTable basics = new BasicsTable("bt")

            CommonTableExpression cte = new CommonTableExpression("list_cte")
                    .as(
                            DSL.select(basics.id, basics.name, basics.numero)
                                    .from(basics)
                    )

            CommonTableExpression.Column<Long> cteId = cte.column("id", basics.id)
            CommonTableExpression.Column<String> cteName = cte.column("name", basics.name)
            CommonTableExpression.Column<Integer> cteNumero = cte.column("numero", basics.numero)

            List expressions = [cteId, cteName, cteNumero]

        when:
            // Test select with List parameter
            List<Record> records = session
                    .with(cte)
                    .select(expressions)
                    .from(cte)
                    .fetchAll()

        then:
            records.size() == 1
            with(records.get(0)) { r ->
                r.get(cteId) == 1L
                r.get(cteName) == "ListTest"
                r.get(cteNumero) == 55
            }

        where:
            session << allSessions
    }

    // ==================== CTE referencing another CTE ====================

    def "CTE referencing another CTE in chain"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "First", 10)
            testFixtures.addBasic(2L, "Second", 20)
            testFixtures.addBasic(3L, "Third", 30)

            BasicsTable basics = new BasicsTable("bt")

            // First CTE: all basics
            CommonTableExpression allBasics = new CommonTableExpression("all_basics")
                    .as(
                            DSL.select(basics.id, basics.name, basics.numero)
                                    .from(basics)
                    )

            CommonTableExpression.Column<Long> allId = allBasics.column("id", basics.id)
            CommonTableExpression.Column<String> allName = allBasics.column("name", basics.name)
            CommonTableExpression.Column<Integer> allNumero = allBasics.column("numero", basics.numero)

            // Second CTE: references first CTE, filters to numero > 15
            CommonTableExpression filteredBasics = new CommonTableExpression("filtered_basics")
                    .as(
                            DSL.select(allId, allName)
                                    .from(allBasics)
                                    .where(allNumero.gt(15))
                    )

            CommonTableExpression.Column<Long> filtId = filteredBasics.column("filt_id", allId)
            CommonTableExpression.Column<String> filtName = filteredBasics.column("filt_name", allName)

            // Third CTE: references second CTE, adds ordering by name
            CommonTableExpression orderedBasics = new CommonTableExpression("ordered_basics")
                    .as(
                            DSL.select(filtId, filtName)
                                    .from(filteredBasics)
                    )

            CommonTableExpression.Column<Long> ordId = orderedBasics.column("ord_id", filtId)
            CommonTableExpression.Column<String> ordName = orderedBasics.column("ord_name", filtName)

        when:
            List<Record> records = session
                    .with(allBasics)
                    .with(filteredBasics)
                    .with(orderedBasics)
                    .select(ordId, ordName)
                    .from(orderedBasics)
                    .orderBy(ordId.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records.get(0)) { r ->
                r.get(ordId) == 2L
                r.get(ordName) == "Second"
            }
            with(records.get(1)) { r ->
                r.get(ordId) == 3L
                r.get(ordName) == "Third"
            }

        where:
            session << allSessions
    }

    // ==================== Edge cases ====================

    def "CTE with aggregation function"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "A", 10)
            testFixtures.addBasic(2L, "B", 20)
            testFixtures.addBasic(3L, "C", 30)

            BasicsTable basics = new BasicsTable("bt")

            CommonTableExpression sumCte = new CommonTableExpression("sum_cte")
                    .as(
                            DSL.select(DSL.sum(basics.numero).as("total"))
                                    .from(basics)
                    )

            CommonTableExpression.Column<Long> total = sumCte.column("total", Long.class)

        when:
            List<Record> records = session
                    .with(sumCte)
                    .select(total)
                    .from(sumCte)
                    .fetchAll()

        then:
            records.size() == 1
            records.get(0).get(total) == 60L

        where:
            session << allSessions
    }

    def "CTE with group by"() {
        given:
            // Use pre-populated employees table
            EmployeesTable employees = new EmployeesTable("emp")

            CommonTableExpression deptStats = new CommonTableExpression("dept_stats")
                    .as(
                            DSL.select(employees.department, DSL.count().as("cnt"))
                                    .from(employees)
                                    .groupBy(employees.department)
                    )

            CommonTableExpression.Column<String> dept = deptStats.column("dept", employees.department)
            CommonTableExpression.Column<Long> cnt = deptStats.column("cnt", Long.class)

        when:
            List<Record> records = session
                    .with(deptStats)
                    .select(dept, cnt)
                    .from(deptStats)
                    .orderBy(dept.asc())
                    .fetchAll()

        then:
            records.size() == 3  // Engineering, Executive, Sales
            records.find { it.get(dept) == "Engineering" }.get(cnt) == 4L
            records.find { it.get(dept) == "Executive" }.get(cnt) == 1L
            records.find { it.get(dept) == "Sales" }.get(cnt) == 4L

        where:
            session << allSessions
    }

    def "recursive CTE with depth limit prevents infinite recursion"() {
        given:
            // Simple recursive CTE with explicit depth limit
            CommonTableExpression limited = new CommonTableExpression("limited")
            CommonTableExpression.Column<Integer> depth = limited.column("depth", Integer.class)

            limited.as(
                    DSL.select(DSL.constant(0))
                            .unionAll(
                                    DSL.select(depth.add(1))
                                            .from(limited)
                                            .where(depth.lt(3))  // Limit to depth 3
                            )
            )

        when:
            List<Record> records = session
                    .withRecursive(limited)
                    .select(depth)
                    .from(limited)
                    .orderBy(depth.asc())
                    .fetchAll()

        then:
            records.size() == 4  // 0, 1, 2, 3
            records.collect { it.get(depth) } == [0, 1, 2, 3]

        where:
            session << allSessions
    }
}
