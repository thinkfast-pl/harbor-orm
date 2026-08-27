// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import io.github.thinkfastpl.harbororm.api.dialect.StandardDialects;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Entity(table = "role")
@Getter
@AllArgsConstructor
class RoleEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @ElementCollection(
            table = "role_permission",
            joinColumns = @JoinColumn(
                    name = "role_id",
                    fieldType = Long.class
            )
    )
    private List<@Column(name = "permission", nullable = true) String> permissions;
}
