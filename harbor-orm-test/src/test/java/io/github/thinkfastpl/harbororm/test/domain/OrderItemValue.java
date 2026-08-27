// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Embeddable;
import lombok.Value;

import java.math.BigDecimal;

@Embeddable
@Value
public class OrderItemValue {

    @Column(name = "product_id", nullable = false)
    Long productId;

    @Column(nullable = false)
    BigDecimal amount;

    @Column(name = "unit_price_net", nullable = false)
    BigDecimal unitPriceNet;

    @Column(name = "total_price_net", nullable = false)
    BigDecimal totalPriceNet;

    @Column(name = "vat_rate", nullable = false)
    BigDecimal vatRate;

    @Column(name = "total_price_gross", nullable = false)
    BigDecimal totalPriceGross;
}
