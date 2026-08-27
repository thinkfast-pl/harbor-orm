// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.Getter;

@Entity(table = "ewle")
@Getter
class EntityWithLargeEmbedded {

    @Id
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "field1", column = @Column(name = "col_1", nullable = false)),
            @AttributeOverride(name = "field2", column = @Column(name = "col_2", nullable = false)),
            @AttributeOverride(name = "field3", column = @Column(name = "col_3", nullable = false)),
            @AttributeOverride(name = "field4", column = @Column(name = "col_4", nullable = false)),
            @AttributeOverride(name = "field5", column = @Column(name = "col_5", nullable = false)),
            @AttributeOverride(name = "field6", column = @Column(name = "col_6", nullable = false)),
            @AttributeOverride(name = "field7", column = @Column(name = "col_7", nullable = false)),
            @AttributeOverride(name = "field8", column = @Column(name = "col_8", nullable = false)),
            @AttributeOverride(name = "field9", column = @Column(name = "col_9", nullable = false)),
            @AttributeOverride(name = "field10", column = @Column(name = "col_10", nullable = false)),
            @AttributeOverride(name = "field11", column = @Column(name = "col_11", nullable = false))
    })
    private LargeEmbeddable data;
}
