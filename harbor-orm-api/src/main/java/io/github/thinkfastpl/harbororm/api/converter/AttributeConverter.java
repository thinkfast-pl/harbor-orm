// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.converter;

/**
 * Defines a custom mapping between a Java entity field type and a database column type.
 * Used with the {@link io.github.thinkfastpl.harbororm.api.annotations.Convert @Convert} annotation to apply
 * the conversion transparently during insert, update, and select operations.
 *
 * <h3>Implementation guidelines</h3>
 * <ul>
 *   <li>Both conversion methods must handle {@code null} values explicitly.</li>
 *   <li>Implementations must have a public no-argument constructor (unless a custom
 *       {@link AttributeConverterSupplier} is configured).</li>
 *   <li>Each converter class is instantiated once and reused across all operations --
 *       implementations should be stateless.</li>
 *   <li>The same converter can be applied to multiple entity fields.</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>{@code
 * public class BooleanToYesNoConverter implements AttributeConverter<Boolean, String> {
 *
 *     @Override
 *     public String convertToDatabaseColumn(Boolean attribute) {
 *         if (attribute == null) return null;
 *         return attribute ? "Y" : "N";
 *     }
 *
 *     @Override
 *     public Boolean convertToEntityAttribute(String dbData) {
 *         if (dbData == null) return null;
 *         return "Y".equals(dbData);
 *     }
 * }
 * }</pre>
 *
 * @param <ENT> the Java entity field type
 * @param <DB>  the database column type
 * @see io.github.thinkfastpl.harbororm.api.annotations.Convert
 * @see AttributeConverterSupplier
 */
public interface AttributeConverter<ENT, DB> {

    /**
     * Converts the given entity attribute value to its database column representation.
     *
     * @param attribute the entity attribute value to convert (may be {@code null})
     * @return the corresponding database column value, or {@code null}
     */
    DB convertToDatabaseColumn(ENT attribute);

    /**
     * Converts the given database column value to its Java entity attribute representation.
     *
     * @param attribute the database column value to convert (may be {@code null})
     * @return the corresponding entity attribute value, or {@code null}
     */
    ENT convertToEntityAttribute(DB attribute);
}
