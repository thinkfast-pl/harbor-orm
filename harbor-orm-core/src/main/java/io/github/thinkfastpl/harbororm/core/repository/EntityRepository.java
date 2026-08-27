// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.repository;

import io.github.thinkfastpl.harbororm.api.HarborSession;
import io.github.thinkfastpl.harbororm.api.metadata.QEntity;
import io.github.thinkfastpl.harbororm.api.repository.CrudRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Abstract base repository providing standard CRUD operations for a given entity type.
 * <p>
 * Subclass this to create a repository for a specific entity. The constructor requires
 * a {@link HarborSession} and the generated {@link QEntity} metadata for the entity type.
 * Custom query methods can be added using the protected {@code session} field.
 *
 * <pre>
 * public class BookRepository extends EntityRepository&lt;Book, Long&gt; {
 *     private static final QBook BOOK = new QBook(null);
 *
 *     public BookRepository(HarborSession session) {
 *         super(session, BOOK);
 *     }
 * }
 * </pre>
 *
 * @param <T>  the entity type
 * @param <ID> the entity's primary key type
 */
@RequiredArgsConstructor
public abstract class EntityRepository<T, ID> implements CrudRepository<T, ID> {

    @NonNull
    protected final HarborSession session;

    @NonNull
    protected final QEntity<T, ID> qEntity;

    @Override
    public void insert(@NonNull T entity) {
        session.insertEntity(qEntity, entity);
    }

    @Override
    public Optional<T> findById(@NonNull ID id) {
        return session.selectEntity(qEntity)
                .where(qEntity.getIdColumn().eq(id))
                .fetchOne();
    }

    @Override
    public Optional<T> findByIdForUpdate(@NonNull ID id) {
        return session.selectEntity(qEntity)
                .where(qEntity.getIdColumn().eq(id))
                .forUpdate()
                .fetchOne();
    }

    @Override
    public List<T> findAll() {
        return session.selectEntity(qEntity).fetchAll();
    }

    @Override
    public List<T> findAllById(@NonNull Collection<ID> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return session.selectEntity(qEntity)
                .where(qEntity.getIdColumn().in(ids))
                .fetchAll();
    }

    @Override
    public Stream<T> streamAll() {
        return session.selectEntity(qEntity).streamAll();
    }

    @Override
    public long countAll() {
        return session.selectEntity(qEntity).count();
    }

    @Override
    public boolean existsById(@NonNull ID id) {
        return session.selectEntity(qEntity)
                .where(qEntity.getIdColumn().eq(id))
                .count() > 0;
    }

    @Override
    public void update(@NonNull T entity) {
        session.updateEntity(qEntity, entity);
    }

    @Override
    public void delete(@NonNull T entity) {
        session.deleteEntity(qEntity, entity);
    }

    @Override
    public void deleteById(@NonNull ID id) {
        session.deleteEntityById(qEntity, id);
    }
}
