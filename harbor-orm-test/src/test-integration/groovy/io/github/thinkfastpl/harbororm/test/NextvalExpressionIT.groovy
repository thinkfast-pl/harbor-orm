// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL

class NextvalExpressionIT extends AbstractHarborIT {

    def "select nextval()"() {
        when:
            Long first = session.select(DSL.nextval("orders_id_seq")).fetchSingle()
            Long second = session.select(DSL.nextval("orders_id_seq")).fetchSingle()

        then:
            first instanceof Long
            second instanceof Long
            second > first

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }
}
