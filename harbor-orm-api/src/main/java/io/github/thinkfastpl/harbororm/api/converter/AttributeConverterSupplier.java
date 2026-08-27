// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.converter;

import lombok.NonNull;

/**
 * Provides custom instantiation of {@link AttributeConverter} instances.
 *
 * <p>By default, converters referenced by {@link io.github.thinkfastpl.harbororm.api.annotations.Convert @Convert}
 * are created using their public no-argument constructor. Implementing this interface allows
 * alternative instantiation strategies -- for example, resolving converters as Spring beans
 * with injected dependencies.
 *
 * <p>A custom supplier is passed to
 * {@code HarborSessionFactory.builder().attributeConverterSupplier(supplier).build()}.
 * The supplier is called once per converter class; the returned instance is cached and reused
 * for the lifetime of the session.
 *
 * <h3>Example -- Spring integration</h3>
 * <pre>{@code
 * AttributeConverterSupplier supplier = clazz -> {
 *     try {
 *         return applicationContext.getBean(clazz);
 *     } catch (NoSuchBeanDefinitionException e) {
 *         return clazz.getConstructor().newInstance();
 *     }
 * };
 * }</pre>
 *
 * @see AttributeConverter
 * @see io.github.thinkfastpl.harbororm.api.annotations.Convert
 */
public interface AttributeConverterSupplier {

    /**
     * Creates or retrieves an {@link AttributeConverter} instance for the given converter class.
     *
     * <p>The returned instance will be cached and reused, so it should be safe for
     * concurrent use.
     *
     * @param clazz the converter class to instantiate
     * @return a converter instance; never {@code null}
     */
    AttributeConverter<?, ?> supply(@NonNull Class<? extends AttributeConverter<?, ?>> clazz);
}
