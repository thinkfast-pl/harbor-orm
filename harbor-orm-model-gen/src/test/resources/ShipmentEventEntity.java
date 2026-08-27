// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;

/**
 * Child of ShipmentEntity via @OneToMany. Deliberately has NO field for the
 * shipment_id FK column — the column must still appear in the generated
 * ShipmentEventsTable.
 */
@Entity(table = "shipment_events")
public class ShipmentEventEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String description;
}
