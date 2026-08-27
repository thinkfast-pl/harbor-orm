// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Param;
import io.github.thinkfastpl.harbororm.api.annotations.StoredFunction;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@StoredFunction(
        name = "get_basics_above",
        params = {
                @Param(name = "minNumero", type = int.class)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetBasicsAbovePrimitive {

    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int numero;
}
