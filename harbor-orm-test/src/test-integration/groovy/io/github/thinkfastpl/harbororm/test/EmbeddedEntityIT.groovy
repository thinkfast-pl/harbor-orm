// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.BasicEmbeddedEntity
import io.github.thinkfastpl.harbororm.test.domain.QBasicEmbeddedEntity
import io.github.thinkfastpl.harbororm.test.domain.SimpleAddressEmbeddable

/**
 * Integration tests for @Embedded and @Embeddable functionality.
 *
 * Tests cover:
 * - Insert entity with @Embedded field
 * - Select entity with @Embedded field
 * - Update entity with @Embedded field
 * - Query by embedded field properties (WHERE on embedded.property)
 * - Null embedded object handling
 */
class EmbeddedEntityIT extends AbstractHarborIT {

    def "should insert entity with embedded field"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            BasicEmbeddedEntity entity = new BasicEmbeddedEntity(
                    1L,
                    "Poland",
                    new SimpleAddressEmbeddable(
                            "Mietowa",
                            "81-589",
                            "Gdynia"
                    )
            )

        when:
            session.insertEntity(qEntity, entity)

        then:
            session.selectEntity(qEntity).count() == 1

        where:
            session << allSessions
    }

    def "should select entity with embedded field populated"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            BasicEmbeddedEntity entity = new BasicEmbeddedEntity(
                    1L,
                    "Poland",
                    new SimpleAddressEmbeddable(
                            "Mietowa",
                            "81-589",
                            "Gdynia"
                    )
            )
            session.insertEntity(qEntity, entity)

        when:
            BasicEmbeddedEntity loadedEntity = session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()

        then:
            with(loadedEntity) { e ->
                e.id == 1L
                e.destinationCountry == "Poland"
                e.destinationAddress != null
                e.destinationAddress.street == "Mietowa"
                e.destinationAddress.postalCode == "81-589"
                e.destinationAddress.city == "Gdynia"
            }

        where:
            session << allSessions
    }

    def "should update entity with embedded field"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            BasicEmbeddedEntity entity = new BasicEmbeddedEntity(
                    1L,
                    "Poland",
                    new SimpleAddressEmbeddable(
                            "Mietowa",
                            "81-589",
                            "Gdynia"
                    )
            )
            session.insertEntity(qEntity, entity)

        when:
            entity.setDestinationCountry("USA")
            entity.setDestinationAddress(new SimpleAddressEmbeddable(
                    "Wall Street",
                    "10005",
                    "New York"
            ))
            session.updateEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { e ->
                e.id == 1L
                e.destinationCountry == "USA"
                e.destinationAddress != null
                e.destinationAddress.street == "Wall Street"
                e.destinationAddress.postalCode == "10005"
                e.destinationAddress.city == "New York"
            }

        where:
            session << allSessions
    }

    def "should query by embedded field property using WHERE clause"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)

            session.insertEntity(qEntity, new BasicEmbeddedEntity(
                    1L,
                    "Poland",
                    new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            ))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(
                    2L,
                    "Germany",
                    new SimpleAddressEmbeddable("Berliner Strasse", "10115", "Berlin")
            ))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(
                    3L,
                    "Poland",
                    new SimpleAddressEmbeddable("Dluga", "00-001", "Warsaw")
            ))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.city.eq("Berlin"))
                    .fetchAll()

        then:
            results.size() == 1
            with(results[0]) { e ->
                e.id == 2L
                e.destinationCountry == "Germany"
                e.destinationAddress.city == "Berlin"
            }

        where:
            session << allSessions
    }

    def "should query by multiple embedded field properties"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)

            session.insertEntity(qEntity, new BasicEmbeddedEntity(
                    1L,
                    "Poland",
                    new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            ))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(
                    2L,
                    "Poland",
                    new SimpleAddressEmbeddable("Dluga", "81-590", "Gdynia")
            ))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(
                    3L,
                    "Poland",
                    new SimpleAddressEmbeddable("Mietowa", "00-001", "Warsaw")
            ))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.city.eq("Gdynia")
                            .and(qEntity.destinationAddress.street.eq("Mietowa")))
                    .fetchAll()

        then:
            results.size() == 1
            with(results[0]) { e ->
                e.id == 1L
                e.destinationAddress.street == "Mietowa"
                e.destinationAddress.city == "Gdynia"
            }

        where:
            session << allSessions
    }

    def "should handle null embedded object during insert"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            BasicEmbeddedEntity entity = new BasicEmbeddedEntity(
                    1L,
                    "Poland",
                    null
            )

        when:
            session.insertEntity(qEntity, entity)

        then:
            session.selectEntity(qEntity).count() == 1

        when:
            BasicEmbeddedEntity loadedEntity = session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()

        then:
            with(loadedEntity) { e ->
                e.id == 1L
                e.destinationCountry == "Poland"
                // When embedded object is null, a new instance is created with null fields
                e.destinationAddress != null
                e.destinationAddress.street == null
                e.destinationAddress.postalCode == null
                e.destinationAddress.city == null
            }

        where:
            session << allSessions
    }

    def "should handle update setting embedded to null"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            BasicEmbeddedEntity entity = new BasicEmbeddedEntity(
                    1L,
                    "Poland",
                    new SimpleAddressEmbeddable(
                            "Mietowa",
                            "81-589",
                            "Gdynia"
                    )
            )
            session.insertEntity(qEntity, entity)

        when:
            entity.setDestinationCountry("USA")
            entity.setDestinationAddress(null)
            session.updateEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { e ->
                e.id == 1L
                e.destinationCountry == "USA"
                e.destinationAddress != null
                e.destinationAddress.street == null
                e.destinationAddress.postalCode == null
                e.destinationAddress.city == null
            }

        where:
            session << allSessions
    }

    def "should query for entities with null embedded field values"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)

            session.insertEntity(qEntity, new BasicEmbeddedEntity(
                    1L,
                    "Poland",
                    new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            ))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(
                    2L,
                    "Germany",
                    null  // null embedded object
            ))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.city.isNull())
                    .fetchAll()

        then:
            results.size() == 1
            with(results[0]) { e ->
                e.id == 2L
                e.destinationCountry == "Germany"
            }

        where:
            session << allSessions
    }

    def "should work with entity alias"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity("e")
            BasicEmbeddedEntity entity = new BasicEmbeddedEntity(
                    1L,
                    "Poland",
                    new SimpleAddressEmbeddable(
                            "Mietowa",
                            "81-589",
                            "Gdynia"
                    )
            )

        when:
            session.insertEntity(qEntity, entity)
            BasicEmbeddedEntity loadedEntity = session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()

        then:
            with(loadedEntity) { e ->
                e.id == 1L
                e.destinationCountry == "Poland"
                e.destinationAddress != null
                e.destinationAddress.street == "Mietowa"
                e.destinationAddress.postalCode == "81-589"
                e.destinationAddress.city == "Gdynia"
            }

        where:
            session << allSessions
    }

    def "should select multiple entities with embedded fields"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)

            session.insertEntity(qEntity, new BasicEmbeddedEntity(
                    1L,
                    "Poland",
                    new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            ))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(
                    2L,
                    "Germany",
                    new SimpleAddressEmbeddable("Berliner Strasse", "10115", "Berlin")
            ))

        when:
            List<BasicEmbeddedEntity> entities = session.selectEntity(qEntity)
                    .orderBy(qEntity.id.asc())
                    .fetchAll()

        then:
            entities.size() == 2

            with(entities[0]) { e ->
                e.id == 1L
                e.destinationCountry == "Poland"
                e.destinationAddress != null
                e.destinationAddress.street == "Mietowa"
                e.destinationAddress.postalCode == "81-589"
                e.destinationAddress.city == "Gdynia"
            }

            with(entities[1]) { e ->
                e.id == 2L
                e.destinationCountry == "Germany"
                e.destinationAddress != null
                e.destinationAddress.street == "Berliner Strasse"
                e.destinationAddress.postalCode == "10115"
                e.destinationAddress.city == "Berlin"
            }

        where:
            session << allSessions
    }
}
