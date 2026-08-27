// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.h2.repository.clob.BasicsStringClobEntity
import io.github.thinkfastpl.harbororm.h2.repository.clob.QBasicsStringClobEntity

class H2StringClobIT extends H2DialectBaseIT {

    void setup() {
        loadScript("basics-clob2.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "insert"() {
        given:
            String data = "abc"

            QBasicsStringClobEntity qBasicsClobEntity = new QBasicsStringClobEntity(null)
            BasicsStringClobEntity entity = new BasicsStringClobEntity(
                    1L,
                    'Gwen',
                    data
            )

        when:
            session.insertEntity(qBasicsClobEntity, entity)

        then:
            session.selectEntity(qBasicsClobEntity).count() == 1
            with(session.selectEntity(qBasicsClobEntity).fetchSingle()) { e ->
                e.id == 1
                e.name == 'Gwen'
                e.data == data
            }
    }

    def "update clob data to longer"() {
        given:
            String data = "abc"

            QBasicsStringClobEntity qBasicsClobEntity = new QBasicsStringClobEntity(null)
            BasicsStringClobEntity entity = new BasicsStringClobEntity(
                    1L,
                    'Gwen',
                    data
            )

            session.insertEntity(qBasicsClobEntity, entity)

            String newData = "zxcv"

        when:
            BasicsStringClobEntity readEntity = session.selectEntity(qBasicsClobEntity).fetchSingle()
            readEntity.data = newData
            session.updateEntity(qBasicsClobEntity, readEntity)

        then:
            with(session.selectEntity(qBasicsClobEntity).fetchSingle()) { e ->
                e.id == 1
                e.name == 'Gwen'
                e.data == newData
            }
    }

    def "update clob data to shorter"() {
        given:
            String data = "abcdef"

            QBasicsStringClobEntity qBasicsClobEntity = new QBasicsStringClobEntity(null)
            BasicsStringClobEntity entity = new BasicsStringClobEntity(
                    1L,
                    'Gwen',
                    data
            )

            session.insertEntity(qBasicsClobEntity, entity)

            String newData = "ab"

        when:
            BasicsStringClobEntity readEntity = session.selectEntity(qBasicsClobEntity).fetchSingle()
            readEntity.data = newData
            session.updateEntity(qBasicsClobEntity, readEntity)

        then:
            with(session.selectEntity(qBasicsClobEntity).fetchSingle()) { e ->
                e.id == 1
                e.name == 'Gwen'
                e.data == newData
            }
    }

    def "clear clob"() {
        given:
            String data = "abc"

            QBasicsStringClobEntity qBasicsClobEntity = new QBasicsStringClobEntity(null)
            BasicsStringClobEntity entity = new BasicsStringClobEntity(
                    1L,
                    'Gwen',
                    data
            )

            session.insertEntity(qBasicsClobEntity, entity)

        when:
            BasicsStringClobEntity readEntity = session.selectEntity(qBasicsClobEntity).fetchSingle()
            readEntity.data = null
            session.updateEntity(qBasicsClobEntity, readEntity)

        then:
            with(session.selectEntity(qBasicsClobEntity).fetchSingle()) { e ->
                e.id == 1
                e.name == 'Gwen'
                e.data == null
            }
    }
}
