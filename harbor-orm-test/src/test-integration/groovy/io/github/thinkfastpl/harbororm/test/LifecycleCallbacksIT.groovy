// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.AuditedEntity
import io.github.thinkfastpl.harbororm.test.domain.QAuditedEntity

import java.time.LocalDateTime

class LifecycleCallbacksIT extends AbstractHarborIT {

    def "should execute @PreInsert callback before insert"() {
        given:
            QAuditedEntity qEntity = new QAuditedEntity(null)
            AuditedEntity entity = new AuditedEntity("Test Entity")

        expect:
            entity.createdAt == null
            entity.insertCounter == 0

        when:
            LocalDateTime beforeInsert = LocalDateTime.now()
            session.insertEntity(qEntity, entity)
            LocalDateTime afterInsert = LocalDateTime.now()

        then:
            entity.id != null
            entity.createdAt != null
            entity.insertCounter == 1
            // Verify createdAt is within the expected time range
            !entity.createdAt.isBefore(beforeInsert.minusSeconds(1))
            !entity.createdAt.isAfter(afterInsert.plusSeconds(1))

        and: "verify persisted entity"
            with(session.selectEntity(qEntity).where(qEntity.id.eq(entity.id)).fetchSingle()) { persisted ->
                persisted.createdAt != null
                persisted.insertCounter == 1
                persisted.updatedAt == null
                persisted.updateCounter == 0
            }

        where:
            session << allSessions
    }

    def "should execute @PreUpdate callback before update"() {
        given:
            QAuditedEntity qEntity = new QAuditedEntity(null)
            AuditedEntity entity = new AuditedEntity("Test Entity")

        and: "insert the entity first"
            session.insertEntity(qEntity, entity)

        expect:
            entity.updatedAt == null
            entity.updateCounter == 0

        when:
            entity.setName("Updated Name")
            LocalDateTime beforeUpdate = LocalDateTime.now()
            session.updateEntity(qEntity, entity)
            LocalDateTime afterUpdate = LocalDateTime.now()

        then:
            entity.updatedAt != null
            entity.updateCounter == 1
            // Verify updatedAt is within the expected time range
            !entity.updatedAt.isBefore(beforeUpdate.minusSeconds(1))
            !entity.updatedAt.isAfter(afterUpdate.plusSeconds(1))

        and: "verify persisted entity"
            with(session.selectEntity(qEntity).where(qEntity.id.eq(entity.id)).fetchSingle()) { persisted ->
                persisted.name == "Updated Name"
                persisted.updatedAt != null
                persisted.updateCounter == 1
                // createdAt should still be set from insert
                persisted.createdAt != null
            }

        where:
            session << allSessions
    }

    def "should modify entity state in @PreInsert callback"() {
        given:
            QAuditedEntity qEntity = new QAuditedEntity(null)
            AuditedEntity entity = new AuditedEntity("Test Entity")
            entity.setInsertCounter(5) // Set initial value

        when:
            session.insertEntity(qEntity, entity)

        then:
            // Counter should be incremented from 5 to 6
            entity.insertCounter == 6

        and: "verify persisted value"
            with(session.selectEntity(qEntity).where(qEntity.id.eq(entity.id)).fetchSingle()) { persisted ->
                persisted.insertCounter == 6
            }

        where:
            session << allSessions
    }

    def "should modify entity state in @PreUpdate callback"() {
        given:
            QAuditedEntity qEntity = new QAuditedEntity(null)
            AuditedEntity entity = new AuditedEntity("Test Entity")
            session.insertEntity(qEntity, entity)

        and: "set initial update counter"
            entity.setUpdateCounter(10)
            session.updateEntity(qEntity, entity)

        when:
            entity.setName("Second Update")
            session.updateEntity(qEntity, entity)

        then:
            // Counter should be incremented from 11 to 12
            entity.updateCounter == 12

        and: "verify persisted value"
            with(session.selectEntity(qEntity).where(qEntity.id.eq(entity.id)).fetchSingle()) { persisted ->
                persisted.updateCounter == 12
            }

        where:
            session << allSessions
    }

    def "should execute both @PreInsert and @PreUpdate callbacks on same entity"() {
        given:
            QAuditedEntity qEntity = new QAuditedEntity(null)
            AuditedEntity entity = new AuditedEntity("Test Entity")

        when: "insert the entity"
            session.insertEntity(qEntity, entity)

        then:
            entity.createdAt != null
            entity.insertCounter == 1
            entity.updatedAt == null
            entity.updateCounter == 0

        when: "update the entity"
            entity.setName("Updated Name")
            session.updateEntity(qEntity, entity)

        then:
            entity.createdAt != null // Still set from insert
            entity.insertCounter == 1 // Not incremented on update
            entity.updatedAt != null
            entity.updateCounter == 1

        and: "verify persisted entity has both timestamps"
            with(session.selectEntity(qEntity).where(qEntity.id.eq(entity.id)).fetchSingle()) { persisted ->
                persisted.createdAt != null
                persisted.updatedAt != null
                persisted.insertCounter == 1
                persisted.updateCounter == 1
            }

        where:
            session << allSessions
    }

    def "should not execute @PreInsert callback on select"() {
        given:
            QAuditedEntity qEntity = new QAuditedEntity(null)
            AuditedEntity entity = new AuditedEntity("Test Entity")
            session.insertEntity(qEntity, entity)
            Integer originalInsertCounter = entity.insertCounter

        when: "select the entity"
            AuditedEntity selected = session.selectEntity(qEntity)
                    .where(qEntity.id.eq(entity.id))
                    .fetchSingle()

        then:
            selected != null
            // Select should not trigger @PreInsert - counter should be unchanged
            selected.insertCounter == originalInsertCounter
            // Verify timestamps are present (not comparing exact values due to DB precision)
            selected.createdAt != null

        where:
            session << allSessions
    }

    def "should not execute @PreUpdate callback on select"() {
        given:
            QAuditedEntity qEntity = new QAuditedEntity(null)
            AuditedEntity entity = new AuditedEntity("Test Entity")
            session.insertEntity(qEntity, entity)
            entity.setName("Updated Name")
            session.updateEntity(qEntity, entity)
            Integer originalUpdateCounter = entity.updateCounter

        when: "select the entity"
            AuditedEntity selected = session.selectEntity(qEntity)
                    .where(qEntity.id.eq(entity.id))
                    .fetchSingle()

        then:
            selected != null
            // Select should not trigger @PreUpdate - counter should be unchanged
            selected.updateCounter == originalUpdateCounter
            // Verify timestamps are present (not comparing exact values due to DB precision)
            selected.updatedAt != null

        where:
            session << allSessions
    }

    def "should execute @PreInsert callback multiple times for multiple inserts"() {
        given:
            QAuditedEntity qEntity = new QAuditedEntity(null)
            AuditedEntity entity1 = new AuditedEntity("Entity 1")
            AuditedEntity entity2 = new AuditedEntity("Entity 2")

        when:
            session.insertEntity(qEntity, entity1)
            session.insertEntity(qEntity, entity2)

        then:
            entity1.createdAt != null
            entity1.insertCounter == 1
            entity2.createdAt != null
            entity2.insertCounter == 1

        and: "both entities should be independently tracked"
            entity1.id != entity2.id

        where:
            session << allSessions
    }

    def "should execute @PreUpdate callback multiple times for multiple updates"() {
        given:
            QAuditedEntity qEntity = new QAuditedEntity(null)
            AuditedEntity entity = new AuditedEntity("Test Entity")
            session.insertEntity(qEntity, entity)

        when:
            entity.setName("Update 1")
            session.updateEntity(qEntity, entity)
            LocalDateTime firstUpdate = entity.updatedAt

            Thread.sleep(10) // Small delay to ensure different timestamps

            entity.setName("Update 2")
            session.updateEntity(qEntity, entity)
            LocalDateTime secondUpdate = entity.updatedAt

        then:
            entity.updateCounter == 2
            firstUpdate != null
            secondUpdate != null
            // Second update should have a later or equal timestamp
            !secondUpdate.isBefore(firstUpdate)

        where:
            session << allSessions
    }

    def "should execute @PreDelete callback before delete"() {
        given:
            QAuditedEntity qEntity = new QAuditedEntity(null)
            AuditedEntity entity = new AuditedEntity("To Delete")
            session.insertEntity(qEntity, entity)

        expect:
            !entity.preDeleteCalled

        when:
            session.deleteEntity(qEntity, entity)

        then:
            entity.preDeleteCalled

        where:
            session << allSessions
    }

    def "should execute @PostInsert callback after insert"() {
        given:
            QAuditedEntity qEntity = new QAuditedEntity(null)
            AuditedEntity entity = new AuditedEntity("Test Entity")

        expect:
            entity.postInsertCounter == 0

        when:
            session.insertEntity(qEntity, entity)

        then:
            entity.postInsertCounter == 1

        where:
            session << allSessions
    }

    def "should execute @PostUpdate callback after update"() {
        given:
            QAuditedEntity qEntity = new QAuditedEntity(null)
            AuditedEntity entity = new AuditedEntity("Test Entity")
            session.insertEntity(qEntity, entity)

        expect:
            entity.postUpdateCounter == 0

        when:
            entity.setName("Updated Name")
            session.updateEntity(qEntity, entity)

        then:
            entity.postUpdateCounter == 1

        where:
            session << allSessions
    }

    def "should execute @PostDelete callback after delete"() {
        given:
            QAuditedEntity qEntity = new QAuditedEntity(null)
            AuditedEntity entity = new AuditedEntity("To Delete")
            session.insertEntity(qEntity, entity)

        expect:
            !entity.postDeleteCalled

        when:
            session.deleteEntity(qEntity, entity)

        then:
            entity.postDeleteCalled

        where:
            session << allSessions
    }

    def "should execute @PostInsert callback per entity in batch insert"() {
        given:
            QAuditedEntity qEntity = new QAuditedEntity(null)
            AuditedEntity entity1 = new AuditedEntity("Batch 1")
            AuditedEntity entity2 = new AuditedEntity("Batch 2")
            AuditedEntity entity3 = new AuditedEntity("Batch 3")

        expect:
            entity1.postInsertCounter == 0
            entity2.postInsertCounter == 0
            entity3.postInsertCounter == 0

        when:
            session.insertEntityBatch(qEntity, [entity1, entity2, entity3])

        then:
            entity1.postInsertCounter == 1
            entity2.postInsertCounter == 1
            entity3.postInsertCounter == 1

        where:
            session << allSessions
    }

    def "should execute @PostUpdate multiple times for multiple updates"() {
        given:
            QAuditedEntity qEntity = new QAuditedEntity(null)
            AuditedEntity entity = new AuditedEntity("Test Entity")
            session.insertEntity(qEntity, entity)

        when:
            entity.setName("Update 1")
            session.updateEntity(qEntity, entity)
            entity.setName("Update 2")
            session.updateEntity(qEntity, entity)

        then:
            entity.postUpdateCounter == 2

        where:
            session << allSessions
    }
}
