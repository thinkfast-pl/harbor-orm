// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.metadata;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.ColumnNameStrategy;
import io.github.thinkfastpl.harbororm.modelgen.source.SourceTypeConstruct;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record EmbeddedMetadata(
        String fieldName,
        String tableFieldNamePrefix,
        @NonNull SourceTypeConstruct type,
        @NonNull EmbeddableMetadata embeddableMetadata,
        Map<String, Column> attributeOverrides,
        @NonNull ColumnNameStrategy columnNameStrategy
) {
    public String getQClassNameInPackage(@NonNull String packageName) {
        if (packageName.equals(type.getPackageName())) {
            return "Q" + type.getSimpleClassName();
        } else {
            return type.getPackageName() + ".Q" + type.getSimpleClassName();
        }
    }

    public List<ColumnMetadata> getColumnsForQTable() {
        List<ColumnMetadata> result = new ArrayList<>(
                embeddableMetadata.columns().stream()
                        .map(c -> c.withFieldNamePrefix(tableFieldNamePrefix))
                        .toList()
        );
        for (EmbeddedMetadata nested : embeddableMetadata.embeddedAttributes()) {
            result.addAll(nested.getColumnsForQTable().stream()
                    .map(c -> c.withFieldNamePrefix(tableFieldNamePrefix))
                    .toList());
        }
        return result;
    }
}
