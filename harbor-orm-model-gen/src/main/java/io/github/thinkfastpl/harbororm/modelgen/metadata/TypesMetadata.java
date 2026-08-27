// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.metadata;

import java.util.Map;

public record TypesMetadata(
        Map<String, String> typesByDialect
) {
}
