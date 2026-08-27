// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.metadata;

import lombok.NonNull;

public record StoredFunctionParamMetadata(
        @NonNull String name,
        @NonNull String typeName
) {
}
