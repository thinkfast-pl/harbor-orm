// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import io.github.thinkfastpl.harbororm.api.metadata.EnumMappingType;
import lombok.*;

import java.util.List;

@Entity(table = "tasks")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TaskEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @ElementCollection(
            table = "task_priorities",
            joinColumns = @JoinColumn(name = "task_id", fieldType = Long.class)
    )
    private List<
            @Column(name = "priority", nullable = false)
            @Enumerated(EnumMappingType.ORDINAL) TaskPriority> priorities;
}
