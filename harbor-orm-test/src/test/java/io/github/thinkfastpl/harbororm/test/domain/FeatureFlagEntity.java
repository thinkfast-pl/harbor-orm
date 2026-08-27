// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import io.github.thinkfastpl.harbororm.test.domain.converter.BooleanToYesNoConverter;
import lombok.*;

import java.util.List;

/**
 * Test entity for @ElementCollection with @Convert on type-use position.
 * Validates that annotation processing handles @Convert via dynamic proxy (C-7 bug fix).
 */
@Entity(table = "feature_flags")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class FeatureFlagEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @ElementCollection(
            table = "feature_flag_values",
            joinColumns = @JoinColumn(name = "feature_flag_id", fieldType = Long.class)
    )
    private List<
            @Column(name = "enabled", nullable = false)
            @Convert(converter = BooleanToYesNoConverter.class) Boolean> values;
}
