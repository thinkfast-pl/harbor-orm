// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.query.ProductsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

/**
 * Integration tests for arithmetic operations that are NOT covered by SelectOperatorIT or SelectFunctionIT.
 *
 * Covered by SelectOperatorIT: add, subtract, multiply, divide on DSL.constant() values
 * Covered by SelectFunctionIT: modulo on constant whole numbers (Integer, Long, Short, Byte)
 *
 * This test covers:
 * 1. Modulo operation on Float and Double constants
 * 2. Arithmetic operations on entity columns (add, subtract, multiply, divide, modulo)
 */
class ArithmeticOperationsIT extends AbstractHarborIT {

    // ===========================================
    // Modulo on Float and Double constants
    // Note: PostgreSQL does not support MOD for float/double types, only for integer/numeric
    // These tests are H2-only
    // ===========================================

    def "modulo two constant floats"() {
        given:
            Expression<Float> moduloExpression = DSL.constant(33.2F).modulo(6F)
            Expression<Float> aliasedModuloExpression = DSL.constant(33.2F).modulo(6.1F).as("my_alias")
            float error = 0.001F

        when:
            List<Record> records = session.select(moduloExpression, aliasedModuloExpression).fetchAll()

        then:
            records.size() == 1
            Record r = records[0]
            r.get(1).class == Float.class
            that((Float) r.get(1), closeTo(3.2F, error))
            r.get(moduloExpression).getClass() == Float.class
            that(r.get(moduloExpression), closeTo(3.2F, error))

            r.get(2).class == Float.class
            that((Float) r.get(2), closeTo(2.7F, error))
            that(r.get(aliasedModuloExpression), closeTo(2.7F, error))
            r.get(aliasedModuloExpression).getClass() == Float.class
            r.get("my_alias").class == Float.class
            that((Float) r.get("my_alias"), closeTo(2.7F, error))

        where:
            session << getSessionsExcept(DbType.POSTGRES)
    }

    def "modulo two constant doubles"() {
        given:
            Expression<Double> moduloExpression = DSL.constant(33.2D).modulo(6D)
            Expression<Double> aliasedModuloExpression = DSL.constant(33.2D).modulo(6.1D).as("my_alias")
            Double error = 0.001D

        when:
            List<Record> records = session.select(moduloExpression, aliasedModuloExpression).fetchAll()

        then:
            records.size() == 1
            Record r = records[0]
            r.get(1).class == Double.class
            that((Double) r.get(1), closeTo(3.2D, error))
            r.get(moduloExpression).getClass() == Double.class
            that(r.get(moduloExpression), closeTo(3.2D, error))

            r.get(2).class == Double.class
            that((Double) r.get(2), closeTo(2.7D, error))
            that(r.get(aliasedModuloExpression), closeTo(2.7D, error))
            r.get(aliasedModuloExpression).getClass() == Double.class
            r.get("my_alias").class == Double.class
            that((Double) r.get("my_alias"), closeTo(2.7D, error))

        where:
            session << getSessionsExcept(DbType.POSTGRES)
    }

    // ===========================================
    // Arithmetic on entity columns - BasicEntity (numero is Integer)
    // ===========================================

    def "add constant to entity integer column"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)

            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> addExpression = basics.numero.add(5)

        when:
            List<Record> records = session.select(basics.name, addExpression)
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(addExpression) == 15
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(addExpression) == 25
            }

        where:
            session << allSessions
    }

    def "subtract constant from entity integer column"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)

            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> subtractExpression = basics.numero.subtract(3)

        when:
            List<Record> records = session.select(basics.name, subtractExpression)
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(subtractExpression) == 7
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(subtractExpression) == 17
            }

        where:
            session << allSessions
    }

    def "multiply entity integer column by constant"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)

            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> multiplyExpression = basics.numero.multiply(2)

        when:
            List<Record> records = session.select(basics.name, multiplyExpression)
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(multiplyExpression) == 20
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(multiplyExpression) == 40
            }

        where:
            session << allSessions
    }

    def "divide entity integer column by constant"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)

            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> divideExpression = basics.numero.divide(5)

        when:
            List<Record> records = session.select(basics.name, divideExpression)
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(divideExpression) == 2
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(divideExpression) == 4
            }

        where:
            session << allSessions
    }

    def "modulo entity integer column by constant"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 23)

            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> moduloExpression = basics.numero.modulo(7)

        when:
            List<Record> records = session.select(basics.name, moduloExpression)
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(moduloExpression) == 3  // 10 % 7 = 3
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get(moduloExpression) == 2  // 23 % 7 = 2
            }

        where:
            session << allSessions
    }

    // ===========================================
    // Arithmetic on entity columns - ProductEntity (BigDecimal columns)
    // ===========================================

    def "add two BigDecimal entity columns"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addProductA()  // priceNet=1.00, vatRate=23, priceGross=1.23
            fixtures.addProductB()  // priceNet=1.50, vatRate=23, priceGross=1.85

            ProductsTable products = new ProductsTable("p")
            Expression<BigDecimal> addExpression = products.priceNet.add(products.vatRate)

        when:
            List<Record> records = session.select(products.name, addExpression.as("total"))
                    .from(products)
                    .orderBy(products.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(products.name) == "A"
                r.get("total") == new BigDecimal("24.00")  // 1.00 + 23
            }
            with(records[1]) { r ->
                r.get(products.name) == "B"
                r.get("total") == new BigDecimal("24.50")  // 1.50 + 23
            }

        where:
            session << allSessions
    }

    def "subtract BigDecimal entity columns"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addProductA()  // priceGross=1.23, priceNet=1.00
            fixtures.addProductB()  // priceGross=1.85, priceNet=1.50

            ProductsTable products = new ProductsTable("p")
            Expression<BigDecimal> subtractExpression = products.priceGross.subtract(products.priceNet)

        when:
            List<Record> records = session.select(products.name, subtractExpression.as("vat_amount"))
                    .from(products)
                    .orderBy(products.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(products.name) == "A"
                r.get("vat_amount") == new BigDecimal("0.23")  // 1.23 - 1.00
            }
            with(records[1]) { r ->
                r.get(products.name) == "B"
                r.get("vat_amount") == new BigDecimal("0.35")  // 1.85 - 1.50
            }

        where:
            session << allSessions
    }

    def "multiply BigDecimal entity column by constant"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addProductA()  // priceNet=1.00
            fixtures.addProductB()  // priceNet=1.50

            ProductsTable products = new ProductsTable("p")
            Expression<BigDecimal> multiplyExpression = products.priceNet.multiply(new BigDecimal("10"))

        when:
            List<Record> records = session.select(products.name, multiplyExpression.as("ten_times"))
                    .from(products)
                    .orderBy(products.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(products.name) == "A"
                r.get("ten_times") == new BigDecimal("10.00")  // 1.00 * 10
            }
            with(records[1]) { r ->
                r.get(products.name) == "B"
                r.get("ten_times") == new BigDecimal("15.00")  // 1.50 * 10
            }

        where:
            session << allSessions
    }

    def "divide BigDecimal entity column by constant"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addProductA()  // priceNet=1.00
            fixtures.addProductB()  // priceNet=1.50

            ProductsTable products = new ProductsTable("p")
            Expression<BigDecimal> divideExpression = products.priceNet.divide(new BigDecimal("2"))

        when:
            List<Record> records = session.select(products.name, divideExpression.as("half_price"))
                    .from(products)
                    .orderBy(products.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(products.name) == "A"
                r.get("half_price") == new BigDecimal("0.50")  // 1.00 / 2
            }
            with(records[1]) { r ->
                r.get(products.name) == "B"
                r.get("half_price") == new BigDecimal("0.75")  // 1.50 / 2
            }

        where:
            session << allSessions
    }

    // ===========================================
    // Complex arithmetic expressions on entity columns
    // ===========================================

    def "complex arithmetic expression: (numero * 2) + 5"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)

            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> complexExpression = basics.numero.multiply(2).add(5)

        when:
            List<Record> records = session.select(basics.name, complexExpression.as("result"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get("result") == 25  // (10 * 2) + 5
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get("result") == 45  // (20 * 2) + 5
            }

        where:
            session << allSessions
    }

    def "arithmetic on two entity columns: numero column add itself"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)

            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> doubleExpression = basics.numero.add(basics.numero)

        when:
            List<Record> records = session.select(basics.name, doubleExpression.as("doubled"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get("doubled") == 20  // 10 + 10
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get("doubled") == 40  // 20 + 20
            }

        where:
            session << allSessions
    }

    def "filter using arithmetic expression in where clause"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 15)
            fixtures.addBasic(3L, "C", 20)

            BasicsTable basics = new BasicsTable(null)

        when:
            // Select records where numero % 10 == 0
            List<Record> records = session.select(basics.name, basics.numero)
                    .from(basics)
                    .where(basics.numero.modulo(10).eq(0))
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get(basics.numero) == 10
            }
            with(records[1]) { r ->
                r.get(basics.name) == "C"
                r.get(basics.numero) == 20
            }

        where:
            session << allSessions
    }
}
