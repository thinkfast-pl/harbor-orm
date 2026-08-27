// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.repository.set2;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Convert;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.*;

@Entity(table = "basics_converted")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BasicConvertedEntity {

    @Id
    private Long id;

    @Column(name = "bool_str_value", nullable = false)
    @Convert(converter = BooleanToStringConverter.class)
    @Setter
    private boolean value;
}
