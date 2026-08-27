// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.*;

/**
 * Package-scoped test entity. Verifies that the annotation processor mirrors
 * the entity's visibility on the generated Q class (see PackageScopedEntityIT).
 */
@Entity(table = "package_scoped_items")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
class PackageScopedEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;
}
