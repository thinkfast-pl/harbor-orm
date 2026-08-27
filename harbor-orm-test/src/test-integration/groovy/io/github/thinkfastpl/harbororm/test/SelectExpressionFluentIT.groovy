// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.*
import io.github.thinkfastpl.harbororm.api.metadata.QColumn
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.query.RolePermissionsTable
import io.github.thinkfastpl.harbororm.query.RolesTable
import io.github.thinkfastpl.harbororm.test.domain.QRoleEntity
import io.github.thinkfastpl.harbororm.test.domain.RoleEntity
import io.github.thinkfastpl.harbororm.test.domain.RolePermission
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

/**
 * Integration tests for FluentSelectExpression - the fluent API returned by DSL.select().
 *
 * These tests focus on exercising the SelectExpression interface methods directly,
 * testing the fluent chain from DSL.select() through to execution via derived tables
 * and subqueries.
 *
 * Tests cover:
 * - DSL.select() variants with derived table execution
 * - .select(expression) and .select(List) for adding columns
 * - .from(tableName) with string table name
 * - .from(QTable) with table metadata
 * - .join(), .leftJoin(), .rightJoin(), .crossJoin() with .on() conditions
 * - .where(condition), .where(List), .where(varargs)
 * - .groupBy(expression), .groupBy(List), .groupBy(varargs)
 * - .having(condition), .having(List), .having(varargs)
 * - .orderBy(order), .orderBy(List), .orderBy(varargs)
 * - .limit() and .offset()
 * - .forUpdate()
 * - .asTableSource() for derived tables
 * - .asNonFluent() for subquery usage
 * - Full fluent chains combining multiple operations
 */
class SelectExpressionFluentIT extends AbstractHarborIT {

    // ============================================
    // DSL.select() starting points
    // ============================================

    def "DSL.select() with explicit column selection builds query expression"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)

            BasicsTable basics = new BasicsTable("b")
            QColumn<Long> derivedId = QColumn.simple(Long.class, "sq", "id")
            QColumn<String> derivedName = QColumn.simple(String.class, "sq", "name")

        when: "using DSL.select() with explicit columns via derived table"
            def subquery = DSL.select(basics.id, basics.name)
                    .from(basics)
                    .asTableSource("sq")

            List<Record> records = session
                    .select(derivedId, derivedName)
                    .from(subquery)
                    .fetchAll()

        then:
            records.size() == 1
            records[0].get(derivedId) == 1L
            records[0].get(derivedName) == "Alice"

        where:
            session << allSessions
    }

    def "DSL.selectAsterisk() selects all columns for derived table"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Bob", 200)

            BasicsTable basics = new BasicsTable("b")
            QColumn<Long> derivedId = QColumn.simple(Long.class, "sq", "id")
            QColumn<String> derivedName = QColumn.simple(String.class, "sq", "name")

        when: "using DSL.selectAsterisk()"
            def subquery = DSL.selectAsterisk()
                    .from(basics)
                    .asTableSource("sq")

            List<Record> records = session
                    .select(derivedId, derivedName)
                    .from(subquery)
                    .fetchAll()

        then:
            records.size() == 1
            records[0].get(derivedId) == 1L
            records[0].get(derivedName) == "Bob"

        where:
            session << allSessions
    }

    def "DSL.selectDistinct() removes duplicate rows in subquery"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Bob", 300)

            BasicsTable basics = new BasicsTable("b")

        when: "using DSL.selectDistinct() in subquery"
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.selectDistinct(basics.name)
                                            .from(basics)
                            )
                    )
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // All 3 rows returned because both Alice and Bob are in the distinct subquery
            names.size() == 3
            names.count { it == "Alice" } == 2
            names.count { it == "Bob" } == 1

        where:
            session << allSessions
    }

    def "DSL.selectDistinctAsterisk() selects distinct rows with all columns"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 100)  // Different id
            fixtures.addBasic(3L, "Bob", 200)

            BasicsTable basics = new BasicsTable("b")
            QColumn<Long> derivedId = QColumn.simple(Long.class, "sq", "id")
            QColumn<String> derivedName = QColumn.simple(String.class, "sq", "name")

        when: "using DSL.selectDistinctAsterisk()"
            def subquery = DSL.selectDistinctAsterisk()
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .asTableSource("sq")

            List<Record> records = session
                    .select(derivedId, derivedName)
                    .from(subquery)
                    .fetchAll()

        then:
            // All rows are distinct (different ids)
            records.size() == 3

        where:
            session << allSessions
    }

    // ============================================
    // .select() - adding columns incrementally
    // ============================================

    def "select(expression) adds single column to selection"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)

            BasicsTable basics = new BasicsTable("b")
            QColumn<Long> derivedId = QColumn.simple(Long.class, "sq", "id")
            QColumn<String> derivedName = QColumn.simple(String.class, "sq", "name")

        when: "using chained select() calls"
            def subquery = DSL.select(basics.id)
                    .select(basics.name)  // Add another column
                    .from(basics)
                    .asTableSource("sq")

            List<Record> records = session
                    .select(derivedId, derivedName)
                    .from(subquery)
                    .fetchAll()

        then:
            records.size() == 1
            records[0].get(derivedId) == 1L
            records[0].get(derivedName) == "Alice"

        where:
            session << allSessions
    }

    def "select(List) adds multiple columns at once"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            BasicsTable basics = new BasicsTable("b")
            QColumn<Long> derivedId = QColumn.simple(Long.class, "sq", "id")
            QColumn<String> derivedName = QColumn.simple(String.class, "sq", "name")
            QColumn<Integer> derivedNumero = QColumn.simple(Integer.class, "sq", "numero")

        when: "using select(List)"
            List<Expression<?>> columns = [basics.id, basics.name, basics.numero]

            def subquery = DSL.select()
                    .select(columns)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .asTableSource("sq")

            List<Record> records = session
                    .select(derivedId, derivedName, derivedNumero)
                    .from(subquery)
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(derivedId) == 1L
            records[0].get(derivedName) == "Alice"
            records[0].get(derivedNumero) == 100
            records[1].get(derivedId) == 2L
            records[1].get(derivedName) == "Bob"

        where:
            session << allSessions
    }

    // ============================================
    // .from() variants
    // ============================================

    def "from(String tableName) uses table name directly"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)

            BasicsTable basics = new BasicsTable("b")
            QColumn<Long> idColumn = QColumn.simple(Long.class, null, "id")
            QColumn<String> nameColumn = QColumn.simple(String.class, null, "name")

        when: "using from(String) in subquery"
            List<Long> ids = session
                    .select(basics.id)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(idColumn)
                                            .from("basics")
                            )
                    )
                    .fetchAll()

        then:
            ids.size() == 1
            ids[0] == 1L

        where:
            session << allSessions
    }

    def "from(QTable) uses table metadata"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

        when: "using from(QTable) in subquery"
            List<Long> ids = session
                    .select(basics.id)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subqueryTable.id)
                                            .from(subqueryTable)
                            )
                    )
                    .fetchAll()

        then:
            ids.size() == 1
            ids[0] == 1L

        where:
            session << allSessions
    }

    // ============================================
    // JOIN operations with .on()
    // ============================================

    def "join() with on() performs inner join in subquery"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", [RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT]))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Viewer", []))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")
            BasicsTable basics = new BasicsTable("b")

            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Admin", 100)
            fixtures.addBasic(2L, "Viewer", 200)

        when: "using join().on() in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(roles.name)
                                            .from(roles)
                                            .join(permissions).on(roles.id.eq(permissions.roleId))
                            )
                    )
                    .fetchAll()

        then:
            // Only Admin has permissions (inner join excludes Viewer)
            records.size() == 1
            records[0].get(basics.name) == "Admin"

        where:
            session << allSessions
    }

    def "leftJoin() with on() performs left outer join in subquery"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", [RolePermission.PRODUCT_ADD]))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Viewer", []))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")
            BasicsTable basics = new BasicsTable("b")

            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Admin", 100)
            fixtures.addBasic(2L, "Viewer", 200)

        when: "using leftJoin().on() in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(roles.name)
                                            .from(roles)
                                            .leftJoin(permissions).on(roles.id.eq(permissions.roleId))
                            )
                    )
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // Left join includes all roles
            records.size() == 2
            records[0].get(basics.name) == "Admin"
            records[1].get(basics.name) == "Viewer"

        where:
            session << allSessions
    }

    def "rightJoin() with on() performs right outer join in subquery"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", [RolePermission.PRODUCT_ADD]))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Viewer", []))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")
            BasicsTable basics = new BasicsTable("b")

            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Admin", 100)
            fixtures.addBasic(2L, "Viewer", 200)

        when: "using rightJoin().on() in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(roles.name)
                                            .from(permissions)
                                            .rightJoin(roles).on(roles.id.eq(permissions.roleId))
                            )
                    )
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // Right join includes all roles
            records.size() == 2
            records[0].get(basics.name) == "Admin"
            records[1].get(basics.name) == "Viewer"

        where:
            session << allSessions
    }

    def "crossJoin() performs cross join in derived table"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)

            BasicsTable b1 = new BasicsTable("b1")
            BasicsTable b2 = new BasicsTable("b2")
            QColumn<String> name1 = QColumn.simple(String.class, "sq", "name1")
            QColumn<String> name2 = QColumn.simple(String.class, "sq", "name2")

        when: "using crossJoin() in derived table"
            def subquery = DSL.select(b1.name.as("name1"), b2.name.as("name2"))
                    .from(b1)
                    .crossJoin(b2.getTableName())  // crossJoin requires QTableSource
                    .orderBy(b1.id.asc(), b2.id.asc())
                    .asTableSource("sq")

            List<Record> records = session
                    .select(name1, name2)
                    .from(subquery)
                    .fetchAll()

        then:
            // Cross join: 2 x 2 = 4 records
            records.size() == 4

        where:
            session << allSessions
    }

    def "multiple joins can be chained in subquery"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", [RolePermission.PRODUCT_ADD]))

            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Admin", 100)
            fixtures.addBasic(2L, "Other", 200)

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")
            BasicsTable basicsInner = new BasicsTable("bi")
            BasicsTable basicsOuter = new BasicsTable("bo")

        when: "chaining multiple joins in subquery"
            List<Record> records = session
                    .select(basicsOuter.id, basicsOuter.name)
                    .from(basicsOuter)
                    .where(
                            basicsOuter.name.in(
                                    DSL.select(roles.name)
                                            .from(roles)
                                            .join(permissions).on(roles.id.eq(permissions.roleId))
                                            .join(basicsInner).on(roles.name.eq(basicsInner.name))
                            )
                    )
                    .fetchAll()

        then:
            records.size() == 1
            records[0].get(basicsOuter.name) == "Admin"

        where:
            session << allSessions
    }

    // ============================================
    // .where() variants
    // ============================================

    def "where(Condition) filters subquery results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

        when: "using where(Condition) in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subqueryTable.id)
                                            .from(subqueryTable)
                                            .where(subqueryTable.numero.gt(150))
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(basics.name) == "Bob"
            records[1].get(basics.name) == "Charlie"

        where:
            session << allSessions
    }

    def "where(List) applies multiple conditions with AND in subquery"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

        when: "using where(List) in subquery"
            def conditions = [
                    subqueryTable.numero.gt(50),
                    subqueryTable.numero.lt(250)
            ]

            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subqueryTable.id)
                                            .from(subqueryTable)
                                            .where(conditions)
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Bob"

        where:
            session << allSessions
    }

    def "where(varargs) applies multiple conditions in subquery"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

        when: "using where(varargs) in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subqueryTable.id)
                                            .from(subqueryTable)
                                            .where(subqueryTable.numero.ge(100), subqueryTable.numero.le(200))
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Bob"

        where:
            session << allSessions
    }

    // ============================================
    // .groupBy() variants
    // ============================================

    def "groupBy(Expression) groups subquery results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Bob", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

        when: "using groupBy(Expression) in subquery"
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(subqueryTable.name)
                                            .from(subqueryTable)
                                            .groupBy(subqueryTable.name)
                            )
                    )
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // All records match (groupBy produces distinct names)
            names.size() == 3
            names.count { it == "Alice" } == 2
            names.count { it == "Bob" } == 1

        where:
            session << allSessions
    }

    def "groupBy(List) groups by multiple expressions in subquery"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 100)
            fixtures.addBasic(3L, "Alice", 200)
            fixtures.addBasic(4L, "Bob", 100)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")
            Expression<Long> countExpr = DSL.count()

        when: "using groupBy(List) in subquery - get names that have more than 1 record for same numero"
            List<Expression<?>> groupByExprs = [subqueryTable.name, subqueryTable.numero]

            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(subqueryTable.name)
                                            .from(subqueryTable)
                                            .groupBy(groupByExprs)
                                            .having(countExpr.gt(1L))
                            )
                    )
                    .fetchAll()

        then:
            // Only Alice with numero=100 has count > 1
            names.size() == 3
            names.every { it == "Alice" }

        where:
            session << allSessions
    }

    def "groupBy(varargs) groups by multiple expressions in subquery"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 100)
            fixtures.addBasic(3L, "Bob", 200)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")
            Expression<Long> countExpr = DSL.count()

        when: "using groupBy(varargs) in subquery"
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(subqueryTable.name)
                                            .from(subqueryTable)
                                            .groupBy(subqueryTable.name, subqueryTable.numero)
                                            .having(countExpr.gt(1L))
                            )
                    )
                    .fetchAll()

        then:
            // Only Alice with numero=100 has count > 1
            names.size() == 2
            names.every { it == "Alice" }

        where:
            session << allSessions
    }

    // ============================================
    // .having() variants
    // ============================================

    def "having(Condition) filters grouped subquery results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Bob", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")
            Expression<Long> countExpr = DSL.count()

        when: "using having(Condition) in subquery"
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(subqueryTable.name)
                                            .from(subqueryTable)
                                            .groupBy(subqueryTable.name)
                                            .having(countExpr.gt(1L))
                            )
                    )
                    .fetchAll()

        then:
            // Only Alice has count > 1
            names.size() == 2
            names.every { it == "Alice" }

        where:
            session << allSessions
    }

    def "having(List) applies multiple HAVING conditions in subquery"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Alice", 300)
            fixtures.addBasic(4L, "Bob", 400)
            fixtures.addBasic(5L, "Bob", 500)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")
            Expression<Long> countExpr = DSL.count()
            Expression<Integer> sumExpr = DSL.sum(subqueryTable.numero)

        when: "using having(List) in subquery"
            def havingConditions = [
                    countExpr.ge(2L),
                    sumExpr.gt(400)
            ]

            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(subqueryTable.name)
                                            .from(subqueryTable)
                                            .groupBy(subqueryTable.name)
                                            .having(havingConditions)
                            )
                    )
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // Alice: count=3, sum=600 - matches both
            // Bob: count=2, sum=900 - matches both
            names.size() == 5  // 3 Alice + 2 Bob

        where:
            session << allSessions
    }

    def "having(varargs) applies multiple HAVING conditions in subquery"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Bob", 50)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")
            Expression<Long> countExpr = DSL.count()
            Expression<Integer> sumExpr = DSL.sum(subqueryTable.numero)

        when: "using having(varargs) in subquery"
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(subqueryTable.name)
                                            .from(subqueryTable)
                                            .groupBy(subqueryTable.name)
                                            .having(countExpr.ge(1L), sumExpr.gt(100))
                            )
                    )
                    .fetchAll()

        then:
            // Alice: count=2, sum=300 - matches
            // Bob: count=1, sum=50 - sum not > 100
            names.size() == 2
            names.every { it == "Alice" }

        where:
            session << allSessions
    }

    // ============================================
    // .orderBy() variants
    // ============================================

    def "orderBy(Order) orders subquery results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Charlie", 300)
            fixtures.addBasic(2L, "Alice", 100)
            fixtures.addBasic(3L, "Bob", 200)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

        when: "using orderBy(Order) in subquery with limit"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subqueryTable.id)
                                            .from(subqueryTable)
                                            .orderBy(subqueryTable.name.asc())
                                            .limit(2)
                            )
                    )
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // Subquery orders by name asc and takes first 2: Alice (id=2), Bob (id=3)
            records.size() == 2
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Bob"

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "orderBy(Order) orders subquery results - no support for 'LIMIT & IN/ALL/ANY/SOME subquery'"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Charlie", 300)
            fixtures.addBasic(2L, "Alice", 100)
            fixtures.addBasic(3L, "Bob", 200)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

            SelectExpressionTableSource limitedIdsTable = DSL.select(subqueryTable.id)
                    .from(subqueryTable)
                    .orderBy(subqueryTable.name.asc())
                    .limit(2)
                    .asTableSource("limited_ids")

            QColumn<Long> limitedId = limitedIdsTable.getColumn(subqueryTable.id, "id")

        when: "using orderBy(Order) in subquery with limit"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(limitedId).from(limitedIdsTable)
                            )
                    )
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // Subquery orders by name asc and takes first 2: Alice (id=2), Bob (id=3)
            records.size() == 2
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Bob"

        where:
            session << allSessions
    }

    def "orderBy(List) orders by multiple columns in subquery"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 200)
            fixtures.addBasic(2L, "Alice", 100)
            fixtures.addBasic(3L, "Bob", 150)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

        when: "using orderBy(List) in subquery with limit"
            List<Order> orders = [subqueryTable.name.asc(), subqueryTable.numero.desc()]

            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subqueryTable.id)
                                            .from(subqueryTable)
                                            .orderBy(orders)
                                            .limit(2)
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // Subquery: Alice with 200 first (name asc, numero desc), then Alice with 100
            records.size() == 2
            records[0].get(basics.id) == 1L  // Alice, 200
            records[1].get(basics.id) == 2L  // Alice, 100

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "orderBy(List) orders by multiple columns in subquery - no support for 'LIMIT & IN/ALL/ANY/SOME subquery'"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 200)
            fixtures.addBasic(2L, "Alice", 100)
            fixtures.addBasic(3L, "Bob", 150)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")
            List<Order> orders = [subqueryTable.name.asc(), subqueryTable.numero.desc()]

            SelectExpressionTableSource limitedIdsTable = DSL.select(subqueryTable.id)
                    .from(subqueryTable)
                    .orderBy(orders)
                    .limit(2)
                    .asTableSource("limited_ids")

            QColumn<Long> limitedId = limitedIdsTable.getColumn(subqueryTable.id, "id")

        when: "using orderBy(List) in subquery with limit"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(limitedId).from(limitedIdsTable)
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // Subquery: Alice with 200 first (name asc, numero desc), then Alice with 100
            records.size() == 2
            records[0].get(basics.id) == 1L  // Alice, 200
            records[1].get(basics.id) == 2L  // Alice, 100

        where:
            session << allSessions
    }

    def "orderBy(varargs) orders by multiple columns in subquery"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Bob", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Alice", 100)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

        when: "using orderBy(varargs) in subquery with limit"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subqueryTable.id)
                                            .from(subqueryTable)
                                            .orderBy(subqueryTable.name.asc(), subqueryTable.numero.asc())
                                            .limit(2)
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // Subquery: Alice 100 (id=3), Alice 200 (id=2)
            records.size() == 2
            records[0].get(basics.id) == 2L
            records[1].get(basics.id) == 3L

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "orderBy(varargs) orders by multiple columns in subquery - no support for 'LIMIT & IN/ALL/ANY/SOME subquery'"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Bob", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Alice", 100)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

            SelectExpressionTableSource limitedIdsTable = DSL.select(subqueryTable.id)
                    .from(subqueryTable)
                    .orderBy(subqueryTable.name.asc(), subqueryTable.numero.asc())
                    .limit(2)
                    .asTableSource("limited_ids")

            QColumn<Long> limitedId = limitedIdsTable.getColumn(subqueryTable.id, "id")

        when: "using orderBy(varargs) in subquery with limit"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(limitedId).from(limitedIdsTable)
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // Subquery: Alice 100 (id=3), Alice 200 (id=2)
            records.size() == 2
            records[0].get(basics.id) == 2L
            records[1].get(basics.id) == 3L

        where:
            session << allSessions
    }

    def "orderBy with DSL.asc/desc by column index in derived table"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Charlie", 300)
            fixtures.addBasic(2L, "Alice", 100)
            fixtures.addBasic(3L, "Bob", 200)

            BasicsTable basics = new BasicsTable("b")
            QColumn<Long> derivedId = QColumn.simple(Long.class, "sq", "id")
            QColumn<String> derivedName = QColumn.simple(String.class, "sq", "name")

        when: "using DSL.asc with column index in derived table"
            def subquery = DSL.select(basics.id, basics.name)
                    .from(basics)
                    .orderBy(DSL.asc(2))  // Order by second column (name)
                    .limit(2)
                    .asTableSource("sq")

            List<Record> records = session
                    .select(derivedId, derivedName)
                    .from(subquery)
                    .fetchAll()

        then:
            // Subquery orders by name asc: Alice (id=2), Bob (id=3)
            records.size() == 2
            records[0].get(derivedName) == "Alice"
            records[1].get(derivedName) == "Bob"

        where:
            session << allSessions
    }

    // ============================================
    // .limit() and .offset()
    // ============================================

    def "limit() restricts number of subquery results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

        when: "using limit() in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subqueryTable.id)
                                            .from(subqueryTable)
                                            .orderBy(subqueryTable.id.asc())
                                            .limit(2)
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Bob"

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "limit() restricts number of subquery results - no support for 'LIMIT & IN/ALL/ANY/SOME subquery'"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

            SelectExpressionTableSource limitedIdsTable = DSL.select(subqueryTable.id)
                    .from(subqueryTable)
                    .orderBy(subqueryTable.id.asc())
                    .limit(2)
                    .asTableSource("limited_ids")

            QColumn<Long> limitedId = limitedIdsTable.getColumn(subqueryTable.id, "id")

        when: "using limit() in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(limitedId).from(limitedIdsTable)
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Bob"

        where:
            session << allSessions
    }

    def "offset() skips initial subquery results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

        when: "using limit() with offset() in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subqueryTable.id)
                                            .from(subqueryTable)
                                            .orderBy(subqueryTable.id.asc())
                                            .limit(2)
                                            .offset(1)
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // Subquery returns ids 2, 3 (offset 1, limit 2)
            records.size() == 2
            records[0].get(basics.name) == "Bob"
            records[1].get(basics.name) == "Charlie"

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "offset() skips initial subquery results - no support for 'LIMIT & IN/ALL/ANY/SOME subquery'"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

            SelectExpressionTableSource limitedIdsTable = DSL.select(subqueryTable.id)
                    .from(subqueryTable)
                    .orderBy(subqueryTable.id.asc())
                    .limit(2)
                    .offset(1)
                    .asTableSource("limited_ids")

            QColumn<Long> limitedId = limitedIdsTable.getColumn(subqueryTable.id, "id")

        when: "using limit() with offset() in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(limitedId).from(limitedIdsTable)
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // Subquery returns ids 2, 3 (offset 1, limit 2)
            records.size() == 2
            records[0].get(basics.name) == "Bob"
            records[1].get(basics.name) == "Charlie"

        where:
            session << allSessions
    }

    // ============================================
    // forUpdate()
    // ============================================

    def "forUpdate() adds FOR UPDATE clause to derived table"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)

            BasicsTable basics = new BasicsTable("b")
            QColumn<Long> derivedId = QColumn.simple(Long.class, "sq", "id")
            QColumn<String> derivedName = QColumn.simple(String.class, "sq", "name")

        when: "using forUpdate() in derived table"
            def subquery = DSL.select(basics.id, basics.name)
                    .from(basics)
                    .forUpdate()
                    .asTableSource("sq")

            List<Record> records = session
                    .select(derivedId, derivedName)
                    .from(subquery)
                    .fetchAll()

        then:
            records.size() == 1
            records[0].get(derivedName) == "Alice"

        where:
            session << allSessions
    }

    // ============================================
    // Full fluent chains
    // ============================================

    def "full fluent chain: select, from, where, groupBy, having, orderBy, limit, offset in subquery"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "A", 20)
            fixtures.addBasic(3L, "A", 30)
            fixtures.addBasic(4L, "B", 15)
            fixtures.addBasic(5L, "B", 25)
            fixtures.addBasic(6L, "C", 100)
            fixtures.addBasic(7L, "D", 5)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")
            Expression<Long> countExpr = DSL.count()

        when: "chaining all fluent steps in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(subqueryTable.name)
                                            .from(subqueryTable)
                                            .where(subqueryTable.numero.gt(5))           // Exclude D (numero=5)
                                            .groupBy(subqueryTable.name)
                                            .having(countExpr.ge(2L))             // Exclude C (count=1)
                                            .orderBy(subqueryTable.name.desc())
                                            .limit(10)
                                            .offset(0)
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // Subquery: WHERE numero > 5, GROUP BY name, HAVING count >= 2
            // Names with count >= 2: A (3 records), B (2 records)
            records.size() == 5
            records[0].get(basics.name) == "A"
            records[1].get(basics.name) == "A"
            records[2].get(basics.name) == "A"
            records[3].get(basics.name) == "B"
            records[4].get(basics.name) == "B"

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "full fluent chain: select, from, where, groupBy, having, orderBy, limit, offset in subquery - no support for 'LIMIT & IN/ALL/ANY/SOME subquery'"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "A", 20)
            fixtures.addBasic(3L, "A", 30)
            fixtures.addBasic(4L, "B", 15)
            fixtures.addBasic(5L, "B", 25)
            fixtures.addBasic(6L, "C", 100)
            fixtures.addBasic(7L, "D", 5)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")
            Expression<Long> countExpr = DSL.count()

            SelectExpressionTableSource limitedNamesTable = DSL.select(subqueryTable.name)
                    .from(subqueryTable)
                    .where(subqueryTable.numero.gt(5))           // Exclude D (numero=5)
                    .groupBy(subqueryTable.name)
                    .having(countExpr.ge(2L))             // Exclude C (count=1)
                    .orderBy(subqueryTable.name.desc())
                    .limit(10)
                    .offset(0)
                    .asTableSource("limited_names")

            QColumn<String> limitedName = limitedNamesTable.getColumn(subqueryTable.name, "name")

        when: "chaining all fluent steps in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(limitedName).from(limitedNamesTable)
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // Subquery: WHERE numero > 5, GROUP BY name, HAVING count >= 2
            // Names with count >= 2: A (3 records), B (2 records)
            records.size() == 5
            records[0].get(basics.name) == "A"
            records[1].get(basics.name) == "A"
            records[2].get(basics.name) == "A"
            records[3].get(basics.name) == "B"
            records[4].get(basics.name) == "B"

        where:
            session << allSessions
    }

    def "fluent chain with joins and complex conditions in subquery"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", [RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT]))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Editor", [RolePermission.PRODUCT_EDIT]))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Viewer", []))

            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Admin", 100)
            fixtures.addBasic(2L, "Editor", 200)
            fixtures.addBasic(3L, "Viewer", 300)

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")
            BasicsTable basics = new BasicsTable("b")
            Expression<Long> countExpr = DSL.count()

        when: "chaining with joins in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(roles.name)
                                            .from(roles)
                                            .leftJoin(permissions).on(roles.id.eq(permissions.roleId))
                                            .where(roles.name.notEq("Viewer"))
                                            .groupBy(roles.name)
                                            .having(countExpr.ge(1L))
                                            .orderBy(countExpr.desc())
                            )
                    )
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // Admin: 2 permissions, Editor: 1 permission
            records.size() == 2
            records[0].get(basics.name) == "Admin"
            records[1].get(basics.name) == "Editor"

        where:
            session << allSessions
    }

    // ============================================
    // asTableSource() and asNonFluent()
    // ============================================

    def "asTableSource() creates derived table usable in FROM"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            BasicsTable basics = new BasicsTable("b")
            QColumn<Long> derivedId = QColumn.simple(Long.class, "derived", "id")
            QColumn<String> derivedName = QColumn.simple(String.class, "derived", "name")

        when: "using asTableSource()"
            def derivedTable = DSL.select(basics.id, basics.name)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .asTableSource("derived")

            List<Record> records = session
                    .select(derivedId, derivedName)
                    .from(derivedTable)
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(derivedName) == "Alice"
            records[1].get(derivedName) == "Bob"

        where:
            session << allSessions
    }

    def "asNonFluent() converts to SelectExpression for subquery usage"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            BasicsTable basics = new BasicsTable("b")

        when: "using asNonFluent()"
            SelectExpression.CompleteStep<String> fluentQuery = DSL.select(basics.name)
                    .from(basics)
                    .orderBy(basics.name.asc())

            SelectExpression<String> nonFluentQuery = fluentQuery.asNonFluent()

            // Non-fluent can be used in subqueries
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(basics.name.in(nonFluentQuery))
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            names.size() == 2
            names[0] == "Alice"
            names[1] == "Bob"

        where:
            session << allSessions
    }

    // ============================================
    // Edge cases
    // ============================================

    def "empty result set from subquery matching nothing"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

        when: "subquery matches nothing"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subqueryTable.id)
                                            .from(subqueryTable)
                                            .where(subqueryTable.numero.gt(9999))
                            )
                    )
                    .fetchAll()

        then:
            records.isEmpty()

        where:
            session << allSessions
    }

    def "limit(0) in subquery returns no matches"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

        when: "using limit(0) in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subqueryTable.id)
                                            .from(subqueryTable)
                                            .limit(0)
                            )
                    )
                    .fetchAll()

        then:
            records.isEmpty()

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "limit(0) in subquery returns no matches - no support for 'LIMIT & IN/ALL/ANY/SOME subquery'"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryTable = new BasicsTable("sq")

            SelectExpressionTableSource limitedIdsTable = DSL.select(subqueryTable.id)
                    .from(subqueryTable)
                    .limit(0)
                    .asTableSource("limited_ids")

            QColumn<Long> limitedId = limitedIdsTable.getColumn(subqueryTable.id, "id")

        when: "using limit(0) in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(limitedId).from(limitedIdsTable)
                            )
                    )
                    .fetchAll()

        then:
            records.isEmpty()

        where:
            session << allSessions
    }
}
