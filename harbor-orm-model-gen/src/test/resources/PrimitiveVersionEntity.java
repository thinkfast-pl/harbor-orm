// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import io.github.thinkfastpl.harbororm.api.annotations.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Entity(table = "primitive_version_products")
@Getter
@AllArgsConstructor
class PrimitiveVersionEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Version
    private long version;
}
