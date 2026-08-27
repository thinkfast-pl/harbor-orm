// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import io.github.thinkfastpl.harbororm.test.domain.converter.ProfileJsonConverter;
import lombok.*;

@Entity(table = "json_test_entities")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class JsonTestEntity {

    @Id
    private Long id;

    @Column(name = "data", nullable = false)
    @Json
    private String data;

    @Column(name = "profile", nullable = false)
    @Json
    private ProfilePojo profile;

    @Column(name = "config", nullable = false)
    @Json
    @Convert(converter = ProfileJsonConverter.class)
    private ProfilePojo config;
}
