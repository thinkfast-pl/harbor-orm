// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import io.github.thinkfastpl.harbororm.api.dialect.StandardDialects;
import io.github.thinkfastpl.harbororm.test.domain.dto.OrderState;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity(table = "orders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class OrderEntity {

    @Id
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    @Enumerated
    @Type(dialect = StandardDialects.POSTGRES, columnType = "order_state")
    private OrderState state;

    @Column(name = "total_price_net", nullable = false)
    private BigDecimal totalPriceNet;

    @Column(name = "total_price_gross", nullable = false)
    private BigDecimal totalPriceGross;

    @Embedded(tableFieldNamePrefix = "addressOfResidence")
    @AttributeOverrides({
            @AttributeOverride(
                    name = AddressValue.Fields.region,
                    column = @Column(name = "residence_region", nullable = false)
            ),
            @AttributeOverride(
                    name = AddressValue.Fields.postalCode,
                    column = @Column(name = "residence_postal_code", nullable = false)
            ),
            @AttributeOverride(
                    name = AddressValue.Fields.town,
                    column = @Column(name = "residence_town", nullable = false)
            ),
            @AttributeOverride(
                    name = AddressValue.Fields.street,
                    column = @Column(name = "residence_street", nullable = false)
            ),
            @AttributeOverride(
                    name = AddressValue.Fields.buildingNo,
                    column = @Column(name = "residence_building_no", nullable = false)
            ),
            @AttributeOverride(
                    name = AddressValue.Fields.apartmentNo,
                    column = @Column(name = "residence_apartment_no", nullable = true)
            ),
    })
    private AddressValue addressOfResidence;

    @Embedded(tableFieldNamePrefix = "addressOfShipment")
    @AttributeOverrides({
            @AttributeOverride(
                    name = AddressValue.Fields.region,
                    column = @Column(name = "shipment_region", nullable = false)
            ),
            @AttributeOverride(
                    name = AddressValue.Fields.postalCode,
                    column = @Column(name = "shipment_postal_code", nullable = false)
            ),
            @AttributeOverride(
                    name = AddressValue.Fields.town,
                    column = @Column(name = "shipment_town", nullable = false)
            ),
            @AttributeOverride(
                    name = AddressValue.Fields.street,
                    column = @Column(name = "shipment_street", nullable = false)
            ),
            @AttributeOverride(
                    name = AddressValue.Fields.buildingNo,
                    column = @Column(name = "shipment_building_no", nullable = false)
            ),
            @AttributeOverride(
                    name = AddressValue.Fields.apartmentNo,
                    column = @Column(name = "shipment_apartment_no", nullable = true)
            ),
    })
    private AddressValue addressOfShipment;

    @ElementCollection(
            table = "order_items",
            joinColumns = @JoinColumn(name = "order_id", fieldType = Long.class)
    )
    private List<@Embedded OrderItemValue> items;
}
