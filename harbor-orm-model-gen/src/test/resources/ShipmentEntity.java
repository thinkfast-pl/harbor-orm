// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.LazyRef;
import io.github.thinkfastpl.harbororm.api.annotations.*;

import java.util.List;

@Entity(table = "shipments")
public class ShipmentEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne(joinColumns = @JoinColumn(name = "shipment_id", fieldType = Long.class))
    private LazyRef<ShipmentLabelEntity> label;

    @OneToMany(joinColumns = @JoinColumn(name = "shipment_id", fieldType = Long.class))
    private List<ShipmentEventEntity> events;
}
