// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import io.github.thinkfastpl.harbororm.postgres.PostgresBaseIT
import io.github.thinkfastpl.harbororm.postgres.repository.oid.BasicsClobEntity
import io.github.thinkfastpl.harbororm.postgres.repository.oid.QBasicsClobEntity

class PostgreSqlClobIT extends PostgresBaseIT {

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
                session.readClobAllChars(e.data) == data
            }
            countPgLargeObjects() == 1
    }

    def "update clob data"() {
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
                session.readClobAllChars(e.data) == newData
            }
            countPgLargeObjects() == 1
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
            session.clearClob(readEntity.data)
            session.updateEntity(qBasicsClobEntity, readEntity)

        then:
            with(session.selectEntity(qBasicsClobEntity).fetchSingle()) { e ->
                e.id == 1
                e.name == 'Gwen'
                !e.data.isPresent()
            }
            countPgLargeObjects() == 0
    }

    def "replace clob preserves old data when write fails"() {
        given:
            String data = "abc"

            QBasicsClobEntity qBasicsClobEntity = new QBasicsClobEntity(null)
            BasicsClobEntity entity = new BasicsClobEntity(
                    1L,
                    'Gwen',
                    session.createClob(new StringReader(data), data.length())
            )

            session.insertEntity(qBasicsClobEntity, entity)

            // Reader that throws IOException on read
            Reader failingReader = new Reader() {
                @Override
                int read(char[] cbuf, int off, int len) throws IOException {
                    throw new IOException("Simulated write failure")
                }

                @Override
                void close() throws IOException {
                }
            }

        when:
            BasicsClobEntity readEntity = session.selectEntity(qBasicsClobEntity).fetchSingle()
            readEntity.data.set(failingReader, 100)
            session.updateEntity(qBasicsClobEntity, readEntity)

        then:
            thrown(RuntimeException)

        and: "old clob data is preserved and no leaked large objects"
            countPgLargeObjects() == 1
            with(session.selectEntity(qBasicsClobEntity).fetchSingle()) { e ->
                e.data.isPresent()
                session.readClobAllChars(e.data) == data
            }
    }

    def "delete with large object"() {
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
            session.deleteEntity(qBasicsClobEntity, entity)

        then:
            session.selectEntity(qBasicsClobEntity).count() == 0
            countPgLargeObjects() == 0
    }
}
