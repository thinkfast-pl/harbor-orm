// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.h2.repository.set_one_to_many.QTransactionEntity
import io.github.thinkfastpl.harbororm.h2.repository.set_one_to_many.TransactionEntity

class H2DialectOneToManyIT extends H2DialectBaseIT {

    void setup() {
        loadScript("transaction.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "select"() {
        given:
            loadScript("transaction-data.sql")
            QTransactionEntity qTransactionEntity = new QTransactionEntity(alias)

        when:
            List<TransactionEntity> transactions = session.selectEntity(qTransactionEntity).fetchAll()

        then:
            transactions.size() == 2
            with(transactions[0]) { t ->
                t.id == 1
                t.name == 'Tra1'
                t.nodes.size() == 3
                with(t.nodes[0]) { n ->
                    n.id == 1
                    n.name == 'Node1'
                }
                with(t.nodes[1]) { n ->
                    n.id == 2
                    n.name == 'Node2'
                }
                with(t.nodes[2]) { n ->
                    n.id == 3
                    n.name == 'Node3'
                }
            }
            with(transactions[1]) { t ->
                t.id == 2
                t.name == 'Tra2'
                t.nodes.size() == 2
                with(t.nodes[0]) { n ->
                    n.id == 4
                    n.name == 'Node4'
                }
                with(t.nodes[1]) { n ->
                    n.id == 5
                    n.name == 'Node5'
                }
            }

        where:
            alias << [null, 'a']
    }

    def "delete"() {
        given:
            loadScript("transaction-data.sql")
            QTransactionEntity qTransactionEntity = new QTransactionEntity(alias)

        when:
            TransactionEntity transactionEntity = session.selectEntity(qTransactionEntity).whereIdEq(1L).fetchSingle()
            session.deleteEntity(qTransactionEntity, transactionEntity)

        then:
            session.selectEntity(qTransactionEntity).count() == 1

        where:
            alias << [null, 'a']
    }
}
