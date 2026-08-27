// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.metadata;

import io.github.thinkfastpl.harbororm.api.metadata.QEntityRelation;
import io.github.thinkfastpl.harbororm.modelgen.source.SourceRefType;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Value
public class EntityRelationMetadata {

    @NonNull
    String fieldName;

    @NonNull
    SourceRefType entityType;

    @NonNull
    SourceRefType entityIdType;

    @NonNull
    QEntityRelation.Type relationType;

    @NonNull
    List<JoinColumnMetadata> joinColumns;

    public String getQClassNameInPackage(@NonNull String packageName) {
        if (packageName.equals(entityType.getPackageName())) {
            return "Q" + entityType.getSimpleName();
        } else {
            return entityType.getPackageName() + ".Q" + entityType.getSimpleName();
        }
    }
}
