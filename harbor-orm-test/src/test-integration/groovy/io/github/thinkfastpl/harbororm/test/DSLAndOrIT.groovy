// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.test.domain.BasicEntity
import io.github.thinkfastpl.harbororm.test.domain.QBasicEntity

class DSLAndOrIT extends AbstractHarborIT {

    def "DSL.and with three conditions filters correctly"() {
        given:
            QBasicEntity q = new QBasicEntity(null)
            session.insertEntity(q, new BasicEntity(1L, "Alpha", 10))
            session.insertEntity(q, new BasicEntity(2L, "Alpha", 20))
            session.insertEntity(q, new BasicEntity(3L, "Alpha", 30))
            session.insertEntity(q, new BasicEntity(4L, "Beta", 20))
            session.insertEntity(q, new BasicEntity(5L, "Gamma", 50))

        when:
            // 3 AND conditions: name = "Alpha" AND numero > 5 AND numero < 25
            def results = session.selectEntity(q)
                    .where(DSL.and(q.name.eq("Alpha"), q.numero.gt(5), q.numero.lt(25)))
                    .orderBy(q.id.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].id == 1L
            results[1].id == 2L

        where:
            session << allSessions
    }

    def "DSL.or with three conditions filters correctly"() {
        given:
            QBasicEntity q = new QBasicEntity(null)
            session.insertEntity(q, new BasicEntity(1L, "Alpha", 10))
            session.insertEntity(q, new BasicEntity(2L, "Beta", 20))
            session.insertEntity(q, new BasicEntity(3L, "Gamma", 30))
            session.insertEntity(q, new BasicEntity(4L, "Delta", 40))

        when:
            // 3 OR conditions: name = "Alpha" OR name = "Gamma" OR name = "Delta"
            def results = session.selectEntity(q)
                    .where(DSL.or(q.name.eq("Alpha"), q.name.eq("Gamma"), q.name.eq("Delta")))
                    .orderBy(q.id.asc())
                    .fetchAll()

        then:
            results.size() == 3
            results[0].id == 1L
            results[1].id == 3L
            results[2].id == 4L

        where:
            session << allSessions
    }

    def "DSL.and with four conditions filters correctly"() {
        given:
            QBasicEntity q = new QBasicEntity(null)
            session.insertEntity(q, new BasicEntity(1L, "Alpha", 10))
            session.insertEntity(q, new BasicEntity(2L, "Alpha", 20))
            session.insertEntity(q, new BasicEntity(3L, "Alpha", 30))
            session.insertEntity(q, new BasicEntity(4L, "Alpha", 40))
            session.insertEntity(q, new BasicEntity(5L, "Beta", 20))

        when:
            // 4 AND conditions: name = "Alpha" AND numero > 5 AND numero < 35 AND id > 1
            def results = session.selectEntity(q)
                    .where(DSL.and(q.name.eq("Alpha"), q.numero.gt(5), q.numero.lt(35), q.id.gt(1L)))
                    .orderBy(q.id.asc())
                    .fetchAll()

        then:
            results.size() == 2
            results[0].id == 2L
            results[1].id == 3L

        where:
            session << allSessions
    }
}
