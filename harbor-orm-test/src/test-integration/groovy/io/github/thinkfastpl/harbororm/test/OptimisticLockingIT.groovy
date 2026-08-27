// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.exception.OptimisticLockException
import io.github.thinkfastpl.harbororm.test.domain.QVersionedProductEntity
import io.github.thinkfastpl.harbororm.test.domain.VersionedProductEntity

class OptimisticLockingIT extends AbstractHarborIT {

    def "insert initializes version to 0 when null"() {
        given:
            def q = new QVersionedProductEntity(null)
            def entity = new VersionedProductEntity(1L, "Widget", "SKU-001", null)

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

    def "insert respects explicit version value"() {
        given:
            def q = new QVersionedProductEntity(null)
            def entity = new VersionedProductEntity(2L, "Gadget", "SKU-002", 5L)

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

    def "update increments version"() {
        given:
            def q = new QVersionedProductEntity(null)
            def entity = new VersionedProductEntity(3L, "Thing", "SKU-003", null)
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

    def "update with stale version throws OptimisticLockException"() {
        given:
            def q = new QVersionedProductEntity(null)
            def entity = new VersionedProductEntity(4L, "Doohickey", "SKU-004", null)
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
            ex.entityType == VersionedProductEntity
            ex.id == 4L
            ex.version == 0L

        where:
            session << allSessions
    }

    def "delete with correct version succeeds"() {
        given:
            def q = new QVersionedProductEntity(null)
            def entity = new VersionedProductEntity(5L, "Thingamajig", "SKU-005", null)
            session.insertEntity(q, entity)

        when:
            session.deleteEntity(q, entity)

        then:
            notThrown(OptimisticLockException)

        and:
            !session.selectEntity(q).whereIdEq(5L).fetchOne().isPresent()

        where:
            session << allSessions
    }

    def "delete with stale version throws OptimisticLockException"() {
        given:
            def q = new QVersionedProductEntity(null)
            def entity = new VersionedProductEntity(6L, "Whatchamacallit", "SKU-006", null)
            session.insertEntity(q, entity)

        and: "read entity, then update it to bump version"
            def staleEntity = session.selectEntity(q).whereIdEq(6L).fetchSingle()
            def freshEntity = session.selectEntity(q).whereIdEq(6L).fetchSingle()
            freshEntity.setName("Modified")
            session.updateEntity(q, freshEntity)

        when: "try to delete with stale version"
            session.deleteEntity(q, staleEntity)

        then:
            def ex = thrown(OptimisticLockException)
            ex.entityType == VersionedProductEntity
            ex.id == 6L
            ex.version == 0L

        where:
            session << allSessions
    }

    def "version field is updated in-memory after successful update"() {
        given:
            def q = new QVersionedProductEntity(null)
            def entity = new VersionedProductEntity(7L, "Gizmo", "SKU-007", null)
            session.insertEntity(q, entity)

        when:
            entity.setName("Gizmo v2")
            session.updateEntity(q, entity)

        then: "in-memory entity has bumped version without re-reading"
            entity.version == 1L

        and: "can update again with the bumped version"
            entity.setName("Gizmo v3")
            session.updateEntity(q, entity)
            entity.version == 2L

        where:
            session << allSessions
    }

    def "updatable=false column is not modified during update"() {
        given:
            def q = new QVersionedProductEntity(null)
            def entity = new VersionedProductEntity(8L, "Original", "ORIGINAL-SKU", null)
            session.insertEntity(q, entity)

        when:
            entity.setName("Updated Name")
            entity.setSku("CHANGED-SKU")
            session.updateEntity(q, entity)

        then: "sku should not be updated in DB"
            def persisted = session.selectEntity(q).whereIdEq(8L).fetchSingle()
            persisted.name == "Updated Name"
            persisted.sku == "ORIGINAL-SKU"

        where:
            session << allSessions
    }
}
