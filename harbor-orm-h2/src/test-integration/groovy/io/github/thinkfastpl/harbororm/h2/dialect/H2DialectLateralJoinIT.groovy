// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.SelectExpressionTableSource
import io.github.thinkfastpl.harbororm.query.RolePermissionTable
import io.github.thinkfastpl.harbororm.query.RoleTable

class H2DialectLateralJoinIT extends H2DialectBaseIT {

    void setup() {
        loadScript("role.sql")
        loadScript("role-data.sql")
    }

    void cleanup() {
        dropAllObjects()
    }

    def "innerJoinLateral throws UnsupportedOperationException on H2"() {
        given:
            RoleTable role = new RoleTable("r")
            RolePermissionTable permission = new RolePermissionTable("rp")
            SelectExpressionTableSource subquery = DSL
                    .select(permission.permission)
                    .from(permission)
                    .where(permission.roleId.eq(role.id))
                    .limit(1)
                    .asTableSource("lat")

        when:
            session.select(role.name)
                    .from(role)
                    .innerJoinLateral(subquery).on(DSL.TRUE)
                    .fetchAll()

        then:
            def ex = thrown(UnsupportedOperationException)
            ex.message == "LATERAL JOIN is not supported by H2 dialect"
    }

    def "leftJoinLateral throws UnsupportedOperationException on H2"() {
        given:
            RoleTable role = new RoleTable("r")
            RolePermissionTable permission = new RolePermissionTable("rp")
            SelectExpressionTableSource subquery = DSL
                    .select(permission.permission)
                    .from(permission)
                    .where(permission.roleId.eq(role.id))
                    .limit(1)
                    .asTableSource("lat")

        when:
            session.select(role.name)
                    .from(role)
                    .leftJoinLateral(subquery).on(DSL.TRUE)
                    .fetchAll()

        then:
            def ex = thrown(UnsupportedOperationException)
            ex.message == "LATERAL JOIN is not supported by H2 dialect"
    }

    def "crossJoinLateral throws UnsupportedOperationException on H2"() {
        given:
            RoleTable role = new RoleTable("r")
            RolePermissionTable permission = new RolePermissionTable("rp")
            SelectExpressionTableSource subquery = DSL
                    .select(permission.permission)
                    .from(permission)
                    .where(permission.roleId.eq(role.id))
                    .limit(1)
                    .asTableSource("lat")

        when:
            session.select(role.name)
                    .from(role)
                    .crossJoinLateral(subquery)
                    .fetchAll()

        then:
            def ex = thrown(UnsupportedOperationException)
            ex.message == "LATERAL JOIN is not supported by H2 dialect"
    }
}
