// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.QBasicEntity
import io.github.thinkfastpl.harbororm.test.domain.fixtures.TestFixtures

class EntityExistsIT extends AbstractHarborIT {

    // ============================================
    // Basic exists() tests
    // ============================================

    def "exists should return true when entity matches WHERE condition"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            boolean result = session.selectEntity(qEntity)
                    .where(qEntity.name.eq("Bob"))
                    .exists()

        then:
            result == true

        where:
            session << allSessions
    }

    def "exists should return false when no entity matches WHERE condition"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            boolean result = session.selectEntity(qEntity)
                    .where(qEntity.name.eq("NonExistent"))
                    .exists()

        then:
            result == false

        where:
            session << allSessions
    }

    def "exists should return false on empty table"() {
        given:
            // No data inserted - table is empty
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            boolean result = session.selectEntity(qEntity)
                    .exists()

        then:
            result == false

        where:
            session << allSessions
    }

    def "exists should return true with no WHERE clause when records exist"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            boolean result = session.selectEntity(qEntity)
                    .exists()

        then:
            result == true

        where:
            session << allSessions
    }

    // ============================================
    // Complex WHERE conditions tests
    // ============================================

    def "exists should work with AND conditions"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Alice", 300)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            boolean result = session.selectEntity(qEntity)
                    .where(qEntity.name.eq("Alice").and(qEntity.numero.eq(100)))
                    .exists()

        then:
            result == true

        when:
            boolean noMatch = session.selectEntity(qEntity)
                    .where(qEntity.name.eq("Alice").and(qEntity.numero.eq(999)))
                    .exists()

        then:
            noMatch == false

        where:
            session << allSessions
    }

    def "exists should work with OR conditions"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            boolean result = session.selectEntity(qEntity)
                    .where(qEntity.name.eq("Alice").or(qEntity.name.eq("Charlie")))
                    .exists()

        then:
            result == true

        when:
            boolean noMatch = session.selectEntity(qEntity)
                    .where(qEntity.name.eq("Nonexistent").or(qEntity.name.eq("AlsoNonexistent")))
                    .exists()

        then:
            noMatch == false

        where:
            session << allSessions
    }

    def "exists should work with complex AND/OR combinations"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)
            fixtures.addBasic(4L, "Alice", 400)
            QBasicEntity qEntity = new QBasicEntity(null)

        when: "complex condition: (name = 'Alice' AND numero > 200) OR (name = 'Bob')"
            boolean result = session.selectEntity(qEntity)
                    .where(
                            qEntity.name.eq("Alice").and(qEntity.numero.gt(200))
                                    .or(qEntity.name.eq("Bob"))
                    )
                    .exists()

        then:
            result == true

        when: "complex condition that matches nothing: (name = 'Alice' AND numero > 500) OR (name = 'Unknown')"
            boolean noMatch = session.selectEntity(qEntity)
                    .where(
                            qEntity.name.eq("Alice").and(qEntity.numero.gt(500))
                                    .or(qEntity.name.eq("Unknown"))
                    )
                    .exists()

        then:
            noMatch == false

        where:
            session << allSessions
    }

    // ============================================
    // Additional condition types
    // ============================================

    def "exists should work with numeric comparisons"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            boolean greaterThan = session.selectEntity(qEntity)
                    .where(qEntity.numero.gt(250))
                    .exists()

        then:
            greaterThan == true

        when:
            boolean lessThan = session.selectEntity(qEntity)
                    .where(qEntity.numero.lt(50))
                    .exists()

        then:
            lessThan == false

        when:
            boolean between = session.selectEntity(qEntity)
                    .where(qEntity.numero.between(150, 250))
                    .exists()

        then:
            between == true

        where:
            session << allSessions
    }

    def "exists should work with whereIdEq"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            boolean existsById = session.selectEntity(qEntity)
                    .whereIdEq(1L)
                    .exists()

        then:
            existsById == true

        when:
            boolean notExistsById = session.selectEntity(qEntity)
                    .whereIdEq(999L)
                    .exists()

        then:
            notExistsById == false

        where:
            session << allSessions
    }

    def "exists should work with whereIdIn"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            fixtures.addBasic(2L, "Bob", 200)
            fixtures.addBasic(3L, "Charlie", 300)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            boolean existsByIds = session.selectEntity(qEntity)
                    .whereIdIn([1L, 3L])
                    .exists()

        then:
            existsByIds == true

        when:
            boolean notExistsByIds = session.selectEntity(qEntity)
                    .whereIdIn([888L, 999L])
                    .exists()

        then:
            notExistsByIds == false

        where:
            session << allSessions
    }

    // ============================================
    // Return type verification
    // ============================================

    def "exists should return primitive boolean type"() {
        given:
            TestFixtures fixtures = new TestFixtures(session)
            fixtures.addBasic(1L, "Alice", 100)
            QBasicEntity qEntity = new QBasicEntity(null)

        when:
            def result = session.selectEntity(qEntity).exists()

        then:
            result instanceof Boolean
            result == true

        when:
            def noResult = session.selectEntity(qEntity)
                    .where(qEntity.name.eq("NonExistent"))
                    .exists()

        then:
            noResult instanceof Boolean
            noResult == false

        where:
            session << allSessions
    }
}
