// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.metadata;

import io.github.thinkfastpl.harbororm.modelgen.source.SourceTypeConstruct;
import lombok.NonNull;

import java.util.List;

public record EmbeddableMetadata(
        @NonNull SourceTypeConstruct type,
        @NonNull List<ColumnMetadata> columns,
        @NonNull List<EmbeddedMetadata> embeddedAttributes,
        @NonNull List<ElementCollectionMetadata> elementCollections,
        @NonNull LifecycleMethodNames lifecycle
) {
    public String qClassName() {
        return "Q" + type.getSimpleClassName();
    }

    public String qClassFullName() {
        return type.getPackageName() + "." + qClassName();
    }
}
