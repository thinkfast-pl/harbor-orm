// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.h2.set_user_rbac.role.Permission
import io.github.thinkfastpl.harbororm.query.RolePermissionTable
import io.github.thinkfastpl.harbororm.query.RoleTable

class H2DialectSelectJoinIT extends H2DialectBaseIT {

    void setup() {
        loadScript("role.sql")
        loadScript("role-data.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "inner join 1"() {
        given:
            RoleTable role = new RoleTable("r")
            RolePermissionTable permission = new RolePermissionTable("rp")

        when:
            List<Record> records = session.select(role.name, permission.permission)
                    .from(role)
                    .innerJoin(permission).on(role.id.eq(permission.roleId))
                    .where(role.id.eq(1L))
                    .orderBy(
                            role.id.asc(),
                            permission.permission.asc()
                    )
                    .fetchAll()

        then:
            records.size() == 5
            with(records[0]) {r ->
                r.get(role.name) == "Admin"
                r.get(permission.permission) == Permission.ROLE_CRUD
            }
            with(records[1]) {r ->
                r.get(role.name) == "Admin"
                r.get(permission.permission) == Permission.USER_CREATE
            }
            with(records[2]) {r ->
                r.get(role.name) == "Admin"
                r.get(permission.permission) == Permission.USER_DELETE
            }
            with(records[3]) {r ->
                r.get(role.name) == "Admin"
                r.get(permission.permission) == Permission.USER_READ
            }
            with(records[4]) {r ->
                r.get(role.name) == "Admin"
                r.get(permission.permission) == Permission.USER_UPDATE
            }
    }

    def "inner join 2"() {
        given:
            RoleTable role = new RoleTable("r")
            RolePermissionTable permission = new RolePermissionTable("rp")

        when:
            List<Record> records = session.select(role.name, permission.permission)
                    .from(role)
                    .innerJoin(permission).on(role.id.eq(permission.roleId))
                    .where(role.id.eq(3L))
                    .orderBy(
                            role.id.asc(),
                            permission.permission.asc()
                    )
                    .fetchAll()

        then:
            records.isEmpty()
    }

    def "left join 1"() {
        given:
            RoleTable role = new RoleTable("r")
            RolePermissionTable permission = new RolePermissionTable("rp")

        when:
            List<Record> records = session.select(role.name, permission.permission)
                    .from(role)
                    .leftJoin(permission).on(role.id.eq(permission.roleId))
                    .where(role.id.eq(1L))
                    .orderBy(
                            role.id.asc(),
                            permission.permission.asc()
                    )
                    .fetchAll()

        then:
            records.size() == 5
            with(records[0]) {r ->
                r.get(role.name) == "Admin"
                r.get(permission.permission) == Permission.ROLE_CRUD
            }
            with(records[1]) {r ->
                r.get(role.name) == "Admin"
                r.get(permission.permission) == Permission.USER_CREATE
            }
            with(records[2]) {r ->
                r.get(role.name) == "Admin"
                r.get(permission.permission) == Permission.USER_DELETE
            }
            with(records[3]) {r ->
                r.get(role.name) == "Admin"
                r.get(permission.permission) == Permission.USER_READ
            }
            with(records[4]) {r ->
                r.get(role.name) == "Admin"
                r.get(permission.permission) == Permission.USER_UPDATE
            }
    }

    def "left join 2"() {
        given:
            RoleTable role = new RoleTable("r")
            RolePermissionTable permission = new RolePermissionTable("rp")

        when:
            List<Record> records = session.select(role.name, permission.permission)
                    .from(role)
                    .leftJoin(permission).on(role.id.eq(permission.roleId))
                    .where(role.id.eq(3L))
                    .orderBy(
                            role.id.asc(),
                            permission.permission.asc()
                    )
                    .fetchAll()

        then:
            records.size() == 1
            with(records[0]) {r ->
                r.get(role.name) == "Empty"
                r.get(permission.permission) == null
            }
    }

    def "right join 1"() {
        given:
            RoleTable role = new RoleTable("r")
            RolePermissionTable permission = new RolePermissionTable("rp")

        when:
            List<Record> records = session.select(role.name, permission.permission)
                    .from(role)
                    .rightJoin(permission).on(role.id.eq(permission.roleId))
                    .where(role.id.eq(1L))
                    .orderBy(
                            role.id.asc(),
                            permission.permission.asc()
                    )
                    .fetchAll()

        then:
            records.size() == 5
            with(records[0]) {r ->
                r.get(role.name) == "Admin"
                r.get(permission.permission) == Permission.ROLE_CRUD
            }
            with(records[1]) {r ->
                r.get(role.name) == "Admin"
                r.get(permission.permission) == Permission.USER_CREATE
            }
            with(records[2]) {r ->
                r.get(role.name) == "Admin"
                r.get(permission.permission) == Permission.USER_DELETE
            }
            with(records[3]) {r ->
                r.get(role.name) == "Admin"
                r.get(permission.permission) == Permission.USER_READ
            }
            with(records[4]) {r ->
                r.get(role.name) == "Admin"
                r.get(permission.permission) == Permission.USER_UPDATE
            }
    }

    def "rightJoin join 2"() {
        given:
            RoleTable role = new RoleTable("r")
            RolePermissionTable permission = new RolePermissionTable("rp")

        when:
            List<Record> records = session.select(role.name, permission.permission)
                    .from(role)
                    .rightJoin(permission).on(role.id.eq(permission.roleId))
                    .where(role.id.eq(3L))
                    .orderBy(
                            role.id.asc(),
                            permission.permission.asc()
                    )
                    .fetchAll()

        then:
            records.isEmpty()
    }

    def "cross join 1"() {
        given:
            RoleTable role = new RoleTable("r")
            RolePermissionTable permission = new RolePermissionTable("rp")

        when:
            List<Record> records = session.select(role.name, permission.permission)
                    .from(role)
                    .crossJoin(permission)
                    .where(role.id.eq(3L))
                    .orderBy(
                            role.id.asc(),
                            permission.permission.asc()
                    )
                    .fetchAll()

        then:
            records.size() == 6
    }

    def "cross join 2"() {
        given:
            RoleTable role = new RoleTable("r")
            RolePermissionTable permission = new RolePermissionTable("rp")

        when:
            List<Record> records = session.select(role.name, permission.permission)
                    .from(role)
                    .crossJoin(permission)
                    .orderBy(
                            role.id.asc(),
                            permission.permission.asc()
                    )
                    .fetchAll()

        then:
            records.size() == 18
    }
}
