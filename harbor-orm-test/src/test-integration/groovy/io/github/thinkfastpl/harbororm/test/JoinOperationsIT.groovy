// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.Condition
import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.query.EmployeesTable
import io.github.thinkfastpl.harbororm.query.RolePermissionsTable
import io.github.thinkfastpl.harbororm.query.RolesTable
import io.github.thinkfastpl.harbororm.test.domain.QRoleEntity
import io.github.thinkfastpl.harbororm.test.domain.RoleEntity
import io.github.thinkfastpl.harbororm.test.domain.RolePermission
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

/**
 * Integration tests for join operations with focus on JoinStep and JoinOnStep interfaces.
 *
 * This test class covers:
 * - JoinStep.join(QTable) - inner join with QTable
 * - JoinStep.leftJoin(QTable) - left join with QTable
 * - JoinStep.rightJoin(QTable) - right join with QTable
 * - JoinStep.crossJoin(QTableSource) - cartesian product
 * - JoinOnStep.on(Condition) - single condition
 * - JoinOnStep.on(Condition...) - varargs conditions
 * - JoinOnStep.on(List<Condition>) - list of conditions
 * - Multiple joins in single query
 * - innerJoin() alias for join()
 *
 * Tests run on both H2 and PostgreSQL databases via allSessions.
 */
class JoinOperationsIT extends AbstractHarborIT {

    // ==================== INNER JOIN ====================

    def "should perform inner join with QTable"() {
        given: "roles with permissions exist in database"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Editor", List.of(RolePermission.PRODUCT_EDIT)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "performing inner join using QTable objects"
            List<Record> records = session.select(roles.id, roles.name, permissions.permission)
                    .from(roles)
                    .join(permissions).on(roles.id.eq(permissions.roleId))
                    .orderBy(roles.id.asc(), permissions.permission.asc())
                    .fetchAll()

        then: "matching rows from both tables are returned"
            records.size() == 3
            records[0].get(roles.name) == "Admin"
            records[0].get(permissions.permission) == RolePermission.PRODUCT_ADD
            records[1].get(roles.name) == "Admin"
            records[1].get(permissions.permission) == RolePermission.PRODUCT_EDIT
            records[2].get(roles.name) == "Editor"
            records[2].get(permissions.permission) == RolePermission.PRODUCT_EDIT

        where:
            session << allSessions
    }

    def "should use innerJoin() as alias for join()"() {
        given: "roles with permissions exist"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "SuperAdmin", List.of(RolePermission.PRODUCT_ADD)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "using innerJoin() method"
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .innerJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .fetchAll()

        then: "inner join works correctly"
            records.size() == 1
            records[0].get(roles.name) == "SuperAdmin"
            records[0].get(permissions.permission) == RolePermission.PRODUCT_ADD

        where:
            session << allSessions
    }

    // ==================== LEFT JOIN ====================

    def "should perform left join returning all left table rows"() {
        given: "a role without any permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Empty Role", List.of()))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "performing left join"
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .leftJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .orderBy(roles.name.asc())
                    .fetchAll()

        then: "all left table rows returned, unmatched have NULL"
            records.size() == 2
            // Admin has permission
            records[0].get(roles.name) == "Admin"
            records[0].get(permissions.permission) == RolePermission.PRODUCT_ADD
            // Empty Role has NULL permission
            records[1].get(roles.name) == "Empty Role"
            records[1].get(permissions.permission) == null

        where:
            session << allSessions
    }

    // ==================== RIGHT JOIN ====================

    def "should perform right join returning all right table rows"() {
        given: "roles with permissions exist in database"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "performing right join"
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .rightJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .orderBy(permissions.permission.asc())
                    .fetchAll()

        then: "all right table rows returned"
            records.size() == 2
            records.every { it.get(roles.name) == "Admin" }
            records[0].get(permissions.permission) == RolePermission.PRODUCT_ADD
            records[1].get(permissions.permission) == RolePermission.PRODUCT_EDIT

        where:
            session << allSessions
    }

    // ==================== CROSS JOIN ====================

    def "should perform cross join producing cartesian product"() {
        given: "roles with permissions exist in database"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Role1", List.of(RolePermission.PRODUCT_ADD)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Role2", List.of(RolePermission.PRODUCT_EDIT)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "performing cross join"
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .crossJoin(permissions)
                    .orderBy(roles.name.asc(), permissions.permission.asc())
                    .fetchAll()

        then: "cartesian product is returned (2 roles x 2 permissions = 4 rows)"
            records.size() == 4
            // Role1 combined with both permissions
            records[0].get(roles.name) == "Role1"
            records[1].get(roles.name) == "Role1"
            // Role2 combined with both permissions
            records[2].get(roles.name) == "Role2"
            records[3].get(roles.name) == "Role2"

        where:
            session << allSessions
    }

    def "should perform cross join using QTableName as QTableSource"() {
        given: "basics and roles exist"
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Basic1", 100)
            fixtures.addBasic(2L, "Basic2", 200)

            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Role1", List.of()))

            BasicsTable basics = new BasicsTable("b")
            // Create a separate RolesTable for the cross join to get proper column references
            RolesTable rolesForJoin = new RolesTable("r")

        when: "performing cross join using QTableName (getTableName())"
            List<Record> records = session.select(basics.name, rolesForJoin.name.as("role_name"))
                    .from(basics)
                    .crossJoin(rolesForJoin.getTableName())  // Uses QTableName as QTableSource
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then: "cartesian product is returned (2 basics x 1 role = 2 rows)"
            records.size() == 2
            records[0].get(basics.name) == "Basic1"
            records[1].get(basics.name) == "Basic2"

        where:
            session << allSessions
    }

    // ==================== JoinOnStep.on() variants ====================

    def "should use on() with single Condition"() {
        given: "roles with permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "using on() with single condition"
            Condition joinCondition = roles.id.eq(permissions.roleId)
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .join(permissions).on(joinCondition)
                    .fetchAll()

        then: "join works correctly"
            records.size() == 1
            records[0].get(roles.name) == "Admin"
            records[0].get(permissions.permission) == RolePermission.PRODUCT_ADD

        where:
            session << allSessions
    }

    def "should use on() with varargs Conditions"() {
        given: "roles with permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Editor", List.of(RolePermission.PRODUCT_EDIT)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "using on() with multiple conditions as varargs"
            Condition c1 = roles.id.eq(permissions.roleId)
            Condition c2 = permissions.permission.eq(RolePermission.PRODUCT_ADD)
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .join(permissions).on(c1, c2)  // varargs
                    .fetchAll()

        then: "both conditions are applied (AND)"
            records.size() == 1
            records[0].get(roles.name) == "Admin"
            records[0].get(permissions.permission) == RolePermission.PRODUCT_ADD

        where:
            session << allSessions
    }

    def "should use on() with List of Conditions"() {
        given: "roles with permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "using on() with list of conditions"
            List<Condition> conditions = [
                    roles.id.eq(permissions.roleId),
                    permissions.permission.eq(RolePermission.PRODUCT_EDIT)
            ]
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .join(permissions).on(conditions)  // List<Condition>
                    .fetchAll()

        then: "all conditions in list are applied (AND)"
            records.size() == 1
            records[0].get(permissions.permission) == RolePermission.PRODUCT_EDIT

        where:
            session << allSessions
    }

    def "should use on() with compound AND condition"() {
        given: "roles with permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Editor", List.of(RolePermission.PRODUCT_EDIT)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "using on() with compound condition using .and()"
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .join(permissions).on(
                        roles.id.eq(permissions.roleId)
                                .and(roles.name.eq(DSL.constant("Admin")))
                    )
                    .orderBy(permissions.permission.asc())
                    .fetchAll()

        then: "compound condition is applied"
            records.size() == 2
            records.every { it.get(roles.name) == "Admin" }

        where:
            session << allSessions
    }

    // ==================== Multiple Joins ====================

    def "should perform multiple joins in single query"() {
        given: "hierarchical employee data exists in database"
            EmployeesTable employees = new EmployeesTable("e")
            EmployeesTable managers = new EmployeesTable("m")
            EmployeesTable grandmanagers = new EmployeesTable("gm")
            def managerName = managers.name.as("manager")
            def grandmanagerName = grandmanagers.name.as("grandmanager")

        when: "performing two-level join"
            List<Record> records = session.select(
                        employees.name,
                        managerName,
                        grandmanagerName
                    )
                    .from(employees)
                    .join(managers).on(employees.managerId.eq(managers.id))
                    .join(grandmanagers).on(managers.managerId.eq(grandmanagers.id))
                    .where(employees.name.eq(DSL.constant("Alice")))
                    .fetchAll()

        then: "multi-level hierarchy is correctly retrieved"
            records.size() == 1
            records[0].get(employees.name) == "Alice"
            records[0].get(managerName) == "CTO"
            records[0].get(grandmanagerName) == "CEO"

        where:
            session << allSessions
    }

    def "should chain multiple join types in same query"() {
        given: "roles with permissions and basics"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Empty", List.of()))

            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Admin", 100)
            fixtures.addBasic(2L, "Empty", 200)

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")
            BasicsTable basics = new BasicsTable("b")

        when: "chaining inner join and left join"
            List<Record> records = session.select(basics.name, roles.name.as("role"), permissions.permission)
                    .from(basics)
                    .join(roles).on(basics.name.eq(roles.name))
                    .leftJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then: "joined results include NULL for empty permissions"
            records.size() == 2
            records[0].get(basics.name) == "Admin"
            records[0].get(permissions.permission) == RolePermission.PRODUCT_ADD
            records[1].get(basics.name) == "Empty"
            records[1].get(permissions.permission) == null

        where:
            session << allSessions
    }

    def "should mix crossJoin and regular join"() {
        given: "basics and employees exist"
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Item1", 10)
            fixtures.addBasic(2L, "Item2", 20)

            BasicsTable b1 = new BasicsTable("b1")
            BasicsTable b2 = new BasicsTable("b2")

        when: "using cross join then filtering"
            List<Record> records = session.select(b1.name.as("name1"), b2.name.as("name2"))
                    .from(b1)
                    .crossJoin(b2)
                    .where(b1.id.lt(b2.id))
                    .orderBy(b1.id.asc(), b2.id.asc())
                    .fetchAll()

        then: "filtered cross join returns expected pairs"
            records.size() == 1
            records[0].get(b1.name.as("name1")) == "Item1"
            records[0].get(b2.name.as("name2")) == "Item2"

        where:
            session << allSessions
    }

    // ==================== Self-join ====================

    def "should perform self-join using QTable with different aliases"() {
        given: "hierarchical employee data"
            EmployeesTable employees = new EmployeesTable("e")
            EmployeesTable managers = new EmployeesTable("m")
            def managerName = managers.name.as("manager")

        when: "performing self-join"
            List<Record> records = session.select(employees.name, managerName)
                    .from(employees)
                    .join(managers).on(employees.managerId.eq(managers.id))
                    .where(employees.name.eq(DSL.constant("Alice")))
                    .fetchAll()

        then: "self-join works correctly"
            records.size() == 1
            records[0].get(employees.name) == "Alice"
            records[0].get(managerName) == "CTO"

        where:
            session << allSessions
    }

    def "should perform left self-join to include rows without matches"() {
        given: "hierarchical employee data"
            EmployeesTable employees = new EmployeesTable("e")
            EmployeesTable managers = new EmployeesTable("m")
            def managerName = managers.name.as("manager")

        when: "performing left self-join"
            List<Record> records = session.select(employees.name, managerName)
                    .from(employees)
                    .leftJoin(managers).on(employees.managerId.eq(managers.id))
                    .where(employees.name.eq(DSL.constant("CEO")))
                    .fetchAll()

        then: "CEO has no manager (NULL)"
            records.size() == 1
            records[0].get(employees.name) == "CEO"
            records[0].get(managerName) == null

        where:
            session << allSessions
    }

    // ==================== Join with additional clauses ====================

    def "should combine join with WHERE, ORDER BY, and LIMIT"() {
        given: "hierarchical employee data"
            EmployeesTable employees = new EmployeesTable("e")
            EmployeesTable managers = new EmployeesTable("m")
            def managerName = managers.name.as("manager")

        when: "using join with additional clauses"
            List<Record> records = session.select(employees.name, employees.salary, managerName)
                    .from(employees)
                    .join(managers).on(employees.managerId.eq(managers.id))
                    .where(employees.department.eq(DSL.constant("Engineering")))
                    .orderBy(employees.salary.desc())
                    .limit(2)
                    .fetchAll()

        then: "query returns paginated results"
            records.size() == 2
            // CTO and Bob are top 2 by salary in Engineering with managers
            // (CTO reports to CEO, Alice/Bob/Charlie report to CTO)
            // CTO: 150000, Bob: 80000, Alice: 75000, Charlie: 70000
            records[0].get(employees.name) == "CTO"
            records[1].get(employees.name) == "Bob"

        where:
            session << allSessions
    }

    def "should handle empty join result"() {
        given: "role without permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "NoPermsRole", List.of()))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "performing inner join where no matches exist"
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .join(permissions).on(roles.id.eq(permissions.roleId))
                    .where(roles.name.eq(DSL.constant("NoPermsRole")))
                    .fetchAll()

        then: "empty result is returned"
            records.isEmpty()

        where:
            session << allSessions
    }

    // ==================== Join with GROUP BY ====================

    def "should combine join with GROUP BY and aggregation"() {
        given: "roles with multiple permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT, RolePermission.PRODUCT_DELETE)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Editor", List.of(RolePermission.PRODUCT_EDIT)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")
            def permissionCount = DSL.count().as("perm_count")

        when: "joining and grouping with count"
            List<Record> records = session.select(roles.name, permissionCount)
                    .from(roles)
                    .join(permissions).on(roles.id.eq(permissions.roleId))
                    .groupBy(roles.name)
                    .orderBy(DSL.desc("perm_count"))
                    .fetchAll()

        then: "aggregated results are correct"
            records.size() == 2
            records[0].get(roles.name) == "Admin"
            records[0].get(permissionCount) == 3L
            records[1].get(roles.name) == "Editor"
            records[1].get(permissionCount) == 1L

        where:
            session << allSessions
    }

    // ==================== Join using QTable.getTableName() ====================

    def "should join using getTableName() from QTable"() {
        given: "basics and roles"
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Test", 100)

            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Test", List.of(RolePermission.PRODUCT_ADD)))

            BasicsTable basics = new BasicsTable("b")
            RolesTable roles = new RolesTable("r")

        when: "using getTableName() for cross join"
            List<Record> records = session.select(basics.name, roles.name.as("role"))
                    .from(basics)
                    .crossJoin(roles.getTableName())
                    .where(basics.name.eq(roles.name))
                    .fetchAll()

        then: "join works correctly"
            records.size() == 1
            records[0].get(basics.name) == "Test"

        where:
            session << allSessions
    }

    // ==================== String-based join and from variants ====================

    def "join with string table name performs inner join"() {
        given: "roles with permissions exist in database"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "StringJoinAdmin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))

            RolesTable roles = new RolesTable("r")
            // Create permissions table without alias to match the string-based join
            RolePermissionsTable permissions = new RolePermissionsTable(null)

        when: "performing inner join using string table name"
            List<Record> records = session.select(roles.id, roles.name, permissions.permission)
                    .from(roles)
                    .join("role_permissions").on(roles.id.eq(permissions.roleId))
                    .where(roles.name.eq(DSL.constant("StringJoinAdmin")))
                    .orderBy(permissions.permission.asc())
                    .fetchAll()

        then: "matching rows from both tables are returned"
            records.size() == 2
            records[0].get(roles.name) == "StringJoinAdmin"
            records[0].get(permissions.permission) == RolePermission.PRODUCT_ADD
            records[1].get(roles.name) == "StringJoinAdmin"
            records[1].get(permissions.permission) == RolePermission.PRODUCT_EDIT

        where:
            session << allSessions
    }

    def "join with table name and schema performs inner join"() {
        given: "roles with permissions exist in database"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "SchemaJoinAdmin", List.of(RolePermission.PRODUCT_DELETE)))

            RolesTable roles = new RolesTable("r")
            // Create permissions table without alias to match the string-based join
            RolePermissionsTable permissions = new RolePermissionsTable(null)

        when: "performing inner join using string table name with null schema (uses default schema)"
            List<Record> records = session.select(roles.id, roles.name, permissions.permission)
                    .from(roles)
                    .join("role_permissions", null).on(roles.id.eq(permissions.roleId))
                    .where(roles.name.eq(DSL.constant("SchemaJoinAdmin")))
                    .fetchAll()

        then: "matching rows from both tables are returned"
            records.size() == 1
            records[0].get(roles.name) == "SchemaJoinAdmin"
            records[0].get(permissions.permission) == RolePermission.PRODUCT_DELETE

        where:
            session << allSessions
    }

    def "leftJoin with string table name"() {
        given: "a role without permissions and a role with permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "StringLeftJoinEmpty", List.of()))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "StringLeftJoinAdmin", List.of(RolePermission.PRODUCT_ADD)))

            RolesTable roles = new RolesTable("r")
            // Create permissions table without alias to match the string-based join
            RolePermissionsTable permissions = new RolePermissionsTable(null)

        when: "performing left join using string table name"
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .leftJoin("role_permissions").on(roles.id.eq(permissions.roleId))
                    .where(roles.name.in(["StringLeftJoinEmpty", "StringLeftJoinAdmin"]))
                    .orderBy(roles.name.asc())
                    .fetchAll()

        then: "all left table rows returned, unmatched have NULL"
            records.size() == 2
            // StringLeftJoinAdmin has permission
            records[0].get(roles.name) == "StringLeftJoinAdmin"
            records[0].get(permissions.permission) == RolePermission.PRODUCT_ADD
            // StringLeftJoinEmpty has NULL permission
            records[1].get(roles.name) == "StringLeftJoinEmpty"
            records[1].get(permissions.permission) == null

        where:
            session << allSessions
    }

    def "from with table name and schema"() {
        given: "basics exist in database"
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "FromSchemaItem", 100)

            // Create basics table without alias to match the string-based from
            BasicsTable basics = new BasicsTable(null)

        when: "using from() with string table name and null schema (uses default schema)"
            List<Record> records = session.select(basics.id, basics.name, basics.numero)
                    .from("basics", null)
                    .where(basics.name.eq(DSL.constant("FromSchemaItem")))
                    .fetchAll()

        then: "data is retrieved correctly"
            records.size() == 1
            records[0].get(basics.name) == "FromSchemaItem"
            records[0].get(basics.numero) == 100

        where:
            session << allSessions
    }
}
