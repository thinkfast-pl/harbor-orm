// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import io.github.thinkfastpl.harbororm.postgres.PostgresBaseIT
import io.github.thinkfastpl.harbororm.postgres.repository.set2.BasicConvertedEntity
import io.github.thinkfastpl.harbororm.postgres.repository.set2.QBasicConvertedEntity

class PostgreSqlDialectConverterIT extends PostgresBaseIT {

    void setup() {
        loadScript("basics-converted.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "insert"() {
        given:
            QBasicConvertedEntity qBasicConvertedEntity = new QBasicConvertedEntity(alias)
            BasicConvertedEntity entity = new BasicConvertedEntity(1L, value)

        when:
            session.insertEntity(qBasicConvertedEntity, entity)

        then:
            session.selectEntity(qBasicConvertedEntity).count() == 1
            with(session.selectEntity(qBasicConvertedEntity).where(qBasicConvertedEntity.id.eq(1L)).fetchSingle()) { e ->
                e.id == 1L
                e.value == value
            }

        where:
            alias | value
            null  | true
            null  | false
            'a'   | true
            'a'   | false
    }

    def "select"() {
        given:
            loadScript("basics-converted-data.sql")
            QBasicConvertedEntity entity = new QBasicConvertedEntity(alias)

        when:
            List<BasicConvertedEntity> entities = session.selectEntity(entity).fetchAll()

        then:
            entities.size() == 3
            with(entities[0]) { e ->
                e.id == 1
                e.value
            }
            with(entities[1]) { e ->
                e.id == 2
                !e.value
            }
            with(entities[2]) { e ->
                e.id == 3
                !e.value
            }

        where:
            alias << [null, 'a']
    }

    def "update"() {
        given:
            QBasicConvertedEntity qBasicConvertedEntity = new QBasicConvertedEntity(alias)
            BasicConvertedEntity entity = new BasicConvertedEntity(1L, value)

            session.insertEntity(qBasicConvertedEntity, entity)

        when:
            BasicConvertedEntity loaded = session.selectEntity(qBasicConvertedEntity).fetchSingle()
            loaded.setValue(updatedValue)
            session.updateEntity(qBasicConvertedEntity, loaded)

        then:
            with(session.selectEntity(qBasicConvertedEntity).where(qBasicConvertedEntity.id.eq(1L)).fetchSingle()) { e ->
                e.value == updatedValue
            }

        where:
            alias | value | updatedValue
            null  | true  | true
            null  | true  | false
            null  | false | true
            null  | false | false
            'a'   | true  | true
            'a'   | true  | false
            'a'   | false | true
            'a'   | false | false
    }

    def "delete"() {
        given:
            loadScript("basics-converted-data.sql")
            QBasicConvertedEntity entity = new QBasicConvertedEntity(alias)

        when:
            session.deleteEntityById(entity, 1L)

        then:
            session.selectEntity(entity).count() == 2

        where:
            alias << [null, 'a']
    }
}

