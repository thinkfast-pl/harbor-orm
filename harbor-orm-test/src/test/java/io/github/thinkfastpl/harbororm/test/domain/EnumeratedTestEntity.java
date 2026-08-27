// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Enumerated;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import io.github.thinkfastpl.harbororm.api.metadata.EnumMappingType;
import lombok.*;

/**
 * Test entity for @Enumerated annotation functionality.
 * Contains fields with both ORDINAL and STRING enum mappings.
 */
@Entity(table = "enumerated_test")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class EnumeratedTestEntity {

    /**
     * Status enum for testing STRING mapping.
     */
    public enum Status {
        ACTIVE,
        INACTIVE,
        PENDING,
        DELETED
    }

    /**
     * Priority enum for testing ORDINAL mapping.
     */
    public enum Priority {
        LOW,      // ordinal = 0
        MEDIUM,   // ordinal = 1
        HIGH,     // ordinal = 2
        CRITICAL  // ordinal = 3
    }

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    /**
     * Enum stored as STRING (default behavior).
     */
    @Column(nullable = false)
    @Enumerated(EnumMappingType.STRING)
    private Status status;

    /**
     * Enum stored as ORDINAL (integer).
     */
    @Column(nullable = false)
    @Enumerated(EnumMappingType.ORDINAL)
    private Priority priority;

    /**
     * Nullable enum stored as STRING.
     */
    @Column(name = "secondary_status", nullable = true)
    @Enumerated(EnumMappingType.STRING)
    private Status secondaryStatus;

    /**
     * Nullable enum stored as ORDINAL.
     */
    @Column(name = "secondary_priority", nullable = true)
    @Enumerated(EnumMappingType.ORDINAL)
    private Priority secondaryPriority;
}
