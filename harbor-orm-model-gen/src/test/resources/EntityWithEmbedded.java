// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Entity(table = "ewe")
@Getter
@AllArgsConstructor
class EntityWithEmbedded {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "postalCode", column = @Column(name = "other_postal_code", nullable = false))
    })
    private AddressEmbeddable address;
}
