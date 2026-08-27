// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Child entity of {@link BookEntity} for testing @OneToOne with a composite-ID parent.
 */
@Entity(table = "book_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookSummaryEntity {

    @Id
    private Long id;

    @Column(name = "isbn_prefix", nullable = false)
    private String isbnPrefix;

    @Column(name = "isbn_suffix", nullable = false)
    private int isbnSuffix;

    @Column(nullable = false)
    private String summary;
}
