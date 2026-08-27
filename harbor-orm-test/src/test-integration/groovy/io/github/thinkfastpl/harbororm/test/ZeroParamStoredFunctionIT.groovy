// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.CountAllBasics
import io.github.thinkfastpl.harbororm.test.domain.CountAllBasicsFunction
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class ZeroParamStoredFunctionIT extends AbstractHarborIT {

    def "zero-param @StoredFunction generates compilable code and can be called"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)
            fixtures.addBasic(3L, "C", 30)

            CountAllBasicsFunction fn = new CountAllBasicsFunction("fn")

        when:
            CountAllBasics result = session.select(fn.call())
                    .fetchSingle()

        then:
            result.total == 3

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "zero-param @StoredFunction returns updated count after insert"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)

            CountAllBasicsFunction fn = new CountAllBasicsFunction("fn")

        when:
            CountAllBasics result = session.select(fn.call())
                    .fetchSingle()

        then:
            result.total == 1

        where:
            session << getSessionsExcept(DbType.H2)
    }
}
