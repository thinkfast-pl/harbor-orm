// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Embeddable;
import io.github.thinkfastpl.harbororm.api.annotations.Embedded;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressWithGeoEmbeddable {

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    @Embedded
    private GeoLocationEmbeddable geoLocation;
}
