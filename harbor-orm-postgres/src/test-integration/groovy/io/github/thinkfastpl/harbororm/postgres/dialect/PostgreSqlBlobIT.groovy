// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import io.github.thinkfastpl.harbororm.postgres.PostgresBaseIT
import io.github.thinkfastpl.harbororm.postgres.repository.oid.BasicsBlobEntity
import io.github.thinkfastpl.harbororm.postgres.repository.oid.QBasicsBlobEntity

class PostgreSqlBlobIT extends PostgresBaseIT {

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
            countPgLargeObjects() == 1
    }

    def "update blob data"() {
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
            countPgLargeObjects() == 1
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
            session.clearBlob(readEntity.data)
            session.updateEntity(qBasicsBlobEntity, readEntity)

        then:
            with(session.selectEntity(qBasicsBlobEntity).fetchSingle()) { e ->
                e.id == 1
                e.name == 'Gwen'
                !e.data.isPresent()
            }
            countPgLargeObjects() == 0
    }

    def "replace blob preserves old data when write fails"() {
        given:
            byte[] data = new byte[]{1, 2, 3}

            QBasicsBlobEntity qBasicsBlobEntity = new QBasicsBlobEntity(null)
            BasicsBlobEntity entity = new BasicsBlobEntity(
                    1L,
                    'Gwen',
                    session.createBlob(new ByteArrayInputStream(data), data.length)
            )

            session.insertEntity(qBasicsBlobEntity, entity)

            // InputStream that throws IOException on read
            InputStream failingStream = new InputStream() {
                @Override
                int read() throws IOException {
                    throw new IOException("Simulated write failure")
                }
            }

        when:
            BasicsBlobEntity readEntity = session.selectEntity(qBasicsBlobEntity).fetchSingle()
            readEntity.data.set(failingStream, 100)
            session.updateEntity(qBasicsBlobEntity, readEntity)

        then:
            thrown(RuntimeException)

        and: "old blob data is preserved and no leaked large objects"
            countPgLargeObjects() == 1
            with(session.selectEntity(qBasicsBlobEntity).fetchSingle()) { e ->
                e.data.isPresent()
                session.readBlobAllBytes(e.data) == data
            }
    }

    def "delete with large object"() {
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
            session.deleteEntity(qBasicsBlobEntity, entity)

        then:
            session.selectEntity(qBasicsBlobEntity).count() == 0
            countPgLargeObjects() == 0
    }
}
