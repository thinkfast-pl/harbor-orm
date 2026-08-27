// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Convert;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import io.github.thinkfastpl.harbororm.test.domain.converter.BooleanToYesNoConverter;
import io.github.thinkfastpl.harbororm.test.domain.converter.JsonConverter;
import lombok.*;

/**
 * Entity with multiple @Convert fields for testing converter functionality.
 */
@Entity(table = "converted_fields")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ConvertedFieldsEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "active_flag", nullable = false)
    @Convert(converter = BooleanToYesNoConverter.class)
    private Boolean active;

    @Column(name = "verified_flag", nullable = false)
    @Convert(converter = BooleanToYesNoConverter.class)
    private Boolean verified;

    @Column(name = "metadata_json", nullable = false)
    @Convert(converter = JsonConverter.class)
    private JsonMetadata metadata;
}
