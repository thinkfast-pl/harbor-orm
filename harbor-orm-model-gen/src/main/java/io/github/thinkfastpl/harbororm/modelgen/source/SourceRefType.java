// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.source;

import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

public interface SourceRefType {

    boolean isPrimitive();

    boolean isArray();

    String getFullName();

    String getSimpleName();

    String getPackageName();

    default String getNameInPackage(@NonNull String packageName) {
        if (packageName.equals(getPackageName())) {
            return getSimpleName();
        } else {
            return getFullName();
        }
    }

    String getTypeClassNameForGenericUse();

    SourceTypeConstruct asTypeConstruct();

    List<SourceRefType> getGenericTypeArguments();

    Optional<SourceRefType> getImplementedClassOrInterface(@NonNull String classOrInterfaceFullName);

    <A extends Annotation> boolean hasAnnotation(Class<A> aClass);

    <A extends Annotation> Optional<A> getAnnotation(Class<A> aClass);

    <A extends Annotation> A[] getAnnotations(Class<A> aClass);
}
