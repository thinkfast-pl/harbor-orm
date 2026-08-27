// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.LazyRef;
import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.*;

@Entity(table = "members")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MemberEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne(joinColumns = @JoinColumn(
            name = "member_id",
            fieldType = Long.class
    ))
    private LazyRef<MemberProfileEntity> profile;
}
