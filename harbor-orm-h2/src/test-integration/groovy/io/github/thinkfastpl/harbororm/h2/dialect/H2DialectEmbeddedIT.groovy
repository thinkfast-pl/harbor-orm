// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.h2.repository.set3.AddressEmbeddable
import io.github.thinkfastpl.harbororm.h2.repository.set3.BasicEmbeddedEntity
import io.github.thinkfastpl.harbororm.h2.repository.set3.QBasicEmbeddedEntity

class H2DialectEmbeddedIT extends H2DialectBaseIT {

    void setup() {
        loadScript("basics-embedded.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "insert"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(alias)
            BasicEmbeddedEntity entity = new BasicEmbeddedEntity(
                    1L,
                    "Poland",
                    new AddressEmbeddable(
                            "Mietowa",
                            "81-589",
                            "Gdynia"
                    )
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
                e.destinationAddress != null
                e.destinationAddress.street == "Mietowa"
                e.destinationAddress.postalCode == "81-589"
                e.destinationAddress.city == "Gdynia"
            }

        where:
            alias << [null, 'a']
    }

    def "insert null"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(alias)
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
                e.destinationAddress != null
                e.destinationAddress.street == null
                e.destinationAddress.postalCode == null
                e.destinationAddress.city == null
            }

        where:
            alias << [null, 'a']
    }

    def "select"() {
        given:
            loadScript("basics-embedded-data.sql")

            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(alias)

        when:
            List<BasicEmbeddedEntity> entities = session.selectEntity(qEntity).fetchAll()

        then:
            entities.size() == 2
            with(entities[0]) { e ->
                e.id == 1L
                e.destinationCountry == "Polska"
                e.destinationAddress != null
                e.destinationAddress.street == "Mietowa"
                e.destinationAddress.postalCode == "81-589"
                e.destinationAddress.city == "Gdynia"
            }
            with(entities[1]) { e ->
                e.id == 2L
                e.destinationCountry == "Germany"
                e.destinationAddress != null
                e.destinationAddress.street == null
                e.destinationAddress.postalCode == null
                e.destinationAddress.city == null
            }

        where:
            alias << [null, 'a']
    }

    def "update"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(alias)
            BasicEmbeddedEntity entity = new BasicEmbeddedEntity(
                    1L,
                    "Poland",
                    new AddressEmbeddable(
                            "Mietowa",
                            "81-589",
                            "Gdynia"
                    )
            )
            session.insertEntity(qEntity, entity)

        when:
            entity.setDestinationCountry("USA")
            entity.setDestinationAddress(new AddressEmbeddable(
                    "Wall Street",
                    "0-00",
                    "New York"
            ))
            session.updateEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { e ->
                e.id == 1L
                e.destinationCountry == "USA"
                e.destinationAddress != null
                e.destinationAddress.street == "Wall Street"
                e.destinationAddress.postalCode == "0-00"
                e.destinationAddress.city == "New York"
            }

        where:
            alias << [null, 'a']
    }

    def "update null"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(alias)
            BasicEmbeddedEntity entity = new BasicEmbeddedEntity(
                    1L,
                    "Poland",
                    new AddressEmbeddable(
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
            alias << [null, 'a']
    }
}
