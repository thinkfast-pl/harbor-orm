// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.repository.set_one_to_many;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity(table = "transaction")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TransactionEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(joinColumns = @JoinColumn(
            name = "transaction_id",
            fieldType = Long.class
    ))
    private List<TransactionNodeEntity> nodes = new ArrayList<>();
}
