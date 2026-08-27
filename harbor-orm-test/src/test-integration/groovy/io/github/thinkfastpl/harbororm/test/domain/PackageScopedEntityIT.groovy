// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain

import io.github.thinkfastpl.harbororm.test.AbstractHarborIT

import java.lang.reflect.Modifier

class PackageScopedEntityIT extends AbstractHarborIT {

    def "generated Q class mirrors package scoped visibility"() {
        expect:
            !Modifier.isPublic(PackageScopedEntity.class.getModifiers())
            !Modifier.isPublic(QPackageScopedEntity.class.getModifiers())
    }

    def "should perform CRUD on package scoped entity"() {
        given:
            QPackageScopedEntity qEntity = new QPackageScopedEntity(null)
            PackageScopedEntity entity = new PackageScopedEntity(701L, "first")

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).fetchAll()) { items ->
                items.size() == 1
                with(items[0]) { item ->
                    item.id == 701L
                    item.name == "first"
                }
            }

        when:
            entity.name = "renamed"
            session.updateEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).fetchAll()) { items ->
                items.size() == 1
                items[0].name == "renamed"
            }

        when:
            session.deleteEntityById(qEntity, 701L)

        then:
            session.selectEntity(qEntity).fetchAll().isEmpty()

        where:
            session << allSessions
    }
}
