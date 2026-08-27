// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsByteaTable
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class SelectCombinationIT extends AbstractHarborIT {

    def "union"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "Ania", 11)
            testFixtures.addBasic(2L, "Asia", 22)
            testFixtures.addBasic(3L, "Sylwia", 33)

            testFixtures.addBasicBytea(1L, "Ania")
            testFixtures.addBasicBytea(2L, "Adam")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .union(
                            DSL
                                    .select(basicsBytea.id, basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1), DSL.asc(2))
                    .fetchAll()

        then:
            records.size() == 4
            records[0].get(basics.id) == 1
            records[0].get(basics.name) == "Ania"
            records[1].get(basics.id) == 2
            records[1].get(basics.name) == "Adam"
            records[2].get(basics.id) == 2
            records[2].get(basics.name) == "Asia"
            records[3].get(basics.id) == 3
            records[3].get(basics.name) == "Sylwia"

        when:
            records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .union(
                            DSL
                                    .select(basicsBytea.id, basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc("id"), DSL.asc("name"))
                    .fetchAll()

        then:
            records.size() == 4
            records[0].get(basics.id) == 1
            records[0].get(basics.name) == "Ania"
            records[1].get(basics.id) == 2
            records[1].get(basics.name) == "Adam"
            records[2].get(basics.id) == 2
            records[2].get(basics.name) == "Asia"
            records[3].get(basics.id) == 3
            records[3].get(basics.name) == "Sylwia"

        where:
            session << allSessions
    }

    def "union all"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "Ania", 11)
            testFixtures.addBasic(2L, "Asia", 22)
            testFixtures.addBasic(3L, "Sylwia", 33)

            testFixtures.addBasicBytea(1L, "Ania")
            testFixtures.addBasicBytea(2L, "Adam")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .unionAll(
                            DSL
                                    .select(basicsBytea.id, basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1), DSL.asc(2))
                    .fetchAll()

        then:
            records.size() == 5
            records[0].get(basics.id) == 1
            records[0].get(basics.name) == "Ania"
            records[1].get(basics.id) == 1
            records[1].get(basics.name) == "Ania"
            records[2].get(basics.id) == 2
            records[2].get(basics.name) == "Adam"
            records[3].get(basics.id) == 2
            records[3].get(basics.name) == "Asia"
            records[4].get(basics.id) == 3
            records[4].get(basics.name) == "Sylwia"

        when:
            records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .unionAll(
                            DSL
                                    .select(basicsBytea.id, basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc("id"), DSL.asc("name"))
                    .fetchAll()

        then:
            records.size() == 5
            records[0].get(basics.id) == 1
            records[0].get(basics.name) == "Ania"
            records[1].get(basics.id) == 1
            records[1].get(basics.name) == "Ania"
            records[2].get(basics.id) == 2
            records[2].get(basics.name) == "Adam"
            records[3].get(basics.id) == 2
            records[3].get(basics.name) == "Asia"
            records[4].get(basics.id) == 3
            records[4].get(basics.name) == "Sylwia"

        where:
            session << allSessions
    }

    def "intersect"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "Ania", 11)
            testFixtures.addBasic(2L, "Asia", 22)
            testFixtures.addBasic(3L, "Sylwia", 33)
            testFixtures.addBasic(4L, "Ania", 44)

            testFixtures.addBasicBytea(1L, "Ania")
            testFixtures.addBasicBytea(2L, "Adam")
            testFixtures.addBasicBytea(3L, "Ania")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> records = session
                    .select(basics.name)
                    .from(basics)
                    .intersect(
                            DSL
                                    .select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .fetchAll()

        then:
            records.size() == 1
            records[0] == "Ania"

        where:
            session << allSessions
    }

    def "intersect all"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "Ania", 11)
            testFixtures.addBasic(2L, "Asia", 22)
            testFixtures.addBasic(3L, "Sylwia", 33)
            testFixtures.addBasic(4L, "Ania", 44)

            testFixtures.addBasicBytea(1L, "Ania")
            testFixtures.addBasicBytea(2L, "Adam")
            testFixtures.addBasicBytea(3L, "Ania")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> records = session
                    .select(basics.name)
                    .from(basics)
                    .intersectAll(
                            DSL
                                    .select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            records.size() == 2
            records[0] == "Ania"
            records[1] == "Ania"

        when:
            records = session
                    .select(basics.name)
                    .from(basics)
                    .intersectAll(
                            DSL
                                    .select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc("name"))
                    .fetchAll()

        then:
            records.size() == 2
            records[0] == "Ania"
            records[1] == "Ania"

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "except"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "Ania", 11)
            testFixtures.addBasic(2L, "Asia", 22)
            testFixtures.addBasic(3L, "Sylwia", 33)
            testFixtures.addBasic(4L, "Ania", 44)
            testFixtures.addBasic(5L, "Sylwia", 55)

            testFixtures.addBasicBytea(1L, "Ania")
            testFixtures.addBasicBytea(2L, "Adam")
            testFixtures.addBasicBytea(3L, "Ania")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> records = session
                    .select(basics.name)
                    .from(basics)
                    .except(
                            DSL
                                    .select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            records.size() == 2
            records[0] == "Asia"
            records[1] == "Sylwia"

        when:
            records = session
                    .select(basics.name)
                    .from(basics)
                    .except(
                            DSL
                                    .select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc("name"))
                    .fetchAll()

        then:
            records.size() == 2
            records[0] == "Asia"
            records[1] == "Sylwia"

        where:
            session << allSessions
    }

    def "except all"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "Ania", 11)
            testFixtures.addBasic(2L, "Asia", 22)
            testFixtures.addBasic(3L, "Sylwia", 33)
            testFixtures.addBasic(4L, "Ania", 44)
            testFixtures.addBasic(5L, "Sylwia", 55)

            testFixtures.addBasicBytea(1L, "Ania")
            testFixtures.addBasicBytea(2L, "Adam")
            testFixtures.addBasicBytea(3L, "Ania")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when:
            List<String> records = session
                    .select(basics.name)
                    .from(basics)
                    .exceptAll(
                            DSL
                                    .select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc(1))
                    .fetchAll()

        then:
            records.size() == 3
            records[0] == "Asia"
            records[1] == "Sylwia"
            records[2] == "Sylwia"

        when:
            records = session
                    .select(basics.name)
                    .from(basics)
                    .exceptAll(
                            DSL
                                    .select(basicsBytea.name)
                                    .from(basicsBytea)
                    )
                    .orderBy(DSL.asc("name"))
                    .fetchAll()

        then:
            records.size() == 3
            records[0] == "Asia"
            records[1] == "Sylwia"
            records[2] == "Sylwia"

        where:
            session << getSessionsExcept(DbType.H2)
    }
}
