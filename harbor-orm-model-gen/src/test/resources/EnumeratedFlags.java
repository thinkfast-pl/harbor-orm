// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Enumerated;
import io.github.thinkfastpl.harbororm.api.annotations.StoredFunction;
import io.github.thinkfastpl.harbororm.api.metadata.EnumMappingType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@StoredFunction(name = "get_enumerated_flags")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class EnumeratedFlags {

    public enum Status {
        ACTIVE,
        INACTIVE
    }

    public enum Priority {
        LOW,
        HIGH
    }

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumMappingType.STRING)
    private Status status;

    @Column(nullable = false)
    @Enumerated(EnumMappingType.ORDINAL)
    private Priority priority;
}
