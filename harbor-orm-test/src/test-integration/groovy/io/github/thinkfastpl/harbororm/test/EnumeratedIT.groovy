// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.EnumeratedTestEntity
import io.github.thinkfastpl.harbororm.test.domain.QEnumeratedTestEntity

import static io.github.thinkfastpl.harbororm.test.domain.EnumeratedTestEntity.Priority
import static io.github.thinkfastpl.harbororm.test.domain.EnumeratedTestEntity.Status

/**
 * Integration tests for @Enumerated annotation functionality.
 * Tests both ORDINAL and STRING enum mapping types.
 */
class EnumeratedIT extends AbstractHarborIT {

    def "should insert entity with @Enumerated STRING field"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            EnumeratedTestEntity entity = new EnumeratedTestEntity(
                    1L, "Test Entity", Status.ACTIVE, Priority.HIGH, null, null
            )

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { loaded ->
                loaded.id == 1L
                loaded.name == "Test Entity"
                loaded.status == Status.ACTIVE
                loaded.priority == Priority.HIGH
                loaded.secondaryStatus == null
                loaded.secondaryPriority == null
            }

        where:
            session << allSessions
    }

    def "should insert entity with @Enumerated ORDINAL field"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            EnumeratedTestEntity entity = new EnumeratedTestEntity(
                    2L, "Ordinal Test", Status.PENDING, Priority.CRITICAL, null, null
            )

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(2L)).fetchSingle()) { loaded ->
                loaded.id == 2L
                loaded.status == Status.PENDING
                // CRITICAL has ordinal 3
                loaded.priority == Priority.CRITICAL
            }

        where:
            session << allSessions
    }

    def "should select entities with enumerated fields"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            session.insertEntity(qEntity, new EnumeratedTestEntity(3L, "Entity 1", Status.ACTIVE, Priority.LOW, null, null))
            session.insertEntity(qEntity, new EnumeratedTestEntity(4L, "Entity 2", Status.INACTIVE, Priority.MEDIUM, null, null))
            session.insertEntity(qEntity, new EnumeratedTestEntity(5L, "Entity 3", Status.DELETED, Priority.HIGH, null, null))

        when:
            List<EnumeratedTestEntity> entities = session.selectEntity(qEntity)
                    .where(qEntity.id.ge(3L).and(qEntity.id.le(5L)))
                    .orderBy(qEntity.id.asc())
                    .fetchAll()

        then:
            entities.size() == 3
            with(entities[0]) { e ->
                e.id == 3L
                e.status == Status.ACTIVE
                e.priority == Priority.LOW
            }
            with(entities[1]) { e ->
                e.id == 4L
                e.status == Status.INACTIVE
                e.priority == Priority.MEDIUM
            }
            with(entities[2]) { e ->
                e.id == 5L
                e.status == Status.DELETED
                e.priority == Priority.HIGH
            }

        where:
            session << allSessions
    }

    def "should update entity with enumerated fields"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            EnumeratedTestEntity entity = new EnumeratedTestEntity(
                    6L, "Update Test", Status.ACTIVE, Priority.LOW, null, null
            )
            session.insertEntity(qEntity, entity)

        when:
            EnumeratedTestEntity loaded = session.selectEntity(qEntity).where(qEntity.id.eq(6L)).fetchSingle()
            loaded.setStatus(Status.INACTIVE)
            loaded.setPriority(Priority.CRITICAL)
            session.updateEntity(qEntity, loaded)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(6L)).fetchSingle()) { updated ->
                updated.id == 6L
                updated.status == Status.INACTIVE
                updated.priority == Priority.CRITICAL
            }

        where:
            session << allSessions
    }

    def "should query by STRING enumerated field"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            session.insertEntity(qEntity, new EnumeratedTestEntity(7L, "Active 1", Status.ACTIVE, Priority.LOW, null, null))
            session.insertEntity(qEntity, new EnumeratedTestEntity(8L, "Inactive", Status.INACTIVE, Priority.MEDIUM, null, null))
            session.insertEntity(qEntity, new EnumeratedTestEntity(9L, "Active 2", Status.ACTIVE, Priority.HIGH, null, null))

        when:
            List<EnumeratedTestEntity> activeEntities = session.selectEntity(qEntity)
                    .where(qEntity.status.eq(Status.ACTIVE))
                    .orderBy(qEntity.id.asc())
                    .fetchAll()

        then:
            activeEntities.size() == 2
            activeEntities[0].id == 7L
            activeEntities[0].name == "Active 1"
            activeEntities[1].id == 9L
            activeEntities[1].name == "Active 2"

        where:
            session << allSessions
    }

    def "should query by ORDINAL enumerated field"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            session.insertEntity(qEntity, new EnumeratedTestEntity(10L, "Low Priority", Status.ACTIVE, Priority.LOW, null, null))
            session.insertEntity(qEntity, new EnumeratedTestEntity(11L, "High Priority", Status.ACTIVE, Priority.HIGH, null, null))
            session.insertEntity(qEntity, new EnumeratedTestEntity(12L, "Another High", Status.INACTIVE, Priority.HIGH, null, null))

        when:
            List<EnumeratedTestEntity> highPriorityEntities = session.selectEntity(qEntity)
                    .where(qEntity.priority.eq(Priority.HIGH))
                    .orderBy(qEntity.id.asc())
                    .fetchAll()

        then:
            highPriorityEntities.size() == 2
            highPriorityEntities[0].id == 11L
            highPriorityEntities[0].name == "High Priority"
            highPriorityEntities[1].id == 12L
            highPriorityEntities[1].name == "Another High"

        where:
            session << allSessions
    }

    def "should handle null enum values"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            EnumeratedTestEntity entity = new EnumeratedTestEntity(
                    13L, "Null Enums", Status.ACTIVE, Priority.LOW, null, null
            )

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(13L)).fetchSingle()) { loaded ->
                loaded.id == 13L
                loaded.secondaryStatus == null
                loaded.secondaryPriority == null
            }

        where:
            session << allSessions
    }

    def "should insert and select nullable enum fields with values"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            EnumeratedTestEntity entity = new EnumeratedTestEntity(
                    14L, "With Nullable", Status.ACTIVE, Priority.LOW, Status.PENDING, Priority.MEDIUM
            )

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(14L)).fetchSingle()) { loaded ->
                loaded.id == 14L
                loaded.status == Status.ACTIVE
                loaded.priority == Priority.LOW
                loaded.secondaryStatus == Status.PENDING
                loaded.secondaryPriority == Priority.MEDIUM
            }

        where:
            session << allSessions
    }

    def "should update nullable enum field from null to value"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            EnumeratedTestEntity entity = new EnumeratedTestEntity(
                    15L, "Update Nullable", Status.ACTIVE, Priority.LOW, null, null
            )
            session.insertEntity(qEntity, entity)

        when:
            EnumeratedTestEntity loaded = session.selectEntity(qEntity).where(qEntity.id.eq(15L)).fetchSingle()
            loaded.setSecondaryStatus(Status.DELETED)
            loaded.setSecondaryPriority(Priority.CRITICAL)
            session.updateEntity(qEntity, loaded)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(15L)).fetchSingle()) { updated ->
                updated.secondaryStatus == Status.DELETED
                updated.secondaryPriority == Priority.CRITICAL
            }

        where:
            session << allSessions
    }

    def "should update nullable enum field from value to null"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            EnumeratedTestEntity entity = new EnumeratedTestEntity(
                    16L, "Clear Nullable", Status.ACTIVE, Priority.LOW, Status.INACTIVE, Priority.HIGH
            )
            session.insertEntity(qEntity, entity)

        when:
            EnumeratedTestEntity loaded = session.selectEntity(qEntity).where(qEntity.id.eq(16L)).fetchSingle()
            loaded.setSecondaryStatus(null)
            loaded.setSecondaryPriority(null)
            session.updateEntity(qEntity, loaded)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(16L)).fetchSingle()) { updated ->
                updated.secondaryStatus == null
                updated.secondaryPriority == null
            }

        where:
            session << allSessions
    }

    def "should query by nullable enum field with isNull"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            session.insertEntity(qEntity, new EnumeratedTestEntity(17L, "Has Secondary", Status.ACTIVE, Priority.LOW, Status.PENDING, null))
            session.insertEntity(qEntity, new EnumeratedTestEntity(18L, "No Secondary", Status.ACTIVE, Priority.LOW, null, null))

        when:
            List<EnumeratedTestEntity> withNullSecondary = session.selectEntity(qEntity)
                    .where(qEntity.secondaryStatus.isNull())
                    .fetchAll()

        then:
            withNullSecondary.size() == 1
            withNullSecondary[0].id == 18L
            withNullSecondary[0].name == "No Secondary"

        where:
            session << allSessions
    }

    def "should query with combined enum conditions"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            session.insertEntity(qEntity, new EnumeratedTestEntity(19L, "Active Low", Status.ACTIVE, Priority.LOW, null, null))
            session.insertEntity(qEntity, new EnumeratedTestEntity(20L, "Active High", Status.ACTIVE, Priority.HIGH, null, null))
            session.insertEntity(qEntity, new EnumeratedTestEntity(21L, "Inactive High", Status.INACTIVE, Priority.HIGH, null, null))

        when:
            List<EnumeratedTestEntity> activeHighPriority = session.selectEntity(qEntity)
                    .where(qEntity.status.eq(Status.ACTIVE).and(qEntity.priority.eq(Priority.HIGH)))
                    .fetchAll()

        then:
            activeHighPriority.size() == 1
            activeHighPriority[0].id == 20L
            activeHighPriority[0].name == "Active High"

        where:
            session << allSessions
    }

    def "should handle all enum ordinal values correctly"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            // Insert entities with each priority to verify ordinal mapping
            session.insertEntity(qEntity, new EnumeratedTestEntity(22L, "Low", Status.ACTIVE, Priority.LOW, null, null))      // ordinal 0
            session.insertEntity(qEntity, new EnumeratedTestEntity(23L, "Medium", Status.ACTIVE, Priority.MEDIUM, null, null))  // ordinal 1
            session.insertEntity(qEntity, new EnumeratedTestEntity(24L, "High", Status.ACTIVE, Priority.HIGH, null, null))      // ordinal 2
            session.insertEntity(qEntity, new EnumeratedTestEntity(25L, "Critical", Status.ACTIVE, Priority.CRITICAL, null, null)) // ordinal 3

        when:
            List<EnumeratedTestEntity> allEntities = session.selectEntity(qEntity)
                    .where(qEntity.id.ge(22L).and(qEntity.id.le(25L)))
                    .orderBy(qEntity.priority.asc())
                    .fetchAll()

        then:
            allEntities.size() == 4
            allEntities[0].priority == Priority.LOW
            allEntities[1].priority == Priority.MEDIUM
            allEntities[2].priority == Priority.HIGH
            allEntities[3].priority == Priority.CRITICAL

        where:
            session << allSessions
    }

    def "should delete entity with enumerated fields"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            EnumeratedTestEntity entity = new EnumeratedTestEntity(
                    26L, "To Delete", Status.ACTIVE, Priority.LOW, Status.PENDING, Priority.MEDIUM
            )
            session.insertEntity(qEntity, entity)

        expect:
            session.selectEntity(qEntity).where(qEntity.id.eq(26L)).count() == 1

        when:
            session.deleteEntityById(qEntity, 26L)

        then:
            session.selectEntity(qEntity).where(qEntity.id.eq(26L)).count() == 0

        where:
            session << allSessions
    }

    def "should handle all STRING enum values correctly"() {
        given:
            QEnumeratedTestEntity qEntity = new QEnumeratedTestEntity(null)
            // Insert entities with each status to verify string mapping
            session.insertEntity(qEntity, new EnumeratedTestEntity(27L, "Status Active", Status.ACTIVE, Priority.LOW, null, null))
            session.insertEntity(qEntity, new EnumeratedTestEntity(28L, "Status Inactive", Status.INACTIVE, Priority.LOW, null, null))
            session.insertEntity(qEntity, new EnumeratedTestEntity(29L, "Status Pending", Status.PENDING, Priority.LOW, null, null))
            session.insertEntity(qEntity, new EnumeratedTestEntity(30L, "Status Deleted", Status.DELETED, Priority.LOW, null, null))

        when:
            List<EnumeratedTestEntity> allEntities = session.selectEntity(qEntity)
                    .where(qEntity.id.ge(27L).and(qEntity.id.le(30L)))
                    .orderBy(qEntity.id.asc())
                    .fetchAll()

        then:
            allEntities.size() == 4
            allEntities[0].status == Status.ACTIVE
            allEntities[1].status == Status.INACTIVE
            allEntities[2].status == Status.PENDING
            allEntities[3].status == Status.DELETED

        where:
            session << allSessions
    }
}
