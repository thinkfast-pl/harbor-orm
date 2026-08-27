// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.source.processor;

import io.github.thinkfastpl.harbororm.modelgen.source.SourceRefType;
import io.github.thinkfastpl.harbororm.modelgen.source.SourceTypeConstruct;
import io.github.thinkfastpl.harbororm.modelgen.utils.AnnotationProcessorUtils;
import lombok.NonNull;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ProcessorSourceRefType implements SourceRefType {
    private final ProcessingEnvironment processingEnv;
    private final TypeMirror typeMirror;
    private final TypeMirror componentTypeMirror;
    private final SourceTypeConstruct typeConstruct;

    public ProcessorSourceRefType(@NonNull ProcessingEnvironment processingEnvironment, @NonNull TypeMirror typeMirror) {
        if (!typeMirror.getKind().isPrimitive() && typeMirror.getKind() != TypeKind.DECLARED && typeMirror.getKind() != TypeKind.ARRAY) {
            throw new IllegalArgumentException("Only primitives and declared types are supported " + typeMirror);
        }

        this.processingEnv = processingEnvironment;
        this.typeMirror = typeMirror;

        if (typeMirror instanceof DeclaredType declaredType && declaredType.asElement() instanceof TypeElement typeElement) {
            this.typeConstruct = new ProcessorSourceTypeConstruct(processingEnv, typeElement);
            this.componentTypeMirror = null;
        } else if (typeMirror instanceof ArrayType arrayType) {
            this.componentTypeMirror = arrayType.getComponentType();
            if (this.componentTypeMirror instanceof DeclaredType declaredType && declaredType.asElement() instanceof TypeElement typeElement) {
                this.typeConstruct = new ProcessorSourceTypeConstruct(processingEnv, typeElement);
            } else {
                this.typeConstruct = null;
            }
        } else {
            this.typeConstruct = null;
            this.componentTypeMirror = null;
        }
    }

    @Override
    public boolean isPrimitive() {
        return this.typeMirror.getKind().isPrimitive();
    }

    @Override
    public boolean isArray() {
        return this.typeMirror.getKind() == TypeKind.ARRAY;
    }

    @Override
    public String getFullName() {
        if (isPrimitive()) {
            // typeMirror.toString() renders the field's declaration annotations for primitives,
            // e.g. "@io.github.thinkfastpl.harbororm.api.annotations.Column(nullable=false) long"
            return ProcessorSourceHelper.getBasicTypeName(typeMirror);
        } else if (isArray()) {
            return typeMirror.toString();
        } else if (typeMirror instanceof DeclaredType declaredType) {
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            return typeElement.getQualifiedName().toString();
        } else {
            return typeMirror.toString();
        }
    }

    @Override
    public String getSimpleName() {
        if (typeMirror instanceof DeclaredType declaredType) {
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            return typeElement.getSimpleName().toString();
        }
        return typeMirror.toString();
    }

    @Override
    public String getPackageName() {
        return ProcessorSourceHelper.getPackageName(typeMirror);
    }

    @Override
    public String getTypeClassNameForGenericUse() {
        return ProcessorSourceHelper.getTypeClassNameForGenericUse(typeMirror);
    }

    @Override
    public SourceTypeConstruct asTypeConstruct() {
        if (typeConstruct == null) {
            throw new IllegalStateException("This is a primitive type");
        }
        return typeConstruct;
    }

    @Override
    public List<SourceRefType> getGenericTypeArguments() {
        if (typeMirror instanceof DeclaredType declaredType) {
            return declaredType.getTypeArguments().stream()
                    .<SourceRefType>map(tm -> new ProcessorSourceRefType(processingEnv, tm))
                    .toList();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<SourceRefType> getImplementedClassOrInterface(@NonNull String classOrInterfaceFullName) {
        return AnnotationProcessorUtils.searchSupertype(processingEnv, typeMirror, classOrInterfaceFullName);
    }

    @Override
    public <A extends Annotation> boolean hasAnnotation(@NonNull Class<A> aClass) {
        return typeMirror.getAnnotation(aClass) != null;
    }

    @Override
    public <A extends Annotation> Optional<A> getAnnotation(@NonNull Class<A> aClass) {
        List<? extends AnnotationMirror> annotationMirrors = typeMirror.getAnnotationMirrors();
        return Optional.ofNullable(AnnotationProcessorUtils.getAnnotationFromMirrors(aClass, annotationMirrors));
    }

    @Override
    public <A extends Annotation> A[] getAnnotations(Class<A> aClass) {
        List<? extends AnnotationMirror> annotationMirrors = typeMirror.getAnnotationMirrors();
        return AnnotationProcessorUtils.getAnnotationsFromMirrors(aClass, annotationMirrors);
    }
}
