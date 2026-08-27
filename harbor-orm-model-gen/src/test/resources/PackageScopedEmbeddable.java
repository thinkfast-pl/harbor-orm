// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Embeddable;

@Embeddable
class PackageScopedEmbeddable {

    @Column(nullable = false)
    private String street;
}
