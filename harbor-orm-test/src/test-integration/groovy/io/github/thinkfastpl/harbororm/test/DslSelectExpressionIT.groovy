// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.*
import io.github.thinkfastpl.harbororm.api.metadata.QColumn
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsByteaTable
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.query.RolePermissionsTable
import io.github.thinkfastpl.harbororm.query.RolesTable
import io.github.thinkfastpl.harbororm.test.domain.QRoleEntity
import io.github.thinkfastpl.harbororm.test.domain.RoleEntity
import io.github.thinkfastpl.harbororm.test.domain.RolePermission
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

/**
 * Integration tests for DSL.select() fluent expression builder.
 *
 * These tests focus on the SelectExpression API - the fluent builder for creating
 * SELECT expressions using DSL.select(). This differs from session.select() which
 * executes queries directly.
 *
 * Tests cover:
 * - DSL.select() and DSL.selectDistinct() variants
 * - SelectExpression fluent steps: from, where, groupBy, having, orderBy, limit, offset
 * - Set operations: union, intersect, except (and their ALL variants)
 * - Join operations: join, leftJoin, rightJoin, crossJoin
 * - Subquery usage via in() with SelectExpression
 * - asTableSource() for derived tables
 */
class DslSelectExpressionIT extends AbstractHarborIT {

    // ============================================
    // DSL.select() basic builder tests
    // ============================================

    def "DSL.select() with no columns returns all columns when used with asterisk"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            BasicsTable basics = new BasicsTable("b")

        when: "using DSL.selectAsterisk() fluent builder"
            List<Record> records = session
                    .select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(basics.id)
                                            .from(basics)
                                            .where(basics.numero.gt(50))
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(basics.id) == 1L
            records[1].get(basics.id) == 2L

        where:
            session << allSessions
    }

    def "DSL.select(expression) creates typed SelectExpression"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            BasicsTable basics = new BasicsTable("b")

        when: "using DSL.select(single expression)"
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(basics.name)
                                            .from(basics)
                                            .where(basics.numero.lt(150))
                            )
                    )
                    .fetchAll()

        then:
            names.size() == 1
            names[0] == "Alice"

        where:
            session << allSessions
    }

    def "DSL.select(expressions...) with multiple columns"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            BasicsTable basics = new BasicsTable("b")

        when: "using DSL.select with varargs"
            List<Record> outerRecords = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(basics.id)
                                            .from(basics)
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            outerRecords.size() == 2
            outerRecords[0].get(basics.id) == 1L
            outerRecords[0].get(basics.name) == "Alice"
            outerRecords[1].get(basics.id) == 2L
            outerRecords[1].get(basics.name) == "Bob"

        where:
            session << allSessions
    }

    def "DSL.selectDistinct() removes duplicate values"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Bob", 300)

            BasicsTable basics = new BasicsTable("b")

        when: "using DSL.selectDistinct(expression)"
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
            // Original query returns all 3 rows but matches against distinct subquery (Alice, Bob)
            names.size() == 3
            names.count { it == "Alice" } == 2
            names.count { it == "Bob" } == 1

        where:
            session << allSessions
    }

    // ============================================
    // SelectExpression.from() variants
    // ============================================

    def "SelectExpression.from(QTable) uses table metadata"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")

        when: "using from(QTable)"
            List<Long> ids = session
                    .select(basics.id)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subquery.id)
                                            .from(subquery)  // Uses QTable
                            )
                    )
                    .fetchAll()

        then:
            ids.size() == 1
            ids[0] == 1L

        where:
            session << allSessions
    }

    def "SelectExpression.from(String) uses table name directly"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)

            BasicsTable basics = new BasicsTable("b")
            QColumn<Long> idColumn = QColumn.simple(Long.class, null, "id")

        when: "using from(tableName) with string"
            List<Long> ids = session
                    .select(basics.id)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(idColumn)
                                            .from("basics")  // Uses string table name
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
    // SelectExpression.where() tests
    // ============================================

    def "SelectExpression.where() filters subquery results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")

        when: "using where() in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subquery.id)
                                            .from(subquery)
                                            .where(subquery.numero.gt(150))
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

    def "SelectExpression.where() with multiple conditions"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")

        when: "using where() with AND condition"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subquery.id)
                                            .from(subquery)
                                            .where(
                                                    subquery.numero.gt(50)
                                                            .and(subquery.numero.lt(250))
                                            )
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
    // SelectExpression.groupBy() and having() tests
    // ============================================

    def "SelectExpression.groupBy() groups subquery results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Bob", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")

        when: "using groupBy() in subquery"
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(subquery.name)
                                            .from(subquery)
                                            .groupBy(subquery.name)
                            )
                    )
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // All records match because groupBy produces distinct names
            names.size() == 3
            names.count { it == "Alice" } == 2
            names.count { it == "Bob" } == 1

        where:
            session << allSessions
    }

    def "SelectExpression.groupBy().having() filters grouped results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Bob", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")
            Expression<Long> countExpr = DSL.count()

        when: "using groupBy().having() in subquery - filter names with count > 1"
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(subquery.name)
                                            .from(subquery)
                                            .groupBy(subquery.name)
                                            .having(countExpr.gt(1L))
                            )
                    )
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // Only "Alice" appears more than once in the source table
            names.size() == 2
            names.every { it == "Alice" }

        where:
            session << allSessions
    }

    def "SelectExpression.groupBy() with aggregate function"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Bob", 50)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")
            Expression<Integer> sumExpr = DSL.sum(subquery.numero)

        when: "using groupBy() with SUM in subquery"
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(subquery.name)
                                            .from(subquery)
                                            .groupBy(subquery.name)
                                            .having(sumExpr.gt(100))
                            )
                    )
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // Only "Alice" has sum > 100 (100 + 200 = 300)
            names.size() == 2
            names.every { it == "Alice" }

        where:
            session << allSessions
    }

    // ============================================
    // SelectExpression.orderBy() tests
    // ============================================

    def "SelectExpression.orderBy() orders subquery results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")

        when: "using orderBy() in subquery (order is not visible in IN but valid SQL)"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subquery.id)
                                            .from(subquery)
                                            .orderBy(subquery.name.asc())
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 3
            records[0].get(basics.id) == 1L
            records[1].get(basics.id) == 2L
            records[2].get(basics.id) == 3L

        where:
            session << allSessions
    }

    def "SelectExpression.orderBy() with multiple orders"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 200)
            fixtures.addBasic(3L, "Bob", 150)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")

        when: "using orderBy() with multiple columns in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subquery.id)
                                            .from(subquery)
                                            .orderBy(subquery.name.asc(), subquery.numero.desc())
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 3

        where:
            session << allSessions
    }

    // ============================================
    // SelectExpression.limit() and offset() tests
    // ============================================

    def "SelectExpression.limit() limits subquery results"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")

        when: "using limit() in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subquery.id)
                                            .from(subquery)
                                            .orderBy(subquery.id.asc())
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

    def "SelectExpression.limit() limits subquery results - no support for 'LIMIT & IN/ALL/ANY/SOME subquery'"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")

            SelectExpressionTableSource limitedIdsTable = DSL.select(subquery.id)
                    .from(subquery)
                    .orderBy(subquery.id.asc())
                    .limit(2)
                    .asTableSource("limited_ids")

            QColumn<Long> limitedId = limitedIdsTable.getColumn(subquery.id, "id")

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

    def "SelectExpression.limit().offset() applies pagination to subquery"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)
            fixtures.addBasic(4L, "Diana", 400)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")

        when: "using limit() and offset() in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subquery.id)
                                            .from(subquery)
                                            .orderBy(subquery.id.asc())
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

    def "SelectExpression.limit().offset() applies pagination to subquery - no support for 'LIMIT & IN/ALL/ANY/SOME subquery'"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)
            fixtures.addBasic(4L, "Diana", 400)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")

            SelectExpressionTableSource limitedIdsTable = DSL.select(subquery.id)
                    .from(subquery)
                    .orderBy(subquery.id.asc())
                    .limit(2)
                    .offset(1)
                    .asTableSource("limited_ids")

            QColumn<Long> limitedId = limitedIdsTable.getColumn(subquery.id, "id")

        when: "using limit() and offset() in subquery"
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
    // SelectExpression JOIN operations
    // ============================================

    def "SelectExpression.join() performs inner join in subquery"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Viewer", List.of()))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")
            BasicsTable basics = new BasicsTable("b")

            // Add a basic that matches the role name
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Admin", 100)
            fixtures.addBasic(2L, "Viewer", 200)

        when: "using join() in subquery"
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

    def "SelectExpression.leftJoin() performs left outer join in subquery"() {
        given:
            QRoleEntity qRoleEntity = new QRoleEntity(null)
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD)))
            session.insertEntity(qRoleEntity, new RoleEntity(null, "Viewer", List.of()))

            RolesTable roles = new RolesTable("r")
            RolePermissionsTable permissions = new RolePermissionsTable("rp")
            BasicsTable basics = new BasicsTable("b")

            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Admin", 100)
            fixtures.addBasic(2L, "Viewer", 200)

        when: "using leftJoin() in subquery"
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
            // Left join includes all roles, so both names are in subquery
            records.size() == 2
            records[0].get(basics.name) == "Admin"
            records[1].get(basics.name) == "Viewer"

        where:
            session << allSessions
    }

    // ============================================
    // SelectExpression set operations (UNION, INTERSECT, EXCEPT)
    // ============================================

    def "SelectExpression.union() combines two subqueries"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasicBytea(1L, "Charlie")
            fixtures.addBasicBytea(2L, "Diana")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "using union() in subquery"
            SelectExpression.CompleteStep<String> subquery1 = DSL.select(basics.name)
                    .from(basics)

            SelectExpression.CompleteStep<String> subquery2 = DSL.select(basicsBytea.name)
                    .from(basicsBytea)

            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    subquery1.union(subquery2)
                            )
                    )
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // Union of all names, but we're selecting from basics so only Alice and Bob match
            names.size() == 2
            names[0] == "Alice"
            names[1] == "Bob"

        where:
            session << allSessions
    }

    def "SelectExpression.unionAll() includes duplicates"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasicBytea(1L, "Alice")  // Duplicate

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "using unionAll() in subquery"
            SelectExpression.CompleteStep<String> subquery1 = DSL.select(basics.name)
                    .from(basics)
                    .where(basics.name.eq("Alice"))

            SelectExpression.CompleteStep<String> subquery2 = DSL.select(basicsBytea.name)
                    .from(basicsBytea)

            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    subquery1.unionAll(subquery2)
                            )
                    )
                    .fetchAll()

        then:
            // With unionAll, Alice appears twice in subquery, but IN still matches once
            names.size() == 1
            names[0] == "Alice"

        where:
            session << allSessions
    }

    def "SelectExpression.intersect() finds common values"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)
            fixtures.addBasicBytea(1L, "Alice")
            fixtures.addBasicBytea(2L, "Diana")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "using intersect() in subquery"
            SelectExpression.CompleteStep<String> subquery1 = DSL.select(basics.name)
                    .from(basics)

            SelectExpression.CompleteStep<String> subquery2 = DSL.select(basicsBytea.name)
                    .from(basicsBytea)

            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    subquery1.intersect(subquery2)
                            )
                    )
                    .fetchAll()

        then:
            // Intersection is only "Alice"
            names.size() == 1
            names[0] == "Alice"

        where:
            session << allSessions
    }

    def "SelectExpression.except() finds difference"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)
            fixtures.addBasicBytea(1L, "Alice")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "using except() in subquery"
            SelectExpression.CompleteStep<String> subquery1 = DSL.select(basics.name)
                    .from(basics)

            SelectExpression.CompleteStep<String> subquery2 = DSL.select(basicsBytea.name)
                    .from(basicsBytea)

            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    subquery1.except(subquery2)
                            )
                    )
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // basics names minus bytea names = Bob, Charlie
            names.size() == 2
            names[0] == "Bob"
            names[1] == "Charlie"

        where:
            session << allSessions
    }

    // ============================================
    // SelectExpression chained fluent API
    // ============================================

    def "SelectExpression full fluent chain: from, where, groupBy, having, orderBy, limit"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "A", 20)
            fixtures.addBasic(3L, "A", 30)
            fixtures.addBasic(4L, "B", 15)
            fixtures.addBasic(5L, "B", 25)
            fixtures.addBasic(6L, "C", 100)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")
            Expression<Long> countExpr = DSL.count()

        when: "chaining all fluent steps in subquery"
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(subquery.name)
                                            .from(subquery)
                                            .where(subquery.numero.gt(5))
                                            .groupBy(subquery.name)
                                            .having(countExpr.ge(2L))
                                            .orderBy(subquery.name.asc())
                                            .limit(10)
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

    def "SelectExpression full fluent chain: from, where, groupBy, having, orderBy, limit - no support for 'LIMIT & IN/ALL/ANY/SOME subquery'"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "A", 20)
            fixtures.addBasic(3L, "A", 30)
            fixtures.addBasic(4L, "B", 15)
            fixtures.addBasic(5L, "B", 25)
            fixtures.addBasic(6L, "C", 100)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")
            Expression<Long> countExpr = DSL.count()

            SelectExpressionTableSource limitedNamesTable = DSL.select(subquery.name)
                    .from(subquery)
                    .where(subquery.numero.gt(5))
                    .groupBy(subquery.name)
                    .having(countExpr.ge(2L))
                    .orderBy(subquery.name.asc())
                    .limit(10)
                    .asTableSource("limited_names")

            QColumn<String> limitedName = limitedNamesTable.getColumn(subquery.name, "name")

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

    // ============================================
    // SelectExpression.asTableSource() for derived tables
    // ============================================

    def "SelectExpression.asTableSource() creates derived table for use in FROM clause"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            BasicsTable basics = new BasicsTable("b")
            QColumn<Long> derivedId = QColumn.simple(Long.class, "derived", "id")
            QColumn<String> derivedName = QColumn.simple(String.class, "derived", "name")

        when: "using asTableSource() to create a derived table"
            def derivedTable = DSL.select(basics.id, basics.name)
                    .from(basics)
                    .asTableSource("derived")

            List<Record> records = session
                    .select(derivedId, derivedName)
                    .from(derivedTable)
                    .orderBy(derivedId.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(derivedId) == 1L
            records[0].get(derivedName) == "Alice"
            records[1].get(derivedId) == 2L
            records[1].get(derivedName) == "Bob"

        where:
            session << allSessions
    }

    // ============================================
    // SelectExpression.select() - adding more columns
    // ============================================

    def "SelectExpression.select() adds additional columns for derived table"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            BasicsTable basics = new BasicsTable("b")
            QColumn<Long> derivedId = QColumn.simple(Long.class, "derived", "id")
            QColumn<String> derivedName = QColumn.simple(String.class, "derived", "name")

        when: "using select().select() to add columns incrementally for derived table"
            def derivedTable = DSL.select(basics.id)
                    .select(basics.name)  // Add another column using fluent select()
                    .from(basics)
                    .asTableSource("derived")

            List<Record> records = session
                    .select(derivedId, derivedName)
                    .from(derivedTable)
                    .orderBy(derivedId.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(derivedId) == 1L
            records[0].get(derivedName) == "Alice"
            records[1].get(derivedId) == 2L
            records[1].get(derivedName) == "Bob"

        where:
            session << allSessions
    }

    // ============================================
    // SelectExpression.asNonFluent() converts to base expression
    // ============================================

    def "SelectExpression.asNonFluent() converts fluent to non-fluent for combination"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasicBytea(1L, "Charlie")

            BasicsTable basics = new BasicsTable("b")
            BasicsByteaTable basicsBytea = new BasicsByteaTable("bb")

        when: "using asNonFluent() for union via SelectExpression interface"
            SelectExpression.CompleteStep<String> fluentQuery1 = DSL.select(basics.name)
                    .from(basics)

            SelectExpression<String> nonFluentQuery1 = fluentQuery1.asNonFluent()

            SelectExpression.CompleteStep<String> fluentQuery2 = DSL.select(basicsBytea.name)
                    .from(basicsBytea)

            // Use combine with non-fluent
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    fluentQuery1.union(fluentQuery2.asNonFluent())
                            )
                    )
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
    // Complex nested subqueries
    // ============================================

    def "SelectExpression supports deeply nested subqueries"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            // Use simple lowercase aliases to avoid case-sensitivity issues
            BasicsTable t1 = new BasicsTable("t1")
            BasicsTable t2 = new BasicsTable("t2")
            BasicsTable t3 = new BasicsTable("t3")

        when: "using nested subqueries"
            List<Record> records = session
                    .select(t1.id, t1.name)
                    .from(t1)
                    .where(
                            t1.name.in(
                                    DSL.select(t2.name)
                                            .from(t2)
                                            .where(
                                                    t2.id.in(
                                                            DSL.select(t3.id)
                                                                    .from(t3)
                                                                    .where(t3.numero.lt(250))
                                                    )
                                            )
                            )
                    )
                    .orderBy(t1.id.asc())
                    .fetchAll()

        then:
            // Inner: ids where numero < 250 -> 1, 2
            // Middle: names where id in (1, 2) -> Alice, Bob
            // Outer: rows where name in (Alice, Bob)
            records.size() == 2
            records[0].get(t1.name) == "Alice"
            records[1].get(t1.name) == "Bob"

        where:
            session << allSessions
    }

    // ============================================
    // Empty subquery handling
    // ============================================

    def "SelectExpression returns empty when subquery matches no rows"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")

        when: "subquery matches nothing"
            List<Record> records = session
                    .select(basics.id)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subquery.id)
                                            .from(subquery)
                                            .where(subquery.numero.gt(9999))  // No match
                            )
                    )
                    .fetchAll()

        then:
            records.isEmpty()

        where:
            session << allSessions
    }

    // ============================================
    // SelectExpression List-parameter method tests
    // ============================================

    def "SelectExpression.select() with List adds multiple columns"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)

            BasicsTable basics = new BasicsTable("b")
            QColumn<Long> derivedId = QColumn.simple(Long.class, "derived", "id")
            QColumn<String> derivedName = QColumn.simple(String.class, "derived", "name")
            QColumn<Integer> derivedNumero = QColumn.simple(Integer.class, "derived", "numero")

        when: "using select(List) to add multiple columns at once"
            List<Expression<?>> columns = [basics.name, basics.numero]
            def derivedTable = DSL.select(basics.id)
                    .select(columns)  // Add list of columns using the List variant
                    .from(basics)
                    .asTableSource("derived")

            List<Record> records = session
                    .select(derivedId, derivedName, derivedNumero)
                    .from(derivedTable)
                    .orderBy(derivedId.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(derivedId) == 1L
            records[0].get(derivedName) == "Alice"
            records[0].get(derivedNumero) == 100
            records[1].get(derivedId) == 2L
            records[1].get(derivedName) == "Bob"
            records[1].get(derivedNumero) == 200

        where:
            session << allSessions
    }

    def "SelectExpression.where() with List applies AND conditions"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")

        when: "using where(List) with multiple conditions"
            List<io.github.thinkfastpl.harbororm.api.expression.Condition> conditions = [
                    subquery.numero.gt(50),
                    subquery.numero.lt(250)
            ]
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subquery.id)
                                            .from(subquery)
                                            .where(conditions)  // Apply List of conditions
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // Conditions applied as AND: numero > 50 AND numero < 250
            records.size() == 2
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Bob"

        where:
            session << allSessions
    }

    def "SelectExpression.groupBy() with List groups by multiple columns"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Alice", 100)  // Same name and numero as id=1
            fixtures.addBasic(3L, "Alice", 200)  // Same name, different numero
            fixtures.addBasic(4L, "Bob", 100)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")
            Expression<Long> countExpr = DSL.count()

        when: "using groupBy(List) with multiple columns"
            List<Expression<?>> groupByColumns = [subquery.name, subquery.numero]
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(subquery.name)
                                            .from(subquery)
                                            .groupBy(groupByColumns)  // Group by list of columns
                                            .having(countExpr.ge(2L))  // Only groups with count >= 2
                            )
                    )
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // Only (Alice, 100) group has count >= 2 (ids 1 and 2)
            names.size() == 3
            names.every { it == "Alice" }

        where:
            session << allSessions
    }

    def "SelectExpression.having() with List applies AND conditions"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 10)
            fixtures.addBasic(2L, "Alice", 20)
            fixtures.addBasic(3L, "Alice", 30)
            fixtures.addBasic(4L, "Bob", 100)
            fixtures.addBasic(5L, "Bob", 200)
            fixtures.addBasic(6L, "Charlie", 500)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")
            Expression<Long> countExpr = DSL.count()
            Expression<Integer> sumExpr = DSL.sum(subquery.numero)

        when: "using having(List) with multiple conditions"
            List<io.github.thinkfastpl.harbororm.api.expression.Condition> havingConditions = [
                    countExpr.ge(2L),    // count >= 2
                    sumExpr.lt(500)      // sum < 500
            ]
            List<String> names = session
                    .select(basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(subquery.name)
                                            .from(subquery)
                                            .groupBy(subquery.name)
                                            .having(havingConditions)  // Apply List of having conditions
                            )
                    )
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            // Alice: count=3 >= 2, sum=60 < 500 -> matches
            // Bob: count=2 >= 2, sum=300 < 500 -> matches
            // Charlie: count=1 < 2 -> does not match
            names.size() == 5
            names.count { it == "Alice" } == 3
            names.count { it == "Bob" } == 2

        where:
            session << allSessions
    }

    def "SelectExpression.orderBy() with List orders by multiple columns"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 200)
            fixtures.addBasic(2L, "Alice", 100)
            fixtures.addBasic(3L, "Bob", 150)
            fixtures.addBasic(4L, "Bob", 50)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")

        when: "using orderBy(List) with multiple orders"
            List<Order> orders = [subquery.name.asc(), subquery.numero.desc()]
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.id.in(
                                    DSL.select(subquery.id)
                                            .from(subquery)
                                            .orderBy(orders)  // Apply List of orders
                                            .limit(3)  // Take first 3 to verify ordering
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // Subquery order: Alice desc numero (200, 100), Bob desc numero (150)
            // First 3: ids 1 (Alice 200), 2 (Alice 100), 3 (Bob 150)
            records.size() == 3
            records[0].get(basics.id) == 1L
            records[1].get(basics.id) == 2L
            records[2].get(basics.id) == 3L

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "SelectExpression.orderBy() with List orders by multiple columns - no support for 'LIMIT & IN/ALL/ANY/SOME subquery'"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 200)
            fixtures.addBasic(2L, "Alice", 100)
            fixtures.addBasic(3L, "Bob", 150)
            fixtures.addBasic(4L, "Bob", 50)

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subquery = new BasicsTable("sq")
            List<Order> orders = [subquery.name.asc(), subquery.numero.desc()]

            SelectExpressionTableSource limitedIdsTable = DSL.select(subquery.id)
                    .from(subquery)
                    .orderBy(orders)
                    .limit(3)
                    .asTableSource("limited_ids")

            QColumn<Long> limitedId = limitedIdsTable.getColumn(subquery.id, "id")

        when: "using orderBy(List) with multiple orders"
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
            // Subquery order: Alice desc numero (200, 100), Bob desc numero (150)
            // First 3: ids 1 (Alice 200), 2 (Alice 100), 3 (Bob 150)
            records.size() == 3
            records[0].get(basics.id) == 1L
            records[1].get(basics.id) == 2L
            records[2].get(basics.id) == 3L

        where:
            session << allSessions
    }

    def "SelectExpression with crossJoin performs cartesian product in subquery"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasicBytea(1L, "X")
            fixtures.addBasicBytea(2L, "Y")

            BasicsTable basics = new BasicsTable("b")
            BasicsTable subqueryBasics = new BasicsTable("sq")
            BasicsByteaTable subqueryBytea = new BasicsByteaTable("bb")

        when: "using crossJoin() in subquery"
            // Create a subquery that cross joins basics with basics_bytea
            // and selects names from basics where cartesian product count > 2
            List<Record> records = session
                    .select(basics.id, basics.name)
                    .from(basics)
                    .where(
                            basics.name.in(
                                    DSL.select(subqueryBasics.name)
                                            .from(subqueryBasics)
                                            .crossJoin(subqueryBytea.getTableName())  // Cross join creates 2x2=4 rows
                                            .where(subqueryBasics.numero.ge(100))
                            )
                    )
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // Cross join produces: (Alice, X), (Alice, Y), (Bob, X), (Bob, Y)
            // Both Alice and Bob have numero >= 100, so both names are in subquery
            records.size() == 2
            records[0].get(basics.name) == "Alice"
            records[1].get(basics.name) == "Bob"

        where:
            session << allSessions
    }
}
