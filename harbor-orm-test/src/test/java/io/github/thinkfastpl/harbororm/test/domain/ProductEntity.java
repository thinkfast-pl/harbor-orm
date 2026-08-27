// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import io.github.thinkfastpl.harbororm.api.annotations.SequenceGenerated;
import lombok.*;

import java.math.BigDecimal;

@Entity(table = "products")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ProductEntity {

    @Id
    @SequenceGenerated(sequence = "products_id_seq")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "price_net", nullable = false)
    private BigDecimal priceNet;

    @Column(name = "vat_rate", nullable = false)
    private BigDecimal vatRate;

    @Column(name = "price_gross", nullable = false)
    private BigDecimal priceGross;
}
