// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a {@code MULTISET} aggregate expression that collects rows into an array of records.
 * Supports an optional filter clause.
 */
@RequiredArgsConstructor
public class MultisetAggExpression implements Expression<Record[]> {

    @NonNull
    @Getter
    private final List<Expression<?>> expressions;

    @Getter
    private final List<Condition> filter = new ArrayList<>();

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }

    @Override
    public Class<Record[]> getJavaType() {
        return Record[].class;
    }
}
