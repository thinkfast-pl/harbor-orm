// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.h2.repository.set1.BasicEntity
import io.github.thinkfastpl.harbororm.h2.repository.set1.QBasicEntity

class H2DialectEntitySelectIT extends H2DialectBaseIT {

    void setup() {
        loadScript("basics.sql")
        loadScript("basics-data.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "select all"() {
        given:
            QBasicEntity qBasicEntity = new QBasicEntity(alias)

        when:
            List<BasicEntity> entities = session.selectEntity(qBasicEntity).fetchAll()

        then:
            entities.size() == 3
            with(entities[0]) { e ->
                e.id == 1
                e.name == 'Ania'
                e.num == 11
            }
            with(entities[1]) { e ->
                e.id == 2
                e.name == 'Asia'
                e.num == 22
            }
            with(entities[2]) { e ->
                e.id == 3
                e.name == 'Sylwia'
                e.num == 33
            }

        where:
            alias << [null, 'a']
    }

    def "select all with order"() {
        given:
            QBasicEntity qBasicEntity = new QBasicEntity(alias)

        when:
            List<BasicEntity> entities = session.selectEntity(qBasicEntity)
                    .orderBy(qBasicEntity.name.desc(), qBasicEntity.id.asc())
                    .fetchAll()

        then:
            entities.size() == 3
            with(entities[0]) { e ->
                e.id == 3
                e.name == 'Sylwia'
                e.num == 33
            }
            with(entities[1]) { e ->
                e.id == 2
                e.name == 'Asia'
                e.num == 22
            }
            with(entities[2]) { e ->
                e.id == 1
                e.name == 'Ania'
                e.num == 11
            }

        where:
            alias << [null, 'a']
    }

    def "select where"() {
        given:
            QBasicEntity qBasicEntity = new QBasicEntity(alias)

        when:
            List<BasicEntity> entities = session.selectEntity(qBasicEntity)
                    .where(qBasicEntity.name.eq('Asia'))
                    .fetchAll()

        then:
            entities.size() == 1
            with(entities[0]) { e ->
                e.id == 2
                e.name == 'Asia'
                e.num == 22
            }

        when:
            entities = session.selectEntity(qBasicEntity)
                    .where(qBasicEntity.name.eq('Stefan'))
                    .fetchAll()

        then:
            entities.size() == 0

        where:
            alias << [null, 'a']
    }

    def "select for update"() {
        given:
            QBasicEntity qBasicEntity = new QBasicEntity(alias)

        when:
            List<BasicEntity> entities = session.selectEntity(qBasicEntity)
                    .where(qBasicEntity.name.eq('Asia'))
                    .forUpdate()
                    .fetchAll()

        then:
            entities.size() == 1

        where:
            alias << [null, 'a']
    }

    def "count all"() {
        given:
            QBasicEntity qBasicEntity = new QBasicEntity(alias)

        when:
            long count = session.selectEntity(qBasicEntity).count()

        then:
            count == 3

        where:
            alias << [null, 'a']
    }

    def "count where"() {
        given:
            QBasicEntity qBasicEntity = new QBasicEntity(alias)

        when:
            long count = session.selectEntity(qBasicEntity)
                    .where(qBasicEntity.name.eq('Asia'))
                    .count()

        then:
            count == 1

        when:
            count = session.selectEntity(qBasicEntity)
                    .where(qBasicEntity.name.eq('Stefan'))
                    .count()

        then:
            count == 0

        where:
            alias << [null, 'a']
    }

    def "exists any"() {
        given:
            QBasicEntity qBasicEntity = new QBasicEntity(alias)

        when:
            boolean exists = session.selectEntity(qBasicEntity).exists()

        then:
            exists

        where:
            alias << [null, 'a']
    }

    def "exists where"() {
        given:
            QBasicEntity qBasicEntity = new QBasicEntity(alias)

        when:
            boolean exists = session.selectEntity(qBasicEntity)
                    .where(qBasicEntity.name.eq('Asia'))
                    .exists()

        then:
            exists

        when:
            exists = session.selectEntity(qBasicEntity)
                    .where(qBasicEntity.name.eq('Stefan'))
                    .exists()

        then:
            !exists

        where:
            alias << [null, 'a']
    }
}
