// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.repository.set6;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.*;

import java.util.List;

@Entity(table = "invoice")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class InvoiceEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    @Setter
    private String name;

    @ElementCollection(
            table = "invoice_entry",
            joinColumns = @JoinColumn(
                    name = "invoice_id",
                    fieldType = Long.class
            )
    )
    @Setter
    private List<@Embedded InvoiceEntry> entries;
}
