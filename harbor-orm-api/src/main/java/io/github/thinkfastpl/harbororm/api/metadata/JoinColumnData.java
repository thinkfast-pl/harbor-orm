// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

import lombok.Value;

/**
 * Immutable value class holding metadata for a join column as declared by {@code @JoinColumn}.
 *
 * <p>Used in {@link QEntityRelation}, {@link QElementCollection}, and {@link QManyToMany} to describe
 * the foreign key columns that link a child/collection table to the parent entity table.
 *
 * <p>A corresponding {@link QColumn} is automatically created and accessible via {@link #getQColumn()}.
 *
 * @see QEntityRelation
 * @see QElementCollection
 * @see QManyToMany
 */
@Value
public class JoinColumnData {

    /** The foreign key column name in the child/join table. */
    String name;

    /** The Java type of the foreign key field (must match the parent entity's ID type). */
    Class<?> fieldType;

    /** The referenced column name in the parent table, or {@code null} to default to the parent ID column. */
    String referencedColumnName;

    /** Whether this join column is included in INSERT statements. */
    boolean insertable;

    /** Whether this join column is included in UPDATE statements. */
    boolean updatable;

    /** Whether this join column allows NULL values. */
    boolean nullable;

    /** A {@link QColumn} reference derived from this join column's metadata. */
    QColumn<?> qColumn;

    /**
     * Creates a new join column metadata instance and auto-generates a matching {@link QColumn}.
     *
     * @param name                 the foreign key column name
     * @param fieldType            the Java type of the foreign key
     * @param referencedColumnName the referenced parent column name, or {@code null}
     * @param insertable           whether included in INSERT statements
     * @param updatable            whether included in UPDATE statements
     * @param nullable             whether NULL is allowed
     */
    public JoinColumnData(String name, Class<?> fieldType, String referencedColumnName, boolean insertable, boolean updatable, boolean nullable) {
        this.name = name;
        this.fieldType = fieldType;
        this.referencedColumnName = referencedColumnName;
        this.insertable = insertable;
        this.updatable = updatable;
        this.nullable = nullable;
        this.qColumn = QColumn.of(this);
    }
}
