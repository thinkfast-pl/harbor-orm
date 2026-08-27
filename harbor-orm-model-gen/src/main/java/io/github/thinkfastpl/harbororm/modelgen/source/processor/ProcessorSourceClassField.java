// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.source.processor;

import io.github.thinkfastpl.harbororm.modelgen.source.SourceClassField;
import io.github.thinkfastpl.harbororm.modelgen.source.SourceRefType;
import lombok.NonNull;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;
import java.util.Optional;

public class ProcessorSourceClassField implements SourceClassField {
    private final ProcessingEnvironment processingEnv;
    private final VariableElement element;
    private final SourceRefType refType;

    public ProcessorSourceClassField(@NonNull ProcessingEnvironment processingEnv, @NonNull VariableElement element) {
        if (element.getKind() != ElementKind.FIELD) {
            throw new IllegalArgumentException("element is not field");
        }
        this.processingEnv = processingEnv;
        this.element = element;
        this.refType = new ProcessorSourceRefType(processingEnv, element.asType());
    }

    @Override
    public String getName() {
        return element.getSimpleName().toString();
    }

    @Override
    public SourceRefType getType() {
        return refType;
    }

    @Override
    public <A extends Annotation> boolean hasAnnotation(@NonNull Class<A> aClass) {
        return element.getAnnotation(aClass) != null;
    }

    @Override
    public <A extends Annotation> Optional<A> getAnnotation(@NonNull Class<A> aClass) {
        return Optional.ofNullable(element.getAnnotation(aClass));
    }

    @Override
    public <A extends Annotation> A[] getAnnotations(Class<A> aClass) {
        return element.getAnnotationsByType(aClass);
    }
}
