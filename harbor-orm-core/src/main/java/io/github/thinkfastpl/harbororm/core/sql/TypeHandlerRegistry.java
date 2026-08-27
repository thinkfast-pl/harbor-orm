// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.api.sql.SqlTypeHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Static cache for {@link SqlTypeHandler} instances.
 * Handlers are instantiated via no-arg constructor and reused.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TypeHandlerRegistry {

    private static final ConcurrentHashMap<Class<?>, SqlTypeHandler<?>> CACHE = new ConcurrentHashMap<>();

    /**
     * Returns the cached {@link SqlTypeHandler} for the given class, instantiating it via
     * its no-arg constructor on first access. Subsequent calls return the same instance.
     *
     * @param handlerClass the {@link SqlTypeHandler} implementation class
     * @param <T> the type handled by the handler
     * @return the cached handler instance
     * @throws RuntimeException if the handler cannot be instantiated
     */
    @SuppressWarnings("unchecked")
    public static <T> SqlTypeHandler<T> get(Class<?> handlerClass) {
        return (SqlTypeHandler<T>) CACHE.computeIfAbsent(handlerClass, clazz -> {
            try {
                return (SqlTypeHandler<?>) clazz.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot instantiate SqlTypeHandler: " + clazz.getName(), e);
            }
        });
    }
}
