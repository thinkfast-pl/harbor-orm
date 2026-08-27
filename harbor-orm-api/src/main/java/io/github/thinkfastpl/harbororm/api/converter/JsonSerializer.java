// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.converter;

import lombok.NonNull;

/**
 * Enables automatic serialization and deserialization for entity fields annotated with
 * {@link io.github.thinkfastpl.harbororm.api.annotations.Json @Json} whose type is a POJO (not a plain {@code String}).
 *
 * <p>A {@code JsonSerializer} is provided when creating the {@link io.github.thinkfastpl.harbororm.api.HarborSession}
 * via {@code HarborSessionFactory.builder().jsonSerializer(jsonSerializer).build()}.
 * It is not needed when all {@code @Json} fields are of type {@code String} or when
 * {@link io.github.thinkfastpl.harbororm.api.annotations.Convert @Convert} is used alongside {@code @Json} for
 * manual conversion.
 *
 * <h3>Example -- Jackson implementation</h3>
 * <pre>{@code
 * JsonSerializer jacksonSerializer = new JsonSerializer() {
 *     private final ObjectMapper mapper = new ObjectMapper();
 *
 *     @Override
 *     public String serialize(Object value) {
 *         return mapper.writeValueAsString(value);
 *     }
 *
 *     @Override
 *     public <T> T deserialize(String json, Class<T> type) {
 *         return mapper.readValue(json, type);
 *     }
 * };
 * }</pre>
 *
 * @see io.github.thinkfastpl.harbororm.api.annotations.Json
 */
public interface JsonSerializer {

    /**
     * Serializes the given object to a JSON string for storage in the database.
     *
     * @param object the object to serialize; never {@code null}
     * @return the JSON string representation
     */
    String serialize(@NonNull Object object);

    /**
     * Deserializes the given JSON string to an instance of the specified type.
     *
     * @param json the JSON string read from the database; never {@code null}
     * @param type the target class to deserialize into; never {@code null}
     * @param <T>  the target type
     * @return the deserialized object
     */
    <T> T deserialize(@NonNull String json, @NonNull Class<T> type);
}
