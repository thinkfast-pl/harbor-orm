// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * An arithmetic expression combining two operands with a binary operator
 * ({@code +}, {@code -}, {@code *}, {@code /}, {@code %}, {@code ^}).
 *
 * @param <T> the Java type of the expression result
 */
@RequiredArgsConstructor
public class BinaryOperatorExpression<T> implements Expression<T> {

    public enum Operator {
        ADDITION,
        SUBTRACTION,
        MULTIPLICATION,
        DIVISION,
        MODULO,
        EXPONENTIATION,
    }

    @Getter
    private final Expression<T> leftExpression;

    @Getter
    private final Expression<T> rightExpression;

    @Getter
    private final Operator operator;
    private final Class<T> javaType;

    @Override
    public Class<T> getJavaType() {
        return javaType;
    }

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return ColumnContext.combineContexts(
                leftExpression.getColumnContext(dialectName),
                rightExpression.getColumnContext(dialectName)
        );
    }
}
