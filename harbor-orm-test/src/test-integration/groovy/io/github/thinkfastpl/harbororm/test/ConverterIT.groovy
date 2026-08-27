// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.ConvertedFieldsEntity
import io.github.thinkfastpl.harbororm.test.domain.JsonMetadata
import io.github.thinkfastpl.harbororm.test.domain.QConvertedFieldsEntity

/**
 * Integration tests for @Convert annotation functionality.
 * Tests AttributeConverter implementations for custom type mapping.
 */
class ConverterIT extends AbstractHarborIT {

    def "should insert entity with converted Boolean fields"() {
        given:
            QConvertedFieldsEntity qEntity = new QConvertedFieldsEntity(null)
            ConvertedFieldsEntity entity = new ConvertedFieldsEntity(
                    1L, "Test Entity", true, false, null
            )

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { loaded ->
                loaded.id == 1L
                loaded.name == "Test Entity"
                loaded.active == true
                loaded.verified == false
                loaded.metadata == null
            }

        where:
            session << allSessions
    }

    def "should insert entity with converted JSON metadata field"() {
        given:
            QConvertedFieldsEntity qEntity = new QConvertedFieldsEntity(null)
            JsonMetadata metadata = new JsonMetadata("testKey", "testValue", 42)
            ConvertedFieldsEntity entity = new ConvertedFieldsEntity(
                    2L, "Entity with JSON", true, true, metadata
            )

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(2L)).fetchSingle()) { loaded ->
                loaded.id == 2L
                loaded.name == "Entity with JSON"
                loaded.active == true
                loaded.verified == true
                loaded.metadata != null
                loaded.metadata.key == "testKey"
                loaded.metadata.value == "testValue"
                loaded.metadata.count == 42
            }

        where:
            session << allSessions
    }

    def "should select entity with converted fields and verify conversion back to Java types"() {
        given:
            QConvertedFieldsEntity qEntity = new QConvertedFieldsEntity(null)
            JsonMetadata metadata = new JsonMetadata("selectKey", "selectValue", 100)

            session.insertEntity(qEntity, new ConvertedFieldsEntity(3L, "Select Test", false, true, metadata))
            session.insertEntity(qEntity, new ConvertedFieldsEntity(4L, "Select Test 2", true, false, null))

        when:
            List<ConvertedFieldsEntity> entities = session.selectEntity(qEntity)
                    .where(qEntity.id.ge(3L).and(qEntity.id.le(4L)))
                    .orderBy(qEntity.id.asc())
                    .fetchAll()

        then:
            entities.size() == 2

            with(entities[0]) { e ->
                e.id == 3L
                e.name == "Select Test"
                e.active == false
                e.verified == true
                e.metadata.key == "selectKey"
                e.metadata.value == "selectValue"
                e.metadata.count == 100
            }

            with(entities[1]) { e ->
                e.id == 4L
                e.name == "Select Test 2"
                e.active == true
                e.verified == false
                e.metadata == null
            }

        where:
            session << allSessions
    }

    def "should update entity with converted fields"() {
        given:
            QConvertedFieldsEntity qEntity = new QConvertedFieldsEntity(null)
            JsonMetadata originalMetadata = new JsonMetadata("original", "data", 1)
            ConvertedFieldsEntity entity = new ConvertedFieldsEntity(5L, "Update Test", true, true, originalMetadata)

            session.insertEntity(qEntity, entity)

        when:
            ConvertedFieldsEntity loaded = session.selectEntity(qEntity).where(qEntity.id.eq(5L)).fetchSingle()
            loaded.setActive(false)
            loaded.setVerified(false)
            loaded.setMetadata(new JsonMetadata("updated", "newData", 999))
            session.updateEntity(qEntity, loaded)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(5L)).fetchSingle()) { updated ->
                updated.id == 5L
                updated.active == false
                updated.verified == false
                updated.metadata.key == "updated"
                updated.metadata.value == "newData"
                updated.metadata.count == 999
            }

        where:
            session << allSessions
    }

    def "should update converted field to null"() {
        given:
            QConvertedFieldsEntity qEntity = new QConvertedFieldsEntity(null)
            JsonMetadata metadata = new JsonMetadata("toBeNulled", "value", 50)
            ConvertedFieldsEntity entity = new ConvertedFieldsEntity(6L, "Null Update Test", true, true, metadata)

            session.insertEntity(qEntity, entity)

        when:
            ConvertedFieldsEntity loaded = session.selectEntity(qEntity).where(qEntity.id.eq(6L)).fetchSingle()
            loaded.setActive(null)
            loaded.setMetadata(null)
            session.updateEntity(qEntity, loaded)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(6L)).fetchSingle()) { updated ->
                updated.id == 6L
                updated.active == null
                updated.metadata == null
            }

        where:
            session << allSessions
    }

    def "should query by converted Boolean field"() {
        given:
            QConvertedFieldsEntity qEntity = new QConvertedFieldsEntity(null)

            session.insertEntity(qEntity, new ConvertedFieldsEntity(7L, "Active Entity", true, false, null))
            session.insertEntity(qEntity, new ConvertedFieldsEntity(8L, "Inactive Entity", false, true, null))
            session.insertEntity(qEntity, new ConvertedFieldsEntity(9L, "Another Active", true, true, null))

        when:
            List<ConvertedFieldsEntity> activeEntities = session.selectEntity(qEntity)
                    .where(qEntity.active.eq(true))
                    .orderBy(qEntity.id.asc())
                    .fetchAll()

        then:
            activeEntities.size() == 2
            activeEntities[0].id == 7L
            activeEntities[0].name == "Active Entity"
            activeEntities[1].id == 9L
            activeEntities[1].name == "Another Active"

        where:
            session << allSessions
    }

    def "should query by multiple converted fields"() {
        given:
            QConvertedFieldsEntity qEntity = new QConvertedFieldsEntity(null)

            session.insertEntity(qEntity, new ConvertedFieldsEntity(10L, "Both True", true, true, null))
            session.insertEntity(qEntity, new ConvertedFieldsEntity(11L, "Active Only", true, false, null))
            session.insertEntity(qEntity, new ConvertedFieldsEntity(12L, "Verified Only", false, true, null))
            session.insertEntity(qEntity, new ConvertedFieldsEntity(13L, "Neither", false, false, null))

        when:
            List<ConvertedFieldsEntity> bothTrue = session.selectEntity(qEntity)
                    .where(qEntity.active.eq(true).and(qEntity.verified.eq(true)))
                    .fetchAll()

        then:
            bothTrue.size() == 1
            bothTrue[0].id == 10L
            bothTrue[0].name == "Both True"

        where:
            session << allSessions
    }

    def "should handle multiple converters on same entity"() {
        given:
            QConvertedFieldsEntity qEntity = new QConvertedFieldsEntity(null)
            JsonMetadata metadata = new JsonMetadata("multi", "converter", 123)
            ConvertedFieldsEntity entity = new ConvertedFieldsEntity(14L, "Multiple Converters", true, false, metadata)

        when:
            session.insertEntity(qEntity, entity)
            ConvertedFieldsEntity loaded = session.selectEntity(qEntity).where(qEntity.id.eq(14L)).fetchSingle()

        then:
            loaded.id == 14L
            loaded.name == "Multiple Converters"
            // BooleanToYesNoConverter for active (true -> "Y" -> true)
            loaded.active == true
            // BooleanToYesNoConverter for verified (false -> "N" -> false)
            loaded.verified == false
            // JsonConverter for metadata
            loaded.metadata.key == "multi"
            loaded.metadata.value == "converter"
            loaded.metadata.count == 123

        where:
            session << allSessions
    }

    def "should delete entity with converted fields"() {
        given:
            QConvertedFieldsEntity qEntity = new QConvertedFieldsEntity(null)
            JsonMetadata metadata = new JsonMetadata("delete", "test", 1)
            ConvertedFieldsEntity entity = new ConvertedFieldsEntity(15L, "To Delete", true, true, metadata)

            session.insertEntity(qEntity, entity)

        expect:
            session.selectEntity(qEntity).where(qEntity.id.eq(15L)).count() == 1

        when:
            session.deleteEntityById(qEntity, 15L)

        then:
            session.selectEntity(qEntity).where(qEntity.id.eq(15L)).count() == 0

        where:
            session << allSessions
    }

    def "should handle JSON metadata with special characters"() {
        given:
            QConvertedFieldsEntity qEntity = new QConvertedFieldsEntity(null)
            // Test with special characters that need JSON escaping
            JsonMetadata metadata = new JsonMetadata("key-with-dash", "value with spaces", 0)
            ConvertedFieldsEntity entity = new ConvertedFieldsEntity(16L, "Special Chars", true, true, metadata)

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(16L)).fetchSingle()) { loaded ->
                loaded.metadata.key == "key-with-dash"
                loaded.metadata.value == "value with spaces"
                loaded.metadata.count == 0
            }

        where:
            session << allSessions
    }
}
