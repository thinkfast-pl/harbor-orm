// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.utils;

import io.github.thinkfastpl.harbororm.modelgen.source.SourceRefType;
import io.github.thinkfastpl.harbororm.modelgen.source.processor.ProcessorSourceRefType;
import lombok.NonNull;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.util.function.Supplier;

public class AnnotationClassValueAccessor {

    public static SourceRefType access(@NonNull ProcessingEnvironment processingEnvironment, @NonNull Supplier<Class<?>> supplier) {
        try {
            Object result = supplier.get();
            if (result instanceof TypeMirror typeMirror) {
                return new ProcessorSourceRefType(processingEnvironment, typeMirror);
            }
            throw new IllegalStateException("Annotation Class value was not accessed");
        } catch (MirroredTypeException mte) {
            return new ProcessorSourceRefType(processingEnvironment, mte.getTypeMirror());
        }
    }
}
