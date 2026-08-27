// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.QEmbeddable;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Equality condition that compares all columns of an embeddable object to corresponding values.
 */
@RequiredArgsConstructor
@Getter
public class EmbeddableEqCondition implements Condition {

    @NonNull
    private final QEmbeddable<?> qEmbeddable;
    private final Object value;

    @Override
    public Expression<Boolean> as(String alias) {
        throw new UnsupportedOperationException("Aliasing conditions on embeddable objects is not supported.");
    }
}
