// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class LargeEmbeddable {

    @Column(nullable = false)
    private String field1;

    @Column(nullable = false)
    private String field2;

    @Column(nullable = false)
    private String field3;

    @Column(nullable = false)
    private String field4;

    @Column(nullable = false)
    private String field5;

    @Column(nullable = false)
    private String field6;

    @Column(nullable = false)
    private String field7;

    @Column(nullable = false)
    private String field8;

    @Column(nullable = false)
    private String field9;

    @Column(nullable = false)
    private String field10;

    @Column(nullable = false)
    private String field11;
}
