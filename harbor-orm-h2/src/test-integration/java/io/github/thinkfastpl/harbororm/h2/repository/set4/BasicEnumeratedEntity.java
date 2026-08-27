// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.repository.set4;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Enumerated;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import io.github.thinkfastpl.harbororm.api.metadata.EnumMappingType;
import lombok.*;

@Entity(table = "basics_enumerated")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BasicEnumeratedEntity {

    public enum State {
        ACTIVE,
        INACTIVE,
    }

    public enum Type {
        FIRST,
        SECOND,
    }

    @Id
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumMappingType.STRING)
    private State state1;

    @Column(nullable = false)
    @Enumerated
    private State state2;

    @Column(nullable = false)
    @Enumerated(EnumMappingType.ORDINAL)
    private Type type;
}
