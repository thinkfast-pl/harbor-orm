// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * Entity for testing the inverse side of @ManyToMany where the related entity
 * ({@link BookEntity}) has a composite ID — both the full-entity and ID-only variants.
 */
@Entity(table = "libraries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LibraryEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToMany(
            table = "library_books",
            joinColumns = @JoinColumn(name = "library_id", fieldType = Long.class),
            inverseJoinColumns = {
                    @JoinColumn(name = "isbn_prefix", fieldType = String.class, referencedColumnName = "isbn_prefix"),
                    @JoinColumn(name = "isbn_suffix", fieldType = Integer.class, referencedColumnName = "isbn_suffix")
            }
    )
    private Set<BookEntity> books;

    @ManyToMany(
            table = "library_books",
            joinColumns = @JoinColumn(name = "library_id", fieldType = Long.class),
            inverseJoinColumns = {
                    @JoinColumn(name = "isbn_prefix", fieldType = String.class, referencedColumnName = "isbn_prefix"),
                    @JoinColumn(name = "isbn_suffix", fieldType = Integer.class, referencedColumnName = "isbn_suffix")
            }
    )
    private Set<BookId> bookIds;
}
