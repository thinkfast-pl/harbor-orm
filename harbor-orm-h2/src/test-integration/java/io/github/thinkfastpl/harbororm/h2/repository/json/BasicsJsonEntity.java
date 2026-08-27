// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.repository.json;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import io.github.thinkfastpl.harbororm.api.annotations.Json;
import lombok.*;

@Entity(table = "basics_json")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BasicsJsonEntity {

    @Id
    private Long id;

    /** Stored in a native H2 {@code json} column. */
    @Json
    @Column(name = "json_data", nullable = false)
    @Setter
    private String jsonData;

    /** Stored in a plain {@code varchar} column. */
    @Json
    @Column(name = "varchar_data", nullable = false)
    @Setter
    private String varcharData;
}
