// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import com.google.gson.Gson
import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.JsonTestEntity
import io.github.thinkfastpl.harbororm.test.domain.QJsonTestEntity
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class PortableFunctionExpressionJsonIT extends AbstractHarborIT {

    private static final Gson GSON = new Gson()

    private static Object parseJson(String json) {
        return GSON.fromJson(json, Object.class)
    }

    def "jsonArray builds a JSON array from varargs elements with mixed types"() {
        when:
            String result = session.select(DSL.jsonArray(
                    DSL.constant(1),
                    DSL.constant(2),
                    DSL.constant("three")))
                    .fetchSingle()

        then:
            parseJson(result) == [1.0d, 2.0d, "three"]

        where:
            session << allSessions
    }

    def "jsonArray List overload builds the same array as varargs"() {
        when:
            String result = session.select(DSL.jsonArray(List.of(
                    DSL.constant(1),
                    DSL.constant(2),
                    DSL.constant("three"))))
                    .fetchSingle()

        then:
            parseJson(result) == [1.0d, 2.0d, "three"]

        where:
            session << allSessions
    }

    def "jsonArray preserves null elements as JSON null on all databases"() {
        when:
            String result = session.select(DSL.jsonArray(
                    DSL.nil(String),
                    DSL.constant("text")))
                    .fetchSingle()

        then:
            parseJson(result) == [null, "text"]

        where:
            session << allSessions
    }

    def "jsonArray without elements builds an empty JSON array"() {
        when:
            String result = session.select(DSL.jsonArray()).fetchSingle()

        then:
            parseJson(result) == []

        where:
            session << allSessions
    }

    def "jsonArray builds a JSON array from column expressions"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alpha", 10)

            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(DSL.jsonArray(basics.name, basics.numero))
                    .from(basics)
                    .where(basics.id.eq(1L))
                    .fetchSingle()

        then:
            parseJson(result) == ["Alpha", 10.0d]

        where:
            session << allSessions
    }

    def "jsonObject builds an inline JSON object with string-literal keys and mixed value types"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alpha", 42)

            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(
                        DSL.jsonObject(
                                DSL.constant("name"), basics.name,
                                DSL.constant("numero"), basics.numero
                        ).as("obj"))
                    .from(basics)
                    .where(basics.id.eq(1L))
                    .fetchSingle()

        then:
            parseJson(result) == [name: "Alpha", numero: 42.0d]

        where:
            session << allSessions
    }

    def "jsonObject builds an inline JSON object from constants only"() {
        when:
            String result = session.select(
                        DSL.jsonObject(
                                DSL.constant("a"), DSL.constant(1),
                                DSL.constant("b"), DSL.constant("two")))
                    .fetchSingle()

        then:
            parseJson(result) == [a: 1.0d, b: "two"]

        where:
            session << allSessions
    }

    def "jsonObject with a single key/value pair"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(7L, "Solo", 99)

            BasicsTable basics = new BasicsTable(null)

        when:
            String result = session.select(
                        DSL.jsonObject(DSL.constant("firstName"), basics.name).as("obj"))
                    .from(basics)
                    .where(basics.id.eq(7L))
                    .fetchSingle()

        then:
            parseJson(result) == [firstName: "Solo"]

        where:
            session << allSessions
    }

    def "jsonObject List overload builds the same object as varargs"() {
        when:
            String result = session.select(DSL.jsonObject(List.of(
                    DSL.constant("a"), DSL.constant(1),
                    DSL.constant("b"), DSL.constant("two"))))
                    .fetchSingle()

        then:
            parseJson(result) == [a: 1.0d, b: "two"]

        where:
            session << allSessions
    }

    def "jsonObject without arguments builds an empty JSON object"() {
        when:
            String result = session.select(DSL.jsonObject()).fetchSingle()

        then:
            parseJson(result) == [:]

        where:
            session << allSessions
    }

    def "jsonArrayLength returns the number of elements of a built JSON array"() {
        when:
            def result = session.select(DSL.jsonArrayLength(DSL.jsonArray(
                    DSL.constant(1),
                    DSL.constant("two"),
                    DSL.constant(3))))
                    .fetchSingle()

        then:
            result == 3

        where:
            session << allSessions
    }

    def "jsonArrayLength of an empty JSON array is zero"() {
        when:
            def result = session.select(DSL.jsonArrayLength(DSL.jsonArray())).fetchSingle()

        then:
            result == 0

        where:
            session << allSessions
    }

    def "jsonArrayLength returns the array length of a JSON column"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(500L, '["a","b","c","d"]', null, null))

        when:
            def result = session.select(DSL.jsonArrayLength(qEntity.data))
                    .from(qEntity.getTableName())
                    .where(qEntity.id.eq(500L))
                    .fetchSingle()

        then:
            result == 4

        where:
            session << allSessions
    }

    def "jsonArrayLength filters rows in a WHERE clause"() {
        given:
            QJsonTestEntity qEntity = new QJsonTestEntity(null)
            session.insertEntity(qEntity, new JsonTestEntity(501L, '[1,2]', null, null))
            session.insertEntity(qEntity, new JsonTestEntity(502L, '[1,2,3,4]', null, null))

        when:
            List<JsonTestEntity> results = session.selectEntity(qEntity)
                    .where(DSL.jsonArrayLength(qEntity.data).gt(2))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].id == 502L

        where:
            session << allSessions
    }
}
