// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.h2.repository.byta.BasicsByteaEntity
import io.github.thinkfastpl.harbororm.h2.repository.byta.QBasicsByteaEntity

class H2DialectByteaIT extends H2DialectBaseIT {

    void setup() {
        loadScript("basics-bytea.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "insert one"() {
        given:
            QBasicsByteaEntity qEntity = new QBasicsByteaEntity(alias)

        when:
            BasicsByteaEntity entity = new BasicsByteaEntity(
                    1L,
                    "A",
                    new byte[]{(byte) 1, (byte) 2, (byte) 3}
            )
            session.insertEntity(qEntity, entity)

        then:
            session.selectEntity(qEntity).count() == 1
            with(session.selectEntity(qEntity).fetchSingle()) { e ->
                e.id == 1L
                e.name == "A"
                e.data.length == 3
                e.data[0] == (byte) 1
                e.data[1] == (byte) 2
                e.data[2] == (byte) 3
            }

        where:
            alias << [null, 'a']
    }
}
