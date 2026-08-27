// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.repository.set3;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Embedded;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.*;

@Entity(table = "basics_embedded")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BasicEmbeddedEntity {

    @Id
    private Long id;

    @Column(name = "dest_country", nullable = false)
    private String destinationCountry;

    @Embedded
    private AddressEmbeddable destinationAddress;
}
