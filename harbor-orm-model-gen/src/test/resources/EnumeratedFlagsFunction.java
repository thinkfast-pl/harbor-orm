// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.expression.ConstantExpression;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.expression.FunctionCallTableSource;
import io.github.thinkfastpl.harbororm.api.metadata.*;
import java.util.List;
import java.util.Map;

class EnumeratedFlagsFunction implements QTable {

    public final QColumn<String> name;

    public final QColumn<entities.EnumeratedFlags.Status> status;

    public final QColumn<entities.EnumeratedFlags.Priority> priority;

    private final List<QColumn<?>> __allColumns;

    private final QTableName __tableName;

    public EnumeratedFlagsFunction(String alias) {
        this.__tableName = new QTableName("get_enumerated_flags", null, alias);
        this.name = QColumn.regular(String.class, alias, null, "name", "name", false, false, false, null, null, true, true, false, false, null, null, null);
        this.status = QColumn.regular(entities.EnumeratedFlags.Status.class, alias, null, "status", "status", false, false, false, null, null, true, true, false, false, EnumMappingType.STRING, null, null);
        this.priority = QColumn.regular(entities.EnumeratedFlags.Priority.class, alias, null, "priority", "priority", false, false, false, null, null, true, true, false, false, EnumMappingType.ORDINAL, null, null);
        this.__allColumns = List.of(
                this.name, this.status, this.priority
        );
    }

    public FunctionCallTableSource<EnumeratedFlags> call() {
        return new FunctionCallTableSource<>(__tableName, __allColumns, List.of(), EnumeratedFlags.class);
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
