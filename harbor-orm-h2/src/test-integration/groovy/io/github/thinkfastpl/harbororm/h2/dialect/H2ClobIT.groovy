// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.h2.repository.clob.BasicsClobEntity
import io.github.thinkfastpl.harbororm.h2.repository.clob.QBasicsClobEntity

class H2ClobIT extends H2DialectBaseIT {

    void setup() {
        loadScript("basics-clob.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "insert"() {
        given:
            String data = "abc"

            QBasicsClobEntity qBasicsClobEntity = new QBasicsClobEntity(null)
            BasicsClobEntity entity = new BasicsClobEntity(
                    1L,
                    'Gwen',
                    session.createClob(new StringReader(data), data.length())
            )

        when:
            session.insertEntity(qBasicsClobEntity, entity)

        then:
            session.selectEntity(qBasicsClobEntity).count() == 1
            with(session.selectEntity(qBasicsClobEntity).fetchSingle()) { e ->
                e.id == 1
                e.name == 'Gwen'
                e.data.isPresent()

                with(session.readClobAllChars(e.data)) { readBytes ->
                    readBytes == data
                }
            }
    }

    def "update clob data to longer"() {
        given:
            String data = "abc"

            QBasicsClobEntity qBasicsClobEntity = new QBasicsClobEntity(null)
            BasicsClobEntity entity = new BasicsClobEntity(
                    1L,
                    'Gwen',
                    session.createClob(new StringReader(data), data.length())
            )

            session.insertEntity(qBasicsClobEntity, entity)

            String newData = "zxcv"

        when:
            BasicsClobEntity readEntity = session.selectEntity(qBasicsClobEntity).fetchSingle()
            session.updateClob(readEntity.data, new StringReader(newData), newData.length())
            session.updateEntity(qBasicsClobEntity, readEntity)

        then:
            with(session.selectEntity(qBasicsClobEntity).fetchSingle()) { e ->
                e.id == 1
                e.name == 'Gwen'
                e.data.isPresent()

                with(session.readClobAllChars(e.data)) { readBytes ->
                    readBytes == newData
                }
            }
    }

    def "update clob data to shorter"() {
        given:
            String data = "abcdef"

            QBasicsClobEntity qBasicsClobEntity = new QBasicsClobEntity(null)
            BasicsClobEntity entity = new BasicsClobEntity(
                    1L,
                    'Gwen',
                    session.createClob(new StringReader(data), data.length())
            )

            session.insertEntity(qBasicsClobEntity, entity)

            String newData = "ab"

        when:
            BasicsClobEntity readEntity = session.selectEntity(qBasicsClobEntity).fetchSingle()
            session.updateClob(readEntity.data, new StringReader(newData), newData.length())
            session.updateEntity(qBasicsClobEntity, readEntity)

        then:
            with(session.selectEntity(qBasicsClobEntity).fetchSingle()) { e ->
                e.id == 1
                e.name == 'Gwen'
                e.data.isPresent()

                with(session.readClobAllChars(e.data)) { readBytes ->
                    readBytes == newData
                }
            }
    }

    def "clear clob"() {
        given:
            String data = "abc"

            QBasicsClobEntity qBasicsClobEntity = new QBasicsClobEntity(null)
            BasicsClobEntity entity = new BasicsClobEntity(
                    1L,
                    'Gwen',
                    session.createClob(new StringReader(data), data.length())
            )

            session.insertEntity(qBasicsClobEntity, entity)

        when:
            BasicsClobEntity readEntity = session.selectEntity(qBasicsClobEntity).fetchSingle()
            readEntity.data.clear()
            session.updateEntity(qBasicsClobEntity, readEntity)

        then:
            with(session.selectEntity(qBasicsClobEntity).fetchSingle()) { e ->
                e.id == 1
                e.name == 'Gwen'
                !e.data.isPresent()
            }
    }
}
