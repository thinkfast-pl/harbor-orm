// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.QUserEntity
import io.github.thinkfastpl.harbororm.test.domain.UserEntity

class EntityUserIT extends AbstractHarborIT {

    def "read"() {
        given:
            QUserEntity qUserEntity = new QUserEntity(null)
            session.insertEntity(qUserEntity, new UserEntity(UUID.randomUUID(), "test1@example.com", "pwd1", "First name1", "Last name1", "555444331"))
            session.insertEntity(qUserEntity, new UserEntity(UUID.randomUUID(), "test2@example.com", "pwd2", "First name2", "Last name2", "555444332"))
            session.insertEntity(qUserEntity, new UserEntity(UUID.randomUUID(), "test3@example.com", "pwd3", "First name3", "Last name3", "555444333"))
            List<UserEntity> users

        expect:
            session.selectEntity(qUserEntity).count() == 3

        when:
            users = session.selectEntity(qUserEntity)
                    .where(qUserEntity.email.eq("test1@example.com"))
                    .fetchAll()

        then:
            users.size() == 1
            with(users[0]) { user ->
                user.id != null
                user.email == "test1@example.com"
                user.password == "pwd1"
                user.firstName == "First name1"
                user.lastName == "Last name1"
                user.phoneNumber == "555444331"
            }

        when:
            users = session.selectEntity(qUserEntity)
                    .orderBy(qUserEntity.password.desc())
                    .limit(2)
                    .fetchAll()

        then:
            users.size() == 2
            users[0].password == "pwd3"
            users[1].password == "pwd2"

        where:
            session << allSessions
    }

    def "update"() {
        given:
            QUserEntity qUserEntity = new QUserEntity(null)

            UUID id = UUID.randomUUID()
            session.insertEntity(qUserEntity, new UserEntity(id, "test@example.com", "pwd", "First name", "Last name", "555444333"))
            session.insertEntity(qUserEntity, new UserEntity(UUID.randomUUID(), "test2@example.com", "pwd2", "First name2", "Last name2", "555444332"))
            UserEntity entity

        when:
            entity = session.selectEntity(qUserEntity).where(qUserEntity.id.eq(id)).forUpdate().fetchSingle()
            entity.setEmail("test-up@example.com")
            entity.setPassword("pwd-up")
            entity.setFirstName("First name up")
            entity.setLastName("Last name up")
            entity.setPhoneNumber("555444777")

            session.updateEntity(qUserEntity, entity)

        then:
            with(session.selectEntity(qUserEntity).where(qUserEntity.id.eq(id)).fetchSingle()) { user ->
                user.id == id
                user.email == "test-up@example.com"
                user.password == "pwd-up"
                user.firstName == "First name up"
                user.lastName == "Last name up"
                user.phoneNumber == "555444777"
            }
            with(session.selectEntity(qUserEntity).where(qUserEntity.id.notEq(id)).fetchSingle()) { user ->
                user.id != id
                user.email == "test2@example.com"
                user.password == "pwd2"
                user.firstName == "First name2"
                user.lastName == "Last name2"
                user.phoneNumber == "555444332"
            }

        where:
            session << allSessions
    }

    def "delete"() {
        given:
            QUserEntity qUserEntity = new QUserEntity(null)

            UUID id = UUID.randomUUID()
            session.insertEntity(qUserEntity, new UserEntity(id, "test@example.com", "pwd", "First name", "Last name", "555444333"))
            session.insertEntity(qUserEntity, new UserEntity(UUID.randomUUID(), "test2@example.com", "pwd2", "First name2", "Last name2", "555444332"))
            UserEntity entity

        expect:
            session.selectEntity(qUserEntity).count() == 2

        when:
            entity = session.selectEntity(qUserEntity).where(qUserEntity.id.eq(id)).forUpdate().fetchSingle()
            session.deleteEntity(qUserEntity, entity)

        then:
            session.selectEntity(qUserEntity).count() == 1

        where:
            session << allSessions
    }

    def "delete all"() {
        given:
            QUserEntity qUserEntity = new QUserEntity(null)

            UUID id = UUID.randomUUID()
            UUID id2 = UUID.randomUUID()
            session.insertEntity(qUserEntity, new UserEntity(id, "test@example.com", "pwd", "First name", "Last name", "555444333"))
            session.insertEntity(qUserEntity, new UserEntity(id2, "test2@example.com", "pwd2", "First name2", "Last name2", "555444332"))
            session.insertEntity(qUserEntity, new UserEntity(UUID.randomUUID(), "test3@example.com", "pwd3", "First name3", "Last name3", "555444333"))
            List<UserEntity> entities

        expect:
            session.selectEntity(qUserEntity).count() == 3

        when:
            entities = session.selectEntity(qUserEntity).where(qUserEntity.id.in([id, id2])).fetchAll()
            session.deleteEntityAll(qUserEntity, entities)

        then:
            session.selectEntity(qUserEntity).count() == 1
            session.selectEntity(qUserEntity).where(qUserEntity.id.in([id, id2])).count() == 0

        where:
            session << allSessions
    }

    def "delete by id"() {
        given:
            QUserEntity qUserEntity = new QUserEntity(null)

            UUID id = UUID.randomUUID()
            session.insertEntity(qUserEntity, new UserEntity(id, "test@example.com", "pwd", "First name", "Last name", "555444333"))
            session.insertEntity(qUserEntity, new UserEntity(UUID.randomUUID(), "test2@example.com", "pwd2", "First name2", "Last name2", "555444332"))

        expect:
            session.selectEntity(qUserEntity).count() == 2

        when:
            session.deleteEntityById(qUserEntity, id)

        then:
            session.selectEntity(qUserEntity).count() == 1
            session.selectEntity(qUserEntity).where(qUserEntity.id.eq(id)).count() == 0

        where:
            session << allSessions
    }

    def "delete by ids"() {
        given:
            QUserEntity qUserEntity = new QUserEntity(null)

            UUID id = UUID.randomUUID()
            UUID id2 = UUID.randomUUID()
            session.insertEntity(qUserEntity, new UserEntity(id, "test@example.com", "pwd", "First name", "Last name", "555444333"))
            session.insertEntity(qUserEntity, new UserEntity(id2, "test2@example.com", "pwd2", "First name2", "Last name2", "555444332"))
            session.insertEntity(qUserEntity, new UserEntity(UUID.randomUUID(), "test3@example.com", "pwd3", "First name3", "Last name3", "555444333"))

        expect:
            session.selectEntity(qUserEntity).count() == 3

        when:
            session.deleteEntityByIds(qUserEntity, [id, id2])

        then:
            session.selectEntity(qUserEntity).count() == 1
            session.selectEntity(qUserEntity).where(qUserEntity.id.in([id, id2])).count() == 0

        where:
            session << allSessions
    }
}
