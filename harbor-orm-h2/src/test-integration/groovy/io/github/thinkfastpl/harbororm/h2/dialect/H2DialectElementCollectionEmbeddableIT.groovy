// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.h2.repository.set6.InvoiceEntity
import io.github.thinkfastpl.harbororm.h2.repository.set6.InvoiceEntry
import io.github.thinkfastpl.harbororm.h2.repository.set6.QInvoiceEntity

class H2DialectElementCollectionEmbeddableIT extends H2DialectBaseIT {

    void setup() {
        loadScript("invoice.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "insert"() {
        given:
            QInvoiceEntity qInvoiceEntity = new QInvoiceEntity(alias)
            InvoiceEntity entity = new InvoiceEntity(
                    1L,
                    "II",
                    List.of(
                            new InvoiceEntry("EE", new BigDecimal("123.4"), new BigDecimal("10")),
                            new InvoiceEntry("EF", new BigDecimal("123.5"), new BigDecimal("11")),
                    )
            )

        when:
            session.insertEntity(qInvoiceEntity, entity)

        then:
            session.selectEntity(qInvoiceEntity).count() == 1
            with(session.selectEntity(qInvoiceEntity).whereIdEq(1L).fetchSingle()) { i ->
                i.id == 1
                i.name == 'II'
                i.entries.size() == 2
                with(i.entries[0]) { e ->
                    e.name == 'EE'
                    e.price == new BigDecimal("123.4")
                    e.amount == new BigDecimal("10")
                }
                with(i.entries[1]) { e ->
                    e.name == 'EF'
                    e.price == new BigDecimal("123.5")
                    e.amount == new BigDecimal("11")
                }
            }

        where:
            alias << [null, 'a']
    }

    def "select"() {
        given:
            loadScript("invoice-data.sql")
            QInvoiceEntity qInvoiceEntity = new QInvoiceEntity(alias)

        when:
            List<InvoiceEntity> invoices = session.selectEntity(qInvoiceEntity).fetchAll()

        then:
            invoices.size() == 2
            with(invoices[0]) { i ->
                i.id == 1
                i.name == 'Inv1'
                i.entries.size() == 3
                with(i.entries[0]) { e ->
                    e.name == 'E1'
                    e.price == new BigDecimal("55.1")
                    e.amount == new BigDecimal("12")
                }
                with(i.entries[1]) { e ->
                    e.name == 'E2'
                    e.price == new BigDecimal("65.1")
                    e.amount == new BigDecimal("13")
                }
                with(i.entries[2]) { e ->
                    e.name == 'E3'
                    e.price == new BigDecimal("75.1")
                    e.amount == new BigDecimal("14")
                }
            }
            with(invoices[1]) { i ->
                i.id == 2
                i.name == 'Inv2'
                i.entries.size() == 2
                with(i.entries[0]) { e ->
                    e.name == 'E4'
                    e.price == new BigDecimal("85.1")
                    e.amount == new BigDecimal("15")
                }
                with(i.entries[1]) { e ->
                    e.name == 'E5'
                    e.price == new BigDecimal("71.1")
                    e.amount == new BigDecimal("16")
                }
            }

        where:
            alias << [null, 'a']
    }

    def "update"() {
        given:
            loadScript("invoice-data.sql")
            QInvoiceEntity qInvoiceEntity = new QInvoiceEntity(alias)

        when:
            InvoiceEntity entity = session.selectEntity(qInvoiceEntity).whereIdEq(1L).fetchSingle()
            entity.setName("E11")
            entity.setEntries(List.of(
                    new InvoiceEntry("EE", new BigDecimal("123.4"), new BigDecimal("10")),
            ))
            session.updateEntity(qInvoiceEntity, entity)

        then:
            with(session.selectEntity(qInvoiceEntity).whereIdEq(1L).fetchSingle()) { i ->
                i.id == 1
                i.name == 'E11'
                i.entries.size() == 1
                with(i.entries[0]) { e ->
                    e.name == 'EE'
                    e.price == new BigDecimal("123.4")
                    e.amount == new BigDecimal("10")
                }
            }

        where:
            alias << [null, 'a']
    }

    def "delete"() {
        given:
            loadScript("invoice-data.sql")
            QInvoiceEntity qInvoiceEntity = new QInvoiceEntity(alias)

        when:
            InvoiceEntity entity = session.selectEntity(qInvoiceEntity).whereIdEq(1L).fetchSingle()
            session.deleteEntity(qInvoiceEntity, entity)

        then:
            session.selectEntity(qInvoiceEntity).count() == 1
            session.selectEntity(qInvoiceEntity).whereIdEq(1L).fetchOne().isEmpty()

        where:
            alias << [null, 'a']
    }
}
