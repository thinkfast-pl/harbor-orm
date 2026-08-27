// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.CommonTableExpression
import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class CommonTableExpressionIT extends AbstractHarborIT {

    def "with select"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "Ania", 11)
            testFixtures.addBasic(2L, "Asia", 22)
            testFixtures.addBasic(3L, "Sylwia", 33)

            BasicsTable basics = new BasicsTable("bt")

            CommonTableExpression cte = new CommonTableExpression("bb")
                    .as(
                            DSL.select(basics.id, basics.name)
                                    .from(basics)
                    )

            CommonTableExpression.Column<Long> bid = cte.column("bid", basics.id)
            CommonTableExpression.Column<String> bname = cte.column("bname", basics.name)

            CommonTableExpression cte2 = new CommonTableExpression("b2")
                    .as(
                            DSL.select(bid)
                                    .from(cte)
                    )

            CommonTableExpression.Column<Long> b2id = cte2.column("b2id", bid)

        when:
            List<Record> records = session
                    .with(cte)
                    .with(cte2)
                    .select(b2id)
                    .from(cte2)
                    .where(b2id.eq(2L))
                    .fetchAll()

        then:
            records.size() == 1
            with(records.get(0)) { r ->
                r.get(b2id) == 2L
            }

        where:
            session << allSessions
    }

    def "with select with extra aliases"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "Ania", 11)
            testFixtures.addBasic(2L, "Asia", 22)
            testFixtures.addBasic(3L, "Sylwia", 33)

            BasicsTable basics = new BasicsTable("bt")

            CommonTableExpression cte = new CommonTableExpression("bb")
                    .as(
                            DSL.select(basics.id.as("xx"), basics.name.as("yy"))
                                    .from(basics)
                    )

            CommonTableExpression.Column<Long> bid = cte.column("bid", basics.id)
            CommonTableExpression.Column<String> bname = cte.column("bname", basics.name)

            CommonTableExpression cte2 = new CommonTableExpression("b2")
                    .as(
                            DSL.select(bid.as("zz"))
                                    .from(cte)
                    )

            CommonTableExpression.Column<Long> b2id = cte2.column("b2id", bid)

        when:
            List<Record> records = session
                    .with(cte)
                    .with(cte2)
                    .select(b2id)
                    .from(cte2)
                    .where(b2id.eq(2L))
                    .fetchAll()

        then:
            records.size() == 1
            with(records.get(0)) { r ->
                r.get(b2id) == 2L
            }

        where:
            session << allSessions
    }

    def "recursive cte"() {
        given:
            CommonTableExpression nums = new CommonTableExpression("nums")
            CommonTableExpression.Column<Integer> numsN = nums.column("n", Integer.class)
            nums.as(
                    DSL
                            .select(DSL.constant(1))
                            .unionAll(
                                    DSL
                                            .select(numsN.add(1))
                                            .from(nums)
                                            .where(numsN.lt(5))
                            )
            )

            CommonTableExpression factorials = new CommonTableExpression("factorials")
            CommonTableExpression.Column<Integer> factorialsN = factorials.column("n", Integer.class)
            CommonTableExpression.Column<Integer> factorialsFact = factorials.column("fact", Integer.class)
            factorials.as(
                    DSL
                            .select(DSL.constant(1), DSL.constant(1))
                            .unionAll(
                                    DSL
                                            .select(factorialsN.add(1), factorialsFact.multiply(DSL.parentheses(factorialsN.add(1))))
                                            .from(factorials)
                                            .where(factorialsN.lt(5))
                            )
            )

        when:
            List<Record> records = session
                    .withRecursive(nums, factorials)
                    .select(numsN, factorialsFact)
                    .from(nums)
                    .join(factorials).on(numsN.eq(factorialsN))
                    .orderBy(numsN.asc())
                    .fetchAll()


        then:
            records.size() == 5
            with(records.get(0)) { r ->
                r.get(numsN) == 1
                r.get(factorialsFact) == 1
            }
            with(records.get(1)) { r ->
                r.get(numsN) == 2
                r.get(factorialsFact) == 2
            }
            with(records.get(2)) { r ->
                r.get(numsN) == 3
                r.get(factorialsFact) == 6
            }
            with(records.get(3)) { r ->
                r.get(numsN) == 4
                r.get(factorialsFact) == 24
            }
            with(records.get(4)) { r ->
                r.get(numsN) == 5
                r.get(factorialsFact) == 120
            }
        where:
            session << allSessions
    }
}
