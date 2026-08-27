// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record

class H2DialectComplexBooleanExpressionIT extends H2DialectBaseIT {

    def "complex boolean expression"() {
        when:
            Record record = session.select([condition]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).class == Boolean.class
            record.get(condition) == result

        where:
            condition                                                  || result
            DSL.constant(true).and(DSL.constant(true).or(false))       || true
            DSL.constant(true).and(DSL.constant(true).or(false)).not() || false
            DSL.constant(true).and(false).or(true)                     || true
            DSL.constant(true).and(false).or(false)                    || false
            DSL.constant(true).or(DSL.constant(true).and(false))       || true
            DSL.constant(false).or(DSL.constant(true).and(false))      || false
    }
}
