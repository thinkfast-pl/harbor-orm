// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.SelectExpressionTableSource
import io.github.thinkfastpl.harbororm.api.metadata.QColumn
import io.github.thinkfastpl.harbororm.api.query.result.Record

class H2DialectSubSelectTableIT extends H2DialectBaseIT {

    def "select 1"() {
        given:
            SelectExpressionTableSource tableSource = DSL.select().asTableSource("i")

            QColumn<Long> a = tableSource.select(DSL.constant(1L), "a")
            QColumn<Long> b = tableSource.select(DSL.constant(2L), "b")
            QColumn<Long> c = tableSource.select(DSL.constant(3L), "c")

        when:
            Record r = session.select(a, b, c)
                    .from(tableSource)
                    .fetchSingle()

        then:
            r.get(a) == 1L
            r.get(b) == 2L
            r.get(c) == 3L
    }

    def "select record getAsArray"() {
        given:
            SelectExpressionTableSource tableSource = DSL.select().asTableSource("i")

            QColumn<Long> a = tableSource.select(DSL.constant(1L), "a")
            QColumn<String> b = tableSource.select(DSL.constant("b"), "b")

        when:
            Object[] array = session.select(a, b)
                    .from(tableSource)
                    .fetchSingle()
                    .getAsArray()

        then:
            array.length == 2
            array[0] == 1L
            array[1] == "b"
    }

    static class AB {
        Long aArg
        String bArg

        AB(Long aArg, String bArg) {
            this.aArg = aArg
            this.bArg = bArg
        }
    }

    def "select record getAsInstanceOf"() {
        given:
            SelectExpressionTableSource tableSource = DSL.select().asTableSource("i")

            QColumn<Long> a = tableSource.select(DSL.constant(1L), "a")
            QColumn<String> b = tableSource.select(DSL.constant("b"), "b")

        when:
            AB ab = session.select(a, b)
                    .from(tableSource)
                    .fetchSingle()
                    .getAsInstanceOf(AB.class)

        then:
            ab.aArg == 1L
            ab.bArg == "b"
    }
}
