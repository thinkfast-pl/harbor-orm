// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Test entity for @OneToMany relationship.
 * Parent entity representing an author with multiple publications (child entities).
 * Used by OneToManyRelationIT for testing one-to-many relationships.
 */
@Entity(table = "authors")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class AuthorEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @OneToMany(joinColumns = @JoinColumn(
            name = "author_id",
            fieldType = Long.class
    ))
    private List<PublicationEntity> publications = new ArrayList<>();
}
