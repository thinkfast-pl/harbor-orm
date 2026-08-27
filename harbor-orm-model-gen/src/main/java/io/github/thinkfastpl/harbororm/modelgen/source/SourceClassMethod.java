// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.source;

import java.lang.annotation.Annotation;
import java.util.Optional;

public interface SourceClassMethod {

    String getName();

    <A extends Annotation> boolean hasAnnotation(Class<A> aClass);

    <A extends Annotation> Optional<A> getAnnotation(Class<A> aClass);
}
