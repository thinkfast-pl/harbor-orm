// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.AuthorEntity
import io.github.thinkfastpl.harbororm.test.domain.PublicationEntity
import io.github.thinkfastpl.harbororm.test.domain.QAuthorEntity
import io.github.thinkfastpl.harbororm.test.domain.QPublicationEntity

/**
 * Integration tests for @OneToMany relationship functionality.
 *
 * Tests cover:
 * - Select parent entity loads children via lazy loading
 * - Insert child entity directly and verify parent-child relationship
 * - Update child entity
 * - Delete child entity directly
 * - Select parent with empty children list
 * - Select multiple parents with their children (batch loading)
 * - Lazy loading behavior verification
 * - Entity alias support
 *
 * Note: @OneToMany in HarborORM is read-only from the parent side.
 * Child entities must be inserted/updated/deleted directly using their own QEntity.
 * The relationship is loaded lazily when accessing the collection.
 */
class OneToManyRelationIT extends AbstractHarborIT {

    // ==================== Basic Select with Children ====================

    def "should select parent entity with children populated via lazy loading"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity(null)
            QPublicationEntity qPublication = new QPublicationEntity(null)

            // Insert parent
            session.insertEntity(qAuthor, new AuthorEntity(1L, "Robert C. Martin", "uncle.bob@cleancode.com", []))

            // Insert children directly with author_id set
            session.insertEntity(qPublication, new PublicationEntity(1L, 1L, "Clean Code", (Integer) 2008))
            session.insertEntity(qPublication, new PublicationEntity(2L, 1L, "Clean Architecture", (Integer) 2017))
            session.insertEntity(qPublication, new PublicationEntity(3L, 1L, "Clean Agile", (Integer) 2019))

        when:
            AuthorEntity author = session.selectEntity(qAuthor).whereIdEq(1L).fetchSingle()

        then:
            with(author) { a ->
                a.id == 1L
                a.name == "Robert C. Martin"
                a.email == "uncle.bob@cleancode.com"
                a.publications != null
                a.publications.size() == 3
            }

            // Verify children are loaded correctly
            author.publications.any { it.id == 1L && it.title == "Clean Code" && it.publicationYear == 2008 }
            author.publications.any { it.id == 2L && it.title == "Clean Architecture" && it.publicationYear == 2017 }
            author.publications.any { it.id == 3L && it.title == "Clean Agile" && it.publicationYear == 2019 }

        where:
            session << allSessions
    }

    def "should select parent with no children returns empty list"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity(null)

            // Insert parent without children
            session.insertEntity(qAuthor, new AuthorEntity(1L, "New Author", "new@author.com", []))

        when:
            AuthorEntity author = session.selectEntity(qAuthor).whereIdEq(1L).fetchSingle()

        then:
            with(author) { a ->
                a.id == 1L
                a.name == "New Author"
                a.publications != null
                a.publications.isEmpty()
            }

        where:
            session << allSessions
    }

    // ==================== Multiple Parents with Children ====================

    def "should select multiple parents with their respective children"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity(null)
            QPublicationEntity qPublication = new QPublicationEntity(null)

            // Insert parents
            session.insertEntity(qAuthor, new AuthorEntity(1L, "Martin Fowler", "fowler@refactoring.com", []))
            session.insertEntity(qAuthor, new AuthorEntity(2L, "Kent Beck", "kent@tdd.com", []))
            session.insertEntity(qAuthor, new AuthorEntity(3L, "Eric Evans", "eric@ddd.com", []))

            // Insert children with author_id set
            session.insertEntity(qPublication, new PublicationEntity(1L, 1L, "Refactoring", (Integer) 1999))
            session.insertEntity(qPublication, new PublicationEntity(2L, 1L, "Patterns of Enterprise Application Architecture", (Integer) 2002))
            session.insertEntity(qPublication, new PublicationEntity(3L, 2L, "Test Driven Development", (Integer) 2002))
            session.insertEntity(qPublication, new PublicationEntity(4L, 3L, "Domain-Driven Design", (Integer) 2003))

        when:
            List<AuthorEntity> authors = session.selectEntity(qAuthor)
                    .orderBy(qAuthor.id.asc())
                    .fetchAll()

        then:
            authors.size() == 3

            with(authors[0]) { a ->
                a.id == 1L
                a.name == "Martin Fowler"
                a.publications.size() == 2
                a.publications.any { it.title == "Refactoring" }
                a.publications.any { it.title == "Patterns of Enterprise Application Architecture" }
            }

            with(authors[1]) { a ->
                a.id == 2L
                a.name == "Kent Beck"
                a.publications.size() == 1
                a.publications[0].title == "Test Driven Development"
            }

            with(authors[2]) { a ->
                a.id == 3L
                a.name == "Eric Evans"
                a.publications.size() == 1
                a.publications[0].title == "Domain-Driven Design"
            }

        where:
            session << allSessions
    }

    // ==================== Child Entity Operations ====================

    def "should insert child entity and verify it appears in parent collection"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity(null)
            QPublicationEntity qPublication = new QPublicationEntity(null)

            // Insert parent
            session.insertEntity(qAuthor, new AuthorEntity(1L, "Joshua Bloch", "josh@java.com", []))

            // Insert first child
            session.insertEntity(qPublication, new PublicationEntity(1L, 1L, "Effective Java", (Integer) 2001))

        when:
            // Add another child
            session.insertEntity(qPublication, new PublicationEntity(2L, 1L, "Effective Java 2nd Edition", (Integer) 2008))

        then:
            AuthorEntity author = session.selectEntity(qAuthor).whereIdEq(1L).fetchSingle()
            author.publications.size() == 2
            author.publications.any { it.title == "Effective Java" }
            author.publications.any { it.title == "Effective Java 2nd Edition" }

        where:
            session << allSessions
    }

    def "should update child entity"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity(null)
            QPublicationEntity qPublication = new QPublicationEntity(null)

            // Insert parent and child
            session.insertEntity(qAuthor, new AuthorEntity(1L, "Joshua Bloch", "josh@java.com", []))
            session.insertEntity(qPublication, new PublicationEntity(1L, 1L, "Effective Java", (Integer) 2001))

        when:
            // Update child entity
            PublicationEntity publication = session.selectEntity(qPublication).whereIdEq(1L).fetchSingle()
            publication.setTitle("Effective Java 3rd Edition")
            publication.setPublicationYear((Integer) 2018)
            session.updateEntity(qPublication, publication)

        then:
            AuthorEntity author = session.selectEntity(qAuthor).whereIdEq(1L).fetchSingle()
            author.publications.size() == 1
            with(author.publications[0]) { p ->
                p.title == "Effective Java 3rd Edition"
                p.publicationYear == 2018
            }

        where:
            session << allSessions
    }

    def "should delete child entity directly"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity(null)
            QPublicationEntity qPublication = new QPublicationEntity(null)

            // Insert parent and children
            session.insertEntity(qAuthor, new AuthorEntity(1L, "Gang of Four Author", "gof@patterns.com", []))
            session.insertEntity(qPublication, new PublicationEntity(1L, 1L, "Design Patterns", (Integer) 1994))
            session.insertEntity(qPublication, new PublicationEntity(2L, 1L, "Another Book", (Integer) 1995))

        when:
            // Delete one child
            PublicationEntity toDelete = session.selectEntity(qPublication).whereIdEq(2L).fetchSingle()
            session.deleteEntity(qPublication, toDelete)

        then:
            AuthorEntity author = session.selectEntity(qAuthor).whereIdEq(1L).fetchSingle()
            author.publications.size() == 1
            author.publications[0].title == "Design Patterns"

        where:
            session << allSessions
    }

    // ==================== Parent Entity Operations ====================

    def "should update parent entity without affecting children"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity(null)
            QPublicationEntity qPublication = new QPublicationEntity(null)

            // Insert parent and children
            session.insertEntity(qAuthor, new AuthorEntity(1L, "Original Name", "original@email.com", []))
            session.insertEntity(qPublication, new PublicationEntity(1L, 1L, "A Book", (Integer) 2000))

        when:
            AuthorEntity author = session.selectEntity(qAuthor).whereIdEq(1L).fetchSingle()
            author.setName("Updated Name")
            author.setEmail("updated@email.com")
            session.updateEntity(qAuthor, author)

        then:
            AuthorEntity updatedAuthor = session.selectEntity(qAuthor).whereIdEq(1L).fetchSingle()
            updatedAuthor.name == "Updated Name"
            updatedAuthor.email == "updated@email.com"
            updatedAuthor.publications.size() == 1
            updatedAuthor.publications[0].title == "A Book"

        where:
            session << allSessions
    }

    def "should delete parent entity (children must be deleted first due to FK constraint)"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity(null)
            QPublicationEntity qPublication = new QPublicationEntity(null)

            // Insert parent and children
            session.insertEntity(qAuthor, new AuthorEntity(1L, "Author to Delete", "delete@me.com", []))
            session.insertEntity(qPublication, new PublicationEntity(1L, 1L, "Book to Delete", (Integer) 2000))

        when:
            // First delete children
            session.deleteEntityById(qPublication, 1L)
            // Then delete parent
            session.deleteEntityById(qAuthor, 1L)

        then:
            session.selectEntity(qAuthor).count() == 0
            session.selectEntity(qPublication).count() == 0

        where:
            session << allSessions
    }

    // ==================== Entity Alias Support ====================

    def "should work with entity alias"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity("auth")
            QPublicationEntity qPublication = new QPublicationEntity("pub")

            // Insert parent and children
            session.insertEntity(qAuthor, new AuthorEntity(1L, "Aliased Author", "alias@test.com", []))
            session.insertEntity(qPublication, new PublicationEntity(1L, 1L, "Aliased Book", (Integer) 2020))

        when:
            AuthorEntity author = session.selectEntity(qAuthor).whereIdEq(1L).fetchSingle()

        then:
            author.id == 1L
            author.name == "Aliased Author"
            author.publications.size() == 1
            author.publications[0].title == "Aliased Book"

        where:
            session << allSessions
    }

    // ==================== Edge Cases ====================

    def "should handle parent with single child"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity(null)
            QPublicationEntity qPublication = new QPublicationEntity(null)

            session.insertEntity(qAuthor, new AuthorEntity(1L, "Single Book Author", "single@book.com", []))
            session.insertEntity(qPublication, new PublicationEntity(1L, 1L, "The Only Book", (Integer) 2021))

        when:
            AuthorEntity author = session.selectEntity(qAuthor).whereIdEq(1L).fetchSingle()

        then:
            author.publications.size() == 1
            author.publications[0].title == "The Only Book"

        where:
            session << allSessions
    }

    def "should handle multiple parents some with children some without"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity(null)
            QPublicationEntity qPublication = new QPublicationEntity(null)

            // Insert parents
            session.insertEntity(qAuthor, new AuthorEntity(1L, "Prolific Author", "prolific@author.com", []))
            session.insertEntity(qAuthor, new AuthorEntity(2L, "Lazy Author", "lazy@author.com", []))
            session.insertEntity(qAuthor, new AuthorEntity(3L, "Another Prolific", "another@author.com", []))

            // Insert children only for first and third authors
            session.insertEntity(qPublication, new PublicationEntity(1L, 1L, "Book 1", (Integer) 2020))
            session.insertEntity(qPublication, new PublicationEntity(2L, 1L, "Book 2", (Integer) 2021))
            session.insertEntity(qPublication, new PublicationEntity(3L, 3L, "Book 3", (Integer) 2022))

        when:
            List<AuthorEntity> authors = session.selectEntity(qAuthor)
                    .orderBy(qAuthor.id.asc())
                    .fetchAll()

        then:
            authors.size() == 3
            authors[0].publications.size() == 2
            authors[1].publications.isEmpty()
            authors[2].publications.size() == 1

        where:
            session << allSessions
    }

    def "should return consistent results on multiple accesses to children collection"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity(null)
            QPublicationEntity qPublication = new QPublicationEntity(null)

            session.insertEntity(qAuthor, new AuthorEntity(1L, "Consistent Author", "consistent@test.com", []))
            session.insertEntity(qPublication, new PublicationEntity(1L, 1L, "Consistent Book", (Integer) 2020))

        when:
            AuthorEntity author = session.selectEntity(qAuthor).whereIdEq(1L).fetchSingle()

            // Access collection multiple times
            int size1 = author.publications.size()
            int size2 = author.publications.size()
            String title1 = author.publications[0].title
            String title2 = author.publications[0].title

        then:
            size1 == size2
            title1 == title2
            size1 == 1
            title1 == "Consistent Book"

        where:
            session << allSessions
    }

    // ==================== Query with Filtering ====================

    def "should select parent by id with children"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity(null)
            QPublicationEntity qPublication = new QPublicationEntity(null)

            session.insertEntity(qAuthor, new AuthorEntity(1L, "Author One", "one@test.com", []))
            session.insertEntity(qAuthor, new AuthorEntity(2L, "Author Two", "two@test.com", []))
            session.insertEntity(qPublication, new PublicationEntity(1L, 1L, "Book One", (Integer) 2020))
            session.insertEntity(qPublication, new PublicationEntity(2L, 2L, "Book Two", (Integer) 2021))

        when:
            AuthorEntity author = session.selectEntity(qAuthor).whereIdEq(2L).fetchSingle()

        then:
            author.id == 2L
            author.name == "Author Two"
            author.publications.size() == 1
            author.publications[0].title == "Book Two"

        where:
            session << allSessions
    }

    def "should select parents with filtering and children loaded"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity(null)
            QPublicationEntity qPublication = new QPublicationEntity(null)

            session.insertEntity(qAuthor, new AuthorEntity(1L, "Author Alpha", "alpha@test.com", []))
            session.insertEntity(qAuthor, new AuthorEntity(2L, "Author Beta", "beta@test.com", []))
            session.insertEntity(qAuthor, new AuthorEntity(3L, "Author Alpha Two", "alpha2@test.com", []))

            session.insertEntity(qPublication, new PublicationEntity(1L, 1L, "Alpha Book 1", (Integer) 2020))
            session.insertEntity(qPublication, new PublicationEntity(2L, 2L, "Beta Book", (Integer) 2021))
            session.insertEntity(qPublication, new PublicationEntity(3L, 3L, "Alpha Book 2", (Integer) 2022))

        when:
            // Filter using specific names (like is not available)
            List<AuthorEntity> alphaAuthors = session.selectEntity(qAuthor)
                    .where(qAuthor.name.eq("Author Alpha").or(qAuthor.name.eq("Author Alpha Two")))
                    .orderBy(qAuthor.id.asc())
                    .fetchAll()

        then:
            alphaAuthors.size() == 2
            alphaAuthors[0].name == "Author Alpha"
            alphaAuthors[0].publications.size() == 1
            alphaAuthors[0].publications[0].title == "Alpha Book 1"
            alphaAuthors[1].name == "Author Alpha Two"
            alphaAuthors[1].publications.size() == 1
            alphaAuthors[1].publications[0].title == "Alpha Book 2"

        where:
            session << allSessions
    }

    def "should cascade-delete removed child when updating parent entity"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity(null)
            QPublicationEntity qPublication = new QPublicationEntity(null)

            // Insert parent with 3 children
            session.insertEntity(qAuthor, new AuthorEntity(1L, "Author With Children", "author@test.com", []))
            session.insertEntity(qPublication, new PublicationEntity(1L, 1L, "Book One", (Integer) 2020))
            session.insertEntity(qPublication, new PublicationEntity(2L, 1L, "Book Two", (Integer) 2021))
            session.insertEntity(qPublication, new PublicationEntity(3L, 1L, "Book Three", (Integer) 2022))

        when:
            // Select parent — accessing publications triggers lazy load
            AuthorEntity author = session.selectEntity(qAuthor).whereIdEq(1L).fetchSingle()
            assert author.publications.size() == 3

            // Remove one child from the collection
            author.publications.removeIf { it.id == 2L }
            assert author.publications.size() == 2

            // Update parent — should cascade-delete the removed child
            session.updateEntity(qAuthor, author)

        then:
            // Re-select parent: should have only 2 children
            AuthorEntity updatedAuthor = session.selectEntity(qAuthor).whereIdEq(1L).fetchSingle()
            updatedAuthor.publications.size() == 2
            updatedAuthor.publications.any { it.id == 1L && it.title == "Book One" }
            updatedAuthor.publications.any { it.id == 3L && it.title == "Book Three" }

            // The removed child should no longer exist in the database
            session.selectEntity(qPublication).where(qPublication.id.eq(2L)).fetchAll().isEmpty()

        where:
            session << allSessions
    }

    def "should count parents correctly regardless of children"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity(null)
            QPublicationEntity qPublication = new QPublicationEntity(null)

            session.insertEntity(qAuthor, new AuthorEntity(1L, "Author 1", "a1@test.com", []))
            session.insertEntity(qAuthor, new AuthorEntity(2L, "Author 2", "a2@test.com", []))
            session.insertEntity(qAuthor, new AuthorEntity(3L, "Author 3", "a3@test.com", []))

            session.insertEntity(qPublication, new PublicationEntity(1L, 1L, "Book", (Integer) 2020))

        when:
            long count = session.selectEntity(qAuthor).count()

        then:
            count == 3

        where:
            session << allSessions
    }
}
