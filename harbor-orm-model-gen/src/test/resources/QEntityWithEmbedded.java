// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.metadata.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class QEntityWithEmbedded implements QEntity<EntityWithEmbedded, Long> {

    public final QColumn<Long> id;

    public final QColumn<String> name;

    public final QAddressEmbeddable address;

    private final List<QAttribute> __allAttributes;

    private final QTableName __tableName;

    public QEntityWithEmbedded(String alias) {
        this.__tableName = new QTableName("ewe", null, alias);
        this.id = QColumn.regular(Long.class, alias, null, "id", "id", true, false, false, null, null, true, true, false, false, null, null, null);
        this.name = QColumn.regular(String.class, alias, null, "name", "name", false, false, false, null, null, true, true, false, false, null, null, null);
        this.address = new QAddressEmbeddable(alias, "address", Map.of("postalCode", QColumn.regular(String.class, alias, null, "postalCode", "other_postal_code", false, false, false, null, null, true, true, false, false, null, null, null)));

        this.__allAttributes = List.of(
                this.id, this.name, this.address
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
    public Class<EntityWithEmbedded> getBeanType() {
        return EntityWithEmbedded.class;
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