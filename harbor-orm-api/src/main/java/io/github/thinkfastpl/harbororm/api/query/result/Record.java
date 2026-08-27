// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.query.result;

import io.github.thinkfastpl.harbororm.api.expression.Expression;
import lombok.NonNull;

/**
 * A single row returned from a SQL query.
 *
 * <p>Provides type-safe access to column values by {@link Expression} reference, by
 * positional index (1-based), or by column label. Records are produced by
 * {@link io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor} and consumed by application code
 * or mapped to entities internally.
 */
public interface Record {

    /**
     * Returns the value of the column identified by the given expression.
     *
     * @param expression the expression that was selected
     * @param <T>        the expression's Java type
     * @return the column value, or {@code null} if the database value is NULL
     */
    <T> T get(Expression<T> expression);

    /**
     * Returns the value of the column at the given 1-based position.
     *
     * @param column the 1-based column index
     * @return the column value, or {@code null} if the database value is NULL
     */
    Object get(int column);

    /**
     * Returns the value of the column with the given label.
     *
     * @param columnLabel the column label (as defined by the SQL AS alias or the column name)
     * @return the column value, or {@code null} if the database value is NULL
     */
    Object get(String columnLabel);

    /**
     * Returns the value of the column at the given 1-based position, cast to the specified type.
     *
     * @param column the 1-based column index
     * @param clazz  the target Java type
     * @param <T>    the target type
     * @return the column value cast to {@code T}, or {@code null} if the database value is NULL
     */
    <T> T get(int column, Class<T> clazz);

    /**
     * Returns the value of the column with the given label, cast to the specified type.
     *
     * @param columnLabel the column label
     * @param clazz       the target Java type
     * @param <T>         the target type
     * @return the column value cast to {@code T}, or {@code null} if the database value is NULL
     */
    <T> T get(String columnLabel, Class<T> clazz);

    /**
     * Returns the number of columns in this record.
     *
     * @return the column count
     */
    int columnsCount();

    /**
     * Returns all column values as an {@code Object} array, in column order (1-based to N).
     *
     * @return an array containing all column values
     */
    default Object[] getAsArray() {
        final int count = columnsCount();
        final Object[] array = new Object[count];
        for (int i = 0; i < count; i++) {
            array[i] = get(i + 1);
        }
        return array;
    }

    /**
     * Maps this record to an instance of the given class by matching column labels to
     * constructor parameters or setter methods.
     *
     * @param clazz the target class
     * @param <C>   the target type
     * @return a new instance of {@code C} populated from this record's values
     */
    <C> C getAsInstanceOf(@NonNull Class<C> clazz);
}
