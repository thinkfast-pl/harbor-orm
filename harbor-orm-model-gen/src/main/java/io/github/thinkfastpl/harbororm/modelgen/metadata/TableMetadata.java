// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.metadata;

import io.github.thinkfastpl.harbororm.modelgen.utils.StringUtils;
import lombok.NonNull;

import java.util.List;

public record TableMetadata(
        @NonNull String packageName,
        @NonNull String tableName,
        String schemaName,
        @NonNull List<ColumnMetadata> columns
) {
    public String metaClassFullName() {
        return packageName + "." + qTableClassName();
    }

    public String qTableClassName() {
        return StringUtils.toPascalCase(tableName) + "Table";
    }
}
