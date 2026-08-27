// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.test.domain.*
import io.github.thinkfastpl.harbororm.test.domain.dto.OrderState
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class EntityInsertIT extends AbstractHarborIT {

    def "insert role when ok"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            RoleEntity roleEntity = new RoleEntity(null, "My role", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT))

        when:
            session.insertEntity(qRoleEntity, roleEntity)

        then:
            roleEntity.id != null
            with(session.selectEntity(qRoleEntity).fetchAll()) { roles ->
                roles.size() == 1
                with(roles[0]) { role ->
                    role.id != null
                    role.id == roleEntity.id
                    role.name == "My role"
                    role.permissions.size() == 2
                    role.permissions[0] == RolePermission.PRODUCT_ADD
                    role.permissions[1] == RolePermission.PRODUCT_EDIT
                }
            }

        where:
            session << allSessions
    }

    def "insert product when ok"() {
        given:
            QProductEntity qProductEntity = new QProductEntity(null)
            ProductEntity productEntity = new ProductEntity(null, "My product", new BigDecimal("11.2"), new BigDecimal("22.3"), new BigDecimal("33.4"))

        when:
            session.insertEntity(qProductEntity, productEntity)

        then:
            productEntity.id != null
            with(session.selectEntity(qProductEntity).fetchAll()) { products ->
                products.size() == 1
                with(products[0]) { product ->
                    product.id != null
                    product.id == productEntity.id
                    product.name == "My product"
                    product.priceNet == new BigDecimal("11.2")
                    product.vatRate == new BigDecimal("22.3")
                    product.priceGross == new BigDecimal("33.4")
                }
            }

        where:
            session << allSessions
    }

    def "insert user when ok"() {
        given:
            UUID id = UUID.randomUUID()

            QUserEntity qUserEntity = new QUserEntity(null)
            UserEntity userEntity = new UserEntity(id, "test@example.com", "pwd", "First name", "Last name", "555444333")

        when:
            session.insertEntity(qUserEntity, userEntity)

        then:
            with(session.selectEntity(qUserEntity).fetchAll()) { users ->
                users.size() == 1
                with(users[0]) { user ->
                    user.id == id
                    user.email == "test@example.com"
                    user.password == "pwd"
                    user.firstName == "First name"
                    user.lastName == "Last name"
                    user.phoneNumber == "555444333"
                }
            }

        where:
            session << allSessions
    }

    def "insert order when ok"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)

            UUID userId = fixtures.addUser()
            Long productAId = fixtures.addProductA();
            Long productBId = fixtures.addProductB();

            QOrderEntity qOrderEntity = new QOrderEntity(null)

            Long id = 1000L

            OrderEntity orderEntity = new OrderEntity(
                    id,
                    userId,
                    OrderState.NEW,
                    new BigDecimal("111.23"),
                    new BigDecimal("136.81"),
                    new AddressValue(
                            "pomorskie", "81-225", "Gdynia", "Morska", "11", "32"
                    ),
                    new AddressValue(
                            "pomorskie", "81-225", "Gdynia", "Morska", "11", "32"
                    ),
                    List.of(
                            new OrderItemValue(productAId, BigDecimal.ONE, new BigDecimal("1.00"), new BigDecimal("1.00"), new BigDecimal("23"), new BigDecimal("1.23")),
                            new OrderItemValue(productBId, new BigDecimal("2"), new BigDecimal("1.50"), new BigDecimal("3.00"), new BigDecimal("23"), new BigDecimal("3.69")),
                    )
            )

        when:
            session.insertEntity(qOrderEntity, orderEntity)

        then:
            with(session.selectEntity(qOrderEntity).fetchAll()) { orders ->
                orders.size() == 1
                with(orders[0]) { order ->
                    order.id == id
                    order.userId == userId
                    order.state == OrderState.NEW
                    order.totalPriceNet == new BigDecimal("111.23")
                    order.totalPriceGross == new BigDecimal("136.81")
                    order.items.size() == 2
                    with(order.items[0]) { item ->
                        item.productId == productAId
                        item.amount == new BigDecimal("1.00")
                        item.unitPriceNet == new BigDecimal("1.00")
                        item.totalPriceNet == new BigDecimal("1.00")
                        item.vatRate == new BigDecimal("23")
                        item.totalPriceGross == new BigDecimal("1.23")
                    }
                    with(order.items[1]) { item ->
                        item.productId == productBId
                        item.amount == new BigDecimal("2")
                        item.unitPriceNet == new BigDecimal("1.50")
                        item.totalPriceNet == new BigDecimal("3.00")
                        item.vatRate == new BigDecimal("23")
                        item.totalPriceGross == new BigDecimal("3.69")
                    }
                }
            }

        where:
            session << allSessions
    }
}
