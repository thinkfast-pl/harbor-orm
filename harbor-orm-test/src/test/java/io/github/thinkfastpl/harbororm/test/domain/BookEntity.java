// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.LazyRef;
import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.*;

import java.util.List;
import java.util.Set;

/**
 * Entity with composite ID for testing composite primary key operations
 * and relations owned by a composite-ID parent.
 */
@Entity(table = "books")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BookEntity {

    @Id
    @Embedded
    private BookId id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @ElementCollection(
            table = "book_tags",
            joinColumns = {
                    @JoinColumn(name = "isbn_prefix", fieldType = String.class, referencedColumnName = "isbn_prefix"),
                    @JoinColumn(name = "isbn_suffix", fieldType = Integer.class, referencedColumnName = "isbn_suffix")
            }
    )
    private List<@Column(name = "tag", nullable = false) String> tags;

    @OneToMany(joinColumns = {
            @JoinColumn(name = "isbn_prefix", fieldType = String.class, referencedColumnName = "isbn_prefix"),
            @JoinColumn(name = "isbn_suffix", fieldType = Integer.class, referencedColumnName = "isbn_suffix")
    })
    private List<BookChapterEntity> chapters;

    @OneToOne(joinColumns = {
            @JoinColumn(name = "isbn_prefix", fieldType = String.class, referencedColumnName = "isbn_prefix"),
            @JoinColumn(name = "isbn_suffix", fieldType = Integer.class, referencedColumnName = "isbn_suffix")
    })
    private LazyRef<BookSummaryEntity> summary;

    @ManyToMany(
            table = "bookstore_books",
            joinColumns = {
                    @JoinColumn(name = "isbn_prefix", fieldType = String.class, referencedColumnName = "isbn_prefix"),
                    @JoinColumn(name = "isbn_suffix", fieldType = Integer.class, referencedColumnName = "isbn_suffix")
            },
            inverseJoinColumns = @JoinColumn(name = "store_id", fieldType = Long.class)
    )
    private Set<BookstoreEntity> stores;

    public BookEntity(BookId id, String title, String author) {
        this(id, title, author, null, null, null, null);
    }
}
