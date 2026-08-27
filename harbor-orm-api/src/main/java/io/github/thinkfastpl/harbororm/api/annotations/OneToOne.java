// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.annotations;

import java.lang.annotation.*;

/**
 * Defines a one-to-one relationship between entities.
 *
 * <p>The annotated field must be of type {@link io.github.thinkfastpl.harbororm.api.LazyRef} parameterized
 * with the related entity type. The related entity is loaded lazily on first access,
 * with batch loading for efficiency (up to 50 parents per query).
 *
 * <p>The parent entity owns the child's lifecycle (DDD aggregate semantics):
 * <ul>
 *   <li>Inserting the parent cascades to insert the child</li>
 *   <li>Updating the parent cascades to update/replace/delete the child</li>
 *   <li>Deleting the parent cascades to delete the child first</li>
 * </ul>
 *
 * <p>At most one child entity may exist per parent. If multiple rows are found,
 * a runtime exception is thrown. Enforce this with a UNIQUE constraint on the
 * foreign key column in the child table.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @Entity(table = "users")
 * public class UserEntity {
 *     @Id
 *     private Long id;
 *
 *     @OneToOne(joinColumns = @JoinColumn(
 *         name = "user_id",
 *         fieldType = Long.class
 *     ))
 *     private LazyRef<ProfileEntity> profile;
 * }
 * }</pre>
 *
 * @see JoinColumn
 * @see io.github.thinkfastpl.harbororm.api.LazyRef
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OneToOne {

    /**
     * The join column(s) that define the foreign key relationship in the child table.
     *
     * @return the join columns
     */
    JoinColumn[] joinColumns() default {};
}
