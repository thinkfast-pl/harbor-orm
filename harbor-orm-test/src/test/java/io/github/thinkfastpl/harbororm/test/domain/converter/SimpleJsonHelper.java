// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain.converter;

/**
 * Shared JSON parsing utilities for test converters.
 */
public final class SimpleJsonHelper {

    private SimpleJsonHelper() {
    }

    public static String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String extractStringValue(String json, String field) {
        String searchPattern = "\"" + field + "\":";
        int start = json.indexOf(searchPattern);
        if (start == -1) {
            return null;
        }
        start += searchPattern.length();

        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        if (start >= json.length()) {
            return null;
        }

        if (json.substring(start).startsWith("null")) {
            return null;
        }

        if (json.charAt(start) != '"') {
            return null;
        }
        start++;

        StringBuilder result = new StringBuilder();
        boolean escaped = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                switch (c) {
                    case 'n': result.append('\n'); break;
                    case 'r': result.append('\r'); break;
                    case 't': result.append('\t'); break;
                    default: result.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static int extractIntValue(String json, String field) {
        String searchPattern = "\"" + field + "\":";
        int start = json.indexOf(searchPattern);
        if (start == -1) {
            return 0;
        }
        start += searchPattern.length();

        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        StringBuilder numStr = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isDigit(c) || c == '-') {
                numStr.append(c);
            } else {
                break;
            }
        }

        if (numStr.length() == 0) {
            return 0;
        }
        return Integer.parseInt(numStr.toString());
    }
}
