// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.metadata.QColumn
import io.github.thinkfastpl.harbororm.query.BasicsTable

class H2DialectOnConflictIT extends H2DialectBaseIT {

    void setupSpec() {
        loadScript("basics.sql")
    }

    void cleanupSpec() {
        dropAllObjects()
    }

    def "ON CONFLICT throws UnsupportedOperationException on H2"() {
        given:
            BasicsTable t = new BasicsTable(null)

        when:
            session.insertInto(t)
                    .set(t.id, 1L)
                    .set(t.name, "Alice")
                    .set(t.num, 10)
                    .onConflict(t.id as QColumn)
                    .doNothing()
                    .execute()

        then:
            def ex = thrown(UnsupportedOperationException)
            ex.message == "ON CONFLICT clause is not supported by H2 dialect"
    }
}
