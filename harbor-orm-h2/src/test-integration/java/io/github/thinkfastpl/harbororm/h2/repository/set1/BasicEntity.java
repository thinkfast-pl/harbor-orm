// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.repository.set1;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(table = "basics")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BasicEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "numero", nullable = false)
    private int num;

    public void update(String name, int num) {
        this.name = name;
        this.num = num;
    }
}
