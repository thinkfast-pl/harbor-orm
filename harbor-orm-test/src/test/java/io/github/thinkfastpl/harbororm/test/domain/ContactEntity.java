// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.*;

import java.util.List;

/**
 * Test entity for @ElementCollection with @Embeddable elements.
 * Used by ElementCollectionIT for testing embeddable element collections.
 */
@Entity(table = "contacts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ContactEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @ElementCollection(
            table = "contact_phones",
            joinColumns = @JoinColumn(name = "contact_id", fieldType = Long.class)
    )
    private List<@Embedded PhoneNumber> phoneNumbers;
}
