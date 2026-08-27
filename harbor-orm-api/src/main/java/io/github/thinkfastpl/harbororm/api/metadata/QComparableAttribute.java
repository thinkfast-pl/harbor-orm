// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import io.github.thinkfastpl.harbororm.api.expression.Condition;

import java.util.Collection;

/**
 * An entity attribute that supports equality comparison and IN-list filtering.
 *
 * <p>This interface extends {@link QAttribute} with comparison capabilities, enabling
 * the attribute to be used in {@code WHERE} clauses. {@link QColumn} extends this interface
 * and adds the full expression API (ordering, between, null checks, etc.). {@link QEmbeddable}
 * also extends this interface to support composite-key equality.
 *
 * @param <T> the Java type of the attribute
 * @see QColumn
 * @see QEmbeddable
 */
public interface QComparableAttribute<T> extends QAttribute {

    /**
     * Returns the Java property name on the entity class that this attribute corresponds to.
     *
     * @return the entity field name
     */
    String getPropertyName();

    /**
     * Returns the Java type of this attribute.
     *
     * @return the attribute Java class
     */
    Class<T> getJavaType();

    /**
     * Creates an equality condition: {@code column = value}.
     *
     * @param value the value to compare against
     * @return a condition for use in WHERE clauses
     */
    Condition eq(T value);

    /**
     * Creates an IN condition: {@code column IN (values)}.
     *
     * @param values the collection of values to match against
     * @return a condition for use in WHERE clauses
     */
    Condition in(Collection<T> values);
}
