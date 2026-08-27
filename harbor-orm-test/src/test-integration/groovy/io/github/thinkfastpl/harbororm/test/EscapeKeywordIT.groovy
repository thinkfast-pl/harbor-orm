// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL

class EscapeKeywordIT extends AbstractHarborIT {

    def "select with alias containing embedded double quote"() {
        when:
            def result = session.select(DSL.constant(42).as('my"alias')).fetchSingle()

        then:
            result == 42

        where:
            session << allSessions
    }
}
