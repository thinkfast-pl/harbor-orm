// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Embeddable value object representing a phone number with type.
 * Used by ContactEntity's @ElementCollection for testing embeddable element collections.
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumber {

    @Column(name = "phone_type", nullable = false)
    private String type;

    @Column(name = "phone_number", nullable = false)
    private String number;
}
