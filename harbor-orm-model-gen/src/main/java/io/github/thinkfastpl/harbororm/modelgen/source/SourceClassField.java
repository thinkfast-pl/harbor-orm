// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.source;

import java.lang.annotation.Annotation;
import java.util.Optional;

public interface SourceClassField {

    String getName();

    SourceRefType getType();

    <A extends Annotation> boolean hasAnnotation(Class<A> aClass);

    <A extends Annotation> Optional<A> getAnnotation(Class<A> aClass);

    <A extends Annotation> A[] getAnnotations(Class<A> aClass);
}
