// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain.mtm;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity(table = "students")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StudentEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToMany(
            table = "student_courses",
            joinColumns = @JoinColumn(name = "student_id", fieldType = Long.class),
            inverseJoinColumns = @JoinColumn(name = "course_id", fieldType = Long.class)
    )
    private Set<CourseEntity> courses;
}
