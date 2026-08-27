// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.AddressWithGeoEmbeddable
import io.github.thinkfastpl.harbororm.test.domain.GeoLocationEmbeddable
import io.github.thinkfastpl.harbororm.test.domain.NestedEmbeddedEntity
import io.github.thinkfastpl.harbororm.test.domain.QNestedEmbeddedEntity

/**
 * Integration tests for nested @Embedded support.
 * An @Embeddable class (AddressWithGeoEmbeddable) contains another @Embedded field (GeoLocationEmbeddable).
 */
class NestedEmbeddedEntityIT extends AbstractHarborIT {

    def "should insert and select entity with nested embedded fields"() {
        given:
            QNestedEmbeddedEntity qEntity = new QNestedEmbeddedEntity(null)
            NestedEmbeddedEntity entity = new NestedEmbeddedEntity(
                    1L,
                    "Office",
                    new AddressWithGeoEmbeddable(
                            "Mietowa",
                            "Gdynia",
                            new GeoLocationEmbeddable(54.5189, 18.5305)
                    )
            )

        when:
            session.insertEntity(qEntity, entity)
            NestedEmbeddedEntity loaded = session.selectEntity(qEntity)
                    .where(qEntity.id.eq(1L))
                    .fetchSingle()

        then:
            with(loaded) { e ->
                e.id == 1L
                e.name == "Office"
                e.address != null
                e.address.street == "Mietowa"
                e.address.city == "Gdynia"
                e.address.geoLocation != null
                e.address.geoLocation.latitude == 54.5189d
                e.address.geoLocation.longitude == 18.5305d
            }

        where:
            session << allSessions
    }

    def "should query by deeply nested embedded field"() {
        given:
            QNestedEmbeddedEntity qEntity = new QNestedEmbeddedEntity(null)

            session.insertEntity(qEntity, new NestedEmbeddedEntity(
                    1L, "Gdynia Office",
                    new AddressWithGeoEmbeddable("Mietowa", "Gdynia", new GeoLocationEmbeddable(54.5189, 18.5305))
            ))
            session.insertEntity(qEntity, new NestedEmbeddedEntity(
                    2L, "Berlin Office",
                    new AddressWithGeoEmbeddable("Berliner Strasse", "Berlin", new GeoLocationEmbeddable(52.5200, 13.4050))
            ))

        when:
            List<NestedEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.address.geoLocation.latitude.gt(53.0d))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].id == 1L
            results[0].name == "Gdynia Office"

        where:
            session << allSessions
    }

    def "should update nested embedded fields"() {
        given:
            QNestedEmbeddedEntity qEntity = new QNestedEmbeddedEntity(null)
            NestedEmbeddedEntity entity = new NestedEmbeddedEntity(
                    1L, "Office",
                    new AddressWithGeoEmbeddable("Mietowa", "Gdynia", new GeoLocationEmbeddable(54.5189, 18.5305))
            )
            session.insertEntity(qEntity, entity)

        when:
            entity.setAddress(new AddressWithGeoEmbeddable(
                    "Berliner Strasse", "Berlin", new GeoLocationEmbeddable(52.5200, 13.4050)
            ))
            session.updateEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { e ->
                e.address.street == "Berliner Strasse"
                e.address.city == "Berlin"
                e.address.geoLocation.latitude == 52.5200d
                e.address.geoLocation.longitude == 13.4050d
            }

        where:
            session << allSessions
    }

    def "should handle null nested embedded object"() {
        given:
            QNestedEmbeddedEntity qEntity = new QNestedEmbeddedEntity(null)
            NestedEmbeddedEntity entity = new NestedEmbeddedEntity(
                    1L, "Office",
                    new AddressWithGeoEmbeddable("Mietowa", "Gdynia", null)
            )

        when:
            session.insertEntity(qEntity, entity)
            NestedEmbeddedEntity loaded = session.selectEntity(qEntity)
                    .where(qEntity.id.eq(1L))
                    .fetchSingle()

        then:
            with(loaded) { e ->
                e.address.street == "Mietowa"
                e.address.city == "Gdynia"
                // Null nested embedded gets reconstructed with null/default fields
                e.address.geoLocation != null
                e.address.geoLocation.latitude == 0.0d
                e.address.geoLocation.longitude == 0.0d
            }

        where:
            session << allSessions
    }

    def "should query filtering on nested embedded with combined conditions"() {
        given:
            QNestedEmbeddedEntity qEntity = new QNestedEmbeddedEntity(null)

            session.insertEntity(qEntity, new NestedEmbeddedEntity(
                    1L, "Gdynia Office",
                    new AddressWithGeoEmbeddable("Mietowa", "Gdynia", new GeoLocationEmbeddable(54.5189, 18.5305))
            ))
            session.insertEntity(qEntity, new NestedEmbeddedEntity(
                    2L, "Warsaw Office",
                    new AddressWithGeoEmbeddable("Dluga", "Warsaw", new GeoLocationEmbeddable(52.2297, 21.0122))
            ))

        when:
            List<NestedEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.address.city.eq("Gdynia")
                            .and(qEntity.address.geoLocation.longitude.gt(18.0d)))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].id == 1L

        where:
            session << allSessions
    }
}
