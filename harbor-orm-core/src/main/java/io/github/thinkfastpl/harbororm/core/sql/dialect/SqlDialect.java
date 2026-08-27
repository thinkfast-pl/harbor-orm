// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql.dialect;

import io.github.thinkfastpl.harbororm.api.converter.AttributeConverterSupplier;
import io.github.thinkfastpl.harbororm.api.converter.JsonSerializer;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.expression.Order;
import io.github.thinkfastpl.harbororm.api.expression.SelectExpression;
import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.api.metadata.QTableSource;
import io.github.thinkfastpl.harbororm.api.query.data.DeleteQueryData;
import io.github.thinkfastpl.harbororm.api.query.data.InsertQueryData;
import io.github.thinkfastpl.harbororm.api.query.data.SelectQueryData;
import io.github.thinkfastpl.harbororm.api.query.data.UpdateQueryData;
import lombok.NonNull;

/**
 * Interface for database-specific SQL generation and conversion.
 * <p>
 * A dialect knows how to escape keywords, convert expressions and query data objects
 * (INSERT, SELECT, UPDATE, DELETE) into executable {@link SqlQuery} instances, and
 * extract query data from sub-select expressions.
 *
 * @see AbstractSqlDialect
 * @see SqlQuery
 */
public interface SqlDialect {

    /**
     * Returns the dialect name (e.g. "h2", "postgresql").
     *
     * @return the dialect name
     * @see io.github.thinkfastpl.harbororm.api.dialect.StandardDialects for dialects supported by this library
     */
    String getName();

    /**
     * Initializes the dialect with the given converter supplier and optional JSON serializer.
     *
     * @param attributeConverterSupplier supplier for attribute converters
     * @param jsonSerializer             JSON serializer, may be {@code null}
     */
    void init(@NonNull AttributeConverterSupplier attributeConverterSupplier, JsonSerializer jsonSerializer);

    /**
     * Escapes a SQL keyword or identifier for this database dialect.
     *
     * @param value the keyword or identifier to escape
     * @return the escaped string
     */
    String escapeKeyword(@NonNull String value);

    /**
     * Converts a table source to a SQL fragment.
     *
     * @param tableSource the table source to convert
     * @return a {@link SqlQuery} representing the table source
     */
    SqlQuery toSqlPart(@NonNull QTableSource tableSource);

    /**
     * Converts an expression to a SQL fragment with optional column context.
     *
     * @param expression    the expression to convert
     * @param columnContext  the column context, may be {@code null}
     * @return a {@link SqlQuery} representing the expression
     */
    SqlQuery toSqlPart(@NonNull Expression<?> expression, ColumnContext columnContext);

    /**
     * Converts an order clause to a SQL fragment.
     *
     * @param order the order clause to convert
     * @return a {@link SqlQuery} representing the order clause
     */
    SqlQuery toSqlPart(@NonNull Order order);

    /**
     * Converts insert query data to a full SQL query.
     *
     * @param data the insert query data
     * @return the executable {@link SqlQuery}
     */
    SqlQuery toQuery(@NonNull InsertQueryData data);

    /**
     * Converts select query data to a full SQL query.
     *
     * @param data the select query data
     * @return the executable {@link SqlQuery}
     */
    SqlQuery toQuery(@NonNull SelectQueryData data);

    /**
     * Converts update query data to a full SQL query.
     *
     * @param data the update query data
     * @return the executable {@link SqlQuery}
     */
    SqlQuery toQuery(@NonNull UpdateQueryData data);

    /**
     * Converts delete query data to a full SQL query.
     *
     * @param data the delete query data
     * @return the executable {@link SqlQuery}
     */
    SqlQuery toQuery(@NonNull DeleteQueryData data);

    /**
     * Extracts {@link SelectQueryData} from a sub-select expression.
     *
     * @param selectExpression the sub-select expression
     * @return the extracted select query data
     */
    SelectQueryData extractQueryData(SelectExpression<?> selectExpression);
}
