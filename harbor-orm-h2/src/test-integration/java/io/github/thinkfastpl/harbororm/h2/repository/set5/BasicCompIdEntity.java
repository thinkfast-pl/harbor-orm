// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.repository.set5;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Embedded;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.*;

@Entity(table = "basics_comp_id")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BasicCompIdEntity {

    @Id
    @Embedded
    private CompId id;

    @Column(nullable = false)
    private String value;
}
