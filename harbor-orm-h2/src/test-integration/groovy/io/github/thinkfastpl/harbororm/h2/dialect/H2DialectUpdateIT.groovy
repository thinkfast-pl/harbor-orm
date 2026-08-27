// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable

class H2DialectUpdateIT extends H2DialectBaseIT {

    void setup() {
        loadScript("basics.sql")
        loadScript("basics-data.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "update one row"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            session.update(basics)
                    .set(basics.name, "Treblinka")
                    .where(basics.id.eq(1L))
                    .execute()

        then:
            session.select(basics.name)
                    .from(basics)
                    .where(basics.id.eq(1L))
                    .fetchSingle() == "Treblinka"
    }

    def "update one row returning"() {
        given:
            BasicsTable basics = new BasicsTable(null)

        when:
            List<Record> rows = session.update(basics)
                    .set(basics.name, "Treblinka")
                    .where(basics.id.eq(1L))
                    .returning(basics.id, basics.name)
                    .executeAndFetchAll()

        then:
            rows.size() == 1
            with(rows[0]) { r ->
                r.get(basics.id) == 1L
                r.get(basics.name) == "Treblinka"
            }
    }
}
