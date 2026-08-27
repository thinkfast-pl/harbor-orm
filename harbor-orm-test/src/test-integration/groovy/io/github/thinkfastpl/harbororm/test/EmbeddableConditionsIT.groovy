// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.BasicEmbeddedEntity
import io.github.thinkfastpl.harbororm.test.domain.QBasicEmbeddedEntity
import io.github.thinkfastpl.harbororm.test.domain.SimpleAddressEmbeddable

/**
 * Integration tests for EmbeddableEqCondition and EmbeddableInCondition.
 *
 * Tests cover:
 * - embeddedColumn.eq(embeddableInstance) - equality check for embedded object
 * - embeddedColumn.eq(embeddableInstance).not() - not equal for embedded object
 * - embeddedColumn.in(List) - IN with list of embeddable instances
 * - embeddedColumn.in(List).not() - NOT IN with list of embeddables
 * - Combining embeddable conditions with regular conditions using .and() and .or()
 * - Embeddable with null values in some fields
 */
class EmbeddableConditionsIT extends AbstractHarborIT {

    def "should select entity using embeddable eq condition"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            SimpleAddressEmbeddable gdyniaAddress = new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            SimpleAddressEmbeddable berlinAddress = new SimpleAddressEmbeddable("Berliner Strasse", "10115", "Berlin")
            SimpleAddressEmbeddable warsawAddress = new SimpleAddressEmbeddable("Dluga", "00-001", "Warsaw")

            session.insertEntity(qEntity, new BasicEmbeddedEntity(1L, "Poland", gdyniaAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(2L, "Germany", berlinAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(3L, "Poland", warsawAddress))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.eq(gdyniaAddress))
                    .fetchAll()

        then:
            results.size() == 1
            with(results[0]) { e ->
                e.id == 1L
                e.destinationCountry == "Poland"
                e.destinationAddress.street == "Mietowa"
                e.destinationAddress.postalCode == "81-589"
                e.destinationAddress.city == "Gdynia"
            }

        where:
            session << allSessions
    }

    def "should select entity using embeddable not eq condition"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            SimpleAddressEmbeddable gdyniaAddress = new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            SimpleAddressEmbeddable berlinAddress = new SimpleAddressEmbeddable("Berliner Strasse", "10115", "Berlin")
            SimpleAddressEmbeddable warsawAddress = new SimpleAddressEmbeddable("Dluga", "00-001", "Warsaw")

            session.insertEntity(qEntity, new BasicEmbeddedEntity(1L, "Poland", gdyniaAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(2L, "Germany", berlinAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(3L, "Poland", warsawAddress))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.eq(gdyniaAddress).not())
                    .orderBy(qEntity.id.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].id == 2L
            results[1].id == 3L

        where:
            session << allSessions
    }

    def "should select entity using embeddable in condition with multiple values"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            SimpleAddressEmbeddable gdyniaAddress = new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            SimpleAddressEmbeddable berlinAddress = new SimpleAddressEmbeddable("Berliner Strasse", "10115", "Berlin")
            SimpleAddressEmbeddable warsawAddress = new SimpleAddressEmbeddable("Dluga", "00-001", "Warsaw")
            SimpleAddressEmbeddable parisAddress = new SimpleAddressEmbeddable("Champs Elysees", "75008", "Paris")

            session.insertEntity(qEntity, new BasicEmbeddedEntity(1L, "Poland", gdyniaAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(2L, "Germany", berlinAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(3L, "Poland", warsawAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(4L, "France", parisAddress))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.in([gdyniaAddress, warsawAddress]))
                    .orderBy(qEntity.id.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].id == 1L
            results[1].id == 3L

        where:
            session << allSessions
    }

    def "should select entity using embeddable not in condition"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            SimpleAddressEmbeddable gdyniaAddress = new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            SimpleAddressEmbeddable berlinAddress = new SimpleAddressEmbeddable("Berliner Strasse", "10115", "Berlin")
            SimpleAddressEmbeddable warsawAddress = new SimpleAddressEmbeddable("Dluga", "00-001", "Warsaw")
            SimpleAddressEmbeddable parisAddress = new SimpleAddressEmbeddable("Champs Elysees", "75008", "Paris")

            session.insertEntity(qEntity, new BasicEmbeddedEntity(1L, "Poland", gdyniaAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(2L, "Germany", berlinAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(3L, "Poland", warsawAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(4L, "France", parisAddress))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.in([gdyniaAddress, berlinAddress]).not())
                    .orderBy(qEntity.id.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].id == 3L
            results[1].id == 4L

        where:
            session << allSessions
    }

    def "should combine embeddable eq condition with regular column condition using and"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            SimpleAddressEmbeddable gdyniaAddress = new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            SimpleAddressEmbeddable warsawAddress = new SimpleAddressEmbeddable("Dluga", "00-001", "Warsaw")

            session.insertEntity(qEntity, new BasicEmbeddedEntity(1L, "Poland", gdyniaAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(2L, "Germany", gdyniaAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(3L, "Poland", warsawAddress))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.eq(gdyniaAddress).and(qEntity.destinationCountry.eq("Poland")))
                    .fetchAll()

        then:
            results.size() == 1
            with(results[0]) { e ->
                e.id == 1L
                e.destinationCountry == "Poland"
            }

        where:
            session << allSessions
    }

    def "should combine embeddable eq condition with regular column condition using or"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            SimpleAddressEmbeddable gdyniaAddress = new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            SimpleAddressEmbeddable berlinAddress = new SimpleAddressEmbeddable("Berliner Strasse", "10115", "Berlin")
            SimpleAddressEmbeddable warsawAddress = new SimpleAddressEmbeddable("Dluga", "00-001", "Warsaw")

            session.insertEntity(qEntity, new BasicEmbeddedEntity(1L, "Poland", gdyniaAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(2L, "Germany", berlinAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(3L, "Poland", warsawAddress))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.eq(gdyniaAddress).or(qEntity.destinationCountry.eq("Germany")))
                    .orderBy(qEntity.id.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].id == 1L
            results[1].id == 2L

        where:
            session << allSessions
    }

    def "should select entity using embeddable eq with null embedded value"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            SimpleAddressEmbeddable gdyniaAddress = new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")

            session.insertEntity(qEntity, new BasicEmbeddedEntity(1L, "Poland", gdyniaAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(2L, "Germany", null))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(3L, "France", null))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.eq(null))
                    .orderBy(qEntity.id.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].id == 2L
            results[1].id == 3L

        where:
            session << allSessions
    }

    def "should select entity using embeddable eq with partially null fields in embedded value"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            SimpleAddressEmbeddable partialAddress = new SimpleAddressEmbeddable("Mietowa", null, "Gdynia")
            SimpleAddressEmbeddable fullAddress = new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")

            session.insertEntity(qEntity, new BasicEmbeddedEntity(1L, "Poland", partialAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(2L, "Germany", fullAddress))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.eq(partialAddress))
                    .fetchAll()

        then:
            results.size() == 1
            with(results[0]) { e ->
                e.id == 1L
                e.destinationAddress.street == "Mietowa"
                e.destinationAddress.postalCode == null
                e.destinationAddress.city == "Gdynia"
            }

        where:
            session << allSessions
    }

    def "should select entity using embeddable in with single value"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            SimpleAddressEmbeddable gdyniaAddress = new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            SimpleAddressEmbeddable berlinAddress = new SimpleAddressEmbeddable("Berliner Strasse", "10115", "Berlin")

            session.insertEntity(qEntity, new BasicEmbeddedEntity(1L, "Poland", gdyniaAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(2L, "Germany", berlinAddress))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.in([gdyniaAddress]))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].id == 1L

        where:
            session << allSessions
    }

    def "should return empty result when embeddable in has empty list"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            SimpleAddressEmbeddable gdyniaAddress = new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")

            session.insertEntity(qEntity, new BasicEmbeddedEntity(1L, "Poland", gdyniaAddress))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.in([]))
                    .fetchAll()

        then:
            results.size() == 0

        where:
            session << allSessions
    }

    def "should combine multiple embeddable conditions with and"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            SimpleAddressEmbeddable gdyniaAddress = new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            SimpleAddressEmbeddable berlinAddress = new SimpleAddressEmbeddable("Berliner Strasse", "10115", "Berlin")
            SimpleAddressEmbeddable warsawAddress = new SimpleAddressEmbeddable("Dluga", "00-001", "Warsaw")

            session.insertEntity(qEntity, new BasicEmbeddedEntity(1L, "Poland", gdyniaAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(2L, "Germany", berlinAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(3L, "Poland", warsawAddress))

        when:
            // This should find entities where address is in [gdynia, warsaw] AND address is not berlin
            // Which effectively means: gdynia and warsaw (since berlin is excluded anyway)
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.in([gdyniaAddress, warsawAddress])
                            .and(qEntity.destinationAddress.eq(berlinAddress).not()))
                    .orderBy(qEntity.id.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].id == 1L
            results[1].id == 3L

        where:
            session << allSessions
    }

    def "should combine multiple embeddable conditions with or"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            SimpleAddressEmbeddable gdyniaAddress = new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            SimpleAddressEmbeddable berlinAddress = new SimpleAddressEmbeddable("Berliner Strasse", "10115", "Berlin")
            SimpleAddressEmbeddable warsawAddress = new SimpleAddressEmbeddable("Dluga", "00-001", "Warsaw")
            SimpleAddressEmbeddable parisAddress = new SimpleAddressEmbeddable("Champs Elysees", "75008", "Paris")

            session.insertEntity(qEntity, new BasicEmbeddedEntity(1L, "Poland", gdyniaAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(2L, "Germany", berlinAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(3L, "Poland", warsawAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(4L, "France", parisAddress))

        when:
            // Find entities where address equals gdynia OR address equals paris
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.eq(gdyniaAddress).or(qEntity.destinationAddress.eq(parisAddress)))
                    .orderBy(qEntity.id.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].id == 1L
            results[1].id == 4L

        where:
            session << allSessions
    }

    def "should use embeddable eq condition with entity alias"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity("e")
            SimpleAddressEmbeddable gdyniaAddress = new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            SimpleAddressEmbeddable berlinAddress = new SimpleAddressEmbeddable("Berliner Strasse", "10115", "Berlin")

            session.insertEntity(qEntity, new BasicEmbeddedEntity(1L, "Poland", gdyniaAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(2L, "Germany", berlinAddress))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.eq(gdyniaAddress))
                    .fetchAll()

        then:
            results.size() == 1
            with(results[0]) { e ->
                e.id == 1L
                e.destinationAddress.city == "Gdynia"
            }

        where:
            session << allSessions
    }

    def "should use embeddable in condition with entity alias"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity("e")
            SimpleAddressEmbeddable gdyniaAddress = new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            SimpleAddressEmbeddable berlinAddress = new SimpleAddressEmbeddable("Berliner Strasse", "10115", "Berlin")
            SimpleAddressEmbeddable warsawAddress = new SimpleAddressEmbeddable("Dluga", "00-001", "Warsaw")

            session.insertEntity(qEntity, new BasicEmbeddedEntity(1L, "Poland", gdyniaAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(2L, "Germany", berlinAddress))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(3L, "Poland", warsawAddress))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.in([gdyniaAddress, warsawAddress]))
                    .orderBy(qEntity.id.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].id == 1L
            results[1].id == 3L

        where:
            session << allSessions
    }

    def "should match entity when all embedded fields match exactly"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            // Same street and city but different postal code
            SimpleAddressEmbeddable address1 = new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            SimpleAddressEmbeddable address2 = new SimpleAddressEmbeddable("Mietowa", "81-590", "Gdynia")

            session.insertEntity(qEntity, new BasicEmbeddedEntity(1L, "Poland", address1))
            session.insertEntity(qEntity, new BasicEmbeddedEntity(2L, "Poland", address2))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.eq(address1))
                    .fetchAll()

        then:
            results.size() == 1
            with(results[0]) { e ->
                e.id == 1L
                e.destinationAddress.postalCode == "81-589"
            }

        where:
            session << allSessions
    }

    def "should not match when one embedded field differs"() {
        given:
            QBasicEmbeddedEntity qEntity = new QBasicEmbeddedEntity(null)
            SimpleAddressEmbeddable storedAddress = new SimpleAddressEmbeddable("Mietowa", "81-589", "Gdynia")
            SimpleAddressEmbeddable queryAddress = new SimpleAddressEmbeddable("Mietowa", "81-590", "Gdynia")

            session.insertEntity(qEntity, new BasicEmbeddedEntity(1L, "Poland", storedAddress))

        when:
            List<BasicEmbeddedEntity> results = session.selectEntity(qEntity)
                    .where(qEntity.destinationAddress.eq(queryAddress))
                    .fetchAll()

        then:
            results.size() == 0

        where:
            session << allSessions
    }
}
