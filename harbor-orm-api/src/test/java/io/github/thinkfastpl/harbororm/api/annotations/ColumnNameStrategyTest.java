// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColumnNameStrategyTest {

    @Test
    void camelCasePreservesFieldName() {
        assertEquals("firstName", ColumnNameStrategy.CAMEL_CASE.apply("firstName"));
        assertEquals("id", ColumnNameStrategy.CAMEL_CASE.apply("id"));
        assertEquals("HTMLParser", ColumnNameStrategy.CAMEL_CASE.apply("HTMLParser"));
        assertEquals("userID", ColumnNameStrategy.CAMEL_CASE.apply("userID"));
    }

    @Test
    void pascalCaseCapitalizesFirst() {
        assertEquals("FirstName", ColumnNameStrategy.PASCAL_CASE.apply("firstName"));
        assertEquals("Id", ColumnNameStrategy.PASCAL_CASE.apply("id"));
        assertEquals("HTMLParser", ColumnNameStrategy.PASCAL_CASE.apply("HTMLParser"));
        assertEquals("UserID", ColumnNameStrategy.PASCAL_CASE.apply("userID"));
    }

    @Test
    void snakeCaseConverts() {
        assertEquals("first_name", ColumnNameStrategy.SNAKE_CASE.apply("firstName"));
        assertEquals("id", ColumnNameStrategy.SNAKE_CASE.apply("id"));
        assertEquals("html_parser", ColumnNameStrategy.SNAKE_CASE.apply("HTMLParser"));
        assertEquals("user_id", ColumnNameStrategy.SNAKE_CASE.apply("userID"));
        assertEquals("bool_as_string", ColumnNameStrategy.SNAKE_CASE.apply("boolAsString"));
        assertEquals("phone_number", ColumnNameStrategy.SNAKE_CASE.apply("phoneNumber"));
        assertEquals("varchar_value", ColumnNameStrategy.SNAKE_CASE.apply("varcharValue"));
    }

    @Test
    void snakeCaseUpperConverts() {
        assertEquals("FIRST_NAME", ColumnNameStrategy.SNAKE_CASE_UPPER.apply("firstName"));
        assertEquals("ID", ColumnNameStrategy.SNAKE_CASE_UPPER.apply("id"));
        assertEquals("HTML_PARSER", ColumnNameStrategy.SNAKE_CASE_UPPER.apply("HTMLParser"));
        assertEquals("USER_ID", ColumnNameStrategy.SNAKE_CASE_UPPER.apply("userID"));
    }

    @Test
    void kebabCaseConverts() {
        assertEquals("first-name", ColumnNameStrategy.KEBAB_CASE.apply("firstName"));
        assertEquals("id", ColumnNameStrategy.KEBAB_CASE.apply("id"));
        assertEquals("html-parser", ColumnNameStrategy.KEBAB_CASE.apply("HTMLParser"));
        assertEquals("user-id", ColumnNameStrategy.KEBAB_CASE.apply("userID"));
    }

    @Test
    void kebabCaseUpperConverts() {
        assertEquals("FIRST-NAME", ColumnNameStrategy.KEBAB_CASE_UPPER.apply("firstName"));
        assertEquals("ID", ColumnNameStrategy.KEBAB_CASE_UPPER.apply("id"));
        assertEquals("HTML-PARSER", ColumnNameStrategy.KEBAB_CASE_UPPER.apply("HTMLParser"));
        assertEquals("USER-ID", ColumnNameStrategy.KEBAB_CASE_UPPER.apply("userID"));
    }

    @Test
    void emptyInput() {
        for (ColumnNameStrategy strategy : ColumnNameStrategy.values()) {
            assertEquals("", strategy.apply(""));
        }
    }
}
