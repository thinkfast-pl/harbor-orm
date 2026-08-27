// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.metadata;

import io.github.thinkfastpl.harbororm.modelgen.utils.StringUtils;
import lombok.NonNull;

public record JoinColumnMetadata(
        String name,
        String typeClassNameForGenericUse,
        String referencedColumnName,
        boolean insertable,
        boolean updatable,
        boolean nullable
) {

    public ColumnMetadata toColumnMetadata(@NonNull ColumnMetadata referencedColumnMetadata) {
        return new ColumnMetadata(
                StringUtils.toCamelCase(name),
                name,
                false,
                false,
                null,
                referencedColumnMetadata.converterMetadata(),
                insertable,
                updatable,
                nullable,
                referencedColumnMetadata.type(),
                referencedColumnMetadata.enumMappingType(),
                referencedColumnMetadata.typesMetadata(),
                false,
                false,
                referencedColumnMetadata.typeHandlerMetadata(),
                true
        );
    }

    public ColumnMetadata toSimpleColumnMetadata() {
        return new ColumnMetadata(
                StringUtils.toCamelCase(name),
                name,
                false,
                false,
                null,
                null,
                insertable,
                updatable,
                nullable,
                new SimpleSourceRefType(typeClassNameForGenericUse),
                null,
                null,
                false,
                false,
                null,
                true
        );
    }
}
