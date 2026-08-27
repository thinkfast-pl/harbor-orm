// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.EmployeesTable

/**
 * Integration tests for self-join functionality.
 * Tests joining a table to itself using different aliases.
 * Tests run on both H2 and PostgreSQL databases.
 *
 * Uses the employees table with hierarchical structure:
 * CEO (1) -> CTO (2) -> Alice (3), Bob (4), Charlie (5)
 * CEO (1) -> Sales VP (6) -> Diana (7), Eve (8), Frank (9)
 */
class SelfJoinIT extends AbstractHarborIT {

    def "should join table to itself using different aliases"() {
        given: "table references with different aliases for self-join"
            EmployeesTable employees = new EmployeesTable("e")
            EmployeesTable managers = new EmployeesTable("m")
            def managerNameCol = managers.name.as("manager")

        when: "performing self-join to get employees with their managers"
            List<Record> records = session.select(employees.name, managerNameCol)
                    .from(employees)
                    .join(managers).on(employees.managerId.eq(managers.id))
                    .orderBy(employees.name.asc())
                    .fetchAll()

        then: "employees with managers are returned with correct relationships"
            records.size() == 8  // All except CEO (who has no manager)

            // Verify specific relationships
            with(records.find { it.get(employees.name) == "Alice" }) { r ->
                r.get(managerNameCol) == "CTO"
            }
            with(records.find { it.get(employees.name) == "Bob" }) { r ->
                r.get(managerNameCol) == "CTO"
            }
            with(records.find { it.get(employees.name) == "Charlie" }) { r ->
                r.get(managerNameCol) == "CTO"
            }
            with(records.find { it.get(employees.name) == "CTO" }) { r ->
                r.get(managerNameCol) == "CEO"
            }
            with(records.find { it.get(employees.name) == "Sales VP" }) { r ->
                r.get(managerNameCol) == "CEO"
            }
            with(records.find { it.get(employees.name) == "Diana" }) { r ->
                r.get(managerNameCol) == "Sales VP"
            }
            with(records.find { it.get(employees.name) == "Eve" }) { r ->
                r.get(managerNameCol) == "Sales VP"
            }
            with(records.find { it.get(employees.name) == "Frank" }) { r ->
                r.get(managerNameCol) == "Sales VP"
            }

        where:
            session << allSessions
    }

    def "should use LEFT JOIN to include records without related records"() {
        given: "table references with different aliases"
            EmployeesTable employees = new EmployeesTable("e")
            EmployeesTable managers = new EmployeesTable("m")
            def managerNameCol = managers.name.as("manager")

        when: "performing left self-join to get all employees including those without managers"
            List<Record> records = session.select(employees.name, managerNameCol)
                    .from(employees)
                    .leftJoin(managers).on(employees.managerId.eq(managers.id))
                    .orderBy(employees.name.asc())
                    .fetchAll()

        then: "all employees returned, CEO has NULL manager"
            records.size() == 9

            // CEO has no manager
            with(records.find { it.get(employees.name) == "CEO" }) { r ->
                r.get(managerNameCol) == null
            }

            // Others have managers
            with(records.find { it.get(employees.name) == "CTO" }) { r ->
                r.get(managerNameCol) == "CEO"
            }

        where:
            session << allSessions
    }

    def "should find employees earning more than their managers"() {
        given: "table references"
            EmployeesTable employees = new EmployeesTable("e")
            EmployeesTable managers = new EmployeesTable("m")

        when: "comparing employee salary to manager salary via self-join"
            List<Record> records = session.select(employees.name, employees.salary, managers.name.as("manager"), managers.salary.as("manager_salary"))
                    .from(employees)
                    .join(managers).on(employees.managerId.eq(managers.id))
                    .where(employees.salary.gt(managers.salary))
                    .orderBy(employees.name.asc())
                    .fetchAll()

        then: "no employees earn more than their managers in this hierarchy"
            records.isEmpty()

        where:
            session << allSessions
    }

    def "should find employees in the same department as their manager"() {
        given: "table references"
            EmployeesTable employees = new EmployeesTable("e")
            EmployeesTable managers = new EmployeesTable("m")

        when: "joining employees with managers in same department"
            List<Record> records = session.select(employees.name, employees.department, managers.name.as("manager"))
                    .from(employees)
                    .join(managers).on(
                        employees.managerId.eq(managers.id)
                                .and(employees.department.eq(managers.department))
                    )
                    .orderBy(employees.name.asc())
                    .fetchAll()

        then: "only employees in same department as their manager are returned"
            // CTO reports to CEO (Engineering -> Executive) - no match
            // Sales VP reports to CEO (Sales -> Executive) - no match
            // Alice reports to CTO (Engineering -> Engineering) - match
            // Bob reports to CTO (Engineering -> Engineering) - match
            // Charlie reports to CTO (Engineering -> Engineering) - match
            // Diana reports to Sales VP (Sales -> Sales) - match
            // Eve reports to Sales VP (Sales -> Sales) - match
            // Frank reports to Sales VP (Sales -> Sales) - match
            records.size() == 6
            records.collect { it.get(employees.name) }.containsAll(["Alice", "Bob", "Charlie", "Diana", "Eve", "Frank"])

        where:
            session << allSessions
    }

    def "should support multiple conditions in self-join ON clause"() {
        given: "table references"
            EmployeesTable employees = new EmployeesTable("e")
            EmployeesTable managers = new EmployeesTable("m")

        when: "using multiple conditions to join"
            List<Record> records = session.select(employees.name, managers.name.as("manager"))
                    .from(employees)
                    .join(managers).on(
                        employees.managerId.eq(managers.id)
                                .and(managers.department.eq(DSL.constant("Executive")))
                    )
                    .orderBy(employees.name.asc())
                    .fetchAll()

        then: "only employees whose manager is in Executive department are returned"
            // CTO reports to CEO (Executive) - match
            // Sales VP reports to CEO (Executive) - match
            // Alice, Bob, Charlie report to CTO (Engineering, not Executive) - no match
            // Diana, Eve, Frank report to Sales VP (Sales, not Executive) - no match
            records.size() == 2
            records.collect { it.get(employees.name) }.containsAll(["CTO", "Sales VP"])

        where:
            session << allSessions
    }

    def "should find all direct reports for a specific manager"() {
        given: "table references"
            EmployeesTable employees = new EmployeesTable("e")
            EmployeesTable managers = new EmployeesTable("m")

        when: "finding all employees who report to CTO"
            List<Record> records = session.select(employees.name, employees.department)
                    .from(employees)
                    .join(managers).on(employees.managerId.eq(managers.id))
                    .where(managers.name.eq(DSL.constant("CTO")))
                    .orderBy(employees.name.asc())
                    .fetchAll()

        then: "Alice, Bob and Charlie are returned as CTO's direct reports"
            records.size() == 3
            records[0].get(employees.name) == "Alice"
            records[1].get(employees.name) == "Bob"
            records[2].get(employees.name) == "Charlie"

        where:
            session << allSessions
    }

    def "should count direct reports per manager using self-join"() {
        given: "table references"
            EmployeesTable employees = new EmployeesTable("e")
            EmployeesTable managers = new EmployeesTable("m")
            def managerName = managers.name.as("manager")
            def reportCount = DSL.count().as("report_count")

        when: "counting direct reports for each manager"
            List<Record> records = session.select(managerName, reportCount)
                    .from(employees)
                    .join(managers).on(employees.managerId.eq(managers.id))
                    .groupBy(managers.name)
                    .orderBy(managers.name.asc())
                    .fetchAll()

        then: "correct count of direct reports per manager"
            records.size() == 3  // CEO, CTO, Sales VP have direct reports

            with(records.find { it.get(managerName) == "CEO" }) { r ->
                r.get(reportCount) == 2L  // CTO and Sales VP
            }
            with(records.find { it.get(managerName) == "CTO" }) { r ->
                r.get(reportCount) == 3L  // Alice, Bob, Charlie
            }
            with(records.find { it.get(managerName) == "Sales VP" }) { r ->
                r.get(reportCount) == 3L  // Diana, Eve, Frank
            }

        where:
            session << allSessions
    }

    def "should find managers at specific level using self-join"() {
        given: "aliases for three-level join (employee -> manager -> grandmanager)"
            EmployeesTable employees = new EmployeesTable("e")
            EmployeesTable managers = new EmployeesTable("m")
            EmployeesTable grandmanagers = new EmployeesTable("gm")
            def managerNameCol = managers.name.as("manager")
            def grandmanagerNameCol = grandmanagers.name.as("grandmanager")

        when: "performing two-level self-join to find grandmanagers"
            List<Record> records = session.select(
                        employees.name,
                        managerNameCol,
                        grandmanagerNameCol
                    )
                    .from(employees)
                    .join(managers).on(employees.managerId.eq(managers.id))
                    .join(grandmanagers).on(managers.managerId.eq(grandmanagers.id))
                    .orderBy(employees.name.asc())
                    .fetchAll()

        then: "employees with both manager and grandmanager are returned"
            // Alice, Bob, Charlie report to CTO who reports to CEO
            // Diana, Eve, Frank report to Sales VP who reports to CEO
            records.size() == 6

            with(records.find { it.get(employees.name) == "Alice" }) { r ->
                r.get(managerNameCol) == "CTO"
                r.get(grandmanagerNameCol) == "CEO"
            }
            with(records.find { it.get(employees.name) == "Bob" }) { r ->
                r.get(managerNameCol) == "CTO"
                r.get(grandmanagerNameCol) == "CEO"
            }
            with(records.find { it.get(employees.name) == "Charlie" }) { r ->
                r.get(managerNameCol) == "CTO"
                r.get(grandmanagerNameCol) == "CEO"
            }
            with(records.find { it.get(employees.name) == "Diana" }) { r ->
                r.get(managerNameCol) == "Sales VP"
                r.get(grandmanagerNameCol) == "CEO"
            }
            with(records.find { it.get(employees.name) == "Eve" }) { r ->
                r.get(managerNameCol) == "Sales VP"
                r.get(grandmanagerNameCol) == "CEO"
            }
            with(records.find { it.get(employees.name) == "Frank" }) { r ->
                r.get(managerNameCol) == "Sales VP"
                r.get(grandmanagerNameCol) == "CEO"
            }

        where:
            session << allSessions
    }

    def "should combine self-join with ORDER BY and LIMIT"() {
        given: "table references"
            EmployeesTable employees = new EmployeesTable("e")
            EmployeesTable managers = new EmployeesTable("m")
            def managerNameCol = managers.name.as("manager")

        when: "getting top 3 employees by salary who have managers"
            List<Record> records = session.select(employees.name, employees.salary, managerNameCol)
                    .from(employees)
                    .join(managers).on(employees.managerId.eq(managers.id))
                    .orderBy(employees.salary.desc())
                    .limit(3)
                    .fetchAll()

        then: "top 3 highest paid employees with managers are returned"
            records.size() == 3
            // CTO (150000), Sales VP (120000), Bob (80000) are top 3 with managers
            records[0].get(employees.name) == "CTO"
            records[1].get(employees.name) == "Sales VP"
            records[2].get(employees.name) == "Bob"

        where:
            session << allSessions
    }
}
