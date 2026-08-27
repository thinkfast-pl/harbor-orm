// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.test.domain.*

/**
 * Integration tests for @OneToMany relationship owned by a parent with a composite (embedded) ID.
 *
 * Tests cover:
 * - Cascade insert of children with composite-ID join columns propagated automatically
 * - Null and empty children collections
 * - Batch lazy loading of children for multiple composite-ID parents
 * - Cascade update (add / remove / modify children via parent update)
 * - Cascade delete of children when deleting the parent
 */
class CompositeIdOneToManyIT extends AbstractHarborIT {

    def "insert book with chapters cascades and propagates composite FK columns"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            BookId bookId = new BookId("978-1", 11111111)

            BookEntity book = new BookEntity(bookId, "Clean Code", "Robert C. Martin")
            book.setChapters(new ArrayList<>([
                    new BookChapterEntity(101L, null, 0, "Clean Code Intro"),
                    new BookChapterEntity(102L, null, 0, "Meaningful Names")
            ]))

        when:
            session.insertEntity(qBook, book)

        then:
            BookEntity loaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(bookId))
                    .fetchSingle()

            loaded.id.isbnPrefix == "978-1"
            loaded.id.isbnSuffix == 11111111
            loaded.chapters != null
            loaded.chapters.size() == 2
            loaded.chapters.any { it.id == 101L && it.title == "Clean Code Intro" }
            loaded.chapters.any { it.id == 102L && it.title == "Meaningful Names" }

            // FK columns of every chapter must equal the parent's composite ID parts
            loaded.chapters.every {
                it.isbnPrefix == loaded.id.isbnPrefix && it.isbnSuffix == loaded.id.isbnSuffix
            }

        where:
            session << allSessions
    }

    def "insert book with null chapters and with empty chapters loads empty collections"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            BookId nullChaptersId = new BookId("978-2", 22222221)
            BookId emptyChaptersId = new BookId("978-2", 22222222)

            // Convenience constructor leaves chapters null
            BookEntity bookWithNullChapters = new BookEntity(nullChaptersId, "Null Chapters Book", "Author A")

            BookEntity bookWithEmptyChapters = new BookEntity(emptyChaptersId, "Empty Chapters Book", "Author B")
            bookWithEmptyChapters.setChapters(new ArrayList<>())

        when:
            session.insertEntity(qBook, bookWithNullChapters)
            session.insertEntity(qBook, bookWithEmptyChapters)

        then:
            BookEntity loadedNull = session.selectEntity(qBook)
                    .where(qBook.id.eq(nullChaptersId))
                    .fetchSingle()
            loadedNull.chapters != null
            loadedNull.chapters.isEmpty()

            BookEntity loadedEmpty = session.selectEntity(qBook)
                    .where(qBook.id.eq(emptyChaptersId))
                    .fetchSingle()
            loadedEmpty.chapters != null
            loadedEmpty.chapters.isEmpty()

        where:
            session << allSessions
    }

    def "batch lazy loading assigns each book exactly its own chapters"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            BookId firstId = new BookId("978-3", 33333331)
            BookId secondId = new BookId("978-3", 33333332)

            BookEntity firstBook = new BookEntity(firstId, "Refactoring", "Martin Fowler")
            firstBook.setChapters(new ArrayList<>([
                    new BookChapterEntity(301L, null, 0, "Refactoring Principles"),
                    new BookChapterEntity(302L, null, 0, "Bad Smells in Code")
            ]))

            BookEntity secondBook = new BookEntity(secondId, "TDD by Example", "Kent Beck")
            secondBook.setChapters(new ArrayList<>([
                    new BookChapterEntity(303L, null, 0, "The Money Example")
            ]))

            session.insertEntity(qBook, firstBook)
            session.insertEntity(qBook, secondBook)

        when:
            List<BookEntity> books = session.selectEntity(qBook)
                    .where(qBook.id.isbnPrefix.eq("978-3"))
                    .orderBy(qBook.id.isbnSuffix.asc())
                    .fetchAll()

        then:
            books.size() == 2

            with(books[0]) { b ->
                b.id.isbnSuffix == 33333331
                b.chapters.size() == 2
                b.chapters.any { it.id == 301L && it.title == "Refactoring Principles" }
                b.chapters.any { it.id == 302L && it.title == "Bad Smells in Code" }
                b.chapters.every { it.isbnPrefix == "978-3" && it.isbnSuffix == 33333331 }
            }

            with(books[1]) { b ->
                b.id.isbnSuffix == 33333332
                b.chapters.size() == 1
                b.chapters[0].id == 303L
                b.chapters[0].title == "The Money Example"
                b.chapters[0].isbnPrefix == "978-3"
                b.chapters[0].isbnSuffix == 33333332
            }

        where:
            session << allSessions
    }

    def "update book cascades added, removed and modified chapters"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            QBookChapterEntity qChapter = new QBookChapterEntity(null)
            BookId bookId = new BookId("978-4", 44444444)

            BookEntity book = new BookEntity(bookId, "Domain-Driven Design", "Eric Evans")
            book.setChapters(new ArrayList<>([
                    new BookChapterEntity(401L, null, 0, "Chapter A"),
                    new BookChapterEntity(402L, null, 0, "Chapter B")
            ]))
            session.insertEntity(qBook, book)

        when:
            BookEntity loaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(bookId))
                    .fetchSingle()
            assert loaded.chapters.size() == 2

            // Remove one loaded chapter
            BookChapterEntity removed = loaded.chapters.remove(0)
            Long removedId = removed.id

            // Modify the remaining loaded chapter's title
            BookChapterEntity remaining = loaded.chapters[0]
            Long remainingId = remaining.id
            remaining.setTitle("Renamed Chapter")

            // Add a new chapter
            loaded.chapters.add(new BookChapterEntity(403L, null, 0, "Added Chapter"))

            session.updateEntity(qBook, loaded)

        then:
            BookEntity reloaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(bookId))
                    .fetchSingle()

            reloaded.chapters.size() == 2
            reloaded.chapters.every { it.id != removedId }
            reloaded.chapters.any { it.id == remainingId && it.title == "Renamed Chapter" }
            reloaded.chapters.any { it.id == 403L && it.title == "Added Chapter" }
            reloaded.chapters.every { it.isbnPrefix == "978-4" && it.isbnSuffix == 44444444 }

            // Removed chapter must be gone from the database
            session.selectEntity(qChapter).whereIdEq(removedId).count() == 0
            // Added and modified chapters exist in the database
            session.selectEntity(qChapter).whereIdEq(403L).fetchSingle().title == "Added Chapter"
            session.selectEntity(qChapter).whereIdEq(remainingId).fetchSingle().title == "Renamed Chapter"

        where:
            session << allSessions
    }

    def "delete book cascades deletion of its chapters"() {
        given:
            QBookEntity qBook = new QBookEntity(null)
            QBookChapterEntity qChapter = new QBookChapterEntity(null)
            BookId bookId = new BookId("978-5", 55555555)

            BookEntity book = new BookEntity(bookId, "Working Effectively with Legacy Code", "Michael Feathers")
            book.setChapters(new ArrayList<>([
                    new BookChapterEntity(501L, null, 0, "Changing Software"),
                    new BookChapterEntity(502L, null, 0, "Seam Model")
            ]))
            session.insertEntity(qBook, book)

        expect:
            session.selectEntity(qChapter)
                    .where(qChapter.id.in([501L, 502L]))
                    .count() == 2

        when:
            BookEntity loaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(bookId))
                    .fetchSingle()
            session.deleteEntity(qBook, loaded)

        then:
            session.selectEntity(qBook)
                    .where(qBook.id.eq(bookId))
                    .count() == 0
            QBookChapterEntity qChapterCheck = new QBookChapterEntity(null)
            session.selectEntity(qChapterCheck)
                    .where(qChapterCheck.id.in([501L, 502L]))
                    .count() == 0

        where:
            session << allSessions
    }
}
