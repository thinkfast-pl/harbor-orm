// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import lombok.NonNull;

import java.util.List;

/**
 * Metadata for an entity relationship ({@code @OneToMany} or {@code @OneToOne}).
 *
 * <p>Relationships follow DDD aggregate semantics: the parent entity owns and manages the child
 * entity's full lifecycle (insert, update, delete are cascaded automatically). Children are loaded
 * lazily on first access.
 *
 * <p>Instances are created via the static factory methods {@link #ofOneToMany} and {@link #ofOneToOne}.
 *
 * @param <T>  the related entity Java type
 * @param <ID> the related entity primary key type
 * @see QAttribute
 * @see QEntity
 * @see JoinColumnData
 */
public interface QEntityRelation<T, ID> extends QAttribute {

    /**
     * The type of entity relationship.
     */
    enum Type {
        /** A one-to-many relationship (parent owns a collection of children). */
        ONE_TO_MANY,
        /** A one-to-one relationship (parent owns a single child). */
        ONE_TO_ONE,
    }

    /**
     * Returns the relationship type.
     *
     * @return {@link Type#ONE_TO_MANY} or {@link Type#ONE_TO_ONE}
     */
    Type getType();

    /**
     * Returns the Java property name of the relation field on the parent entity.
     *
     * @return the entity field name
     */
    String getPropertyName();

    /**
     * Returns the join columns that link the child table to the parent entity.
     *
     * @return an immutable list of join column metadata
     */
    List<JoinColumnData> getJoinColumns();

    /**
     * Returns the metadata of the related (child) entity.
     *
     * @return the related entity's {@link QEntity} metadata
     */
    QEntity<T, ID> getRelatedQEntity();

    /**
     * Creates a one-to-many relationship metadata instance.
     *
     * @param <T>          the child entity type
     * @param <ID>         the child entity ID type
     * @param propertyName the property name on the parent entity
     * @param entity       the child entity metadata
     * @param joinColumns  the join columns linking child to parent
     * @return a new one-to-many relation metadata
     */
    static <T, ID> QEntityRelation<T, ID> ofOneToMany(@NonNull String propertyName, @NonNull QEntity<T, ID> entity, @NonNull List<JoinColumnData> joinColumns) {
        return new QEntityRelation<>() {
            @Override
            public Type getType() {
                return Type.ONE_TO_MANY;
            }

            @Override
            public String getPropertyName() {
                return propertyName;
            }

            @Override
            public List<JoinColumnData> getJoinColumns() {
                return joinColumns;
            }

            @Override
            public QEntity<T, ID> getRelatedQEntity() {
                return entity;
            }
        };
    }

    /**
     * Creates a one-to-one relationship metadata instance.
     *
     * @param <T>          the child entity type
     * @param <ID>         the child entity ID type
     * @param propertyName the property name on the parent entity
     * @param entity       the child entity metadata
     * @param joinColumns  the join columns linking child to parent
     * @return a new one-to-one relation metadata
     */
    static <T, ID> QEntityRelation<T, ID> ofOneToOne(@NonNull String propertyName, @NonNull QEntity<T, ID> entity, @NonNull List<JoinColumnData> joinColumns) {
        return new QEntityRelation<>() {
            @Override
            public Type getType() {
                return Type.ONE_TO_ONE;
            }

            @Override
            public String getPropertyName() {
                return propertyName;
            }

            @Override
            public List<JoinColumnData> getJoinColumns() {
                return joinColumns;
            }

            @Override
            public QEntity<T, ID> getRelatedQEntity() {
                return entity;
            }
        };
    }
}
