// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Convert;
import io.github.thinkfastpl.harbororm.api.annotations.Param;
import io.github.thinkfastpl.harbororm.api.annotations.StoredFunction;
import io.github.thinkfastpl.harbororm.test.domain.converter.BooleanToYesNoConverter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@StoredFunction(
        name = "get_basics_flags",
        params = {
                @Param(name = "minNumero", type = Integer.class)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetBasicsFlags {

    @Column(nullable = false)
    private String name;

    @Column(name = "active_flag", nullable = false)
    @Convert(converter = BooleanToYesNoConverter.class)
    private Boolean active;
}
