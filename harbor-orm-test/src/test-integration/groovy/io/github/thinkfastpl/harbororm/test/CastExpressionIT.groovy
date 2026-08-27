// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.api.query.result.Record
import io.github.thinkfastpl.harbororm.query.BasicsTable
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

import java.sql.Date
import java.sql.Time
import java.sql.Timestamp

/**
 * Integration tests for CAST expression functionality.
 * Tests type conversions across numeric, string, date/time types.
 *
 * Covers:
 * 1. Cast numeric types (INT to BIGINT, numeric to INT, etc.)
 * 2. Cast to/from strings (VARCHAR to INT, INT to VARCHAR)
 * 3. Cast date/time types (TIMESTAMP to DATE, DATE to VARCHAR)
 * 4. Cast in WHERE clause (type-safe comparisons)
 * 5. Cast in SELECT clause (return casted values)
 * 6. Cast NULL values
 */
class CastExpressionIT extends AbstractHarborIT {

    // ===========================================
    // Cast numeric types
    // ===========================================

    def "cast integer to long"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 100)
            fixtures.addBasic(2L, "B", 200)

            BasicsTable basics = new BasicsTable(null)
            Expression<Long> castExpression = basics.numero.cast(Long.class)

        when:
            List<Record> records = session.select(basics.name, castExpression.as("numero_long"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get("numero_long") == 100L
                r.get("numero_long").class == Long.class
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get("numero_long") == 200L
                r.get("numero_long").class == Long.class
            }

        where:
            session << allSessions
    }

    def "cast integer to short"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)

            BasicsTable basics = new BasicsTable(null)
            Expression<Short> castExpression = basics.numero.cast(Short.class)

        when:
            List<Record> records = session.select(basics.name, castExpression.as("numero_short"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get("numero_short") == (short) 10
                r.get("numero_short").class == Short.class
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get("numero_short") == (short) 20
                r.get("numero_short").class == Short.class
            }

        where:
            session << allSessions
    }

    def "cast integer to bigdecimal"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 100)
            fixtures.addBasic(2L, "B", 200)

            BasicsTable basics = new BasicsTable(null)
            Expression<BigDecimal> castExpression = basics.numero.cast(BigDecimal.class)

        when:
            List<Record> records = session.select(basics.name, castExpression.as("numero_decimal"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get("numero_decimal") == new BigDecimal("100")
                r.get("numero_decimal").class == BigDecimal.class
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get("numero_decimal") == new BigDecimal("200")
                r.get("numero_decimal").class == BigDecimal.class
            }

        where:
            session << allSessions
    }

    // ===========================================
    // Cast to/from strings
    // ===========================================

    def "cast integer to string"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 123)
            fixtures.addBasic(2L, "B", 456)

            BasicsTable basics = new BasicsTable(null)
            Expression<String> castExpression = basics.numero.cast(String.class)

        when:
            List<Record> records = session.select(basics.name, castExpression.as("numero_str"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get("numero_str") == "123"
                r.get("numero_str").class == String.class
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get("numero_str") == "456"
                r.get("numero_str").class == String.class
            }

        where:
            session << allSessions
    }

    def "cast string to integer using constant"() {
        given:
            Expression<Integer> castExpression = DSL.constant("999").cast(Integer.class)

        when:
            Integer result = session.select(castExpression).fetchSingle()

        then:
            result == 999
            result.class == Integer.class

        where:
            session << allSessions
    }

    def "cast name expression string to integer"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            // Use name expression for string column that contains numeric value
            Expression<Integer> castExpression = DSL.name(String.class, "name").cast(Integer.class)

        when:
            // Insert test data with numeric strings
            session.insertInto(basics)
                    .set(basics.id, 3L)
                    .set(basics.name, "100")
                    .set(basics.numero, 1)
                    .execute()
            session.insertInto(basics)
                    .set(basics.id, 4L)
                    .set(basics.name, "200")
                    .set(basics.numero, 2)
                    .execute()

            List<Record> records = session.select(basics.id, castExpression.as("name_as_int"))
                    .from(basics)
                    .where(basics.id.ge(3L))
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.id) == 3L
                r.get("name_as_int") == 100
            }
            with(records[1]) { r ->
                r.get(basics.id) == 4L
                r.get("name_as_int") == 200
            }

        where:
            session << allSessions
    }

    // ===========================================
    // Cast date/time types
    // ===========================================

    def "cast timestamp to date"() {
        given:
            Expression<Timestamp> timestampExpr = DSL.name(Timestamp.class, "event_timestamp")
            Expression<Date> castExpression = timestampExpr.cast(Date.class)

        when:
            List<Record> records = session.select(DSL.name(Long.class, "id"), castExpression.as("date_only"))
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchAll()

        then:
            records.size() == 1
            with(records[0]) { r ->
                r.get(1) == 1L
                // Should be date only, no time portion
                r.get("date_only").class == Date.class
                r.get("date_only").toString() == "2024-06-15"
            }

        where:
            session << allSessions
    }

    def "cast timestamp to time"() {
        given:
            Expression<Timestamp> timestampExpr = DSL.name(Timestamp.class, "event_timestamp")
            Expression<Time> castExpression = timestampExpr.cast(Time.class)

        when:
            List<Record> records = session.select(DSL.name(Long.class, "id"), castExpression.as("time_only"))
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchAll()

        then:
            records.size() == 1
            with(records[0]) { r ->
                r.get(1) == 1L
                // Should be time only
                r.get("time_only").class == Time.class
                r.get("time_only").toString() == "14:30:45"
            }

        where:
            session << allSessions
    }

    def "cast date to string"() {
        given:
            Expression<Date> dateExpr = DSL.name(Date.class, "event_date")
            Expression<String> castExpression = dateExpr.cast(String.class)

        when:
            List<Record> records = session.select(DSL.name(Long.class, "id"), castExpression.as("date_str"))
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchAll()

        then:
            records.size() == 1
            with(records[0]) { r ->
                r.get(1) == 1L
                r.get("date_str").class == String.class
                r.get("date_str").contains("2024")
                r.get("date_str").contains("06")
                r.get("date_str").contains("15")
            }

        where:
            session << allSessions
    }

    def "cast timestamp to string"() {
        given:
            Expression<Timestamp> timestampExpr = DSL.name(Timestamp.class, "event_timestamp")
            Expression<String> castExpression = timestampExpr.cast(String.class)

        when:
            List<Record> records = session.select(DSL.name(Long.class, "id"), castExpression.as("ts_str"))
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchAll()

        then:
            records.size() == 1
            with(records[0]) { r ->
                r.get(1) == 1L
                r.get("ts_str").class == String.class
                r.get("ts_str").contains("2024")
                r.get("ts_str").contains("06")
                r.get("ts_str").contains("15")
            }

        where:
            session << allSessions
    }

    // ===========================================
    // Cast in WHERE clause
    // ===========================================

    def "use cast in where clause for type-safe comparison"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 100)
            fixtures.addBasic(2L, "B", 200)
            fixtures.addBasic(3L, "C", 300)

            BasicsTable basics = new BasicsTable(null)
            // Cast numero to Long and compare with Long value
            Expression<Long> castExpression = basics.numero.cast(Long.class)

        when:
            List<Record> records = session.select(basics.name, basics.numero)
                    .from(basics)
                    .where(castExpression.gt(150L))
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(basics.name) == "B"
            records[0].get(basics.numero) == 200
            records[1].get(basics.name) == "C"
            records[1].get(basics.numero) == 300

        where:
            session << allSessions
    }

    def "cast string column to integer in where clause"() {
        given:
            BasicsTable basics = new BasicsTable(null)
            Expression<Integer> castExpression = basics.name.cast(Integer.class)

        when:
            // Insert test data with numeric strings in name column
            session.insertInto(basics)
                    .set(basics.id, 10L)
                    .set(basics.name, "50")
                    .set(basics.numero, 1)
                    .execute()
            session.insertInto(basics)
                    .set(basics.id, 11L)
                    .set(basics.name, "150")
                    .set(basics.numero, 2)
                    .execute()
            session.insertInto(basics)
                    .set(basics.id, 12L)
                    .set(basics.name, "250")
                    .set(basics.numero, 3)
                    .execute()

            List<Record> records = session.select(basics.id, basics.name)
                    .from(basics)
                    .where(basics.id.ge(10L).and(castExpression.gt(100)))
                    .orderBy(basics.id.asc())
                    .fetchAll()

        then:
            records.size() == 2
            records[0].get(basics.id) == 11L
            records[0].get(basics.name) == "150"
            records[1].get(basics.id) == 12L
            records[1].get(basics.name) == "250"

        where:
            session << allSessions
    }

    // ===========================================
    // Cast NULL values
    // ===========================================

    def "cast null constant to integer"() {
        given:
            Expression<Integer> castExpression = DSL.constant(Integer.class, null).cast(Integer.class)

        when:
            Integer result = session.select(castExpression).fetchSingle()

        then:
            result == null

        where:
            session << allSessions
    }

    def "cast null constant to string"() {
        given:
            Expression<String> castExpression = DSL.constant(String.class, null).cast(String.class)

        when:
            String result = session.select(castExpression).fetchSingle()

        then:
            result == null

        where:
            session << allSessions
    }

    def "cast null constant to bigdecimal"() {
        given:
            Expression<BigDecimal> castExpression = DSL.constant(BigDecimal.class, null).cast(BigDecimal.class)

        when:
            BigDecimal result = session.select(castExpression).fetchSingle()

        then:
            result == null

        where:
            session << allSessions
    }

    // ===========================================
    // Complex cast scenarios
    // ===========================================

    def "chain cast with arithmetic operations"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 10)
            fixtures.addBasic(2L, "B", 20)

            BasicsTable basics = new BasicsTable(null)
            // Cast to long, multiply, then cast to string
            Expression<Long> castToLong = basics.numero.cast(Long.class)
            Expression<Long> multiplied = castToLong.multiply(10L)
            Expression<String> castToString = multiplied.cast(String.class)

        when:
            List<Record> records = session.select(basics.name, castToString.as("result"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get("result") == "100"
                r.get("result").class == String.class
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get("result") == "200"
                r.get("result").class == String.class
            }

        where:
            session << allSessions
    }

    def "cast with aliasing"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 42)

            BasicsTable basics = new BasicsTable(null)
            Expression<String> castExpression = basics.numero.cast(String.class).as("numero_as_text")

        when:
            List<Record> records = session.select(basics.name, castExpression)
                    .from(basics)
                    .where(basics.id.eq(1L))
                    .fetchAll()

        then:
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get("numero_as_text") == "42"
                r.get(castExpression) == "42"
            }

        where:
            session << allSessions
    }

    def "cast integer to double"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 100)
            fixtures.addBasic(2L, "B", 200)

            BasicsTable basics = new BasicsTable(null)
            Expression<Double> castExpression = basics.numero.cast(Double.class)

        when:
            List<Record> records = session.select(basics.name, castExpression.as("numero_double"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                (r.get("numero_double") as Double) == 100.0d
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                (r.get("numero_double") as Double) == 200.0d
            }

        where:
            session << allSessions
    }

    def "cast integer to boolean"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 1)
            fixtures.addBasic(2L, "B", 0)

            BasicsTable basics = new BasicsTable(null)
            Expression<Boolean> castExpression = basics.numero.cast(Boolean.class)

        when:
            List<Record> records = session.select(basics.name, castExpression.as("numero_bool"))
                    .from(basics)
                    .orderBy(basics.name.asc())
                    .fetchAll()

        then:
            records.size() == 2
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get("numero_bool") == true
            }
            with(records[1]) { r ->
                r.get(basics.name) == "B"
                r.get("numero_bool") == false
            }

        where:
            session << allSessions
    }

    def "cast identity - casting to same type returns same expression"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "A", 100)

            BasicsTable basics = new BasicsTable(null)
            // Cast to the same type (Integer -> Integer)
            Expression<Integer> castExpression = basics.numero.cast(Integer.class)

        when:
            List<Record> records = session.select(basics.name, castExpression.as("numero_int"))
                    .from(basics)
                    .where(basics.id.eq(1L))
                    .fetchAll()

        then:
            records.size() == 1
            with(records[0]) { r ->
                r.get(basics.name) == "A"
                r.get("numero_int") == 100
                r.get("numero_int").class == Integer.class
            }

        where:
            session << allSessions
    }
}
