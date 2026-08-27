// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import lombok.*;

/**
 * A simple value object that can be serialized to/from JSON.
 * Used to test custom converter functionality.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class JsonMetadata {

    private String key;
    private String value;
    private int count;
}
