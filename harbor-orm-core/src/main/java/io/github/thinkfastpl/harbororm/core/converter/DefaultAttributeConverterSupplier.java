// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.converter;

import io.github.thinkfastpl.harbororm.api.converter.AttributeConverter;
import io.github.thinkfastpl.harbororm.api.converter.AttributeConverterSupplier;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default {@link AttributeConverterSupplier} that creates converter instances via their
 * no-arg constructor and caches them for reuse.
 * <p>
 * Thread-safe: backed by a {@link java.util.concurrent.ConcurrentHashMap}.
 */
public class DefaultAttributeConverterSupplier implements AttributeConverterSupplier {

    private final Map<Class<? extends AttributeConverter<?, ?>>, AttributeConverter<?, ?>> converters = new ConcurrentHashMap<>();

    @Override
    public AttributeConverter<?, ?> supply(@NonNull Class<? extends AttributeConverter<?, ?>> clazz) {
        return converters.computeIfAbsent(clazz, aClass -> {
            try {
                return aClass.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
