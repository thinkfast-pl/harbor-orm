// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.BasicsStringClobEntity
import io.github.thinkfastpl.harbororm.test.domain.QBasicsStringClobEntity

class ClobStringSupportIT extends AbstractHarborIT {

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

        where:
            session << allSessions
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

        where:
            session << allSessions
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

        where:
            session << allSessions
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

        where:
            session << allSessions
    }
}
