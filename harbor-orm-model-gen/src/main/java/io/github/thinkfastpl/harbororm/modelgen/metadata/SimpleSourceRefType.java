// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.metadata;

import io.github.thinkfastpl.harbororm.modelgen.source.SourceRefType;
import io.github.thinkfastpl.harbororm.modelgen.source.SourceTypeConstruct;
import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

/**
 * Minimal SourceRefType backed by a type class name string.
 * Used for join table column generation where only the type name is needed.
 */
record SimpleSourceRefType(@NonNull String typeClassNameForGenericUse) implements SourceRefType {

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public String getFullName() {
        return typeClassNameForGenericUse;
    }

    @Override
    public String getSimpleName() {
        return typeClassNameForGenericUse;
    }

    @Override
    public String getPackageName() {
        return "";
    }

    @Override
    public String getTypeClassNameForGenericUse() {
        return typeClassNameForGenericUse;
    }

    @Override
    public SourceTypeConstruct asTypeConstruct() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<SourceRefType> getGenericTypeArguments() {
        return List.of();
    }

    @Override
    public Optional<SourceRefType> getImplementedClassOrInterface(@NonNull String classOrInterfaceFullName) {
        return Optional.empty();
    }

    @Override
    public <A extends Annotation> boolean hasAnnotation(Class<A> aClass) {
        return false;
    }

    @Override
    public <A extends Annotation> Optional<A> getAnnotation(Class<A> aClass) {
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Annotation> A[] getAnnotations(Class<A> aClass) {
        return (A[]) java.lang.reflect.Array.newInstance(aClass, 0);
    }
}
