// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.h2.repository.blob.BasicsBlobEntity
import io.github.thinkfastpl.harbororm.h2.repository.blob.QBasicsBlobEntity

class H2BlobIT extends H2DialectBaseIT {

    void setup() {
        loadScript("basics-blob.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "insert"() {
        given:
            byte[] data = new byte[]{1, 2, 3}

            QBasicsBlobEntity qBasicsBlobEntity = new QBasicsBlobEntity(null)
            BasicsBlobEntity entity = new BasicsBlobEntity(
                    1L,
                    'Gwen',
                    session.createBlob(new ByteArrayInputStream(data), data.length)
            )

        when:
            session.insertEntity(qBasicsBlobEntity, entity)

        then:
            session.selectEntity(qBasicsBlobEntity).count() == 1
            with(session.selectEntity(qBasicsBlobEntity).fetchSingle()) { e ->
                e.id == 1
                e.name == 'Gwen'
                e.data.isPresent()

                with(session.readBlobAllBytes(e.data)) { readBytes ->
                    readBytes == data
                }
            }
    }

    def "update blob data to longer"() {
        given:
            byte[] data = new byte[]{1, 2, 3}

            QBasicsBlobEntity qBasicsBlobEntity = new QBasicsBlobEntity(null)
            BasicsBlobEntity entity = new BasicsBlobEntity(
                    1L,
                    'Gwen',
                    session.createBlob(new ByteArrayInputStream(data), data.length)
            )

            session.insertEntity(qBasicsBlobEntity, entity)

            byte[] newData = new byte[]{4, 5, 6, 7}

        when:
            BasicsBlobEntity readEntity = session.selectEntity(qBasicsBlobEntity).fetchSingle()
            session.updateBlob(readEntity.data, new ByteArrayInputStream(newData), newData.length)
            session.updateEntity(qBasicsBlobEntity, readEntity)

        then:
            with(session.selectEntity(qBasicsBlobEntity).fetchSingle()) { e ->
                e.id == 1
                e.name == 'Gwen'
                e.data.isPresent()

                with(session.readBlobAllBytes(e.data)) { readBytes ->
                    readBytes == newData
                }
            }
    }

    def "update blob data to shorter"() {
        given:
            byte[] data = new byte[]{1, 2, 3}

            QBasicsBlobEntity qBasicsBlobEntity = new QBasicsBlobEntity(null)
            BasicsBlobEntity entity = new BasicsBlobEntity(
                    1L,
                    'Gwen',
                    session.createBlob(new ByteArrayInputStream(data), data.length)
            )

            session.insertEntity(qBasicsBlobEntity, entity)

            byte[] newData = new byte[]{4, 5}

        when:
            BasicsBlobEntity readEntity = session.selectEntity(qBasicsBlobEntity).fetchSingle()
            session.updateBlob(readEntity.data, new ByteArrayInputStream(newData), newData.length)
            session.updateEntity(qBasicsBlobEntity, readEntity)

        then:
            with(session.selectEntity(qBasicsBlobEntity).fetchSingle()) { e ->
                e.id == 1
                e.name == 'Gwen'
                e.data.isPresent()

                with(session.readBlobAllBytes(e.data)) { readBytes ->
                    readBytes == newData
                }
            }
    }

    def "clear blob"() {
        given:
            byte[] data = new byte[]{1, 2, 3}

            QBasicsBlobEntity qBasicsBlobEntity = new QBasicsBlobEntity(null)
            BasicsBlobEntity entity = new BasicsBlobEntity(
                    1L,
                    'Gwen',
                    session.createBlob(new ByteArrayInputStream(data), data.length)
            )

            session.insertEntity(qBasicsBlobEntity, entity)

        when:
            BasicsBlobEntity readEntity = session.selectEntity(qBasicsBlobEntity).fetchSingle()
            readEntity.data.clear()
            session.updateEntity(qBasicsBlobEntity, readEntity)

        then:
            with(session.selectEntity(qBasicsBlobEntity).fetchSingle()) { e ->
                e.id == 1
                e.name == 'Gwen'
                !e.data.isPresent()
            }
    }
}
