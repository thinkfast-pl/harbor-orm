// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.*

/**
 * Integration tests for @ElementCollection functionality.
 *
 * Tests cover:
 * - Insert entity with @ElementCollection of simple types (String)
 * - Insert entity with @ElementCollection of @Embeddable elements
 * - Select entity loads element collection properly
 * - Update entity with element collection (add/remove elements)
 * - Empty element collection handling
 * - Delete entity with element collection (cascading delete)
 * - Batch operations on entities with element collections
 *
 * Note: EntityRoleIT already covers @ElementCollection with @Enumerated types.
 * This test focuses on simple types and embeddable element collections.
 */
class ElementCollectionIT extends AbstractHarborIT {

    // ==================== Simple Type Element Collection (String) ====================

    def "should insert entity with simple type element collection"() {
        given:
            QArticleEntity qArticle = new QArticleEntity(null)
            ArticleEntity article = new ArticleEntity(
                    1L,
                    "Introduction to HarborORM",
                    List.of("java", "orm", "database")
            )

        when:
            session.insertEntity(qArticle, article)

        then:
            session.selectEntity(qArticle).count() == 1

        where:
            session << allSessions
    }

    def "should select entity with simple type element collection populated"() {
        given:
            QArticleEntity qArticle = new QArticleEntity(null)
            ArticleEntity article = new ArticleEntity(
                    1L,
                    "Introduction to HarborORM",
                    List.of("java", "orm", "database")
            )
            session.insertEntity(qArticle, article)

        when:
            ArticleEntity loaded = session.selectEntity(qArticle).whereIdEq(1L).fetchSingle()

        then:
            with(loaded) { a ->
                a.id == 1L
                a.title == "Introduction to HarborORM"
                a.tags != null
                a.tags.size() == 3
                a.tags.containsAll(["java", "orm", "database"])
            }

        where:
            session << allSessions
    }

    def "should update entity by adding elements to simple type collection"() {
        given:
            QArticleEntity qArticle = new QArticleEntity(null)
            ArticleEntity article = new ArticleEntity(
                    1L,
                    "Introduction to HarborORM",
                    List.of("java", "orm")
            )
            session.insertEntity(qArticle, article)

        when:
            ArticleEntity loaded = session.selectEntity(qArticle).whereIdEq(1L).fetchSingle()
            loaded.setTags(List.of("java", "orm", "database", "tutorial"))
            session.updateEntity(qArticle, loaded)

        then:
            with(session.selectEntity(qArticle).whereIdEq(1L).fetchSingle()) { a ->
                a.tags.size() == 4
                a.tags.containsAll(["java", "orm", "database", "tutorial"])
            }

        where:
            session << allSessions
    }

    def "should update entity by removing elements from simple type collection"() {
        given:
            QArticleEntity qArticle = new QArticleEntity(null)
            ArticleEntity article = new ArticleEntity(
                    1L,
                    "Introduction to HarborORM",
                    List.of("java", "orm", "database", "tutorial")
            )
            session.insertEntity(qArticle, article)

        when:
            ArticleEntity loaded = session.selectEntity(qArticle).whereIdEq(1L).fetchSingle()
            loaded.setTags(List.of("java", "orm"))
            session.updateEntity(qArticle, loaded)

        then:
            with(session.selectEntity(qArticle).whereIdEq(1L).fetchSingle()) { a ->
                a.tags.size() == 2
                a.tags.containsAll(["java", "orm"])
            }

        where:
            session << allSessions
    }

    def "should handle empty simple type element collection on insert"() {
        given:
            QArticleEntity qArticle = new QArticleEntity(null)
            ArticleEntity article = new ArticleEntity(
                    1L,
                    "Article with no tags",
                    List.of()
            )

        when:
            session.insertEntity(qArticle, article)

        then:
            with(session.selectEntity(qArticle).whereIdEq(1L).fetchSingle()) { a ->
                a.id == 1L
                a.title == "Article with no tags"
                a.tags != null
                a.tags.isEmpty()
            }

        where:
            session << allSessions
    }

    def "should update entity by clearing all elements from simple type collection"() {
        given:
            QArticleEntity qArticle = new QArticleEntity(null)
            ArticleEntity article = new ArticleEntity(
                    1L,
                    "Introduction to HarborORM",
                    List.of("java", "orm", "database")
            )
            session.insertEntity(qArticle, article)

        when:
            ArticleEntity loaded = session.selectEntity(qArticle).whereIdEq(1L).fetchSingle()
            loaded.setTags(List.of())
            session.updateEntity(qArticle, loaded)

        then:
            with(session.selectEntity(qArticle).whereIdEq(1L).fetchSingle()) { a ->
                a.tags != null
                a.tags.isEmpty()
            }

        where:
            session << allSessions
    }

    def "should delete entity with simple type element collection"() {
        given:
            QArticleEntity qArticle = new QArticleEntity(null)
            ArticleEntity article = new ArticleEntity(
                    1L,
                    "Introduction to HarborORM",
                    List.of("java", "orm", "database")
            )
            session.insertEntity(qArticle, article)

        when:
            ArticleEntity loaded = session.selectEntity(qArticle).whereIdEq(1L).fetchSingle()
            session.deleteEntity(qArticle, loaded)

        then:
            session.selectEntity(qArticle).count() == 0

        where:
            session << allSessions
    }

    def "should select multiple entities with simple type element collections"() {
        given:
            QArticleEntity qArticle = new QArticleEntity(null)
            session.insertEntity(qArticle, new ArticleEntity(1L, "Article One", List.of("java", "spring")))
            session.insertEntity(qArticle, new ArticleEntity(2L, "Article Two", List.of("python", "django")))
            session.insertEntity(qArticle, new ArticleEntity(3L, "Article Three", List.of()))

        when:
            List<ArticleEntity> articles = session.selectEntity(qArticle)
                    .orderBy(qArticle.id.asc())
                    .fetchAll()

        then:
            articles.size() == 3

            with(articles[0]) { a ->
                a.id == 1L
                a.title == "Article One"
                a.tags.size() == 2
                a.tags.containsAll(["java", "spring"])
            }

            with(articles[1]) { a ->
                a.id == 2L
                a.title == "Article Two"
                a.tags.size() == 2
                a.tags.containsAll(["python", "django"])
            }

            with(articles[2]) { a ->
                a.id == 3L
                a.title == "Article Three"
                a.tags.isEmpty()
            }

        where:
            session << allSessions
    }

    // ==================== Embeddable Element Collection ====================

    def "should insert entity with embeddable element collection"() {
        given:
            QContactEntity qContact = new QContactEntity(null)
            ContactEntity contact = new ContactEntity(
                    1L,
                    "John Doe",
                    List.of(
                            new PhoneNumber("mobile", "+1-555-0100"),
                            new PhoneNumber("work", "+1-555-0200")
                    )
            )

        when:
            session.insertEntity(qContact, contact)

        then:
            session.selectEntity(qContact).count() == 1

        where:
            session << allSessions
    }

    def "should select entity with embeddable element collection populated"() {
        given:
            QContactEntity qContact = new QContactEntity(null)
            ContactEntity contact = new ContactEntity(
                    1L,
                    "John Doe",
                    List.of(
                            new PhoneNumber("mobile", "+1-555-0100"),
                            new PhoneNumber("work", "+1-555-0200")
                    )
            )
            session.insertEntity(qContact, contact)

        when:
            ContactEntity loaded = session.selectEntity(qContact).whereIdEq(1L).fetchSingle()

        then:
            with(loaded) { c ->
                c.id == 1L
                c.name == "John Doe"
                c.phoneNumbers != null
                c.phoneNumbers.size() == 2
            }

            // Verify phone numbers (order may vary, so check contents)
            loaded.phoneNumbers.any { it.type == "mobile" && it.number == "+1-555-0100" }
            loaded.phoneNumbers.any { it.type == "work" && it.number == "+1-555-0200" }

        where:
            session << allSessions
    }

    def "should update entity by adding elements to embeddable collection"() {
        given:
            QContactEntity qContact = new QContactEntity(null)
            ContactEntity contact = new ContactEntity(
                    1L,
                    "John Doe",
                    List.of(new PhoneNumber("mobile", "+1-555-0100"))
            )
            session.insertEntity(qContact, contact)

        when:
            ContactEntity loaded = session.selectEntity(qContact).whereIdEq(1L).fetchSingle()
            loaded.setPhoneNumbers(List.of(
                    new PhoneNumber("mobile", "+1-555-0100"),
                    new PhoneNumber("work", "+1-555-0200"),
                    new PhoneNumber("home", "+1-555-0300")
            ))
            session.updateEntity(qContact, loaded)

        then:
            with(session.selectEntity(qContact).whereIdEq(1L).fetchSingle()) { c ->
                c.phoneNumbers.size() == 3
                c.phoneNumbers.any { it.type == "mobile" && it.number == "+1-555-0100" }
                c.phoneNumbers.any { it.type == "work" && it.number == "+1-555-0200" }
                c.phoneNumbers.any { it.type == "home" && it.number == "+1-555-0300" }
            }

        where:
            session << allSessions
    }

    def "should update entity by removing elements from embeddable collection"() {
        given:
            QContactEntity qContact = new QContactEntity(null)
            ContactEntity contact = new ContactEntity(
                    1L,
                    "John Doe",
                    List.of(
                            new PhoneNumber("mobile", "+1-555-0100"),
                            new PhoneNumber("work", "+1-555-0200"),
                            new PhoneNumber("home", "+1-555-0300")
                    )
            )
            session.insertEntity(qContact, contact)

        when:
            ContactEntity loaded = session.selectEntity(qContact).whereIdEq(1L).fetchSingle()
            loaded.setPhoneNumbers(List.of(new PhoneNumber("mobile", "+1-555-0100")))
            session.updateEntity(qContact, loaded)

        then:
            with(session.selectEntity(qContact).whereIdEq(1L).fetchSingle()) { c ->
                c.phoneNumbers.size() == 1
                c.phoneNumbers[0].type == "mobile"
                c.phoneNumbers[0].number == "+1-555-0100"
            }

        where:
            session << allSessions
    }

    def "should handle empty embeddable element collection on insert"() {
        given:
            QContactEntity qContact = new QContactEntity(null)
            ContactEntity contact = new ContactEntity(
                    1L,
                    "Jane Doe",
                    List.of()
            )

        when:
            session.insertEntity(qContact, contact)

        then:
            with(session.selectEntity(qContact).whereIdEq(1L).fetchSingle()) { c ->
                c.id == 1L
                c.name == "Jane Doe"
                c.phoneNumbers != null
                c.phoneNumbers.isEmpty()
            }

        where:
            session << allSessions
    }

    def "should update entity by clearing all elements from embeddable collection"() {
        given:
            QContactEntity qContact = new QContactEntity(null)
            ContactEntity contact = new ContactEntity(
                    1L,
                    "John Doe",
                    List.of(
                            new PhoneNumber("mobile", "+1-555-0100"),
                            new PhoneNumber("work", "+1-555-0200")
                    )
            )
            session.insertEntity(qContact, contact)

        when:
            ContactEntity loaded = session.selectEntity(qContact).whereIdEq(1L).fetchSingle()
            loaded.setPhoneNumbers(List.of())
            session.updateEntity(qContact, loaded)

        then:
            with(session.selectEntity(qContact).whereIdEq(1L).fetchSingle()) { c ->
                c.phoneNumbers != null
                c.phoneNumbers.isEmpty()
            }

        where:
            session << allSessions
    }

    def "should delete entity with embeddable element collection"() {
        given:
            QContactEntity qContact = new QContactEntity(null)
            ContactEntity contact = new ContactEntity(
                    1L,
                    "John Doe",
                    List.of(
                            new PhoneNumber("mobile", "+1-555-0100"),
                            new PhoneNumber("work", "+1-555-0200")
                    )
            )
            session.insertEntity(qContact, contact)

        when:
            ContactEntity loaded = session.selectEntity(qContact).whereIdEq(1L).fetchSingle()
            session.deleteEntity(qContact, loaded)

        then:
            session.selectEntity(qContact).count() == 0

        where:
            session << allSessions
    }

    def "should select multiple entities with embeddable element collections"() {
        given:
            QContactEntity qContact = new QContactEntity(null)
            session.insertEntity(qContact, new ContactEntity(1L, "John Doe",
                    List.of(new PhoneNumber("mobile", "+1-555-0100"))))
            session.insertEntity(qContact, new ContactEntity(2L, "Jane Doe",
                    List.of(new PhoneNumber("work", "+1-555-0200"), new PhoneNumber("home", "+1-555-0300"))))
            session.insertEntity(qContact, new ContactEntity(3L, "Bob Smith", List.of()))

        when:
            List<ContactEntity> contacts = session.selectEntity(qContact)
                    .orderBy(qContact.id.asc())
                    .fetchAll()

        then:
            contacts.size() == 3

            with(contacts[0]) { c ->
                c.id == 1L
                c.name == "John Doe"
                c.phoneNumbers.size() == 1
            }

            with(contacts[1]) { c ->
                c.id == 2L
                c.name == "Jane Doe"
                c.phoneNumbers.size() == 2
            }

            with(contacts[2]) { c ->
                c.id == 3L
                c.name == "Bob Smith"
                c.phoneNumbers.isEmpty()
            }

        where:
            session << allSessions
    }

    // ==================== Batch Operations ====================

    def "should delete all entities with element collections"() {
        given:
            QArticleEntity qArticle = new QArticleEntity(null)
            ArticleEntity article1 = new ArticleEntity(1L, "Article One", List.of("java", "spring"))
            ArticleEntity article2 = new ArticleEntity(2L, "Article Two", List.of("python", "django"))
            ArticleEntity article3 = new ArticleEntity(3L, "Article Three", List.of("go", "gin"))
            session.insertEntity(qArticle, article1)
            session.insertEntity(qArticle, article2)
            session.insertEntity(qArticle, article3)

        when:
            List<ArticleEntity> articles = session.selectEntity(qArticle).fetchAll()
            session.deleteEntityAll(qArticle, [articles[0], articles[1]])

        then:
            session.selectEntity(qArticle).count() == 1

        where:
            session << allSessions
    }

    def "should delete entity by id with element collection"() {
        given:
            QArticleEntity qArticle = new QArticleEntity(null)
            session.insertEntity(qArticle, new ArticleEntity(1L, "Article One", List.of("java", "spring")))
            session.insertEntity(qArticle, new ArticleEntity(2L, "Article Two", List.of("python", "django")))

        when:
            session.deleteEntityById(qArticle, 1L)

        then:
            session.selectEntity(qArticle).count() == 1
            session.selectEntity(qArticle).whereIdEq(2L).fetchOne().isPresent()

        where:
            session << allSessions
    }

    def "should delete entities by ids with element collections"() {
        given:
            QArticleEntity qArticle = new QArticleEntity(null)
            session.insertEntity(qArticle, new ArticleEntity(1L, "Article One", List.of("java", "spring")))
            session.insertEntity(qArticle, new ArticleEntity(2L, "Article Two", List.of("python", "django")))
            session.insertEntity(qArticle, new ArticleEntity(3L, "Article Three", List.of("go", "gin")))

        when:
            session.deleteEntityByIds(qArticle, [1L, 2L])

        then:
            session.selectEntity(qArticle).count() == 1
            session.selectEntity(qArticle).whereIdEq(3L).fetchOne().isPresent()

        where:
            session << allSessions
    }

    // ==================== Edge Cases ====================

    def "should handle update without changing element collection"() {
        given:
            QArticleEntity qArticle = new QArticleEntity(null)
            ArticleEntity article = new ArticleEntity(
                    1L,
                    "Introduction to HarborORM",
                    List.of("java", "orm", "database")
            )
            session.insertEntity(qArticle, article)

        when:
            ArticleEntity loaded = session.selectEntity(qArticle).whereIdEq(1L).fetchSingle()
            loaded.setTitle("Updated Title")
            // Not changing tags
            session.updateEntity(qArticle, loaded)

        then:
            with(session.selectEntity(qArticle).whereIdEq(1L).fetchSingle()) { a ->
                a.title == "Updated Title"
                a.tags.size() == 3
                a.tags.containsAll(["java", "orm", "database"])
            }

        where:
            session << allSessions
    }

    def "should work with entity alias for simple type collection"() {
        given:
            QArticleEntity qArticle = new QArticleEntity("art")
            ArticleEntity article = new ArticleEntity(
                    1L,
                    "Introduction to HarborORM",
                    List.of("java", "orm", "database")
            )

        when:
            session.insertEntity(qArticle, article)
            ArticleEntity loaded = session.selectEntity(qArticle).whereIdEq(1L).fetchSingle()

        then:
            with(loaded) { a ->
                a.id == 1L
                a.title == "Introduction to HarborORM"
                a.tags.size() == 3
            }

        where:
            session << allSessions
    }

    def "should work with entity alias for embeddable collection"() {
        given:
            QContactEntity qContact = new QContactEntity("con")
            ContactEntity contact = new ContactEntity(
                    1L,
                    "John Doe",
                    List.of(
                            new PhoneNumber("mobile", "+1-555-0100"),
                            new PhoneNumber("work", "+1-555-0200")
                    )
            )

        when:
            session.insertEntity(qContact, contact)
            ContactEntity loaded = session.selectEntity(qContact).whereIdEq(1L).fetchSingle()

        then:
            with(loaded) { c ->
                c.id == 1L
                c.name == "John Doe"
                c.phoneNumbers.size() == 2
            }

        where:
            session << allSessions
    }

    def "should replace entire element collection on update"() {
        given:
            QArticleEntity qArticle = new QArticleEntity(null)
            ArticleEntity article = new ArticleEntity(
                    1L,
                    "Introduction to HarborORM",
                    List.of("java", "orm", "database")
            )
            session.insertEntity(qArticle, article)

        when:
            ArticleEntity loaded = session.selectEntity(qArticle).whereIdEq(1L).fetchSingle()
            // Replace with completely different tags
            loaded.setTags(List.of("python", "sqlalchemy", "postgresql"))
            session.updateEntity(qArticle, loaded)

        then:
            with(session.selectEntity(qArticle).whereIdEq(1L).fetchSingle()) { a ->
                a.tags.size() == 3
                a.tags.containsAll(["python", "sqlalchemy", "postgresql"])
                !a.tags.contains("java")
                !a.tags.contains("orm")
                !a.tags.contains("database")
            }

        where:
            session << allSessions
    }
}
