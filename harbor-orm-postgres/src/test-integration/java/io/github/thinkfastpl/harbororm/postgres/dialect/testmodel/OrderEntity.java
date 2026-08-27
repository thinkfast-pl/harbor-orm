// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect.testmodel;

import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.Getter;

@Entity(table = "orders")
@Getter
public class OrderEntity {

    @Id
    private Long id;


}
