// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
class CaseBuilderImpl<T> implements CaseBuilder<T> {

    @NonNull
    private final Class<T> javaType;

    private final List<CaseWhenThenExpression.WhenThen<T>> whenThens = new ArrayList<>();

    @Override
    public CaseBuilder<T> whenThen(@NonNull Condition condition, Expression<T> then) {
        this.whenThens.add(new CaseWhenThenExpression.WhenThen<>(condition, then));
        return this;
    }

    @Override
    public Expression<T> else_(Expression<T> else_) {
        return new CaseWhenThenExpression<>(new ArrayList<>(whenThens), else_, javaType);
    }

    @Override
    public Expression<T> end() {
        return new CaseWhenThenExpression<>(new ArrayList<>(whenThens), null, javaType);
    }
}
