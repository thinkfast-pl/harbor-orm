// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.utils;

import io.github.thinkfastpl.harbororm.modelgen.source.SourceRefType;
import io.github.thinkfastpl.harbororm.modelgen.source.processor.ProcessorSourceRefType;
import lombok.NonNull;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.util.*;

public class AnnotationProcessorUtils {

    public static Optional<SourceRefType> searchSupertype(@NonNull ProcessingEnvironment processingEnv, @NonNull TypeMirror typeMirror, @NonNull String classOrInterfaceFullName) {
        try {
            List<? extends TypeMirror> typeMirrors = processingEnv.getTypeUtils().directSupertypes(typeMirror);
            for (TypeMirror supertypeTypeMirror : typeMirrors) {
                SourceRefType sourceRefType = new ProcessorSourceRefType(processingEnv, supertypeTypeMirror);
                if (sourceRefType.getFullName().equals(classOrInterfaceFullName)) {
                    return Optional.of(sourceRefType);
                }
            }

            for (TypeMirror supertypeTypeMirror : typeMirrors) {
                Optional<SourceRefType> sourceRefTypeInImplemented = searchSupertype(processingEnv, supertypeTypeMirror, classOrInterfaceFullName);
                if (sourceRefTypeInImplemented.isPresent()) {
                    return sourceRefTypeInImplemented;
                }
            }

            return Optional.empty();
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A[] getAnnotationsFromMirrors(Class<A> aClass, List<? extends AnnotationMirror> annotationMirrors) {
        return annotationMirrors.stream()
                .map(am -> getAnnotationFromMirrors(aClass, List.of(am)))
                .filter(Objects::nonNull)
                .toArray(length -> (A[]) Array.newInstance(aClass, length));
    }

    public static <A extends Annotation> A getAnnotationFromMirrors(Class<A> aClass, List<? extends AnnotationMirror> annotationMirrors) {
        Optional<? extends AnnotationMirror> mirrorOptional = annotationMirrors.stream()
                .filter(annotationMirror -> annotationMirror.getAnnotationType().toString().equals(aClass.getName()))
                .findFirst();

        if (mirrorOptional.isPresent()) {
            AnnotationMirror annotationMirror = mirrorOptional.get();

            Map<String, Object> elementValues = new HashMap<>();
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
                elementValues.put(entry.getKey().toString(), entry.getValue().getValue());
            }

            Object aProxy = Proxy.newProxyInstance(
                    aClass.getClassLoader(),
                    new Class[]{aClass},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "hashCode" -> elementValues.hashCode();
                        case "equals" -> proxy == args[0];
                        case "toString" -> elementValues.toString();
                        default -> {
                            String key = method.getName() + "()";
                            if (elementValues.containsKey(key)) {
                                final Object elementValue = elementValues.get(key);
                                if (method.getReturnType().isArray() && elementValue instanceof List<?> elementValueAsList) {
                                    yield convertToArray(elementValueAsList, method.getReturnType());
                                } else if (elementValue instanceof VariableElement variableElement && method.getReturnType().isEnum()) {
                                    yield Enum.valueOf((Class<Enum>) method.getReturnType(), variableElement.getSimpleName().toString());
                                } else if (elementValue instanceof VariableElement variableElement && variableElement.getConstantValue() != null) {
                                    yield variableElement.getConstantValue();
                                } else if (Class.class.isAssignableFrom(method.getReturnType()) && elementValue instanceof TypeMirror typeMirror) {
                                    throw new MirroredTypeException(typeMirror);
                                } else {
                                    yield elementValue;
                                }
                            } else {
                                yield method.getDefaultValue();
                            }
                        }
                    }
            );

            return aClass.cast(aProxy);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static Object convertToArray(List<?> objects, Class<?> targetArrayClass) {
        if (objects == null) {
            return null;
        }

        Class<?> componentType = targetArrayClass.getComponentType();
        Object[] result = (Object[]) Array.newInstance(componentType, objects.size());

        int i = 0;
        for (Object object : objects) {
            if (object == null) {
                result[i] = null;
            } else if (componentType.isAnnotation() && object instanceof AnnotationMirror objectAsAnnotationMirror) {
                result[i] = getAnnotationFromMirrors((Class<? extends Annotation>) componentType, List.of(objectAsAnnotationMirror));
            } else {
                result[i] = componentType.cast(object);
            }

            i++;
        }

        return result;
    }
}
