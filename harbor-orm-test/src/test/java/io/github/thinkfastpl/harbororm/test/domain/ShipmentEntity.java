// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.LazyRef;
import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.*;

import java.util.List;

/**
 * Parent entity for testing that relation join columns appear in the
 * generated table classes of children that do not declare the FK field.
 */
@Entity(table = "shipments")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
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
