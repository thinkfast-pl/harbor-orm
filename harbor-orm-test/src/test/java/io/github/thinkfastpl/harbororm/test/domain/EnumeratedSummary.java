// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Convert;
import io.github.thinkfastpl.harbororm.api.annotations.Enumerated;
import io.github.thinkfastpl.harbororm.api.annotations.View;
import io.github.thinkfastpl.harbororm.api.metadata.EnumMappingType;
import io.github.thinkfastpl.harbororm.test.domain.converter.BooleanToYesNoConverter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@View(name = "enumerated_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnumeratedSummary {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumMappingType.STRING)
    private EnumeratedTestEntity.Status status;

    @Column(nullable = false)
    @Enumerated(EnumMappingType.ORDINAL)
    private EnumeratedTestEntity.Priority priority;

    @Column(name = "active_flag", nullable = false)
    @Convert(converter = BooleanToYesNoConverter.class)
    private Boolean active;
}
