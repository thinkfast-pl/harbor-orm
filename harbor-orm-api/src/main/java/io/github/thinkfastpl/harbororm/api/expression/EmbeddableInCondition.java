// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.QEmbeddable;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * {@code IN} condition that checks an embeddable object's columns against a collection of values.
 */
@RequiredArgsConstructor
@Getter
public class EmbeddableInCondition implements Condition {

    @NonNull
    private final QEmbeddable<?> qEmbeddable;
    private final Collection<?> values;

    @Override
    public Expression<Boolean> as(String alias) {
        throw new UnsupportedOperationException("Aliasing conditions on embeddable objects is not supported.");
    }
}
