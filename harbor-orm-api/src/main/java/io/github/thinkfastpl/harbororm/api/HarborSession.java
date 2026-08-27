// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api;

import io.github.thinkfastpl.harbororm.api.expression.CommonTableExpression;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.expression.FunctionCallTableSource;
import io.github.thinkfastpl.harbororm.api.lob.PortableBlob;
import io.github.thinkfastpl.harbororm.api.lob.PortableClob;
import io.github.thinkfastpl.harbororm.api.metadata.QEntity;
import io.github.thinkfastpl.harbororm.api.metadata.QTable;
import io.github.thinkfastpl.harbororm.api.metadata.QTableName;
import io.github.thinkfastpl.harbororm.api.metadata.QView;
import io.github.thinkfastpl.harbororm.api.query.*;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Main entry point for all database operations in HarborORM.
 *
 * <p>{@code HarborSession} provides a unified API for both entity-level operations (insert, update,
 * delete, select entities) and raw SQL operations (insert, update, delete, select using table
 * metadata). It also supports BLOB/CLOB creation, batch operations, Common Table Expressions (CTEs),
 * and stored procedure/function calls.
 *
 * <p>HarborSession is typically created via {@code HarborSessionFactory} and configured as a
 * singleton (e.g., a Spring bean). It uses a {@code SqlConnectionManager} for connection lifecycle
 * management and an {@code RdbmsSupport} implementation for database-specific SQL generation.
 *
 * <p>Key design principles:
 * <ul>
 *   <li>No level-1 or level-2 caching -- operations execute exactly as instructed</li>
 *   <li>No transaction management -- use an external transaction manager (e.g., Spring)</li>
 *   <li>Thread-safe when backed by a connection manager that provides per-thread connections</li>
 * </ul>
 *
 * <h2>Usage examples</h2>
 *
 * <p>Entity operations:
 * <pre>{@code
 * QSomeEntity ENTITY = new QSomeEntity(null);
 *
 * session.insertEntity(ENTITY, new SomeEntity(1L, "Alice"));
 *
 * List<SomeEntity> results = session.selectEntity(ENTITY)
 *     .where(ENTITY.name.eq("Alice"))
 *     .fetchAll();
 * }</pre>
 *
 * <p>Raw SQL operations:
 * <pre>{@code
 * SomesTable TABLE = new SomesTable(null);
 *
 * session.insertInto(TABLE)
 *     .set(TABLE.id, 1L)
 *     .set(TABLE.name, "Alice")
 *     .execute();
 *
 * List<Record> records = session.select(TABLE.id, TABLE.name)
 *     .from(TABLE)
 *     .where(TABLE.name.eq("Alice"))
 *     .fetchAll();
 * }</pre>
 *
 * @see io.github.thinkfastpl.harbororm.api.query.InsertQuery
 * @see io.github.thinkfastpl.harbororm.api.query.SelectQuery
 * @see io.github.thinkfastpl.harbororm.api.query.UpdateQuery
 * @see io.github.thinkfastpl.harbororm.api.query.DeleteQuery
 * @see io.github.thinkfastpl.harbororm.api.query.EntityQuery
 * @see io.github.thinkfastpl.harbororm.api.metadata.QEntity
 */
public interface HarborSession {

    /**
     * Starts building an INSERT statement using the fluent API.
     *
     * <p>Example:
     * <pre>{@code
     * session.insert()
     *     .into(TABLE)
     *     .set(TABLE.name, "Alice")
     *     .set(TABLE.age, 30)
     *     .execute();
     * }</pre>
     *
     * @return the INTO step of the insert query builder
     * @see InsertQuery
     */
    InsertQuery.IntoStep insert();

    /**
     * Convenience method that starts an INSERT statement targeting the specified table.
     * Delegates to {@code insert().into(table)}.
     *
     * @param table the target table name
     * @return the SET step of the insert query builder
     * @see #insert()
     */
    default InsertQuery.SetStep insertInto(@NonNull QTableName table) {
        return insert().into(table);
    }

    /**
     * Convenience method that starts an INSERT statement targeting the specified table.
     * Delegates to {@code insertInto(table.getTableName())}.
     *
     * @param table the target table metadata
     * @return the SET step of the insert query builder
     * @see #insert()
     */
    default InsertQuery.SetStep insertInto(@NonNull QTable table) {
        return insertInto(table.getTableName());
    }

    /**
     * Inserts a single entity into the database.
     *
     * <p>The entity's {@code @PreInsert} callbacks are invoked before the SQL statement executes,
     * and {@code @PostInsert} callbacks are invoked after the entity and all cascaded children
     * have been persisted. Auto-generated or sequence-generated IDs are populated on the entity
     * after insertion. Child entities annotated with {@code @OneToMany}, {@code @OneToOne},
     * {@code @ManyToMany}, or {@code @ElementCollection} are cascaded automatically.
     *
     * @param <T>     the entity type
     * @param <ID>    the entity's primary key type
     * @param qEntity the entity metadata (generated {@code Q<EntityName>} class)
     * @param entity  the entity instance to insert
     */
    <T, ID> void insertEntity(@NonNull QEntity<T, ID> qEntity, @NonNull T entity);

    /**
     * Inserts multiple entities in a batch operation using the default batch size.
     *
     * <p>Batch insertion is more efficient than inserting entities one by one because it
     * groups multiple INSERT statements into fewer database round-trips.
     *
     * @param <T>      the entity type
     * @param <ID>     the entity's primary key type
     * @param qEntity  the entity metadata
     * @param entities the collection of entities to insert
     * @see #insertEntityBatch(QEntity, Collection, int)
     */
    <T, ID> void insertEntityBatch(@NonNull QEntity<T, ID> qEntity, @NonNull Collection<T> entities);

    /**
     * Inserts multiple entities in a batch operation with the specified batch size.
     *
     * <p>Entities are grouped into batches of the given size. Each batch is sent to the
     * database in a single round-trip.
     *
     * @param <T>       the entity type
     * @param <ID>      the entity's primary key type
     * @param qEntity   the entity metadata
     * @param entities  the collection of entities to insert
     * @param batchSize the number of entities per batch
     */
    <T, ID> void insertEntityBatch(@NonNull QEntity<T, ID> qEntity, @NonNull Collection<T> entities, int batchSize);

    /**
     * Starts a SELECT query preceded by one or more Common Table Expressions (CTEs).
     *
     * <p>CTEs allow you to define named temporary result sets that can be referenced in the
     * subsequent SELECT statement. Use the {@code recursive} parameter to enable recursive CTEs.
     *
     * <p>Example:
     * <pre>{@code
     * CommonTableExpression cte = DSL.cte("recent_orders")
     *     .as(session.select(TABLE.id, TABLE.total)
     *         .from(TABLE)
     *         .where(TABLE.createdAt.gt(cutoffDate)));
     *
     * session.with(List.of(cte), false)
     *     .select(cte.column("id"), cte.column("total"))
     *     .from(cte)
     *     .fetchAll();
     * }</pre>
     *
     * @param commonTableExpressions the list of CTEs to prepend to the query
     * @param recursive              whether to use {@code WITH RECURSIVE} syntax
     * @return the WITH step of the select query builder
     * @see io.github.thinkfastpl.harbororm.api.expression.CommonTableExpression
     */
    SelectQuery.WithStep<Record> with(@NonNull List<CommonTableExpression> commonTableExpressions, boolean recursive);

    /**
     * Starts a non-recursive CTE query. Convenience overload that delegates to
     * {@code with(commonTableExpressions, false)}.
     *
     * @param commonTableExpressions the list of CTEs
     * @return the WITH step of the select query builder
     * @see #with(List, boolean)
     */
    default SelectQuery.WithStep<Record> with(@NonNull List<CommonTableExpression> commonTableExpressions) {
        return with(commonTableExpressions, false);
    }

    /**
     * Starts a non-recursive CTE query with a single CTE. Convenience overload that delegates to
     * {@code with(List.of(cte), false)}.
     *
     * @param cte the Common Table Expression
     * @return the WITH step of the select query builder
     * @see #with(List, boolean)
     */
    default SelectQuery.WithStep<Record> with(@NonNull CommonTableExpression cte) {
        return with(List.of(cte), false);
    }

    /**
     * Starts a non-recursive CTE query with multiple CTEs. Convenience overload that delegates to
     * {@code with(Arrays.asList(ctes), false)}.
     *
     * @param ctes the Common Table Expressions
     * @return the WITH step of the select query builder
     * @see #with(List, boolean)
     */
    default SelectQuery.WithStep<Record> with(@NonNull CommonTableExpression... ctes) {
        return with(Arrays.asList(ctes), false);
    }

    /**
     * Starts a recursive CTE query. Convenience overload that delegates to
     * {@code with(commonTableExpressions, true)}.
     *
     * @param commonTableExpressions the list of CTEs
     * @return the WITH step of the select query builder
     * @see #with(List, boolean)
     */
    default SelectQuery.WithStep<Record> withRecursive(@NonNull List<CommonTableExpression> commonTableExpressions) {
        return with(commonTableExpressions, true);
    }

    /**
     * Starts a recursive CTE query with a single CTE. Convenience overload that delegates to
     * {@code with(List.of(cte), true)}.
     *
     * @param cte the Common Table Expression
     * @return the WITH step of the select query builder
     * @see #with(List, boolean)
     */
    default SelectQuery.WithStep<Record> withRecursive(@NonNull CommonTableExpression cte) {
        return with(List.of(cte), true);
    }

    /**
     * Starts a recursive CTE query with multiple CTEs. Convenience overload that delegates to
     * {@code with(Arrays.asList(ctes), true)}.
     *
     * @param ctes the Common Table Expressions
     * @return the WITH step of the select query builder
     * @see #with(List, boolean)
     */
    default SelectQuery.WithStep<Record> withRecursive(@NonNull CommonTableExpression... ctes) {
        return with(Arrays.asList(ctes), true);
    }

    /**
     * Starts building a SELECT query with the specified column expressions.
     *
     * <p>When multiple expressions are selected, results are returned as {@link Record} objects.
     *
     * <p>Example:
     * <pre>{@code
     * List<Record> records = session.select(TABLE.id, TABLE.name)
     *     .from(TABLE)
     *     .where(TABLE.name.eq("Alice"))
     *     .fetchAll();
     * }</pre>
     *
     * @param expressions the column expressions to select
     * @return the SELECT step of the query builder
     * @see SelectQuery
     */
    SelectQuery.SelectStep<Record> select(@NonNull List<? extends Expression<?>> expressions);

    /**
     * Convenience varargs overload of {@link #select(List)}. Delegates to
     * {@code select(Arrays.asList(expressions))}.
     *
     * @param expressions the column expressions to select
     * @return the SELECT step of the query builder
     * @see #select(List)
     */
    default SelectQuery.SelectStep<Record> select(Expression<?>... expressions) {
        return select(Arrays.asList(expressions));
    }

    /**
     * Starts building a single-column SELECT query.
     *
     * <p>When a single expression is selected, results are returned as the expression's Java type
     * directly, rather than wrapped in {@link Record}.
     *
     * @param <T>        the Java type of the selected expression
     * @param expression the single column expression to select
     * @return the SELECT step of the query builder
     */
    <T> SelectQuery.SelectStep<T> select(@NonNull Expression<T> expression);

    /**
     * Starts building a SELECT DISTINCT query with the specified column expressions.
     *
     * <p>Duplicate rows are eliminated from the result set.
     *
     * @param expressions the column expressions to select
     * @return the SELECT step of the query builder
     */
    SelectQuery.SelectStep<Record> selectDistinct(@NonNull List<Expression<?>> expressions);

    /**
     * Convenience varargs overload of {@link #selectDistinct(List)}. Delegates to
     * {@code selectDistinct(Arrays.asList(expressions))}.
     *
     * @param expressions the column expressions to select
     * @return the SELECT step of the query builder
     * @see #selectDistinct(List)
     */
    default SelectQuery.SelectStep<Record> selectDistinct(Expression<?>... expressions) {
        return selectDistinct(Arrays.asList(expressions));
    }

    /**
     * Starts building a single-column SELECT DISTINCT query.
     *
     * <p>Duplicate values are eliminated from the result set.
     *
     * @param <T>        the Java type of the selected expression
     * @param expression the single column expression to select
     * @return the SELECT step of the query builder
     */
    <T> SelectQuery.SelectStep<T> selectDistinct(@NonNull Expression<T> expression);

    /**
     * Starts building a type-safe entity SELECT query.
     *
     * <p>Unlike {@link #select(List)}, this method returns fully mapped entity instances
     * with all relationships (lazy-loaded), element collections, and type conversions applied.
     *
     * <p>Example:
     * <pre>{@code
     * QSomeEntity ENTITY = new QSomeEntity(null);
     * List<SomeEntity> results = session.selectEntity(ENTITY)
     *     .where(ENTITY.name.eq("Alice"))
     *     .orderBy(ENTITY.id.asc())
     *     .fetchAll();
     * }</pre>
     *
     * @param <T>     the entity type
     * @param <ID>    the entity's primary key type
     * @param qEntity the entity metadata (generated {@code Q<EntityName>} class)
     * @return the entity query builder
     * @see EntityQuery
     */
    <T, ID> EntityQuery<T, ID> selectEntity(@NonNull QEntity<T, ID> qEntity);

    /**
     * Starts building a type-safe view SELECT query.
     *
     * <p>Returns fully mapped view instances with type conversions applied.
     * Views are read-only — no insert, update, or delete operations are supported.
     *
     * <p>Example:
     * <pre>{@code
     * QUserReport VIEW = new QUserReport(null);
     * List<UserReport> reports = session.select(VIEW)
     *     .where(VIEW.totalSpent.gt(new BigDecimal("1000")))
     *     .orderBy(VIEW.userName.asc())
     *     .fetchAll();
     * }</pre>
     *
     * @param <T>   the view class type
     * @param qView the view metadata (generated {@code Q<ClassName>} class)
     * @return the view query builder
     * @see ViewQuery
     */
    <T> ViewQuery<T> select(@NonNull QView<T> qView);

    /**
     * Starts building a type-safe SELECT query for a table-returning stored function.
     *
     * <p>Returns fully mapped instances of the {@code @StoredFunction} class with type
     * conversions applied. The query reuses the {@link ViewQuery} API (read-only).
     *
     * <p>Example:
     * <pre>{@code
     * GetBasicsAboveFunction fn = new GetBasicsAboveFunction("fn");
     * List<GetBasicsAbove> results = session.select(fn.call(15))
     *     .where(fn.numero.gt(20))
     *     .orderBy(fn.numero.asc())
     *     .fetchAll();
     * }</pre>
     *
     * @param <T>          the stored function result class type
     * @param functionCall the function call table source produced by a generated {@code call()} method
     * @return the view query builder
     * @see ViewQuery
     */
    <T> ViewQuery<T> select(@NonNull FunctionCallTableSource<T> functionCall);

    /**
     * Starts building an UPDATE statement using the fluent API.
     *
     * <p>Example:
     * <pre>{@code
     * session.update()
     *     .table(TABLE)
     *     .set(TABLE.name, "Bob")
     *     .where(TABLE.id.eq(1L))
     *     .execute();
     * }</pre>
     *
     * @return the TABLE step of the update query builder
     * @see UpdateQuery
     */
    UpdateQuery.TableStep update();

    /**
     * Convenience method that starts an UPDATE statement targeting the specified table.
     * Delegates to {@code update().table(table)}.
     *
     * @param table the target table name
     * @return the SET step of the update query builder
     * @see #update()
     */
    default UpdateQuery.SetStep update(@NonNull QTableName table) {
        return update().table(table);
    }

    /**
     * Convenience method that starts an UPDATE statement targeting the specified table.
     * Delegates to {@code update(table.getTableName())}.
     *
     * @param table the target table metadata
     * @return the SET step of the update query builder
     * @see #update()
     */
    default UpdateQuery.SetStep update(@NonNull QTable table) {
        return update(table.getTableName());
    }

    /**
     * Updates a single entity in the database.
     *
     * <p>The entity's {@code @PreUpdate} callbacks are invoked before the SQL statement executes,
     * and {@code @PostUpdate} callbacks are invoked after the entity and all cascaded children
     * have been updated. If the entity has a {@code @Version} field, optimistic locking is
     * enforced and the version is incremented atomically. Child entities are cascaded only if
     * their collection or reference was accessed (lazy loading was triggered).
     *
     * @param <T>     the entity type
     * @param <ID>    the entity's primary key type
     * @param qEntity the entity metadata
     * @param entity  the entity instance to update
     * @throws io.github.thinkfastpl.harbororm.api.exception.OptimisticLockException if the entity has a {@code @Version}
     *                                                         field and the row was modified by another transaction
     */
    <T, ID> void updateEntity(@NonNull QEntity<T, ID> qEntity, @NonNull T entity);

    /**
     * Replaces (upserts) a single entity in the database.
     *
     * <p>If the entity exists, it is updated; if it does not exist, it is inserted.
     * Unlike {@link #updateEntity}, this method does not perform {@code @Version} checks
     * (upsert semantics).
     *
     * @param <T>     the entity type
     * @param <ID>    the entity's primary key type
     * @param qEntity the entity metadata
     * @param entity  the entity instance to replace
     */
    <T, ID> void replaceEntity(@NonNull QEntity<T, ID> qEntity, @NonNull T entity);

    /**
     * Starts building a DELETE statement using the fluent API.
     *
     * <p>Example:
     * <pre>{@code
     * session.delete()
     *     .from(TABLE)
     *     .where(TABLE.id.eq(1L))
     *     .execute();
     * }</pre>
     *
     * @return the FROM step of the delete query builder
     * @see DeleteQuery
     */
    DeleteQuery.FromStep delete();

    /**
     * Convenience method that starts a DELETE statement targeting the specified table.
     * Delegates to {@code delete().from(table)}.
     *
     * @param table the target table name
     * @return the WHERE step of the delete query builder
     * @see #delete()
     */
    default DeleteQuery.WhereStep delete(@NonNull QTableName table) {
        return delete().from(table);
    }

    /**
     * Convenience method that starts a DELETE statement targeting the specified table.
     * Delegates to {@code delete(table.getTableName())}.
     *
     * @param table the target table metadata
     * @return the WHERE step of the delete query builder
     * @see #delete()
     */
    default DeleteQuery.WhereStep delete(@NonNull QTable table) {
        return delete(table.getTableName());
    }

    /**
     * Deletes a single entity from the database.
     *
     * <p>The entity's {@code @PreDelete} callback is invoked before deletion, and
     * {@code @PostDelete} is invoked after. Child entities ({@code @OneToMany},
     * {@code @OneToOne}) and element collections are deleted first to satisfy
     * foreign key constraints. If the entity has a {@code @Version} field,
     * optimistic locking is enforced.
     *
     * @param <T>     the entity type
     * @param <ID>    the entity's primary key type
     * @param qEntity the entity metadata
     * @param entity  the entity instance to delete
     * @throws io.github.thinkfastpl.harbororm.api.exception.OptimisticLockException if the entity has a {@code @Version}
     *                                                         field and the row was modified by another transaction
     */
    <T, ID> void deleteEntity(@NonNull QEntity<T, ID> qEntity, @NonNull T entity);

    /**
     * Deletes multiple entities from the database.
     *
     * <p>This is a batch delete operation. Note that batch deletes do not perform
     * {@code @Version} checks even if the entity has a {@code @Version} field.
     *
     * @param <T>      the entity type
     * @param <ID>     the entity's primary key type
     * @param qEntity  the entity metadata
     * @param entities the collection of entities to delete
     */
    <T, ID> void deleteEntityAll(@NonNull QEntity<T, ID> qEntity, @NonNull Collection<T> entities);

    /**
     * Deletes a single entity by its primary key.
     *
     * @param <T>     the entity type
     * @param <ID>    the entity's primary key type
     * @param qEntity the entity metadata
     * @param id      the primary key of the entity to delete
     */
    <T, ID> void deleteEntityById(@NonNull QEntity<T, ID> qEntity, @NonNull ID id);

    /**
     * Deletes multiple entities by their primary keys.
     *
     * @param <T>     the entity type
     * @param <ID>    the entity's primary key type
     * @param qEntity the entity metadata
     * @param ids     the collection of primary keys of entities to delete
     */
    <T, ID> void deleteEntityByIds(@NonNull QEntity<T, ID> qEntity, @NonNull Collection<ID> ids);

    /**
     * Calls a stored procedure with the given parameters.
     *
     * <p>Example:
     * <pre>{@code
     * session.call("refresh_materialized_views");
     * session.call("archive_old_orders", LocalDate.of(2023, 1, 1));
     * }</pre>
     *
     * @param procedureName the name of the stored procedure to call
     * @param params        the parameters to pass to the procedure
     */
    void call(@NonNull String procedureName, Object... params);

    /**
     * Calls a stored function that returns a scalar value.
     *
     * <p>Example:
     * <pre>{@code
     * BigDecimal tax = session.callReturning("calculate_tax", BigDecimal.class, orderId);
     * int count = session.callReturning("count_active_users", Integer.class);
     * }</pre>
     *
     * @param <T>          the return type
     * @param functionName the name of the stored function to call
     * @param returnType   the expected Java return type
     * @param params       the parameters to pass to the function
     * @return the function's return value
     */
    <T> T callReturning(@NonNull String functionName, @NonNull Class<T> returnType, Object... params);

    /**
     * Creates a portable BLOB (Binary Large Object) from the given input stream.
     *
     * <p>The created BLOB can be assigned to entity fields of type {@link PortableBlob}
     * and works across all supported databases. Content is read back via
     * {@link #readBlobData(PortableBlob)} or {@link #readBlobAllBytes(PortableBlob)},
     * and modified via {@link #updateBlob(PortableBlob, InputStream, long)} and
     * {@link #clearBlob(PortableBlob)}.
     *
     * @param inputStream the input stream providing the binary data
     * @param length      the length of the binary data in bytes
     * @return a new {@link PortableBlob} wrapping the binary data
     * @see PortableBlob
     */
    PortableBlob createBlob(@NonNull InputStream inputStream, long length);

    /**
     * Replaces the content of the given BLOB with data read from the input stream.
     *
     * <p>Depending on the database dialect this may allocate a new underlying database
     * object (e.g. a new PostgreSQL Large Object). Call {@link #updateEntity} on the
     * owning entity afterwards to persist the updated reference:
     * <pre>{@code
     * ImageEntity image = session.selectEntity(qImage).whereIdEq(1L).fetchSingle();
     * session.updateBlob(image.getData(), new ByteArrayInputStream(newData), newData.length);
     * session.updateEntity(qImage, image);
     * }</pre>
     *
     * @param blob        the BLOB to update
     * @param inputStream the input stream providing the new binary data
     * @param length      the length of the new binary data in bytes
     */
    void updateBlob(@NonNull PortableBlob blob, @NonNull InputStream inputStream, long length);

    /**
     * Removes the content of the given BLOB, releasing its underlying database
     * resources. Afterwards {@link PortableBlob#isPresent()} returns {@code false}.
     * Call {@link #updateEntity} on the owning entity to persist the cleared state.
     *
     * @param blob the BLOB to clear
     */
    void clearBlob(@NonNull PortableBlob blob);

    /**
     * Opens a stream over the content of the given BLOB.
     *
     * <p>The returned stream must be closed by the caller and consumed while the
     * session's connection (and, on PostgreSQL, the surrounding transaction) is still
     * open. For small payloads prefer {@link #readBlobAllBytes(PortableBlob)}.
     *
     * @param blob the BLOB to read
     * @return a stream over the binary content, or {@link Optional#empty()} if the BLOB is empty
     */
    Optional<InputStream> readBlobData(PortableBlob blob);

    /**
     * Reads the entire content of the given BLOB into a byte array.
     *
     * @param blob the BLOB to read
     * @return the full binary content
     * @throws IOException                      if reading the content fails
     * @throws java.util.NoSuchElementException if the BLOB is empty
     * @see #readBlobData(PortableBlob)
     */
    default byte[] readBlobAllBytes(@NonNull PortableBlob blob) throws IOException {
        try (InputStream inputStream = readBlobData(blob).orElseThrow()) {
            return inputStream.readAllBytes();
        }
    }

    /**
     * Returns the length of the given BLOB's content.
     *
     * @param blob the BLOB to measure
     * @return the content length in bytes, or {@code 0} if the BLOB is empty
     */
    long getBlobLength(PortableBlob blob);

    /**
     * Creates a portable CLOB (Character Large Object) from the given reader.
     *
     * <p>The created CLOB can be assigned to entity fields of type {@link PortableClob}
     * and works across all supported databases. Content is read back via
     * {@link #readClobData(PortableClob)} or {@link #readClobAllChars(PortableClob)},
     * and modified via {@link #updateClob(PortableClob, Reader, long)} and
     * {@link #clearClob(PortableClob)}.
     *
     * @param reader the reader providing the character data
     * @param length the length of the character data in characters
     * @return a new {@link PortableClob} wrapping the character data
     * @see PortableClob
     */
    PortableClob createClob(@NonNull Reader reader, long length);

    /**
     * Replaces the content of the given CLOB with data read from the reader.
     *
     * <p>Depending on the database dialect this may allocate a new underlying database
     * object (e.g. a new PostgreSQL Large Object). Call {@link #updateEntity} on the
     * owning entity afterwards to persist the updated reference:
     * <pre>{@code
     * ArticleEntity article = session.selectEntity(qArticle).whereIdEq(1L).fetchSingle();
     * session.updateClob(article.getBody(), new StringReader(newText), newText.length());
     * session.updateEntity(qArticle, article);
     * }</pre>
     *
     * @param clob   the CLOB to update
     * @param reader the reader providing the new character data
     * @param length the length of the new character data in characters
     */
    void updateClob(@NonNull PortableClob clob, @NonNull Reader reader, long length);

    /**
     * Removes the content of the given CLOB, releasing its underlying database
     * resources. Afterwards {@link PortableClob#isPresent()} returns {@code false}.
     * Call {@link #updateEntity} on the owning entity to persist the cleared state.
     *
     * @param clob the CLOB to clear
     */
    void clearClob(@NonNull PortableClob clob);

    /**
     * Opens a reader over the content of the given CLOB.
     *
     * <p>The returned reader must be closed by the caller and consumed while the
     * session's connection (and, on PostgreSQL, the surrounding transaction) is still
     * open. For small payloads prefer {@link #readClobAllChars(PortableClob)}.
     *
     * @param clob the CLOB to read
     * @return a reader over the character content, or {@link Optional#empty()} if the CLOB is empty
     */
    Optional<Reader> readClobData(PortableClob clob);

    /**
     * Reads the entire content of the given CLOB into a string.
     *
     * @param clob the CLOB to read
     * @return the full character content
     * @throws IOException                      if reading the content fails
     * @throws java.util.NoSuchElementException if the CLOB is empty
     * @see #readClobData(PortableClob)
     */
    default String readClobAllChars(@NonNull PortableClob clob) throws IOException {
        try (Reader reader = readClobData(clob).orElseThrow()) {
            StringWriter stringWriter = new StringWriter();
            reader.transferTo(stringWriter);
            return stringWriter.toString();
        }
    }

    /**
     * Returns the length of the given CLOB's content.
     *
     * @param clob the CLOB to measure
     * @return the content length in characters, or {@code 0} if the CLOB is empty
     */
    long getClobLength(PortableClob clob);

}
