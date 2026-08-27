// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain.repository;

import io.github.thinkfastpl.harbororm.api.HarborSession;
import io.github.thinkfastpl.harbororm.core.repository.EntityRepository;
import io.github.thinkfastpl.harbororm.test.domain.BasicsByteaEntity;
import io.github.thinkfastpl.harbororm.test.domain.QBasicsByteaEntity;
import lombok.NonNull;

public class BasicsByteaRepository extends EntityRepository<BasicsByteaEntity, Long> {

    public BasicsByteaRepository(@NonNull HarborSession session) {
        super(session, new QBasicsByteaEntity(null));
    }
}
