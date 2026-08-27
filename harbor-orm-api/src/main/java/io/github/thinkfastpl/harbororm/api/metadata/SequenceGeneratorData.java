// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.metadata;

/**
 * Record holding the database sequence name for columns annotated with {@code @SequenceGenerated}.
 *
 * <p>The sequence is called before each insertion and the generated value is set on the entity's ID field.
 *
 * @param sequenceName the database sequence name
 * @see QColumn#isSequenceGenerated()
 * @see QColumn#getSequenceGeneratorMetadata()
 */
public record SequenceGeneratorData(
        String sequenceName
) {

}
