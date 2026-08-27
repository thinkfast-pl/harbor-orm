// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.QRoleEntity
import io.github.thinkfastpl.harbororm.test.domain.RoleEntity
import io.github.thinkfastpl.harbororm.test.domain.RolePermission

class EntityRoleIT extends AbstractHarborIT {

    def "create"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            RoleEntity roleEntity = new RoleEntity(null, "My role", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT))

        when:
            session.insertEntity(qRoleEntity, roleEntity)

        then:
            roleEntity.id != null
            with(session.selectEntity(qRoleEntity).fetchAll()) { roles ->
                roles.size() == 1
                with(roles[0]) { role ->
                    role.id != null
                    role.id == roleEntity.id
                    role.name == "My role"
                    role.permissions.size() == 2
                    role.permissions.containsAll(List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT))
                }
            }

        where:
            session << allSessions
    }

    def "read"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "My role 1", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "My role 2", List.of(RolePermission.PRODUCT_DELETE)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "My role 3", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT, RolePermission.PRODUCT_DELETE)))

        when:
            List<RoleEntity> entities = session.selectEntity(qRoleEntity)
                    .orderBy(qRoleEntity.id.asc())
                    .fetchAll()

        then:
            entities.size() == 3
            with(entities[0]) { role ->
                role.id != null
                role.name == "My role 1"
                role.permissions.size() == 2
                role.permissions.containsAll(List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT))
            }
            with(entities[1]) { role ->
                role.id != null
                role.name == "My role 2"
                role.permissions.size() == 1
                role.permissions[0] == RolePermission.PRODUCT_DELETE
            }
            with(entities[2]) { role ->
                role.id != null
                role.name == "My role 3"
                role.permissions.size() == 3
                role.permissions.containsAll(List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT, RolePermission.PRODUCT_DELETE))
            }

        where:
            session << allSessions
    }

    def "update"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "My role 1", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))

        when:
            RoleEntity entity = session.selectEntity(qRoleEntity).fetchSingle()
            entity.setName("My new name")
            entity.setPermissions(List.of(RolePermission.PRODUCT_DELETE, RolePermission.PRODUCT_EDIT))
            session.updateEntity(qRoleEntity, entity)

        then:
            with(session.selectEntity(qRoleEntity).fetchSingle()) { role ->
                role.name == "My new name"
                role.permissions.size() == 2
                role.permissions.containsAll(List.of(RolePermission.PRODUCT_DELETE, RolePermission.PRODUCT_EDIT))
            }

        when:
            entity = session.selectEntity(qRoleEntity).fetchSingle()
            entity.setName("My new name 2")
            session.updateEntity(qRoleEntity, entity)

        then:
            with(session.selectEntity(qRoleEntity).fetchSingle()) { role ->
                role.name == "My new name 2"
                role.permissions.size() == 2
                role.permissions.containsAll(List.of(RolePermission.PRODUCT_DELETE, RolePermission.PRODUCT_EDIT))
            }

        when:
            entity = session.selectEntity(qRoleEntity).fetchSingle()
            session.updateEntity(qRoleEntity, entity)

        then:
            with(session.selectEntity(qRoleEntity).fetchSingle()) { role ->
                role.name == "My new name 2"
                role.permissions.size() == 2
                role.permissions.containsAll(List.of(RolePermission.PRODUCT_DELETE, RolePermission.PRODUCT_EDIT))
            }

        when:
            entity = session.selectEntity(qRoleEntity).fetchSingle()
            entity.setName("My new name 3")
            entity.setPermissions(List.of())
            session.updateEntity(qRoleEntity, entity)

        then:
            with(session.selectEntity(qRoleEntity).fetchSingle()) { role ->
                role.name == "My new name 3"
                role.permissions.size() == 0
            }

        when:
            entity = session.selectEntity(qRoleEntity).fetchSingle()
            entity.setName("My new name 4")
            entity.getPermissions().add(RolePermission.PRODUCT_ADD)
            session.updateEntity(qRoleEntity, entity)

        then:
            with(session.selectEntity(qRoleEntity).fetchSingle()) { role ->
                role.name == "My new name 4"
                role.permissions.size() == 1
                role.permissions.containsAll(List.of(RolePermission.PRODUCT_ADD))
            }

        when:
            entity = session.selectEntity(qRoleEntity).fetchSingle()
            entity.setName("My new name 5")
            entity.getPermissions().clear()
            session.updateEntity(qRoleEntity, entity)

        then:
            with(session.selectEntity(qRoleEntity).fetchSingle()) { role ->
                role.name == "My new name 5"
                role.permissions.size() == 0
            }

        where:
            session << allSessions
    }

    def "delete"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            RoleEntity entity

        when:
            session.insertEntity(qRoleEntity, new RoleEntity(null, "My role 1", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))
            entity = session.selectEntity(qRoleEntity).fetchSingle()
            session.deleteEntity(qRoleEntity, entity)

        then:
            session.selectEntity(qRoleEntity).count() == 0

        when:
            session.insertEntity(qRoleEntity, new RoleEntity(null, "My role 1", List.of()))
            entity = session.selectEntity(qRoleEntity).fetchSingle()
            session.deleteEntity(qRoleEntity, entity)

        then:
            session.selectEntity(qRoleEntity).count() == 0

        when:
            session.insertEntity(qRoleEntity, new RoleEntity(null, "My role 1", List.of()))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "My role 2", List.of()))
            entity = session.selectEntity(qRoleEntity).orderBy(qRoleEntity.id.asc()).limit(1).fetchSingle()
            session.deleteEntity(qRoleEntity, entity)

        then:
            session.selectEntity(qRoleEntity).count() == 1

        where:
            session << allSessions
    }

    def "delete all"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            RoleEntity entity = new RoleEntity(null, "My role 1", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT))
            RoleEntity entity2 = new RoleEntity(null, "My role 2", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT))
            RoleEntity entity3 = new RoleEntity(null, "My role 3", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT))
            session.insertEntity(qRoleEntity, entity)
            session.insertEntity(qRoleEntity, entity2)
            session.insertEntity(qRoleEntity, entity3)

        when:
            session.deleteEntityAll(qRoleEntity, [entity, entity2])

        then:
            session.selectEntity(qRoleEntity).count() == 1

        where:
            session << allSessions
    }

    def "delete by id"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            RoleEntity entity = new RoleEntity(null, "My role 1", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT))
            RoleEntity entity2 = new RoleEntity(null, "My role 2", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT))
            session.insertEntity(qRoleEntity, entity)
            session.insertEntity(qRoleEntity, entity2)

        when:
            session.deleteEntityById(qRoleEntity, entity.id)

        then:
            session.selectEntity(qRoleEntity).count() == 1

        where:
            session << allSessions
    }

    def "delete by ids"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            RoleEntity entity = new RoleEntity(null, "My role 1", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT))
            RoleEntity entity2 = new RoleEntity(null, "My role 2", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT))
            RoleEntity entity3 = new RoleEntity(null, "My role 3", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT))
            session.insertEntity(qRoleEntity, entity)
            session.insertEntity(qRoleEntity, entity2)
            session.insertEntity(qRoleEntity, entity3)

        when:
            session.deleteEntityByIds(qRoleEntity, [entity.id, entity2.id])

        then:
            session.selectEntity(qRoleEntity).count() == 1

        where:
            session << allSessions
    }
}
