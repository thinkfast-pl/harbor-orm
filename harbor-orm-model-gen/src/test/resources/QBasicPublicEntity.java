// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.metadata.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QBasicPublicEntity implements QEntity<BasicPublicEntity, Long> {

    public final QColumn<Long> id;

    public final QColumnConverted<Boolean, String> boolAsString;

    private final List<QAttribute> __allAttributes;

    private final QTableName __tableName;

    public QBasicPublicEntity(String alias) {
        this.__tableName = new QTableName("basic", null, alias);
        this.id = QColumn.regular(Long.class, alias, null, "id", "id", true, false, false, null, null, true, true, false, false, null, null, null);
        this.boolAsString = QColumn.converted(Boolean.class, String.class, alias, null, "boolAsString", "bool_as_string", false, false, false, null, new ConverterData(entities.BooleanToStringConverter.class, Boolean.class, String.class), true, true, false, false, null, null, null);
        this.__allAttributes = List.of(
                this.id, this.boolAsString
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
    public Class<BasicPublicEntity> getBeanType() {
        return BasicPublicEntity.class;
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
