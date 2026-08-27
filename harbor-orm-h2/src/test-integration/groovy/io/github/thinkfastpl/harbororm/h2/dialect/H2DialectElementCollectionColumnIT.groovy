// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.h2.set_user_rbac.role.Permission
import io.github.thinkfastpl.harbororm.h2.set_user_rbac.role.QRoleEntity
import io.github.thinkfastpl.harbororm.h2.set_user_rbac.role.RoleEntity

class H2DialectElementCollectionColumnIT extends H2DialectBaseIT {

    void setup() {
        loadScript("role.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "insert"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(alias)

        when:
            RoleEntity role = new RoleEntity(1L, 'First role', List.of(Permission.USER_CREATE, Permission.USER_READ))
            session.insertEntity(qRoleEntity, role)

        then:
            with(session.selectEntity(qRoleEntity).where(qRoleEntity.id.eq(1L)).fetchSingle()) { r ->
                r.id == 1
                r.name == 'First role'
                r.permissions.size() == 2
                r.permissions.contains(Permission.USER_CREATE)
                r.permissions.contains(Permission.USER_READ)
            }

        where:
            alias << [null, 'a']
    }

    def "select"() {
        given:
            loadScript("role-data.sql")
            QRoleEntity qRoleEntity = new QRoleEntity(alias)

        when:
            List<RoleEntity> roles = session.selectEntity(qRoleEntity).fetchAll()

        then:
            roles.size() == 3
            with(roles[0]) { role ->
                role.id == 1
                role.name == 'Admin'
                role.permissions.size() == 5
                role.permissions.contains(Permission.USER_CREATE)
                role.permissions.contains(Permission.USER_READ)
                role.permissions.contains(Permission.USER_UPDATE)
                role.permissions.contains(Permission.USER_DELETE)
                role.permissions.contains(Permission.ROLE_CRUD)
            }
            with(roles[1]) { role ->
                role.id == 2
                role.name == 'User'
                role.permissions.size() == 1
                role.permissions[0] == Permission.USER_READ
            }
            with(roles[2]) { role ->
                role.id == 3
                role.name == 'Empty'
                role.permissions.size() == 0
            }

        where:
            alias << [null, 'a']
    }

    def "update"() {
        given:
            loadScript("role-data.sql")
            QRoleEntity qRoleEntity = new QRoleEntity(alias)

        when:
            RoleEntity role = session.selectEntity(qRoleEntity).whereIdEq(1L).fetchSingle()
            role.setPermissions(List.of(Permission.ROLE_CRUD))
            session.updateEntity(qRoleEntity, role)

        then:
            with(session.selectEntity(qRoleEntity).whereIdEq(1L).fetchSingle()) { r ->
                r.id == 1
                r.name == 'Admin'
                r.permissions.size() == 1
                r.permissions.contains(Permission.ROLE_CRUD)
            }

        where:
            alias << [null, 'a', 'update']
    }

    def "update when no change"() {
        given:
            loadScript("role-data.sql")
            QRoleEntity qRoleEntity = new QRoleEntity(alias)

        when:
            RoleEntity role = session.selectEntity(qRoleEntity).whereIdEq(1L).fetchSingle()
            role.setName('Admin2')
            session.updateEntity(qRoleEntity, role)

        then:
            with(session.selectEntity(qRoleEntity).whereIdEq(1L).fetchSingle()) { r ->
                r.id == 1
                r.name == 'Admin2'
                role.permissions.size() == 5
                role.permissions.contains(Permission.USER_CREATE)
                role.permissions.contains(Permission.USER_READ)
                role.permissions.contains(Permission.USER_UPDATE)
                role.permissions.contains(Permission.USER_DELETE)
                role.permissions.contains(Permission.ROLE_CRUD)
            }

        where:
            alias << [null, 'a', 'update']
    }

    def "delete all"() {
        given:
            loadScript("role-data.sql")
            QRoleEntity qRoleEntity = new QRoleEntity(alias)

        when:
            List<RoleEntity> roles = session.selectEntity(qRoleEntity).fetchAll()
            session.deleteEntityAll(qRoleEntity, roles)

        then:
            session.selectEntity(qRoleEntity).count() == 0

        where:
            alias << [null, 'a']
    }
}
