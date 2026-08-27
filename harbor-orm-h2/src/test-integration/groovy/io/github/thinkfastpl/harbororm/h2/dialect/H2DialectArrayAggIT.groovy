// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable

class H2DialectArrayAggIT extends H2DialectBaseIT {

    void setup() {
        loadScript("basics.sql")
        loadScript("basics-data-array_agg.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "array_agg"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            Expression<Integer[]> numArray = DSL.arrayAgg(basics.num)

        when:
            List<Record> records = session.select(basics.name, numArray)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { record ->
                record.get(basics.name) == "A"
                record.get(numArray).length == 2
            }
            with(records[1]) { record ->
                record.get(basics.name) == "B"
                record.get(numArray).length == 3
            }
    }

    def "array_agg with order"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            Expression<Integer[]> numArray = DSL.arrayAgg(basics.num, List.of(basics.num.asc()))

        when:
            List<Record> records = session.select(basics.name, numArray)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { record ->
                record.get(basics.name) == "A"
                with(record.get(numArray)) { nums ->
                    nums.length == 2
                    nums[0] == 11
                    nums[1] == 22
                }
            }
            with(records[1]) { record ->
                record.get(basics.name) == "B"
                with(record.get(numArray)) { nums ->
                    nums.length == 3
                    nums[0] == 33
                    nums[1] == 44
                    nums[2] == 55
                }
            }
    }

    def "array_agg with filter"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            Expression<Integer[]> numArray = DSL.arrayAggWhere(
                    basics.num,
                    List.of(basics.num.modulo(2).eq(0)),
                    List.of(basics.num.asc())
            )

        when:
            List<Record> records = session.select(basics.name, numArray)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { record ->
                record.get(basics.name) == "A"
                with(record.get(numArray)) { nums ->
                    nums.length == 1
                    nums[0] == 22
                }
            }
            with(records[1]) { record ->
                record.get(basics.name) == "B"
                with(record.get(numArray)) { nums ->
                    nums.length == 1
                    nums[0] == 44
                }
            }
    }
}
