// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.metadata;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import io.github.thinkfastpl.harbororm.modelgen.source.SourceClassMethod;

import java.lang.annotation.Annotation;
import java.util.List;

public record LifecycleMethodNames(
        String preInsertMethodName,
        String preUpdateMethodName,
        String preDeleteMethodName,
        String postInsertMethodName,
        String postUpdateMethodName,
        String postDeleteMethodName
) {
    public static LifecycleMethodNames extract(List<? extends SourceClassMethod> methods) {
        return new LifecycleMethodNames(
                findMethodName(methods, PreInsert.class),
                findMethodName(methods, PreUpdate.class),
                findMethodName(methods, PreDelete.class),
                findMethodName(methods, PostInsert.class),
                findMethodName(methods, PostUpdate.class),
                findMethodName(methods, PostDelete.class)
        );
    }

    private static <A extends Annotation> String findMethodName(List<? extends SourceClassMethod> methods, Class<A> annotationClass) {
        return methods.stream()
                .filter(f -> f.hasAnnotation(annotationClass))
                .map(SourceClassMethod::getName)
                .findFirst()
                .orElse(null);
    }
}
