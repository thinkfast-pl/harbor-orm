// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import java.util.List;

/**
 * Compile-time generated metadata for a class annotated with
 * {@link io.github.thinkfastpl.harbororm.api.annotations.View @View}.
 *
 * <p>Each {@code @View} class produces a {@code Q<ClassName>} class that implements
 * this interface. It exposes typed {@link QColumn} fields and the view's table name,
 * enabling type-safe, read-only queries via
 * {@link io.github.thinkfastpl.harbororm.api.HarborSession#select(QView) session.select(QView)}.
 *
 * <p>Unlike {@link QEntity}, a {@code QView} has no ID column, no lifecycle callbacks,
 * and no relationship metadata — it is purely a column-to-field mapping for SELECT queries.
 *
 * @param <T> the view class type
 * @see io.github.thinkfastpl.harbororm.api.annotations.View
 * @see io.github.thinkfastpl.harbororm.api.query.ViewQuery
 */
public interface QView<T> {

    /**
     * Returns the qualified view name (view name, optional schema, optional alias).
     *
     * @return the view's table name descriptor
     */
    QTableName getTableName();

    /**
     * Returns the Java class that view rows are mapped to.
     *
     * @return the view bean type
     */
    Class<T> getBeanType();

    /**
     * Returns all column definitions declared on the view class.
     *
     * @return an immutable list of column metadata
     */
    List<QColumn<?>> getAllColumns();
}
