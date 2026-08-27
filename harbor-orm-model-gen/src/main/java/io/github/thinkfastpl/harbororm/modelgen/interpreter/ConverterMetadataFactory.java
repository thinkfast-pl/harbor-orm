// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.interpreter;

import io.github.thinkfastpl.harbororm.api.annotations.Convert;
import io.github.thinkfastpl.harbororm.api.converter.AttributeConverter;
import io.github.thinkfastpl.harbororm.modelgen.metadata.ConverterMetadata;
import io.github.thinkfastpl.harbororm.modelgen.source.SourceRefType;
import io.github.thinkfastpl.harbororm.modelgen.utils.AnnotationClassValueAccessor;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.List;

/**
 * Builds {@link ConverterMetadata} from a {@link Convert @Convert} annotation by resolving the
 * converter class and the generic type arguments of its
 * {@link AttributeConverter} implementation.
 */
public final class ConverterMetadataFactory {

    private ConverterMetadataFactory() {
    }

    /**
     * Resolves the converter declared by {@code convertAnnotation}.
     *
     * @return the converter metadata, or {@code null} when {@code convertAnnotation} is {@code null}
     * @throws IllegalStateException when the converter class does not implement
     *         {@link AttributeConverter} with exactly two generic type arguments
     */
    public static ConverterMetadata create(ProcessingEnvironment processingEnv, Convert convertAnnotation) {
        if (convertAnnotation == null) {
            return null;
        }

        SourceRefType refType = AnnotationClassValueAccessor.access(processingEnv, convertAnnotation::converter);
        SourceRefType converterType = refType.getImplementedClassOrInterface(AttributeConverter.class.getName())
                .orElseThrow(() -> new IllegalStateException("Cannot find AttributeConverter implementation"));

        List<SourceRefType> genericTypeArguments = converterType.getGenericTypeArguments();
        if (genericTypeArguments.size() != 2) {
            throw new IllegalStateException("Expected 2 generic types but got: " + genericTypeArguments);
        }

        return new ConverterMetadata(
                refType.getFullName(),
                genericTypeArguments.get(0).getTypeClassNameForGenericUse(),
                genericTypeArguments.get(1).getTypeClassNameForGenericUse()
        );
    }
}
