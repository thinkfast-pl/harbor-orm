// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.utils;

import lombok.NonNull;

import java.util.function.Consumer;

/**
 * String utilities for blank-checking and delimiter-joined iteration into a {@link StringBuilder}.
 */
public class HarborStringUtils {

    /**
     * Returns {@code true} if the string is non-null and not blank (contains at least one
     * non-whitespace character).
     *
     * @param value the string to check
     * @return {@code true} if the string is non-null and not blank, {@code false} otherwise
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Iterates over the items, invoking the consumer for each element (which should append
     * to the provided {@link StringBuilder}), and inserts the delimiter between consecutive items.
     *
     * @param sb the string builder to append to
     * @param delimiter the delimiter to insert between items
     * @param iterable the items to iterate over
     * @param consumer the consumer that appends each item's representation to the string builder
     * @param <T> the element type
     */
    public static <T> void iterateAppendingJoiningDelimiter(@NonNull StringBuilder sb, @NonNull String delimiter, @NonNull Iterable<T> iterable, @NonNull Consumer<T> consumer) {
        boolean first = true;
        for (T t : iterable) {
            if (first) {
                first = false;
            } else {
                sb.append(delimiter);
            }
            consumer.accept(t);
        }
    }
}
