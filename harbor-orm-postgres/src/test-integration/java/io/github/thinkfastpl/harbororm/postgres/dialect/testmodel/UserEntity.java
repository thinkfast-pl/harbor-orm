// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect.testmodel;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import io.github.thinkfastpl.harbororm.api.annotations.ValueObject;
import lombok.Getter;
import lombok.Value;

@Entity(table = "users")
@Getter
class UserEntity {

    @ValueObject
    @Value
    static class Phone {

        @Column(nullable = false)
        String phoneNo;

        @Column(nullable = false)
        String description;
    }

    @Id
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;
}
