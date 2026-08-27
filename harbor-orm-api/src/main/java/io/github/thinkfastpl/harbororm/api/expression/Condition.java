// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.NonNull;

/**
 * Base interface for boolean SQL conditions.
 * Provides default implementations for logical operators {@code AND}, {@code OR}, and {@code NOT}.
 */
public interface Condition extends Expression<Boolean> {

    @Override
    default Class<Boolean> getJavaType() {
        return Boolean.class;
    }

    default Condition and(Condition other) {
        return new ComplexCondition(this, ComplexCondition.Operator.AND, other);
    }

    default Condition and(boolean and) {
        return and(new BooleanConstantExpression(and));
    }

    default Condition or(Condition other) {
        return new ComplexCondition(this, ComplexCondition.Operator.OR, other);
    }

    default Condition or(boolean or) {
        return or(new BooleanConstantExpression(or));
    }

    default Condition not() {
        return new UnaryOperatorCondition(this, UnaryOperatorCondition.Operator.NOT);
    }

    @Override
    default ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }
}
