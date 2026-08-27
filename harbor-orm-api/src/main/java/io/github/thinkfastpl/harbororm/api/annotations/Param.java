// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a parameter of a {@link StoredFunction}.
 * Used inside {@code @StoredFunction(params = {...})}.
 */
@Target({})
@Retention(RetentionPolicy.SOURCE)
public @interface Param {
    /** The parameter name (used as the Java method parameter name in generated code). */
    String name();

    /** The Java type of the parameter. */
    Class<?> type();
}
