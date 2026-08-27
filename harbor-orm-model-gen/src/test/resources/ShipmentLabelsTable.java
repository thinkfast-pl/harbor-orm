// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.metadata.*;
import java.util.List;
import java.util.Map;

public class ShipmentLabelsTable implements QTable {

    public final QColumn<Long> id;

    public final QColumn<String> barcode;

    public final QColumn<Long> shipmentId;

    private final List<QColumn<?>> __allColumns;

    private final QTableName __tableName;

    public ShipmentLabelsTable(String alias) {
        this.__tableName = new QTableName("shipment_labels", null, alias);
        this.id = QColumn.regular(Long.class, alias, null, "id", "id", true, false, false, null, null, true, true, false, false, null, null, null);
        this.barcode = QColumn.regular(String.class, alias, null, "barcode", "barcode", false, false, false, null, null, true, true, false, false, null, null, null);
        this.shipmentId = QColumn.regular(Long.class, alias, null, "shipmentId", "shipment_id", false, false, false, null, null, true, true, false, false, null, null, null);
        this.__allColumns = List.of(
                this.id, this.barcode, this.shipmentId
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
