// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.LazyRef
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.ShipmentEventsTable
import io.github.thinkfastpl.harbororm.query.ShipmentLabelsTable
import io.github.thinkfastpl.harbororm.test.domain.QShipmentEntity
import io.github.thinkfastpl.harbororm.test.domain.ShipmentEntity
import io.github.thinkfastpl.harbororm.test.domain.ShipmentLabelEntity

/**
 * Integration tests verifying that join columns declared on @OneToOne and
 * @OneToMany relations appear in the generated table classes of child
 * entities that do not declare the FK column as a field.
 */
class RelationJoinColumnTableIT extends AbstractHarborIT {

    def "should expose @OneToOne join column in generated table class"() {
        given:
            ShipmentLabelsTable labels = new ShipmentLabelsTable(null)
            QShipmentEntity qShipment = new QShipmentEntity(null)
            session.insertEntity(qShipment, new ShipmentEntity(1L, "Package", null, []))

        when:
            session.insert()
                    .into("shipment_labels")
                    .set(labels.id, 10L)
                    .set(labels.shipmentId, 1L)
                    .set(labels.barcode, "BAR-1")
                    .execute()

        then:
            Record record = session.select(labels.allColumns)
                    .from(labels)
                    .where(labels.shipmentId.eq(1L))
                    .fetchSingle()
            record.get(labels.id) == 10L
            record.get(labels.shipmentId) == 1L
            record.get(labels.barcode) == "BAR-1"

        where:
            session << allSessions
    }

    def "should set @OneToOne join column on cascade insert and read it via table class"() {
        given:
            ShipmentLabelsTable labels = new ShipmentLabelsTable(null)
            QShipmentEntity qShipment = new QShipmentEntity(null)
            ShipmentEntity shipment = new ShipmentEntity(
                    1L, "Package", LazyRef.of(new ShipmentLabelEntity(10L, "BAR-1")), [])

        when:
            session.insertEntity(qShipment, shipment)

        then:
            Record record = session.select(labels.allColumns)
                    .from(labels)
                    .where(labels.shipmentId.eq(1L))
                    .fetchSingle()
            record.get(labels.id) == 10L
            record.get(labels.barcode) == "BAR-1"

        where:
            session << allSessions
    }

    def "should expose @OneToMany join column in generated table class"() {
        given:
            ShipmentEventsTable events = new ShipmentEventsTable(null)
            QShipmentEntity qShipment = new QShipmentEntity(null)
            session.insertEntity(qShipment, new ShipmentEntity(1L, "Package", null, []))
            session.insert()
                    .into("shipment_events")
                    .set(events.id, 100L)
                    .set(events.shipmentId, 1L)
                    .set(events.description, "Created")
                    .execute()
            session.insert()
                    .into("shipment_events")
                    .set(events.id, 101L)
                    .set(events.shipmentId, 1L)
                    .set(events.description, "Dispatched")
                    .execute()

        when:
            List<Record> records = session.select(events.allColumns)
                    .from(events)
                    .where(events.shipmentId.eq(1L))
                    .orderBy(events.id.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(events.description) == "Created"
            records[1].get(events.description) == "Dispatched"

        and: "lazy loading via the relation still works for the field-less child"
            ShipmentEntity shipment = session.selectEntity(qShipment).whereIdEq(1L).fetchSingle()
            shipment.events.size() == 2

        where:
            session << allSessions
    }
}
