// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.BookEntity
import io.github.thinkfastpl.harbororm.test.domain.BookId
import io.github.thinkfastpl.harbororm.test.domain.QBookEntity

/**
 * Integration tests for composite ID operations.
 * Tests CRUD operations and partial ID queries with entities using embedded composite IDs.
 */
class CompositeIdIT extends AbstractHarborIT {

    def "insert entity with composite ID"() {
        given:
            QBookEntity qEntity = new QBookEntity(null)
            BookEntity entity = new BookEntity(
                    new BookId("978-3", 16148410),
                    "Clean Code",
                    "Robert C. Martin"
            )

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).fetchAll()) { books ->
                books.size() == 1
                with(books[0]) { book ->
                    book.id.isbnPrefix == "978-3"
                    book.id.isbnSuffix == 16148410
                    book.title == "Clean Code"
                    book.author == "Robert C. Martin"
                }
            }

        where:
            session << allSessions
    }

    def "find entity by composite ID using eq"() {
        given:
            QBookEntity qEntity = new QBookEntity(null)
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-3", 16148410),
                    "Clean Code",
                    "Robert C. Martin"
            ))
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-0", 13468599),
                    "The Pragmatic Programmer",
                    "David Thomas"
            ))
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-0", 20161622),
                    "Refactoring",
                    "Martin Fowler"
            ))

        when:
            BookEntity found = session.selectEntity(qEntity)
                    .where(qEntity.id.eq(new BookId("978-0", 13468599)))
                    .fetchSingle()

        then:
            found.id.isbnPrefix == "978-0"
            found.id.isbnSuffix == 13468599
            found.title == "The Pragmatic Programmer"
            found.author == "David Thomas"

        where:
            session << allSessions
    }

    def "find entities by composite ID using in"() {
        given:
            QBookEntity qEntity = new QBookEntity(null)
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-3", 16148410),
                    "Clean Code",
                    "Robert C. Martin"
            ))
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-0", 13468599),
                    "The Pragmatic Programmer",
                    "David Thomas"
            ))
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-0", 20161622),
                    "Refactoring",
                    "Martin Fowler"
            ))

        when:
            List<BookEntity> found = session.selectEntity(qEntity)
                    .where(qEntity.id.in([
                            new BookId("978-3", 16148410),
                            new BookId("978-0", 20161622)
                    ]))
                    .orderBy(qEntity.id.isbnSuffix.asc())
                    .fetchAll()

        then:
            found.size() == 2
            found[0].id.isbnPrefix == "978-3"
            found[0].title == "Clean Code"
            found[1].id.isbnPrefix == "978-0"
            found[1].title == "Refactoring"

        where:
            session << allSessions
    }

    def "update entity with composite ID"() {
        given:
            QBookEntity qEntity = new QBookEntity(null)
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-3", 16148410),
                    "Clean Code",
                    "Robert C. Martin"
            ))

        when:
            BookEntity entity = session.selectEntity(qEntity)
                    .where(qEntity.id.eq(new BookId("978-3", 16148410)))
                    .fetchSingle()
            entity.setTitle("Clean Code: A Handbook of Agile Software Craftsmanship")
            session.updateEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity)
                    .where(qEntity.id.eq(new BookId("978-3", 16148410)))
                    .fetchSingle()) { book ->
                book.id.isbnPrefix == "978-3"
                book.id.isbnSuffix == 16148410
                book.title == "Clean Code: A Handbook of Agile Software Craftsmanship"
                book.author == "Robert C. Martin"
            }

        where:
            session << allSessions
    }

    def "delete entity with composite ID"() {
        given:
            QBookEntity qEntity = new QBookEntity(null)
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-3", 16148410),
                    "Clean Code",
                    "Robert C. Martin"
            ))
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-0", 13468599),
                    "The Pragmatic Programmer",
                    "David Thomas"
            ))

        expect:
            session.selectEntity(qEntity).count() == 2

        when:
            BookEntity entity = session.selectEntity(qEntity)
                    .where(qEntity.id.eq(new BookId("978-3", 16148410)))
                    .fetchSingle()
            session.deleteEntity(qEntity, entity)

        then:
            session.selectEntity(qEntity).count() == 1
            session.selectEntity(qEntity)
                    .where(qEntity.id.eq(new BookId("978-3", 16148410)))
                    .count() == 0
            session.selectEntity(qEntity)
                    .where(qEntity.id.eq(new BookId("978-0", 13468599)))
                    .count() == 1

        where:
            session << allSessions
    }

    def "delete entity by composite ID"() {
        given:
            QBookEntity qEntity = new QBookEntity(null)
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-3", 16148410),
                    "Clean Code",
                    "Robert C. Martin"
            ))
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-0", 13468599),
                    "The Pragmatic Programmer",
                    "David Thomas"
            ))

        expect:
            session.selectEntity(qEntity).count() == 2

        when:
            session.deleteEntityById(qEntity, new BookId("978-3", 16148410))

        then:
            session.selectEntity(qEntity).count() == 1
            session.selectEntity(qEntity)
                    .where(qEntity.id.eq(new BookId("978-3", 16148410)))
                    .count() == 0

        where:
            session << allSessions
    }

    def "delete entities by composite IDs"() {
        given:
            QBookEntity qEntity = new QBookEntity(null)
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-3", 16148410),
                    "Clean Code",
                    "Robert C. Martin"
            ))
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-0", 13468599),
                    "The Pragmatic Programmer",
                    "David Thomas"
            ))
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-0", 20161622),
                    "Refactoring",
                    "Martin Fowler"
            ))

        expect:
            session.selectEntity(qEntity).count() == 3

        when:
            session.deleteEntityByIds(qEntity, [
                    new BookId("978-3", 16148410),
                    new BookId("978-0", 13468599)
            ])

        then:
            session.selectEntity(qEntity).count() == 1
            session.selectEntity(qEntity).fetchSingle().title == "Refactoring"

        where:
            session << allSessions
    }

    def "query with partial composite ID - filter by isbn prefix"() {
        given:
            QBookEntity qEntity = new QBookEntity(null)
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-3", 16148410),
                    "Clean Code",
                    "Robert C. Martin"
            ))
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-0", 13468599),
                    "The Pragmatic Programmer",
                    "David Thomas"
            ))
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-0", 20161622),
                    "Refactoring",
                    "Martin Fowler"
            ))

        when:
            List<BookEntity> found = session.selectEntity(qEntity)
                    .where(qEntity.id.isbnPrefix.eq("978-0"))
                    .orderBy(qEntity.id.isbnSuffix.asc())
                    .fetchAll()

        then:
            found.size() == 2
            found[0].title == "The Pragmatic Programmer"
            found[1].title == "Refactoring"

        where:
            session << allSessions
    }

    def "query with partial composite ID - filter by isbn suffix"() {
        given:
            QBookEntity qEntity = new QBookEntity(null)
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-3", 16148410),
                    "Clean Code",
                    "Robert C. Martin"
            ))
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-0", 13468599),
                    "The Pragmatic Programmer",
                    "David Thomas"
            ))
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-0", 20161622),
                    "Refactoring",
                    "Martin Fowler"
            ))

        when:
            BookEntity found = session.selectEntity(qEntity)
                    .where(qEntity.id.isbnSuffix.eq(16148410))
                    .fetchSingle()

        then:
            found.id.isbnPrefix == "978-3"
            found.title == "Clean Code"
            found.author == "Robert C. Martin"

        where:
            session << allSessions
    }

    def "combined query with partial composite ID and other columns"() {
        given:
            QBookEntity qEntity = new QBookEntity(null)
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-3", 16148410),
                    "Clean Code",
                    "Robert C. Martin"
            ))
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-0", 13468599),
                    "The Pragmatic Programmer",
                    "David Thomas"
            ))
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-0", 20161622),
                    "Refactoring",
                    "Martin Fowler"
            ))
            session.insertEntity(qEntity, new BookEntity(
                    new BookId("978-0", 13235088),
                    "Clean Architecture",
                    "Robert C. Martin"
            ))

        when:
            List<BookEntity> found = session.selectEntity(qEntity)
                    .where(qEntity.id.isbnPrefix.eq("978-0").and(qEntity.author.eq("Robert C. Martin")))
                    .fetchAll()

        then:
            found.size() == 1
            found[0].title == "Clean Architecture"

        where:
            session << allSessions
    }
}
