// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain.converter;

import io.github.thinkfastpl.harbororm.api.converter.AttributeConverter;
import io.github.thinkfastpl.harbororm.test.domain.ProfilePojo;

public class ProfileJsonConverter implements AttributeConverter<ProfilePojo, String> {

    @Override
    public String convertToDatabaseColumn(ProfilePojo attribute) {
        if (attribute == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"name\":").append(attribute.getName() == null ? "null" : "\"" + SimpleJsonHelper.escapeJson(attribute.getName()) + "\"");
        sb.append(",");
        sb.append("\"age\":").append(attribute.getAge());
        sb.append(",");
        sb.append("\"city\":").append(attribute.getCity() == null ? "null" : "\"" + SimpleJsonHelper.escapeJson(attribute.getCity()) + "\"");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public ProfilePojo convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        String name = SimpleJsonHelper.extractStringValue(dbData, "name");
        int age = SimpleJsonHelper.extractIntValue(dbData, "age");
        String city = SimpleJsonHelper.extractStringValue(dbData, "city");
        return new ProfilePojo(name, age, city);
    }
}
