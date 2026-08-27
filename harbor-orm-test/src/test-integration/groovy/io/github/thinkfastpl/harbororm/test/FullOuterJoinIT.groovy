// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.RolePermissionsTable
import io.github.thinkfastpl.harbororm.query.RolesTable
import io.github.thinkfastpl.harbororm.test.domain.QRoleEntity
import io.github.thinkfastpl.harbororm.test.domain.RoleEntity
import io.github.thinkfastpl.harbororm.test.domain.RolePermission

/**
 * Integration tests for FULL OUTER JOIN functionality.
 * Tests run only on PostgreSQL as H2 does not support FULL OUTER JOIN.
 */
class FullOuterJoinIT extends AbstractHarborIT {

    def "should return all rows from both tables with matching data"() {
        given: "roles with permissions exist in database"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Editor", List.of(RolePermission.PRODUCT_EDIT)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "performing full outer join between roles and permissions"
            List<Record> records = session.select(roles.id, roles.name, permissions.permission)
                    .from(roles)
                    .fullOuterJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .orderBy(roles.id.asc(), permissions.permission.asc())
                    .fetchAll()

        then: "all rows from both tables are returned"
            records.size() == 3

            with(records[0]) { r ->
                r.get(roles.name) == "Admin"
                r.get(permissions.permission) == RolePermission.PRODUCT_ADD
            }
            with(records[1]) { r ->
                r.get(roles.name) == "Admin"
                r.get(permissions.permission) == RolePermission.PRODUCT_EDIT
            }
            with(records[2]) { r ->
                r.get(roles.name) == "Editor"
                r.get(permissions.permission) == RolePermission.PRODUCT_EDIT
            }

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "should return NULL values for unmatched left table rows"() {
        given: "a role without any permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Empty Role", List.of()))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "performing full outer join"
            List<Record> records = session.select(roles.id, roles.name, permissions.permission)
                    .from(roles)
                    .fullOuterJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .orderBy(roles.id.asc())
                    .fetchAll()

        then: "the role is returned with NULL permission"
            records.size() == 1
            with(records[0]) { r ->
                r.get(roles.name) == "Empty Role"
                r.get(permissions.permission) == null
            }

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "should return all rows when both tables have unmatched rows"() {
        given: "roles with varying permissions - some with, some without"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Empty", List.of()))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "performing full outer join"
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .fullOuterJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .orderBy(roles.name.asc(), permissions.permission.asc())
                    .fetchAll()

        then: "all roles and permissions returned - matched ones together, unmatched with NULL"
            records.size() == 3

            // Admin has 2 permissions
            records[0].get(roles.name) == "Admin"
            records[0].get(permissions.permission) == RolePermission.PRODUCT_ADD
            records[1].get(roles.name) == "Admin"
            records[1].get(permissions.permission) == RolePermission.PRODUCT_EDIT

            // Empty has no permissions - NULL
            records[2].get(roles.name) == "Empty"
            records[2].get(permissions.permission) == null

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "should filter by column in WHERE clause"() {
        given: "multiple roles with permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Editor", List.of(RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Viewer", List.of()))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "filtering by role name"
            List<Record> records = session.select(roles.id, roles.name, permissions.permission)
                    .from(roles)
                    .fullOuterJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .where(roles.name.eq("Admin"))
                    .orderBy(permissions.permission.asc())
                    .fetchAll()

        then: "only Admin role rows are returned"
            records.size() == 2
            records.every { it.get(roles.name) == "Admin" }
            records[0].get(permissions.permission) == RolePermission.PRODUCT_ADD
            records[1].get(permissions.permission) == RolePermission.PRODUCT_EDIT

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "should select columns from both tables correctly"() {
        given: "roles with permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "selecting specific columns from both tables"
            List<Record> records = session.select(roles.id, roles.name, permissions.roleId, permissions.permission)
                    .from(roles)
                    .fullOuterJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .fetchAll()

        then: "columns from both tables are accessible in the result"
            records.size() == 1
            with(records[0]) { r ->
                r.get(roles.id) != null
                r.get(roles.name) == "Admin"
                r.get(permissions.roleId) == r.get(roles.id)
                r.get(permissions.permission) == RolePermission.PRODUCT_ADD
            }

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "should support ORDER BY clause"() {
        given: "roles with permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_EDIT, RolePermission.PRODUCT_ADD)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Viewer", List.of()))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "using full outer join with ORDER BY"
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .fullOuterJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .orderBy(roles.name.desc(), permissions.permission.asc())
                    .fetchAll()

        then: "results are sorted correctly"
            records.size() == 3

            // Viewer comes first (descending name order)
            records[0].get(roles.name) == "Viewer"
            records[0].get(permissions.permission) == null

            // Admin with permissions sorted ascending
            records[1].get(roles.name) == "Admin"
            records[1].get(permissions.permission) == RolePermission.PRODUCT_ADD
            records[2].get(roles.name) == "Admin"
            records[2].get(permissions.permission) == RolePermission.PRODUCT_EDIT

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "should return empty result when both tables are empty"() {
        given: "no roles or permissions exist in database"
            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "performing full outer join on empty tables"
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .fullOuterJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .fetchAll()

        then: "no records are returned"
            records.isEmpty()

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "should combine full outer join with limit and offset"() {
        given: "roles with permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Role1", List.of(RolePermission.PRODUCT_ADD)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Role2", List.of(RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Role3", List.of(RolePermission.PRODUCT_DELETE)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "using full outer join with limit and offset"
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .fullOuterJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .orderBy(roles.name.asc())
                    .limit(2)
                    .offset(1)
                    .fetchAll()

        then: "pagination is applied correctly"
            records.size() == 2
            records[0].get(roles.name) == "Role2"
            records[1].get(roles.name) == "Role3"

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }
}
