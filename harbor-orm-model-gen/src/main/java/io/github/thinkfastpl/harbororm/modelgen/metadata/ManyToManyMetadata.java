// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.metadata;

import io.github.thinkfastpl.harbororm.modelgen.source.SourceRefType;
import lombok.NonNull;

import java.util.List;

public record ManyToManyMetadata(
        @NonNull String fieldName,
        @NonNull String tableName,
        String tableSchemaName,
        @NonNull List<JoinColumnMetadata> joinColumns,
        @NonNull List<JoinColumnMetadata> inverseJoinColumns,
        SourceRefType relatedEntityType,
        SourceRefType relatedEntityIdType,
        SourceRefType elementEmbeddableType,
        boolean idOnly
) {
    public String getQClassNameInPackage(@NonNull String packageName) {
        return qClassNameInPackage(relatedEntityType, packageName);
    }

    public String getElementEmbeddableQClassNameInPackage(@NonNull String packageName) {
        return qClassNameInPackage(elementEmbeddableType, packageName);
    }

    private static String qClassNameInPackage(SourceRefType type, String packageName) {
        if (type == null) {
            return null;
        }
        if (packageName.equals(type.getPackageName())) {
            return "Q" + type.getSimpleName();
        } else {
            return type.getPackageName() + ".Q" + type.getSimpleName();
        }
    }
}
