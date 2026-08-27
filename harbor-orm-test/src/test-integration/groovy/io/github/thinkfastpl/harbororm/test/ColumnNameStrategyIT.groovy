// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.ColumnStrategyEntity
import io.github.thinkfastpl.harbororm.test.domain.QColumnStrategyEntity

class ColumnNameStrategyIT extends AbstractHarborIT {

    def "insert and select entity with default SNAKE_CASE column name strategy"() {
        given:
            QColumnStrategyEntity qEntity = new QColumnStrategyEntity(null)
            ColumnStrategyEntity entity = new ColumnStrategyEntity(1L, "John", "Doe", "555123456")

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).fetchAll()) { results ->
                results.size() == 1
                with(results[0]) { result ->
                    result.id == 1L
                    result.firstName == "John"
                    result.lastName == "Doe"
                    result.phoneNumber == "555123456"
                }
            }

        where:
            session << allSessions
    }

    def "insert and select entity with null optional field"() {
        given:
            QColumnStrategyEntity qEntity = new QColumnStrategyEntity(null)
            ColumnStrategyEntity entity = new ColumnStrategyEntity(2L, "Jane", "Smith", null)

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).fetchAll()) { results ->
                results.size() == 1
                with(results[0]) { result ->
                    result.id == 2L
                    result.firstName == "Jane"
                    result.lastName == "Smith"
                    result.phoneNumber == null
                }
            }

        where:
            session << allSessions
    }

    def "filter by snake_case-mapped column"() {
        given:
            QColumnStrategyEntity qEntity = new QColumnStrategyEntity(null)
            session.insertEntity(qEntity, new ColumnStrategyEntity(10L, "Alice", "Walker", "111"))
            session.insertEntity(qEntity, new ColumnStrategyEntity(11L, "Bob", "Walker", "222"))

        when:
            def results = session.selectEntity(qEntity)
                    .where(qEntity.firstName.eq("Alice"))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].firstName == "Alice"
            results[0].lastName == "Walker"

        where:
            session << allSessions
    }
}
