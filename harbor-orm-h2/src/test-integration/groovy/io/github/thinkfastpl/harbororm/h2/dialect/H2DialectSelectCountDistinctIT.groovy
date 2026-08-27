// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.query.BasicsTable

class H2DialectSelectCountDistinctIT extends H2DialectBaseIT {

    void setup() {
        loadScript("basics.sql")
        loadScript("basics-data.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "select count distinct"() {
        given:
            BasicsTable basics = new BasicsTable(alias)

        when:
            long distinct = session
                    .select(DSL.countDistinct(basics.name))
                    .from(basics)
                    .fetchSingle()

        then:
            distinct == 3

        where:
            alias << [null, 'alias']
    }
}
