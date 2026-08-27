// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import io.github.thinkfastpl.harbororm.postgres.PostgresBaseIT
import io.github.thinkfastpl.harbororm.postgres.repository.set1.BasicEntity
import io.github.thinkfastpl.harbororm.postgres.repository.set1.QBasicEntity

class PostgreSqlDialectEntityInsertIT extends PostgresBaseIT {

    void setup() {
        loadScript("basics.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "insert one"() {
        given:
            QBasicEntity qBasicEntity = new QBasicEntity(alias)
            BasicEntity entity = new BasicEntity(1L, 'Gwen', 111)

        when:
            session.insertEntity(qBasicEntity, entity)

        then:
            session.selectEntity(qBasicEntity).count() == 1
            with(session.selectEntity(qBasicEntity).fetchSingle()) { e ->
                e.id == 1
                e.name == 'Gwen'
                e.num == 111
            }

        where:
            alias << [null, 'a']
    }

    def "insert batch"() {
        given:
            QBasicEntity qBasicEntity = new QBasicEntity(alias)
            var entities = [
                    new BasicEntity(1L, 'Gwen', 111),
                    new BasicEntity(2L, 'Annie', 222),
                    new BasicEntity(3L, 'Ashe', 333),
                    new BasicEntity(4L, 'Lux', 444),
                    new BasicEntity(5L, 'Leona', 555),
            ]

        when:
            session.insertEntityBatch(qBasicEntity, entities, 2)

        then:
            session.selectEntity(qBasicEntity).count() == 5
            with(session.selectEntity(qBasicEntity).fetchAll()) { es ->
                es.size() == 5
                with(es[0]) { e ->
                    e.id == 1
                    e.name == 'Gwen'
                    e.num == 111
                }
                with(es[1]) { e ->
                    e.id == 2
                    e.name == 'Annie'
                    e.num == 222
                }
                with(es[2]) { e ->
                    e.id == 3
                    e.name == 'Ashe'
                    e.num == 333
                }
                with(es[3]) { e ->
                    e.id == 4
                    e.name == 'Lux'
                    e.num == 444
                }
                with(es[4]) { e ->
                    e.id == 5
                    e.name == 'Leona'
                    e.num == 555
                }
            }

        where:
            alias << [null, 'a']
    }
}
