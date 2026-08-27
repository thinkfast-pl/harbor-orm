// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.metadata;

import lombok.NonNull;

import java.util.List;

/**
 * Compile-time metadata extracted from a class annotated with
 * {@link io.github.thinkfastpl.harbororm.api.annotations.View @View}.
 *
 * <p>Holds the information the annotation processor needs to generate
 * the {@code Q<ClassName>} metadata class (see {@link io.github.thinkfastpl.harbororm.modelgen.writer.MetadataWriter}).
 *
 * @param packageName the package of the annotated view class
 * @param className   the simple name of the annotated view class
 * @param publicClass whether the annotated view class is declared {@code public}
 * @param viewName    the SQL view name from {@code @View(name = ...)}
 * @param schemaName  the optional SQL schema, or {@code null} for the default schema
 * @param columns     column metadata for each {@code @Column(nullable = false)}-annotated field
 */
public record ViewMetadata(
        @NonNull String packageName,
        @NonNull String className,
        boolean publicClass,
        @NonNull String viewName,
        String schemaName,
        @NonNull List<ColumnMetadata> columns
) {
    /** Returns the generated metadata class name, e.g. {@code "QUserReport"}. */
    public String qClassName() {
        return "Q" + className;
    }

    /** Returns the fully qualified generated metadata class name. */
    public String qClassFullName() {
        return packageName + "." + qClassName();
    }
}
