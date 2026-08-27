// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.InvoiceEntryTable

class H2DialectDistinctIT extends H2DialectBaseIT {

    void setup() {
        loadScript("invoice.sql")
        loadScript("invoice-data.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "select distinct 1"() {
        given:
            InvoiceEntryTable entry = new InvoiceEntryTable(alias)

        when:
            List<Long> ids = session.selectDistinct(entry.invoiceId)
                    .from(entry)
                    .orderBy(entry.invoiceId.asc())
                    .fetchAll()

        then:
            ids.size() == 2
            ids[0] == 1L
            ids[1] == 2L

        where:
            alias << [null, "a"]
    }

    def "select distinct 2"() {
        given:
            InvoiceEntryTable entry = new InvoiceEntryTable(alias)

        when:
            List<Record> records = session.selectDistinct(entry.invoiceId, entry.name)
                    .from(entry)
                    .orderBy(entry.invoiceId.asc())
                    .fetchAll()

        then:
            records.size() == 5

        where:
            alias << [null, "a"]
    }
}
