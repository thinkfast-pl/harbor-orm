// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.HarborSession;
import io.github.thinkfastpl.harbororm.api.expression.CommonTableExpression;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.expression.FunctionCallTableSource;
import io.github.thinkfastpl.harbororm.api.lob.PortableBlob;
import io.github.thinkfastpl.harbororm.api.lob.PortableClob;
import io.github.thinkfastpl.harbororm.api.lob.PortableLobSupport;
import io.github.thinkfastpl.harbororm.api.metadata.QEntity;
import io.github.thinkfastpl.harbororm.api.metadata.QView;
import io.github.thinkfastpl.harbororm.api.query.*;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
class DefaultHarborSession implements HarborSession {

    @NonNull
    private final QueryExecutor executor;

    @NonNull
    private final PortableLobSupport portableLobSupport;

    private final int defaultInsertBatchSize;

    private final int defaultLazyLoadBatchSize;

    private final int defaultStreamFetchSize;

    @Override
    public InsertQuery.IntoStep insert() {
        return new FluentInsertQuery(new DefaultInsertQuery(executor));
    }

    @Override
    public <T, ID> void insertEntity(@NonNull QEntity<T, ID> qEntity, @NonNull T entity) {
        EntityHandler.insertEntity(executor, qEntity, entity, null, defaultStreamFetchSize);
    }

    @Override
    public <T, ID> void insertEntityBatch(@NonNull QEntity<T, ID> qEntity, @NonNull Collection<T> entities) {
        EntityHandler.insertEntityBatch(executor, qEntity, entities, defaultInsertBatchSize, defaultStreamFetchSize);
    }

    @Override
    public <T, ID> void insertEntityBatch(@NonNull QEntity<T, ID> qEntity, @NonNull Collection<T> entities, int batchSize) {
        EntityHandler.insertEntityBatch(executor, qEntity, entities, batchSize, defaultStreamFetchSize);
    }

    @Override
    public SelectQuery.WithStep<Record> with(@NonNull List<CommonTableExpression> commonTableExpressions, boolean recursive) {
        return new DefaultSelectQuery<>(executor, false, Record.class, record -> record, defaultStreamFetchSize)
                .with(commonTableExpressions, recursive);
    }

    @Override
    public SelectQuery.SelectStep<Record> select(@NonNull List<? extends Expression<?>> expressions) {
        return new DefaultSelectQuery<>(executor, false, Record.class, record -> record, defaultStreamFetchSize)
                .select(expressions);
    }

    @Override
    public <T> SelectQuery.SelectStep<T> select(@NonNull Expression<T> expression) {
        return DefaultSelectQuery.ofSingleExpressionSelect(executor, false, expression, defaultStreamFetchSize);
    }

    @Override
    public SelectQuery.SelectStep<Record> selectDistinct(@NonNull List<Expression<?>> expressions) {
        return new DefaultSelectQuery<>(executor, true, Record.class, record -> record, defaultStreamFetchSize)
                .select(expressions);
    }

    @Override
    public <T> SelectQuery.SelectStep<T> selectDistinct(@NonNull Expression<T> expression) {
        return DefaultSelectQuery.ofSingleExpressionSelect(executor, true, expression, defaultStreamFetchSize);
    }

    @Override
    public <T, ID> EntityQuery<T, ID> selectEntity(@NonNull QEntity<T, ID> qEntity) {
        return new DefaultEntityQuery<>(executor, qEntity, defaultLazyLoadBatchSize, defaultStreamFetchSize);
    }

    @Override
    public <T> ViewQuery<T> select(@NonNull QView<T> qView) {
        return new DefaultViewQuery<>(executor, qView, defaultStreamFetchSize);
    }

    @Override
    public <T> ViewQuery<T> select(@NonNull FunctionCallTableSource<T> functionCall) {
        return new DefaultViewQuery<>(executor, functionCall, defaultStreamFetchSize);
    }

    @Override
    public UpdateQuery.TableStep update() {
        return new DefaultUpdateQuery(executor);
    }

    @Override
    public <T, ID> void updateEntity(@NonNull QEntity<T, ID> qEntity, @NonNull T entity) {
        EntityHandler.updateEntity(executor, portableLobSupport, qEntity, entity, defaultLazyLoadBatchSize, defaultStreamFetchSize);
    }

    @Override
    public <T, ID> void replaceEntity(@NonNull QEntity<T, ID> qEntity, @NonNull T entity) {
        EntityHandler.replaceEntity(executor, portableLobSupport, qEntity, entity, defaultLazyLoadBatchSize, defaultStreamFetchSize);
    }

    @Override
    public DeleteQuery.FromStep delete() {
        return new DefaultDeleteQuery(executor);
    }

    @Override
    public <T, ID> void deleteEntity(@NonNull QEntity<T, ID> qEntity, @NonNull T entity) {
        EntityHandler.deleteEntity(executor, portableLobSupport, qEntity, entity);
    }

    @Override
    public <T, ID> void deleteEntityAll(@NonNull QEntity<T, ID> qEntity, @NonNull Collection<T> entities) {
        if (entities.isEmpty()) {
            return;
        }

        EntityHandler.deleteEntities(executor, portableLobSupport, qEntity, entities);
    }

    @Override
    public <T, ID> void deleteEntityById(@NonNull QEntity<T, ID> qEntity, @NonNull ID id) {
        if (EntityHandler.simpleDeletionPermitted(portableLobSupport, qEntity)) {
            new DefaultDeleteQuery(executor)
                    .from(qEntity.getTableName())
                    .where(qEntity.getIdColumn().eq(id))
                    .execute();
        } else {
            selectEntity(qEntity).where(qEntity.getIdColumn().eq(id)).fetchOne()
                    .ifPresent(entity -> deleteEntity(qEntity, entity));
        }
    }

    @Override
    public <T, ID> void deleteEntityByIds(@NonNull QEntity<T, ID> qEntity, @NonNull Collection<ID> ids) {
        if (ids.isEmpty()) {
            return;
        }

        if (EntityHandler.simpleDeletionPermitted(portableLobSupport, qEntity)) {
            new DefaultDeleteQuery(executor)
                    .from(qEntity.getTableName())
                    .where(qEntity.getIdColumn().in(ids))
                    .execute();
        } else {
            deleteEntityAll(
                    qEntity,
                    selectEntity(qEntity).where(qEntity.getIdColumn().in(ids)).fetchAll()
            );
        }
    }

    @Override
    public void call(@NonNull String procedureName, Object... params) {
        executor.call(procedureName, params == null ? new Object[0] : params);
    }

    @Override
    public <T> T callReturning(@NonNull String functionName, @NonNull Class<T> returnType, Object... params) {
        return executor.callReturning(functionName, returnType, params == null ? new Object[0] : params);
    }

    @Override
    public PortableBlob createBlob(@NonNull InputStream inputStream, long length) {
        return portableLobSupport.createBlob(inputStream, length);
    }

    @Override
    public void updateBlob(@NonNull PortableBlob blob, @NonNull InputStream inputStream, long length) {
        portableLobSupport.updateBlob(blob, inputStream, length);
    }

    @Override
    public void clearBlob(@NonNull PortableBlob blob) {
        portableLobSupport.clearBlob(blob);
    }

    @Override
    public Optional<InputStream> readBlobData(PortableBlob blob) {
        return blob == null ? Optional.empty() : portableLobSupport.readBlobData(blob);
    }

    @Override
    public long getBlobLength(PortableBlob blob) {
        return blob == null ? 0 : portableLobSupport.getBlobLength(blob);
    }

    @Override
    public PortableClob createClob(@NonNull Reader reader, long length) {
        return portableLobSupport.createClob(reader, length);
    }

    @Override
    public void updateClob(@NonNull PortableClob clob, @NonNull Reader reader, long length) {
        portableLobSupport.updateClob(clob, reader, length);
    }

    @Override
    public void clearClob(@NonNull PortableClob clob) {
        portableLobSupport.clearClob(clob);
    }

    @Override
    public Optional<Reader> readClobData(PortableClob clob) {
        return clob == null ? Optional.empty() : portableLobSupport.readClobData(clob);
    }

    @Override
    public long getClobLength(PortableClob clob) {
        return clob == null ? 0 : portableLobSupport.getClobLength(clob);
    }
}
