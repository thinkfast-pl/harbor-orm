// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record

class UnaryOperatorConditionIT extends AbstractHarborIT {

    def "select not"() {
        given:
            Expression<Boolean> expression = DSL.constant(param).not()

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).getClass() == Boolean.class
            record.get(1, Boolean.class) == result
            record.get(expression) == result

        where:
            session << allSessions

        combined:
            param || result
            true || false
            false || true
    }

    def "select not with alias"() {
        given:
            Expression<Boolean> expression = DSL.constant(param).not().as("my_alias")

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).getClass() == Boolean.class
            record.get(1, Boolean.class) == result
            record.get(expression) == result
            record.get("my_alias") == result
            record.get("my_alias", Boolean.class) == result

        where:
            session << allSessions

        combined:
            param || result
            true || false
            false || true
    }

    def "select double not"() {
        given:
            Expression<Boolean> expression = DSL.constant(param).not().not()

        when:
            Record record = session.select([expression]).fetchSingle()

        then:
            record.get(1) == result
            record.get(1).getClass() == Boolean.class
            record.get(1, Boolean.class) == result
            record.get(expression) == result

        where:
            session << allSessions

        combined:
            param || result
            true || true
            false || false
    }
}
