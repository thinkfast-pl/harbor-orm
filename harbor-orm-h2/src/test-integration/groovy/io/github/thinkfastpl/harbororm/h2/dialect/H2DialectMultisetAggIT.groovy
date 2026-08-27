// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable

class H2DialectMultisetAggIT extends H2DialectBaseIT {

    void setup() {
        loadScript("basics.sql")
        loadScript("basics-data-multiset_agg.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "multiset"() {
        given:
            BasicsTable basics = new BasicsTable(null)

            Expression<Record[]> multisetAgg = DSL.multisetAgg(List.of(basics.num, basics.id))

        when:
            List<Record> records = session.select(basics.name, multisetAgg)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { record ->
                record.get(basics.name) == "A"
                record.get(multisetAgg).length == 2
            }
            with(records[1]) { record ->
                record.get(basics.name) == "B"
                record.get(multisetAgg).length == 3
            }
    }
}
