// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.exception.OptimisticLockException
import io.github.thinkfastpl.harbororm.test.domain.QVersionedProductEntity2
import io.github.thinkfastpl.harbororm.test.domain.VersionedProductEntity2

class OptimisticLockingPrimitiveVersionIT extends AbstractHarborIT {

    def "insert persists primitive long version default of 0"() {
        given:
            def q = new QVersionedProductEntity2(null)
            def entity = new VersionedProductEntity2(1L, "Widget", "SKU-P001", 0L)

        when:
            session.insertEntity(q, entity)

        then:
            entity.version == 0L

        and: "persisted value matches"
            def persisted = session.selectEntity(q).whereIdEq(1L).fetchSingle()
            persisted.version == 0L

        where:
            session << allSessions
    }

    def "insert respects explicit primitive long version value"() {
        given:
            def q = new QVersionedProductEntity2(null)
            def entity = new VersionedProductEntity2(2L, "Gadget", "SKU-P002", 5L)

        when:
            session.insertEntity(q, entity)

        then:
            entity.version == 5L

        and:
            def persisted = session.selectEntity(q).whereIdEq(2L).fetchSingle()
            persisted.version == 5L

        where:
            session << allSessions
    }

    def "update increments primitive long version"() {
        given:
            def q = new QVersionedProductEntity2(null)
            def entity = new VersionedProductEntity2(3L, "Thing", "SKU-P003", 0L)
            session.insertEntity(q, entity)

        when:
            entity.setName("Thing v2")
            session.updateEntity(q, entity)

        then:
            entity.version == 1L

        when:
            entity.setName("Thing v3")
            session.updateEntity(q, entity)

        then:
            entity.version == 2L

        and: "persisted version matches"
            def persisted = session.selectEntity(q).whereIdEq(3L).fetchSingle()
            persisted.version == 2L
            persisted.name == "Thing v3"

        where:
            session << allSessions
    }

    def "update with stale primitive long version throws OptimisticLockException"() {
        given:
            def q = new QVersionedProductEntity2(null)
            def entity = new VersionedProductEntity2(4L, "Doohickey", "SKU-P004", 0L)
            session.insertEntity(q, entity)

        and: "simulate two users reading the same entity"
            def copy1 = session.selectEntity(q).whereIdEq(4L).fetchSingle()
            def copy2 = session.selectEntity(q).whereIdEq(4L).fetchSingle()

        and: "first user updates successfully"
            copy1.setName("Updated by user 1")
            session.updateEntity(q, copy1)

        when: "second user tries to update with stale version"
            copy2.setName("Updated by user 2")
            session.updateEntity(q, copy2)

        then:
            def ex = thrown(OptimisticLockException)
            ex.entityType == VersionedProductEntity2
            ex.id == 4L
            ex.version == 0L

        where:
            session << allSessions
    }

    def "delete with stale primitive long version throws OptimisticLockException"() {
        given:
            def q = new QVersionedProductEntity2(null)
            def entity = new VersionedProductEntity2(5L, "Whatsit", "SKU-P005", 0L)
            session.insertEntity(q, entity)

        and: "read entity, then update it to bump version"
            def staleEntity = session.selectEntity(q).whereIdEq(5L).fetchSingle()
            def freshEntity = session.selectEntity(q).whereIdEq(5L).fetchSingle()
            freshEntity.setName("Modified")
            session.updateEntity(q, freshEntity)

        when: "try to delete with stale version"
            session.deleteEntity(q, staleEntity)

        then:
            def ex = thrown(OptimisticLockException)
            ex.entityType == VersionedProductEntity2
            ex.id == 5L
            ex.version == 0L

        where:
            session << allSessions
    }
}
