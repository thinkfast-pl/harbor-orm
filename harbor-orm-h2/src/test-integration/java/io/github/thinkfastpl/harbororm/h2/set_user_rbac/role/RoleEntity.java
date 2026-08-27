// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.set_user_rbac.role;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.*;

import java.util.List;

@Entity(table = "role")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class RoleEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    @Setter
    private String name;

    @ElementCollection(
            table = "role_permission",
            joinColumns = @JoinColumn(
                    name = "role_id",
                    fieldType = Long.class
            )
    )
    @Setter
    private List<@Column(name = "permission", nullable = false) @Enumerated Permission> permissions;
}
