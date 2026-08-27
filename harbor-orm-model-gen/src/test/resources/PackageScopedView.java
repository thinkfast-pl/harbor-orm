// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.View;

@View(name = "package_scoped_view")
class PackageScopedView {

    @Column(nullable = false)
    private String name;
}
