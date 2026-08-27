// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.*
import io.github.thinkfastpl.harbororm.test.domain.repository.AccountRepository
import io.github.thinkfastpl.harbororm.test.domain.repository.BasicRepository

import java.sql.SQLException

class ConstraintViolationIT extends AbstractHarborIT {

    def "should throw exception when inserting duplicate primary key"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            BasicEntity entity1 = new BasicEntity(1L, "First Entity", 42)
            BasicEntity entity2 = new BasicEntity(1L, "Second Entity", 99)

        when:
            repository.insert(entity1)
            repository.insert(entity2)

        then:
            def ex = thrown(RuntimeException)
            def rootCause = getRootCause(ex)
            rootCause instanceof SQLException
            def message = rootCause.message.toLowerCase()
            message.contains("primary key") ||
                message.contains("duplicate") ||
                message.contains("unique")

        where:
            session << allSessions
    }

    private static Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable
        while (cause.cause != null && cause.cause != cause) {
            cause = cause.cause
        }
        return cause
    }

    def "should throw exception when inserting with invalid foreign key reference"() {
        given:
            QOrderEntity qOrderEntity = new QOrderEntity(null)
            UUID nonExistentUserId = UUID.randomUUID()

            OrderEntity orderEntity = new OrderEntity(
                    1L,
                    nonExistentUserId,  // Foreign key to non-existent user
                    io.github.thinkfastpl.harbororm.test.domain.dto.OrderState.NEW,
                    new BigDecimal("100.00"),
                    new BigDecimal("123.00"),
                    new AddressValue("region", "12345", "city", "street", "1", null),
                    new AddressValue("region", "12345", "city", "street", "1", null),
                    []
            )

        when:
            session.insertEntity(qOrderEntity, orderEntity)

        then:
            def ex = thrown(RuntimeException)
            def rootCause = getRootCause(ex)
            rootCause instanceof SQLException
            def message = rootCause.message.toLowerCase()
            message.contains("foreign key") ||
                message.contains("violates") ||
                message.contains("constraint") ||
                message.contains("referential")

        where:
            session << allSessions
    }

    def "should throw exception when inserting duplicate on unique constraint"() {
        given:
            AccountRepository repository = new AccountRepository(session)
            AccountEntity account1 = new AccountEntity(1L, "john_doe", "john@example.com", 25)
            AccountEntity account2 = new AccountEntity(2L, "john_doe", "jane@example.com", 30)  // Duplicate username

        when:
            repository.insert(account1)
            repository.insert(account2)

        then:
            def ex = thrown(RuntimeException)
            def rootCause = getRootCause(ex)
            rootCause instanceof SQLException
            def message = rootCause.message.toLowerCase()
            message.contains("unique") ||
                message.contains("duplicate") ||
                message.contains("constraint")

        where:
            session << allSessions
    }

    def "should throw exception when inserting duplicate email on unique constraint"() {
        given:
            AccountRepository repository = new AccountRepository(session)
            AccountEntity account1 = new AccountEntity(3L, "alice", "alice@example.com", 25)
            AccountEntity account2 = new AccountEntity(4L, "bob", "alice@example.com", 30)  // Duplicate email

        when:
            repository.insert(account1)
            repository.insert(account2)

        then:
            def ex = thrown(RuntimeException)
            def rootCause = getRootCause(ex)
            rootCause instanceof SQLException
            def message = rootCause.message.toLowerCase()
            message.contains("unique") ||
                message.contains("duplicate") ||
                message.contains("constraint")

        where:
            session << allSessions
    }

    def "should throw exception when inserting null on not null column"() {
        given:
            AccountRepository repository = new AccountRepository(session)
            // Create account with null username (NOT NULL constraint)
            AccountEntity account = new AccountEntity(6L, null, "test@example.com", 25)

        when:
            repository.insert(account)

        then:
            def ex = thrown(RuntimeException)
            def rootCause = getRootCause(ex)
            rootCause instanceof SQLException
            def message = rootCause.message.toLowerCase()
            message.contains("null") ||
                message.contains("constraint")

        where:
            session << allSessions
    }

    def "should throw exception when violating check constraint"() {
        given:
            AccountRepository repository = new AccountRepository(session)
            AccountEntity account = new AccountEntity(5L, "youngster", "young@example.com", 16)  // age < 18

        when:
            repository.insert(account)

        then:
            def ex = thrown(RuntimeException)
            def rootCause = getRootCause(ex)
            rootCause instanceof SQLException
            def message = rootCause.message.toLowerCase()
            message.contains("check") ||
                message.contains("constraint") ||
                message.contains("violat")

        where:
            session << allSessions
    }

    def "should allow inserting valid account meeting all constraints"() {
        given:
            AccountRepository repository = new AccountRepository(session)
            AccountEntity account = new AccountEntity(10L, "validuser", "valid@example.com", 25)

        when:
            repository.insert(account)

        then:
            noExceptionThrown()

            QAccountEntity qAccountEntity = new QAccountEntity(null)
            with(session.selectEntity(qAccountEntity).fetchAll()) { accounts ->
                accounts.size() == 1
                with(accounts[0]) { acc ->
                    acc.id == 10L
                    acc.username == "validuser"
                    acc.email == "valid@example.com"
                    acc.age == 25
                }
            }

        where:
            session << allSessions
    }
}
