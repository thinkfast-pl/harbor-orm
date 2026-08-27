// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.h2.repository.set5.BasicCompIdEntity
import io.github.thinkfastpl.harbororm.h2.repository.set5.CompId
import io.github.thinkfastpl.harbororm.h2.repository.set5.QBasicCompIdEntity

class H2DialectEntityCompIdIT extends H2DialectBaseIT {

    void setup() {
        loadScript("basics-comp-id.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "insert"() {
        given:
            QBasicCompIdEntity qEntity = new QBasicCompIdEntity(alias)
            BasicCompIdEntity entity = new BasicCompIdEntity(
                    new CompId(CompId.Type.A, 12),
                    "CBA"
            )

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(new CompId(CompId.Type.A, 12))).fetchSingle()) { e ->
                e.id.type == CompId.Type.A
                e.id.num == 12
                e.value == "CBA"
            }

        where:
            alias << [null, 'a']
    }

    def "select eq"() {
        given:
            loadScript("basics-comp-id-data.sql")
            QBasicCompIdEntity qEntity = new QBasicCompIdEntity(alias)

        when:
            BasicCompIdEntity entity = session.selectEntity(qEntity).where(qEntity.id.eq(new CompId(CompId.Type.B, 13))).fetchSingle()

        then:
            with(entity) { e ->
                e.id.type == CompId.Type.B
                e.id.num == 13
                e.value == 'Maly Fiat'
            }

        where:
            alias << [null, 'a']
    }

    def "select in 0"() {
        given:
            loadScript("basics-comp-id-data.sql")
            QBasicCompIdEntity qEntity = new QBasicCompIdEntity(alias)

        when:
            List<BasicCompIdEntity> entities = session.selectEntity(qEntity).where(qEntity.id.in(List.of())).fetchAll()

        then:
            entities.isEmpty()

        where:
            alias << [null, 'a']
    }

    def "select in 1"() {
        given:
            loadScript("basics-comp-id-data.sql")
            QBasicCompIdEntity qEntity = new QBasicCompIdEntity(alias)

        when:
            BasicCompIdEntity entity = session.selectEntity(qEntity).where(qEntity.id.in(List.of(new CompId(CompId.Type.B, 13)))).fetchSingle()

        then:
            with(entity) { e ->
                e.id.type == CompId.Type.B
                e.id.num == 13
                e.value == 'Maly Fiat'
            }

        where:
            alias << [null, 'a']
    }

    def "select in 2"() {
        given:
            loadScript("basics-comp-id-data.sql")
            QBasicCompIdEntity qEntity = new QBasicCompIdEntity(alias)
            BasicCompIdEntity entity = new BasicCompIdEntity(
                    new CompId(CompId.Type.A, 12),
                    "CBA"
            )
            session.insertEntity(qEntity, entity)

        when:
            List<BasicCompIdEntity> entities = session.selectEntity(qEntity).where(qEntity.id.in(List.of(
                    new CompId(CompId.Type.A, 12),
                    new CompId(CompId.Type.B, 13),
            ))).fetchAll()

        then:
            entities.size() == 2
            with(entities[0]) { e ->
                e.id.type == CompId.Type.B
                e.id.num == 13
                e.value == 'Maly Fiat'
            }
            with(entities[1]) { e ->
                e.id.type == CompId.Type.A
                e.id.num == 12
                e.value == 'CBA'
            }

        where:
            alias << [null, 'a']
    }

    def "update"() {
        given:
            loadScript("basics-comp-id-data.sql")
            QBasicCompIdEntity qEntity = new QBasicCompIdEntity(alias)

        when:
            BasicCompIdEntity entity = session.selectEntity(qEntity).where(qEntity.id.eq(new CompId(CompId.Type.B, 13))).fetchSingle()
            entity.setValue("Duzy Fiat")
            session.updateEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(new CompId(CompId.Type.B, 13))).fetchSingle()) { e ->
                e.id.type == CompId.Type.B
                e.id.num == 13
                e.value == 'Duzy Fiat'
            }

        where:
            alias << [null, 'a']
    }
}
