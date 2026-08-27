// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.repository.oid;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import io.github.thinkfastpl.harbororm.api.lob.PortableClob;
import lombok.*;

@Entity(table = "basics_clob")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BasicsClobEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    @Setter
    private String name;

    @Column(nullable = false)
    @Setter
    private PortableClob data;
}
