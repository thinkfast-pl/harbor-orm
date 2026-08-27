// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.expression.EmbeddableEqCondition;
import io.github.thinkfastpl.harbororm.api.expression.EmbeddableInCondition;

import java.util.Collection;

/**
 * Metadata for an {@code @Embeddable} value object embedded within an entity.
 *
 * <p>Generated {@code Q<EmbeddableName>} classes implement this interface. An embeddable acts as
 * both an attribute of its parent entity and a holder of its own child attributes (columns).
 * This supports composite value objects such as {@code Address}, {@code Money}, etc.
 *
 * <p>When used as a composite primary key (via {@code @Id @Embedded}), the {@link #eq} and {@link #in}
 * methods produce multi-column equality conditions.
 *
 * <p>Embeddables also support lifecycle callbacks ({@code @PreInsert}, {@code @PreUpdate}, etc.)
 * through the {@link QAttributeHolder} interface, which are invoked recursively by the runtime.
 *
 * @param <T> the Java type of the embeddable class
 * @see QAttributeHolder
 * @see QComparableAttribute
 */
public interface QEmbeddable<T> extends QAttribute, QComparableAttribute<T>, QAttributeHolder {

    /**
     * {@inheritDoc}
     */
    String getPropertyName();

    /**
     * {@inheritDoc}
     */
    Class<T> getJavaType();

    /**
     * Creates a multi-column equality condition comparing all fields of the embeddable.
     *
     * @param value the embeddable value to compare against
     * @return a condition for use in WHERE clauses
     */
    @Override
    default Condition eq(T value) {
        return new EmbeddableEqCondition(this, value);
    }

    /**
     * Creates a multi-column IN condition matching any of the given embeddable values.
     *
     * @param values the collection of embeddable values to match against
     * @return a condition for use in WHERE clauses
     */
    @Override
    default Condition in(Collection<T> values) {
        return new EmbeddableInCondition(this, values);
    }
}
