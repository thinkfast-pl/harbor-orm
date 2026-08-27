// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.h2.repository.json.BasicsJsonEntity
import io.github.thinkfastpl.harbororm.h2.repository.json.QBasicsJsonEntity

/**
 * @Json values must be bound with FORMAT JSON on H2: a plain VARCHAR bind into a
 * native json column is stored as a JSON *string* (double-encoded).
 */
class H2DialectJsonColumnIT extends H2DialectBaseIT {

    void setup() {
        loadScript("basics-json.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "should round-trip @Json into native json and varchar columns"() {
        given:
            QBasicsJsonEntity qEntity = new QBasicsJsonEntity(null)
            String json = '{"key":"value","num":42}'

        when:
            session.insertEntity(qEntity, new BasicsJsonEntity(1L, json, json))

        then:
            with(session.selectEntity(qEntity).fetchSingle()) { e ->
                e.id == 1L
                e.jsonData == json
                e.varcharData == json
            }
    }

    def "should update @Json columns"() {
        given:
            QBasicsJsonEntity qEntity = new QBasicsJsonEntity(null)
            session.insertEntity(qEntity, new BasicsJsonEntity(1L, '{"v":1}', '{"v":1}'))
            String updated = '{"v":2,"list":[1,2]}'

        when:
            BasicsJsonEntity loaded = session.selectEntity(qEntity).fetchSingle()
            loaded.jsonData = updated
            loaded.varcharData = updated
            session.updateEntity(qEntity, loaded)

        then:
            with(session.selectEntity(qEntity).fetchSingle()) { e ->
                e.jsonData == updated
                e.varcharData == updated
            }
    }
}
