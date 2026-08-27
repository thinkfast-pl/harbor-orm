// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql

import io.github.thinkfastpl.harbororm.core.converter.DefaultAttributeConverterSupplier
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlDialect
import spock.lang.Specification

import java.sql.Connection
import java.sql.ResultSet

class RecordIteratorTest extends Specification {

    def connection = Mock(Connection)
    def rdbmsSupport = Mock(RdbmsSupport)
    def dialect = Mock(SqlDialect)
    def resultSet = Mock(ResultSet)

    def setup() {
        rdbmsSupport.getColumnsLabels(connection, resultSet) >> [:]
    }

    private ExpressionsBasedRecordFactory newFactory() {
        new ExpressionsBasedRecordFactory(connection, rdbmsSupport, dialect, resultSet, [],
                new DefaultAttributeConverterSupplier(), null)
    }

    def "first hasNext() loads and maps a full batch of fetchSize records"() {
        given:
            def mapped = 0
            def iterator = new RecordIterator<Integer>(newFactory(), { r -> ++mapped }, 3)

        when:
            def result = iterator.hasNext()

        then:
            3 * resultSet.next() >> true
            result
            mapped == 3
    }

    def "iterates all records across batches, mapping in order"() {
        given:
            resultSet.next() >>> [true, true, true, true, true, false]
            def counter = 0
            def iterator = new RecordIterator<Integer>(newFactory(), { r -> ++counter }, 2)

        when:
            def results = []
            while (iterator.hasNext()) {
                results << iterator.next()
            }

        then:
            results == [1, 2, 3, 4, 5]

        when:
            iterator.next()

        then:
            thrown(NoSuchElementException)
    }

    def "partial last batch smaller than fetchSize is served fully"() {
        given:
            resultSet.next() >>> [true, true, false]
            def counter = 0
            def iterator = new RecordIterator<Integer>(newFactory(), { r -> ++counter }, 5)

        when:
            def results = []
            while (iterator.hasNext()) {
                results << iterator.next()
            }

        then:
            results == [1, 2]
    }

    def "empty result set yields no elements and next() throws"() {
        given:
            resultSet.next() >> false
            def iterator = new RecordIterator<Integer>(newFactory(), { r -> 1 }, 3)

        expect:
            !iterator.hasNext()

        when:
            iterator.next()

        then:
            thrown(NoSuchElementException)
    }

    def "fetchSize lower than 1 is clamped to 1"() {
        given:
            resultSet.next() >>> [true, false]
            def counter = 0
            def iterator = new RecordIterator<Integer>(newFactory(), { r -> ++counter }, 0)

        when:
            def results = []
            while (iterator.hasNext()) {
                results << iterator.next()
            }

        then:
            results == [1]
    }
}
