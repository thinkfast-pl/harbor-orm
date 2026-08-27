// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.BasicEntity
import io.github.thinkfastpl.harbororm.test.domain.repository.BasicRepository

class RepositoryBulkMethodsIT extends AbstractHarborIT {

    // ============ insertAll tests ============

    def "should insert multiple entities using insertAll"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            List<BasicEntity> entities = [
                new BasicEntity(1L, "First", 100),
                new BasicEntity(2L, "Second", 200),
                new BasicEntity(3L, "Third", 300)
            ]

        when:
            repository.insertAll(entities)

        then:
            with(repository.findAll()) { results ->
                results.size() == 3
                results.find { it.id == 1L }.name == "First"
                results.find { it.id == 2L }.name == "Second"
                results.find { it.id == 3L }.name == "Third"
            }

        where:
            session << allSessions
    }

    def "should handle empty collection in insertAll"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            List<BasicEntity> entities = []

        when:
            repository.insertAll(entities)

        then:
            noExceptionThrown()
            repository.countAll() == 0

        where:
            session << allSessions
    }

    def "should insert single entity using insertAll"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            List<BasicEntity> entities = [new BasicEntity(1L, "Single", 999)]

        when:
            repository.insertAll(entities)

        then:
            repository.countAll() == 1
            with(repository.findById(1L).get()) { entity ->
                entity.name == "Single"
                entity.numero == 999
            }

        where:
            session << allSessions
    }

    // ============ deleteAll(List) tests ============

    def "should delete all entities from provided list using deleteAll"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            List<BasicEntity> entities = [
                new BasicEntity(1L, "ToDelete1", 100),
                new BasicEntity(2L, "ToDelete2", 200),
                new BasicEntity(3L, "ToDelete3", 300)
            ]
            repository.insertAll(entities)

        when:
            repository.deleteAll(entities)

        then:
            repository.countAll() == 0
            repository.findAll().isEmpty()

        where:
            session << allSessions
    }

    def "should handle empty list in deleteAll"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            repository.insert(new BasicEntity(1L, "Existing", 100))
            List<BasicEntity> emptyList = []

        when:
            repository.deleteAll(emptyList)

        then:
            noExceptionThrown()
            repository.countAll() == 1

        where:
            session << allSessions
    }

    def "should delete partial list of entities using deleteAll"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            BasicEntity entity1 = new BasicEntity(1L, "Keep", 100)
            BasicEntity entity2 = new BasicEntity(2L, "Delete", 200)
            BasicEntity entity3 = new BasicEntity(3L, "Delete", 300)
            repository.insertAll([entity1, entity2, entity3])

        when:
            repository.deleteAll([entity2, entity3])

        then:
            repository.countAll() == 1
            repository.findById(1L).isPresent()
            repository.findById(2L).isEmpty()
            repository.findById(3L).isEmpty()

        where:
            session << allSessions
    }

    // ============ deleteAllById tests ============

    def "should delete multiple entities by IDs using deleteAllById"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            repository.insertAll([
                new BasicEntity(1L, "Entity1", 100),
                new BasicEntity(2L, "Entity2", 200),
                new BasicEntity(3L, "Entity3", 300)
            ])

        when:
            repository.deleteAllById([1L, 2L, 3L])

        then:
            repository.countAll() == 0

        where:
            session << allSessions
    }

    def "should handle empty collection in deleteAllById"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            repository.insert(new BasicEntity(1L, "Existing", 100))
            Collection<Long> emptyIds = []

        when:
            repository.deleteAllById(emptyIds)

        then:
            noExceptionThrown()
            repository.countAll() == 1

        where:
            session << allSessions
    }

    def "should delete partial entities by IDs using deleteAllById"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            repository.insertAll([
                new BasicEntity(1L, "Keep", 100),
                new BasicEntity(2L, "Delete", 200),
                new BasicEntity(3L, "Delete", 300)
            ])

        when:
            repository.deleteAllById([2L, 3L])

        then:
            repository.countAll() == 1
            repository.findById(1L).isPresent()
            repository.findById(2L).isEmpty()
            repository.findById(3L).isEmpty()

        where:
            session << allSessions
    }

    def "should silently ignore non-existent IDs in deleteAllById"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            repository.insert(new BasicEntity(1L, "Existing", 100))

        when:
            repository.deleteAllById([1L, 999L, 888L])

        then:
            noExceptionThrown()
            repository.countAll() == 0

        where:
            session << allSessions
    }

    // ============ findAll tests ============

    def "should return all entities using findAll"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            repository.insertAll([
                new BasicEntity(1L, "First", 100),
                new BasicEntity(2L, "Second", 200),
                new BasicEntity(3L, "Third", 300)
            ])

        when:
            List<BasicEntity> results = repository.findAll()

        then:
            results.size() == 3
            results.find { it.id == 1L } != null
            results.find { it.id == 2L } != null
            results.find { it.id == 3L } != null

        where:
            session << allSessions
    }

    def "should return empty list when no entities exist using findAll"() {
        given:
            BasicRepository repository = new BasicRepository(session)

        when:
            List<BasicEntity> results = repository.findAll()

        then:
            results.isEmpty()

        where:
            session << allSessions
    }

    // ============ findAllById tests ============

    def "should find multiple entities by IDs using findAllById"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            repository.insertAll([
                new BasicEntity(1L, "First", 100),
                new BasicEntity(2L, "Second", 200),
                new BasicEntity(3L, "Third", 300)
            ])

        when:
            List<BasicEntity> results = repository.findAllById([1L, 3L])

        then:
            results.size() == 2
            results.find { it.id == 1L }?.name == "First"
            results.find { it.id == 3L }?.name == "Third"

        where:
            session << allSessions
    }

    def "should return empty list for empty IDs collection in findAllById"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            repository.insert(new BasicEntity(1L, "Existing", 100))

        when:
            List<BasicEntity> results = repository.findAllById([])

        then:
            results.isEmpty()

        where:
            session << allSessions
    }

    def "should return only existing entities when some IDs do not exist in findAllById"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            repository.insert(new BasicEntity(1L, "Existing", 100))

        when:
            List<BasicEntity> results = repository.findAllById([1L, 999L, 888L])

        then:
            results.size() == 1
            results[0].id == 1L

        where:
            session << allSessions
    }

    // ============ countAll tests ============

    def "should return correct count using countAll"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            repository.insertAll([
                new BasicEntity(1L, "First", 100),
                new BasicEntity(2L, "Second", 200),
                new BasicEntity(3L, "Third", 300)
            ])

        when:
            long count = repository.countAll()

        then:
            count == 3

        where:
            session << allSessions
    }

    def "should return zero count when no entities exist using countAll"() {
        given:
            BasicRepository repository = new BasicRepository(session)

        when:
            long count = repository.countAll()

        then:
            count == 0

        where:
            session << allSessions
    }

    def "should return updated count after insertions and deletions"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            repository.insertAll([
                new BasicEntity(1L, "First", 100),
                new BasicEntity(2L, "Second", 200)
            ])

        expect:
            repository.countAll() == 2

        when:
            repository.insert(new BasicEntity(3L, "Third", 300))

        then:
            repository.countAll() == 3

        when:
            repository.deleteById(1L)

        then:
            repository.countAll() == 2

        where:
            session << allSessions
    }

    // ============ Combined operations tests ============

    def "should correctly handle combined bulk operations"() {
        given:
            BasicRepository repository = new BasicRepository(session)

        when: "insert multiple entities"
            repository.insertAll([
                new BasicEntity(1L, "Alpha", 10),
                new BasicEntity(2L, "Beta", 20),
                new BasicEntity(3L, "Gamma", 30),
                new BasicEntity(4L, "Delta", 40),
                new BasicEntity(5L, "Epsilon", 50)
            ])

        then:
            repository.countAll() == 5

        when: "delete some by IDs"
            repository.deleteAllById([2L, 4L])

        then:
            repository.countAll() == 3
            with(repository.findAllById([1L, 3L, 5L])) { results ->
                results.size() == 3
                results.collect { it.name } as Set == ["Alpha", "Gamma", "Epsilon"] as Set
            }

        when: "find remaining entities and delete them"
            List<BasicEntity> remaining = repository.findAll()
            repository.deleteAll(remaining)

        then:
            repository.countAll() == 0
            repository.findAll().isEmpty()

        where:
            session << allSessions
    }

    // ============ existsById tests (bonus) ============

    def "should return true when entity exists using existsById"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            repository.insert(new BasicEntity(1L, "Existing", 100))

        expect:
            repository.existsById(1L)

        where:
            session << allSessions
    }

    def "should return false when entity does not exist using existsById"() {
        given:
            BasicRepository repository = new BasicRepository(session)

        expect:
            !repository.existsById(999L)

        where:
            session << allSessions
    }
}
