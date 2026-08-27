// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

/**
 * Marker interface for anything that can appear in a SQL {@code FROM} clause.
 *
 * <p>Implementations include:
 * <ul>
 *   <li>{@link QTableName} - a concrete table reference (name, schema, alias)</li>
 *   <li>Subquery-derived table sources used with lateral joins</li>
 *   <li>Function call table sources produced by {@code @StoredFunction} metadata</li>
 * </ul>
 *
 * @see QTableName
 */
public interface QTableSource {

}
