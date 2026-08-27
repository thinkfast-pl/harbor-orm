// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.test.domain.QBasicEntity
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures
import spock.lang.Unroll

class DslBooleanIdentityIT extends AbstractHarborIT {

    @Unroll
    def "empty and() acts as TRUE — returns all rows"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Bob", 20)
            def qEntity = new QBasicEntity(null)

        when:
            def results = session.selectEntity(qEntity)
                .where(DSL.and(List.of()))
                .fetchAll()

        then:
            results.size() == 2

        where:
            session << allSessions
    }

    @Unroll
    def "empty or() acts as FALSE — returns no rows"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Bob", 20)
            def qEntity = new QBasicEntity(null)

        when:
            def results = session.selectEntity(qEntity)
                .where(DSL.or(List.of()))
                .fetchAll()

        then:
            results.size() == 0

        where:
            session << allSessions
    }

    @Unroll
    def "empty and() combined with another condition preserves that condition"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Bob", 20)
            def qEntity = new QBasicEntity(null)

        when:
            def results = session.selectEntity(qEntity)
                .where(DSL.and(List.of()).and(qEntity.name.eq("Alice")))
                .fetchAll()

        then:
            results.size() == 1
            results[0].name == "Alice"

        where:
            session << allSessions
    }

    @Unroll
    def "empty or() combined with another condition via or() preserves that condition"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Bob", 20)
            def qEntity = new QBasicEntity(null)

        when:
            def results = session.selectEntity(qEntity)
                .where(DSL.or(List.of()).or(qEntity.name.eq("Alice")))
                .fetchAll()

        then:
            results.size() == 1
            results[0].name == "Alice"

        where:
            session << allSessions
    }
}
