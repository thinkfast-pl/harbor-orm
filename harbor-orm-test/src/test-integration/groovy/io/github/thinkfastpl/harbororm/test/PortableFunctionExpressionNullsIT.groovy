// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.Expression
import io.github.thinkfastpl.harbororm.test.domain.QUserEntity
import io.github.thinkfastpl.harbororm.test.domain.UserEntity

class PortableFunctionExpressionNullsIT extends AbstractHarborIT {

    def "COALESCE returns first non-null value with 2 arguments"() {
        expect:
            session.select(DSL.coalesce(DSL.constant("First"), DSL.constant("Second"))).fetchSingle() == "First"

        where:
            session << allSessions
    }

    def "COALESCE returns second value when first is null with 2 arguments"() {
        expect:
            session.select(DSL.coalesce(DSL.nil(String), DSL.constant("Second"))).fetchSingle() == "Second"

        where:
            session << allSessions
    }

    def "COALESCE returns null when all arguments are null with 2 arguments"() {
        expect:
            session.select(DSL.coalesce(DSL.nil(String), DSL.nil(String))).fetchSingle() == null

        where:
            session << allSessions
    }

    def "COALESCE returns first non-null value with 3 arguments"() {
        expect:
            session.select(
                    DSL.coalesce(
                            DSL.nil(String),
                            DSL.constant("Second"),
                            DSL.constant("Third")
                    )
            ).fetchSingle() == "Second"

        where:
            session << allSessions
    }

    def "COALESCE returns third value when first two are null with 3 arguments"() {
        expect:
            session.select(
                    DSL.coalesce(
                            DSL.nil(String),
                            DSL.nil(String),
                            DSL.constant("Third")
                    )
            ).fetchSingle() == "Third"

        where:
            session << allSessions
    }

    def "COALESCE returns first non-null value with 4 arguments"() {
        expect:
            session.select(
                    DSL.coalesce(
                            DSL.nil(Integer),
                            DSL.nil(Integer),
                            DSL.constant(42),
                            DSL.constant(100)
                    )
            ).fetchSingle() == 42

        where:
            session << allSessions
    }

    def "COALESCE works with numeric types"() {
        expect:
            session.select(
                    DSL.coalesce(
                            DSL.nil(BigDecimal),
                            DSL.constant(new BigDecimal("123.45"))
                    )
            ).fetchSingle() == new BigDecimal("123.45")

        where:
            session << allSessions
    }

    def "COALESCE works with entity columns"() {
        given:
            QUserEntity qUser = new QUserEntity(null)
            UUID userId = UUID.randomUUID()

            UserEntity user = new UserEntity(
                    userId,
                    "test@example.com",
                    "password123",
                    "John",
                    "Doe",
                    null  // phoneNumber is null
            )
            session.insertEntity(qUser, user)

        when:
            def result = session.select(
                    DSL.coalesce(qUser.phoneNumber, DSL.constant("No phone"))
            )
                    .from("users")
                    .where(qUser.id.eq(userId))
                    .fetchSingle()

        then:
            result == "No phone"

        cleanup:
            session.delete().from("users").where(qUser.id.eq(userId)).execute()

        where:
            session << allSessions
    }

    def "COALESCE in WHERE clause filters correctly"() {
        given:
            QUserEntity qUser = new QUserEntity(null)
            UUID userId1 = UUID.randomUUID()
            UUID userId2 = UUID.randomUUID()

            UserEntity user1 = new UserEntity(
                    userId1,
                    "user1@example.com",
                    "password1",
                    "Jane",
                    "Smith",
                    "555-1234"
            )
            UserEntity user2 = new UserEntity(
                    userId2,
                    "user2@example.com",
                    "password2",
                    "Bob",
                    "Jones",
                    null
            )
            session.insertEntity(qUser, user1)
            session.insertEntity(qUser, user2)

        when:
            def results = session.selectEntity(qUser)
                    .where(
                            DSL.coalesce(qUser.phoneNumber, DSL.constant("NONE"))
                                    .eq("NONE")
                    )
                    .fetchAll()

        then:
            results.size() == 1
            results[0].id == userId2
            results[0].phoneNumber == null

        cleanup:
            session.delete().from("users").where(qUser.id.in([userId1, userId2])).execute()

        where:
            session << allSessions
    }

    def "COALESCE with multiple entity columns"() {
        given:
            QUserEntity qUser = new QUserEntity(null)
            UUID userId = UUID.randomUUID()

            UserEntity user = new UserEntity(
                    userId,
                    "test@example.com",
                    "password123",
                    "John",
                    "Doe",
                    null  // phoneNumber is null
            )
            session.insertEntity(qUser, user)

        when:
            def result = session.select(
                    DSL.coalesce(
                            qUser.phoneNumber,
                            qUser.email,
                            DSL.constant("No contact")
                    )
            )
                    .from("users")
                    .where(qUser.id.eq(userId))
                    .fetchSingle()

        then:
            result == "test@example.com"  // Falls back to email since phone is null

        cleanup:
            session.delete().from("users").where(qUser.id.eq(userId)).execute()

        where:
            session << allSessions
    }

    def "COALESCE works in SELECT list with aliases"() {
        given:
            QUserEntity qUser = new QUserEntity(null)
            UUID userId = UUID.randomUUID()

            UserEntity user = new UserEntity(
                    userId,
                    "test@example.com",
                    "password123",
                    "John",
                    "Doe",
                    null
            )
            session.insertEntity(qUser, user)

        when:
            def results = session.select(
                    qUser.email,
                    DSL.coalesce(qUser.phoneNumber, DSL.constant("N/A"))
            )
                    .from("users")
                    .where(qUser.id.eq(userId))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].get(1) == "test@example.com"
            results[0].get(2) == "N/A"

        cleanup:
            session.delete().from("users").where(qUser.id.eq(userId)).execute()

        where:
            session << allSessions
    }

    def "COALESCE with 5+ arguments returns first non-null"() {
        expect:
            session.select(
                    DSL.coalesce(
                            DSL.nil(String),
                            DSL.nil(String),
                            DSL.nil(String),
                            DSL.nil(String),
                            DSL.constant("Fifth"),
                            DSL.constant("Sixth")
                    )
            ).fetchSingle() == "Fifth"

        where:
            session << allSessions
    }

    def "COALESCE(Expression, value) returns expression when not null"() {
        expect:
            session.select(DSL.coalesce(DSL.constant("First"), "Fallback")).fetchSingle() == "First"

        where:
            session << allSessions
    }

    def "COALESCE(Expression, value) returns value when expression is null"() {
        expect:
            session.select(DSL.coalesce(DSL.nil(String), "Fallback")).fetchSingle() == "Fallback"

        where:
            session << allSessions
    }

    def "COALESCE(Expression, value) works with Integer"() {
        expect:
            session.select(DSL.coalesce(DSL.nil(Integer), 42)).fetchSingle() == 42

        where:
            session << allSessions
    }

    def "COALESCE(Expression, value, value...) returns first non-null"() {
        expect:
            session.select(DSL.coalesce(DSL.nil(String), "Second", "Third")).fetchSingle() == "Second"

        where:
            session << allSessions
    }

    def "COALESCE(Expression, value, value...) with entity column falls through to last value"() {
        given:
            QUserEntity qUser = new QUserEntity(null)
            UUID userId = UUID.randomUUID()

            UserEntity user = new UserEntity(
                    userId,
                    "test@example.com",
                    "password123",
                    "John",
                    "Doe",
                    null
            )
            session.insertEntity(qUser, user)

        when:
            def result = session.select(
                    DSL.coalesce(qUser.phoneNumber, "Fallback1", "Fallback2")
            )
                    .from("users")
                    .where(qUser.id.eq(userId))
                    .fetchSingle()

        then:
            result == "Fallback1"

        cleanup:
            session.delete().from("users").where(qUser.id.eq(userId)).execute()

        where:
            session << allSessions
    }

    // ==================== NULLIF TESTS ====================

    def "select nullif() returns null when equal"() {
        when:
            Integer result = session.select(DSL.nullif(DSL.constant(5), 5)).fetchSingle()

        then:
            result == null

        where:
            session << allSessions
    }

    def "select nullif() returns first value when not equal"() {
        when:
            Integer result = session.select(DSL.nullif(DSL.constant(5), 3)).fetchSingle()

        then:
            result == 5

        where:
            session << allSessions
    }

    def "NULLIF returns null when equal"() {
        expect:
            session.select(DSL.nullif(DSL.constant(1), DSL.constant(1))).fetchSingle() == null

        where:
            session << allSessions
    }

    def "NULLIF returns first value when not equal"() {
        expect:
            session.select(DSL.nullif(DSL.constant(1), DSL.constant(2))).fetchSingle() == 1

        where:
            session << allSessions
    }

    def "IFNULL returns value when not null"() {
        expect:
            session.select(DSL.ifNull(DSL.constant("Hello"), "Default")).fetchSingle() == "Hello"

        where:
            session << getSessionsExcept(DbType.POSTGRES)
    }

    def "IFNULL returns default when null"() {
        expect:
            session.select(DSL.ifNull(DSL.nil(String), "Default")).fetchSingle() == "Default"

        where:
            session << getSessionsExcept(DbType.POSTGRES)
    }

    def "NVL returns value when not null"() {
        expect:
            session.select(DSL.nvl(DSL.constant("Hello"), "Default")).fetchSingle() == "Hello"

        where:
            session << getSessionsExcept(DbType.POSTGRES)
    }

    def "NVL returns default when null"() {
        expect:
            session.select(DSL.nvl(DSL.nil(String), "Default")).fetchSingle() == "Default"

        where:
            session << getSessionsExcept(DbType.POSTGRES)
    }

    def "NULLIF preserves column Java type (H-13)"() {
        given:
            Expression<Long> idExpr = DSL.name(Long.class, "id")
            Expression<Long> expr = DSL.nullif(idExpr, DSL.constant(Long.class, 999L))

        expect:
            expr.getJavaType() == Long.class
            def value = session.select(expr).from("employees").orderBy(DSL.asc("id")).fetchAll().get(0)
            (value instanceof Long)

        where:
            session << allSessions
    }

    def "NULLIF"() {
        expect:
            def value = session.select(DSL.nullif(expr1, expr2)).fetchAll().get(0)
            value == result

        where:
            session << allSessions

        combined:
            expr1 | expr2 || result
            DSL.constant(1) | DSL.constant(1) || null
            DSL.constant(1) | DSL.constant(2) || 1
            DSL.constant(1) | DSL.nil(Integer.class) || 1
            DSL.nil(Integer.class) | DSL.constant(2) || null
            DSL.nil(Integer.class) | DSL.nil(Integer.class) || null
    }

    def "IFNULL"() {
        expect:
            def value = session.select(DSL.ifNull(expr1, expr2)).fetchAll().get(0)
            value == result

        where:
            session << allSessions

        combined:
            expr1 | expr2 || result
            DSL.constant(1) | DSL.constant(2) || 1
            DSL.constant(1) | DSL.nil(Integer.class) || 1
            DSL.nil(Integer.class) | DSL.constant(2) || 2
            DSL.nil(Integer.class) | DSL.nil(Integer.class) || null
    }

    def "IFNULL preserves column Java type (H-13)"() {
        given:
            Expression<BigDecimal> salaryExpr = DSL.name(BigDecimal.class, "salary")
            Expression<BigDecimal> expr = DSL.ifNull(salaryExpr, DSL.constant(BigDecimal.class, BigDecimal.ZERO))

        expect:
            expr.getJavaType() == BigDecimal.class
            def value = session.select(expr).from("employees").orderBy(DSL.asc("id")).fetchAll().get(0)
            (value instanceof BigDecimal)

        where:
            session << allSessions
    }

    def "NVL"() {
        expect:
            def value = session.select(DSL.nvl(expr1, expr2)).fetchAll().get(0)
            value == result

        where:
            session << allSessions

        combined:
            expr1 | expr2 || result
            DSL.constant(1) | DSL.constant(2) || 1
            DSL.constant(1) | DSL.nil(Integer.class) || 1
            DSL.nil(Integer.class) | DSL.constant(2) || 2
            DSL.nil(Integer.class) | DSL.nil(Integer.class) || null
    }

    def "NVL preserves column Java type (H-13)"() {
        given:
            Expression<String> nameExpr = DSL.name(String.class, "name")
            Expression<String> expr = DSL.nvl(nameExpr, DSL.constant(String.class, "Unknown"))

        expect:
            expr.getJavaType() == String.class
            def value = session.select(expr).from("employees").orderBy(DSL.asc("id")).fetchAll().get(0)
            (value instanceof String)

        where:
            session << allSessions
    }

    def "num_nulls()"() {
        when:
            int sum = session
                    .select(DSL.numNulls(
                            DSL.nil(Integer.class),
                            DSL.constant(1),
                            DSL.nil(Integer.class),
                            DSL.constant(2),
                            DSL.nil(Integer.class)
                    ))
                    .fetchSingle()

        then:
            sum == 3

        where:
            session << allSessions
    }

    def "num_nulls() with list"() {
        when:
            int sum = session
                    .select(DSL.numNulls([
                            DSL.nil(Integer.class),
                            DSL.constant(1),
                            DSL.nil(Integer.class)
                    ] as List<Expression<?>>))
                    .fetchSingle()

        then:
            sum == 2

        where:
            session << allSessions
    }

    def "num_nonnulls ()"() {
        when:
            int sum = session
                    .select(DSL.numNonNulls(
                            DSL.nil(Integer.class),
                            DSL.constant(1),
                            DSL.nil(Integer.class),
                            DSL.constant(2),
                            DSL.nil(Integer.class)
                    ))
                    .fetchSingle()

        then:
            sum == 2

        where:
            session << allSessions
    }

    def "num_nonnulls() with list"() {
        when:
            int sum = session
                    .select(DSL.numNonNulls([
                            DSL.nil(Integer.class),
                            DSL.constant(1),
                            DSL.constant(2)
                    ] as List<Expression<?>>))
                    .fetchSingle()

        then:
            sum == 2

        where:
            session << allSessions
    }
}
