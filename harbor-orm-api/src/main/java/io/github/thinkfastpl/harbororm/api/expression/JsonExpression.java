// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Represents JSON/JSONB operations including extraction ({@code ->}, {@code ->>}),
 * path extraction ({@code #>}, {@code #>>}), containment ({@code @>}, {@code <@}),
 * and key existence ({@code ?}, {@code ?|}, {@code ?&}).
 * Supports method chaining for nested JSON navigation.
 */
@RequiredArgsConstructor
@Getter
public class JsonExpression implements Expression<Object> {

    public enum Operator {
        // Extraction
        EXTRACT,            // -> (returns JSON)
        EXTRACT_TEXT,       // ->> (returns text)
        EXTRACT_PATH,       // #> (returns JSON)
        EXTRACT_PATH_TEXT,  // #>> (returns text)
        // Containment
        CONTAINS,           // @>
        CONTAINED_IN,       // <@
        // Key existence
        HAS_KEY,            // ?
        HAS_ANY_KEY,        // ?|
        HAS_ALL_KEYS,       // ?&
        // Literal
        LITERAL,            // JSON literal value (for DSL.json("..."))
    }

    @NonNull
    private final Expression<?> source;

    @NonNull
    private final Operator operator;

    // For single-key ops: the key string. For path ops: the path segments. For literal: the JSON string.
    private final String[] args;

    // For binary JSON ops (contains, containedIn): the other JSON expression
    private final Expression<?> other;

    // --- Chainable extraction ---

    public JsonExpression extract(@NonNull String key) {
        return new JsonExpression(this, Operator.EXTRACT, new String[]{key}, null);
    }

    public Expression<String> extractText(@NonNull String key) {
        return new JsonTextExpression(this, Operator.EXTRACT_TEXT, new String[]{key});
    }

    public JsonExpression extractPath(@NonNull String... keys) {
        return new JsonExpression(this, Operator.EXTRACT_PATH, keys, null);
    }

    public Expression<String> extractPathText(@NonNull String... keys) {
        return new JsonTextExpression(this, Operator.EXTRACT_PATH_TEXT, keys);
    }

    // --- Containment ---

    public Condition contains(@NonNull JsonExpression other) {
        return new JsonCondition(this, Operator.CONTAINS, null, other);
    }

    public Condition containedIn(@NonNull JsonExpression other) {
        return new JsonCondition(this, Operator.CONTAINED_IN, null, other);
    }

    // --- Key existence ---

    public Condition hasKey(@NonNull String key) {
        return new JsonCondition(this, Operator.HAS_KEY, new String[]{key}, null);
    }

    public Condition hasAnyKey(@NonNull String... keys) {
        return new JsonCondition(this, Operator.HAS_ANY_KEY, keys, null);
    }

    public Condition hasAllKeys(@NonNull String... keys) {
        return new JsonCondition(this, Operator.HAS_ALL_KEYS, keys, null);
    }

    // --- Array ---

    public Expression<Integer> arrayLength() {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.JSON_ARRAY_LENGTH, List.of(this), Integer.class);
    }

    // --- Expression interface ---

    @Override
    public Class<Object> getJavaType() {
        return Object.class;
    }

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return source.getColumnContext(dialectName);
    }
}
