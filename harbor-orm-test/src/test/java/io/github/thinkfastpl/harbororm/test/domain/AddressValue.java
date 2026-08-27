// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Embeddable;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Embeddable
@Value
@FieldNameConstants
public class AddressValue {

    @Column(nullable = false)
    String region;

    @Column(name = "postal_code", nullable = false)
    String postalCode;

    @Column(nullable = false)
    String town;

    @Column(nullable = false)
    String street;

    @Column(name = "building_no", nullable = false)
    String buildingNo;

    @Column(name = "apartment_no", nullable = true)
    String apartmentNo;
}
