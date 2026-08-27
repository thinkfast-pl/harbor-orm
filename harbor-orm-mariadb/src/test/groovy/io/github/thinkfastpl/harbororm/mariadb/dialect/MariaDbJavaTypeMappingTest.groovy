// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.dialect

import spock.lang.Specification

class MariaDbJavaTypeMappingTest extends Specification {

    def "BigDecimal with scale > precision generates valid DECIMAL type"() {
        expect:
            MariaDbJavaTypeMapping.map(BigDecimal.class, value) == expected

        where:
            value                          || expected
            new BigDecimal("0.001")        || "DECIMAL(3, 3)"    // precision=1, scale=3 → needs adjustment
            new BigDecimal("0.0050")       || "DECIMAL(4, 4)"    // precision=2, scale=4 → needs adjustment
            new BigDecimal("123.45")       || "DECIMAL(5, 2)"    // precision=5, scale=2 → no adjustment needed
            new BigDecimal("1.0")          || "DECIMAL(2, 1)"    // precision=2, scale=1 → no adjustment needed
            new BigDecimal("0.5")          || "DECIMAL(1, 1)"    // precision=1, scale=1 → no adjustment needed
    }

    def "BigDecimal null value returns plain DECIMAL"() {
        expect:
            MariaDbJavaTypeMapping.map(BigDecimal.class, null) == "DECIMAL"
    }
}
