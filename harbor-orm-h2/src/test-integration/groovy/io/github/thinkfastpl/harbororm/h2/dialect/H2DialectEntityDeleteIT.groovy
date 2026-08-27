// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.h2.repository.set1.BasicEntity
import io.github.thinkfastpl.harbororm.h2.repository.set1.QBasicEntity

class H2DialectEntityDeleteIT extends H2DialectBaseIT {

    void setup() {
        loadScript("basics.sql")
        loadScript("basics-data.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "deleteEntity"() {
        given:
            QBasicEntity qBasicEntity = new QBasicEntity(alias)
            BasicEntity basicEntity = session.selectEntity(qBasicEntity)
                    .where(qBasicEntity.id.eq(1))
                    .fetchSingle()

        when:
            session.deleteEntity(qBasicEntity, basicEntity)

        then:
            session.selectEntity(qBasicEntity).count() == 2

        where:
            alias << [null, 'a']
    }

    def "deleteEntityAll"() {
        given:
            QBasicEntity qBasicEntity = new QBasicEntity(alias)
            List<BasicEntity> basicEntities = session.selectEntity(qBasicEntity)
                    .where(qBasicEntity.id.notEq(1))
                    .fetchAll()

        when:
            session.deleteEntityAll(qBasicEntity, basicEntities)

        then:
            session.selectEntity(qBasicEntity).count() == 1

        where:
            alias << [null, 'a']
    }

    def "deleteEntityById"() {
        given:
            QBasicEntity qBasicEntity = new QBasicEntity(alias)

        when:
            session.deleteEntityById(qBasicEntity, 1L)

        then:
            session.selectEntity(qBasicEntity).count() == 2

        where:
            alias << [null, 'a']
    }

    def "deleteEntityByIds"() {
        given:
            QBasicEntity qBasicEntity = new QBasicEntity(alias)

        when:
            session.deleteEntityByIds(qBasicEntity, List.of(1L, 2L))

        then:
            session.selectEntity(qBasicEntity).count() == 1

        where:
            alias << [null, 'a']
    }
}
