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
 * Related entity for testing the owning side of @ManyToMany declared on the
 * composite-ID {@link BookEntity}. Intentionally has no reference back to BookEntity.
 */
@Entity(table = "bookstores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookstoreEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;
}
