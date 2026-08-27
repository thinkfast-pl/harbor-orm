// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.metadata.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class QRoleEntity implements QEntity<RoleEntity, Long> {

    public final QColumn<Long> id;

    public final QColumn<String> name;

    public final QElementCollection<String> permissions;

    private final List<QAttribute> __allAttributes;

    private final QTableName __tableName;

    public QRoleEntity(String alias) {
        this.__tableName = new QTableName("role", null, alias);
        this.id = QColumn.regular(Long.class, alias, null, "id", "id", true, false, false, null, null, true, true, false, false, null, null, null);
        this.name = QColumn.regular(String.class, alias, null, "name", "name", false, false, false, null, null, true, true, false, false, null, null, null);
        this.permissions = QElementCollection.of(QColumn.onElementCollection(String.class, "permission", null, null, null, null), "permissions", "role_permission", null, List.of(new JoinColumnData("role_id", Long.class, null, true, true, false)));

        this.__allAttributes = List.of(
                this.id, this.name, this.permissions
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
    public Class<RoleEntity> getBeanType() {
        return RoleEntity.class;
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
