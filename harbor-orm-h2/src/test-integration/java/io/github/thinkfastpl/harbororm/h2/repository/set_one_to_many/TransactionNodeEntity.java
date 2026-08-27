// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.repository.set_one_to_many;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.*;

@Entity(table = "transaction_node")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TransactionNodeEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;
}
