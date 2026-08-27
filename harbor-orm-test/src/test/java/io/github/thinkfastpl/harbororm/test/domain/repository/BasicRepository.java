// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain.repository;

import io.github.thinkfastpl.harbororm.api.HarborSession;
import io.github.thinkfastpl.harbororm.core.repository.EntityRepository;
import io.github.thinkfastpl.harbororm.test.domain.BasicEntity;
import io.github.thinkfastpl.harbororm.test.domain.QBasicEntity;
import lombok.NonNull;

public class BasicRepository extends EntityRepository<BasicEntity, Long> {

    public BasicRepository(@NonNull HarborSession session) {
        super(session, new QBasicEntity(null));
    }
}
