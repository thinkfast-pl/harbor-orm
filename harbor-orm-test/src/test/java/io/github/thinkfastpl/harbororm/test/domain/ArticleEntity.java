// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.*;

import java.util.List;

/**
 * Test entity for @ElementCollection with simple String type.
 * Used by ElementCollectionIT for testing basic element collections.
 */
@Entity(table = "articles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ArticleEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String title;

    @ElementCollection(
            table = "article_tags",
            joinColumns = @JoinColumn(name = "article_id", fieldType = Long.class)
    )
    private List<@Column(name = "tag", nullable = false) String> tags;
}
