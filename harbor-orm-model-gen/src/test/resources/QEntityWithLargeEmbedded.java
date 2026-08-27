// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.metadata.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class QEntityWithLargeEmbedded implements QEntity<EntityWithLargeEmbedded, Long> {

    public final QColumn<Long> id;

    public final QLargeEmbeddable data;

    private final List<QAttribute> __allAttributes;

    private final QTableName __tableName;

    public QEntityWithLargeEmbedded(String alias) {
        this.__tableName = new QTableName("ewle", null, alias);
        this.id = QColumn.regular(Long.class, alias, null, "id", "id", true, false, false, null, null, true, true, false, false, null, null, null);
        this.data = new QLargeEmbeddable(alias, "data", Map.ofEntries(Map.entry("field11", QColumn.regular(String.class, alias, null, "field11", "col_11", false, false, false, null, null, true, true, false, false, null, null, null)), Map.entry("field1", QColumn.regular(String.class, alias, null, "field1", "col_1", false, false, false, null, null, true, true, false, false, null, null, null)), Map.entry("field10", QColumn.regular(String.class, alias, null, "field10", "col_10", false, false, false, null, null, true, true, false, false, null, null, null)), Map.entry("field7", QColumn.regular(String.class, alias, null, "field7", "col_7", false, false, false, null, null, true, true, false, false, null, null, null)), Map.entry("field6", QColumn.regular(String.class, alias, null, "field6", "col_6", false, false, false, null, null, true, true, false, false, null, null, null)), Map.entry("field9", QColumn.regular(String.class, alias, null, "field9", "col_9", false, false, false, null, null, true, true, false, false, null, null, null)), Map.entry("field8", QColumn.regular(String.class, alias, null, "field8", "col_8", false, false, false, null, null, true, true, false, false, null, null, null)), Map.entry("field3", QColumn.regular(String.class, alias, null, "field3", "col_3", false, false, false, null, null, true, true, false, false, null, null, null)), Map.entry("field2", QColumn.regular(String.class, alias, null, "field2", "col_2", false, false, false, null, null, true, true, false, false, null, null, null)), Map.entry("field5", QColumn.regular(String.class, alias, null, "field5", "col_5", false, false, false, null, null, true, true, false, false, null, null, null)), Map.entry("field4", QColumn.regular(String.class, alias, null, "field4", "col_4", false, false, false, null, null, true, true, false, false, null, null, null))));

        this.__allAttributes = List.of(
                this.id, this.data
        );
    }

    @Override
    public QTableName getTableName() {
        return __tableName;
    }

    @Override
    public QComparableAttribute<Long> getIdColumn() {
        return this.id;
    }

    @Override
    public Class<EntityWithLargeEmbedded> getBeanType() {
        return EntityWithLargeEmbedded.class;
    }

    @Override
    public List<QAttribute> getAllAttributes() {
        return this.__allAttributes;
    }

    @Override
    public Optional<String> getPreInsertMethodName() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getPreUpdateMethodName() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getPreDeleteMethodName() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getPostInsertMethodName() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getPostUpdateMethodName() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getPostDeleteMethodName() {
        return Optional.empty();
    }

    @Override
    public Optional<QColumn<?>> getVersionColumn() {
        return Optional.empty();
    }
}
