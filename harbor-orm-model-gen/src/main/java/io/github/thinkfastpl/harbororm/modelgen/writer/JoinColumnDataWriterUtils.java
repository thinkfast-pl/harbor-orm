// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.writer;

import io.github.thinkfastpl.harbororm.modelgen.metadata.JoinColumnMetadata;
import io.github.thinkfastpl.harbororm.modelgen.utils.StringUtils;
import lombok.NonNull;

class JoinColumnDataWriterUtils {

    static String generateNewObject(@NonNull JoinColumnMetadata metadata) {
        return "new JoinColumnData(\"%s\", %s.class, %s, %s, %s, %s)".formatted(
                metadata.name().replace("\\", "\\\\").replace("\"", "\\\""),
                metadata.typeClassNameForGenericUse(),
                StringUtils.isNotBlank(metadata.referencedColumnName()) ? "\"" + metadata.referencedColumnName().replace("\\", "\\\\").replace("\"", "\\\"") + "\"" : "null",
                metadata.insertable(),
                metadata.updatable(),
                metadata.nullable()
        );
    }
}
