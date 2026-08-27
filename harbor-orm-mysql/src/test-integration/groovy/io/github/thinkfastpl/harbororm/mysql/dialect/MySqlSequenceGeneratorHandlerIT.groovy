// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mysql.dialect

import io.github.thinkfastpl.harbororm.mysql.MySqlBaseIT

class MySqlSequenceGeneratorHandlerIT extends MySqlBaseIT {

    def setup() {
        loadScript("sequence-test.sql")
    }

    def cleanup() {
        dropAllObjects()
    }

    def "should return consecutive values when called repeatedly"() {
        given:
            def handler = new MySqlStoredProcedureSequenceGeneratorHandler("harbor_sequence_nextval")

        expect:
            handler.nextVal(connection, "test_seq") == 1L
            handler.nextVal(connection, "test_seq") == 2L
            handler.nextVal(connection, "test_seq") == 3L
    }

    def "should throw when sequence is unknown"() {
        given:
            def handler = new MySqlStoredProcedureSequenceGeneratorHandler("harbor_sequence_nextval")

        when:
            handler.nextVal(connection, "missing_seq")

        then:
            thrown(IllegalStateException)
    }
}
