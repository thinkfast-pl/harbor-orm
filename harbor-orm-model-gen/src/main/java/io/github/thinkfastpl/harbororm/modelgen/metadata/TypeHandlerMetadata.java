// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.metadata;

import java.util.Map;

/**
 * Compile-time metadata for {@code @TypeHandler} annotations.
 *
 * @param handlersByDialect map of dialect name to handler fully-qualified class name
 */
public record TypeHandlerMetadata(
        Map<String, String> handlersByDialect
) {
}
