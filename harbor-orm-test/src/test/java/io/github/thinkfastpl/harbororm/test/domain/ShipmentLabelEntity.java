// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.*;

/**
 * Child of {@link ShipmentEntity} via @OneToOne. Deliberately has NO field
 * for the shipment_id FK column — the column must still appear in the
 * generated ShipmentLabelsTable.
 */
@Entity(table = "shipment_labels")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ShipmentLabelEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String barcode;
}
