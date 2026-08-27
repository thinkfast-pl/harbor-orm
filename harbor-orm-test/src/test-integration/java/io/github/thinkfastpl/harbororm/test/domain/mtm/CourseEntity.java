// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain.mtm;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(table = "courses")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CourseEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String title;
}
