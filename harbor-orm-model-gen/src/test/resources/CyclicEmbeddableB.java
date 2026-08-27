// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Embeddable;
import io.github.thinkfastpl.harbororm.api.annotations.Embedded;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CyclicEmbeddableB {

    @Column(nullable = false)
    private String value;

    @Embedded
    private CyclicEmbeddableA nested;
}
