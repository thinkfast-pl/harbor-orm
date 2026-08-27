// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.RolePermissionsTable
import io.github.thinkfastpl.harbororm.query.RolesTable
import io.github.thinkfastpl.harbororm.test.domain.QRoleEntity
import io.github.thinkfastpl.harbororm.test.domain.RoleEntity
import io.github.thinkfastpl.harbororm.test.domain.RolePermission

/**
 * Integration tests for LEFT JOIN functionality.
 * Tests run on both H2 and PostgreSQL databases.
 */
class LeftJoinIT extends AbstractHarborIT {

    def "should return all rows from left table with matching right table data"() {
        given: "roles with permissions exist in database"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Editor", List.of(RolePermission.PRODUCT_EDIT)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "performing left join between roles and permissions"
            List<Record> records = session.select(roles.id, roles.name, permissions.permission)
                    .from(roles)
                    .leftJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .orderBy(roles.id.asc(), permissions.permission.asc())
                    .fetchAll()

        then: "all role rows are returned with their corresponding permissions"
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
            session << allSessions
    }

    def "should return NULL values for unmatched right table rows"() {
        given: "a role without any permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Empty Role", List.of()))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "performing left join"
            List<Record> records = session.select(roles.id, roles.name, permissions.permission)
                    .from(roles)
                    .leftJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .orderBy(roles.id.asc())
                    .fetchAll()

        then: "the role is returned with NULL permission"
            records.size() == 1
            with(records[0]) { r ->
                r.get(roles.name) == "Empty Role"
                r.get(permissions.permission) == null
            }

        where:
            session << allSessions
    }

    def "should filter by left table column in WHERE clause"() {
        given: "multiple roles with permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Editor", List.of(RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Viewer", List.of()))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "filtering by role name from left table"
            List<Record> records = session.select(roles.id, roles.name, permissions.permission)
                    .from(roles)
                    .leftJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .where(roles.name.eq("Admin"))
                    .orderBy(permissions.permission.asc())
                    .fetchAll()

        then: "only Admin role rows are returned"
            records.size() == 2
            records.every { it.get(roles.name) == "Admin" }
            records[0].get(permissions.permission) == RolePermission.PRODUCT_ADD
            records[1].get(permissions.permission) == RolePermission.PRODUCT_EDIT

        where:
            session << allSessions
    }

    def "should filter by right table column in WHERE clause after join"() {
        given: "multiple roles with various permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT, RolePermission.PRODUCT_DELETE)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Editor", List.of(RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Viewer", List.of()))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "filtering by permission from right table"
            List<Record> records = session.select(roles.id, roles.name, permissions.permission)
                    .from(roles)
                    .leftJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .where(permissions.permission.eq(RolePermission.PRODUCT_EDIT))
                    .orderBy(roles.name.asc())
                    .fetchAll()

        then: "only rows with PRODUCT_EDIT permission are returned"
            records.size() == 2
            with(records[0]) { r ->
                r.get(roles.name) == "Admin"
                r.get(permissions.permission) == RolePermission.PRODUCT_EDIT
            }
            with(records[1]) { r ->
                r.get(roles.name) == "Editor"
                r.get(permissions.permission) == RolePermission.PRODUCT_EDIT
            }

        where:
            session << allSessions
    }

    def "should support multiple conditions in ON clause"() {
        given: "roles with permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Editor", List.of(RolePermission.PRODUCT_EDIT)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "using multiple conditions in ON clause"
            List<Record> records = session.select(roles.id, roles.name, permissions.permission)
                    .from(roles)
                    .leftJoin(permissions).on(
                        roles.id.eq(permissions.roleId)
                                .and(permissions.permission.eq(RolePermission.PRODUCT_ADD))
                    )
                    .orderBy(roles.name.asc())
                    .fetchAll()

        then: "join respects both conditions - only PRODUCT_ADD permissions match"
            records.size() == 2

            with(records[0]) { r ->
                r.get(roles.name) == "Admin"
                r.get(permissions.permission) == RolePermission.PRODUCT_ADD
            }
            with(records[1]) { r ->
                r.get(roles.name) == "Editor"
                r.get(permissions.permission) == null  // No PRODUCT_ADD permission for Editor
            }

        where:
            session << allSessions
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
                    .leftJoin(permissions).on(roles.id.eq(permissions.roleId))
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
            session << allSessions
    }

    def "should handle left join with mixed matched and unmatched rows"() {
        given: "roles with varying permissions - some with, some without"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Empty", List.of()))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Deleter", List.of(RolePermission.PRODUCT_DELETE)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "performing left join"
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .leftJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .orderBy(roles.name.asc(), permissions.permission.asc())
                    .fetchAll()

        then: "all roles returned - matched ones with permissions, unmatched with NULL"
            records.size() == 4

            // Admin has 2 permissions
            records[0].get(roles.name) == "Admin"
            records[0].get(permissions.permission) == RolePermission.PRODUCT_ADD
            records[1].get(roles.name) == "Admin"
            records[1].get(permissions.permission) == RolePermission.PRODUCT_EDIT

            // Deleter has 1 permission
            records[2].get(roles.name) == "Deleter"
            records[2].get(permissions.permission) == RolePermission.PRODUCT_DELETE

            // Empty has no permissions - NULL
            records[3].get(roles.name) == "Empty"
            records[3].get(permissions.permission) == null

        where:
            session << allSessions
    }

    def "should return empty result when left table is empty"() {
        given: "no roles exist in database"
            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "performing left join on empty left table"
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .leftJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .fetchAll()

        then: "no records are returned"
            records.isEmpty()

        where:
            session << allSessions
    }

    def "should combine left join with limit and offset"() {
        given: "roles with permissions"
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Role1", List.of(RolePermission.PRODUCT_ADD)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Role2", List.of(RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Role3", List.of(RolePermission.PRODUCT_DELETE)))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")

        when: "using left join with limit and offset"
            List<Record> records = session.select(roles.name, permissions.permission)
                    .from(roles)
                    .leftJoin(permissions).on(roles.id.eq(permissions.roleId))
                    .orderBy(roles.name.asc())
                    .limit(2)
                    .offset(1)
                    .fetchAll()

        then: "pagination is applied correctly"
            records.size() == 2
            records[0].get(roles.name) == "Role2"
            records[1].get(roles.name) == "Role3"

        where:
            session << allSessions
    }
}
