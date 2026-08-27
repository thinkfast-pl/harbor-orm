// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain.converter;

import io.github.thinkfastpl.harbororm.api.converter.AttributeConverter;
import io.github.thinkfastpl.harbororm.test.domain.JsonMetadata;

/**
 * Converts JsonMetadata object to JSON string for database storage.
 * This is a simple JSON conversion without using external libraries.
 */
public class JsonConverter implements AttributeConverter<JsonMetadata, String> {

    @Override
    public String convertToDatabaseColumn(JsonMetadata attribute) {
        if (attribute == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"key\":").append(attribute.getKey() == null ? "null" : "\"" + SimpleJsonHelper.escapeJson(attribute.getKey()) + "\"");
        sb.append(",");
        sb.append("\"value\":").append(attribute.getValue() == null ? "null" : "\"" + SimpleJsonHelper.escapeJson(attribute.getValue()) + "\"");
        sb.append(",");
        sb.append("\"count\":").append(attribute.getCount());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public JsonMetadata convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        String key = SimpleJsonHelper.extractStringValue(dbData, "key");
        String value = SimpleJsonHelper.extractStringValue(dbData, "value");
        int count = SimpleJsonHelper.extractIntValue(dbData, "count");
        return new JsonMetadata(key, value, count);
    }
}
