// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Container for repeatable {@link TypeHandler} annotations.
 *
 * @see TypeHandler
 */
@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TypeHandlers {
    TypeHandler[] value();
}
