// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.repository.set_inner_embeddable;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Entity(table = "basics_comp_id2")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BasicCompIdEntity2 {

    @Embeddable
    @Getter
    @FieldNameConstants
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompId {

        public enum Type {
            A,
            B
        }

        @Column(nullable = false)
        @Enumerated
        private Type type;

        @Column(nullable = false)
        private int num;
    }

    @Id
    @Embedded
    private CompId id;

    @Column(nullable = false)
    private String value;
}
