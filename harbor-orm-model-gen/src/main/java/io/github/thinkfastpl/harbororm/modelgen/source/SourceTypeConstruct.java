// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.source;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

public interface SourceTypeConstruct {

    String getSimpleClassName();

    String getSimpleClassNameWithClassParents();

    String getPackageName();

    boolean isPublicClass();

    default String getFullClassName() {
        return getPackageName() + "." + getSimpleClassNameWithClassParents();
    }

    <A extends Annotation> Optional<A> getAnnotation(Class<A> aClass);

    List<SourceClassField> getFields();

    List<SourceClassMethod> getMethods();
}
