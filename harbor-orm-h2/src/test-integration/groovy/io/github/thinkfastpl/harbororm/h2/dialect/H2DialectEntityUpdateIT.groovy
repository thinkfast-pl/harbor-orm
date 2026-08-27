// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.h2.repository.set1.BasicEntity
import io.github.thinkfastpl.harbororm.h2.repository.set1.QBasicEntity

class H2DialectEntityUpdateIT extends H2DialectBaseIT {

    void setup() {
        loadScript("basics.sql")
        loadScript("basics-data.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "update"() {
        given:
            QBasicEntity qBasicEntity = new QBasicEntity(alias)
            BasicEntity basicEntity = session.selectEntity(qBasicEntity)
                    .where(qBasicEntity.id.eq(1))
                    .fetchSingle()

        when:
            basicEntity.update("Annie", 1111)
            session.updateEntity(qBasicEntity, basicEntity)

            BasicEntity basicEntity2 = session.selectEntity(qBasicEntity)
                    .where(qBasicEntity.id.eq(1))
                    .fetchSingle()

        then:
            basicEntity2.id == 1
            basicEntity2.name == "Annie"
            basicEntity2.num == 1111

        where:
            alias << [null, 'a']
    }
}
