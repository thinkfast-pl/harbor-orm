// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.test.domain.GetBasicsAbovePrimitive
import io.github.thinkfastpl.harbororm.test.domain.GetBasicsAbovePrimitiveFunction
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

/**
 * Integration tests for @StoredFunction with primitive type parameters (M-9 bug fix).
 *
 * Verifies that @Param(type = int.class) generates valid code with boxed types
 * in Expression<Integer> and ConstantExpression<>(Integer.class, ...).
 */
class PrimitiveParamStoredFunctionIT extends AbstractHarborIT {

    def "raw-value call() works with primitive int parameter"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)
            fixtures.addBasic(3L, "C", 30)

            GetBasicsAbovePrimitiveFunction fn = new GetBasicsAbovePrimitiveFunction("fn")

        when:
            List<GetBasicsAbovePrimitive> results = session.select(fn.call(15))
                    .fetchAll()

        then:
            results.size() == 2
            results[0].name == "B"
            results[0].numero == 20
            results[1].name == "C"
            results[1].numero == 30

        where:
            session << getSessionsExcept(DbType.H2)
    }

    def "expression-based call() works with primitive int parameter"() {
        given:
            def fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)
            fixtures.addBasic(3L, "C", 30)

            GetBasicsAbovePrimitiveFunction fn = new GetBasicsAbovePrimitiveFunction("fn")

        when:
            List<GetBasicsAbovePrimitive> results = session.select(fn.call(DSL.constant(15)))
                    .fetchAll()

        then:
            results.size() == 2
            results[0].name == "B"
            results[0].numero == 20
            results[1].name == "C"
            results[1].numero == 30

        where:
            session << getSessionsExcept(DbType.H2)
    }
}
