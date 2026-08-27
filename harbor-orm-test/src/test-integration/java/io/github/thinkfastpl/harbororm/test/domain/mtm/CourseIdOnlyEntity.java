// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain.mtm;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity(table = "tutors")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CourseIdOnlyEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToMany(
            table = "tutor_students",
            joinColumns = @JoinColumn(name = "tutor_id", fieldType = Long.class),
            inverseJoinColumns = @JoinColumn(name = "student_id", fieldType = Long.class)
    )
    private Set<Long> studentIds;
}
