// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.query.RolePermissionsTable
import io.github.thinkfastpl.harbororm.test.domain.*
import io.github.thinkfastpl.harbororm.test.domain.dto.OrderState
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

/**
 * Regression coverage for {@code IN(...)} on columns whose database type is a
 * native PostgreSQL {@code enum} declared via
 * {@code @Type(dialect = POSTGRES, columnType = "<enum_type>")}.
 */
class NativeEnumInIT extends AbstractHarborIT {

    def "in() on a column of a native PostgreSQL enum type returns matching rows"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin",
                    [RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT]))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Viewer",
                    [RolePermission.PRODUCT_DELETE]))

            RolePermissionsTable permissions = new RolePermissionsTable(null)

        when:
            List<RolePermission> matching = session
                    .select(permissions.permission)
                    .from(permissions)
                    .where(permissions.permission.in(
                            List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_DELETE)))
                    .orderBy(permissions.permission.asc())
                    .fetchAll()

        then:
            matching == [RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_DELETE]

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }

    def "in() on entity column of a native PostgreSQL enum type returns matching rows"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            UUID userId = fixtures.addUser()
            Long productAId = fixtures.addProductA()

            QOrderEntity qOrderEntity = new QOrderEntity(null)

            AddressValue addr = new AddressValue("pomorskie", "81-225",
                    "Gdynia", "Morska", "11", "32")
            OrderItemValue item = new OrderItemValue(productAId, BigDecimal.ONE,
                    new BigDecimal("1.00"), new BigDecimal("1.00"),
                    new BigDecimal("23"), new BigDecimal("1.23"))

            session.insertEntity(qOrderEntity, new OrderEntity(
                    session.select(DSL.nextval("orders_id_seq")).fetchSingle(),
                    userId, OrderState.NEW,
                    new BigDecimal("1.00"), new BigDecimal("1.23"),
                    addr, addr, List.of(item)))
            session.insertEntity(qOrderEntity, new OrderEntity(
                    session.select(DSL.nextval("orders_id_seq")).fetchSingle(),
                    userId, OrderState.PROCESSING,
                    new BigDecimal("1.00"), new BigDecimal("1.23"),
                    addr, addr, List.of(item)))
            session.insertEntity(qOrderEntity, new OrderEntity(
                    session.select(DSL.nextval("orders_id_seq")).fetchSingle(),
                    userId, OrderState.SENT,
                    new BigDecimal("1.00"), new BigDecimal("1.23"),
                    addr, addr, List.of(item)))

        when:
            List<OrderState> states = session.selectEntity(qOrderEntity)
                    .where(qOrderEntity.state.in(List.of(OrderState.NEW, OrderState.SENT)))
                    .orderBy(qOrderEntity.state.asc())
                    .fetchAll()
                    .collect { it.state }

        then:
            states == [OrderState.NEW, OrderState.SENT]

        where:
            session << getSessionsExcept(DbType.H2, DbType.MARIADB, DbType.MYSQL)
    }
}
