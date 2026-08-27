// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines strategies for converting Java field names to database column names.
 *
 * <p>The strategy is specified at the entity level via {@link Entity#columnNameStrategy()}
 * and applies to all fields that don't have an explicit {@link Column#name()}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * // With SNAKE_CASE (default), field "firstName" maps to column "first_name"
 * @Entity(table = "users", columnNameStrategy = ColumnNameStrategy.SNAKE_CASE)
 * public class UserEntity {
 *     private String firstName;  // -> first_name
 * }
 * }</pre>
 *
 * @see Entity#columnNameStrategy()
 */
public enum ColumnNameStrategy {

    /**
     * Preserves camelCase: {@code firstName} → {@code firstName}
     */
    CAMEL_CASE {
        @Override
        public String apply(String fieldName) {
            if (fieldName == null || fieldName.isEmpty()) {
                return fieldName;
            }
            return fieldName;
        }
    },

    /**
     * Converts to PascalCase: {@code firstName} → {@code FirstName}
     */
    PASCAL_CASE {
        @Override
        public String apply(String fieldName) {
            if (fieldName == null || fieldName.isEmpty()) {
                return fieldName;
            }
            return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        }
    },

    /**
     * Converts to lowercase snake_case: {@code firstName} → {@code first_name}
     */
    SNAKE_CASE {
        @Override
        public String apply(String fieldName) {
            return joinWords(splitCamelCase(fieldName), "_", false);
        }
    },

    /**
     * Converts to uppercase snake_case: {@code firstName} → {@code FIRST_NAME}
     */
    SNAKE_CASE_UPPER {
        @Override
        public String apply(String fieldName) {
            return joinWords(splitCamelCase(fieldName), "_", true);
        }
    },

    /**
     * Converts to lowercase kebab-case: {@code firstName} → {@code first-name}
     */
    KEBAB_CASE {
        @Override
        public String apply(String fieldName) {
            return joinWords(splitCamelCase(fieldName), "-", false);
        }
    },

    /**
     * Converts to uppercase kebab-case: {@code firstName} → {@code FIRST-NAME}
     */
    KEBAB_CASE_UPPER {
        @Override
        public String apply(String fieldName) {
            return joinWords(splitCamelCase(fieldName), "-", true);
        }
    };

    /**
     * Converts a Java field name to a database column name according to this strategy.
     *
     * @param fieldName the Java field name (typically camelCase)
     * @return the converted column name
     */
    public abstract String apply(String fieldName);

    /**
     * Splits a camelCase string into words.
     * <p>Handles transitions like:
     * <ul>
     *   <li>{@code firstName} → [first, Name]</li>
     *   <li>{@code userID} → [user, ID]</li>
     *   <li>{@code HTMLParser} → [HTML, Parser]</li>
     *   <li>{@code simple} → [simple]</li>
     * </ul>
     */
    static List<String> splitCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return List.of();
        }

        List<String> words = new ArrayList<>();
        int wordStart = 0;

        for (int i = 1; i < input.length(); i++) {
            char current = input.charAt(i);
            char previous = input.charAt(i - 1);

            // Transition: lowercase → uppercase (e.g., "firstName" at 'N')
            if (Character.isLowerCase(previous) && Character.isUpperCase(current)) {
                words.add(input.substring(wordStart, i));
                wordStart = i;
            }
            // Transition: uppercase → lowercase with preceding uppercase run
            // (e.g., "HTMLParser" at 'a' — split before 'P')
            else if (i > 1 && Character.isUpperCase(previous) && Character.isLowerCase(current)
                    && Character.isUpperCase(input.charAt(i - 2))) {
                words.add(input.substring(wordStart, i - 1));
                wordStart = i - 1;
            }
        }

        words.add(input.substring(wordStart));
        return words;
    }

    private static String joinWords(List<String> words, String separator, boolean uppercase) {
        if (words.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(uppercase ? words.get(i).toUpperCase() : words.get(i).toLowerCase());
        }
        return sb.toString();
    }
}
