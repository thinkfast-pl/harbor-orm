// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

/**
 * Marker interface for entity attributes in the HarborORM metadata model.
 *
 * <p>All metadata representations of entity fields implement this interface, including:
 * <ul>
 *   <li>{@link QColumn} - simple column attributes</li>
 *   <li>{@link QEmbeddable} - embedded value object attributes</li>
 *   <li>{@link QElementCollection} - element collection attributes</li>
 *   <li>{@link QEntityRelation} - {@code @OneToMany} and {@code @OneToOne} relation attributes</li>
 *   <li>{@link QManyToMany} - {@code @ManyToMany} relation attributes</li>
 * </ul>
 *
 * <p>Instances are produced by the annotation processor and made available through
 * {@link QAttributeHolder#getAllAttributes()}.
 *
 * @see QColumn
 * @see QEmbeddable
 * @see QEntityRelation
 * @see QElementCollection
 * @see QManyToMany
 */
public interface QAttribute {

}
