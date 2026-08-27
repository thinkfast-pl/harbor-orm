// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.metadata;

import lombok.NonNull;

import java.util.List;

public record StoredFunctionMetadata(
        @NonNull String packageName,
        @NonNull String className,
        boolean publicClass,
        @NonNull String functionName,
        String schemaName,
        @NonNull List<StoredFunctionParamMetadata> params,
        @NonNull List<ColumnMetadata> columns
) {
    public String generatedClassName() {
        return className + "Function";
    }

    public String generatedClassFullName() {
        return packageName + "." + generatedClassName();
    }
}
