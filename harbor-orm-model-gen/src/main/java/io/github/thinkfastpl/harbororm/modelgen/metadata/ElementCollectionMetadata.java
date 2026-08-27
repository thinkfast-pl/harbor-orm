// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.metadata;

import java.util.List;

public record ElementCollectionMetadata(
        String fieldName,
        String typeClassNameForGenericUse,
        String tableName,
        String tableSchemaName,
        List<JoinColumnMetadata> joinColumns,
        ColumnMetadata columnMetadata,
        EmbeddedMetadata embeddedMetadata
) {
}
