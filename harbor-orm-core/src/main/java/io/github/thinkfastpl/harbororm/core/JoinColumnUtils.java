// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.expression.Condition;
import io.github.thinkfastpl.harbororm.api.expression.DSL;
import io.github.thinkfastpl.harbororm.api.metadata.*;
import io.github.thinkfastpl.harbororm.core.utils.HarborBeanUtils;
import lombok.NonNull;

import java.util.*;

class JoinColumnUtils {

    static <T> Condition getJoinConditions(final List<JoinColumnData> joinColumns, final Map<T, Map<JoinColumnData, Object>> entitiesData) {
        if (joinColumns.isEmpty()) {
            throw new IllegalStateException("No join columns found");
        }

        if (joinColumns.size() == 1) {
            final JoinColumnData joinColumn = joinColumns.get(0);

            final List<Object> columnValues = entitiesData.values().stream()
                    .map(joinColumnDataValues -> joinColumnDataValues.get(joinColumn))
                    .toList();

            @SuppressWarnings("unchecked") final QColumn<Object> qColumnCasted = (QColumn<Object>) joinColumn.getQColumn();
            return qColumnCasted.in(columnValues);
        } else {
            List<Condition> allConditions = entitiesData.values().stream()
                    .map(entityData -> {
                        List<Condition> conditions = joinColumns.stream()
                                .map(joinColumn -> {
                                    @SuppressWarnings("unchecked") final QColumn<Object> qColumnCasted = (QColumn<Object>) joinColumn.getQColumn();
                                    return qColumnCasted.eq(entityData.get(joinColumn));
                                })
                                .toList();

                        return DSL.and(conditions);
                    })
                    .toList();

            return DSL.or(allConditions);
        }
    }

    static <T, ID> Map<JoinColumnData, Object> getEntityJoinColumnData(@NonNull QEntity<T, ID> qEntity, @NonNull T entity, @NonNull List<JoinColumnData> joinColumns) {
        return getEntityJoinColumnData(qEntity, List.of(entity), joinColumns).get(entity);
    }

    static <T, ID> Map<T, Map<JoinColumnData, Object>> getEntityJoinColumnData(@NonNull QEntity<T, ID> qEntity, @NonNull Collection<T> entities, @NonNull List<JoinColumnData> joinColumns) {
        final Map<JoinColumnData, List<String>> joinColumnPaths = new IdentityHashMap<>();
        for (JoinColumnData joinColumn : joinColumns) {
            joinColumnPaths.put(joinColumn, resolveJoinColumnPath(qEntity, joinColumn));
        }

        final Map<T, Map<JoinColumnData, Object>> entitiesData = new IdentityHashMap<>();
        for (T entity : entities) {
            final Map<JoinColumnData, Object> joinColumnDataValues = new IdentityHashMap<>();
            for (JoinColumnData joinColumn : joinColumns) {
                joinColumnDataValues.put(joinColumn, HarborBeanUtils.getPropertyValue(entity, joinColumnPaths.get(joinColumn)));
            }
            entitiesData.put(entity, joinColumnDataValues);
        }
        return entitiesData;
    }

    private static <T, ID> List<String> resolveJoinColumnPath(@NonNull QEntity<T, ID> qEntity, @NonNull JoinColumnData joinColumnData) {
        if (joinColumnData.getReferencedColumnName() == null) {
            final QComparableAttribute<ID> idColumn = qEntity.getIdColumn();
            if (idColumn instanceof QEmbeddable<?>) {
                throw new IllegalStateException(
                        "Join column '" + joinColumnData.getName() + "' has no referencedColumnName but entity "
                                + qEntity.getBeanType().getName()
                                + " has a composite ID; specify referencedColumnName on each @JoinColumn");
            }
            return List.of(idColumn.getPropertyName());
        }

        final List<String> path = findPathByColumnName(qEntity, joinColumnData.getReferencedColumnName(), List.of());
        if (path == null) {
            throw new IllegalStateException(
                    "Referenced column '" + joinColumnData.getReferencedColumnName() + "' of join column '"
                            + joinColumnData.getName() + "' not found in entity " + qEntity.getBeanType().getName());
        }
        return path;
    }

    static Map<JoinColumnData, List<String>> resolveInverseIdPaths(@NonNull QAttributeHolder idHolder, @NonNull List<JoinColumnData> inverseJoinColumns) {
        final Map<JoinColumnData, List<String>> result = new IdentityHashMap<>();
        for (JoinColumnData joinColumn : inverseJoinColumns) {
            if (joinColumn.getReferencedColumnName() == null) {
                throw new IllegalStateException(
                        "Inverse join column '" + joinColumn.getName()
                                + "' must specify referencedColumnName when the related ID is composite");
            }
            final List<String> path = findPathByColumnName(idHolder, joinColumn.getReferencedColumnName(), List.of());
            if (path == null) {
                throw new IllegalStateException(
                        "Referenced column '" + joinColumn.getReferencedColumnName() + "' of inverse join column '"
                                + joinColumn.getName() + "' not found in composite ID attributes");
            }
            result.put(joinColumn, path);
        }
        return result;
    }

    private static List<String> findPathByColumnName(@NonNull QAttributeHolder attributeHolder, @NonNull String referencedColumnName, @NonNull List<String> prefix) {
        for (QAttribute qAttribute : attributeHolder.getAllAttributes()) {
            if (qAttribute instanceof QColumn<?> qColumn && qColumn.getColumnName().equals(referencedColumnName)) {
                final List<String> path = new ArrayList<>(prefix);
                path.add(qColumn.getPropertyName());
                return path;
            }
            if (qAttribute instanceof QEmbeddable<?> qEmbeddable) {
                final List<String> nestedPrefix = new ArrayList<>(prefix);
                nestedPrefix.add(qEmbeddable.getPropertyName());
                final List<String> found = findPathByColumnName(qEmbeddable, referencedColumnName, nestedPrefix);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }
}
