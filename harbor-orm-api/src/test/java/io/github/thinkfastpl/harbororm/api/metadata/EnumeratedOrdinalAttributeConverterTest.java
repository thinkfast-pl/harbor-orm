// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumeratedOrdinalAttributeConverterTest {

    private enum Color { RED, GREEN, BLUE }

    private final EnumeratedOrdinalAttributeConverter<Color> converter =
            new EnumeratedOrdinalAttributeConverter<>(Color.class);

    @Test
    void convertToDatabaseColumn_returnsOrdinal() {
        assertEquals(0, converter.convertToDatabaseColumn(Color.RED));
        assertEquals(1, converter.convertToDatabaseColumn(Color.GREEN));
        assertEquals(2, converter.convertToDatabaseColumn(Color.BLUE));
    }

    @Test
    void convertToDatabaseColumn_nullReturnsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_returnsEnum() {
        assertEquals(Color.RED, converter.convertToEntityAttribute(0));
        assertEquals(Color.GREEN, converter.convertToEntityAttribute(1));
        assertEquals(Color.BLUE, converter.convertToEntityAttribute(2));
    }

    @Test
    void convertToEntityAttribute_nullReturnsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_negativeOrdinalThrowsDescriptiveException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> converter.convertToEntityAttribute(-1));
        assertTrue(ex.getMessage().contains("-1"), "Message should contain the invalid ordinal");
        assertTrue(ex.getMessage().contains("Color"), "Message should contain the enum type name");
    }

    @Test
    void convertToEntityAttribute_tooLargeOrdinalThrowsDescriptiveException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> converter.convertToEntityAttribute(3));
        assertTrue(ex.getMessage().contains("3"), "Message should contain the invalid ordinal");
        assertTrue(ex.getMessage().contains("Color"), "Message should contain the enum type name");
    }

    @Test
    void convertToEntityAttribute_maxIntOrdinalThrowsDescriptiveException() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.convertToEntityAttribute(Integer.MAX_VALUE));
    }
}
