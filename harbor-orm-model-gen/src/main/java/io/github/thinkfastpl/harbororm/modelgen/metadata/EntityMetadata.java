// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.metadata;

import io.github.thinkfastpl.harbororm.modelgen.source.SourceClassField;
import lombok.NonNull;

import java.util.List;

public record EntityMetadata(
        @NonNull String packageName,
        @NonNull String className,
        boolean publicClass,
        @NonNull String tableName,
        String schemaName,
        @NonNull List<String> imports,
        @NonNull List<ColumnMetadata> columns,
        @NonNull List<EmbeddedMetadata> embeddedAttributes,
        @NonNull List<ElementCollectionMetadata> elementCollections,
        @NonNull List<EntityRelationMetadata> entityRelations,
        @NonNull List<ManyToManyMetadata> manyToManyRelations,
        @NonNull LifecycleMethodNames lifecycle,
        SourceClassField idField
) {
    public String qClassName() {
        return "Q" + className;
    }

    public String qClassFullName() {
        return packageName + "." + qClassName();
    }
}
