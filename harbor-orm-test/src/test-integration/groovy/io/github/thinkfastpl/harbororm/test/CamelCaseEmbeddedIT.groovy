// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.metadata.QColumn
import io.github.thinkfastpl.harbororm.test.domain.CamelCaseEmbeddedEntity
import io.github.thinkfastpl.harbororm.test.domain.QCamelCaseEmbeddedEntity
import io.github.thinkfastpl.harbororm.test.domain.SimpleAddressEmbeddable

class CamelCaseEmbeddedIT extends AbstractHarborIT {

    def "should insert and select entity with CAMEL_CASE strategy and embedded field"() {
        given:
            QCamelCaseEmbeddedEntity qEntity = new QCamelCaseEmbeddedEntity(null)
            CamelCaseEmbeddedEntity entity = new CamelCaseEmbeddedEntity(
                    1L,
                    "John",
                    new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            )

        when:
            session.insertEntity(qEntity, entity)
            CamelCaseEmbeddedEntity loaded = session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()

        then:
            with(loaded) { e ->
                e.id == 1L
                e.firstName == "John"
                e.address != null
                e.address.street == "Mietowa"
                e.address.postalCode == "81-589"
                e.address.city == "Gdynia"
            }

        where:
            session << allSessions
    }

    def "should update entity with CAMEL_CASE strategy and embedded field"() {
        given:
            QCamelCaseEmbeddedEntity qEntity = new QCamelCaseEmbeddedEntity(null)
            CamelCaseEmbeddedEntity entity = new CamelCaseEmbeddedEntity(
                    1L,
                    "John",
                    new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            )
            session.insertEntity(qEntity, entity)

        when:
            entity.setFirstName("Jane")
            entity.setAddress(new SimpleAddressEmbeddable("Dluga", "00-001", "Warsaw"))
            session.updateEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { e ->
                e.id == 1L
                e.firstName == "Jane"
                e.address.street == "Dluga"
                e.address.postalCode == "00-001"
                e.address.city == "Warsaw"
            }

        where:
            session << allSessions
    }

    def "should query by embedded field with CAMEL_CASE strategy"() {
        given:
            QCamelCaseEmbeddedEntity qEntity = new QCamelCaseEmbeddedEntity(null)
            session.insertEntity(qEntity, new CamelCaseEmbeddedEntity(
                    1L, "John", new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            ))
            session.insertEntity(qEntity, new CamelCaseEmbeddedEntity(
                    2L, "Jane", new SimpleAddressEmbeddable("Dluga", "00-001", "Warsaw")
            ))

        when:
            def results = session.selectEntity(qEntity)
                    .where(qEntity.address.city.eq("Warsaw"))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].id == 2L
            results[0].firstName == "Jane"
            results[0].address.city == "Warsaw"

        where:
            session << allSessions
    }

    def "should update existing entity via replaceEntity with CAMEL_CASE strategy"() {
        given:
            QCamelCaseEmbeddedEntity qEntity = new QCamelCaseEmbeddedEntity(null)
            session.insertEntity(qEntity, new CamelCaseEmbeddedEntity(
                    1L, "John", new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            ))

        when:
            session.replaceEntity(qEntity, new CamelCaseEmbeddedEntity(
                    1L, "Jane", new SimpleAddressEmbeddable("Dluga", "00-001", "Warsaw")
            ))

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { e ->
                e.id == 1L
                e.firstName == "Jane"
                e.address.street == "Dluga"
                e.address.postalCode == "00-001"
                e.address.city == "Warsaw"
            }

        and:
            session.selectEntity(qEntity).fetchAll().size() == 1

        where:
            session << allSessions
    }

    def "should insert missing entity via replaceEntity with CAMEL_CASE strategy"() {
        given:
            QCamelCaseEmbeddedEntity qEntity = new QCamelCaseEmbeddedEntity(null)

        when:
            session.replaceEntity(qEntity, new CamelCaseEmbeddedEntity(
                    1L, "John", new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            ))

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { e ->
                e.id == 1L
                e.firstName == "John"
                e.address.street == "Mietowa"
                e.address.postalCode == "81-589"
                e.address.city == "Gdynia"
            }

        where:
            session << allSessions
    }

    def "should update via fluent update query with CAMEL_CASE strategy"() {
        given:
            QCamelCaseEmbeddedEntity qEntity = new QCamelCaseEmbeddedEntity(null)
            session.insertEntity(qEntity, new CamelCaseEmbeddedEntity(
                    1L, "John", new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            ))

        when:
            session.update(qEntity.getTableName())
                    .set(qEntity.firstName, "Jane")
                    .set(qEntity.address.city, "Warsaw")
                    .where(qEntity.id.eq(1L))
                    .execute()

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { e ->
                e.id == 1L
                e.firstName == "Jane"
                e.address.street == "Mietowa"
                e.address.postalCode == "81-589"
                e.address.city == "Warsaw"
            }

        where:
            session << allSessions
    }

    def "should upsert with ON CONFLICT DO UPDATE and CAMEL_CASE strategy"() {
        given:
            QCamelCaseEmbeddedEntity qEntity = new QCamelCaseEmbeddedEntity(null)
            session.insertEntity(qEntity, new CamelCaseEmbeddedEntity(
                    1L, "John", new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            ))

        when:
            session.insertInto(qEntity.getTableName())
                    .set(qEntity.id, 1L)
                    .set(qEntity.firstName, "Bob")
                    .set(qEntity.address.street, "Dluga")
                    .set(qEntity.address.postalCode, "00-001")
                    .set(qEntity.address.city, "Warsaw")
                    .onConflict(qEntity.id as QColumn)
                    .doUpdate()
                    .set(qEntity.firstName, "Updated")
                    .set(qEntity.address.city, "Krakow")
                    .execute()

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { e ->
                e.id == 1L
                e.firstName == "Updated"
                e.address.street == "Mietowa"
                e.address.postalCode == "81-589"
                e.address.city == "Krakow"
            }

        where:
            session << getSessionsExcept(DbType.H2)
    }
}
