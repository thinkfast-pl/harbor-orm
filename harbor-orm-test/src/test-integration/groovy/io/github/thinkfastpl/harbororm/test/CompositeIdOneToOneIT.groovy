// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.LazyRef
import io.github.thinkfastpl.harbororm.test.domain.*

/**
 * Integration tests for @OneToOne relations owned by a composite-ID parent.
 * BookEntity (composite BookId) -> LazyRef<BookSummaryEntity> joined via isbn_prefix + isbn_suffix.
 */
class CompositeIdOneToOneIT extends AbstractHarborIT {

    def "should insert book with summary via @OneToOne cascade and propagate composite FK"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            QBookSummaryEntity qSummary = new QBookSummaryEntity(null)
            BookEntity book = new BookEntity(
                    new BookId("978-3", 16148410),
                    "Clean Code",
                    "Robert C. Martin"
            )
            book.setSummary(LazyRef.of(new BookSummaryEntity(1L, null, 0, "A handbook of agile software craftsmanship")))

        when:
            session.insertEntity(qBook, book)

        then:
            session.selectEntity(qBook).count() == 1
            session.selectEntity(qSummary).count() == 1

            BookEntity reloaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-3", 16148410)))
                    .fetchSingle()
            with(reloaded.summary.get()) { child ->
                child.summary == "A handbook of agile software craftsmanship"
                child.isbnPrefix == reloaded.id.isbnPrefix
                child.isbnSuffix == reloaded.id.isbnSuffix
            }

        where:
            session << allSessions
    }

    def "should insert book with null summary and lazily resolve null"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            QBookSummaryEntity qSummary = new QBookSummaryEntity(null)
            BookEntity book = new BookEntity(
                    new BookId("978-0", 13468599),
                    "The Pragmatic Programmer",
                    "David Thomas"
            )

        when:
            session.insertEntity(qBook, book)

        then:
            session.selectEntity(qBook).count() == 1
            session.selectEntity(qSummary).count() == 0

            BookEntity reloaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-0", 13468599)))
                    .fetchSingle()
            reloaded.summary != null
            reloaded.summary.get() == null

        where:
            session << allSessions
    }

    def "should batch-load summaries for multiple books with composite IDs"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            BookEntity first = new BookEntity(
                    new BookId("978-3", 16148410),
                    "Clean Code",
                    "Robert C. Martin"
            )
            first.setSummary(LazyRef.of(new BookSummaryEntity(1L, null, 0, "Summary of Clean Code")))
            BookEntity second = new BookEntity(
                    new BookId("978-0", 20161622),
                    "Refactoring",
                    "Martin Fowler"
            )
            second.setSummary(LazyRef.of(new BookSummaryEntity(2L, null, 0, "Summary of Refactoring")))
            session.insertEntity(qBook, first)
            session.insertEntity(qBook, second)

        when:
            List<BookEntity> books = session.selectEntity(qBook)
                    .orderBy(qBook.id.isbnSuffix.asc())
                    .fetchAll()

        then:
            books.size() == 2
            books[0].id.isbnSuffix == 16148410
            books[0].summary.get() != null
            books[0].summary.get().summary == "Summary of Clean Code"
            books[0].summary.get().isbnPrefix == "978-3"
            books[0].summary.get().isbnSuffix == 16148410
            books[1].id.isbnSuffix == 20161622
            books[1].summary.get() != null
            books[1].summary.get().summary == "Summary of Refactoring"
            books[1].summary.get().isbnPrefix == "978-0"
            books[1].summary.get().isbnSuffix == 20161622

        where:
            session << allSessions
    }

    def "should cascade update to modified summary entity"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            QBookSummaryEntity qSummary = new QBookSummaryEntity(null)
            BookEntity book = new BookEntity(
                    new BookId("978-1", 59327584),
                    "The Linux Command Line",
                    "William Shotts"
            )
            book.setSummary(LazyRef.of(new BookSummaryEntity(1L, null, 0, "Old summary text")))
            session.insertEntity(qBook, book)

        when:
            BookEntity loaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-1", 59327584)))
                    .fetchSingle()
            loaded.summary.get().setSummary("New summary text")
            session.updateEntity(qBook, loaded)

        then:
            BookEntity reloaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-1", 59327584)))
                    .fetchSingle()
            reloaded.summary.get().summary == "New summary text"
            session.selectEntity(qSummary).whereIdEq(1L).fetchSingle().summary == "New summary text"

        where:
            session << allSessions
    }

    def "should replace summary with a different entity on update"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            QBookSummaryEntity qSummary = new QBookSummaryEntity(null)
            BookEntity book = new BookEntity(
                    new BookId("978-0", 13235088),
                    "Clean Architecture",
                    "Robert C. Martin"
            )
            book.setSummary(LazyRef.of(new BookSummaryEntity(1L, null, 0, "Old summary")))
            session.insertEntity(qBook, book)

        when:
            BookEntity loaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-0", 13235088)))
                    .fetchSingle()
            loaded.summary.get() // trigger lazy load
            loaded.setSummary(LazyRef.of(new BookSummaryEntity(2L, null, 0, "New summary")))
            session.updateEntity(qBook, loaded)

        then:
            session.selectEntity(qSummary).count() == 1
            session.selectEntity(qSummary).whereIdEq(1L).count() == 0
            with(session.selectEntity(qSummary).whereIdEq(2L).fetchSingle()) { child ->
                child.summary == "New summary"
                child.isbnPrefix == "978-0"
                child.isbnSuffix == 13235088
            }
            BookEntity reloaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-0", 13235088)))
                    .fetchSingle()
            reloaded.summary.get().id == 2L
            reloaded.summary.get().summary == "New summary"

        where:
            session << allSessions
    }

    def "should remove summary when LazyRef replaced with null-holding ref"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            QBookSummaryEntity qSummary = new QBookSummaryEntity(null)
            BookEntity book = new BookEntity(
                    new BookId("978-1", 68050386),
                    "The Rust Programming Language",
                    "Steve Klabnik"
            )
            book.setSummary(LazyRef.of(new BookSummaryEntity(1L, null, 0, "Summary to remove")))
            session.insertEntity(qBook, book)

        when:
            BookEntity loaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-1", 68050386)))
                    .fetchSingle()
            loaded.summary.get() // trigger lazy load
            loaded.setSummary(LazyRef.of(null))
            session.updateEntity(qBook, loaded)

        then:
            session.selectEntity(qSummary).count() == 0
            BookEntity reloaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-1", 68050386)))
                    .fetchSingle()
            reloaded.summary.get() == null

        where:
            session << allSessions
    }

    def "should cascade delete summary when book is deleted"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            QBookSummaryEntity qSummary = new QBookSummaryEntity(null)
            BookEntity book = new BookEntity(
                    new BookId("978-0", 32163537),
                    "Domain-Driven Design",
                    "Eric Evans"
            )
            book.setSummary(LazyRef.of(new BookSummaryEntity(1L, null, 0, "Tackling complexity in software")))
            session.insertEntity(qBook, book)

        expect:
            session.selectEntity(qBook).count() == 1
            session.selectEntity(qSummary).count() == 1

        when:
            BookEntity loaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-0", 32163537)))
                    .fetchSingle()
            session.deleteEntity(qBook, loaded)

        then:
            session.selectEntity(qBook).count() == 0
            session.selectEntity(qSummary).count() == 0

        where:
            session << allSessions
    }
}
