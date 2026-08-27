// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql.dialect;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.NonNull;

/**
 * Extension point for dialect-specific expressions defined outside of core.
 * <p>
 * Implementations live in dialect modules (e.g. PostgreSQL tsvector expressions) and render
 * themselves to SQL. {@link EmptySqlDialect#toSqlPart(io.github.thinkfastpl.harbororm.api.expression.Expression, ColumnContext)}
 * dispatches to this interface after all built-in expression types, guarding that the
 * expression's declared dialect matches the rendering dialect.
 */
public interface DialectSpecificExpression {

    /**
     * The dialect this expression is specific to.
     *
     * @return a dialect name constant from {@link io.github.thinkfastpl.harbororm.api.dialect.StandardDialects}
     */
    String getDialectName();

    /**
     * Renders this expression to SQL.
     *
     * @param dialect       the rendering dialect (for sub-expression rendering)
     * @param columnContext optional column context propagated from the surrounding query
     * @return the rendered SQL part
     */
    SqlQuery toSqlPart(@NonNull EmptySqlDialect dialect, ColumnContext columnContext);
}
