// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Convert;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import io.github.thinkfastpl.harbororm.test.domain.converter.PrefixStringConverter;
import lombok.*;

@Entity(table = "custom_supplier_test")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CustomSupplierEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    @Convert(converter = PrefixStringConverter.class)
    private String value;
}
