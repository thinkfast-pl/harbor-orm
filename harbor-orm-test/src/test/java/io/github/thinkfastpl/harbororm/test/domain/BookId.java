// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Composite ID for BookEntity - consists of ISBN prefix and ISBN suffix.
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookId {

    @Column(name = "isbn_prefix", nullable = false)
    private String isbnPrefix;

    @Column(name = "isbn_suffix", nullable = false)
    private int isbnSuffix;
}
