// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.metadata.*;
import java.util.List;
import java.util.Map;

public class ShipmentEventsTable implements QTable {

    public final QColumn<Long> id;

    public final QColumn<String> description;

    public final QColumn<Long> shipmentId;

    private final List<QColumn<?>> __allColumns;

    private final QTableName __tableName;

    public ShipmentEventsTable(String alias) {
        this.__tableName = new QTableName("shipment_events", null, alias);
        this.id = QColumn.regular(Long.class, alias, null, "id", "id", true, false, false, null, null, true, true, false, false, null, null, null);
        this.description = QColumn.regular(String.class, alias, null, "description", "description", false, false, false, null, null, true, true, false, false, null, null, null);
        this.shipmentId = QColumn.regular(Long.class, alias, null, "shipmentId", "shipment_id", false, false, false, null, null, true, true, false, false, null, null, null);
        this.__allColumns = List.of(
                this.id, this.description, this.shipmentId
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
