// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import lombok.Value;
import lombok.With;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable value class encapsulating dialect-specific column context: custom database type,
 * converter metadata, and JSON flag.
 *
 * <p>Returned by {@code QColumn.getColumnContext(dialectName)} to provide the runtime with the
 * information needed to correctly bind parameters and read results for a given column on a
 * specific database dialect.
 *
 * @see QColumn
 * @see ConverterData
 */
@Value
public class ColumnContext {

    /** The dialect-specific custom database type (e.g., a PostgreSQL enum type name), or {@code null}. */
    String customType;

    /** The converter metadata for this column, or {@code null} if no converter is attached. */
    ConverterData converterData;

    /** Whether this column stores JSON/JSONB data. */
    @With
    boolean json;

    /** The resolved type handler class for this column, or {@code null}. */
    Class<?> typeHandlerClass;

    /**
     * Creates a column context with the JSON flag set to {@code false} and no type handler.
     *
     * @param customType    the custom database type, or {@code null}
     * @param converterData the converter metadata, or {@code null}
     */
    public ColumnContext(String customType, ConverterData converterData) {
        this(customType, converterData, false, null);
    }

    /**
     * Creates a column context with the JSON flag specified and no type handler.
     *
     * @param customType    the custom database type, or {@code null}
     * @param converterData the converter metadata, or {@code null}
     * @param json          whether the column stores JSON data
     */
    public ColumnContext(String customType, ConverterData converterData, boolean json) {
        this(customType, converterData, json, null);
    }

    /**
     * Creates a column context with all fields specified.
     *
     * @param customType       the custom database type, or {@code null}
     * @param converterData    the converter metadata, or {@code null}
     * @param json             whether the column stores JSON data
     * @param typeHandlerClass the type handler class, or {@code null}
     */
    public ColumnContext(String customType, ConverterData converterData, boolean json, Class<?> typeHandlerClass) {
        this.customType = customType;
        this.converterData = converterData;
        this.json = json;
        this.typeHandlerClass = typeHandlerClass;
    }

    /**
     * Combines two column contexts, returning the non-null one if only one is present.
     *
     * @param context1 the first context, or {@code null}
     * @param context2 the second context, or {@code null}
     * @return the combined context, or {@code null} if both are {@code null}
     * @throws IllegalStateException if both contexts are non-null and not equal
     */
    public static ColumnContext combineContexts(ColumnContext context1, ColumnContext context2) {
        if (context1 == null && context2 == null) {
            return null;
        } else if (context1 == null) {
            return context2;
        } else if (context2 == null) {
            return context1;
        } else if (Objects.equals(context1, context2)) {
            return context1;
        } else {
            throw new IllegalStateException("Multiple column contexts in use: " + Arrays.toString(new ColumnContext[]{context1, context2}));
        }
    }

    /**
     * Combines multiple column contexts, ensuring at most one distinct context exists among them.
     *
     * @param contexts the contexts to combine (may contain {@code null} entries)
     * @return the combined context, or {@code null} if all are {@code null}
     * @throws IllegalStateException if multiple distinct non-null contexts are found
     */
    public static ColumnContext combineContexts(ColumnContext... contexts) {
        if (contexts == null || contexts.length == 0) {
            return null;
        } else if (contexts.length == 1) {
            return contexts[0];
        } else {
            ColumnContext data = combineContexts(contexts[0], contexts[1]);
            for (int i = 2; i < contexts.length; i++) {
                data = combineContexts(data, contexts[i]);
            }
            return data;
        }
    }
}
