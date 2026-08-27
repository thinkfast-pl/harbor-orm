// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.source.processor;

import io.github.thinkfastpl.harbororm.modelgen.source.SourceClassMethod;
import lombok.NonNull;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;
import java.util.Optional;

public class ProcessorSourceClassMethod implements SourceClassMethod {
    private final ExecutableElement element;

    public ProcessorSourceClassMethod(@NonNull ExecutableElement element) {
        if (element.getKind() != ElementKind.METHOD) {
            throw new IllegalArgumentException("element is not method");
        }
        this.element = element;
    }

    @Override
    public String getName() {
        return element.getSimpleName().toString();
    }

    @Override
    public <A extends Annotation> boolean hasAnnotation(Class<A> aClass) {
        return element.getAnnotation(aClass) != null;
    }

    @Override
    public <A extends Annotation> Optional<A> getAnnotation(Class<A> aClass) {
        return Optional.ofNullable(element.getAnnotation(aClass));
    }
}
