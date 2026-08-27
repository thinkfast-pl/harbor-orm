// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.interpreter;

import io.github.thinkfastpl.harbororm.api.annotations.TypeHandler;
import io.github.thinkfastpl.harbororm.api.annotations.TypeHandlers;
import io.github.thinkfastpl.harbororm.modelgen.metadata.TypeHandlerMetadata;
import io.github.thinkfastpl.harbororm.modelgen.source.SourceClassField;
import io.github.thinkfastpl.harbororm.modelgen.utils.AnnotationClassValueAccessor;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds {@link TypeHandlerMetadata} from the (repeatable) {@link TypeHandler @TypeHandler}
 * annotations declared on a field.
 */
public final class TypeHandlerMetadataFactory {

    private TypeHandlerMetadataFactory() {
    }

    /**
     * Resolves the type handlers declared on {@code field}.
     *
     * @return the type handler metadata, or {@code null} when the field declares no {@code @TypeHandler}
     */
    public static TypeHandlerMetadata create(ProcessingEnvironment processingEnv, SourceClassField field) {
        TypeHandler[] typeHandlers = field.getAnnotation(TypeHandlers.class)
                .map(TypeHandlers::value)
                .orElse(field.getAnnotations(TypeHandler.class));
        return create(processingEnv, typeHandlers);
    }

    /**
     * Resolves the given type handler annotations.
     *
     * @return the type handler metadata keyed by dialect, or {@code null} when {@code typeHandlers} is empty
     */
    public static TypeHandlerMetadata create(ProcessingEnvironment processingEnv, TypeHandler[] typeHandlers) {
        if (typeHandlers == null || typeHandlers.length == 0) {
            return null;
        }

        Map<String, String> res = new HashMap<>();
        for (TypeHandler th : typeHandlers) {
            res.put(th.dialect(), AnnotationClassValueAccessor.access(processingEnv, th::value).getFullName());
        }
        return new TypeHandlerMetadata(res);
    }
}
