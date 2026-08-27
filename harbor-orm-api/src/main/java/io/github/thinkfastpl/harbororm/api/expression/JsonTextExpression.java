// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Result of a JSON text extraction (->> or #>>). Returns String type.
 */
@RequiredArgsConstructor
@Getter
public class JsonTextExpression implements Expression<String> {

    @NonNull
    private final Expression<?> source;

    @NonNull
    private final JsonExpression.Operator operator;

    @NonNull
    private final String[] args;

    @Override
    public Class<String> getJavaType() {
        return String.class;
    }

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }
}
