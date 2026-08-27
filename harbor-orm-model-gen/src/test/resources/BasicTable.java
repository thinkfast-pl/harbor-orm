// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.metadata.*;
import java.util.List;

public class BasicTable implements QTable {

    public final QColumn<Long> id;

    public final QColumn<Boolean> boolAsString;

    private final List<QColumn<?>> __allColumns;

    private final QTableName __tableName;

    public BasicTable(String alias) {
        this.__tableName = new QTableName("basic", null, alias);
        this.id = QColumn.regular(Long.class, alias, null, "id", "id", true, false, false, null, null, true, true, false, false, null, null, null);
        this.boolAsString = QColumn.regular(Boolean.class, alias, null, "boolAsString", "bool_as_string", false, false, false, null, new ConverterData(entities.BooleanToStringConverter.class, Boolean.class, String.class), true, true, false, false, null, null, null);
        this.__allColumns = List.of(
                this.id, this.boolAsString
        );
    }

    @Override
    public QTableName getTableName() {
        return __tableName;
    }

    @Override
    public List<QColumn<?>> getAllColumns() {
        return this.__allColumns;
    }
}
