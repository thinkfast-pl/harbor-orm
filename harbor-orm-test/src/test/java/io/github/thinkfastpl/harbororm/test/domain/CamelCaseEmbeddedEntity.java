// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.*;

@Entity(table = "camel_case_embedded", columnNameStrategy = ColumnNameStrategy.CAMEL_CASE)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CamelCaseEmbeddedEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Embedded
    private SimpleAddressEmbeddable address;
}
