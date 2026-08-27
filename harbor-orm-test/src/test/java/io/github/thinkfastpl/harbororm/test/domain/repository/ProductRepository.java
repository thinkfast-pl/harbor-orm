// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain.repository;

import io.github.thinkfastpl.harbororm.api.HarborSession;
import io.github.thinkfastpl.harbororm.core.repository.EntityRepository;
import io.github.thinkfastpl.harbororm.test.domain.ProductEntity;
import io.github.thinkfastpl.harbororm.test.domain.QProductEntity;
import lombok.NonNull;

public class ProductRepository extends EntityRepository<ProductEntity, Long> {

    public ProductRepository(@NonNull HarborSession session) {
        super(session, new QProductEntity(null));
    }
}
