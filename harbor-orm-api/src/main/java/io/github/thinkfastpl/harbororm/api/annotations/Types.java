// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Container annotation for multiple {@link Type} annotations.
 *
 * <p>This annotation is used automatically by Java when multiple {@code @Type}
 * annotations are applied to the same field. It can also be used explicitly.
 *
 * @see Type
 */
@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Types {

    /**
     * The array of {@link Type} annotations.
     *
     * @return the type annotations
     */
    Type[] value();
}
