// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StringUtils {

    public static boolean isNotBlank(String str) {
        return str != null && !str.isBlank();
    }

    public static String ucFirst(String str) {
        if (str == null) {
            return null;
        }

        if (str.isEmpty()) {
            return str;
        }

        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String lcFirst(String str) {
        if (str == null) {
            return null;
        }

        if (str.isEmpty()) {
            return str;
        }

        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    public static String toPascalCase(String str) {
        if (str == null) {
            return null;
        }

        String[] words = str.trim().split("(\\s+|_+)");

        return Arrays.stream(words)
                .map(StringUtils::ucFirst)
                .collect(Collectors.joining(""));
    }

    public static String toCamelCase(String str) {
        if (str == null) {
            return null;
        }

        String[] words = str.trim().split("(\\s+|_+)");

        StringBuilder sb = new StringBuilder();
        if (words.length > 0) {
            sb.append(lcFirst(words[0]));
        }
        for (int i = 1; i < words.length; i++) {
            sb.append(ucFirst(words[i]));
        }
        return sb.toString();
    }

    public static String escapeQuotationMarks(String str) {
        if (str == null) {
            return null;
        }

        return str.replace("\"", "\\\"");
    }
}
