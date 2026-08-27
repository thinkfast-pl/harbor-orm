// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.repository.set_in_to_any;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Enumerated;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import io.github.thinkfastpl.harbororm.api.metadata.EnumMappingType;
import lombok.*;

@Entity(table = "in_to_any_test")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class InToAnyEntity {

    public enum Status {
        ACTIVE,
        INACTIVE,
        PENDING
    }

    public enum Priority {
        HIGH,
        MEDIUM,
        LOW
    }

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumMappingType.STRING)
    private Status status;

    @Column(nullable = false)
    @Enumerated(EnumMappingType.ORDINAL)
    private Priority priority;
}
