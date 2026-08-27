// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PortableAggregateExpression<T> implements Expression<T> {

    public enum Function {
        ANY_VALUE,
        ARRAY_AGG,
        AVG,
        BIT_AND_AGG,
        BIT_NAND_AGG,
        BIT_NOR_AGG,
        BIT_OR_AGG,
        BIT_XOR_AGG,
        BIT_XNOR_AGG,
        BOOL_AND,
        BOOL_OR,
        COUNT,
        EVERY,
        GROUP_CONCAT,
        JSON_ARRAY_AGG,
        JSON_OBJECT_AGG,
        MIN,
        MAX,
        STDDEV_POP,
        STDDEV_SAMP,
        SUM,
        VAR_POP,
        VAR_SAMP,
    }

    @NonNull
    private final Class<T> clazz;

    @NonNull
    @Getter
    private final AggregateQuantifier quantifier;

    @NonNull
    @Getter
    private final Function function;

    @NonNull
    @Getter
    private final List<Expression<?>> expressions;

    @Getter
    private final List<Order> orders = new ArrayList<>();

    @Getter
    private final List<Condition> filter = new ArrayList<>();

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }

    @Override
    public Class<T> getJavaType() {
        return clazz;
    }
}
