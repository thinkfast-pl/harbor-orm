// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.repository.EntityNotFoundException
import io.github.thinkfastpl.harbororm.test.domain.BasicEntity
import io.github.thinkfastpl.harbororm.test.domain.repository.BasicRepository

class RepositoryExceptionIT extends AbstractHarborIT {

    def "findByIdOrThrow returns entity when exists"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            BasicEntity entity = new BasicEntity(1L, "Test Entity", 42)
            repository.insert(entity)

        when:
            BasicEntity result = repository.findByIdOrThrow(1L)

        then:
            result != null
            result.id == 1L
            result.name == "Test Entity"
            result.numero == 42

        where:
            session << allSessions
    }

    def "findByIdOrThrow throws EntityNotFoundException when not found"() {
        given:
            BasicRepository repository = new BasicRepository(session)

        when:
            repository.findByIdOrThrow(999L)

        then:
            EntityNotFoundException ex = thrown(EntityNotFoundException)
            ex.message == "Entity with ID = 999 not found"

        where:
            session << allSessions
    }

    def "findByIdForUpdateOrThrow returns entity with lock when exists"() {
        given:
            BasicRepository repository = new BasicRepository(session)
            BasicEntity entity = new BasicEntity(2L, "Locked Entity", 100)
            repository.insert(entity)

        when:
            BasicEntity result = repository.findByIdForUpdateOrThrow(2L)

        then:
            result != null
            result.id == 2L
            result.name == "Locked Entity"
            result.numero == 100

        where:
            session << allSessions
    }

    def "findByIdForUpdateOrThrow throws EntityNotFoundException when not found"() {
        given:
            BasicRepository repository = new BasicRepository(session)

        when:
            repository.findByIdForUpdateOrThrow(888L)

        then:
            EntityNotFoundException ex = thrown(EntityNotFoundException)
            ex.message == "Entity with ID = 888 not found"

        where:
            session << allSessions
    }
}
