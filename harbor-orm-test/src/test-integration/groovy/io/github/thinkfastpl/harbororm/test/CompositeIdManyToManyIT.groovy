// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.test.domain.*

/**
 * Integration tests for @ManyToMany relations involving composite IDs.
 *
 * Covers:
 * - Owning side declared on a composite-ID entity (BookEntity.stores -> bookstore_books)
 * - Inverse side where the related entity has a composite ID, entity mode (LibraryEntity.books -> library_books)
 * - Inverse side, ID-only mode (LibraryEntity.bookIds -> library_books)
 */
class CompositeIdManyToManyIT extends AbstractHarborIT {

    QBookEntity qBook = new QBookEntity(null)
    QBookstoreEntity qStore = new QBookstoreEntity(null)
    QLibraryEntity qLibrary = new QLibraryEntity(null)

    // === OWNING SIDE: composite-ID entity declares the Set ===

    def "should insert composite-ID entity with Set<Entity> and create join table rows"() {
        given:
            BookstoreEntity downtown = new BookstoreEntity(1L, "Downtown Books")
            BookstoreEntity uptown = new BookstoreEntity(2L, "Uptown Books")
            session.insertEntity(qStore, downtown)
            session.insertEntity(qStore, uptown)

            BookEntity book = new BookEntity(new BookId("978-3", 16148410), "Clean Code", "Robert C. Martin")
            book.setStores(Set.of(downtown, uptown))

        when:
            session.insertEntity(qBook, book)

        then:
            BookEntity loaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-3", 16148410)))
                    .fetchSingle()
            loaded.stores.size() == 2
            loaded.stores.any { it.name == "Downtown Books" }
            loaded.stores.any { it.name == "Uptown Books" }

        where:
            session << allSessions
    }

    def "should handle mixed add and remove of stores on composite-ID owner update"() {
        given:
            BookstoreEntity downtown = new BookstoreEntity(11L, "Downtown Books")
            BookstoreEntity uptown = new BookstoreEntity(12L, "Uptown Books")
            BookstoreEntity midtown = new BookstoreEntity(13L, "Midtown Books")
            session.insertEntity(qStore, downtown)
            session.insertEntity(qStore, uptown)
            session.insertEntity(qStore, midtown)

            BookEntity book = new BookEntity(new BookId("978-0", 13468599), "The Pragmatic Programmer", "David Thomas")
            book.setStores(Set.of(downtown, uptown))
            session.insertEntity(qBook, book)

        when:
            BookEntity loaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-0", 13468599)))
                    .fetchSingle()
            loaded.stores.removeIf { it.name == "Downtown Books" }
            loaded.stores.add(midtown)
            session.updateEntity(qBook, loaded)

        then:
            BookEntity reloaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(new BookId("978-0", 13468599)))
                    .fetchSingle()
            reloaded.stores.size() == 2
            reloaded.stores.any { it.name == "Uptown Books" }
            reloaded.stores.any { it.name == "Midtown Books" }

        where:
            session << allSessions
    }

    def "should delete join table rows when composite-ID owner is deleted, but stores remain"() {
        given:
            BookstoreEntity downtown = new BookstoreEntity(21L, "Downtown Books")
            BookstoreEntity uptown = new BookstoreEntity(22L, "Uptown Books")
            session.insertEntity(qStore, downtown)
            session.insertEntity(qStore, uptown)

            BookId bookId = new BookId("978-0", 20161622)
            BookEntity book = new BookEntity(bookId, "Refactoring", "Martin Fowler")
            book.setStores(Set.of(downtown, uptown))
            session.insertEntity(qBook, book)

        when:
            BookEntity loaded = session.selectEntity(qBook)
                    .where(qBook.id.eq(bookId))
                    .fetchSingle()
            session.deleteEntity(qBook, loaded)

        then:
            session.selectEntity(qBook).count() == 0
            // Stores are not cascade-deleted
            session.selectEntity(qStore).count() == 2

        when: "a new book is inserted with the same composite ID"
            session.insertEntity(qBook, new BookEntity(bookId, "Refactoring 2nd Edition", "Martin Fowler"))

        then: "no stale join rows are resolved for it"
            BookEntity reinserted = session.selectEntity(qBook)
                    .where(qBook.id.eq(bookId))
                    .fetchSingle()
            reinserted.stores.isEmpty()

        where:
            session << allSessions
    }

    // === INVERSE SIDE, ENTITY MODE: related entity has composite ID ===

    def "should insert entity with Set<Entity> of composite-ID entities and create join table rows"() {
        given:
            BookEntity cleanCode = new BookEntity(new BookId("978-3", 16148410), "Clean Code", "Robert C. Martin")
            BookEntity pragProg = new BookEntity(new BookId("978-0", 13468599), "The Pragmatic Programmer", "David Thomas")
            session.insertEntity(qBook, cleanCode)
            session.insertEntity(qBook, pragProg)

            LibraryEntity library = new LibraryEntity(1L, "City Library", Set.of(cleanCode, pragProg), null)

        when:
            session.insertEntity(qLibrary, library)

        then:
            LibraryEntity loaded = session.selectEntity(qLibrary).whereIdEq(1L).fetchSingle()
            loaded.books.size() == 2
            loaded.books.any { it.id == new BookId("978-3", 16148410) && it.title == "Clean Code" }
            loaded.books.any { it.id == new BookId("978-0", 13468599) && it.title == "The Pragmatic Programmer" }

        where:
            session << allSessions
    }

    def "should batch load composite-ID related entities across multiple parents"() {
        given:
            BookEntity cleanCode = new BookEntity(new BookId("978-3", 16148410), "Clean Code", "Robert C. Martin")
            BookEntity pragProg = new BookEntity(new BookId("978-0", 13468599), "The Pragmatic Programmer", "David Thomas")
            BookEntity refactoring = new BookEntity(new BookId("978-0", 20161622), "Refactoring", "Martin Fowler")
            session.insertEntity(qBook, cleanCode)
            session.insertEntity(qBook, pragProg)
            session.insertEntity(qBook, refactoring)

            session.insertEntity(qLibrary, new LibraryEntity(11L, "City Library", Set.of(cleanCode, pragProg), null))
            session.insertEntity(qLibrary, new LibraryEntity(12L, "Campus Library", Set.of(refactoring), null))

        when:
            List<LibraryEntity> libraries = session.selectEntity(qLibrary).fetchAll()

        then:
            libraries.size() == 2
            with(libraries.find { it.name == "City Library" }) { city ->
                city.books.size() == 2
                city.books.any { it.title == "Clean Code" }
                city.books.any { it.title == "The Pragmatic Programmer" }
            }
            with(libraries.find { it.name == "Campus Library" }) { campus ->
                campus.books.size() == 1
                campus.books.first().title == "Refactoring"
            }

        where:
            session << allSessions
    }

    def "should handle mixed add and remove of composite-ID books on update"() {
        given:
            BookEntity cleanCode = new BookEntity(new BookId("978-3", 16148410), "Clean Code", "Robert C. Martin")
            BookEntity pragProg = new BookEntity(new BookId("978-0", 13468599), "The Pragmatic Programmer", "David Thomas")
            BookEntity refactoring = new BookEntity(new BookId("978-0", 20161622), "Refactoring", "Martin Fowler")
            session.insertEntity(qBook, cleanCode)
            session.insertEntity(qBook, pragProg)
            session.insertEntity(qBook, refactoring)

            session.insertEntity(qLibrary, new LibraryEntity(21L, "City Library", Set.of(cleanCode, pragProg), null))

        when:
            LibraryEntity loaded = session.selectEntity(qLibrary).whereIdEq(21L).fetchSingle()
            loaded.books.removeIf { it.title == "Clean Code" }
            loaded.books.add(refactoring)
            session.updateEntity(qLibrary, loaded)

        then:
            LibraryEntity reloaded = session.selectEntity(qLibrary).whereIdEq(21L).fetchSingle()
            reloaded.books.size() == 2
            reloaded.books.any { it.title == "The Pragmatic Programmer" }
            reloaded.books.any { it.title == "Refactoring" }

        where:
            session << allSessions
    }

    def "should delete join table rows when library is deleted, but composite-ID books remain"() {
        given:
            BookEntity cleanCode = new BookEntity(new BookId("978-3", 16148410), "Clean Code", "Robert C. Martin")
            BookEntity pragProg = new BookEntity(new BookId("978-0", 13468599), "The Pragmatic Programmer", "David Thomas")
            session.insertEntity(qBook, cleanCode)
            session.insertEntity(qBook, pragProg)

            session.insertEntity(qLibrary, new LibraryEntity(31L, "City Library", Set.of(cleanCode, pragProg), null))

        when:
            LibraryEntity loaded = session.selectEntity(qLibrary).whereIdEq(31L).fetchSingle()
            session.deleteEntity(qLibrary, loaded)

        then:
            session.selectEntity(qLibrary).count() == 0
            // Books are not cascade-deleted
            session.selectEntity(qBook).count() == 2

        where:
            session << allSessions
    }

    // === INVERSE SIDE, ID-ONLY MODE: Set<BookId> ===

    def "should insert entity with Set<BookId> and create join table rows"() {
        given:
            BookId cleanCodeId = new BookId("978-3", 16148410)
            BookId pragProgId = new BookId("978-0", 13468599)
            session.insertEntity(qBook, new BookEntity(cleanCodeId, "Clean Code", "Robert C. Martin"))
            session.insertEntity(qBook, new BookEntity(pragProgId, "The Pragmatic Programmer", "David Thomas"))

            LibraryEntity library = new LibraryEntity(41L, "City Library", null, Set.of(cleanCodeId, pragProgId))

        when:
            session.insertEntity(qLibrary, library)

        then:
            LibraryEntity loaded = session.selectEntity(qLibrary).whereIdEq(41L).fetchSingle()
            loaded.bookIds.size() == 2
            loaded.bookIds == Set.of(cleanCodeId, pragProgId)

        where:
            session << allSessions
    }

    def "should handle mixed add and remove on Set<BookId> update"() {
        given:
            BookId cleanCodeId = new BookId("978-3", 16148410)
            BookId pragProgId = new BookId("978-0", 13468599)
            BookId refactoringId = new BookId("978-0", 20161622)
            session.insertEntity(qBook, new BookEntity(cleanCodeId, "Clean Code", "Robert C. Martin"))
            session.insertEntity(qBook, new BookEntity(pragProgId, "The Pragmatic Programmer", "David Thomas"))
            session.insertEntity(qBook, new BookEntity(refactoringId, "Refactoring", "Martin Fowler"))

            session.insertEntity(qLibrary, new LibraryEntity(51L, "City Library", null, Set.of(cleanCodeId, pragProgId)))

        when:
            LibraryEntity loaded = session.selectEntity(qLibrary).whereIdEq(51L).fetchSingle()
            loaded.bookIds.remove(cleanCodeId)
            loaded.bookIds.add(refactoringId)
            session.updateEntity(qLibrary, loaded)

        then:
            LibraryEntity reloaded = session.selectEntity(qLibrary).whereIdEq(51L).fetchSingle()
            reloaded.bookIds.size() == 2
            reloaded.bookIds == Set.of(pragProgId, refactoringId)

        where:
            session << allSessions
    }
}
