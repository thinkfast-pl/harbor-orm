// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.metadata;

public record ConverterMetadata(
        String converterTypeName,
        String convEntityTypeNameForGenericUse,
        String convDbTypeNameForGenericUse
) {
}
