// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.expression.JsonExpression;
import lombok.NonNull;

import java.util.Map;

/**
 * JSON-specific column reference that extends {@link QColumn} with JSON query operators.
 *
 * <p>Generated for entity fields annotated with {@code @Json}. Adds PostgreSQL JSON/JSONB operators
 * for extraction, containment, and key existence checks. These operators are PostgreSQL-only;
 * using them on H2 will throw {@code UnsupportedOperationException} at query execution time.
 *
 * <p>Example:
 * <pre>{@code
 * QUserEntity USER = new QUserEntity(null);
 * // USER.payload is a QJsonColumn<String>
 *
 * // Extract text value (PostgreSQL ->> operator)
 * session.selectEntity(USER)
 *     .where(USER.payload.extractText("status").eq("active"))
 *     .fetchAll();
 *
 * // Containment check (PostgreSQL @> operator)
 * session.selectEntity(USER)
 *     .where(USER.payload.contains(DSL.json("{\"role\":\"admin\"}")))
 *     .fetchAll();
 * }</pre>
 *
 * @param <T> the Java type of the column value
 * @see QColumn
 */
public interface QJsonColumn<T> extends QColumn<T> {

    /**
     * Extracts a JSON sub-object by key (PostgreSQL {@code ->} operator).
     *
     * @param key the JSON object key
     * @return a JSON expression representing the extracted value
     */
    default JsonExpression extract(@NonNull String key) {
        return asJson().extract(key);
    }

    /**
     * Extracts a JSON value as text by key (PostgreSQL {@code ->>} operator).
     *
     * @param key the JSON object key
     * @return a string expression representing the extracted text value
     */
    default Expression<String> extractText(@NonNull String key) {
        return asJson().extractText(key);
    }

    /**
     * Extracts a JSON sub-object by path (PostgreSQL {@code #>} operator).
     *
     * @param keys the path elements to traverse
     * @return a JSON expression representing the value at the given path
     */
    default JsonExpression extractPath(@NonNull String... keys) {
        return asJson().extractPath(keys);
    }

    /**
     * Extracts a JSON value as text by path (PostgreSQL {@code #>>} operator).
     *
     * @param keys the path elements to traverse
     * @return a string expression representing the text value at the given path
     */
    default Expression<String> extractPathText(@NonNull String... keys) {
        return asJson().extractPathText(keys);
    }

    /**
     * Tests whether this JSON column contains the given JSON value (PostgreSQL {@code @>} operator).
     *
     * @param other the JSON value to check containment against
     * @return a condition for use in WHERE clauses
     */
    default Condition contains(@NonNull JsonExpression other) {
        return asJson().contains(other);
    }

    /**
     * Tests whether this JSON column is contained within the given JSON value (PostgreSQL {@code <@} operator).
     *
     * @param other the JSON value to check containment within
     * @return a condition for use in WHERE clauses
     */
    default Condition containedIn(@NonNull JsonExpression other) {
        return asJson().containedIn(other);
    }

    /**
     * Tests whether the JSON object has the given top-level key (PostgreSQL {@code ?} operator).
     *
     * @param key the key to check for
     * @return a condition for use in WHERE clauses
     */
    default Condition hasKey(@NonNull String key) {
        return asJson().hasKey(key);
    }

    /**
     * Tests whether the JSON object has any of the given top-level keys (PostgreSQL {@code ?|} operator).
     *
     * @param keys the keys to check for
     * @return a condition for use in WHERE clauses
     */
    default Condition hasAnyKey(@NonNull String... keys) {
        return asJson().hasAnyKey(keys);
    }

    /**
     * Tests whether the JSON object has all of the given top-level keys (PostgreSQL {@code ?&} operator).
     *
     * @param keys the keys to check for
     * @return a condition for use in WHERE clauses
     */
    default Condition hasAllKeys(@NonNull String... keys) {
        return asJson().hasAllKeys(keys);
    }

    /**
     * Returns the length of the top-level JSON array (PostgreSQL {@code json_array_length} function).
     *
     * @return an integer expression representing the array length
     */
    default Expression<Integer> arrayLength() {
        return asJson().arrayLength();
    }

    private JsonExpression asJson() {
        return new JsonExpression(this, JsonExpression.Operator.LITERAL, null, null);
    }

    /**
     * Creates a standard JSON column reference with full configuration.
     *
     * @param <T>                    the Java type of the column
     * @param javaType               the Java class of the column value
     * @param tableAlias             the table alias, or {@code null}
     * @param alias                  the column alias, or {@code null}
     * @param propertyName           the entity property name, or {@code null}
     * @param columnName             the database column name
     * @param id                     whether this is the primary key column
     * @param autoGenerated          whether the value is database-generated
     * @param sequenceGenerated      whether the value is sequence-generated
     * @param sequenceGeneratorData  sequence metadata, or {@code null}
     * @param converterData          converter metadata, or {@code null}
     * @param insertable             whether included in INSERT statements
     * @param updatable              whether included in UPDATE statements
     * @param nullable               whether the column allows NULL
     * @param version                whether this is the optimistic lock version column
     * @param enumMappingType        the enum mapping strategy, or {@code null}
     * @param customTypes            dialect-specific custom type overrides, or {@code null}
     * @param typeHandlerClasses     dialect-specific type handler class overrides, or {@code null}
     * @return a new JSON column reference
     */
    static <T> QJsonColumn<T> regular(
            final Class<T> javaType,
            final String tableAlias,
            final String alias,
            final String propertyName,
            final String columnName,
            final boolean id,
            final boolean autoGenerated,
            final boolean sequenceGenerated,
            final SequenceGeneratorData sequenceGeneratorData,
            final ConverterData converterData,
            final boolean insertable,
            final boolean updatable,
            final boolean nullable,
            final boolean version,
            final EnumMappingType enumMappingType,
            final Map<String, String> customTypes,
            final Map<String, Class<?>> typeHandlerClasses
    ) {
        return new DefaultQJsonColumn<>(
                javaType, tableAlias, alias, propertyName, columnName,
                id, autoGenerated, sequenceGenerated,
                sequenceGeneratorData, converterData,
                insertable, updatable, nullable, version,
                enumMappingType, customTypes, typeHandlerClasses
        );
    }

    /**
     * Creates a JSON column reference with an attached {@link io.github.thinkfastpl.harbororm.api.converter.AttributeConverter}.
     *
     * @param <T>                    the entity-side Java type
     * @param <C>                    the database-side Java type
     * @param convertJavaType        the entity Java class
     * @param dbJavaType             the database Java class
     * @param tableAlias             the table alias, or {@code null}
     * @param alias                  the column alias, or {@code null}
     * @param propertyName           the entity property name, or {@code null}
     * @param columnName             the database column name
     * @param id                     whether this is the primary key column
     * @param autoGenerated          whether the value is database-generated
     * @param sequenceGenerated      whether the value is sequence-generated
     * @param sequenceGeneratorData  sequence metadata, or {@code null}
     * @param converterData          converter metadata
     * @param insertable             whether included in INSERT statements
     * @param updatable              whether included in UPDATE statements
     * @param nullable               whether the column allows NULL
     * @param version                whether this is the optimistic lock version column
     * @param enumMappingType        the enum mapping strategy, or {@code null}
     * @param customTypes            dialect-specific custom type overrides, or {@code null}
     * @param typeHandlerClasses     dialect-specific type handler class overrides, or {@code null}
     * @return a new converted JSON column reference
     */
    static <T, C> QJsonColumn<T> converted(
            final Class<T> convertJavaType,
            final Class<C> dbJavaType,
            final String tableAlias,
            final String alias,
            final String propertyName,
            final String columnName,
            final boolean id,
            final boolean autoGenerated,
            final boolean sequenceGenerated,
            final SequenceGeneratorData sequenceGeneratorData,
            final ConverterData converterData,
            final boolean insertable,
            final boolean updatable,
            final boolean nullable,
            final boolean version,
            final EnumMappingType enumMappingType,
            final Map<String, String> customTypes,
            final Map<String, Class<?>> typeHandlerClasses
    ) {
        return new ConvertedQJsonColumn<>(
                convertJavaType, dbJavaType, tableAlias, alias, propertyName, columnName,
                id, autoGenerated, sequenceGenerated,
                sequenceGeneratorData, converterData,
                insertable, updatable, nullable, version,
                enumMappingType, customTypes, typeHandlerClasses
        );
    }
}
