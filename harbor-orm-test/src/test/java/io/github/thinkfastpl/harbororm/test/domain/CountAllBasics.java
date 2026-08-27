// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.StoredFunction;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@StoredFunction(
        name = "count_all_basics"
)
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CountAllBasics {

    @Column(nullable = false)
    private Long total;
}
