// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect

import io.github.thinkfastpl.harbororm.api.converter.AttributeConverterSupplier
import io.github.thinkfastpl.harbororm.api.converter.JsonSerializer
import spock.lang.Specification

class PostgreSqlDialectMetadataSpec extends Specification {

    def dialect = new PostgreSqlDialect()

    def "getName() returns postgres"() {
        expect:
            dialect.getName() == "postgres"
    }

    def "init() accepts supplier and null serializer"() {
        given:
            def supplier = Mock(AttributeConverterSupplier)
        when:
            dialect.init(supplier, null)
        then:
            noExceptionThrown()
    }

    def "init() accepts supplier and non-null serializer"() {
        given:
            def supplier = Mock(AttributeConverterSupplier)
            def serializer = Mock(JsonSerializer)
        when:
            dialect.init(supplier, serializer)
        then:
            noExceptionThrown()
    }
}
