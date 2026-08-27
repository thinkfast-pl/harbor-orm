// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.*;

/**
 * Test entity for @OneToMany relationship.
 * Child entity representing a publication belonging to an author (parent entity).
 * Used by OneToManyRelationIT for testing one-to-many relationships.
 */
@Entity(table = "publications")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PublicationEntity {

    @Id
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private String title;

    @Column(name = "publication_year", nullable = false)
    private Integer publicationYear;
}
