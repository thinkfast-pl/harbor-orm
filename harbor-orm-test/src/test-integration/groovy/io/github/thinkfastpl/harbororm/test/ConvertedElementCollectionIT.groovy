// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.FeatureFlagEntity
import io.github.thinkfastpl.harbororm.test.domain.QFeatureFlagEntity

/**
 * Integration tests for @ElementCollection with @Convert on type-use position.
 * Validates the C-7 bug fix: @Convert on element collection generic type parameter
 * must work through the dynamic proxy annotation mechanism.
 */
class ConvertedElementCollectionIT extends AbstractHarborIT {

    def "should insert and select entity with converted element collection"() {
        given:
            QFeatureFlagEntity qFlag = new QFeatureFlagEntity(null)
            FeatureFlagEntity flag = new FeatureFlagEntity(
                    1L,
                    "dark-mode",
                    List.of(true, false)
            )

        when:
            session.insertEntity(qFlag, flag)
            FeatureFlagEntity loaded = session.selectEntity(qFlag).whereIdEq(1L).fetchSingle()

        then:
            with(loaded) { f ->
                f.id == 1L
                f.name == "dark-mode"
                f.values != null
                f.values.size() == 2
                f.values.containsAll([true, false])
            }

        where:
            session << allSessions
    }

    def "should update entity with converted element collection"() {
        given:
            QFeatureFlagEntity qFlag = new QFeatureFlagEntity(null)
            FeatureFlagEntity flag = new FeatureFlagEntity(
                    1L,
                    "dark-mode",
                    List.of(true)
            )
            session.insertEntity(qFlag, flag)

        when:
            FeatureFlagEntity loaded = session.selectEntity(qFlag).whereIdEq(1L).fetchSingle()
            loaded.setValues(List.of(false))
            session.updateEntity(qFlag, loaded)

        then:
            with(session.selectEntity(qFlag).whereIdEq(1L).fetchSingle()) { f ->
                f.values.size() == 1
                f.values[0] == false
            }

        where:
            session << allSessions
    }

    def "should handle empty converted element collection"() {
        given:
            QFeatureFlagEntity qFlag = new QFeatureFlagEntity(null)
            FeatureFlagEntity flag = new FeatureFlagEntity(
                    1L,
                    "dark-mode",
                    List.of()
            )

        when:
            session.insertEntity(qFlag, flag)

        then:
            with(session.selectEntity(qFlag).whereIdEq(1L).fetchSingle()) { f ->
                f.values != null
                f.values.isEmpty()
            }

        where:
            session << allSessions
    }
}
