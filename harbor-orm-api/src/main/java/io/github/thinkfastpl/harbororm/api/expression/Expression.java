// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;

/**
 * Core interface for all typed SQL expressions.
 * Provides default methods for comparison operators, arithmetic, null checks, range predicates,
 * casting, ordering, and window function support.
 *
 * @param <T> the Java type that this expression evaluates to
 */
public interface Expression<T> extends SelectableExpression<T> {

    ColumnContext getColumnContext(@NonNull String dialectName);

    default String getAlias() {
        return null;
    }

    default Expression<T> as(String alias) {
        return new AliasedExpression<>(this, alias);
    }

    default String getResultColumnLabel() {
        return null;
    }

    default Order asc() {
        return order(true);
    }

    default Order desc() {
        return order(false);
    }

    default Order order(boolean asc) {
        return new Order(this, asc);
    }

    default <C> Expression<C> cast(@NonNull Class<C> type) {
        if (getJavaType() == type) {
            @SuppressWarnings("unchecked") Expression<C> castExpression = (Expression<C>) this;
            return castExpression;
        } else {
            return new CastExpression<>(this, type);
        }
    }

    default Expression<T> add(@NonNull Expression<T> other) {
        return new BinaryOperatorExpression<>(this, other, BinaryOperatorExpression.Operator.ADDITION, getJavaType());
    }

    default Expression<T> add(@NonNull T other) {
        return add(new ConstantExpression<>(getJavaType(), other));
    }

    default Expression<T> subtract(@NonNull Expression<T> other) {
        return new BinaryOperatorExpression<>(this, other, BinaryOperatorExpression.Operator.SUBTRACTION, getJavaType());
    }

    default Expression<T> subtract(@NonNull T constant) {
        return subtract(new ConstantExpression<>(getJavaType(), constant));
    }

    default Expression<T> multiply(@NonNull Expression<T> other) {
        return new BinaryOperatorExpression<>(this, other, BinaryOperatorExpression.Operator.MULTIPLICATION, getJavaType());
    }

    default Expression<T> multiply(@NonNull T constant) {
        return multiply(new ConstantExpression<>(getJavaType(), constant));
    }

    default Expression<T> divide(@NonNull Expression<T> other) {
        return new BinaryOperatorExpression<>(this, other, BinaryOperatorExpression.Operator.DIVISION, getJavaType());
    }

    default Expression<T> divide(@NonNull T constant) {
        return divide(new ConstantExpression<>(getJavaType(), constant));
    }

    default Expression<T> modulo(@NonNull Expression<T> other) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.MOD, List.of(this, other), getJavaType());
    }

    default Expression<T> modulo(@NonNull T constant) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.MOD, List.of(this, new ConstantExpression<>(getJavaType(), constant)), getJavaType());
    }

    default Expression<T> exponentiate(@NonNull Expression<T> other) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.POWER, List.of(this, other), getJavaType());
    }

    default Expression<T> exponentiate(@NonNull T constant) {
        return exponentiate(new ConstantExpression<>(getJavaType(), constant));
    }

    default WindowExpression<T> over() {
        return new DefaultWindowExpression<>(this, getJavaType());
    }

    default Condition isNull() {
        return new UnaryOperatorCondition(this, UnaryOperatorCondition.Operator.IS_NULL);
    }

    default Condition isNotNull() {
        return new UnaryOperatorCondition(this, UnaryOperatorCondition.Operator.IS_NOT_NULL);
    }

    default Condition isTrue() {
        return new UnaryOperatorCondition(this, UnaryOperatorCondition.Operator.IS_TRUE);
    }

    default Condition isNotTrue() {
        return new UnaryOperatorCondition(this, UnaryOperatorCondition.Operator.IS_NOT_TRUE);
    }

    default Condition isFalse() {
        return new UnaryOperatorCondition(this, UnaryOperatorCondition.Operator.IS_FALSE);
    }

    default Condition isNotFalse() {
        return new UnaryOperatorCondition(this, UnaryOperatorCondition.Operator.IS_NOT_FALSE);
    }

    default Condition isUnknown() {
        return new UnaryOperatorCondition(this, UnaryOperatorCondition.Operator.IS_UNKNOWN);
    }

    default Condition isNotUnknown() {
        return new UnaryOperatorCondition(this, UnaryOperatorCondition.Operator.IS_NOT_UNKNOWN);
    }

    default Condition isDistinctFrom(@NonNull Expression<T> expression) {
        return new BinaryOperatorCondition(this, expression, BinaryOperatorCondition.Operator.IS_DISTINCT_FROM);
    }

    default Condition isDistinctFrom(T value) {
        return isDistinctFrom(new ConstantExpression<>(getJavaType(), value));
    }

    default Condition isNotDistinctFrom(@NonNull Expression<T> expression) {
        return new BinaryOperatorCondition(this, expression, BinaryOperatorCondition.Operator.IS_NOT_DISTINCT_FROM);
    }

    default Condition isNotDistinctFrom(T value) {
        return isNotDistinctFrom(new ConstantExpression<>(getJavaType(), value));
    }

    default Condition eq(T value) {
        return value == null ? isNull() : eq(new ConstantExpression<>(getJavaType(), value));
    }

    default Condition eq(Expression<T> value) {
        return new BinaryOperatorCondition(this, value, BinaryOperatorCondition.Operator.EQ);
    }

    default Condition eqIgnoreCase(@NonNull String text) {
        return eqIgnoreCase(DSL.constant(text));
    }

    default Condition eqIgnoreCase(@NonNull Expression<String> value) {
        return new BinaryOperatorCondition(DSL.lower(this.cast(String.class)), DSL.lower(value), BinaryOperatorCondition.Operator.EQ);
    }

    default Condition containsIgnoreCase(@NonNull String text) {
        return containsIgnoreCase(DSL.constant(text));
    }

    default Condition containsIgnoreCase(@NonNull Expression<String> value) {
        return DSL.containsIgnoreCase(this.cast(String.class), value);
    }

    default Condition notEq(T value) {
        return value == null ? isNotNull() : notEq(new ConstantExpression<>(getJavaType(), value));
    }

    default Condition notEq(Expression<T> value) {
        return new BinaryOperatorCondition(this, value, BinaryOperatorCondition.Operator.NOT_EQ);
    }

    default Condition le(@NonNull T value) {
        return le(new ConstantExpression<>(getJavaType(), value));
    }

    default Condition le(Expression<T> value) {
        return new BinaryOperatorCondition(this, value, BinaryOperatorCondition.Operator.LE);
    }

    default Condition lt(@NonNull T value) {
        return lt(new ConstantExpression<>(getJavaType(), value));
    }

    default Condition lt(Expression<T> value) {
        return new BinaryOperatorCondition(this, value, BinaryOperatorCondition.Operator.LT);
    }

    default Condition ge(@NonNull T value) {
        return ge(new ConstantExpression<>(getJavaType(), value));
    }

    default Condition ge(Expression<T> value) {
        return new BinaryOperatorCondition(this, value, BinaryOperatorCondition.Operator.GE);
    }

    default Condition gt(@NonNull T value) {
        return gt(new ConstantExpression<>(getJavaType(), value));
    }

    default Condition gt(Expression<T> value) {
        return new BinaryOperatorCondition(this, value, BinaryOperatorCondition.Operator.GT);
    }

    default Condition in(Collection<T> values) {
        if (values == null || values.isEmpty()) {
            return new BooleanConstantExpression(Boolean.FALSE);
        }
        return in(new ConstantsExpression<>(getJavaType(), values));
    }

    default Condition in(Expression<T> expression) {
        return new BinaryOperatorCondition(this, expression, BinaryOperatorCondition.Operator.IN);
    }

    default Condition between(Expression<T> from, Expression<T> to) {
        return new TernaryOperatorCondition(this, from, to, TernaryOperatorCondition.Operator.BETWEEN);
    }

    default Condition between(T from, T to) {
        return between(new ConstantExpression<>(getJavaType(), from), new ConstantExpression<>(getJavaType(), to));
    }

    default Condition between(Expression<T> from, T to) {
        return between(from, new ConstantExpression<>(getJavaType(), to));
    }

    default Condition between(T from, Expression<T> to) {
        return between(new ConstantExpression<>(getJavaType(), from), to);
    }

    default Condition betweenSymmetric(Expression<T> from, Expression<T> to) {
        return new TernaryOperatorCondition(this, from, to, TernaryOperatorCondition.Operator.BETWEEN_SYMMETRIC);
    }

    default Condition betweenSymmetric(T from, T to) {
        return betweenSymmetric(new ConstantExpression<>(getJavaType(), from), new ConstantExpression<>(getJavaType(), to));
    }

    default Condition betweenSymmetric(Expression<T> from, T to) {
        return betweenSymmetric(from, new ConstantExpression<>(getJavaType(), to));
    }

    default Condition betweenSymmetric(T from, Expression<T> to) {
        return betweenSymmetric(new ConstantExpression<>(getJavaType(), from), to);
    }

    default Condition notBetween(Expression<T> from, Expression<T> to) {
        return new TernaryOperatorCondition(this, from, to, TernaryOperatorCondition.Operator.NOT_BETWEEN);
    }

    default Condition notBetween(T from, T to) {
        return notBetween(new ConstantExpression<>(getJavaType(), from), new ConstantExpression<>(getJavaType(), to));
    }

    default Condition notBetween(Expression<T> from, T to) {
        return notBetween(from, new ConstantExpression<>(getJavaType(), to));
    }

    default Condition notBetween(T from, Expression<T> to) {
        return notBetween(new ConstantExpression<>(getJavaType(), from), to);
    }

    default Condition notBetweenSymmetric(Expression<T> from, Expression<T> to) {
        return new TernaryOperatorCondition(this, from, to, TernaryOperatorCondition.Operator.NOT_BETWEEN_SYMMETRIC);
    }

    default Condition notBetweenSymmetric(T from, T to) {
        return notBetweenSymmetric(new ConstantExpression<>(getJavaType(), from), new ConstantExpression<>(getJavaType(), to));
    }

    default Condition notBetweenSymmetric(Expression<T> from, T to) {
        return notBetweenSymmetric(from, new ConstantExpression<>(getJavaType(), to));
    }

    default Condition notBetweenSymmetric(T from, Expression<T> to) {
        return notBetweenSymmetric(new ConstantExpression<>(getJavaType(), from), to);
    }
}
