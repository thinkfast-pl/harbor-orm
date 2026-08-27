// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Marks a nested class as a value object within an entity.
 *
 * <p>Value objects are immutable types that represent domain concepts without
 * identity. This annotation is used for nested static classes inside entities
 * that serve as value holders.
 *
 * <p><strong>Note:</strong> This annotation is experimental. For most use cases,
 * prefer {@link Embeddable} which provides full integration with the query API.
 *
 * @see Embeddable
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValueObject {
}
