// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.BasicEntity
import io.github.thinkfastpl.harbororm.test.domain.QBasicEntity
import io.github.thinkfastpl.harbororm.test.domain.QUserEntity
import io.github.thinkfastpl.harbororm.test.domain.UserEntity

/**
 * Integration tests for replaceEntity (upsert) operation.
 *
 * replaceEntity performs an upsert:
 * - If entity doesn't exist (ID is null or not found in DB): INSERT
 * - If entity exists (found by ID): UPDATE
 */
class ReplaceEntityIT extends AbstractHarborIT {

    def "should insert entity when entity does not exist in database"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            // BasicEntity has Long ID without auto-generation
            BasicEntity entity = new BasicEntity(999L, "New Entity", 42)

        expect:
            session.selectEntity(qEntity).count() == 0
            !session.selectEntity(qEntity).whereIdEq(999L).exists()

        when:
            session.replaceEntity(qEntity, entity)

        then: "entity should be inserted"
            session.selectEntity(qEntity).count() == 1
            with(session.selectEntity(qEntity).whereIdEq(999L).fetchSingle()) { persisted ->
                persisted.id == 999L
                persisted.name == "New Entity"
                persisted.numero == 42
            }

        where:
            session << allSessions
    }

    def "should update entity when entity exists in database"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            Long entityId = 100L

            // Insert initial entity
            BasicEntity initialEntity = new BasicEntity(entityId, "Initial Name", 10)
            session.insertEntity(qEntity, initialEntity)

        expect:
            session.selectEntity(qEntity).whereIdEq(entityId).exists()
            with(session.selectEntity(qEntity).whereIdEq(entityId).fetchSingle()) { persisted ->
                persisted.name == "Initial Name"
                persisted.numero == 10
            }

        when: "replace with updated values"
            BasicEntity updatedEntity = new BasicEntity(entityId, "Updated Name", 99)
            session.replaceEntity(qEntity, updatedEntity)

        then: "entity should be updated"
            session.selectEntity(qEntity).count() == 1
            with(session.selectEntity(qEntity).whereIdEq(entityId).fetchSingle()) { persisted ->
                persisted.id == entityId
                persisted.name == "Updated Name"
                persisted.numero == 99
            }

        where:
            session << allSessions
    }

    def "should preserve ID after replace on existing entity"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            Long originalId = 200L

            // Insert initial entity
            session.insertEntity(qEntity, new BasicEntity(originalId, "Original", 1))

        when: "replace with same ID but different values"
            BasicEntity replacementEntity = new BasicEntity(originalId, "Replaced", 2)
            session.replaceEntity(qEntity, replacementEntity)

        then: "ID should remain the same"
            session.selectEntity(qEntity).count() == 1
            with(session.selectEntity(qEntity).whereIdEq(originalId).fetchSingle()) { persisted ->
                persisted.id == originalId
                persisted.name == "Replaced"
                persisted.numero == 2
            }

        where:
            session << allSessions
    }

    def "should replace all fields of entity"() {
        given:
            QUserEntity qEntity = new QUserEntity(null)
            UUID userId = UUID.randomUUID()

            // Insert initial entity
            session.insertEntity(qEntity, new UserEntity(
                    userId,
                    "original@example.com",
                    "originalPwd",
                    "OriginalFirst",
                    "OriginalLast",
                    "111111111"
            ))

        expect:
            session.selectEntity(qEntity).whereIdEq(userId).exists()

        when: "replace with completely different values"
            UserEntity replacementEntity = new UserEntity(
                    userId,
                    "replaced@example.com",
                    "replacedPwd",
                    "ReplacedFirst",
                    "ReplacedLast",
                    "999999999"
            )
            session.replaceEntity(qEntity, replacementEntity)

        then: "all fields should be replaced"
            session.selectEntity(qEntity).count() == 1
            with(session.selectEntity(qEntity).whereIdEq(userId).fetchSingle()) { persisted ->
                persisted.id == userId
                persisted.email == "replaced@example.com"
                persisted.password == "replacedPwd"
                persisted.firstName == "ReplacedFirst"
                persisted.lastName == "ReplacedLast"
                persisted.phoneNumber == "999999999"
            }

        where:
            session << allSessions
    }

    def "should behave like insertEntity when entity does not exist"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            Long entityId = 300L

            BasicEntity entityForInsert = new BasicEntity(entityId, "Insert Test", 30)
            BasicEntity entityForReplace = new BasicEntity(entityId + 1, "Replace Test", 31)

        expect:
            session.selectEntity(qEntity).count() == 0

        when: "insert one entity using insertEntity"
            session.insertEntity(qEntity, entityForInsert)

        and: "insert another entity using replaceEntity (upsert as insert)"
            session.replaceEntity(qEntity, entityForReplace)

        then: "both entities should exist"
            session.selectEntity(qEntity).count() == 2
            session.selectEntity(qEntity).whereIdEq(entityId).exists()
            session.selectEntity(qEntity).whereIdEq(entityId + 1).exists()

        and: "both should have correct values"
            with(session.selectEntity(qEntity).whereIdEq(entityId).fetchSingle()) { e1 ->
                e1.name == "Insert Test"
                e1.numero == 30
            }
            with(session.selectEntity(qEntity).whereIdEq(entityId + 1).fetchSingle()) { e2 ->
                e2.name == "Replace Test"
                e2.numero == 31
            }

        where:
            session << allSessions
    }

    def "should behave like updateEntity when entity exists"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            Long entityId = 400L

            // Insert initial entity
            session.insertEntity(qEntity, new BasicEntity(entityId, "Initial", 40))

        when: "update using updateEntity"
            BasicEntity entityForUpdate = new BasicEntity(entityId, "Updated via Update", 41)
            session.updateEntity(qEntity, entityForUpdate)

            String nameAfterUpdate = session.selectEntity(qEntity).whereIdEq(entityId).fetchSingle().name
            int numeroAfterUpdate = session.selectEntity(qEntity).whereIdEq(entityId).fetchSingle().numero

        and: "then replace using replaceEntity"
            BasicEntity entityForReplace = new BasicEntity(entityId, "Updated via Replace", 42)
            session.replaceEntity(qEntity, entityForReplace)

        then: "updateEntity should have updated the values"
            nameAfterUpdate == "Updated via Update"
            numeroAfterUpdate == 41

        and: "replaceEntity should have updated the values similarly"
            session.selectEntity(qEntity).count() == 1
            with(session.selectEntity(qEntity).whereIdEq(entityId).fetchSingle()) { persisted ->
                persisted.id == entityId
                persisted.name == "Updated via Replace"
                persisted.numero == 42
            }

        where:
            session << allSessions
    }

    def "should update entity when called on same entity twice"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            Long entityId = 500L

            // First, insert the entity using replaceEntity
            session.replaceEntity(qEntity, new BasicEntity(entityId, "First", 1))

        expect: "entity was inserted"
            session.selectEntity(qEntity).whereIdEq(entityId).count() == 1
            session.selectEntity(qEntity).whereIdEq(entityId).fetchSingle().name == "First"

        when: "second replaceEntity - should update"
            session.replaceEntity(qEntity, new BasicEntity(entityId, "Second", 2))

        then:
            session.selectEntity(qEntity).count() == 1
            with(session.selectEntity(qEntity).whereIdEq(entityId).fetchSingle()) { persisted ->
                persisted.name == "Second"
                persisted.numero == 2
            }

        where:
            session << allSessions
    }

    def "should handle replaceEntity on different entities independently"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            Long id1 = 600L
            Long id2 = 601L

            // Insert first entity
            session.insertEntity(qEntity, new BasicEntity(id1, "Entity One", 60))

        when: "replace existing entity and insert new entity"
            session.replaceEntity(qEntity, new BasicEntity(id1, "Entity One Updated", 61))
            session.replaceEntity(qEntity, new BasicEntity(id2, "Entity Two New", 62))

        then: "both operations should succeed independently"
            session.selectEntity(qEntity).count() == 2
            with(session.selectEntity(qEntity).whereIdEq(id1).fetchSingle()) { e1 ->
                e1.name == "Entity One Updated"
                e1.numero == 61
            }
            with(session.selectEntity(qEntity).whereIdEq(id2).fetchSingle()) { e2 ->
                e2.name == "Entity Two New"
                e2.numero == 62
            }

        where:
            session << allSessions
    }
}
