// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.*;

/**
 * Child of {@link ShipmentEntity} via @OneToMany. Deliberately has NO field
 * for the shipment_id FK column — the column must still appear in the
 * generated ShipmentEventsTable.
 */
@Entity(table = "shipment_events")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ShipmentEventEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String description;
}
