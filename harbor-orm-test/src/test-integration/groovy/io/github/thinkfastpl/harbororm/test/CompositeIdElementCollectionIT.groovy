// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.BookEntity
import io.github.thinkfastpl.harbororm.test.domain.BookId
import io.github.thinkfastpl.harbororm.test.domain.QBookEntity

/**
 * Integration tests for @ElementCollection owned by an entity with a composite ID.
 *
 * BookEntity has a composite ID (BookId: isbn_prefix + isbn_suffix) and a
 * List&lt;String&gt; tags element collection stored in the book_tags table
 * (columns: isbn_prefix, isbn_suffix, tag).
 *
 * Tests cover:
 * - Insert cascades tags to the collection table
 * - Null and empty tag lists
 * - Batch lazy loading of collections for multiple entities
 * - Update after mutating the loaded collection
 * - Update with a fully replaced plain list
 * - Cascading delete of collection rows on entity delete
 */
class CompositeIdElementCollectionIT extends AbstractHarborIT {

    def "should cascade tags to collection table when inserting book with composite ID"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            BookId id = new BookId("978-3", 16148410)
            BookEntity book = new BookEntity(id, "Clean Code", "Robert C. Martin")
            book.setTags(new ArrayList<>(["java", "craftsmanship", "agile"]))

        when:
            session.insertEntity(qBook, book)

        then:
            with(session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-3", 16148410)))
                    .fetchSingle()) { loaded ->
                loaded.id.isbnPrefix == "978-3"
                loaded.id.isbnSuffix == 16148410
                loaded.title == "Clean Code"
                loaded.author == "Robert C. Martin"
                loaded.tags != null
                loaded.tags.size() == 3
                loaded.tags.containsAll(["java", "craftsmanship", "agile"])
            }

        where:
            session << allSessions
    }

    def "should load empty tags when book inserted with null or empty tag list"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            BookEntity withNullTags = new BookEntity(
                    new BookId("978-0", 13468599),
                    "The Pragmatic Programmer",
                    "David Thomas"
            )
            BookEntity withEmptyTags = new BookEntity(
                    new BookId("978-0", 20161622),
                    "Refactoring",
                    "Martin Fowler"
            )
            withEmptyTags.setTags(new ArrayList<>())

        when:
            session.insertEntity(qBook, withNullTags)
            session.insertEntity(qBook, withEmptyTags)

        then:
            with(session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-0", 13468599)))
                    .fetchSingle()) { loaded ->
                loaded.title == "The Pragmatic Programmer"
                loaded.tags.isEmpty()
            }

            with(session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-0", 20161622)))
                    .fetchSingle()) { loaded ->
                loaded.title == "Refactoring"
                loaded.tags.isEmpty()
            }

        where:
            session << allSessions
    }

    def "should load each book with exactly its own tags when fetching multiple books"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            BookEntity first = new BookEntity(
                    new BookId("978-1", 61729205),
                    "Domain-Driven Design",
                    "Eric Evans"
            )
            first.setTags(new ArrayList<>(["ddd", "architecture"]))
            BookEntity second = new BookEntity(
                    new BookId("978-1", 49204551),
                    "Designing Data-Intensive Applications",
                    "Martin Kleppmann"
            )
            second.setTags(new ArrayList<>(["distributed", "databases", "streaming"]))
            session.insertEntity(qBook, first)
            session.insertEntity(qBook, second)

        when:
            List<BookEntity> books = session.selectEntity(qBook)
                    .orderBy(qBook.id.isbnSuffix.asc())
                    .fetchAll()

        then:
            books.size() == 2

            with(books[0]) { book ->
                book.id.isbnSuffix == 49204551
                book.title == "Designing Data-Intensive Applications"
                book.tags.size() == 3
                book.tags.containsAll(["distributed", "databases", "streaming"])
            }

            with(books[1]) { book ->
                book.id.isbnSuffix == 61729205
                book.title == "Domain-Driven Design"
                book.tags.size() == 2
                book.tags.containsAll(["ddd", "architecture"])
            }

        where:
            session << allSessions
    }

    def "should persist collection changes when mutating loaded tags list before update"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            BookEntity book = new BookEntity(
                    new BookId("978-0", 32163161),
                    "Working Effectively with Legacy Code",
                    "Michael Feathers"
            )
            book.setTags(new ArrayList<>(["legacy", "testing", "refactoring"]))
            session.insertEntity(qBook, book)

        when:
            BookEntity loaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-0", 32163161)))
                    .fetchSingle()
            loaded.tags.add("seams")
            loaded.tags.remove("legacy")
            session.updateEntity(qBook, loaded)

        then:
            with(session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-0", 32163161)))
                    .fetchSingle()) { reloaded ->
                reloaded.tags.size() == 3
                reloaded.tags.containsAll(["testing", "refactoring", "seams"])
                !reloaded.tags.contains("legacy")
            }

        where:
            session << allSessions
    }

    def "should fully replace tags when setting a new plain list before update"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            BookEntity book = new BookEntity(
                    new BookId("978-0", 59651038),
                    "Head First Design Patterns",
                    "Eric Freeman"
            )
            book.setTags(new ArrayList<>(["patterns", "oop"]))
            session.insertEntity(qBook, book)

        when:
            BookEntity loaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-0", 59651038)))
                    .fetchSingle()
            loaded.setTags(new ArrayList<>(["design", "gof", "java"]))
            session.updateEntity(qBook, loaded)

        then:
            with(session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-0", 59651038)))
                    .fetchSingle()) { reloaded ->
                reloaded.tags.size() == 3
                reloaded.tags.containsAll(["design", "gof", "java"])
                !reloaded.tags.contains("patterns")
                !reloaded.tags.contains("oop")
            }

        where:
            session << allSessions
    }

    def "should cascade delete of tags when deleting book with composite ID"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            BookId id = new BookId("978-0", 76790845)
            BookEntity book = new BookEntity(id, "Release It!", "Michael Nygard")
            book.setTags(new ArrayList<>(["stability", "operations", "resilience"]))
            session.insertEntity(qBook, book)

        when:
            BookEntity loaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-0", 76790845)))
                    .fetchSingle()
            session.deleteEntity(qBook, loaded)

            // Re-insert a fresh book under the SAME composite ID with no tags.
            // If the old book_tags rows were not cascaded, they would resurface here.
            BookEntity reborn = new BookEntity(
                    new BookId("978-0", 76790845),
                    "Release It! Second Edition",
                    "Michael Nygard"
            )
            session.insertEntity(qBook, reborn)

        then:
            with(session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-0", 76790845)))
                    .fetchSingle()) { reloaded ->
                reloaded.title == "Release It! Second Edition"
                reloaded.tags.isEmpty()
            }

        where:
            session << allSessions
    }
}
