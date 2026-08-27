// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.repository.set5;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Embeddable;
import io.github.thinkfastpl.harbororm.api.annotations.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Embeddable
@Getter
@FieldNameConstants
@NoArgsConstructor
@AllArgsConstructor
public class CompId {

    public enum Type {
        A,
        B
    }

    @Column(nullable = false)
    @Enumerated
    private Type type;

    @Column(nullable = false)
    private int num;
}
