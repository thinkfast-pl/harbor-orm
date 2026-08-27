// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain.repository;

import io.github.thinkfastpl.harbororm.api.HarborSession;
import io.github.thinkfastpl.harbororm.core.repository.EntityRepository;
import io.github.thinkfastpl.harbororm.test.domain.QUserEntity;
import io.github.thinkfastpl.harbororm.test.domain.UserEntity;
import lombok.NonNull;

import java.util.UUID;

public class UserRepository extends EntityRepository<UserEntity, UUID> {

    public UserRepository(@NonNull HarborSession session) {
        super(session, new QUserEntity(null));
    }
}
