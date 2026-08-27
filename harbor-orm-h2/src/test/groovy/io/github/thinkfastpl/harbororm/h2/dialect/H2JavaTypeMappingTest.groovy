// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import spock.lang.Specification

class H2JavaTypeMappingTest extends Specification {

    def "BigDecimal with scale > precision generates valid DEC type"() {
        expect:
            H2JavaTypeMapping.map(BigDecimal.class, value) == expected

        where:
            value                          || expected
            new BigDecimal("0.001")        || "DEC(3, 3)"    // precision=1, scale=3 → needs adjustment
            new BigDecimal("0.0050")       || "DEC(4, 4)"    // precision=2, scale=4 → needs adjustment
            new BigDecimal("123.45")       || "DEC(5, 2)"    // precision=5, scale=2 → no adjustment needed
            new BigDecimal("1.0")          || "DEC(2, 1)"    // precision=2, scale=1 → no adjustment needed
            new BigDecimal("0.5")          || "DEC(1, 1)"    // precision=1, scale=1 → no adjustment needed
    }

    def "BigDecimal null value returns plain DEC"() {
        expect:
            H2JavaTypeMapping.map(BigDecimal.class, null) == "DEC"
    }
}
