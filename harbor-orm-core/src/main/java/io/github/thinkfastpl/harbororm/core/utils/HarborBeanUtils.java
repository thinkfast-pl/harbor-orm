// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.utils;

import lombok.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reflection utilities for bean instantiation, property access, and method invocation.
 * <p>
 * Used internally to construct entity and embeddable instances, read/write field values,
 * invoke lifecycle callbacks, and find matching constructors.
 */
public class HarborBeanUtils {

    /**
     * Creates a new instance of the given class using its no-arg constructor.
     * The constructor is made accessible even if it is not public.
     *
     * @param clazz the class to instantiate
     * @param <T>   the type of the instance
     * @return a new instance of the given class
     * @throws RuntimeException if instantiation fails (e.g., no no-arg constructor exists)
     */
    public static <T> T newInstance(@NonNull Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads a field value from a bean by field name using reflection.
     * The field is made accessible even if it is not public.
     *
     * @param bean         the object to read the field from
     * @param propertyName the name of the field to read
     * @return the value of the field, or {@code null} if the field is null
     * @throws RuntimeException if the field does not exist or cannot be accessed
     */
    public static Object getPropertyValue(@NonNull Object bean, @NonNull String propertyName) {
        try {
            Field field = findField(bean.getClass(), propertyName);
            field.setAccessible(true);
            return field.get(bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads a nested field value by walking a property path (e.g. {@code ["id", "isbnPrefix"]}).
     * Each path element is resolved with {@link #getPropertyValue(Object, String)} on the
     * previous element's value.
     *
     * @param bean         the object to start from
     * @param propertyPath the ordered field names to traverse
     * @return the value at the end of the path, or {@code null} if any intermediate value is null
     * @throws RuntimeException if a field on the path does not exist or cannot be accessed
     */
    public static Object getPropertyValue(@NonNull Object bean, @NonNull java.util.List<String> propertyPath) {
        Object current = bean;
        for (String propertyName : propertyPath) {
            if (current == null) {
                return null;
            }
            current = getPropertyValue(current, propertyName);
        }
        return current;
    }

    /**
     * Reads and casts a field value from a bean by field name using reflection.
     *
     * @param bean          the object to read the field from
     * @param propertyName  the name of the field to read
     * @param propertyClass the expected type of the field value
     * @param <T>           the expected type of the field value
     * @return the value of the field cast to the given type
     * @throws ClassCastException if the field value is not assignable to the given type
     * @throws RuntimeException   if the field does not exist or cannot be accessed
     */
    public static <T> T getPropertyValue(@NonNull Object bean, @NonNull String propertyName, @NonNull Class<T> propertyClass) {
        return propertyClass.cast(getPropertyValue(bean, propertyName));
    }

    /**
     * Sets a field value on a bean by field name using reflection.
     * The field is made accessible even if it is not public.
     *
     * @param bean         the object to set the field on
     * @param propertyName the name of the field to set
     * @param value        the value to assign to the field
     * @throws RuntimeException if the field does not exist or cannot be accessed
     */
    public static void setPropertyValue(@NonNull Object bean, @NonNull String propertyName, Object value) {
        try {
            Field field = findField(bean.getClass(), propertyName);
            field.setAccessible(true);
            field.set(bean, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Invokes a no-arg method on a bean by method name using reflection.
     * The method is made accessible even if it is not public.
     *
     * @param bean       the object on which to invoke the method
     * @param methodName the name of the method to invoke
     * @throws RuntimeException if the method does not exist or invocation fails
     */
    public static void callMethod(@NonNull Object bean, @NonNull String methodName) {
        try {
            Method method = findMethod(bean.getClass(), methodName);
            method.setAccessible(true);
            method.invoke(bean);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            } else {
                throw new RuntimeException(e.getCause());
            }
        }
    }

    /**
     * Finds a public constructor matching the given parameter types.
     * Supports primitive/wrapper compatibility when comparing parameter types.
     *
     * @param clazz the class to search for a matching constructor
     * @param types the expected parameter types of the constructor
     * @return an {@link Optional} containing the matching constructor, or empty if none found
     */
    public static Optional<Constructor<?>> findConstructor(@NonNull Class<?> clazz, @NonNull Class<?>[] types) {
        final Constructor<?>[] constructors = clazz.getConstructors();

        outer:
        for (Constructor<?> constructor : constructors) {
            final Class<?>[] constructorParameterTypes = constructor.getParameterTypes();

            if (constructorParameterTypes.length != types.length) {
                continue;
            }

            for (int j = 0; j < constructorParameterTypes.length; j++) {
                if (!isAssignable(constructorParameterTypes[j], types[j])) {
                    continue outer;
                }
            }

            return Optional.of(constructor);
        }

        return Optional.empty();
    }

    /**
     * Constructs a new instance by finding a matching public constructor and invoking it
     * with the given values. The constructor is matched using {@link #findConstructor(Class, Class[])}.
     *
     * @param clazz  the class to instantiate
     * @param types  the parameter types used to locate the constructor
     * @param values the argument values to pass to the constructor
     * @param <T>    the type of the instance
     * @return a new instance of the given class
     * @throws IllegalArgumentException if {@code types} and {@code values} have different lengths,
     *                                  or if no matching constructor is found
     * @throws RuntimeException         if constructor invocation fails
     */
    public static <T> T tryConstruct(@NonNull Class<T> clazz, @NonNull Class<?>[] types, @NonNull Object[] values) {
        if (types.length != values.length) {
            throw new IllegalArgumentException("Length must be equal");
        }

        final Optional<Constructor<?>> constructorOptional = findConstructor(clazz, types);
        if (constructorOptional.isEmpty()) {
            final String typeNames = Stream.of(types).map(Class::getName).collect(Collectors.joining(", "));
            throw new IllegalArgumentException("No suitable constructor found in " + clazz.getName() + ". Parameter types: " + typeNames);
        }

        try {
            return clazz.cast(constructorOptional.get().newInstance(values));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private static Method findMethod(Class<?> clazz, String methodName) throws NoSuchMethodException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredMethod(methodName);
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchMethodException(clazz.getName() + "." + methodName + "()");
    }

    private static boolean isAssignable(Class<?> aClass, Class<?> bClass) {
        if (aClass.isPrimitive() == bClass.isPrimitive()) {
            return aClass.isAssignableFrom(bClass);
        } else if (aClass.isPrimitive()) {
            return getWrapperType(aClass).isAssignableFrom(bClass);
        } else {
            return aClass.isAssignableFrom(getWrapperType(bClass));
        }
    }

    private static Class<?> getWrapperType(Class<?> primitiveType) {
        if (primitiveType == int.class) return Integer.class;
        if (primitiveType == long.class) return Long.class;
        if (primitiveType == double.class) return Double.class;
        if (primitiveType == float.class) return Float.class;
        if (primitiveType == boolean.class) return Boolean.class;
        if (primitiveType == char.class) return Character.class;
        if (primitiveType == byte.class) return Byte.class;
        if (primitiveType == short.class) return Short.class;
        throw new IllegalArgumentException("Unsupported primitive type: " + primitiveType);
    }
}
