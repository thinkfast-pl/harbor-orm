// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Convert;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Entity(table = "basic")
@Getter
@AllArgsConstructor
public class BasicPublicEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    @Convert(converter = BooleanToStringConverter.class)
    private boolean boolAsString;
}
