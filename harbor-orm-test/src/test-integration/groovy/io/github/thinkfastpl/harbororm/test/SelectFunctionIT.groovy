// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.query.ProductsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class SelectFunctionIT extends AbstractHarborIT {

    def "select length()"() {
        when:
            def result = session.select(DSL.length(value)).fetchSingle()

        then:
            result == expected
            result.getClass() == Integer.class

        where:
            session << allSessions

        combined:
            value || expected
            'hello' || 5
            'Hello World' || 11
            'test' || 4
            '' || 0
            ' ' || 1
            'a' || 1
            '12345' || 5
            'josé' || 4
            '   spaces   ' || 12
            'Test123Test' || 11
    }


    def "select count(*)"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)

            fixtures.addProductA()
            fixtures.addProductA()
            fixtures.addProductB()

            ProductsTable products = new ProductsTable("p")

        when:
            Long count = session.select(DSL.count())
                    .from(products)
                    .fetchSingle()

        then:
            count == 3

        where:
            session << allSessions
    }

    def "select count(distinct ...)"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)

            fixtures.addProductA()
            fixtures.addProductA()
            fixtures.addProductB()

            ProductsTable products = new ProductsTable("p")

        when:
            Long count = session.select(DSL.countDistinct(products.name))
                    .from(products)
                    .fetchSingle()

        then:
            count == 2

        where:
            session << allSessions
    }




    def "modulo two constant whole numbers"() {
        given:
            Expression<?> sumExpression = expression.modulo(value)
            Expression<?> aliasedSumExpression = expression.modulo(aliasedValue).as("my_alias")

        when:
            List<Record> records = session.select(sumExpression, aliasedSumExpression).fetchAll()

        then:
            records.size() == 1
            with(records.get(0)) { r ->
                r.get(1) == sum
                r.get(1).getClass() == type
                r.get(sumExpression) == sum
                r.get(sumExpression).getClass() == type

                r.get(2) == aliasedSum
                r.get(2).getClass() == type
                r.get(aliasedSumExpression) == aliasedSum
                r.get(aliasedSumExpression).getClass() == type
                r.get("my_alias") == aliasedSum
                r.get("my_alias").getClass() == type
            }

        where:
            session << allSessions

        combined:
            value | aliasedValue | expression | type || sum | aliasedSum
            4 | 3 | DSL.constant(10) | Integer.class || 2 | 1
            4L | 3L | DSL.constant(10L) | Long.class || 2L | 1L
            (short) 4 | (short) 3 | DSL.constant((short) 10) | Short.class || (short) 2 | (short) 1
            (byte) 4 | (byte) 3 | DSL.constant((byte) 10) | Byte.class || (byte) 2 | (byte) 1
    }

    def "multiset"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "A", 11)
            testFixtures.addBasic(2L, "A", 22)
            testFixtures.addBasic(3L, "B", 33)
            testFixtures.addBasic(4L, "B", 44)
            testFixtures.addBasic(5L, "B", 55)

            BasicsTable basics = new BasicsTable(null)

        when:
            Expression<Record[]> multisetAgg = DSL.multisetAgg(List.of(basics.numero, basics.id))
            List<Record> records = session.select(basics.name, multisetAgg)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { record ->
                record.get(basics.name) == "A"
                record.get(multisetAgg).length == 2
            }
            with(records[1]) { record ->
                record.get(basics.name) == "B"
                record.get(multisetAgg).length == 3
            }

        where:
            session << allSessions
    }



    // ==================== LAG/LEAD WINDOW FUNCTIONS ====================

    def "select lag() with offset and default"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "A", 10)
            testFixtures.addBasic(2L, "B", 20)
            testFixtures.addBasic(3L, "C", 30)
            testFixtures.addBasic(4L, "D", 40)

            BasicsTable basics = new BasicsTable(null)

        when:
            def lagExpr = DSL.lag(basics.numero, 1, 0).orderBy(basics.id.asc()).as("prev_numero")
            List<Record> records = session.select(basics.id, basics.numero, lagExpr)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 4
            records[0].get("prev_numero") == 0   // first row, no previous, uses default
            records[1].get("prev_numero") == 10  // previous row's numero
            records[2].get("prev_numero") == 20
            records[3].get("prev_numero") == 30

        where:
            session << allSessions
    }

    def "select lead() with offset and default"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "A", 10)
            testFixtures.addBasic(2L, "B", 20)
            testFixtures.addBasic(3L, "C", 30)
            testFixtures.addBasic(4L, "D", 40)

            BasicsTable basics = new BasicsTable(null)

        when:
            def leadExpr = DSL.lead(basics.numero, 1, 0).orderBy(basics.id.asc()).as("next_numero")
            List<Record> records = session.select(basics.id, basics.numero, leadExpr)
                    .from(basics)
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 4
            records[0].get("next_numero") == 20  // next row's numero
            records[1].get("next_numero") == 30
            records[2].get("next_numero") == 40
            records[3].get("next_numero") == 0   // last row, no next, uses default

        where:
            session << allSessions
    }

    // ==================== SELECT DISTINCT ====================

    def "select distinct via DSL.selectDistinct() in subquery"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "A", 10)
            testFixtures.addBasic(2L, "A", 20)
            testFixtures.addBasic(3L, "B", 30)
            testFixtures.addBasic(4L, "B", 40)

            BasicsTable basics = new BasicsTable("b")

        when:
            // Use selectDistinct in an IN subquery to get distinct names
            List<Record> records = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .where(basics.name.in(DSL.selectDistinct(basics.name).from(basics)))
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            // All 4 rows returned since both A and B are in the distinct names
            records.size() == 4
            records[0].get(basics.name) == "A"
            records[1].get(basics.name) == "A"
            records[2].get(basics.name) == "B"
            records[3].get(basics.name) == "B"

        where:
            session << allSessions
    }

    // ==================== POWER OVERLOADS ====================

    def "power() with Number and Expression"() {
        when:
            // Use explicit Expression type to avoid Groovy type inference issues
            BigDecimal result = session.select(DSL.power((Integer) 2, DSL.constant((Integer) 3))).fetchSingle()

        then:
            result == 8.0

        where:
            session << allSessions
    }

    def "power() with Expression and Number"() {
        when:
            BigDecimal result = session.select(DSL.power(DSL.constant((Integer) 2), (Integer) 3)).fetchSingle()

        then:
            result == 8.0

        where:
            session << allSessions
    }

    // ==================== AND/OR CONDITION COMBINERS ====================

    def "DSL.and() combines multiple conditions"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "A", 10)
            testFixtures.addBasic(2L, "B", 20)
            testFixtures.addBasic(3L, "C", 30)

            BasicsTable basics = new BasicsTable(null)

        when:
            def cond1 = basics.name.notEq("A")
            def cond2 = basics.numero.lt(30)
            List<Record> records = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .where(DSL.and(cond1, cond2))
                    .fetchAll()

        then:
            records.size() == 1
            records[0].get(basics.name) == "B"
            records[0].get(basics.numero) == 20

        where:
            session << allSessions
    }

    def "DSL.or() with varargs conditions"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "A", 10)
            testFixtures.addBasic(2L, "B", 20)
            testFixtures.addBasic(3L, "C", 30)

            BasicsTable basics = new BasicsTable(null)

        when:
            def cond1 = basics.name.eq("A")
            def cond2 = basics.name.eq("C")
            List<Record> records = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .where(DSL.or(cond1, cond2))
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(basics.name) == "A"
            records[1].get(basics.name) == "C"

        where:
            session << allSessions
    }

    def "DSL.or() with list of conditions"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "A", 10)
            testFixtures.addBasic(2L, "B", 20)
            testFixtures.addBasic(3L, "C", 30)

            BasicsTable basics = new BasicsTable(null)

        when:
            def conditionList = [basics.name.eq("A"), basics.name.eq("B")]
            List<Record> records = session.select(basics.id, basics.name, basics.numero)
                    .from(basics)
                    .where(DSL.or(conditionList))
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(basics.name) == "A"
            records[1].get(basics.name) == "B"

        where:
            session << allSessions
    }

    // ==================== ARRAY AGG FILTER (not supported on MariaDB) ====================

    def "select arrayAggFilter() aggregates with filter"() {
        given:
            TestFixtures testFixtures = new TestFixtures(session)
            testFixtures.addBasic(1L, "A", 11)
            testFixtures.addBasic(2L, "A", 22)
            testFixtures.addBasic(3L, "A", 33)
            testFixtures.addBasic(4L, "B", 44)
            testFixtures.addBasic(5L, "B", 55)

            BasicsTable basics = new BasicsTable(null)

        when:
            // Filter to only include even numbers
            def arrayExpr = DSL.arrayAggWhere(basics.numero, [basics.numero.modulo(2).eq(0)]).as("even_nums")
            List<Record> records = session.select(basics.name, arrayExpr)
                    .from(basics)
                    .groupBy(basics.name)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { record ->
                record.get(basics.name) == "A"
                with(record.get("even_nums")) { nums ->
                    nums.length == 1
                    nums[0] == 22
                }
            }
            with(records[1]) { record ->
                record.get(basics.name) == "B"
                with(record.get("even_nums")) { nums ->
                    nums.length == 1
                    nums[0] == 44
                }
            }

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    // ==================== CONSTANT(INTEGER) with arithmetic ====================

    def "DSL.constant() with Integer creates expression usable in arithmetic"() {
        when:
            Integer result = session.select(DSL.constant(42).add(8)).fetchSingle()

        then:
            result == 50

        where:
            session << allSessions
    }

    // ==================== Expression<X> + raw value overloads ====================

    def "select btrim(Expression, String)"() {
        when:
            def result = session.select(DSL.btrim(DSL.constant(string), characters)).fetchSingle()

        then:
            result == expected
            result.getClass() == String.class

        where:
            session << allSessions

        combined:
            string | characters || expected
            'xyzHelloxyz' | 'xyz' || 'Hello'
            '###test###' | '#' || 'test'
            'nochange' | 'xyz' || 'nochange'
    }

    def "select log(Expression, value) Float"() {
        when:
            Float result = session.select(DSL.log(DSL.constant(2.0F), 64.0F)).fetchSingle()

        then:
            result == 6.0F
            result.getClass() == Float.class

        where:
            session << allSessions
    }

    def "select log(Expression, value) Double"() {
        when:
            Double result = session.select(DSL.log(DSL.constant(2.0D), 64.0D)).fetchSingle()

        then:
            result == 6.0D
            result.getClass() == Double.class

        where:
            session << allSessions
    }

    def "select log(Expression, value) BigDecimal"() {
        when:
            BigDecimal result = session.select(DSL.log(DSL.constant(new BigDecimal("2.0")), new BigDecimal("64.0"))).fetchSingle()

        then:
            result == new BigDecimal("6.0")
            result.getClass() == BigDecimal.class

        where:
            session << allSessions
    }

    def "select mod(Expression, value) Byte"() {
        when:
            Byte result = session.select(DSL.mod(DSL.constant((byte) 9), (byte) 4)).fetchSingle()

        then:
            result == (byte) 1
            result.getClass() == Byte.class

        where:
            session << allSessions
    }

    def "select mod(Expression, value) Short"() {
        when:
            Short result = session.select(DSL.mod(DSL.constant((short) 17), (short) 5)).fetchSingle()

        then:
            result == (short) 2
            result.getClass() == Short.class

        where:
            session << allSessions
    }

    def "select mod(Expression, value) Integer"() {
        when:
            Integer result = session.select(DSL.mod(DSL.constant(17), 5)).fetchSingle()

        then:
            result == 2
            result.getClass() == Integer.class

        where:
            session << allSessions
    }

    def "select mod(Expression, value) Long"() {
        when:
            Long result = session.select(DSL.mod(DSL.constant(17L), 5L)).fetchSingle()

        then:
            result == 2L
            result.getClass() == Long.class

        where:
            session << allSessions
    }

    def "select mod(Expression, value) BigInteger"() {
        when:
            BigInteger result = session.select(DSL.mod(DSL.constant(BigInteger.valueOf(17)), BigInteger.valueOf(5))).fetchSingle()

        then:
            result == BigInteger.valueOf(2)
            result.getClass() == BigInteger.class

        where:
            session << allSessions
    }

    def "select mod(Expression, value) BigDecimal"() {
        when:
            BigDecimal result = session.select(DSL.mod(DSL.constant(new BigDecimal("9.5")), new BigDecimal("2.0"))).fetchSingle()

        then:
            result == new BigDecimal("1.5")
            result.getClass() == BigDecimal.class

        where:
            session << allSessions
    }

    def "select bitAnd(Expression, value) Byte"() {
        when:
            Byte result = session.select(DSL.bitAnd(DSL.constant((byte) 12), (byte) 10)).fetchSingle()

        then:
            result == (byte) 8
            result.getClass() == Byte.class

        where:
            session << allSessions
    }

    def "select bitAnd(Expression, value) Short"() {
        when:
            Short result = session.select(DSL.bitAnd(DSL.constant((short) 255), (short) 15)).fetchSingle()

        then:
            result == (short) 15
            result.getClass() == Short.class

        where:
            session << allSessions
    }

    def "select bitAnd(Expression, value) Integer"() {
        when:
            Integer result = session.select(DSL.bitAnd(DSL.constant(12), 10)).fetchSingle()

        then:
            result == 8
            result.getClass() == Integer.class

        where:
            session << allSessions
    }

    def "select bitAnd(Expression, value) Long"() {
        when:
            Long result = session.select(DSL.bitAnd(DSL.constant(12L), 10L)).fetchSingle()

        then:
            result == 8L
            result.getClass() == Long.class

        where:
            session << allSessions
    }

    def "select bitOr(Expression, value) Byte"() {
        when:
            Byte result = session.select(DSL.bitOr(DSL.constant((byte) 12), (byte) 10)).fetchSingle()

        then:
            result == (byte) 14
            result.getClass() == Byte.class

        where:
            session << allSessions
    }

    def "select bitOr(Expression, value) Short"() {
        when:
            Short result = session.select(DSL.bitOr(DSL.constant((short) 255), (short) 15)).fetchSingle()

        then:
            result == (short) 255
            result.getClass() == Short.class

        where:
            session << allSessions
    }

    def "select bitOr(Expression, value) Integer"() {
        when:
            Integer result = session.select(DSL.bitOr(DSL.constant(12), 10)).fetchSingle()

        then:
            result == 14
            result.getClass() == Integer.class

        where:
            session << allSessions
    }

    def "select bitOr(Expression, value) Long"() {
        when:
            Long result = session.select(DSL.bitOr(DSL.constant(12L), 10L)).fetchSingle()

        then:
            result == 14L
            result.getClass() == Long.class

        where:
            session << allSessions
    }

    def "select bitGet(Expression, value) Byte"() {
        when:
            Byte result = session.select(DSL.bitGet(DSL.constant((byte) 12), 2)).fetchSingle()

        then:
            result == (byte) 1
            result.getClass() == Byte.class

        where:
            session << allSessions
    }

    def "select bitGet(Expression, value) Short"() {
        when:
            Short result = session.select(DSL.bitGet(DSL.constant((short) 12), 3)).fetchSingle()

        then:
            result == (short) 1
            result.getClass() == Short.class

        where:
            session << allSessions
    }

    def "select bitGet(Expression, value) Integer"() {
        when:
            Integer result = session.select(DSL.bitGet(DSL.constant(12), 2)).fetchSingle()

        then:
            result == 1
            result.getClass() == Integer.class

        where:
            session << allSessions
    }

    def "select bitSet(Expression, value) Byte"() {
        when:
            Byte result = session.select(DSL.bitSet(DSL.constant((byte) 12), 0)).fetchSingle()

        then:
            result == (byte) 13
            result.getClass() == Byte.class

        where:
            session << allSessions
    }

    def "select bitSet(Expression, value) Short"() {
        when:
            Short result = session.select(DSL.bitSet(DSL.constant((short) 12), 1)).fetchSingle()

        then:
            result == (short) 14
            result.getClass() == Short.class

        where:
            session << allSessions
    }

    def "select bitSet(Expression, value) Integer"() {
        when:
            Integer result = session.select(DSL.bitSet(DSL.constant(12), 4)).fetchSingle()

        then:
            result == 28
            result.getClass() == Integer.class

        where:
            session << allSessions
    }

    def "select shl(Expression, value) Byte"() {
        when:
            Byte result = session.select(DSL.shl(DSL.constant((byte) 5), 2)).fetchSingle()

        then:
            result == (byte) 20
            result.getClass() == Byte.class

        where:
            session << allSessions
    }

    def "select shl(Expression, value) Short"() {
        when:
            Short result = session.select(DSL.shl(DSL.constant((short) 5), 2)).fetchSingle()

        then:
            result == (short) 20
            result.getClass() == Short.class

        where:
            session << allSessions
    }

    def "select shl(Expression, value) Integer"() {
        when:
            Integer result = session.select(DSL.shl(DSL.constant(5), 2)).fetchSingle()

        then:
            result == 20
            result.getClass() == Integer.class

        where:
            session << allSessions
    }

    def "select shl(Expression, value) Long"() {
        when:
            Long result = session.select(DSL.shl(DSL.constant(1024L), 10)).fetchSingle()

        then:
            result == 1048576L
            result.getClass() == Long.class

        where:
            session << allSessions
    }

    def "select shr(Expression, value) Byte"() {
        when:
            Byte result = session.select(DSL.shr(DSL.constant((byte) 20), 2)).fetchSingle()

        then:
            result == (byte) 5
            result.getClass() == Byte.class

        where:
            session << allSessions
    }

    def "select shr(Expression, value) Short"() {
        when:
            Short result = session.select(DSL.shr(DSL.constant((short) 20), 2)).fetchSingle()

        then:
            result == (short) 5
            result.getClass() == Short.class

        where:
            session << allSessions
    }

    def "select shr(Expression, value) Integer"() {
        when:
            Integer result = session.select(DSL.shr(DSL.constant(20), 2)).fetchSingle()

        then:
            result == 5
            result.getClass() == Integer.class

        where:
            session << allSessions
    }

    def "select shr(Expression, value) Long"() {
        when:
            Long result = session.select(DSL.shr(DSL.constant(1048576L), 10)).fetchSingle()

        then:
            result == 1024L
            result.getClass() == Long.class

        where:
            session << allSessions
    }

    def "select bitNand(Expression, value) Byte"() {
        when:
            Byte result = session.select(DSL.bitNand(DSL.constant((byte) 12), (byte) 10)).fetchSingle()

        then:
            result == (byte) -9
            result.getClass() == Byte.class

        where:
            session << allSessions
    }

    def "select bitNand(Expression, value) Short"() {
        when:
            Short result = session.select(DSL.bitNand(DSL.constant((short) 255), (short) 15)).fetchSingle()

        then:
            result == (short) -16
            result.getClass() == Short.class

        where:
            session << allSessions
    }

    def "select bitNand(Expression, value) Integer"() {
        when:
            Integer result = session.select(DSL.bitNand(DSL.constant(12), 10)).fetchSingle()

        then:
            result == -9
            result.getClass() == Integer.class

        where:
            session << allSessions
    }

    def "select bitNand(Expression, value) Long"() {
        when:
            Long result = session.select(DSL.bitNand(DSL.constant(12L), 10L)).fetchSingle()

        then:
            result == -9L
            result.getClass() == Long.class

        where:
            session << allSessions
    }

    def "select bitNor(Expression, value) Byte"() {
        when:
            Byte result = session.select(DSL.bitNor(DSL.constant((byte) 12), (byte) 10)).fetchSingle()

        then:
            result == (byte) -15
            result.getClass() == Byte.class

        where:
            session << allSessions
    }

    def "select bitNor(Expression, value) Short"() {
        when:
            Short result = session.select(DSL.bitNor(DSL.constant((short) 255), (short) 15)).fetchSingle()

        then:
            result == (short) -256
            result.getClass() == Short.class

        where:
            session << allSessions
    }

    def "select bitNor(Expression, value) Integer"() {
        when:
            Integer result = session.select(DSL.bitNor(DSL.constant(12), 10)).fetchSingle()

        then:
            result == -15
            result.getClass() == Integer.class

        where:
            session << allSessions
    }

    def "select bitNor(Expression, value) Long"() {
        when:
            Long result = session.select(DSL.bitNor(DSL.constant(12L), 10L)).fetchSingle()

        then:
            result == -15L
            result.getClass() == Long.class

        where:
            session << allSessions
    }

    def "select bitXNor(Expression, value) Byte"() {
        when:
            Byte result = session.select(DSL.bitXNor(DSL.constant((byte) 12), (byte) 10)).fetchSingle()

        then:
            result == (byte) -7
            result.getClass() == Byte.class

        where:
            session << allSessions
    }

    def "select bitXNor(Expression, value) Short"() {
        when:
            Short result = session.select(DSL.bitXNor(DSL.constant((short) 255), (short) 15)).fetchSingle()

        then:
            result == (short) -241
            result.getClass() == Short.class

        where:
            session << allSessions
    }

    def "select bitXNor(Expression, value) Integer"() {
        when:
            Integer result = session.select(DSL.bitXNor(DSL.constant(12), 10)).fetchSingle()

        then:
            result == -7
            result.getClass() == Integer.class

        where:
            session << allSessions
    }

    def "select bitXNor(Expression, value) Long"() {
        when:
            Long result = session.select(DSL.bitXNor(DSL.constant(12L), 10L)).fetchSingle()

        then:
            result == -7L
            result.getClass() == Long.class

        where:
            session << allSessions
    }

    def "select bitXor(Expression, value) Byte"() {
        when:
            Byte result = session.select(DSL.bitXor(DSL.constant((byte) 12), (byte) 10)).fetchSingle()

        then:
            result == (byte) 6
            result.getClass() == Byte.class

        where:
            session << allSessions
    }

    def "select bitXor(Expression, value) Short"() {
        when:
            Short result = session.select(DSL.bitXor(DSL.constant((short) 255), (short) 15)).fetchSingle()

        then:
            result == (short) 240
            result.getClass() == Short.class

        where:
            session << allSessions
    }

    def "select bitXor(Expression, value) Integer"() {
        when:
            Integer result = session.select(DSL.bitXor(DSL.constant(12), 10)).fetchSingle()

        then:
            result == 6
            result.getClass() == Integer.class

        where:
            session << allSessions
    }

    def "select bitXor(Expression, value) Long"() {
        when:
            Long result = session.select(DSL.bitXor(DSL.constant(12L), 10L)).fetchSingle()

        then:
            result == 6L
            result.getClass() == Long.class

        where:
            session << allSessions
    }

    def "select round(Expression, s) Float"() {
        when:
            Float result = session.select(DSL.round(DSL.constant(1234.56F), -1)).fetchSingle()

        then:
            result == 1230F
            result.getClass() == Float.class

        where:
            session << allSessions
    }

    def "select round(Expression, s) Double"() {
        when:
            Double result = session.select(DSL.round(DSL.constant(1234.56D), -1)).fetchSingle()

        then:
            result == 1230D
            result.getClass() == Double.class

        where:
            session << allSessions
    }

    def "select round(Expression, s) BigDecimal"() {
        when:
            BigDecimal result = session.select(DSL.round(DSL.constant(new BigDecimal("1234.56")), -1)).fetchSingle()

        then:
            result == new BigDecimal("1230")
            result.getClass() == BigDecimal.class

        where:
            session << allSessions
    }





































}
