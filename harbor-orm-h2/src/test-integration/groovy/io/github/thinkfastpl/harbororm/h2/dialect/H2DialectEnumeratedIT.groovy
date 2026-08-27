// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.h2.repository.set4.BasicEnumeratedEntity
import io.github.thinkfastpl.harbororm.h2.repository.set4.QBasicEnumeratedEntity

class H2DialectEnumeratedIT extends H2DialectBaseIT {

    void setup() {
        loadScript("basics-enumerated.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "insert"() {
        given:
            QBasicEnumeratedEntity qEntity = new QBasicEnumeratedEntity(null)
            BasicEnumeratedEntity entity = new BasicEnumeratedEntity(
                    1L,
                    BasicEnumeratedEntity.State.ACTIVE,
                    BasicEnumeratedEntity.State.INACTIVE,
                    BasicEnumeratedEntity.Type.FIRST
            )

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { e ->
                e.id == 1L
                e.state1 == BasicEnumeratedEntity.State.ACTIVE
                e.state2 == BasicEnumeratedEntity.State.INACTIVE
                e.type == BasicEnumeratedEntity.Type.FIRST
            }
    }

    def "update"() {
        given:
            QBasicEnumeratedEntity qEntity = new QBasicEnumeratedEntity(null)
            BasicEnumeratedEntity entity = new BasicEnumeratedEntity(
                    1L,
                    BasicEnumeratedEntity.State.ACTIVE,
                    BasicEnumeratedEntity.State.INACTIVE,
                    BasicEnumeratedEntity.Type.FIRST
            )
            session.insertEntity(qEntity, entity)

        when:
            BasicEnumeratedEntity loaded = session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()
            loaded.setState1(BasicEnumeratedEntity.State.INACTIVE)
            loaded.setState2(BasicEnumeratedEntity.State.ACTIVE)
            loaded.setType(BasicEnumeratedEntity.Type.SECOND)
            session.updateEntity(qEntity, loaded)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { e ->
                e.id == 1L
                e.state1 == BasicEnumeratedEntity.State.INACTIVE
                e.state2 == BasicEnumeratedEntity.State.ACTIVE
                e.type == BasicEnumeratedEntity.Type.SECOND
            }
    }
}
