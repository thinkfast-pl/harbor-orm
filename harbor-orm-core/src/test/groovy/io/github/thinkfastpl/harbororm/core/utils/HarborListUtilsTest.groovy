// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.utils

import spock.lang.Specification

import java.util.stream.StreamSupport

class HarborListUtilsTest extends Specification {

    def "batch"() {
        given:
            List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9)

        when:
            List<List<Integer>> batches = StreamSupport.stream(HarborListUtils.batch(list, 4).spliterator(), false).toList()

        then:
            batches.size() == 3
            batches[0].size() == 4
            batches[1].size() == 4
            batches[2].size() == 1

            batches[0][0] == 1
            batches[0][1] == 2
            batches[0][2] == 3
            batches[0][3] == 4
            batches[1][0] == 5
            batches[1][1] == 6
            batches[1][2] == 7
            batches[1][3] == 8
            batches[2][0] == 9
    }
}
