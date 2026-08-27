// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.source.processor;

import io.github.thinkfastpl.harbororm.modelgen.source.SourceClassField;
import io.github.thinkfastpl.harbororm.modelgen.source.SourceClassMethod;
import io.github.thinkfastpl.harbororm.modelgen.source.SourceTypeConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ProcessorSourceTypeConstruct implements SourceTypeConstruct {

    @NonNull
    private final ProcessingEnvironment processingEnv;

    @NonNull
    private final TypeElement element;

    @Override
    public String getSimpleClassName() {
        return ProcessorSourceHelper.getSimpleClassNameWithClassParentsUnderscore(element);
    }

    @Override
    public String getSimpleClassNameWithClassParents() {
        return ProcessorSourceHelper.getSimpleClassNameWithClassParentsDot(element);
    }

    @Override
    public String getPackageName() {
        return ProcessorSourceHelper.getPackageName(element);
    }

    @Override
    public boolean isPublicClass() {
        return element.getModifiers().contains(Modifier.PUBLIC);
    }

    @Override
    public <A extends Annotation> Optional<A> getAnnotation(@NonNull Class<A> aClass) {
        return Optional.ofNullable(element.getAnnotation(aClass));
    }

    @Override
    public List<SourceClassField> getFields() {
        return element.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.FIELD && el instanceof VariableElement)
                .<SourceClassField>map(el -> new ProcessorSourceClassField(processingEnv, (VariableElement) el))
                .toList();
    }

    @Override
    public List<SourceClassMethod> getMethods() {
        return element.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.METHOD && el instanceof ExecutableElement)
                .<SourceClassMethod>map(el -> new ProcessorSourceClassMethod((ExecutableElement) el))
                .toList();
    }
}
