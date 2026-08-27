// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

/**
 * A {@link QColumn} that has an attached {@link io.github.thinkfastpl.harbororm.api.converter.AttributeConverter},
 * providing a bridge between the entity Java type ({@code T}) and the database Java type ({@code C}).
 *
 * <p>Created via {@link QColumn#converted} when an entity field uses {@code @Convert} or {@code @Enumerated}.
 * The runtime uses this interface to apply the converter during SQL parameter binding and result mapping.
 *
 * @param <T> the entity-side Java type (what the application code sees)
 * @param <C> the database-side Java type (what is stored in the database)
 * @see QColumn#converted
 * @see io.github.thinkfastpl.harbororm.api.converter.AttributeConverter
 */
public interface QColumnConverted<T, C> extends QColumn<T> {

    /**
     * Returns the Java type that the database column actually stores.
     *
     * @return the database-side Java class
     */
    Class<C> getDbJavaType();

    /**
     * Returns an equivalent {@link QColumn} of the database type, without the converter attached.
     *
     * <p>Useful when building raw SQL expressions that operate on the database representation directly.
     *
     * @return an unconverted column reference of the database type
     */
    QColumn<C> asUnconverted();
}
