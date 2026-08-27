// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;

/**
 * Child of ShipmentEntity via @OneToOne. Deliberately has NO field for the
 * shipment_id FK column — the column must still appear in the generated
 * ShipmentLabelsTable.
 */
@Entity(table = "shipment_labels")
public class ShipmentLabelEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String barcode;
}
