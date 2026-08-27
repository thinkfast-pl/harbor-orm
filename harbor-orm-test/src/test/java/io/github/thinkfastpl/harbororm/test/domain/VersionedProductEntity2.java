// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import io.github.thinkfastpl.harbororm.api.annotations.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(table = "versioned_products2")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VersionedProductEntity2 {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(updatable = false, nullable = false)
    private String sku;

    @Column(nullable = false)
    @Version
    private long version;
}
