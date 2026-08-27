// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.*;

@Entity(table = "member_profiles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MemberProfileEntity {

    @Id
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String bio;
}
