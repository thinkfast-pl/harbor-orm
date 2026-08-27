// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.View;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@View(name = "basics_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BasicSummary {

    @Column(nullable = false)
    private String name;

    @Column(name = "numero", nullable = false)
    private int total;
}
